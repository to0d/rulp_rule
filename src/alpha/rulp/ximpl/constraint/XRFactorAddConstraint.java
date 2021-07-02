package alpha.rulp.ximpl.constraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintFactory.RConstraint;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRNamedNode;

public class XRFactorAddConstraint extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	static IRConstraint1 _typeConstraint(RConstraint cons, Map<String, Integer> varIndexMap) throws RException {

		RType columnType = RType.toType(RulpUtil.asAtom(cons.constraintValue).asString());
		if (columnType == null || !ReteUtil.isEntryValueType(columnType)) {
			throw new RException("Invalid column type: " + columnType);
		}

		return ConstraintFactory.createConstraintType(getColumnIndex(cons.onObject, varIndexMap), columnType);
	}

	static IRConstraint1 _uniqConstraint(RConstraint cons, Map<String, Integer> varIndexMap) throws RException {

		switch (cons.onObject.getType()) {
		case INT:
		case ATOM:
			return ConstraintFactory.createConstraintUniq(getColumnIndex(cons.onObject, varIndexMap));

		case LIST:

			IRList onList = (IRList) cons.onObject;

			int uniqIndexCount = onList.size();
			int[] columnIndexs = new int[uniqIndexCount];

			for (int i = 0; i < uniqIndexCount; ++i) {
				columnIndexs[i] = getColumnIndex(onList.get(i), varIndexMap);
			}

			return ConstraintFactory.createConstraintUniq(columnIndexs);

		default:
			throw new RException("Invalid column: " + cons.onObject);
		}
	}

	public static int getColumnIndex(IRObject obj, Map<String, Integer> varIndexMap) throws RException {

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

	static IRConstraint1 _exprConstraint(IRExpr rightExpr, Map<String, Integer> varIndexMap, IRList namedList)
			throws RException {

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

	public XRFactorAddConstraint(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		/********************************************/
		// Check parameters
		/********************************************/
		if (argSize < 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = null;
		int argIndex = 1;

		/**************************************************/
		// Check model
		/**************************************************/
		{
			IRObject obj = interpreter.compute(frame, args.get(argIndex));
			if (obj instanceof IRModel) {
				model = (IRModel) obj;
				++argIndex;
			} else {
				model = RuleUtil.getDefaultModel(frame);
				if (model == null) {
					throw new RException("no model be specified");
				}
			}
		}

		/**************************************************/
		// Check named list
		/**************************************************/
		IRList namedList = RulpUtil.asList(interpreter.compute(frame, args.get(argIndex++)));
		IRNamedNode node = ReteUtil.findNameNode(model.getNodeGraph(), namedList);
		if (node == null) {
			throw new RException("named node not found: " + namedList);
		}

		Map<String, Integer> varIndexMap = new HashMap<>();
		ReteUtil.buildTreeVarList(namedList, varIndexMap);

		/********************************************/
		// Check constraint list
		/********************************************/
		ArrayList<IRConstraint1> constraintList = new ArrayList<>();

		IRIterator<? extends IRObject> it = args.listIterator(argIndex);
		while (it.hasNext()) {

			IRObject obj = it.next();

			switch (obj.getType()) {

			case LIST:

				RConstraint cons = ConstraintFactory.toConstraint((IRList) obj, interpreter, frame);
				switch (cons.constraintType) {
				case TYPE:
					constraintList.add(_typeConstraint(cons, varIndexMap));
					break;

				case UNIQ:
					constraintList.add(_uniqConstraint(cons, varIndexMap));
					break;

				default:
					throw new RException("unsupport constraint: " + cons.constraintType);
				}
				break;

			case EXPR:
				constraintList.add(_exprConstraint((IRExpr) obj, varIndexMap, namedList));
				break;

			default:
				throw new RException("no constraint list: " + obj);
			}

		}

		if (constraintList.isEmpty()) {
			throw new RException("no constraint list: " + args);
		}

		/********************************************/
		// Update Constraint
		/********************************************/
		int updateCount = 0;
		for (IRConstraint1 cons : constraintList) {
			if (node.addConstraint(cons)) {
				updateCount++;
			}
		}

		return RulpFactory.createInteger(updateCount);
	}

//	static IMatchNode1 _buildExprNode2(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {
//
//		IRList leftTree = (IRList) reteTree.get(0);
//		IRExpr rightExpr = RulpUtil.asExpression(reteTree.get(1));
//
////		IRReteNode leftNode = _findReteNode(leftTree, tmpVarBuilder);
//		ArrayList<IRObject> leftVarList = ReteUtil.buildVarList(leftTree);
//
//		/*********************************************************/
//		// Check Entry length
//		/*********************************************************/
//		if (leftVarList.size() == 0) {
//			throw new RException("Invalid expr entry length found: " + reteTree);
//		}
//
//	}

}
