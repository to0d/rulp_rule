package alpha.rulp.ximpl.node;

import static alpha.rulp.lang.Constant.F_EQUAL;
import static alpha.rulp.rule.Constant.A_ENTRY_LEN;
import static alpha.rulp.rule.Constant.A_ENTRY_ORDER;
import static alpha.rulp.rule.Constant.A_Inherit;
import static alpha.rulp.rule.Constant.A_Order_by;
import static alpha.rulp.rule.Constant.A_RETE_TYPE;
import static alpha.rulp.rule.Constant.A_Uniq;
import static alpha.rulp.rule.Constant.DEF_GC_INACTIVE_LEAF;
import static alpha.rulp.rule.Constant.F_VAR_CHANGED;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DISABLED;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_INACTIVE;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.Constant.STMT_MAX_LEN;
import static alpha.rulp.rule.RCountType.AssumeCount;
import static alpha.rulp.rule.RCountType.BindFromCount;
import static alpha.rulp.rule.RCountType.BindToCount;
import static alpha.rulp.rule.RCountType.CreateEntry;
import static alpha.rulp.rule.RCountType.DefinedCount;
import static alpha.rulp.rule.RCountType.DeleteEntry;
import static alpha.rulp.rule.RCountType.DropCount;
import static alpha.rulp.rule.RCountType.ExecCount;
import static alpha.rulp.rule.RCountType.FailedCount;
import static alpha.rulp.rule.RCountType.FixedCount;
import static alpha.rulp.rule.RCountType.IdleCount;
import static alpha.rulp.rule.RCountType.MatchCount;
import static alpha.rulp.rule.RCountType.MaxLevel;
import static alpha.rulp.rule.RCountType.MaxPriority;
import static alpha.rulp.rule.RCountType.MinPriority;
import static alpha.rulp.rule.RCountType.NodeCount;
import static alpha.rulp.rule.RCountType.NullCount;
import static alpha.rulp.rule.RCountType.QueryFetch;
import static alpha.rulp.rule.RCountType.QueryMatch;
import static alpha.rulp.rule.RCountType.ReasonCount;
import static alpha.rulp.rule.RCountType.RedundantCount;
import static alpha.rulp.rule.RCountType.RemoveCount;
import static alpha.rulp.rule.RCountType.SourceCount;
import static alpha.rulp.rule.RCountType.TempCount;
import static alpha.rulp.rule.RCountType.UpdateCount;
import static alpha.rulp.rule.RReteStatus.ASSUME;
import static alpha.rulp.rule.RReteStatus.DEFINE;
import static alpha.rulp.rule.RReteStatus.FIXED_;
import static alpha.rulp.rule.RReteStatus.REASON;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRReteNode.InheritIndex;
import alpha.rulp.rule.IRReteNode.JoinIndex;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.rule.RCountType;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.AttrUtil;
import alpha.rulp.utils.HeapStack;
import alpha.rulp.utils.MatchTree;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.action.ActionUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.cache.IRCacheWorker;
import alpha.rulp.ximpl.cache.IRCacheWorker.CacheStatus;
import alpha.rulp.ximpl.constraint.ConstraintFactory;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.REntryQueueType;
import alpha.rulp.ximpl.model.IReteNodeMatrix;
import alpha.rulp.ximpl.model.XRUniqObjBuilder;

public class XRNodeGraph implements IRNodeGraph {

	static class ActivateInfo {
		public IRReteNode node;
		public int oldPriority = -1;
	}

	static class ReteNodeList {
		public ArrayList<AbsReteNode> nodes = new ArrayList<>();
	}

	static class SourceInfo {

		public int lastRuleIndex = -1;

		public int lastRuleUpdateId = -1;

		public Set<SourceNode> sourceNodes = null;

		public RReteType sourceType;

		public String stmtName = null;

		public int stmtSize = 0;

		public String uniqName = null;

		public IRList uniqStmt = null;

		public int varCount = 0;

		public boolean match(IRList actionUniqStmt) throws RException {

			switch (sourceType) {
			case ROOT0:
			case NAME0:
				return actionUniqStmt.size() == stmtSize && RuleUtil.equal(actionUniqStmt.getNamedName(), stmtName);

			case ALPH0:
			case CONST:
				return ReteUtil.matchUniqStmt(actionUniqStmt, uniqStmt);

			default:
				return false;
			}

		}

		public void update(XRNodeGraph graph, IRRule rule) throws RException {

			XRGraphInfo ruleNodeInfo = (XRGraphInfo) rule.getGraphInfo();
			SourceNode sourceNode = null;

			NEXT_ACTION: for (IAction action : rule.getActionList()) {

				for (IRList actionUniqStmt : ruleNodeInfo.getRuleActionUniqStmt(action, graph)) {

					if (match(actionUniqStmt)) {
						if (sourceNode == null) {
							sourceNode = new SourceNode();
							sourceNode.rule = rule;
							sourceNode.uniqStmt = uniqName;
						}

						sourceNode.actionList.add(action);
						continue NEXT_ACTION;
					}
				}

			}

			if (sourceNode != null) {
				sourceNodes.add(sourceNode);
			}
		}

	}

	public static boolean isSymmetricBetaNode(IRReteNode node) {

		if (!RReteType.isBetaType(node.getReteType())) {
			return false;
		}

		if (node.getParentNodes()[0] != node.getParentNodes()[1]) {
			return false;
		}

		IRBetaNode betaNode = (IRBetaNode) node;

		List<JoinIndex> joinIndexs = betaNode.getJoinIndexList();
		if (joinIndexs != null) {
			for (JoinIndex ji : joinIndexs) {
				if (ji.leftIndex != ji.rightIndex) {
					return false;
				}
			}
		}

		return true;
	}

	protected Map<IRReteNode, List<IRReteNode>> _affectNodeMap = null;

	protected Map<IRReteNode, IRNodeSubGraph> _constraintCheckSubGraphMap = null;

	protected Map<String, IRNodeSubGraph> _ruleGroupSubGraphMap = null;

	protected Map<IRReteNode, IRNodeSubGraph> _sourceSubGraphMap = null;

	protected int anonymousRuleIndex = 0;

	protected int anonymousWorkIndex = 0;

	protected final XRGraphCount counter = new XRGraphCount();

	protected IREntryTable entryTable;

	protected int gcMaxCacheNodeCount = 0;

	protected int gcMaxInactiveLeafCount = DEF_GC_INACTIVE_LEAF;

	protected long gcRemoveNodeCountArray[] = new long[RCountType.COUNT_TYPE_NUM];

	protected int maxNodeIndex = 0;

	protected int maxRootStmtLen = 0;

	protected int maxRuleId = -1;

	protected IRModel model;

	protected final Map<String, AbsReteNode> namedNodeMap = new HashMap<>();

	protected ArrayList<IRReteNode> nodeInfoArray = new ArrayList<>();

	protected final ReteNodeList[] nodeListArray = new ReteNodeList[RReteType.RETE_TYPE_NUM];

	protected final Map<String, AbsReteNode> nodeUniqNameMap = new HashMap<>();

	protected AbsReteNode[] rootNodeArray = new AbsReteNode[STMT_MAX_LEN + 1];

	protected final Map<String, XRNodeRule0> ruleNodeMap = new HashMap<>();

	protected int ruleUpdateId = 0;

	protected Map<String, SourceInfo> sourceInfoMap = new HashMap<>();

	protected XRUniqObjBuilder uniqBuilder = new XRUniqObjBuilder();

	protected final Map<String, IRReteNode> varNodeMap = new HashMap<>();

	public XRNodeGraph(IRModel model, IREntryTable entryTable) {

		this.model = model;
		this.entryTable = entryTable;

		for (int i = 0; i < RReteType.RETE_TYPE_NUM; ++i) {
			this.nodeListArray[i] = new ReteNodeList();
		}

		for (int i = 0; i < RCountType.COUNT_TYPE_NUM; ++i) {
			this.gcRemoveNodeCountArray[i] = 0;
		}
	}

	protected void _addNode(AbsReteNode node) throws RException {

		String uniqName = node.getUniqName();
		if (this.nodeUniqNameMap.containsKey(uniqName)) {
			return;
		}

		RReteType reteType = node.getReteType();

		switch (reteType) {

		case RULE:
			XRNodeRule0 ruleNode = (XRNodeRule0) node;
			this.ruleNodeMap.put(ruleNode.getRuleName(), (XRNodeRule0) node);
			break;

		case ALPH0:
			if (!ReteUtil.isUniqReteStmt(_toList(node.getUniqName()))) {
				throw new RException("invalid uniqname: " + node);
			}
			break;

		default:
			break;
		}

		if (node.getReteType() != RReteType.NAME0 && node.getReteTree() != null
				&& node.getReteTree().getNamedName() != null) {
			node.setNamedName(node.getReteTree().getNamedName());
		}

		node.setGraphInfo(new XRGraphInfo(node));
		this.nodeListArray[reteType.getIndex()].nodes.add(node);
		this.nodeUniqNameMap.put(uniqName, node);

		int nodeId = node.getNodeId();
		while (this.nodeInfoArray.size() <= nodeId) {
			this.nodeInfoArray.add(null);
		}
		this.nodeInfoArray.set(nodeId, node);

		_graphChanged();

		AttrUtil.setAttribute(node, A_RETE_TYPE, RulpFactory.createInteger(node.getReteType().getIndex()));
		AttrUtil.setAttribute(node, A_ENTRY_LEN, RulpFactory.createInteger(node.getEntryLength()));
	}

