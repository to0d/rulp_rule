package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.A_By;
import static alpha.rulp.lang.Constant.F_B_NOT;
import static alpha.rulp.lang.Constant.F_EQUAL;
import static alpha.rulp.lang.Constant.F_NOT_EQUAL;
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
import static alpha.rulp.lang.Constant.O_B_AND;
import static alpha.rulp.lang.Constant.O_False;
import static alpha.rulp.lang.Constant.O_True;
import static alpha.rulp.rule.Constant.A_Order;
import static alpha.rulp.rule.Constant.F_HAS_STMT;
import static alpha.rulp.ximpl.node.RReteType.ALPH0;
import static alpha.rulp.ximpl.node.RReteType.ALPH1;
import static alpha.rulp.ximpl.node.RReteType.BETA0;
import static alpha.rulp.ximpl.node.RReteType.BETA1;
import static alpha.rulp.ximpl.node.RReteType.BETA2;
import static alpha.rulp.ximpl.node.RReteType.BETA3;
import static alpha.rulp.ximpl.node.RReteType.EXPR0;
import static alpha.rulp.ximpl.node.RReteType.EXPR1;
import static alpha.rulp.ximpl.node.RReteType.EXPR2;
import static alpha.rulp.ximpl.node.RReteType.RETE_TYPE_TOTAL;
import static alpha.rulp.ximpl.node.RReteType.ZETA0;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import alpha.rulp.ximpl.factor.XRFactorHasStmt;
import alpha.rulp.ximpl.model.IReteNodeMatrix;
import alpha.rulp.ximpl.node.IRReteNodeCounter;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.optimize.EROUtil;
import alpha.rulp.ximpl.optimize.OptUtil;

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

		int[] reteRefCount = new int[RETE_TYPE_TOTAL];

		int unRefCount = 0;

		public RefArray() {
			for (int i = 0; i < RETE_TYPE_TOTAL; ++i) {
				reteRefCount[i] = 0;
			}
		}
	}

	static class RuleCounter {
		IRReteNodeCounter counter;
		IRRule rule;
	}

	static boolean OPT_ERO = false;

	public static boolean OPT_RULE_HAS_STMT = false;

	static final RReteType SHARED_RETE_TYPES[] = { ALPH0, ALPH1, EXPR0, EXPR1, EXPR2, BETA0, BETA1, BETA2, BETA3,
			ZETA0 };

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

	protected static IRExpr _optimizeHasStmtExpr(IRExpr expr, List<IRObject> leftVarList) throws RException {

		if (!XRFactorHasStmt.isSimpleHasStmtExpr(expr)) {
			return expr;
		}

		/*********************************************************/
		// no LHS var
		// - (has-stmt '(?a ?b ?c)), ?a ?b ?c are all external vars
		// - (has-stmt '(a b c))
		/*********************************************************/
		if (leftVarList.isEmpty()) {
			return expr;
		}

		/*********************************************************/
		// no var
		// - (has-stmt '(a b c))
		/*********************************************************/
		ArrayList<IRObject> thisVarList = ReteUtil.buildVarList(expr);
		if (thisVarList.isEmpty()) {
			return expr;
		}

		/*********************************************************/
		// Optimization: has var, create index
		// before: '(?a ?b c) '(has-stmt '(?a x ?y))
		// after : '(?a ?b c) '(has-stmt '(?a x ?y) order by 0)
		/*********************************************************/
		List<IRObject> oldElements = null;
		List<IRObject> newList = null;

		int indexCount = 0;
		for (IRObject var : thisVarList) {

			if (!leftVarList.contains(var)) {
				continue;
			}

			if (oldElements == null) {

				// (has-stmt '(a b c))
				if (expr.size() == 2) {
					oldElements = RulpUtil.toArray((IRList) expr.get(1));
				} else {
					oldElements = RulpUtil.toArray((IRList) expr.get(2));
				}

			}

			int idx = oldElements.indexOf(var);
			if (idx != -1) {

				if (newList == null) {
					newList = RulpUtil.toArray(expr);
				}

				newList.add(RulpFactory.createAtom(A_Order));
				newList.add(RulpFactory.createAtom(A_By));
				newList.add(RulpFactory.createInteger(idx));
				indexCount++;
			}
		}

		/*********************************************************/
		// all var are LHS vars
		// - (?a ?b ?c) (has-stmt '(?a ?b ?c))
		/*********************************************************/
		if (indexCount == thisVarList.size()) {
			return expr;
		}

		/*********************************************************/
		// all var are external vars
		// - (has-stmt '(?a ?b ?c))
		/*********************************************************/
		if (indexCount == 0) {
			return expr;
		}

		return RulpFactory.createExpression(newList);
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

	private static Pair<IRList, IRList> _optRuleHasStmt_1(Pair<IRList, IRList> rule, IRInterpreter interpreter,
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

			HashSet<String> testCondVars = new HashSet<>(ReteUtil.varList(testCond));
			if (testCondVars.isEmpty()) {
				continue;
			}

			ArrayList<IRList> newCondList = new ArrayList<>();
			ArrayList<IRList> newListList = new ArrayList<>();
			for (IRList cond : condList) {
				if (cond != testCond) {
					newCondList.add(cond);
					if (cond.getType() == RType.LIST) {
						newListList.add(cond);
					}
				}
			}

			HashSet<String> newCondVars = new HashSet<>(ReteUtil.varList(newListList));

			/*************************************************/
			// All test vars should be found in other condition vars
			/*************************************************/
			if (!newCondVars.containsAll(testCondVars)) {
				continue;
			}

			/*************************************************/
			// All action vars can be found in condition vars
			/*************************************************/
			if (actionVars == null) {
				actionVars = new HashSet<>(ReteUtil.varList(actions));
			}
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

			IRExpr newAction = RulpUtil
					.toIfExpr(RulpFactory.createExpression(RulpFactory.createAtom(F_HAS_STMT), testCond), actions);

			return new Pair<IRList, IRList>(RulpFactory.createList(newCondList), RulpFactory.createList(newAction));

		}

		return null;
	}

	private static Pair<IRList, IRList> _optRuleHasStmt_2(Pair<IRList, IRList> rule, IRInterpreter interpreter,
			IRFrame frame) throws RException {

		IRList conds = rule.getKey();
		IRList actions = rule.getValue();

		ArrayList<IRList> condList = new ArrayList<>();
		ArrayList<IRExpr> exprList = new ArrayList<>();

		/*************************************/
		// Get Cond list
		/*************************************/
		IRIterator<? extends IRObject> iter = conds.iterator();
		NEXT: while (iter.hasNext()) {

			IRObject cond = iter.next();
			if (cond.getType() == RType.EXPR) {

				IRExpr expr = (IRExpr) cond;
				if (expr.size() > 0) {
					switch (expr.get(0).asString()) {
					case F_HAS_STMT:
						exprList.add(expr);
						continue NEXT;

					default:
						break;
					}
				}
			}

			condList.add((IRList) cond);
		}

		if (exprList.size() == 1) {
			return new Pair<IRList, IRList>(RulpFactory.createList(condList),
					RulpFactory.createList(RulpUtil.toIfExpr(exprList.get(0), actions)));
		}

		if (exprList.size() > 1) {
			return new Pair<IRList, IRList>(RulpFactory.createList(condList),
					RulpFactory.createList(RulpUtil.toIfExpr(RulpUtil.toExpr(O_B_AND, exprList), actions)));
		}

		return null;
	}

	private static IRObject _rebuildActionIndexVar(IRList condList, IRObject obj) throws RException {

		if (obj == null) {
			return null;
		}

		switch (obj.getType()) {
		case ATOM:
			String name = RulpUtil.asAtom(obj).getName();
			if (!ReteUtil.isIndexVarName(name)) {
				return obj;
			}

			int index = Integer.valueOf(name.substring(1));
			if (index < 0 || index >= condList.size()) {
				throw new RException("Invalid index var found: " + name);
			}

			IRObject cond = condList.get(index);
			if (!ReteUtil.isReteStmt(cond)) {
				throw new RException(String.format("Invalid rete stmt found at index %s: ", name, cond));
			}

			return cond;

		case EXPR:

			IRExpr expr = RulpUtil.asExpression(obj);
			ArrayList<IRObject> newArr = null;
			int size = expr.size();

			for (int pos = 0; pos < size; ++pos) {

				IRObject ex = expr.get(pos);
				IRObject ey = _rebuildActionIndexVar(condList, ex);
				if (ex != ey) {
					if (newArr == null) {
						newArr = new ArrayList<>();
						for (int i = 0; i < pos; ++i) {
							newArr.add(expr.get(i));
						}
					}
				}

				if (newArr != null) {
					newArr.add(ey);
				}
			}

			if (newArr == null) {
				return obj;
			}

			return RulpFactory.createExpression(newArr);

		default:

			return obj;
		}

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
				int ruleCount = model.getNodeGraph().listRelatedRules(node).size();
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

	public static IRExpr optimizeActionExpr(IRExpr expr, IRModel model) throws RException {

		if (OPT_ERO) {
			expr = RulpUtil.asExpression(EROUtil.rebuild(expr, model.getInterpreter(), model.getFrame()));
		}

		return expr;
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
			if (expr.size() == 3 && OptUtil.getExprLevel(expr) == 1
					&& (op = RulpUtil.toRelationalOperator(expr.get(0).asString())) != null
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

	public static IRExpr optimizeHasStmtExpr(IRExpr expr, List<IRObject> leftVarList) throws RException {

		int size = expr.size();
		if (size <= 1) {
			return expr;
		}

		if (RulpUtil.isFactor(expr.get(0), F_HAS_STMT)) {
			return _optimizeHasStmtExpr(expr, leftVarList);
		}

		ArrayList<IRObject> newList = null;

		for (int i = 1; i < size; ++i) {

			IRObject ex = expr.get(i);
			if (ex != null && ex.getType() != RType.EXPR) {
				if (newList != null) {
					newList.add(ex);
				}
				continue;
			}

			IRExpr oldExpr = (IRExpr) ex;
			IRExpr newExpr = optimizeHasStmtExpr(oldExpr, leftVarList);
			if (newExpr == oldExpr) {
				continue;
			}

			if (newList == null) {
				newList = new ArrayList<>();
				for (int j = 0; j < i; ++j) {
					newList.add(expr.get(j));
				}
			}
			newList.add(newExpr);
		}

		if (newList != null) {
			expr = RulpFactory.createExpression(newList);
		}

		return expr;
	}

	public static IRList optimizeMatchTree(IRList matchTree) throws RException {

		if (!_canOptimizeMatchTree(matchTree)) {
			return matchTree;
		}

		return (IRList) _buildOptimizeMatchTree(matchTree);
	}

	public static IRList optimizeRuleRemoveUnusedCondition(IRList condList, IRList actionList) throws RException {

//		Set<String> actionVars = new HashSet<>();
//
//		ReteUtil.fillVarList(actionList, actionVars);

		return condList;
	}

	static boolean _equal(Set<String> a, Set<String> b) throws RException {

		if (a.size() != b.size()) {
			return false;
		}

		for (String x : a) {
			if (!b.contains(x)) {
				return false;
			}
		}

		return true;
	}

	static Set<String> _join(Set<String> a, Set<String> b) throws RException {

		HashSet<String> c = new HashSet<>(a);
		c.retainAll(b);

		return c;
	}

//	public static IRList _optimizeMatchTreeUnusedVar(IRList reteTree, Set<String> actionVarSet) throws RException {
//
//		Set<String> treeVarSet = _toVarSet(reteTree);
//		Set<String> joinVarSet = _join(treeVarSet, actionVarSet);
//
//		if (_equal(treeVarSet, joinVarSet)) {
//			return reteTree;
//		}
//
//		RType treeType = reteTree.getType();
//		int treeSize = reteTree.size();
//		IRObject e0 = reteTree.get(0);
//
//		RType e0Type = reteTree.get(0).getType();
//		RType e1Type = treeSize >= 2 ? reteTree.get(1).getType() : null;
//
//		// alpha node: '(a b c)
//		if (treeType == RType.LIST && ReteUtil.isEntryValueType(e0Type)) {
//
//			return _buildAlphaNode(reteTree, tmpVarBuilder);
//		}
//
//		// Beta0: '('(a b c) '(x y z))
//		if (ReteUtil.isBetaTree(reteTree, treeSize)) {
//			return _buildBetaNode(reteTree, tmpVarBuilder);
//		}
//
//		// Beta1 or Expr
//		if (treeSize == 2 && treeType == RType.LIST && e0Type == RType.LIST && e1Type == RType.EXPR) {
//
//			IRList l1 = (IRList) reteTree.get(1);
//
//			// Build beta1 node:
//			// - beta1 node: '('(a b ?v) (var-changed ?var v1 ?v)) // have join vars
//			// - beta1 node: '('(a b c) (var-changed ?var v1 v2)) // no join vars
//			if (ReteUtil.isVarChangeExpr(l1)) {
//				return _buildBetaNode(reteTree, tmpVarBuilder);
//			}
//
//			// Build expr node: '('(a b c) (x y z))
//			return _buildExprNode(reteTree, tmpVarBuilder);
//		}
//
//		// (var-changed)
//		if (ReteUtil.isVarChangeExpr(reteTree)) {
//
//			// (var-changed ?State Running Completed)
//			if (e0Type == RType.ATOM) {
//				return _buildVarNode(reteTree, tmpVarBuilder);
//			}
//
//			// ((var-changed ?s1 ?v1 v2) (not-equal ?v1 a))
//			if (ReteUtil.isVarChangeExpr(e0) && treeSize == 2 && e1Type == RType.EXPR) {
//				return _buildExprNode(reteTree, tmpVarBuilder);
//			}
//		}
//
//		// (inherit '(?a ?b ?c) 0)
//		if (ReteUtil.isInheritExpr(reteTree)) {
//			return _buildInheritNode(reteTree, tmpVarBuilder);
//		}
//
//		// beta3: '(?a b c) '(?x y z) (not-equal ?a ?x)
//		if (ReteUtil.isBeta3Tree(reteTree, treeSize)) {
//			return _buildBetaNode(reteTree, tmpVarBuilder);
//		}
//
//		// beta:'((var-changed ?x ?xv) (var-changed ?y ?yv))
//		if (treeSize == 2 && treeType == RType.LIST && ReteUtil.isVarChangeExpr(e0)
//				&& ReteUtil.isVarChangeExpr(reteTree.get(1))) {
//			return _buildBetaNode(reteTree, tmpVarBuilder);
//		}
//
//		int ModifierCount = ReteUtil.getReteTreeModifierCount(reteTree);
//		if (ModifierCount > 0 && treeSize > ModifierCount) {
//
//			ArrayList<IRObject> newReteTreeList = new ArrayList<>();
//			int newSize = treeSize - ModifierCount;
//			for (int i = 0; i < newSize; ++i) {
//				newReteTreeList.add(reteTree.get(i));
//			}
//
//			AbsReteNode processNode = _buildReteNode(RulpFactory.createList(newReteTreeList), tmpVarBuilder);
//			for (int i = newSize; i < treeSize; ++i) {
//				processNode = _processNodeModifier(processNode, RulpUtil.asAtom(reteTree.get(i)).asString());
//			}
//
//			processNode.setUniqName(ReteUtil.uniqName(reteTree));
//
//			return processNode;
//		}
//
////		// beta:((var-changed ?x ?xv) url-entry:'(?url-name ?url))
////		if (treeSize == 2 && treeType == RType.EXPR && ReteUtility.isVarChangeExpr(e0) && e1Type == RType.LIST) {
////			return _buildBetaNode(reteTree, tmpVarBuilder);
////		}
//
//		// beta3: '(?a b c) '(?x y z) (not-equal ?a ?x)
//		if (ReteUtil.isZetaTree(reteTree, treeSize)) {
//			return _buildZetaNode(reteTree, tmpVarBuilder);
//		}
//
//	}

	static Set<String> _toVarSet(IRList list) throws RException {
		HashSet<String> varNames = new HashSet<>();
		ReteUtil.buildVarList(list, new ArrayList<>(), varNames);
		return varNames;
	}

	public static IRList optimizeMatchTree(IRList matchTree, List<IRExpr> actionExprList) throws RException {

		/***************************************************/
		// optimize unused beta var
		// before: '('(?a p ?b) '(?a p ?c)) -> (func ?a)
		// after : '('(?a p ?b) (inherit '(?a p ?c) 0)
		/***************************************************/
		HashSet<String> actionVarSet = new HashSet<>();
		for (IRObject var : ReteUtil.buildVarList(actionExprList)) {
			actionVarSet.add(var.asString());
		}

		return matchTree;
	}

	public static IRList optimizeRuleActionIndexVar(IRList condList, IRList actionList) throws RException {

		ArrayList<IRObject> newList = null;

		int size = actionList.size();

		for (int pos = 0; pos < size; ++pos) {

			IRObject ex = actionList.get(pos);

			/***************************************************/
			// before: if '(?x p ?y) '(?y p ?z) do (remove-stmt ?0)
			// after : if '(?x p ?y) '(?y p ?z) do (remove-stmt '(?x p ?y))
			/***************************************************/
			IRObject ey = _rebuildActionIndexVar(condList, ex);
			if (ex != ey) {
				if (newList == null) {
					newList = new ArrayList<>();
					for (int i = 0; i < pos; ++i) {
						newList.add(actionList.get(i));
					}
				}
			}

			if (newList != null) {
				newList.add(ey);
			}
		}

		if (newList == null) {
			return actionList;
		}

		return RulpFactory.createList(newList);
	}

	public static Pair<IRList, IRList> optimizeRuleHasStmt(IRList condList, IRList actionList,
			IRInterpreter interpreter, IRFrame frame) throws RException {

		Pair<IRList, IRList> rule = new Pair<>(condList, actionList);

		do {

			/***************************************************/
			// before: if xxx '(yyy) do zzz
			// after : if xxx do (if (has-stmt '(yyy)) zzz)
			//
			// '(yyy) does not have any new var
			/***************************************************/
			Pair<IRList, IRList> rst = _optRuleHasStmt_1(rule, interpreter, frame);
			if (rst != null) {
				rule = rst;
				continue;
			}

			/***************************************************/
			// before: if xxx (has-stmt yyy) do zzz
			// after : if xxx do (if (has-stmt yyy)) zzz)
			/***************************************************/
			rst = _optRuleHasStmt_2(rule, interpreter, frame);
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
