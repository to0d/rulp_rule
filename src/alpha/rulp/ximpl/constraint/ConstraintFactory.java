package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.F_EQUAL;
import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.F_NOT_EQUAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;

public class ConstraintFactory {

	public static IRConstraint1 createConstraint1OneOf(int index, IRList valueList) {
		return new XRConstraint1OneOf(index, valueList);
	}

	public static IRConstraint2 createConstraint2EntryOrder() {
		return new XRConstraint2EntryOrder();
	}

	public static IRConstraint1 createConstraintCompareValue(RRelationalOperator op, int index, IRObject obj) {
		return new XRConstraint1CompareValue(op, index, obj);
	}

	public static IRConstraint1 createConstraintCompareIndex(RRelationalOperator op, int idx1, int idx2) {

		if (idx1 > idx2) {
			return new XRConstraint1CompareIndex(RRelationalOperator.toOpposite(op), idx2, idx1);
		} else {
			return new XRConstraint1CompareIndex(op, idx1, idx2);
		}
	}

	public static IRConstraint1 createConstraintEqualIndex(int idx1, int idx2) {
		return createConstraintCompareIndex(RRelationalOperator.EQ, idx1, idx2);
	}

	public static IRConstraint1 createConstraintEqualValue(int index, IRObject obj) {
		return createConstraintCompareValue(RRelationalOperator.EQ, index, obj);
	}

	public static IRConstraint1 createConstraintExpr0Node(IRExpr expr, IRObject[] varEntry) throws RException {

		int varEntryLen = varEntry.length;

		IRObject[] newVarEntry = new IRObject[varEntryLen];
		Map<String, IRObject> rebuildVarMap = new HashMap<>();
		int externalVarCount = 0;

		for (IRObject var : ReteUtil.uniqVarList(expr)) {

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

		int[] constraintIndex = new int[rebuildVarMap.size()];
		int cIndex = 0;
		for (int i = 0; i < varEntryLen; ++i) {
			if (newVarEntry[i] != null) {
				constraintIndex[cIndex] = i;
			}
		}

		/*****************************************/
		// Build expr
		/*****************************************/
		IRExpr newExpr = RulpUtil.asExpression(RuntimeUtil.rebuild(expr, rebuildVarMap));

		return new XRConstraint1Expr0X(newExpr, newVarEntry, constraintIndex, externalVarCount);
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

		if (size != 3) {
			return null;
		}

		RRelationalOperator op = null;

		switch (RulpUtil.asAtom(e0).getName()) {
		case F_EQUAL:
			op = RRelationalOperator.EQ;
			break;
		case F_NOT_EQUAL:
			op = RRelationalOperator.NE;
			break;

		default:
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
			return ConstraintFactory.createConstraintCompareValue(op, leftVarIndex, a2);
		}

		// (equal a v)
		if (leftVarIndex == -1 && rightVarIndex != -1) {
			return ConstraintFactory.createConstraintCompareValue(RRelationalOperator.toOpposite(op), rightVarIndex,
					a1);
		}

		// (equal v1 v2)
		if (leftVarIndex != -1 && rightVarIndex != -1 && leftVarIndex != rightVarIndex) {
			return ConstraintFactory.createConstraintCompareIndex(op, leftVarIndex, rightVarIndex);
		}

		return null;
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

	public static IRConstraint1 createConstraintMin(int columnIndex, IRObject maxValue) {
		return new XRConstraint1Min(columnIndex, maxValue);
	}

	public static IRConstraint1 createConstraintNotEqualIndex(int idx1, int idx2) {
		return createConstraintCompareIndex(RRelationalOperator.NE, idx1, idx2);
	}

	public static IRConstraint1 createConstraintNotEqualValue(int index, IRObject obj) {
		return createConstraintCompareValue(RRelationalOperator.NE, index, obj);
	}

	public static IRConstraint1 createConstraintNotNull(int index) {
		return createConstraintCompareValue(RRelationalOperator.NE, index, O_Nil);
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

//	static TableColumnConstraint[] getColumnConstraint(IRList tableList) throws RException {
//
//		int tableLen = tableList.size();
//		TableColumnConstraint[] columnConstraint = new TableColumnConstraint[tableLen];
//
//		int constraintCount = 0;
//
//		for (int i = 0; i < tableLen; ++i) {
//
//			IRObject columnObj = tableList.get(i);
//			switch (columnObj.getType()) {
//			case ATOM:
//				// atom should be: ?x or ?
//				if (!RulpUtil.isVarAtom(columnObj) && !ReteUtil.isAnyVar(columnObj)) {
//					throw new RException("Invalid atom column: " + columnObj);
//				}
//				break;
//
//			case LIST:
//
//			default:
//				throw new RException("unsupport column: " + columnObj);
//			}
//		}
//
//		if (constraintCount == 0) {
//			return null;
//		}
//
//		return columnConstraint;
//	}

}