	protected AbsReteNode _buildAlphaNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		if (!ReteUtil.isAlphaMatchTree(reteTree)) {
			throw new RException("Invalid alpha tree node found: " + reteTree);
		}

		String namedName = reteTree.getNamedName();
		int stmtLen = reteTree.size();

		/***********************************************/
		// Match same variable 1
		// (?x ?x ?x) ==> (?x ?y ?z) and ?x=?y & ?x=?z
		/***********************************************/
		if (reteTree.size() > 1 && RulpUtil.isVarAtom(reteTree.get(0))) {

			boolean notSameFound = false;

			IRObject var = reteTree.get(0);
			for (int i = 1; i < stmtLen; ++i) {
				if (reteTree.get(i) != var) {
					notSameFound = true;
					break;
				}
			}

			if (!notSameFound) {

				List<IRObject> list = RulpUtil.toArray(reteTree);
				list.set(0, tmpVarBuilder.next());

				IRReteNode parentNode = _findReteNode(RulpFactory.createNamedList(namedName, list), tmpVarBuilder);

				IRObject[] varEntry = ReteUtil._varEntry(parentNode.getVarEntry());
				varEntry[0] = null;

				AbsReteNode alph0Node = RNodeFactory.createAlpha0Node(model, _getNextNodeId(),
						ReteUtil.uniqName(reteTree), stmtLen, parentNode, varEntry);

				addConstraint(alph0Node, ConstraintFactory.cmpEntryIndex(RRelationalOperator.EQ, 0, 1));

				return alph0Node;
			}
		}

		/***********************************************/
		// Match same variable
		// (?x p ?x) ==> (?x p ?y) and ?x=?y
		/***********************************************/
		{
			int lastVarPos = stmtLen - 1;
			IRObject lastVar = null;

			for (; lastVar == null && lastVarPos >= 0; --lastVarPos) {
				IRObject obj = reteTree.get(lastVarPos);
				if (RulpUtil.isVarAtom(obj)) {
					lastVar = obj;
					break;
				}
			}

			// find same variable object
			if (lastVarPos > 0) {

				int lastSamePos = lastVarPos - 1;
				for (; lastSamePos >= 0; --lastSamePos) {
					if (reteTree.get(lastSamePos) == lastVar) {
						break;
					}
				}

				// Same object found, link to (?x p ?y)
				if (lastSamePos >= 0) {

					List<IRObject> list = RulpUtil.toArray(reteTree);
					list.set(lastVarPos, tmpVarBuilder.next());

					IRReteNode parentNode = _findReteNode(RulpFactory.createNamedList(namedName, list), tmpVarBuilder);

					IRObject[] varEntry = ReteUtil._varEntry(parentNode.getVarEntry());
					varEntry[lastVarPos] = null;

					AbsReteNode alph0Node = RNodeFactory.createAlpha0Node(model, _getNextNodeId(),
							ReteUtil.uniqName(reteTree), stmtLen, parentNode, varEntry);

					addConstraint(alph0Node,
							ConstraintFactory.cmpEntryIndex(RRelationalOperator.EQ, lastSamePos, lastVarPos));

					return alph0Node;
				}
			}
		}

