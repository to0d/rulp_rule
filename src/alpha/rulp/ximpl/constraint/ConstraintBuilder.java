package alpha.rulp.ximpl.constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import alpha.rulp.ximpl.constraint.ConstraintFactory.RConstraint;

public class ConstraintBuilder {

	private IRList namedList;

	private Map<String, Integer> varIndexMap = new HashMap<>();

	public ConstraintBuilder(IRList namedList) throws RException {
		super();
		this.namedList = namedList;
		ReteUtil.buildTreeVarList(namedList, varIndexMap);
	}

	private IRConstraint1 _exprConstraint(IRExpr rightExpr) throws RException {

		ArrayList<IRObject> rightVarList = ReteUtil.buildVarList(rightExpr);
		ArrayList<IRObject> externalVarList = new ArrayList<>();

		for (IRObject v : rightVarList) {
			if (!varIndexMap.containsKey(v.asString())) {
				externalVarList.add(v);
			}
		}

		IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(namedList));

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
		ArrayList<IRObject> alphaVarList = RulpUtil.toArray(namedList);

		IRConstraint1 expr1MatchNode = ConstraintFactory.createConstraintExpr1Node(rightExpr, alphaVarList);
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

		if (obj.getType() == RType.INT) {
			return RulpUtil.asInteger(obj).asInteger();

		} else if (obj.getType() == RType.ATOM) {

			String columnName = RulpUtil.asAtom(obj).getName();
			if (!varIndexMap.containsKey(columnName)) {
				throw new RException("invalid column: " + obj);
			}

			return varIndexMap.get(columnName);

		} else {

			throw new RException("Invalid column: " + obj);
		}
	}

	private IRConstraint1 _typeConstraint(RConstraint cons) throws RException {

		RType columnType = RType.toType(RulpUtil.asAtom(cons.constraintValue).asString());
		if (columnType == null || !ReteUtil.isEntryValueType(columnType)) {
			throw new RException("Invalid column type: " + columnType);
		}

		return ConstraintFactory.createConstraintType(_getColumnIndex(cons.onObject), columnType);
	};

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

	public List<IRConstraint1> buildconstraintList(IRList args, IRInterpreter interpreter, IRFrame frame)
			throws RException {

		/********************************************/
		// Check constraint list
		/********************************************/
		ArrayList<IRConstraint1> constraintList = new ArrayList<>();

		IRIterator<? extends IRObject> it = args.listIterator(0);
		while (it.hasNext()) {

			IRObject obj = it.next();

			switch (obj.getType()) {

			case LIST:

				RConstraint cons = ConstraintFactory.toConstraint((IRList) obj, interpreter, frame);
				switch (cons.constraintType) {
				case TYPE:
					constraintList.add(_typeConstraint(cons));
					break;

				case UNIQ:
					constraintList.add(_uniqConstraint(cons));
					break;

				default:
					throw new RException("unsupport constraint: " + cons.constraintType);
				}
				break;

			case EXPR:
				constraintList.add(_exprConstraint((IRExpr) obj));
				break;

			default:
				throw new RException("no constraint list: " + obj);
			}

		}

		return constraintList;
	}
}
