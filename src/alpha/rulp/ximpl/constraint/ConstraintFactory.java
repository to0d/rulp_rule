package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.A_EXPRESSION;
import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.lang.Constant.O_Nil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRFrameEntry;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;

public class ConstraintFactory {

	protected static IRConstraint1 _rebuildExprConstraint(IRReteNode node, IRConstraint1Expr constraint)
			throws RException {

		IRExpr expr = constraint.getExpr();
		RRelationalOperator op = null;

		if ((op = ReteUtil.toRelationalOperator(expr.get(0).asString())) != null && ReteUtil.getExprLevel(expr) == 1
				&& expr.size() == 3 && constraint instanceof XRConstraint1Expr0X) {

			XRConstraint1Expr0X consExprX = (XRConstraint1Expr0X) constraint;
			if (consExprX.getConstraintIndex().length == 1 && consExprX.getExternalVarCount() == 0) {

				int constraintIndex = consExprX.getConstraintIndex()[0];
				String constraintIndexName = String.format("?%d", constraintIndex);

				// (op ?0 value)
				if (expr.get(1).asString().equals(constraintIndexName)) {
					return ConstraintFactory.cmpEntryValue(op, constraintIndex, expr.get(2));
				}

				// (op value ?0)
				if (expr.get(2).asString().equals(constraintIndexName)) {
					return ConstraintFactory.cmpEntryValue(RRelationalOperator.oppositeOf(op), constraintIndex,
							expr.get(1));
				}
			}

			// (!= ?0 ?1)
			if (consExprX.getConstraintIndex().length == 2 && consExprX.getExternalVarCount() == 0) {
				return ConstraintFactory.cmpEntryIndex(op, consExprX.getConstraintIndex()[0],
						consExprX.getConstraintIndex()[1]);
			}
		}

		return constraint;
	}

	public static IRConstraint1 cmpEntryIndex(RRelationalOperator op, int idx1, int idx2) {

		if (idx1 > idx2) {
			return new XRConstraint1CompareEntryIndex(RRelationalOperator.oppositeOf(op), idx2, idx1);
		} else {
			return new XRConstraint1CompareEntryIndex(op, idx1, idx2);
		}
	}

	public static IRConstraint1 cmpEntryValue(RRelationalOperator op, int index, IRObject obj) {

		if (obj.getType() == RType.ATOM && obj.asString().equals(A_NIL)) {
			obj = O_Nil;
		}

		return new XRConstraint1CompareEntryValue(op, index, obj);
	}

	public static IRConstraint1 cmpEntryVar(RRelationalOperator op, int index, IRObject varObj, IRFrame frame)
			throws RException {

		switch (varObj.getType()) {
		case VAR:
			return cmpEntryVar(op, index, (IRVar) varObj);

		case ATOM:

			IRFrameEntry varEntry = RuntimeUtil.lookupFrameEntry((IRAtom) varObj, frame);
			if (varEntry == null) {
				throw new RException("var not found: " + varObj);
			}

			return cmpEntryVar(op, index, varEntry.getValue(), frame);

		default:
		}

		throw new RException("Invalid var: " + varObj);
	}

	public static IRConstraint1 cmpEntryVar(RRelationalOperator op, int index, IRVar var) {
		return new XRConstraint1CompareEntryVar(op, index, var);
	}

	public static IRConstraint2 entryOrder() {
		return new XRConstraint2EntryOrder();
	}

	public static IRConstraint1 expr0(IRExpr expr, IRObject[] varEntry, IRFrame frame) throws RException {

		int varEntryLen = varEntry.length;

		IRObject[] newVarEntry = new IRObject[varEntryLen];
		Map<String, IRObject> rebuildVarMap = new HashMap<>();
		int externalVarCount = 0;

		for (IRObject var : ReteUtil.buildVarList(expr)) {

			int findIndex = -1;

			FIND: for (int i = 0; i < varEntryLen; ++i) {
				IRObject eVar = varEntry[i];
				if (eVar != null && var.asString().equals(eVar.asString())) {
					findIndex = i;
					break FIND;
				}
			}

			if (findIndex != -1) {
				IRAtom idxVar = RulpFactory.createAtom("?" + findIndex);
				rebuildVarMap.put(var.asString(), idxVar);
				newVarEntry[findIndex] = idxVar;
			} else {
				externalVarCount++;
			}
		}

		if (rebuildVarMap.isEmpty()) {
			return new XRConstraint1Expr0(expr, varEntry);
		}

		int[] constraintIndexs = new int[rebuildVarMap.size()];
		int cIndex = 0;
		for (int i = 0; i < varEntryLen; ++i) {
			if (newVarEntry[i] != null) {
				constraintIndexs[cIndex] = i;
			}
		}

		/*****************************************/
		// Build expr
		/*****************************************/
		expr = RulpUtil.asExpression(RuntimeUtil.rebuild(expr, rebuildVarMap));

		RRelationalOperator op = null;
		if ((op = ReteUtil.toRelationalOperator(expr.get(0).asString())) != null && ReteUtil.getExprLevel(expr) == 1
				&& expr.size() == 3) {

			if (constraintIndexs.length == 1 && externalVarCount == 0) {

				int constraintIndex = constraintIndexs[0];
				String constraintIndexName = String.format("?%d", constraintIndex);

				// (op ?0 value)
				if (expr.get(1).asString().equals(constraintIndexName)) {
					return ConstraintFactory.cmpEntryValue(op, constraintIndex, expr.get(2));
				}

				// (op value ?0)
				if (expr.get(2).asString().equals(constraintIndexName)) {
					return ConstraintFactory.cmpEntryValue(RRelationalOperator.oppositeOf(op), constraintIndex,
							expr.get(1));
				}
			}

			// (!= ?0 ?1)
			if (constraintIndexs.length == 2 && externalVarCount == 0) {
				return ConstraintFactory.cmpEntryIndex(op, constraintIndexs[0], constraintIndexs[1]);
			}

			if (constraintIndexs.length == 1 && externalVarCount == 1) {

				int constraintIndex = constraintIndexs[0];
				String constraintIndexName = String.format("?%d", constraintIndex);

				// (op ?0 value)
				if (expr.get(1).asString().equals(constraintIndexName)) {
					return ConstraintFactory.cmpEntryVar(op, constraintIndex, expr.get(2), frame);
				}

				// (op value ?0)
				if (expr.get(2).asString().equals(constraintIndexName)) {
					return ConstraintFactory.cmpEntryVar(RRelationalOperator.oppositeOf(op), constraintIndex,
							expr.get(1), frame);
				}

			}
		}

		return new XRConstraint1Expr0X(expr, newVarEntry, constraintIndexs, externalVarCount);
	}