		/***********************************************/
		// Find the the first value from tail
		// (?x ?y b) ==> b
		/***********************************************/
		{
			IRObject lastValue = null;
			int lastValuePos = stmtLen - 1;

			for (; lastValue == null && lastValuePos >= 0; --lastValuePos) {
				IRObject obj = reteTree.get(lastValuePos);
				if (!RulpUtil.isVarAtom(obj)) {
					lastValue = obj;
					break;
				}
			}

			/***********************************************/
			// not value found, link to root node
			/***********************************************/
			if (lastValuePos == -1) {
				return createNodeRoot(namedName, stmtLen);

			} else {

				List<IRObject> list = RulpUtil.toArray(reteTree);

				list.set(lastValuePos, tmpVarBuilder.next());

				IRReteNode parentNode = _findReteNode(RulpFactory.createNamedList(namedName, list), tmpVarBuilder);

				AbsReteNode alph0Node = RNodeFactory.createAlpha0Node(model, _getNextNodeId(),
						ReteUtil.uniqName(reteTree), stmtLen, parentNode,
						ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));
				addConstraint(alph0Node,
						ConstraintFactory.cmpEntryValue(RRelationalOperator.EQ, lastValuePos, lastValue));

				return alph0Node;
			}
		}
	}

	protected AbsReteNode _buildBetaNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		IRObject e0 = reteTree.get(0);
		IRObject e1 = reteTree.get(1);

		IRList leftTree = e0.getType() == RType.EXPR ? RulpUtil.asExpression(e0) : RulpUtil.asList(e0);
		IRList rightTree = e1.getType() == RType.EXPR ? RulpUtil.asExpression(e1) : RulpUtil.asList(e1);

		ArrayList<IRObject> unProcessNodes = new ArrayList<>();

		IRReteNode leftNode = _findReteNode(leftTree, tmpVarBuilder);
		IRReteNode rightNode = _findReteNode(rightTree, tmpVarBuilder);

		{
			IRIterator<? extends IRObject> it = reteTree.listIterator(2);
			while (it.hasNext()) {
				unProcessNodes.add(it.next());
			}
		}

		ArrayList<IRObject> thisVarList = ReteUtil.buildTreeVarList(reteTree);

		int beteEntryLen = thisVarList.size();
		if (beteEntryLen == 0) {
			throw new RException("Invalid beta entry length found: " + reteTree);
		}

		Map<String, Integer> leftVarIndexMap = new HashMap<>();
		Map<String, Integer> rightVarIndexMap = new HashMap<>();
		ReteUtil.buildTreeVarList(leftTree, leftVarIndexMap);
		ReteUtil.buildTreeVarList(rightTree, rightVarIndexMap);

		/*********************************************/
		// Build Inherit & Join index
		/*********************************************/
		InheritIndex[] inheritIndexs = new InheritIndex[beteEntryLen];
		ArrayList<JoinIndex> joinIndexList = new ArrayList<>();

		{

			for (int i = 0; i < beteEntryLen; ++i) {

				IRObject thisVar = thisVarList.get(i);
				String thisVarNamne = RulpUtil.asAtom(thisVar).getName();

				Integer leftIndex = leftVarIndexMap.get(thisVarNamne);
				Integer rightIndex = rightVarIndexMap.get(thisVarNamne);

				// Variable exist in left
				if (leftIndex != null) {

					inheritIndexs[i] = new InheritIndex(0, leftIndex);

					// Variable exist in both left and right
					if (rightIndex != null) {
						joinIndexList.add(new JoinIndex(leftIndex, rightIndex));
					}
				}
				// Variable only exist in right
				else if (rightIndex != null) {

					inheritIndexs[i] = new InheritIndex(1, rightIndex);

				} else {

					throw new RException(String.format("var<%s> was not found in both left and right: %s",
							thisVar.toString(), reteTree.toString()));
				}
			}

		}

		// Optimize: '(?x ?y ?z) '(?a ?b ?c) (equal ?x ?a) ==> '(?x ?y ?z) '(?x ?b ?c)
		{
			Iterator<IRObject> it = unProcessNodes.iterator();
			NEXT_NODE: while (it.hasNext()) {

				IRObject obj = it.next();
				if (obj.getType() == RType.EXPR) {

					IRList expr = RulpUtil.asExpression(obj);
					if (expr.size() == 3 && OptimizeUtil.matchExprFactor(expr, F_EQUAL)) {

						IRObject v1 = expr.get(1);
						IRObject v2 = expr.get(2);

						if (RulpUtil.isVarAtom(v1) && RulpUtil.isVarAtom(v2)) {

							String v1Name = RulpUtil.asAtom(v1).getName();
							String v2Name = RulpUtil.asAtom(v2).getName();

							// '(?x ?y ?z) '(?a ?b ?c) (equal ?x ?a)
							if (leftVarIndexMap.containsKey(v1Name) && rightVarIndexMap.containsKey(v2Name)) {

								int leftIndex = leftVarIndexMap.get(v1Name);
								int rightIndex = rightVarIndexMap.get(v2Name);

								joinIndexList.add(new JoinIndex(leftIndex, rightIndex));

								it.remove();

								continue NEXT_NODE;
							}

							// '(?x ?y ?z) '(?a ?b ?c) (equal ?a ?x)
							if (leftVarIndexMap.containsKey(v2Name) && rightVarIndexMap.containsKey(v1Name)) {

								int leftIndex = leftVarIndexMap.get(v2Name);
								int rightIndex = rightVarIndexMap.get(v1Name);

								joinIndexList.add(new JoinIndex(leftIndex, rightIndex));

								it.remove();

								continue NEXT_NODE;
							}
						}
					}
				}
			}
		}

		IRObject[] varEntry = ReteUtil._varEntry(thisVarList);

		if (unProcessNodes.isEmpty() && (rightNode.getReteType() == RReteType.ALPH1
				|| rightNode.getReteType() == RReteType.VAR || rightNode.getReteType() == RReteType.CONST)) {
			return RNodeFactory.createBeta1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), beteEntryLen,
					leftNode, rightNode, varEntry, inheritIndexs, joinIndexList);

		}

		if (joinIndexList.isEmpty()) {

			IRConstraint1 constraint = null;

			if (reteTree.size() == 3 && reteTree.get(2).getType() == RType.EXPR) {

				IRExpr expr = RulpUtil.asExpression(reteTree.get(2));
				// Optimize: '(?x ?y ?z) '(?a ?b ?c) (equal ?x ?a) ==> '(?x ?y ?z) '(?x ?b ?c)

				constraint = ConstraintFactory.expr1(expr, thisVarList);
				if (constraint == null) {
					constraint = ConstraintFactory.expr0(expr, varEntry, this.model.getFrame());
				}

				if (constraint == null) {
					throw new RException("unlinked expr: " + expr);
				}
			}

			AbsReteNode node = RNodeFactory.createBeta3Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
					beteEntryLen, leftNode, rightNode, varEntry, inheritIndexs);

			// match mode
			if (constraint != null) {
				addConstraint(node, constraint);
			}

			return node;
		}

		// The left is the main inherit parent. The entry from the main inherit parent
		// can decide the child(beta) entry by its own. Since the parent's entry is
		// unique, so that we can ignore the main inherit's entry once it generate a
		// child entry.
		if (leftNode.getEntryLength() == beteEntryLen && ReteUtil.getMainInheritIndex(inheritIndexs) == 0) {
			return RNodeFactory.createBeta2Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), beteEntryLen,
					leftNode, rightNode, varEntry, inheritIndexs, joinIndexList);
		}

		return RNodeFactory.createBeta0Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), beteEntryLen,
				leftNode, rightNode, varEntry, inheritIndexs, joinIndexList);

	}

	protected AbsReteNode _buildConstNode(IRList constStmt) throws RException {

		if (!ReteUtil.isAlphaMatchTree(constStmt) || ReteUtil.getStmtVarCount(constStmt) != 0) {
			throw new RException("Invalid const tree node found: " + constStmt);
		}

		int stmtLen = constStmt.size();
		IRReteNode parentNode = createNodeRoot(null, stmtLen);

		return RNodeFactory.createConstNode(model, _getNextNodeId(), constStmt, entryTable, parentNode);
	}

	protected AbsReteNode _buildExprNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		IRList leftTree = (IRList) reteTree.get(0);
		IRExpr rightExpr = RulpUtil.asExpression(reteTree.get(1));

		IRReteNode leftNode = _findReteNode(leftTree, tmpVarBuilder);
		ArrayList<IRObject> leftVarList = ReteUtil.buildVarList(leftTree);

		/*********************************************************/
		// Check Entry length
		/*********************************************************/
		if (leftVarList.size() == 0) {
			throw new RException("Invalid expr entry length found: " + reteTree);
		}

		ArrayList<IRObject> rightVarList = ReteUtil.buildVarList(rightExpr);
		ArrayList<IRObject> externalVarList = new ArrayList<>();

		for (IRObject v : rightVarList) {
			if (!leftVarList.contains(v)) {
				externalVarList.add(v);
			}
		}

		if (!externalVarList.isEmpty()) {

			/*********************************************************/
			// Expr2: '(?a ?b ?c) (factor ?a ?x) - has left variable in expr
			/*********************************************************/
			if (externalVarList.size() != rightVarList.size()) {

				IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree));
				int entryLen = leftNode.getEntryLength();

				IRExpr newRightExpr = OptimizeUtil.optimizeHasStmtExpr(rightExpr, leftVarList);
				if (newRightExpr != rightExpr) {
					reteTree = RulpFactory.createList(leftTree, newRightExpr);
					rightExpr = newRightExpr;
				}

				AbsReteNode node = RNodeFactory.createExpr2Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
						entryLen, leftNode, varEntry);

				addConstraint(node, ConstraintFactory.expr0(rightExpr,
						ReteUtil._varEntry(ReteUtil.buildTreeVarList(leftTree)), this.model.getFrame()));

				return node;

			}
			/*********************************************************/
			// Expr3: '(?a ?b ?c) (factor ?x) - no left variable in expr
			/*********************************************************/
			else {

				IRObject[] leftVarEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(leftTree));
				IRObject[] rightVarEntry = ReteUtil._varEntry(rightVarList);

				IRObject[] varEntry = new IRObject[leftVarEntry.length + rightVarEntry.length];
				for (int i = 0; i < leftVarEntry.length; ++i) {
					varEntry[i] = leftVarEntry[i];
				}

				for (int i = 0; i < rightVarEntry.length; ++i) {
					varEntry[leftVarEntry.length + i] = rightVarEntry[i];
				}

				AbsReteNode node = RNodeFactory.createExpr3Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
						varEntry.length, leftNode.getEntryLength(), leftNode, varEntry);

				// constant
				addConstraint(node, ConstraintFactory.expr3(rightExpr, model.getFrame()));

				return node;
			}
		}

		/*********************************************************/
		// Expr1: (equal ?a b) or (not-equal ?a b)
		/*********************************************************/
		// alpha node
		if (RReteType.isAlphaType(leftNode.getReteType()) || RReteType.isRootType(leftNode.getReteType())) {

			List<IRObject> alphaVarList = RulpUtil.toArray(leftTree);

			IRConstraint1 expr1MatchNode = ConstraintFactory.expr1(rightExpr, alphaVarList);
			if (expr1MatchNode != null) {

				AbsReteNode node = RNodeFactory.createExpr1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
						leftNode.getEntryLength(), leftNode, leftNode.getVarEntry());

				// constant
				addConstraint(node, expr1MatchNode);

				return node;
			}

		}
		// expr 1
		else if (leftNode.getReteType() == RReteType.EXPR1 || leftNode.getReteType() == RReteType.EXPR2) {

			// Find left last alpha node
			IRReteNode leftAlphaNode = leftNode.getParentNodes()[0];
			IRObject leftParentObj = leftTree.get(0);

			while (leftAlphaNode.getReteType() == RReteType.EXPR1) {
				leftAlphaNode = leftAlphaNode.getParentNodes()[0];
				leftParentObj = RulpUtil.asList(leftParentObj).get(0);
			}

			if (RReteType.isAlphaType(leftAlphaNode.getReteType()) || leftAlphaNode.getReteType() == RReteType.ROOT0) {

				List<IRObject> alphaVarList = RulpUtil.toArray(RulpUtil.asList(leftParentObj));

				IRConstraint1 expr1MatchNode = ConstraintFactory.expr1(rightExpr, alphaVarList);
				if (expr1MatchNode != null) {
					AbsReteNode node = RNodeFactory.createExpr1Node(model, _getNextNodeId(),
							ReteUtil.uniqName(reteTree), leftNode.getEntryLength(), leftNode, leftNode.getVarEntry());
					addConstraint(node, expr1MatchNode);
					return node;
				}
			}

		} else {

			IRConstraint1 expr1MatchNode = ConstraintFactory.expr1(rightExpr, leftVarList);
			if (expr1MatchNode != null) {
				AbsReteNode node = RNodeFactory.createExpr1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
						leftVarList.size(), leftNode, ReteUtil._varEntry(leftVarList));
				addConstraint(node, expr1MatchNode);
				return node;
			}
		}

		// Other node

		/*********************************************************/
		// Expr0: (factor ?a b)
		/*********************************************************/
		IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree));
		int entryLen = leftNode.getEntryLength();

		AbsReteNode node = RNodeFactory.createExpr0Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), entryLen,
				leftNode, varEntry);

		// constant
		addConstraint(node, ConstraintFactory.expr0(rightExpr, varEntry, this.model.getFrame()));

		return node;
	}

	protected AbsReteNode _buildInheritNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		IRList parentTree = (IRList) reteTree.get(1);
		IRReteNode parentNode = _findReteNode(parentTree, tmpVarBuilder);

		int entryLength = reteTree.size() - 2;
		int[] inheritIndexs = new int[entryLength];
		IRObject[] inheritVarEntry = new IRObject[entryLength];

		int i = 0;
		int lastIndex = -1;

		IRIterator<? extends IRObject> it = reteTree.listIterator(2);
		while (it.hasNext()) {

			IRObject indexObj = it.next();
			if (indexObj.getType() != RType.INT) {
				throw new RException("invalid index : " + indexObj);
			}

			int index = RulpUtil.asInteger(indexObj).asInteger();

			if (index < 0 || index >= parentNode.getEntryLength()) {
				throw new RException(String.format("invalid index : %d", index));
			}

			if (index == lastIndex) {
				throw new RException(String.format("duplicated index : %d", index));
			}

			IRObject var = parentNode.getVarEntry()[index];
			if (var == null) {
				if (!RReteType.isRootType(parentNode.getReteType()) && parentNode.getReteType() != RReteType.EXPR1) {
					throw new RException(String.format("not var found at index : %d", index));
				}
			}

			inheritIndexs[i] = index;
			inheritVarEntry[i] = var;
			lastIndex = index;
			++i;
		}

		return RNodeFactory.createInheritNode(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), entryLength,
				parentNode, inheritVarEntry, inheritIndexs);
	}

	protected AbsReteNode _buildReteNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		RType treeType = reteTree.getType();
		int treeSize = reteTree.size();
		IRObject e0 = reteTree.get(0);

		RType e0Type = reteTree.get(0).getType();
		RType e1Type = treeSize >= 2 ? reteTree.get(1).getType() : null;

		// Build alpha node: '(a b c)
		if (treeType == RType.LIST && ReteUtil.isEntryValueType(e0Type)) {

			int varCount = ReteUtil.getStmtVarCount(reteTree);

			// Build const node
			if (varCount == 0) {
				return _buildConstNode(reteTree);
			}

			return _buildAlphaNode(reteTree, tmpVarBuilder);
		}

		// Beta0: '('(a b c) '(x y z))
		if (ReteUtil.isBetaTree(reteTree, treeSize)) {
			return _buildBetaNode(reteTree, tmpVarBuilder);
		}

		// Beta1 or Expr
		if (treeSize == 2 && treeType == RType.LIST && e0Type == RType.LIST && e1Type == RType.EXPR) {

			IRList l1 = (IRList) reteTree.get(1);

			// Build beta1 node:
			// - beta1 node: '('(a b ?v) (var-changed ?var v1 ?v)) // have join vars
			// - beta1 node: '('(a b c) (var-changed ?var v1 v2)) // no join vars
			if (ReteUtil.isVarChangeExpr(l1)) {
				return _buildBetaNode(reteTree, tmpVarBuilder);
			}

			// Build expr node: '('(a b c) (x y z))
			return _buildExprNode(reteTree, tmpVarBuilder);
		}

		// (var-changed)
		if (ReteUtil.isVarChangeExpr(reteTree)) {

			// (var-changed ?State Running Completed)
			if (e0Type == RType.ATOM) {
				return _buildVarNode(reteTree, tmpVarBuilder);
			}

			// ((var-changed ?s1 ?v1 v2) (not-equal ?v1 a))
			if (ReteUtil.isVarChangeExpr(e0) && treeSize == 2 && e1Type == RType.EXPR) {
				return _buildExprNode(reteTree, tmpVarBuilder);
			}
		}

		// (inherit '(?a ?b ?c) 0)
		if (ReteUtil.isInheritExpr(reteTree)) {
			return _buildInheritNode(reteTree, tmpVarBuilder);
		}

		// beta3: '(?a b c) '(?x y z) (not-equal ?a ?x)
		if (ReteUtil.isBeta3Tree(reteTree, treeSize)) {
			return _buildBetaNode(reteTree, tmpVarBuilder);
		}

		// beta:'((var-changed ?x ?xv) (var-changed ?y ?yv))
		if (treeSize == 2 && treeType == RType.LIST && ReteUtil.isVarChangeExpr(e0)
				&& ReteUtil.isVarChangeExpr(reteTree.get(1))) {
			return _buildBetaNode(reteTree, tmpVarBuilder);
		}

		int ModifierCount = ReteUtil.getReteTreeModifierCount(reteTree);
		if (ModifierCount > 0 && treeSize > ModifierCount) {

			ArrayList<IRObject> newReteTreeList = new ArrayList<>();
			int newSize = treeSize - ModifierCount;
			for (int i = 0; i < newSize; ++i) {
				newReteTreeList.add(reteTree.get(i));
			}

			AbsReteNode processNode = _buildReteNode(RulpFactory.createList(newReteTreeList), tmpVarBuilder);
			for (int i = newSize; i < treeSize; ++i) {
				processNode = _processNodeModifier(processNode, RulpUtil.asAtom(reteTree.get(i)).asString());
			}

			processNode.setUniqName(ReteUtil.uniqName(reteTree));

			return processNode;
		}

