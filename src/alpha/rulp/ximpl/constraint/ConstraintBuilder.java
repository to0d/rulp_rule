package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.S_QUESTION;
import static alpha.rulp.rule.Constant.A_Max;
import static alpha.rulp.rule.Constant.A_Min;
import static alpha.rulp.rule.Constant.A_NOT_NULL;
import static alpha.rulp.rule.Constant.A_On;
import static alpha.rulp.rule.Constant.A_One_Of;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Uniq;

import java.util.ArrayList;
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
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StringUtil;
import alpha.rulp.ximpl.node.IRNamedNode;

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

	static RConstraint _toConstraint(IRList constraintlist, IRInterpreter interpreter, IRFrame frame)
			throws RException {

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

		// (min 10 on ?x)
		if (consListSize == 4 && _isAtom(constraintlist, 0, A_Min) && _isAtom(constraintlist, 2, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_Min;
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

		// (one-of '(a b c) on ?x)
		if (consListSize == 4 && _isAtom(constraintlist, 0, A_One_Of) && _isAtom(constraintlist, 2, A_On)) {

			RConstraint cons = new RConstraint();
			cons.constraintName = A_One_Of;
			cons.constraintValue = interpreter.compute(frame, constraintlist.get(1));
			cons.onObject = interpreter.compute(frame, constraintlist.get(3));

			return cons;
		}

		throw new RException("unsupport constraint list: " + constraintlist);
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

	private IRConstraint1 _exprConstraint(IRExpr rightExpr) throws RException {

		ArrayList<IRObject> rightVarList = ReteUtil.buildVarList(rightExpr);
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
				return ConstraintFactory.createConstraintExpr0Node(rightExpr, varEntry);
			}

			/*********************************************************/
			// '(?a ?b ?c) (factor ?x) - no left variable in expr
			/*********************************************************/
			return ConstraintFactory.createConstraintExpr3Node(rightExpr);
		}

		/*********************************************************/
		// (equal ?a b) or (not-equal ?a b)
		/*********************************************************/
		IRConstraint1 expr1MatchNode = ConstraintFactory.createConstraintExpr1Node(rightExpr,
				RulpUtil.toArray2(varEntry));
		if (expr1MatchNode != null) {
			return expr1MatchNode;
		}

		// Other node

		/*********************************************************/
		// Expr0: (factor ?a b)
		/*********************************************************/
		return ConstraintFactory.createConstraintExpr0Node(rightExpr, varEntry);

	}

	private int _getColumnIndex(IRObject obj) throws RException {

		if (obj.getType() != RType.ATOM) {
			throw new RException("Invalid column: " + obj);
		}

		String columnName = RulpUtil.asAtom(obj).getName();
		if (columnName.equals(S_QUESTION)) {
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

	private Set<String> _matchConstraint(RConstraint cons, IRNamedNode node) throws RException {

		Set<String> matchedConstraints = new HashSet<>();

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
			if (!cons.constraintName.equals(S_QUESTION)
					&& !cons.constraintName.equals(constraint.getConstraintName())) {
				continue;
			}

			if (columnIndexs != null && !matchIndexs(constraint.getConstraintIndex(), columnIndexs)) {
				continue;
			}

			matchedConstraints.add(constraint.getConstraintExpression());
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
			return ConstraintFactory.createConstraintMax(_getColumnIndex(cons.onObject), cons.constraintValue);

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
			return ConstraintFactory.createConstraintMin(_getColumnIndex(cons.onObject), cons.constraintValue);

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	private IRConstraint1 _notNullConstraint(RConstraint cons) throws RException {

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.createConstraintNotNull(_getColumnIndex(cons.onObject));

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	private IRConstraint1 _oneOfConstraint(RConstraint cons) throws RException {

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.createConstraint1OneOf(_getColumnIndex(cons.onObject),
					RulpUtil.asList(cons.constraintValue));

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	private int[] _toIndex(IRObject obj) throws RException {

		int columnIndexs[] = null;

		switch (obj.getType()) {
		case ATOM:

			String columnName = RulpUtil.asAtom(obj).getName();
			if (columnName.equals(S_QUESTION)) {
				return null;
			}

			if (!varIndexMap.containsKey(columnName)) {
				throw new RException("invalid column: " + columnName);
			}

			columnIndexs = new int[1];
			columnIndexs[0] = varIndexMap.get(columnName);
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

		return ConstraintFactory.createConstraintType(_getColumnIndex(cons.onObject), columnType);
	}

	private IRConstraint1 _uniqConstraint(RConstraint cons) throws RException {

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.createConstraintUniq(_getColumnIndex(cons.onObject));

		case LIST:

			IRList onList = (IRList) cons.onObject;

			int uniqIndexCount = onList.size();
			int[] columnIndexs = new int[uniqIndexCount];

			for (int i = 0; i < uniqIndexCount; ++i) {
				columnIndexs[i] = _getColumnIndex(onList.get(i));
			}

			return ConstraintFactory.createConstraintUniq(columnIndexs);

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	public IRConstraint1 build(IRObject obj, IRInterpreter interpreter, IRFrame frame) throws RException {

		switch (obj.getType()) {

		case LIST:

			RConstraint cons = _toConstraint((IRList) obj, interpreter, frame);
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
				throw new RException("unsupport constraint: " + cons.constraintName);
			}

		case EXPR:
			return _exprConstraint((IRExpr) obj);

		default:
			throw new RException("no constraint list: " + obj);
		}
	}

	public List<String> match(IRNamedNode node, IRList args, IRInterpreter interpreter, IRFrame frame)
			throws RException {

		Set<String> matchedConstraints = new HashSet<>();

		IRIterator<? extends IRObject> it = args.iterator();
		while (it.hasNext()) {

			IRObject obj = it.next();

			switch (obj.getType()) {
			case LIST:
				matchedConstraints.addAll(_matchConstraint(_toConstraint((IRList) obj, interpreter, frame), node));
				break;

			case EXPR:
				matchedConstraints.add(_exprConstraint((IRExpr) obj).getConstraintExpression());
				break;

			default:
				throw new RException("no constraint list: " + obj);
			}
		}

		return new ArrayList<>(matchedConstraints);
	}
}
