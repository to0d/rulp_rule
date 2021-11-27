package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.F_B_NOT;
import static alpha.rulp.lang.Constant.F_EQUAL;
import static alpha.rulp.lang.Constant.F_IF;
import static alpha.rulp.lang.Constant.F_O_ADD;
import static alpha.rulp.lang.Constant.F_O_BY;
import static alpha.rulp.lang.Constant.F_O_DIV;
import static alpha.rulp.lang.Constant.F_O_EQ;
import static alpha.rulp.lang.Constant.F_O_GE;
import static alpha.rulp.lang.Constant.F_O_GT;
import static alpha.rulp.lang.Constant.F_O_LE;
import static alpha.rulp.lang.Constant.F_O_LT;
import static alpha.rulp.lang.Constant.F_O_NE;
import static alpha.rulp.lang.Constant.F_O_POWER;
import static alpha.rulp.lang.Constant.F_O_SUB;
import static alpha.rulp.lang.Constant.O_False;
import static alpha.rulp.lang.Constant.O_True;
import static alpha.rulp.rule.Constant.F_HAS_STMT;
import static alpha.rulp.rule.Constant.F_NOT_EQUAL;
import static alpha.rulp.ximpl.node.RReteType.ALPH0;
import static alpha.rulp.ximpl.node.RReteType.ALPH1;
import static alpha.rulp.ximpl.node.RReteType.ALPH2;
import static alpha.rulp.ximpl.node.RReteType.BETA0;
import static alpha.rulp.ximpl.node.RReteType.BETA1;
import static alpha.rulp.ximpl.node.RReteType.BETA2;
import static alpha.rulp.ximpl.node.RReteType.BETA3;
import static alpha.rulp.ximpl.node.RReteType.EXPR0;
import static alpha.rulp.ximpl.node.RReteType.EXPR1;
import static alpha.rulp.ximpl.node.RReteType.EXPR2;
import static alpha.rulp.ximpl.node.RReteType.RETE_TYPE_NUM;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.RCountType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.ximpl.model.IReteNodeMatrix;
import alpha.rulp.ximpl.node.IRReteNodeCounter;
import alpha.rulp.ximpl.node.RReteType;

public class OptimizeUtil {

	static class NodeCount {
		int count;
		IRReteNode node;
	}

	static class OptiData {
		IRExpr newExpr;
		IRExpr oldExpr;
	}

	static class OutputType {

		RCountType countType;

		RReteType reteType;

		public OutputType(RReteType reteType, RCountType countType) {
			super();
			this.reteType = reteType;
			this.countType = countType;
		}
	}

	static class RefArray {

		int[] reteRefCount = new int[RETE_TYPE_NUM];

		int unRefCount = 0;

		public RefArray() {
			for (int i = 0; i < RETE_TYPE_NUM; ++i) {
				reteRefCount[i] = 0;
			}
		}
	}

	static class RuleCounter {
		IRReteNodeCounter counter;
		IRRule rule;
	}

	public static boolean OPT_RULE = false;

	static final RReteType SHARED_RETE_TYPES[] = { ALPH0, ALPH1, ALPH2, EXPR0, EXPR1, EXPR2, BETA0, BETA1, BETA2,
			BETA3 };

	protected static IRObject _buildOptimizeMatchTree(IRObject obj) throws RException {

		switch (obj.getType()) {
		case LIST:
		case EXPR:
		default:
			return obj;
		}
	}

	protected static boolean _canOptimizeMatchTree(IRObject obj) throws RException {

		switch (obj.getType()) {
		case LIST:

			IRList list = RulpUtil.asList(obj);
			int size = list.size();
			for (int i = 0; i < size; ++i) {
				if (_canOptimizeMatchTree(list.get(i))) {
					return true;
				}
			}

			// '('(?0 ?1 ?2) (not-equal ?2 ?0)) -> '('(?0 ?1 ?2) (not-equal ?0 ?1))
			if (size == 2) {

				IRObject o0 = list.get(0);
				IRObject o1 = list.get(1);

				if (o0.getType() == RType.LIST && o1.getType() == RType.EXPR) {

					IRExpr e1 = RulpUtil.asExpression(o1);

					if (e1.size() == 3) {

						String f0 = e1.get(0).asString();
						if (f0.equals(F_NOT_EQUAL) || f0.equals(F_EQUAL)) {

							String a = e1.get(1).asString();
							String b = e1.get(2).asString();

							// '(?a ?p ?b) (equal ?a ?a) -> '(?a ?p ?b)
							// '(?a ?p ?b) (not-equal ?a ?a) -> '(?a ?p ?b) (false)

							IRList l0 = RulpUtil.asList(o0);

							// '(?a ?p ?b) (equal value ?a) -> '(?a ?p ?b) (equal ?a value)
							// '(?a ?p ?b) (not-equal value ?a) -> '(?a ?p ?b) (not-equal ?a value)
						}

					}
				}
			}

			return false;

		case EXPR:
		default:
			return false;
		}
	}