//		// beta:((var-changed ?x ?xv) url-entry:'(?url-name ?url))
//		if (treeSize == 2 && treeType == RType.EXPR && ReteUtility.isVarChangeExpr(e0) && e1Type == RType.LIST) {
//			return _buildBetaNode(reteTree, tmpVarBuilder);
//		}

		throw new RException("Invalid tree found: " + reteTree);
	}

	protected IRNodeSubGraph _buildSubGraphConstraintCheck(IRReteNode rootNode) throws RException {

		XRNodeSubGraph subGraph = new XRNodeSubGraph(this);

		Map<IRReteNode, List<IRReteNode>> affectNodeMap = _openAffectNodeMap(rootNode);

		LinkedList<IRReteNode> visitStack = new LinkedList<>();
		visitStack.add(rootNode);
		Set<IRReteNode> visitedNodes = new HashSet<>();

		while (!visitStack.isEmpty()) {

			IRReteNode node = visitStack.pop();
			// ignore visited node
			if (visitedNodes.contains(node)) {
				continue;
			}
			visitedNodes.add(node);

			List<IRReteNode> affectNodes = affectNodeMap.get(node);
			if (affectNodes != null) {
				subGraph.addNode(node);
				visitStack.addAll(affectNodes);
				continue;
			}

			if (node.getReteType() == RReteType.NAME0 && node.getConstraint1Count() > 0) {
				subGraph.addNode(node);
			}
		}

		for (IRReteNode node : new ArrayList<>(subGraph.getNodes())) {
			if (node.getReteType() == RReteType.RULE) {
				for (IRReteNode anode : ((IRRule) node).getAllNodes()) {
					subGraph.addNode(anode);
				}
			}
		}

		return subGraph;
	}

	protected IRNodeSubGraph _buildSubGraphRuleGroup(String ruleGroupName) throws RException {

		XRNodeSubGraph subGraph = new XRNodeSubGraph(this);

		IRList ruleList = RuleUtil.getRuleGroupRuleList(model, ruleGroupName);
		if (ruleList.size() == 0) {
			throw new RException("no rule found for group: " + ruleGroupName);
		}

		IRIterator<? extends IRObject> it = ruleList.iterator();
		while (it.hasNext()) {
			RuleUtil.travelReteParentNodeByPostorder(RuleUtil.asRule(it.next()), (node) -> {
				if (!subGraph.containNode(node) && node.getPriority() < RETE_PRIORITY_MAXIMUM
						&& node.getPriority() > RETE_PRIORITY_DISABLED) {
					subGraph.addNode(node);
				}
				return false;
			});
		}

		return subGraph;
	}

	protected IRNodeSubGraph _buildSubGroupSource(IRReteNode queryNode) throws RException {

		XRNodeSubGraph subGraph = new XRNodeSubGraph(this);

		boolean isRootMode = RReteType.isRootType(queryNode.getReteType());

		/******************************************************/
		// Build source graph
		/******************************************************/
		LinkedList<IRReteNode> visitStack = new LinkedList<>();
		visitStack.add(queryNode);
		Set<IRReteNode> visitedNodes = new HashSet<>();

		while (!visitStack.isEmpty()) {

			IRReteNode sourceNode = visitStack.pop();
			// ignore visited node
			if (visitedNodes.contains(sourceNode)) {
				continue;
			}
			visitedNodes.add(sourceNode);

			if (!isRootMode && RReteType.isRootType(sourceNode.getReteType())) {
				continue;
			}

			subGraph.addNode(sourceNode);

			if (sourceNode.getParentNodes() != null) {
				for (IRReteNode newSrcNode : sourceNode.getParentNodes()) {
					visitStack.add(newSrcNode);
				}
			}

			for (IRReteNode newSrcNode : listBindFromNodes(sourceNode)) {
				visitStack.add(newSrcNode);
			}

			for (SourceNode newSrcNode : RuleUtil.listSource(model, sourceNode)) {
				visitStack.add(newSrcNode.rule);
			}
		}

		RuleUtil.travelReteParentNodeByPostorder(queryNode, (node) -> {

			if (!subGraph.containNode(node)) {
				subGraph.addNode(node);
				visitStack.add(node);
				visitedNodes.add(node);
			}

			return false;
		});

		return subGraph;
	}

