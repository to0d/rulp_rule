package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.A_QUESTION;
import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.A_Max;
import static alpha.rulp.rule.Constant.A_Min;
import static alpha.rulp.rule.Constant.A_NOT_NULL;
import static alpha.rulp.rule.Constant.A_On;
import static alpha.rulp.rule.Constant.A_One_Of;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Uniq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StringUtil;

public class ConstraintBuilder {

	static class RConstraint {
		String constraintName;
		IRObject constraintValue;
		IRObject onObject;
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

	static RConstraint _toConstraint(IRExpr expr, IRInterpreter interpreter, IRFrame frame) throws RException {

		int consListSize = expr.size();

		// (type int on ?x)
		if (consListSize == 4 && _isAtom(expr, 0, A_Type) && _isAtom(expr, 2, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_Type;
			cons.constraintValue = interpreter.compute(frame, expr.get(1));
			cons.onObject = interpreter.compute(frame, expr.get(3));

			return cons;
		}

		// (uniq on ?x)
		if (consListSize == 3 && (_isAtom(expr, 0, A_Uniq) || _isFactor(expr, 0, A_Uniq)) && _isAtom(expr, 1, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_Uniq;
			cons.onObject = interpreter.compute(frame, expr.get(2));

			return cons;
		}

		// (max 10 on ?x)
		if (consListSize == 4 && _isAtom(expr, 0, A_Max) && _isAtom(expr, 2, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_Max;
			cons.constraintValue = interpreter.compute(frame, expr.get(1));
			cons.onObject = interpreter.compute(frame, expr.get(3));
			return cons;
		}

		// (min 10 on ?x)
		if (consListSize == 4 && _isAtom(expr, 0, A_Min) && _isAtom(expr, 2, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_Min;
			cons.constraintValue = interpreter.compute(frame, expr.get(1));
			cons.onObject = interpreter.compute(frame, expr.get(3));
			return cons;
		}

		// (? on ?x)
		if (consListSize == 3 && _isAtom(expr, 0, A_QUESTION) && _isAtom(expr, 1, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_QUESTION;
			cons.onObject = interpreter.compute(frame, expr.get(2));

			return cons;
		}

		// (not-nil on ?x)
		if (consListSize == 3 && _isAtom(expr, 0, A_NOT_NULL) && _isAtom(expr, 1, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_NOT_NULL;
			cons.onObject = interpreter.compute(frame, expr.get(2));

			return cons;
		}

		// (one-of '(a b c) on ?x)
		if (consListSize == 4 && _isAtom(expr, 0, A_One_Of) && _isAtom(expr, 2, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_One_Of;
			cons.constraintValue = interpreter.compute(frame, expr.get(1));
			cons.onObject = interpreter.compute(frame, expr.get(3));

			return cons;
		}

		return null;
	}

	public static boolean matchIndexs(int[] idx1, int[] idx2) throws RException {

		if (idx1.length != idx2.length) {
			return false;
		}

		if (idx1.length == 0) {
			return true;
		}

		// All index1 should be actual index number
		{
			int lastIdx = -2;
			for (int idx : idx1) {
				if (idx < 0 || idx == lastIdx) {
					throw new RException("invalid index1: " + idx);
				}
				lastIdx = idx;
			}
		}

		// All index2 should be in order
		int anyIdx2Count = 0;
		{
			int lastIdx = -2;
			for (int idx : idx2) {

				if (idx < lastIdx || (idx >= 0 && idx == lastIdx)) {
					throw new RException("invalid index2: " + idx);
				}

				if (idx == -1) {
					anyIdx2Count++;
				}

				lastIdx = idx;
			}
		}

		final int len = idx1.length;

		// match index one by one
		if (anyIdx2Count == 0) {

			for (int i = 0; i < len; ++i) {
				if (idx1[i] != idx2[i]) {
					return false;
				}
			}

			return true;
		}

		int i = 0;
		NEXT: for (int j = anyIdx2Count; j < len; ++j) {
			int idx = idx2[j];
			while (i < len) {
				if (idx1[i++] == idx) {
					continue NEXT;
				}
			}
			return false;
		}

		return true;
	}

	private IRObject[] varEntry;

	private Map<String, Integer> varIndexMap = new HashMap<>();

	public ConstraintBuilder(IRObject[] varEntry) throws RException {
		super();
		this.varEntry = varEntry;

		for (int i = 0; i < varEntry.length; ++i) {

			IRObject obj = varEntry[i];
			if (obj != null && RulpUtil.isVarAtom(obj)) {
				varIndexMap.put(RulpUtil.asAtom(obj).getName(), i);
			}

		}
	}

	private IRConstraint1 _exprConstraint(IRExpr expr, IRInterpreter interpreter, IRFrame frame) throws RException {

		IRObject rst = OptimizeUtil.optimizeExpr(expr, interpreter, frame);

		switch (rst.getType()) {
		case EXPR:
			expr = (IRExpr) rst;
			break;

		case BOOL:
			if (RulpUtil.asBoolean(rst).asBoolean()) {
				return null;
			} else {
				throw new RException("false expr constraint found: " + expr);
			}

		default:
			throw new RException("Can't optimize constraint: " + expr);
		}

		ArrayList<IRObject> rightVarList = ReteUtil.buildVarList(expr);
		ArrayList<IRObject> externalVarList = new ArrayList<>();

		for (IRObject v : rightVarList) {
			if (!varIndexMap.containsKey(v.asString())) {
				externalVarList.add(v);
			}
		}

		if (!externalVarList.isEmpty()) {

			/*********************************************************/
			// '(?a ?b ?c) (factor ?a ?x) - has left variable in expr
			/*********************************************************/
			if (externalVarList.size() != rightVarList.size()) {
				return ConstraintFactory.expr0(expr, varEntry, frame);
			}

			/*********************************************************/
			// '(?a ?b ?c) (factor ?x) - no left variable in expr
			/*********************************************************/
			return ConstraintFactory.expr3(expr);
		}

		/*********************************************************/
		// (equal ?a b) or (not-equal ?a b)
		/*********************************************************/
		IRConstraint1 expr1MatchNode = ConstraintFactory.expr1(expr, RulpUtil.toArray2(varEntry));
		if (expr1MatchNode != null) {
			return expr1MatchNode;
		}

		// Other node

		/*********************************************************/
		// Expr0: (factor ?a b)
		/*********************************************************/
		return ConstraintFactory.expr0(expr, varEntry, frame);

	}

	private int _getColumnIndex(IRObject obj) throws RException {

		if (obj.getType() != RType.ATOM) {
			throw new RException("Invalid column: " + obj);
		}

		String columnName = RulpUtil.asAtom(obj).getName();
		if (columnName.equals(A_QUESTION)) {
			return -1;
		}

		if (varIndexMap.containsKey(columnName)) {
			return varIndexMap.get(columnName);
		}

		String varName = columnName.substring(1);
		if (StringUtil.isNumber(varName)) {
			return Integer.valueOf(varName);
		}

		throw new RException("invalid column: " + obj);
	}

	private Set<IRConstraint1> _matchConstraint(RConstraint cons, IRReteNode node) throws RException {

		Set<IRConstraint1> matchedConstraints = new HashSet<>();

		RType columnType = null;

		/**********************************************/
		// Column type
		/**********************************************/
		if (cons.constraintName.equals(A_Type) && !ReteUtil.isAnyVar(cons.constraintValue)) {
			columnType = RType.toType(RulpUtil.asAtom(cons.constraintValue).asString());
			if (columnType == null || !ReteUtil.isEntryValueType(columnType)) {
				throw new RException("Invalid column type: " + columnType);
			}
		}

		/**********************************************/
		// Column index
		/**********************************************/
		int columnIndexs[] = _toIndex(cons.onObject);

		/**********************************************/
		// Check
		/**********************************************/
		int totalConstraintCount = node.getConstraint1Count();
		for (int i = 0; i < totalConstraintCount; ++i) {

			IRConstraint1 constraint = node.getConstraint1(i);
			if (!cons.constraintName.equals(A_QUESTION)
					&& !cons.constraintName.equals(constraint.getConstraintName())) {
				continue;
			}

			if (columnIndexs != null && !matchIndexs(constraint.getConstraintIndex(), columnIndexs)) {
				continue;
			}

			matchedConstraints.add(constraint);
		}

		return matchedConstraints;
	}

	private IRConstraint1 _maxConstraint(RConstraint cons) throws RException {

		if (cons.constraintValue == null
				|| (cons.constraintValue.getType() != RType.INT && cons.constraintValue.getType() != RType.FLOAT)) {
			throw new RException("Invalid column type: " + cons.constraintValue);
		}

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.max(_getColumnIndex(cons.onObject), cons.constraintValue);

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	private IRConstraint1 _minConstraint(RConstraint cons) throws RException {

		if (cons.constraintValue == null
				|| (cons.constraintValue.getType() != RType.INT && cons.constraintValue.getType() != RType.FLOAT)) {
			throw new RException("Invalid column type: " + cons.constraintValue);
		}

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.min(_getColumnIndex(cons.onObject), cons.constraintValue);

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	private IRConstraint1 _notNullConstraint(RConstraint cons) throws RException {

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.cmpEntryValue(RRelationalOperator.NE, _getColumnIndex(cons.onObject), O_Nil);

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	private IRConstraint1 _oneOfConstraint(RConstraint cons) throws RException {

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.oneOf(_getColumnIndex(cons.onObject), RulpUtil.asList(cons.constraintValue));

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	private int[] _toIndex(IRObject obj) throws RException {

		int columnIndexs[] = null;

		switch (obj.getType()) {
		case ATOM:

			int index = _getColumnIndex(obj);
			if (index == -1) {
				return null;
			}

			columnIndexs = new int[1];
			columnIndexs[0] = index;
			break;

		case INT:
			columnIndexs = new int[1];
			columnIndexs[0] = RulpUtil.asInteger(obj).asInteger();
			break;

		case LIST:

			IRList onList = RulpUtil.asList(obj);

			int uniqIndexCount = onList.size();
			columnIndexs = new int[uniqIndexCount];

			for (int i = 0; i < uniqIndexCount; ++i) {
				columnIndexs[i] = _getColumnIndex(onList.get(i));
			}
			break;

		default:
			throw new RException("invalid column index: " + obj);
		}

		return columnIndexs;
	}

	private IRConstraint1 _typeConstraint(RConstraint cons) throws RException {

		RType columnType = RType.toType(RulpUtil.asAtom(cons.constraintValue).asString());
		if (columnType == null || !ReteUtil.isEntryValueType(columnType)) {
			throw new RException("Invalid column type: " + columnType);
		}

		return ConstraintFactory.type(_getColumnIndex(cons.onObject), columnType);
	}

	private IRConstraint1 _uniqConstraint(RConstraint cons) throws RException {

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.uniq(_getColumnIndex(cons.onObject));

		case LIST:

			IRList onList = (IRList) cons.onObject;

			int uniqIndexCount = onList.size();
			int[] columnIndexs = new int[uniqIndexCount];

			for (int i = 0; i < uniqIndexCount; ++i) {
				columnIndexs[i] = _getColumnIndex(onList.get(i));
			}

			return ConstraintFactory.uniq(columnIndexs);

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	public IRConstraint1 build(IRExpr expr, IRInterpreter interpreter, IRFrame frame) throws RException {

		RConstraint cons = _toConstraint(expr, interpreter, frame);
		if (cons != null) {
			switch (cons.constraintName) {
			case A_Type:
				return _typeConstraint(cons);

			case A_Uniq:
				return _uniqConstraint(cons);

			case A_NOT_NULL:
				return _notNullConstraint(cons);

			case A_Max:
				return _maxConstraint(cons);

			case A_Min:
				return _minConstraint(cons);

			case A_One_Of:
				return _oneOfConstraint(cons);

			default:
				throw new RException("unsupport constraint: " + expr);
			}
		}

		return _exprConstraint(expr, interpreter, frame);
	}

	public List<IRConstraint1> match(IRReteNode node, IRExpr expr, IRInterpreter interpreter, IRFrame frame)
			throws RException {

		Set<IRConstraint1> matchedConstraints = new HashSet<>();

		RConstraint cons = _toConstraint(expr, interpreter, frame);
		if (cons != null) {

			matchedConstraints.addAll(_matchConstraint(cons, node));

		} else {
			IRConstraint1 cons1 = _exprConstraint(expr, interpreter, frame);
			if (cons1 != null) {
				matchedConstraints.add(cons1);
			}
		}

		ArrayList<IRConstraint1> matchedConstraintList = new ArrayList<>(matchedConstraints);
		if (matchedConstraintList.size() > 1) {
			Collections.sort(matchedConstraintList, (c1, c2) -> {
				return c1.getConstraintExpression().compareTo(c2.getConstraintExpression());
			});
		}

		return matchedConstraintList;
	}
}