	public static boolean isConstOperatorName(String name) {

		switch (name) {
		case F_O_BY:
		case F_O_ADD:
		case F_O_DIV:
		case F_O_SUB:
		case F_O_POWER:
		case F_O_EQ:
		case F_EQUAL:
		case F_O_NE:
		case F_NOT_EQUAL:
		case F_O_GT:
		case F_O_GE:
		case F_O_LE:
		case F_O_LT:
//		case F_STR_LENGTH:
			return true;

		default:
			return false;
		}
	}

	protected static boolean _containConstExpr(IRExpr expr) throws RException {

		int constCount = 0;

		IRIterator<? extends IRObject> it = expr.listIterator(1);
		while (it.hasNext()) {
			IRObject ex = it.next();
			if (ex.getType() == RType.EXPR) {
				if (_containConstExpr((IRExpr) ex)) {
					return true;
				}
			} else {
				if (isConstObject(ex)) {
					constCount++;
				}
			}
		}

		return constCount == (expr.size() - 1) && isConstOperatorName(expr.get(0).asString());
	}

//	private static IRObject _optimizeExpr(IRObject obj) throws RException {
//
//		if (obj.getType() != RType.EXPR) {
//			return null;
//		}
//
//		IRExpr expr = (IRExpr) obj;
//		int size = expr.size();
//		int update = 0;
//
//		/*******************************************************/
//		// Optimize child
//		/*******************************************************/
//		{
//
//			ArrayList<IRObject> optiArray = null;
//
//			for (int i = 0; i < size; ++i) {
//
//				IRObject e = expr.get(i);
//				IRObject optiE = null;
//
//				if ((optiE = _optimizeExpr(e)) != null) {
//
//					if (optiArray == null) {
//						optiArray = new ArrayList<>();
//
//						// Copy previous elements
//						for (int j = 0; j < i - 1; ++j) {
//							optiArray.add(expr.get(j));
//						}
//					}
//
//					optiArray.add(optiE);
//
//				} else {
//
//					if (optiArray != null) {
//						optiArray.add(e);
//					}
//				}
//			}
//
//			if (optiArray != null) {
//				expr = RulpFactory.createExpression(optiArray);
//				++update;
//			}
//		}
//
//		// (not (equal a b)) ==> (not-equal a b)
//		if (expr.size() == 2 && matchExprFactor(expr, F_B_NOT) && matchExprFactor(expr.get(1), F_EQUAL)) {
//
//			ArrayList<IRObject> optiArray = new ArrayList<>();
//			IRIterator<? extends IRObject> childExprIter = ((IRExpr) expr.get(1)).listIterator(1);
//			optiArray.add(RulpFactory.createAtom(F_NOT_EQUAL));
//			while (childExprIter.hasNext()) {
//				optiArray.add(childExprIter.next());
//			}
//
//			expr = RulpFactory.createExpression(optiArray);
//			++update;
//		}
//
//		return update > 0 ? expr : null;
//	}