//	protected IRReteNode _buildVarChangeNode3(String varName, IRList reteTree, XTempVarBuilder tmpVarBuilder)
//			throws RException {
//
//		IRObject obj = reteTree.get(2);
//
//		/*****************************************************/
//		// (var-changed ?varName ?v2) -> (var-changed ?varName ?tmp ?v2)
//		/*****************************************************/
//		if (RulpUtil.isVarAtom(obj)) {
//
//			// (var-changed ?varName ?anyVar ?tmp)
//			List<IRObject> list = new ArrayList<>();
//			list.add(reteTree.get(0));
//			list.add(reteTree.get(1));
//			list.add(tmpVarBuilder.next());
//			list.add(reteTree.get(2));
//
////			InheritIndex[] inheritIndexs = new InheritIndex[2];
////			inheritIndexs[0] = new InheritIndex(0, 0);
////			inheritIndexs[1] = new InheritIndex(0, 2);
//
//			IRReteNode parentNode = _findReteNode(RulpFactory.createExpression(list), tmpVarBuilder);
//
//			AbsReteNode alph0Node = RNodeFactory.createAlpha1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
//					3, parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));
//
//			return alph0Node;
//		}
//
//		/*****************************************************/
//		// (var-changed ?varName new-value) -> (var-changed ?varName ?tmp new-value)
//		/*****************************************************/
//
//		// (var-changed ?varName ?tmp1 ?tmp2)
//		List<IRObject> list = new ArrayList<>();
//		list.add(reteTree.get(0));
//		list.add(reteTree.get(1));
//		list.add(tmpVarBuilder.next());
//		list.add(tmpVarBuilder.next());
//
//		IRReteNode parentNode = _findReteNode(RulpFactory.createExpression(list), tmpVarBuilder);
//
//		AbsReteNode alph0Node = RNodeFactory.createAlpha1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), 3,
//				parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));
//
//		addConstraint(alph0Node, ConstraintFactory.cmpEntryValue(RRelationalOperator.EQ, 2, obj));
//		return alph0Node;
//	}

	protected AbsReteNode _buildVarChangeNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		String varName = RulpUtil.asAtom(reteTree.get(1)).getName();

		// the e1 must be a var name format
		if (!RulpUtil.isVarName(varName)) {
			throw new RException("the 2nd element must be a var name format: " + varName);
		}

		switch (reteTree.size()) {
		case 4:
			/*****************************************************/
			// (var-changed ?varName old-value new-value)
			/*****************************************************/
			return _buildVarChangeNode4(varName, reteTree, tmpVarBuilder);

//		case 3:
//			/*****************************************************/
//			// (var-changed ?varName new-value)
//			/*****************************************************/
//			return _buildVarChangeNode3(varName, reteTree, tmpVarBuilder);

		default:
			throw new RException("invalid tree: " + reteTree);
		}

	}

	protected AbsReteNode _buildVarChangeNode4(String varName, IRList reteTree, XTempVarBuilder tmpVarBuilder)
			throws RException {

		/*****************************************************/
		// (var-changed ?varName same-value same-value) invalid
		/*****************************************************/
		if (RulpUtil.equal(reteTree.get(2), reteTree.get(3))) {
			throw new RException("invalid tree: " + reteTree);
		}

		int varCount = 0;
		int lastVarPos = -1;
		IRObject lastValue = null;

		// (var-changed ?varName a b)
		for (int i = reteTree.size() - 1; i >= 2; --i) {

			IRObject obj = reteTree.get(i);
			if (!ReteUtil.isEntryValueType(obj.getType())) {
				throw new RException("invalid element: " + obj);
			}

			// var
			if (RulpUtil.isVarAtom(obj)) {
				++varCount;
			}
			// value
			else if (lastValue == null) {
				lastValue = obj;
				lastVarPos = i;
			}
		}

		/*****************************************************/
		// (var-changed ?varName value1 value2)
		/*****************************************************/
		if (varCount == 0) {

			// (var-changed ?varName ?tmp1 ?tmp2)
			List<IRObject> list = RulpUtil.toArray(reteTree);
			list.set(2, tmpVarBuilder.next());
			list.set(3, tmpVarBuilder.next());

			IRReteNode parentNode = _findReteNode(RulpFactory.createExpression(list), tmpVarBuilder);

			AbsReteNode alph0Node = RNodeFactory.createAlpha1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
					3, parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));

			// (?varName a ?tmp)
			addConstraint(alph0Node, ConstraintFactory.cmpEntryValue(RRelationalOperator.EQ, 1, reteTree.get(2)));
			addConstraint(alph0Node, ConstraintFactory.cmpEntryValue(RRelationalOperator.EQ, 2, reteTree.get(3)));
			return alph0Node;
		}

		/*****************************************************/
		// (var-changed ?varName ?v1 value)
		// (var-changed ?varName value ?v2)
		/*****************************************************/
		if (lastValue != null) {

			// (var-changed ?varName a ?tmp)
			List<IRObject> list = RulpUtil.toArray(reteTree);
			list.set(lastVarPos, tmpVarBuilder.next());

			IRReteNode parentNode = _findReteNode(RulpFactory.createExpression(list), tmpVarBuilder);

			// (?varName ?v1 ?tmp)
			AbsReteNode alph0Node = RNodeFactory.createAlpha1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
					3, parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));

			// (= ?tmp value)
			addConstraint(alph0Node,
					ConstraintFactory.cmpEntryValue(RRelationalOperator.EQ, lastVarPos - 1, lastValue));

			return alph0Node;
		}

		/*****************************************************/
		// (var-changed ?varName ?v1 ?v2)
		/*****************************************************/
		IRVar var = model.getVar(varName);
		if (var == null) {
			throw new RException("var entry not found: " + varName);
		}

		AbsReteNode varNode = RNodeFactory.createVarNode(model, _getNextNodeId(), var, ReteUtil.uniqName(reteTree),
				entryTable);

		varNode.setVarEntry(ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));

		var.addVarListener((v, o, n) -> {

			IRObject elements[] = new IRObject[3];

			elements[0] = v;
			elements[1] = o;
			elements[2] = n;

			IRReteEntry reteEntry = entryTable.createEntry(null, elements, TEMP__, true);
			varNode.incEntryCreateCount();

			if (!varNode.addReteEntry(reteEntry)) {
				entryTable.deleteEntry(reteEntry);
				varNode.incEntryDeleteCount();
				return;
			}

			model.addUpdateNode(varNode);
		});

		varNodeMap.put(var.getName(), varNode);
		return varNode;
	}

	protected AbsReteNode _buildVarNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		IRObject e0 = reteTree.get(0);

		switch (RulpUtil.asAtom(e0).getName()) {

		// (var-changed ?varName old-value new-value)
		case F_VAR_CHANGED:
			return _buildVarChangeNode(reteTree, tmpVarBuilder);

		default:
			throw new RException("factor not support: " + e0);
		}
	}

	protected IRReteNode _findReteNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		String uniqName = ReteUtil.uniqName(reteTree);
		AbsReteNode node = nodeUniqNameMap.get(uniqName);

		if (node == null) {
			node = _buildReteNode(reteTree, tmpVarBuilder);
			node.setReteTree(reteTree);
			_addNode(node);
		}

		((XRGraphInfo) node.getGraphInfo()).incUseCount();

		return node;
	}

	protected IRReteNode _findReteNode(List<IRList> matchStmtList) throws RException {
		return _findReteNode(MatchTree.build(matchStmtList, this.model.getInterpreter(), this.model.getFrame()),
				new XTempVarBuilder("rete"));
	}

	protected int _getNextNodeId() {
		return ++maxNodeIndex;
	}

	protected IRReteNode _getNodeInfo(int nodeId) {

		if (nodeId <= 0 || nodeInfoArray.size() <= nodeId) {
			return null;
		}

		return nodeInfoArray.get(nodeId);
	}

	protected void _graphChanged() {
		_sourceSubGraphMap = null;
		_ruleGroupSubGraphMap = null;
		_affectNodeMap = null;
		_constraintCheckSubGraphMap = null;
	}

	protected boolean _isUnusedNode(IRReteNode node) {

		if (!node.getChildNodes().isEmpty()) {
			return false;
		}

		if (node.getPriority() > RETE_PRIORITY_INACTIVE) {
			return false;
		}

		XRGraphInfo info = ((XRGraphInfo) node.getGraphInfo());
		if (info.hasBindNode()) {
			return false;
		}

		return true;
	}

	protected Map<IRReteNode, List<IRReteNode>> _openAffectNodeMap(IRReteNode rootNode) throws RException {

		if (_affectNodeMap == null) {
			_affectNodeMap = new HashMap<>();

			for (IRReteNode node : listNodes(RReteType.NAME0)) {

				if (node.getConstraint1Count() == 0) {
					continue;
				}

				// Build named -> rule map
				List<IRRule> relatedRules = ((XRGraphInfo) node.getGraphInfo()).relatedRules;
				if (relatedRules != null) {

					List<IRReteNode> affectList = _affectNodeMap.get(node);
					if (affectList == null) {
						affectList = new LinkedList<>();
						_affectNodeMap.put(node, affectList);
					}

					for (IRRule rn : relatedRules) {
						if (!affectList.contains(rn)) {
							affectList.add(rn);
						}
					}
				}

				// Build rule -> named map
				for (SourceNode sourceNode : RuleUtil.listSource(model, node)) {

					if (sourceNode.rule.getReteType() == RReteType.RULE) {

						List<IRReteNode> affectList = _affectNodeMap.get(sourceNode.rule);
						if (affectList == null) {
							affectList = new LinkedList<>();
							_affectNodeMap.put(sourceNode.rule, affectList);
						}

						if (!affectList.contains(node)) {
							affectList.add(node);
						}
					}
				}
			}
		}

		return _affectNodeMap;
	}

	protected AbsReteNode _processNodeModifier(AbsReteNode node, String modifier) throws RException {

		switch (modifier) {

		// beta: '(?a) '(?b) entry-order
		case A_ENTRY_ORDER:

			// This constraint will be only added to "symmetric" betaNode
			if (isSymmetricBetaNode(node)) {
				RuleUtil.asBetaNode(node).addConstraint2(ConstraintFactory.entryOrder());
			}

			break;

		default:
			throw new RException("invalid modifier: " + modifier);
		}

		return node;
	}

	protected void _removeNode(AbsReteNode node) throws RException {

		// update gc cache count before removed
		{
			counter.gcRemoveNodeCount++;

			gcRemoveNodeCountArray[DefinedCount.getIndex()] += node.getEntryQueue().getEntryCounter()
					.getEntryCount(DEFINE);
			gcRemoveNodeCountArray[FixedCount.getIndex()] += node.getEntryQueue().getEntryCounter()
					.getEntryCount(FIXED_);
			gcRemoveNodeCountArray[AssumeCount.getIndex()] += node.getEntryQueue().getEntryCounter()
					.getEntryCount(ASSUME);
			gcRemoveNodeCountArray[ReasonCount.getIndex()] += node.getEntryQueue().getEntryCounter()
					.getEntryCount(REASON);
			gcRemoveNodeCountArray[DropCount.getIndex()] += node.getEntryQueue().getEntryCounter().getEntryCount(null);
			gcRemoveNodeCountArray[RemoveCount.getIndex()] += node.getEntryQueue().getEntryCounter()
					.getEntryCount(REMOVE);
			gcRemoveNodeCountArray[TempCount.getIndex()] += node.getEntryQueue().getEntryCounter()
					.getEntryCount(TEMP__);
			gcRemoveNodeCountArray[NullCount.getIndex()] += node.getEntryQueue().getEntryCounter().getEntryNullCount();
			gcRemoveNodeCountArray[RedundantCount.getIndex()] += node.getEntryQueue().getRedundantCount();
			gcRemoveNodeCountArray[BindFromCount.getIndex()] += model.getNodeGraph().listBindFromNodes(node).size();
			gcRemoveNodeCountArray[BindToCount.getIndex()] += model.getNodeGraph().listBindToNodes(node).size();
			gcRemoveNodeCountArray[NodeCount.getIndex()] += 1;
			gcRemoveNodeCountArray[SourceCount.getIndex()] += RuleUtil.listSource(model, node).size();
			gcRemoveNodeCountArray[MatchCount.getIndex()] += node.getNodeMatchCount();
			gcRemoveNodeCountArray[ExecCount.getIndex()] += node.getNodeExecCount();
			gcRemoveNodeCountArray[IdleCount.getIndex()] += node.getNodeIdleCount();
			gcRemoveNodeCountArray[UpdateCount.getIndex()] += node.getEntryQueue().getUpdateCount();
			gcRemoveNodeCountArray[FailedCount.getIndex()] += node.getNodeFailedCount();
			gcRemoveNodeCountArray[QueryFetch.getIndex()] += node.getEntryQueue().getQueryFetchCount();
			gcRemoveNodeCountArray[QueryMatch.getIndex()] += node.getQueryMatchCount();
			gcRemoveNodeCountArray[QueryMatch.getIndex()] = Math.min(gcRemoveNodeCountArray[QueryMatch.getIndex()],
					node.getReteLevel());
			gcRemoveNodeCountArray[MinPriority.getIndex()] = Math.min(gcRemoveNodeCountArray[MinPriority.getIndex()],
					node.getPriority());
			gcRemoveNodeCountArray[MaxLevel.getIndex()] = Math.min(gcRemoveNodeCountArray[MaxLevel.getIndex()],
					node.getReteLevel());
			gcRemoveNodeCountArray[MaxPriority.getIndex()] = Math.min(gcRemoveNodeCountArray[MaxPriority.getIndex()],
					node.getPriority());
			gcRemoveNodeCountArray[CreateEntry.getIndex()] += node.getEntryCreateCount();
			gcRemoveNodeCountArray[DeleteEntry.getIndex()] += node.getEntryDeleteCount();
		}

		RReteType reteType = node.getReteType();

		switch (reteType) {

		case RULE:
			this.ruleNodeMap.remove(((XRNodeRule0) node).getRuleName());
			break;

		default:
			break;
		}

		this.nodeListArray[reteType.getIndex()].nodes.remove(node);
		this.nodeUniqNameMap.remove(node.getUniqName());
		this.nodeInfoArray.set(node.getNodeId(), null);

		_graphChanged();

	}

	protected void _setNodePriority(IRReteNode node, int priority) throws RException {

		if (node.getPriority() == priority) {
			return;
		}

		node.setPriority(priority);
		_graphChanged();
	}

	protected boolean _supportInheritOpt(IRReteNode parentNode, IRObject[] ruleVarEntry, List<IRExpr> indexExprList,
			List<IRExpr> actionStmtList) throws RException {

		/******************************************************/
		// Does not use inherit node if there is var-change expr
		/******************************************************/
		switch (parentNode.getReteType()) {
		case ALPH1:
		case BETA1:
			return false;

		default:
			break;
		}

		if (!indexExprList.isEmpty()) {
			return false;
		}

		/******************************************************/
		// Does not use inherit node if all actions are simple statements
		/******************************************************/
		if (ActionUtil.isSimpleAddStmtAction(actionStmtList, ruleVarEntry)) {
			return false;
		}

		return true;
	}

	protected IRList _toList(String list) throws RException {

		List<IRObject> objs = model.getInterpreter().getParser().parse(list);
		if (objs.size() != 1) {
			throw new RException("invalid list: " + list);
		}

		return RulpUtil.asList(objs.get(0));
	}

	@Override
	public boolean addConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addConstraint: " + node + ", " + constraint);
		}

		counter.addConstraint++;

		constraint = ConstraintFactory.rebuildConstraint(node, constraint);
		constraint.setNode(node);

		if (!node.addConstraint1(constraint)) {
			return false;
		}

		switch (constraint.getConstraintName()) {

		// If a sub-list is uniq, then the whole list is also uniq. Change the Queue
		// from "UNIQ" to "MULTI"
		case A_Uniq:
		case A_Order_by:
			ReteUtil.tryChangeNodeQueue(node, REntryQueueType.UNIQ, REntryQueueType.MULTI);

			break;
		}

		_graphChanged();
		return true;
	}

	@Override
	public void bindNode(IRReteNode fromNode, IRReteNode toNode) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> bindNode: " + fromNode + ", " + toNode);
		}

		counter.bindNode++;

		XRGraphInfo fromNodeInfo = (XRGraphInfo) fromNode.getGraphInfo();
		XRGraphInfo toNodeInfo = (XRGraphInfo) toNode.getGraphInfo();
		fromNodeInfo.bind(toNodeInfo);

		_graphChanged();
	}

	@Override
	public IRReteNode createNodeByTree(IRList tree) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> createNodeByTree: " + tree);
		}

		counter.createNodeByTree++;

		if (ReteUtil.isReteStmt(tree)) {
			List<IRList> matchStmtList = new LinkedList<>();
			matchStmtList.add(tree);
			return _findReteNode(matchStmtList);
		}

		if (ReteUtil.isCondList(tree)) {
			return _findReteNode(ReteUtil.toCondList(tree, this, uniqBuilder));
		}

		throw new RException("Can't find node by stmts: " + tree);
	}

	@Override
	public IRReteNode createNodeIndex(IRReteNode node, List<OrderEntry> orderList) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> createNodeIndex: " + node);
		}

		counter.createNodeIndex++;

		switch (node.getReteType()) {
		case ROOT0:
		case NAME0:
		case ALPH0:
			break;

		default:
			throw new RException(String.format("Can't add index to node: %s", node.getUniqName()));
		}

		String uniqName = ReteUtil.getIndexNodeUniqName(node.getUniqName(), orderList);
		IRReteNode oldIndexNode = ReteUtil.matchChildNode(node, RReteType.INDEX, uniqName);
		if (oldIndexNode != null) {
			return oldIndexNode;
		}

		AbsReteNode indexNode = RNodeFactory.createIndexNode(model, _getNextNodeId(), uniqName, node.getEntryLength(),
				node, node.getVarEntry(), orderList);

		_addNode(indexNode);

		return indexNode;
	}

	@Override
	public AbsReteNode createNodeRoot(String name, int stmtLen) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> createNodeRoot: " + name + ", " + stmtLen);
		}

		counter.createNodeRoot++;

		if (name == null) {

			AbsReteNode rootNode = rootNodeArray[stmtLen];
			if (rootNode == null) {

				rootNode = RNodeFactory.createRoot0Node(model, _getNextNodeId(), stmtLen);
				rootNodeArray[stmtLen] = rootNode;
				if (stmtLen > maxRootStmtLen) {
					maxRootStmtLen = stmtLen;
				}

				_addNode(rootNode);
				rootNode.setReteTree(_toList(ReteUtil.getRootUniqName(null, stmtLen)));
			}

			return rootNode;
		}

		if (stmtLen == -1) {
			throw new RException(String.format("invalid namedNode<%s:%d>", name, stmtLen));
		}

		AbsReteNode namedNode = namedNodeMap.get(name);
		if (namedNode == null) {
			namedNode = RNodeFactory.createName0Node(model, _getNextNodeId(), name, stmtLen);
			namedNodeMap.put(name, namedNode);
			_addNode(namedNode);
			namedNode.setReteTree(_toList(ReteUtil.getRootUniqName(name, stmtLen)));
		}

		if (stmtLen != -1 && namedNode.getEntryLength() != stmtLen) {
			throw new RException(String.format("EntryLength not match: expect=%d, actual=%s, node=%s", stmtLen,
					namedNode.getEntryLength(), namedNode.getNodeName()));
		}

		return namedNode;
	}

	@Override
	public IRRule createNodeRule(String ruleName, IRList condList, IRList actionList, int priority) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out
					.println("==> createNodeRule: " + ruleName + ", " + condList + ", " + actionList + ", " + priority);
		}

		counter.createNodeRule++;

		if (ruleName == null) {
			ruleName = String.format("RU%03d", anonymousRuleIndex++);

		} else if (findRule(ruleName) != null) {
			throw new RException("Duplicate rule name: " + ruleName);
		}

		List<IRList> matchStmtList = ReteUtil.toCondList(condList, this, uniqBuilder);
		List<IRExpr> actionStmtList = new LinkedList<>();

		IRIterator<? extends IRObject> actionIter = actionList.iterator();
		while (actionIter.hasNext()) {
			IRObject action = actionIter.next();
			actionStmtList.add(RulpUtil.asExpression(action));
		}

		if (matchStmtList.isEmpty() || actionStmtList.isEmpty()) {
			throw new RException(String.format("not support stmt: cond=%s, action=%s", condList, actionList));
		}

		/******************************************************/
		// Check index expression: (!= ?0 ?1)
		/******************************************************/
		List<IRList> actualMatchStmtList = new ArrayList<>();
		List<IRExpr> indexExprList = new ArrayList<>();

		for (IRList stmt : matchStmtList) {

			// ?1 ?2 ?3
			if (ReteUtil.isIndexVarStmt(stmt)) {

				if (stmt.getType() != RType.EXPR) {
					throw new RException("Invalid index stmt: " + stmt);
				}

				indexExprList.add((IRExpr) stmt);

			} else {

				if (!indexExprList.isEmpty()) {
					throw new RException("The index expression should be at the tail of LHS: " + matchStmtList);
				}

				actualMatchStmtList.add(stmt);
			}
		}

		IRList matchTree = MatchTree.build(actualMatchStmtList, this.model.getInterpreter(), this.model.getFrame());
		XTempVarBuilder varBuilder = new XTempVarBuilder("rule");

		IRReteNode parentNode = _findReteNode(matchTree, varBuilder);
		IRObject[] ruleVarEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(matchTree));

		/******************************************************/
		// Create inherit node if possible
		/******************************************************/
		if (_supportInheritOpt(parentNode, ruleVarEntry, indexExprList, actionStmtList)) {

			int entryLen = ruleVarEntry.length;
			int matchVarCount = 0;
			for (int i = 0; i < entryLen; ++i) {
				if (ruleVarEntry[i] != null) {
					matchVarCount++;
				}
			}

			ArrayList<Integer> foundIndexList = new ArrayList<>();
			HashSet<IRObject> actionVars = new HashSet<>(ReteUtil.buildVarList(actionStmtList));

			for (int i = 0; i < entryLen; ++i) {
				IRObject matchVar = ruleVarEntry[i];
				if (matchVar == null) {
					continue;
				}

				boolean found = false;
				for (IRObject actionVar : actionVars) {
					if (RulpUtil.equal(actionVar, matchVar)) {
						found = true;
						break;
					}
				}

				if (found) {
					foundIndexList.add(i);
				}
			}

			/******************************************************/
			// '(?a ?b ?c) (inherit 0 1) ==> '(?a ?b)
			/******************************************************/
			if (!foundIndexList.isEmpty() && foundIndexList.size() < matchVarCount) {

				IRObject[] newRuleVarEntry = new IRObject[foundIndexList.size()];

				ArrayList<IRObject> inheritExprObjs = new ArrayList<>();
				inheritExprObjs.add(RulpFactory.createAtom(A_Inherit));
				inheritExprObjs.add(matchTree);

				int i = 0;
				for (int index : foundIndexList) {
					newRuleVarEntry[i] = ruleVarEntry[index];
					inheritExprObjs.add(RulpFactory.createInteger(index));
					i++;
				}

				parentNode = _findReteNode(RulpFactory.createExpression(inheritExprObjs), varBuilder);
				ruleVarEntry = newRuleVarEntry;
			}

		}

		XRNodeRule0 ruleNode = RNodeFactory.createRuleNode(model, _getNextNodeId(), ruleName,
				parentNode.getEntryLength(), parentNode, ruleVarEntry, actionStmtList);

		ruleNode.setMatchStmtList(actualMatchStmtList);
		_setNodePriority(ruleNode, priority);

		for (IRExpr expr : indexExprList) {
			addConstraint(ruleNode, ConstraintFactory.expr4(expr, actualMatchStmtList));
		}

		/******************************************************/
		// Update node rule & priority
		/******************************************************/
		RuleUtil.travelReteParentNodeByPostorder(parentNode, (node) -> {

			if (node.getReteType() != RReteType.ROOT0) {

				ruleNode.addNode(node);
				if (node != ruleNode) {
					((XRGraphInfo) node.getGraphInfo()).addRule(ruleNode);
				}

				if (node.getPriority() < priority) {
					_setNodePriority(node, priority);
				}
			}
			return false;
		});

		ruleNode.addNode(ruleNode);
		_addNode(ruleNode);

		if (parentNode.getEntryQueue().size() > 0) {
			model.addUpdateNode(ruleNode);
		}

		if (ruleNode.getNodeId() > maxRuleId) {
			maxRuleId = ruleNode.getNodeId();
		}

		ruleUpdateId++;

		return ruleNode;
	}

	@Override
	public IRNodeSubGraph createSubGraphForConstraintCheck(IRReteNode rootNode) throws RException {

		counter.createSubGraphForConstraintCheck++;

		if (_constraintCheckSubGraphMap == null) {
			_constraintCheckSubGraphMap = new HashMap<>();
		}

		IRNodeSubGraph subGraph = _constraintCheckSubGraphMap.get(rootNode);
		if (subGraph == null) {
			subGraph = _buildSubGraphConstraintCheck(rootNode);
			_constraintCheckSubGraphMap.put(rootNode, subGraph);
		}

		return subGraph;
	}

	@Override
	public IRNodeSubGraph createSubGraphForQueryNode(IRReteNode queryNode) throws RException {

		counter.createSubGraphForQueryNode++;

		if (_sourceSubGraphMap == null) {
			_sourceSubGraphMap = new HashMap<>();
		}

		IRNodeSubGraph subGraph = _sourceSubGraphMap.get(queryNode);
		if (subGraph == null) {
			subGraph = _buildSubGroupSource(queryNode);
			_sourceSubGraphMap.put(queryNode, subGraph);
		}

		return subGraph;
	}

	@Override
	public IRNodeSubGraph createSubGraphForRuleGroup(String ruleGroupName) throws RException {

		counter.createSubGraphForRuleGroup++;

		if (_ruleGroupSubGraphMap == null) {
			_ruleGroupSubGraphMap = new HashMap<>();
		}

		IRNodeSubGraph subGraph = _ruleGroupSubGraphMap.get(ruleGroupName);
		if (subGraph == null) {
			subGraph = _buildSubGraphRuleGroup(ruleGroupName);
			_ruleGroupSubGraphMap.put(ruleGroupName, subGraph);
		}

		return subGraph;
	}

	@Override
	public IRReteNode createWorkNode(String name, IRWorker worker) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addWorker: " + name);
		}

		counter.createWorkNode++;

		if (name == null) {
			name = String.format("W%03d", anonymousWorkIndex++);
		}

		if (nodeUniqNameMap.containsKey(name)) {
			throw new RException("Duplicate worker name: " + name);
		}

		AbsReteNode workNode = RNodeFactory.createWorkerNode(model, _getNextNodeId(), name, worker);
		_addNode(workNode);

		return workNode;
	}

	@Override
	public void doGc() throws RException {

		counter.doGc++;

		/**************************************************/
		// Clean unused nodes
		/**************************************************/
		if (gcMaxInactiveLeafCount > -1) {

			HeapStack<AbsReteNode> nodeHeap = new HeapStack<>(new Comparator<AbsReteNode>() {
				@Override
				public int compare(AbsReteNode n1, AbsReteNode n2) {
					return ((XRGraphInfo) n2.getGraphInfo()).getUseCount()
							- ((XRGraphInfo) n1.getGraphInfo()).getUseCount();
				}
			});

			for (AbsReteNode node : listNodes(RReteType.ALPH0)) {
				if (_isUnusedNode(node)) {
					nodeHeap.push(node);
				}
			}

			if (nodeHeap.size() > gcMaxInactiveLeafCount) {
				counter.gcInactiveLeafCount++;
				while (nodeHeap.size() > gcMaxInactiveLeafCount) {
					AbsReteNode node = nodeHeap.pop();
					node.setReteStage(RReteStage.InActive);
					removeNode(node);
				}
			}

		}

		/**************************************************/
		// Clean unused cache node
		/**************************************************/
		if (gcMaxCacheNodeCount > 0) {

			counter.gcCacheCount++;

			HeapStack<AbsReteNode> nodeHeap = new HeapStack<>(new Comparator<AbsReteNode>() {

				@Override
				public int compare(AbsReteNode n1, AbsReteNode n2) {
					return ((XRGraphInfo) n2.getGraphInfo()).getUseCount()
							- ((XRGraphInfo) n1.getGraphInfo()).getUseCount();
				}
			});

			for (AbsReteNode node : listNodes(RReteType.NAME0)) {

				if (!node.getChildNodes().isEmpty()) {
					continue;
				}

				IRCacheWorker cache = node.getCacheWorker();
				if (cache == null) {
					continue;
				}

				if (cache.getStatus() != CacheStatus.LOADED) {
					continue;
				}

				if (cache.isDirty()) {
					continue;
				}

				nodeHeap.push(node);
			}

			if (nodeHeap.size() > gcMaxCacheNodeCount) {

				counter.gcInactiveLeafCount++;

				while (nodeHeap.size() > gcMaxCacheNodeCount) {
					IRNamedNode node = (IRNamedNode) nodeHeap.pop();
					node.cleanCache();
					counter.gcCleanNodeCount++;
				}
			}
		}

	}

	@Override
	public int doOptimize() throws RException {

		counter.doOptimize++;

		int optCount = 0;

		/***********************************************************/
		// Link node and its single child node
		/***********************************************************/
		for (IRReteNode node : nodeUniqNameMap.values()) {

			// ignore node which is not active
			if (node.getPriority() == 0) {
				continue;
			}

			// ignore node which is not fresh
			if (!node.isNodeFresh()) {
				continue;
			}

			// ignore node which has multi child
			List<IRReteNode> allChild = node.getChildNodes();
			if (allChild.size() != 1) {
				continue;
			}

			IRReteNode child = allChild.get(0);

			// ignore node whose child is not active
			if (child.getPriority() == 0) {
				continue;
			}

			// ignore node whose child is not same type
			if (child.getReteType() != node.getReteType()) {
				continue;
			}

			// ignore node whose child has multi parent
			if (child.getParentCount() != 1) {
				continue;
			}

			switch (node.getReteType()) {
			case EXPR1:

				XRNodeRete1 exprParent1 = (XRNodeRete1) node;
				XRNodeRete1 exprChild1 = (XRNodeRete1) child;
				IRReteNode parentNode = exprParent1.getParentNodes()[0];

				// Remove link
				exprParent1.removeChildNode(exprChild1);

				// Add new link
				parentNode.addChildNode(exprChild1);
				exprChild1.setParentNodes(ReteUtil.toNodesArray(parentNode));

				// Copy match node from parent to child
				int consSize = exprParent1.getConstraint1Count();
				for (int i = 0; i < consSize; ++i) {
					addConstraint(exprChild1, exprParent1.getConstraint1(i));
				}

				// Update priority
				if (exprParent1.getPriority() > exprChild1.getPriority()) {
					_setNodePriority(exprChild1, exprParent1.getPriority());
				}

				// disable parent node
				_setNodePriority(exprParent1, 0);

				++optCount;

				break;

			default:
			}
		}

		return optCount;
	}

	@Override
	public IRReteNode findNodeByUniqName(String uniqName) throws RException {
		return nodeUniqNameMap.get(uniqName);
	}

	@Override
	public IRReteNode findRootNode(String name, int stmtLen) throws RException {

		if (name == null) {
			return rootNodeArray[stmtLen];
		}

		IRReteNode node = namedNodeMap.get(name);
		if (node != null && stmtLen > 0 && node.getEntryLength() != stmtLen) {
			throw new RException(String.format("EntryLength not match: expect=%d, actual=%s, node=%s", stmtLen,
					node.getEntryLength(), node.getNodeName()));
		}

		return node;
	}

	@Override
	public IRRule findRule(String ruleName) {
		return ruleNodeMap.get(ruleName);
	}

	@Override
	public List<String> getCounterKeyList() {
		return XRGraphCount.getCounterKeyList();
	}

	@Override
	public long getCounterValue(String countkey) {
		return counter.getCounterValue(countkey);
	}

	@Override
	public long getGcNodeRemoveCount(RCountType countType) throws RException {
		return gcRemoveNodeCountArray[countType.getIndex()];
	}

	@Override
	public int getGcRemoveNodeCount() {
		return counter.gcRemoveNodeCount;
	}

	@Override
	public int getMaxRootStmtLen() {
		return maxRootStmtLen;
	}

	@Override
	public IReteNodeMatrix getNodeMatrix() {

		return new IReteNodeMatrix() {

			protected List<IRReteNode> allNodeList = null;

			@Override
			public List<? extends IRReteNode> getAllNodes() {

				if (allNodeList == null) {
					allNodeList = ReteUtil.getAllNodes(XRNodeGraph.this);
				}

				return allNodeList;
			}

			@Override
			public IRModel getModel() {
				return model;
			}

			@Override
			public List<? extends IRReteNode> getNodeList(RReteType reteType) {
				return nodeListArray[reteType.getIndex()].nodes;
			}
		};
	}

	@Override
	public int getUniqueObjectCount() {
		return uniqBuilder.size();
	}

	@Override
	public int getUseCount(IRReteNode node) {
		return ((XRGraphInfo) node.getGraphInfo()).getUseCount();

	}

	@Override
	public List<IRReteNode> listBindFromNodes(IRReteNode node) throws RException {
		XRGraphInfo nodeInfo = (XRGraphInfo) node.getGraphInfo();
		return nodeInfo.bindFromNodeList == null ? Collections.emptyList() : nodeInfo.bindFromNodeList;
	}

	@Override
	public List<IRReteNode> listBindToNodes(IRReteNode node) throws RException {
		XRGraphInfo nodeInfo = (XRGraphInfo) node.getGraphInfo();
		return nodeInfo.bindToNodeList == null ? Collections.emptyList() : nodeInfo.bindToNodeList;
	}

	@Override
	public List<AbsReteNode> listNodes(RReteType reteType) {
		return this.nodeListArray[reteType.getIndex()].nodes;
	}

	@Override
	public List<IRRule> listRelatedRules(IRReteNode node) throws RException {

		XRGraphInfo nodeInfo = (XRGraphInfo) node.getGraphInfo();
		return nodeInfo.relatedRules == null ? Collections.emptyList() : nodeInfo.relatedRules;
	}

	@Override
	public Collection<SourceNode> listSourceNodes(IRList cond) throws RException {

		counter.listSourceNodes++;

		if (!ReteUtil.isCond(cond)) {
			return Collections.emptyList();
		}

		String uniqName = ReteUtil.uniqName(cond);

		SourceInfo info = sourceInfoMap.get(uniqName);
		if (info == null) {
			info = new SourceInfo();
			info.uniqName = uniqName;
			info.uniqStmt = _toList(uniqName);
			info.stmtSize = cond.size();
			info.stmtName = cond.getNamedName();
			info.varCount = ReteUtil.varList(cond).size();

			if (info.varCount == info.stmtSize) {
				info.sourceType = info.stmtName == null ? RReteType.ROOT0 : RReteType.NAME0;

			} else if (info.varCount > 0) {
				info.sourceType = RReteType.ALPH0;

			} else {
				info.sourceType = RReteType.CONST;
			}

		}

		if (info.lastRuleUpdateId != this.ruleUpdateId) {

			/*****************************************/
			// Check removed node
			/*****************************************/
			if (info.sourceNodes != null) {

				Iterator<SourceNode> it = info.sourceNodes.iterator();
				while (it.hasNext()) {
					SourceNode sn = it.next();
					if (sn.rule.getReteStage() == RReteStage.Removed) {
						it.remove();
					}
				}
			} else {
				info.sourceNodes = new HashSet<>();
			}

			for (IRReteNode ruleNode : listNodes(RReteType.RULE)) {

				// the rule has been processed
				if (ruleNode.getNodeId() <= info.lastRuleIndex) {
					continue;
				}

				info.update(this, (IRRule) ruleNode);
				info.lastRuleIndex = ruleNode.getNodeId();
			}

			info.lastRuleUpdateId = this.ruleUpdateId;
		}

		return info.sourceNodes;
	}

	@Override
	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> removeConstraint: " + node + ", " + constraint);
		}

		counter.removeConstraint++;

		IRConstraint1 removedConstraint = node.removeConstraint(constraint.getConstraintExpression());
		if (removedConstraint == null) {
			return null;
		}

		switch (constraint.getConstraintName()) {

		// If there is other constraint, change the queue back to uniq
		case A_Uniq:
		case A_Order_by:
			ReteUtil.tryChangeNodeQueue(node, REntryQueueType.MULTI, REntryQueueType.UNIQ);

			break;
		}

		_graphChanged();

		return removedConstraint;
	}

	@Override
	public boolean removeNode(IRReteNode node) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> removeNode: " + node);
		}

		counter.removeNode++;

		/********************************************/
		// Check node type
		/********************************************/
		switch (node.getReteType()) {
		case ROOT0:
			return false;
		default:
			break;
		}

		/********************************************/
		// Check node children
		/********************************************/
		if (!node.getChildNodes().isEmpty()) {
			return false;
		}

		/********************************************/
		// Check node stage
		/********************************************/
		switch (node.getReteStage()) {
		case Active:
		case InQueue:
			return false;

		case Removed:
			return true;

		case InActive:
		case OutQueue:
		default:
			break;
		}

		/********************************************/
		// remove constraint for named node
		/********************************************/

		/********************************************/
		// remove node from graph
		/********************************************/
		_removeNode((AbsReteNode) node);

		/********************************************/
		// remove node from its parent
		/********************************************/
		if (node.getParentCount() > 0) {
			for (IRReteNode parent : node.getParentNodes()) {
				parent.removeChildNode(node);
			}
		}

		/********************************************/
		// remove node from related node
		/********************************************/

		return false;
	}

	public void setGcMaxCacheNodeCount(int gcMaxCacheNodeCount) {
		this.gcMaxCacheNodeCount = gcMaxCacheNodeCount;
	}

	public void setGcMaxInactiveLeafCount(int gcMaxInactiveLeafCount) {
		this.gcMaxInactiveLeafCount = gcMaxInactiveLeafCount;
	}

	@Override
	public void setRulePriority(IRRule rule, int priority) throws RException {

		counter.setRulePriority++;

		if (priority < 0 || priority > RETE_PRIORITY_MAXIMUM) {
			throw new RException("Invalid priority: " + priority);
		}

		if (rule.getPriority() == priority) {
			return;
		}

		_setNodePriority(rule, priority);

		/******************************************************/
		// Update all rule node priority
		/******************************************************/
		if (rule.getReteType() == RReteType.RULE) {

			RuleUtil.travelReteParentNodeByPostorder(rule, (_node) -> {
				if (_node.getReteType() != RReteType.ROOT0 && _node != rule) {
					int newPriority = RuleUtil.recalcuatePriority(model, _node);
					_setNodePriority(_node, newPriority);
				}
				return false;
			});
		}
	}

}
