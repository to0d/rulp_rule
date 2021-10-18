package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.F_EQUAL;
import static alpha.rulp.lang.Constant.S_QUESTION;
import static alpha.rulp.rule.Constant.A_Max;
import static alpha.rulp.rule.Constant.A_NOT_NULL;
import static alpha.rulp.rule.Constant.A_On;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Uniq;
import static alpha.rulp.rule.Constant.F_NOT_EQUAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;

public class ConstraintFactory {

	public static class RConstraint {
		String constraintName;
		IRObject constraintValue;
		IRObject onObject;
	}

	static class TableColumnConstraint {
		Boolean notNull = null;
		RType type = null;
		Boolean uniq = null;
	}

	static boolean _isAtom(IRList list, int index, String name) throws RException {

		IRObject obj = list.get(index);
		if (obj.getType() != RType.ATOM) {
			return false;
		}

		return RulpUtil.asAtom(obj).getName().equals(name);
	}

	static boolean _isFactor(IRList list, int index, String name) throws RException {

		IRObject obj = list.get(index);
		if (obj.getType() != RType.FACTOR) {
			return false;
		}

		return RulpUtil.asFactor(obj).getName().equals(name);
	}

	public static IRConstraint2 createConstraint2EntryOrder() {
		return new XRConstraint2EntryOrder();
	}

	public static IRConstraint1 createConstraintEqualIndex(int idx1, int idx2) {
		return new XRConstraint1EqualIndex(idx1, idx2);
	}

	public static IRConstraint1 createConstraintEqualValue(int index, IRObject obj) {
		return new XRConstraint1EqualValue(index, obj);
	}

	public static IRConstraint1 createConstraintExpr0Node(IRExpr expr, IRObject[] varEntry) throws RException {
		return new XRConstraint1Expr0(expr, varEntry);
	}

	public static IRConstraint1 createConstraintExpr1Node(IRExpr expr, List<IRObject> leftVarList) throws RException {

		int size = expr.size();

		if (size <= 0) {
			return null;
		}

		IRObject e0 = expr.get(0);
		if (e0.getType() != RType.ATOM) {
			return null;
		}

		String name0 = RulpUtil.asAtom(e0).getName();

		switch (name0) {
		case F_EQUAL:
		case F_NOT_EQUAL:

			if (size != 3) {
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
				if (name0.equals(F_EQUAL)) {
					return ConstraintFactory.createConstraintEqualValue(leftVarIndex, a2);
				} else {
					return ConstraintFactory.createConstraintNotEqualValue(leftVarIndex, a2);
				}
			}

			// (equal a v)
			if (leftVarIndex == -1 && rightVarIndex != -1) {
				if (name0.equals(F_EQUAL)) {
					return ConstraintFactory.createConstraintEqualValue(rightVarIndex, a1);
				} else {
					return ConstraintFactory.createConstraintNotEqualValue(rightVarIndex, a1);
				}
			}

			// (equal v1 v2)
			if (leftVarIndex != -1 && rightVarIndex != -1 && leftVarIndex != rightVarIndex) {

				if (name0.equals(F_EQUAL)) {
					return ConstraintFactory.createConstraintEqualIndex(leftVarIndex, rightVarIndex);
				} else {
					return ConstraintFactory.createConstraintNotEqualIndex(leftVarIndex, rightVarIndex);
				}
			}

			return null;

		default:

			return null;
		}

	}

	public static IRConstraint1 createConstraintExpr3Node(IRExpr expr) throws RException {
		return new XRConstraint1Expr3(expr);
	}

	public static IRConstraint1 createConstraintExpr4Node(IRExpr expr, List<IRList> matchStmtList) throws RException {
		return new XRConstraint1Expr4(expr, matchStmtList);
	}

	public static IRConstraint1 createConstraintMax(int columnIndex, IRObject maxValue) {
		return new XRConstraint1Max(columnIndex, maxValue);
	}

	public static IRConstraint1 createConstraintNotEqualIndex(int idx1, int idx2) {
		return new XRConstraint1NotEqualIndex(idx1, idx2);

	}

	public static IRConstraint1 createConstraintNotEqualValue(int index, IRObject obj) {
		return new XRConstraint1NotEqualValue(index, obj);
	}

	public static IRConstraint1 createConstraintNotNull(int index) {
		return new XRConstraint1NotNull(index);
	}

	public static IRConstraint1 createConstraintType(int columnIndex, RType columnType) {
		return new XRConstraint1Type(columnIndex, columnType);
	}

	public static IRConstraint1 createConstraintUniq(int... columnIndexs) throws RException {

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

	static IRConstraint1 createSingleConstraint(IRList constraintList) {

		IRIterator<? extends IRObject> iterator = constraintList.iterator();
		return null;

		// Skip first ?x atom

	}

	static TableColumnConstraint[] getColumnConstraint(IRList tableList) throws RException {

		int tableLen = tableList.size();
		TableColumnConstraint[] columnConstraint = new TableColumnConstraint[tableLen];

		int constraintCount = 0;

		for (int i = 0; i < tableLen; ++i) {

			IRObject columnObj = tableList.get(i);
			switch (columnObj.getType()) {
			case ATOM:
				// atom should be: ?x or ?
				if (!RulpUtil.isVarAtom(columnObj) && !ReteUtil.isAnyVar(columnObj)) {
					throw new RException("Invalid atom column: " + columnObj);
				}
				break;

			case LIST:

			default:
				throw new RException("unsupport column: " + columnObj);
			}
		}

		if (constraintCount == 0) {
			return null;
		}

		return columnConstraint;
	}

	static RConstraint toConstraint(IRList constraintlist, IRInterpreter interpreter, IRFrame frame) throws RException {

		int consListSize = constraintlist.size();

		// (type int on ?x)
		if (consListSize == 4 && _isAtom(constraintlist, 0, A_Type) && _isAtom(constraintlist, 2, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_Type;
			cons.constraintValue = interpreter.compute(frame, constraintlist.get(1));
			cons.onObject = interpreter.compute(frame, constraintlist.get(3));

			return cons;
		}

		// (uniq on ?x)
		if (consListSize == 3 && (_isAtom(constraintlist, 0, A_Uniq) || _isFactor(constraintlist, 0, A_Uniq))
				&& _isAtom(constraintlist, 1, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_Uniq;
			cons.onObject = interpreter.compute(frame, constraintlist.get(2));

			return cons;
		}

		// (max 10 on ?x)
		if (consListSize == 4 && _isAtom(constraintlist, 0, A_Max) && _isAtom(constraintlist, 2, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_Max;
			cons.constraintValue = interpreter.compute(frame, constraintlist.get(1));
			cons.onObject = interpreter.compute(frame, constraintlist.get(3));
			return cons;
		}

		// (? on ?x)
		if (consListSize == 3 && _isAtom(constraintlist, 0, S_QUESTION) && _isAtom(constraintlist, 1, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = S_QUESTION;
			cons.onObject = interpreter.compute(frame, constraintlist.get(2));

			return cons;
		}

		// (not-nil on ?x)
		if (consListSize == 3 && _isAtom(constraintlist, 0, A_NOT_NULL) && _isAtom(constraintlist, 1, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_NOT_NULL;
			cons.onObject = interpreter.compute(frame, constraintlist.get(2));

			return cons;
		}

		throw new RException("unsupport constraint list: " + constraintlist);
	}
}