	private static Pair<IRList, IRList> _optimizeRule_1(Pair<IRList, IRList> rule, IRInterpreter interpreter,
			IRFrame frame) throws RException {

		IRList conds = rule.getKey();
		IRList actions = rule.getValue();

		int searchIndex = 0;

		ArrayList<IRList> condList = new ArrayList<>();

		/*************************************/
		// Get Cond list
		/*************************************/
		IRIterator<? extends IRObject> iter = conds.iterator();
		while (iter.hasNext()) {

			IRObject cond = iter.next();
			if (cond.getType() == RType.LIST) {
				condList.add(RulpUtil.asList(cond));
			} else {
				condList.add(RulpUtil.asExpression(cond));
			}
		}

		HashSet<String> actionVars = null;

		/*************************************/
		// find (?x ?y ?z)
		/*************************************/
		for (; searchIndex < condList.size(); ++searchIndex) {

			IRList testCond = condList.get(searchIndex);

			if (testCond.getType() != RType.LIST) {
				continue;
			}

			if (!ReteUtil.isValidStmtLen(testCond.size())) {
				continue;
			}

			if (ReteUtil.varList(testCond).size() != testCond.size()) {
				continue;
			}

			ArrayList<IRList> newCondList = new ArrayList<>();
			for (IRList cond : condList) {
				if (cond != testCond) {
					newCondList.add(cond);
				}
			}

			HashSet<String> newCondVars = new HashSet<>(ReteUtil.varList(newCondList));

			if (actionVars == null) {
				actionVars = new HashSet<>(ReteUtil.varList(actions));
			}

			/*************************************************/
			// All action vars can be found in condition vars
			/*************************************************/
			if (!newCondVars.containsAll(actionVars)) {
				continue;
			}

			/*************************************************/
			// Try build match tree
			/*************************************************/

			try {
				if (MatchTree.build(newCondList, interpreter, frame) == null) {
					continue;
				}
			} catch (RException e) {
				continue;
			}

			// (if (has-stmt '(?x ?y ?z)) old-actions )

			IRExpr newAction = null;

			if (actions.size() == 1) {

				newAction = RulpFactory.createExpression(RulpFactory.createAtom(F_IF),
						RulpFactory.createExpression(RulpFactory.createAtom(F_HAS_STMT), testCond), actions.get(0));

			} else {

				ArrayList<IRObject> doList = new ArrayList<>();
				doList.add(RulpFactory.createAtom(A_DO));
				doList.addAll(ReteUtil.toExprList(actions));

				newAction = RulpFactory.createExpression(RulpFactory.createAtom(F_IF),
						RulpFactory.createExpression(RulpFactory.createAtom(F_HAS_STMT), testCond),
						RulpFactory.createExpression(doList));

			}

			IRList newConds = RulpFactory.createList(newCondList);
			IRList newActions = RulpFactory.createList(newAction);

			return new Pair<IRList, IRList>(newConds, newActions);

		}

		return null;
	}

	protected static IRObject _rebuildConstExpr(IRExpr expr, IRInterpreter interpreter, IRFrame frame)
			throws RException {

		if (!_containConstExpr(expr)) {
			return expr;
		}

		ArrayList<IRObject> elements = null;

		int constCount = 0;
		int size = expr.size();

		for (int i = 1; i < size; ++i) {

			boolean update = false;
			IRObject element = expr.get(i);

			if (element.getType() == RType.EXPR) {
				IRObject rst = _rebuildConstExpr((IRExpr) element, interpreter, frame);
				if (rst != element) {
					update = true;
					element = rst;
				}
			}

			if (isConstObject(element)) {
				constCount++;
			}

			if (update || elements != null) {
				if (elements == null) {
					elements = new ArrayList<>();
					for (int ii = 0; ii < i; ++ii) {
						elements.add(expr.get(ii));
					}
				}

				elements.add(element);
			}
		}

		IRObject rst = expr;
		if (elements != null) {
			rst = RulpFactory.createExpression(elements);
		}

		if (constCount == (expr.size() - 1) && isConstOperatorName(expr.get(0).asString())) {
			rst = RuntimeUtil.compute(rst, interpreter, frame);
		}

		return rst;
	}

	public static int getSharedNodeCount(IRModel model) throws RException {

		IReteNodeMatrix nodeMatrix = model.getNodeGraph().getNodeMatrix();

		int count = 0;

		for (RReteType reteType : SHARED_RETE_TYPES) {
			for (IRReteNode node : nodeMatrix.getNodeList(reteType)) {
				int ruleCount = model.getNodeGraph().getRelatedRules(node).size();
				if (ruleCount > 1) {
					count += ruleCount - 1;
				}
			}
		}

		return count;
	}