	public static IRConstraint1 expr1(IRExpr expr, List<IRObject> leftVarList) throws RException {

		int size = expr.size();

		if (size <= 0) {
			return null;
		}

		IRObject e0 = expr.get(0);
		if (e0.getType() != RType.ATOM) {
			return null;
		}

		if (size != 3) {
			return null;
		}

		RRelationalOperator op = ReteUtil.toRelationalOperator(RulpUtil.asAtom(e0).getName());
		if (op == null) {
			return null;
		}

		for (int i = 0; i < size; ++i) {
			IRObject ei = expr.get(i);
			if (ei.getType() != RType.ATOM) {
				return null;
			}
		}

		IRAtom a1 = RulpUtil.asAtom(expr.get(1));
		IRAtom a2 = RulpUtil.asAtom(expr.get(2));

		int leftVarIndex = -1;
		int rightVarIndex = -1;

		// Find Left index
		if (RulpUtil.isVarAtom(a1)) {
			for (int i = 0; i < leftVarList.size(); ++i) {
				if (leftVarList.get(i) == a1) {
					leftVarIndex = i;
					break;
				}
			}

			if (leftVarIndex == -1) {
				return null;
			}
		}

		// Find Left index
		if (RulpUtil.isVarAtom(a2)) {

			for (int i = 0; i < leftVarList.size(); ++i) {
				if (leftVarList.get(i) == a2) {
					rightVarIndex = i;
					break;
				}
			}

			if (rightVarIndex == -1) {
				return null;
			}
		}

		// (equal a b) or (not-equal a b)
		if (leftVarIndex == -1 && rightVarIndex == -1) {
			throw new RException("Invalid expression, enhancement this later: " + expr);
		}

		// (equal v b)
		if (leftVarIndex != -1 && rightVarIndex == -1) {
			return ConstraintFactory.cmpEntryValue(op, leftVarIndex, a2);
		}

		// (equal a v)
		if (leftVarIndex == -1 && rightVarIndex != -1) {
			return ConstraintFactory.cmpEntryValue(RRelationalOperator.oppositeOf(op), rightVarIndex, a1);
		}

		// (equal v1 v2)
		if (leftVarIndex != -1 && rightVarIndex != -1 && leftVarIndex != rightVarIndex) {
			return ConstraintFactory.cmpEntryIndex(op, leftVarIndex, rightVarIndex);
		}

		return null;
	}

	public static IRConstraint1 expr3(IRExpr expr) throws RException {
		return new XRConstraint1Expr3(expr);
	}

	public static IRConstraint1 expr4(IRExpr expr, List<IRList> matchStmtList) throws RException {
		return new XRConstraint1Expr4(expr, matchStmtList);
	}

	public static IRConstraint1 max(int columnIndex, IRObject maxValue) {
		return new XRConstraint1Max(columnIndex, maxValue);
	}

	public static IRConstraint1 min(int columnIndex, IRObject maxValue) {
		return new XRConstraint1Min(columnIndex, maxValue);
	}

	public static IRConstraint1 oneOf(int index, IRList valueList) {
		return new XRConstraint1OneOf(index, valueList);
	}

	public static IRConstraint1 rebuildConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		IRConstraint1 lastConstraint = constraint;
		while (true) {

			IRConstraint1 newConstraint = lastConstraint;

			switch (constraint.getConstraintName()) {
			case A_EXPRESSION:
				newConstraint = _rebuildExprConstraint(node, (IRConstraint1Expr) lastConstraint);

			default:
			}

			// no change
			if (newConstraint == lastConstraint) {
				break;
			}

			lastConstraint = newConstraint;
		}

		return lastConstraint;
	}

	public static IRConstraint1 type(int columnIndex, RType columnType) {
		return new XRConstraint1Type(columnIndex, columnType);
	}

	public static IRConstraint1 uniq(int... columnIndexs) throws RException {

		int size = columnIndexs.length;

		ArrayList<Integer> indexs = new ArrayList<>();
		for (int index : columnIndexs) {
			indexs.add(index);
		}
		Collections.sort(indexs);

		int lastIndex = -1;

		int[] uniqColumnIndexs = new int[size];
		for (int i = 0; i < size; ++i) {

			int columnIndex = indexs.get(i);
			if (columnIndex > 0 && columnIndex == lastIndex) {
				throw new RException("duplicate column index: " + columnIndex);
			}

			uniqColumnIndexs[i] = columnIndex;
			lastIndex = columnIndex;
		}

		return new XRConstraint1Uniq(uniqColumnIndexs);
	}

}
