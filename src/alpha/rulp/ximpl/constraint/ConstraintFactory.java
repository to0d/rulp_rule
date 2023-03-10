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
import alpha.rulp.runtime.IRFunction;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.optimize.OptUtil;

public class ConstraintFactory {

	protected static IRConstraint1 _rebuildExprConstraint(IRReteNode node, IRConstraint1Expr constraint)
			throws RException {

		IRExpr expr = constraint.getExpr();
		RRelationalOperator op = null;

		if ((op = RulpUtil.toRelationalOperator(expr.get(0).asString())) != null && OptUtil.getExprLevel(expr) == 1
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

	protected static IRVar _toVar(IRObject obj, IRFrame frame) throws RException {

		switch (obj.getType()) {
		case VAR:
			return (IRVar) obj;

		case ATOM:

			IRFrameEntry varEntry = RuntimeUtil.lookupFrameEntry(frame, RulpUtil.asAtom(obj).getName());
			if (varEntry == null) {
				throw new RException("var not found: " + obj);
			}

			return RulpUtil.asVar(varEntry.getValue());

		default:
			throw new RException("not var: " + obj);
		}
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

			IRFrameEntry varEntry = RuntimeUtil.lookupFrameEntry(frame, RulpUtil.asAtom(varObj).getName());
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

	public static IRConstraint1 cmpVarVal(RRelationalOperator op, IRObject varObj, IRObject val, IRFrame frame)
			throws RException {

		return cmpVarVal(op, _toVar(varObj, frame), val);
	}

	public static IRConstraint1 cmpVarVal(RRelationalOperator op, IRVar var, IRObject val) {
		return new XRConstraint1CompareVarValue(op, var, val);
	}

	public static IRConstraint1 cmpVarVar(RRelationalOperator op, IRObject var1Obj, IRObject var2Obj, IRFrame frame)
			throws RException {

		IRVar var1 = _toVar(var1Obj, frame);
		IRVar var2 = _toVar(var2Obj, frame);

		int d = var1.getName().compareTo(var2.getName());
		if (d > 0) {
			return cmpVarVar(op, var1, var2);
		}

		if (d < 0) {
			return cmpVarVar(RRelationalOperator.oppositeOf(op), var2, var1);
		}

		throw new RException("same var: " + var1Obj + ", " + var2Obj);
	}

	public static IRConstraint1 cmpVarVar(RRelationalOperator op, IRVar var1, IRVar var2) {
		return new XRConstraint1CompareVarVar(op, var1, var2);
	}

	public static IRConstraint2 entryOrder() {
		return new XRConstraint2EntryOrder();
	}

	public static IRConstraint1 expr0(IRExpr expr, IRObject[] varEntry, IRFrame frame) throws RException {

		int varEntryLen = varEntry.length;

		IRObject[] newVarEntry = new IRObject[varEntryLen];
		Map<String, IRObject> rebuildVarMap = new HashMap<>();

		int externalVarCount = 0;
		ArrayList<String> externalVarNames = new ArrayList<>();
		ArrayList<Integer> constraintIndexArray = new ArrayList<>();

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
				constraintIndexArray.add(findIndex);
			} else {
				externalVarCount++;
				String externalVarName = var.asString();
				if (!externalVarNames.contains(externalVarName)) {
					externalVarNames.add(externalVarName);
				}
			}
		}

		if (rebuildVarMap.isEmpty()) {
			return new XRConstraint1Expr0(expr, varEntry);
		}

		int[] constraintIndexs = new int[constraintIndexArray.size()];
		int cIndex = 0;
		for (int index : constraintIndexArray) {
			constraintIndexs[cIndex++] = index;
		}

//		for (int i = 0; i < varEntryLen; ++i) {
//			if (newVarEntry[i] != null) {
//				constraintIndexs[cIndex++] = i;
//			}
//		}

		/*****************************************/
		// Build expr
		/*****************************************/
		expr = RulpUtil.asExpression(RuntimeUtil.rebuild(expr, rebuildVarMap));

		RRelationalOperator op = null;
		if ((op = RulpUtil.toRelationalOperator(expr.get(0).asString())) != null && OptUtil.getExprLevel(expr) == 1
				&& expr.size() == 3) {

			// (op ?0 value)
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

			// (op ?0 var)
			if (constraintIndexs.length == 1 && externalVarCount == 1) {

				int constraintIndex = constraintIndexs[0];
				String constraintIndexName = String.format("?%d", constraintIndex);

				// (op ?0 var)
				if (expr.get(1).asString().equals(constraintIndexName)) {
					return ConstraintFactory.cmpEntryVar(op, constraintIndex, expr.get(2), frame);
				}

				// (op var ?0)
				if (expr.get(2).asString().equals(constraintIndexName)) {
					return ConstraintFactory.cmpEntryVar(RRelationalOperator.oppositeOf(op), constraintIndex,
							expr.get(1), frame);
				}

			}

//			// (op var val)
//			if (constraintIndexs.length == 0 && externalVarCount == 1) {
//
//				String externalVarName = externalVarNames.get(0);
//
//				// (op ?0 var)
//				if (expr.get(1).asString().equals(externalVarName)) {
//					return ConstraintFactory.cmpVarVal(op, expr.get(1), expr.get(2), frame);
//				}
//
//				// (op var ?0)
//				if (expr.get(2).asString().equals(externalVarName)) {
//					return ConstraintFactory.cmpVarVal(RRelationalOperator.oppositeOf(op), expr.get(2), expr.get(1),
//							frame);
//				}
//			}
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

		RRelationalOperator op = RulpUtil.toRelationalOperator(RulpUtil.asAtom(e0).getName());
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

	public static IRConstraint1 expr3(IRExpr expr, IRFrame frame) throws RException {

		RRelationalOperator op = null;
		if ((op = RulpUtil.toRelationalOperator(expr.get(0).asString())) != null && OptUtil.getExprLevel(expr) == 1
				&& expr.size() == 3) {

			IRObject e1 = expr.get(1);
			IRObject e2 = expr.get(2);

			if (RulpUtil.isVarAtom(e1)) {

				// (op ?x ?y)
				if (RulpUtil.isVarAtom(e2)) {
					return ConstraintFactory.cmpVarVar(op, expr.get(1), expr.get(2), frame);
				}
				// (op ?x value)
				else {
					return ConstraintFactory.cmpVarVal(op, expr.get(1), expr.get(2), frame);
				}

			} else {

				// (op value ?x)
				if (RulpUtil.isVarAtom(e2)) {
					return ConstraintFactory.cmpVarVal(RRelationalOperator.oppositeOf(op), expr.get(2), expr.get(1),
							frame);
				}
			}
		}

		return new XRConstraint1Expr3(expr);
	}

	public static IRConstraint1 expr4(IRExpr expr, List<IRList> matchStmtList) throws RException {
		return new XRConstraint1Expr4(expr, matchStmtList);
	}

	public static IRConstraintX exprx(IRExpr expr, List<IRObject[]> varEntryList) throws RException {
		return new XRConstraintXExpr0(expr, varEntryList);
	}

	public static IRConstraint1Func func(IRFunction func, String funcName) throws RException {
		return new XRConstraint1Func(func, funcName);
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

	public static IRConstraint1 orderBy(boolean asc, int... columnIndexs) throws RException {

		ArrayList<OrderEntry> orderList = new ArrayList<>();

		for (int index : columnIndexs) {
			OrderEntry orderEntry = new OrderEntry();
			orderEntry.index = index;
			orderEntry.asc = asc;
			orderList.add(orderEntry);
		}

		return new XRConstraint1OrderBy(orderList);
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

	public static IRConstraint1 single() {
		return new XRConstraint1Single();
	}

	public static IRConstraint1 type(int columnIndex, RType columnType) {
		return new XRConstraint1Type(columnIndex, columnType);
	}

	public static IRConstraint1Uniq uniq(int... columnIndexs) throws RException {

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