	public static boolean isConstObject(IRObject obj) {

		switch (obj.getType()) {
		case INT:
		case BOOL:
		case DOUBLE:
		case FLOAT:
		case LONG:
		case NIL:
		case STRING:
			return true;

		default:
			return false;
		}
	}

	public static boolean matchExprFactor(IRObject obj, String factorName) throws RException {

		if (obj.getType() != RType.EXPR) {
			return false;
		}

		IRObject e0 = ((IRExpr) obj).get(0);
		switch (e0.getType()) {
		case ATOM:
			return ((IRAtom) e0).getName().equals(factorName);

		default:
			return false;
		}

	}

	public static IRObject optimizeExpr(IRExpr expr, IRInterpreter interpreter, IRFrame frame) throws RException {

		OPT: while (true) {

			// (not (equal a b)) ==> (not-equal a b)
			if (expr.size() == 2 && OptimizeUtil.matchExprFactor(expr, F_B_NOT)
					&& OptimizeUtil.matchExprFactor(expr.get(1), F_EQUAL)) {

				ArrayList<IRObject> optiArray = new ArrayList<>();
				IRIterator<? extends IRObject> childExprIter = ((IRExpr) expr.get(1)).listIterator(1);
				optiArray.add(RulpFactory.createAtom(F_NOT_EQUAL));
				while (childExprIter.hasNext()) {
					optiArray.add(childExprIter.next());
				}

				expr = RulpFactory.createExpression(optiArray);
				continue OPT;
			}

			// (op ?a ?a)
			RRelationalOperator op = null;
			if (expr.size() == 3 && ReteUtil.getExprLevel(expr) == 1
					&& (op = ReteUtil.toRelationalOperator(expr.get(0).asString())) != null
					&& expr.get(1).asString().equals(expr.get(2).asString())) {

				switch (op) {
				case EQ: // (= ?a ?a) ==> true
					return O_True;
				case GE: // (>= ?a ?a) ==> true
					return O_True;
				case GT: // (> ?a ?a) ==> false
					return O_False;
				case LE: // (<= ?a ?a) ==> true
					return O_True;
				case LT: // (< ?a ?a) ==> false
					return O_False;
				case NE:// (!= ?a ?a) ==> false
					return O_False;

				default:
				}
			}

			// (op ?a (+ 1 2)) => const expression
			if (_containConstExpr(expr)) {
				IRObject rst = _rebuildConstExpr(expr, interpreter, frame);
				if (rst.getType() == RType.EXPR) {
					expr = (IRExpr) rst;
				} else {
					return rst;
				}
			}

			// (var-changed ?v xx -> (var-changed ?v ? xx)
			if (ReteUtil.isVarChangeExpr(expr) && expr.size() == 3) {

				List<IRObject> list = new ArrayList<>();
				list.add(expr.get(0));
				list.add(expr.get(1));
				list.add(RulpFactory.createAtom(ReteUtil.varChangeOldName(expr.get(1).asString())));
				list.add(expr.get(2));

				expr = RulpFactory.createExpression(list);
			}

			break;
		}

		return expr;
	}

//	public static IRObject optimizeExpr(IRObject obj) throws RException {
//
//		IRObject optiObj = null;
//
//		while ((optiObj = _optimizeExpr(obj)) != null) {
//			obj = optiObj;
//		}
//
//		return obj;
//	}

	public static IRList optimizeMatchTree(IRList matchTree) throws RException {

		if (!_canOptimizeMatchTree(matchTree)) {
			return matchTree;
		}

		return (IRList) _buildOptimizeMatchTree(matchTree);
	}

	public static Pair<IRList, IRList> optimizeRule(IRList condList, IRList actionList, IRInterpreter interpreter,
			IRFrame frame) throws RException {

		Pair<IRList, IRList> rule = new Pair<>(condList, actionList);

		do {

			Pair<IRList, IRList> rst = _optimizeRule_1(rule, interpreter, frame);
			if (rst != null) {
				rule = rst;
				continue;
			}

		} while (false);

		return rule;
	}

	public static <T> String toString(List<T> names) {

		StringBuffer sb = new StringBuffer();
		sb.append('[');

		for (T name : names) {
			if (sb.length() > 1) {
				sb.append(',');
			}
			sb.append("" + name);
		}
		sb.append(']');

		return sb.toString();
	}

}
