package alpha.rulp.ximpl.node;

import static alpha.rulp.lang.Constant.F_EQUAL;
import static alpha.rulp.rule.Constant.A_ENTRY_ORDER;
import static alpha.rulp.rule.Constant.F_VAR_CHANGED;
import static alpha.rulp.rule.Constant.STMT_MAX_LEN;
import static alpha.rulp.rule.RReteStatus.TEMP__;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRReteNode.InheritIndex;
import alpha.rulp.rule.IRReteNode.JoinIndex;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.MatchTree;
import alpha.rulp.utils.ModelUtil;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.action.ActionUtil;
import alpha.rulp.ximpl.constraint.ConstraintFactory;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IGraphInfo;
import alpha.rulp.ximpl.model.IReteNodeMatrix;
import alpha.rulp.ximpl.model.XRUniqObjBuilder;

public class XRNodeGraph implements IRNodeGraph {

	static class ReteNodeList {
		public ArrayList<IRReteNode> nodes = new ArrayList<>();
	}

	static class XGraphInfo implements IGraphInfo {

		public IRList alphaUniqStmt = null;

		public List<IRReteNode> bindFromNodeList = null;

		public List<IRReteNode> bindToNodeList = null;

		public int lastRuleIndex = -1;

		public IRReteNode node;

		public int partialRecoveryPriority = -1;

		public List<IRRule> relatedRules = null;

		public List<IRList> ruleActionUniqStmtList = null;

		public Set<IRReteNode> sourceNodes = null;

		public XGraphInfo(IRReteNode node) {
			this.node = node;
		}

		public void addRule(IRRule rule) {

			if (relatedRules == null) {
				relatedRules = new LinkedList<>();
			}

			relatedRules.add(rule);
		}

		public void bind(XGraphInfo toNode) {

			if (this.bindToNodeList == null) {
				this.bindToNodeList = new LinkedList<>();
			}

			if (toNode.bindFromNodeList == null) {
				toNode.bindFromNodeList = new LinkedList<>();
			}

			/*************************************/
			// Bind toNode <--- fromNode
			/*************************************/
			if (!this.bindToNodeList.contains(toNode.node)) {
				this.bindToNodeList.add(toNode.node);
			}

			/*************************************/
			// Bind fromNode ---> toNode
			/*************************************/
			if (!toNode.bindFromNodeList.contains(this.node)) {
				toNode.bindFromNodeList.add(this.node);
			}
		}

		public List<IRList> getRuleActionUniqStmtList(XRNodeGraph graph) throws RException {

			if (ruleActionUniqStmtList == null) {

				Set<String> actionUniqStmtNames = new HashSet<>();

				for (IRExpr expr : ((IRRule) node).getActionStmtList()) {
					actionUniqStmtNames.addAll(ActionUtil.buildRelatedStmtUniqNames(expr));
				}

				ruleActionUniqStmtList = new ArrayList<>();
				for (String uniqName : actionUniqStmtNames) {
					ruleActionUniqStmtList.add(graph._getUniqStmt(uniqName));
				}
			}

			return ruleActionUniqStmtList;
		}
	}

	protected int anonymousRuleIndex = 0;

	protected int anonymousWorkIndex = 0;

	protected IREntryTable entryTable;

	protected int maxNodeIndex = 0;

	protected int maxRootStmtLen = 0;

	protected IRModel model;

	protected final Map<String, IRNamedNode> namedNodeMap = new HashMap<>();

	protected ArrayList<IRReteNode> nodeInfoArray = new ArrayList<>();

	protected final ReteNodeList[] nodeListArray = new ReteNodeList[RReteType.RETE_TYPE_NUM];

	protected final Map<String, IRReteNode> nodeUniqNameMap = new HashMap<>();

	protected IRRootNode[] rootNodeArray = new IRRootNode[STMT_MAX_LEN + 1];

	protected final Map<String, XRRuleNode> ruleNodeMap = new HashMap<>();

	protected XRUniqObjBuilder uniqBuilder = new XRUniqObjBuilder();

	protected final Map<String, IRReteNode> varNodeMap = new HashMap<>();

	public XRNodeGraph(IRModel model, IREntryTable entryTable) {

		this.model = model;
		this.entryTable = entryTable;

		for (int i = 0; i < RReteType.RETE_TYPE_NUM; ++i) {
			this.nodeListArray[i] = new ReteNodeList();
		}
	}

	protected void _addReteNode(IRReteNode node) throws RException {

		RReteType reteType = node.getReteType();

		switch (reteType) {

		case RULE:
			XRRuleNode ruleNode = (XRRuleNode) node;
			this.ruleNodeMap.put(ruleNode.getRuleName(), (XRRuleNode) node);
			break;

		case ALPH0:
			if (!ReteUtil.isUniqReteStmt(_getUniqStmt(node.getUniqName()))) {
				throw new RException("invalid uniqname: " + node);
			}
			break;

		default:
			break;
		}

		this.nodeListArray[reteType.getIndex()].nodes.add(node);
		node.setGraphInfo(new XGraphInfo(node));
		nodeUniqNameMap.put(node.getUniqName(), node);
		_setNodeArray(node);
	}

	protected IRReteNode _buildAlphaNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

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

				IRReteNode parentNode = _findReteNode(RulpFactory.createNamedList(list, namedName), tmpVarBuilder);

				IRObject[] varEntry = ReteUtil._varEntry(parentNode.getVarEntry());
				varEntry[0] = null;

				XRReteNode1 alph0Node = RNodeFactory.createAlpha0Node(model, _getNextNodeId(),
						ReteUtil.uniqName(reteTree), stmtLen, parentNode, varEntry);

				ModelUtil.addConstraint(model, alph0Node, ConstraintFactory.createConstraintEqualIndex(0, 1));

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

					IRReteNode parentNode = _findReteNode(RulpFactory.createNamedList(list, namedName), tmpVarBuilder);

					IRObject[] varEntry = ReteUtil._varEntry(parentNode.getVarEntry());
					varEntry[lastVarPos] = null;

					XRReteNode1 alph0Node = RNodeFactory.createAlpha0Node(model, _getNextNodeId(),
							ReteUtil.uniqName(reteTree), stmtLen, parentNode, varEntry);
					ModelUtil.addConstraint(model, alph0Node,
							ConstraintFactory.createConstraintEqualIndex(lastSamePos, lastVarPos));
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

				if (namedName == null) {
					return _buildRootNode(stmtLen);
				} else {
					return _buildNamedNode(namedName, stmtLen);
				}

			} else {

				List<IRObject> list = RulpUtil.toArray(reteTree);

				list.set(lastValuePos, tmpVarBuilder.next());

				IRReteNode parentNode = _findReteNode(RulpFactory.createNamedList(list, namedName), tmpVarBuilder);

				XRReteNode1 alph0Node = RNodeFactory.createAlpha0Node(model, _getNextNodeId(),
						ReteUtil.uniqName(reteTree), stmtLen, parentNode,
						ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));
				ModelUtil.addConstraint(model, alph0Node,
						ConstraintFactory.createConstraintEqualValue(lastValuePos, lastValue));
				return alph0Node;
			}
		}
	}

	protected IRReteNode _buildBetaNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

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
					entryTable, leftNode, rightNode, varEntry, inheritIndexs, joinIndexList);

		}

		if (joinIndexList.isEmpty()) {

			IRConstraint1 matchNode = null;

			if (reteTree.size() == 3) {

				IRExpr expr = RulpUtil.asExpression(reteTree.get(2));
				// Optimize: '(?x ?y ?z) '(?a ?b ?c) (equal ?x ?a) ==> '(?x ?y ?z) '(?x ?b ?c)

				matchNode = ConstraintFactory.createConstraintExpr1Node(expr, thisVarList);
				if (matchNode == null) {
					matchNode = ConstraintFactory.createConstraintExpr0Node(expr, varEntry);
				}

				if (matchNode == null) {
					throw new RException("unlinked expr: " + expr);
				}
			}

			return RNodeFactory.createBeta3Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), beteEntryLen,
					entryTable, leftNode, rightNode, varEntry, inheritIndexs, matchNode);

		}

		// The left is the main inherit parent. The entry from the main inherit parent
		// can decide the child(beta) entry by its own. Since the parent's entry is
		// unique, so that we can ignore the main inherit's entry once it generate a
		// child entry.
		if (leftNode.getEntryLength() == beteEntryLen && ReteUtil.getMainInheritIndex(inheritIndexs) == 0) {
			return RNodeFactory.createBeta2Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), beteEntryLen,
					entryTable, leftNode, rightNode, varEntry, inheritIndexs, joinIndexList);

		}

		return RNodeFactory.createBeta0Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), beteEntryLen,
				entryTable, leftNode, rightNode, varEntry, inheritIndexs, joinIndexList);

	}

	protected IRReteNode _buildConstNode(IRList constStmt) throws RException {

		if (!ReteUtil.isAlphaMatchTree(constStmt) || ReteUtil.getStmtVarCount(constStmt) != 0) {
			throw new RException("Invalid const tree node found: " + constStmt);
		}

		int stmtLen = constStmt.size();
		IRRootNode parentNode = getRootNode(stmtLen);

		return RNodeFactory.createConstNode(model, _getNextNodeId(), constStmt, entryTable, parentNode);
	}

	protected IRReteNode _buildExprNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

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

				return RNodeFactory.createExpr2Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), entryLen,
						leftNode, ConstraintFactory.createConstraintExpr0Node(rightExpr,
								ReteUtil._varEntry(ReteUtil.buildTreeVarList(leftTree))),
						varEntry);

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

				return RNodeFactory.createExpr3Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
						varEntry.length, leftNode.getEntryLength(), entryTable, leftNode,
						ConstraintFactory.createConstraintExpr3Node(rightExpr), varEntry);

			}
		}

		/*********************************************************/
		// Expr1: (equal ?a b) or (not-equal ?a b)
		/*********************************************************/
		// alpha node
		if (RReteType.isAlphaType(leftNode.getReteType()) || RReteType.isRootType(leftNode.getReteType())) {

			List<IRObject> alphaVarList = RulpUtil.toArray(leftTree);

			IRConstraint1 expr1MatchNode = ConstraintFactory.createConstraintExpr1Node(rightExpr, alphaVarList);
			if (expr1MatchNode != null) {
				return RNodeFactory.createExpr1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
						leftNode.getEntryLength(), leftNode, expr1MatchNode, leftNode.getVarEntry());
			}

		}
		// expr 1
		else if (leftNode.getReteType() == RReteType.EXPR1) {

			// Find left last alpha node
			IRReteNode leftAlphaNode = leftNode.getParentNodes()[0];
			IRObject leftParentObj = leftTree.get(0);

			while (leftAlphaNode.getReteType() == RReteType.EXPR1) {
				leftAlphaNode = leftAlphaNode.getParentNodes()[0];
				leftParentObj = RulpUtil.asList(leftParentObj).get(0);
			}

			if (RReteType.isAlphaType(leftAlphaNode.getReteType()) || leftAlphaNode.getReteType() == RReteType.ROOT0) {

				List<IRObject> alphaVarList = RulpUtil.toArray(RulpUtil.asList(leftParentObj));

				IRConstraint1 expr1MatchNode = ConstraintFactory.createConstraintExpr1Node(rightExpr, alphaVarList);
				if (expr1MatchNode != null) {

					return RNodeFactory.createExpr1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
							leftNode.getEntryLength(), leftNode, expr1MatchNode, leftNode.getVarEntry());
				}
			}

		} else {

			IRConstraint1 expr1MatchNode = ConstraintFactory.createConstraintExpr1Node(rightExpr, leftVarList);
			if (expr1MatchNode != null) {
				return RNodeFactory.createExpr1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
						leftVarList.size(), leftNode, expr1MatchNode, ReteUtil._varEntry(leftVarList));
			}
		}

		// Other node

		/*********************************************************/
		// Expr0: (factor ?a b)
		/*********************************************************/
		IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree));
		int entryLen = leftNode.getEntryLength();

		return RNodeFactory.createExpr0Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), entryLen, leftNode,
				ConstraintFactory.createConstraintExpr0Node(rightExpr, varEntry), varEntry);

	}

	protected IRNamedNode _buildNamedNode(String namedName, int stmtLen) throws RException {

		if (namedName == null || stmtLen == -1) {
			throw new RException(String.format("invalid namedNode<%s:%d>", namedName, stmtLen));
		}

		IRNamedNode namedNode = namedNodeMap.get(namedName);

		if (namedNode == null) {

			namedNode = RNodeFactory.createName0Node(model, _getNextNodeId(), namedName, stmtLen);
			namedNodeMap.put(namedName, namedNode);

		} else if (namedNode.getEntryLength() != stmtLen) {
			throw new RException(String.format("EntryLength not match: expect=%d, actual=%s, node=", stmtLen,
					namedNode.getEntryLength(), namedNode.getNodeName()));
		}

		return namedNode;

	}

	protected IRReteNode _buildReteNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

		RType treeType = reteTree.getType();
		int treeSize = reteTree.size();
		IRObject e0 = reteTree.get(0);

		RType e0Type = reteTree.get(0).getType();
		RType e1Type = treeSize >= 2 ? reteTree.get(1).getType() : null;

		// Build alpha node: '(a b c)
		if (treeType == RType.LIST && ReteUtil.isEntryValueType(e0Type)) {

			// Build const node
			if (ReteUtil.getStmtVarCount(reteTree) == 0) {
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

			IRReteNode processNode = _buildReteNode(RulpFactory.createList(newReteTreeList), tmpVarBuilder);
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

	protected IRReteNode _processNodeModifier(IRReteNode node, String modifier) throws RException {

		switch (modifier) {

		// beta: '(?a) '(?b) entry-order
		case A_ENTRY_ORDER:

			// This constraint will be only added to "symmetric" betaNode
			if (isSymmetricBetaNode(node)) {
				RuleUtil.asBetaNode(node).addConstraint2(ConstraintFactory.createConstraint2EntryOrder());
			}

			break;

		default:
			throw new RException("invalid modifier: " + modifier);
		}

		return node;
	}

	protected IRReteNode _buildRootNode(int stmtLen) throws RException {

		IRRootNode rootNode = rootNodeArray[stmtLen];
		if (rootNode == null) {
			rootNode = RNodeFactory.createRoot0Node(model, _getNextNodeId(), stmtLen);
			rootNodeArray[stmtLen] = rootNode;
			if (stmtLen > maxRootStmtLen) {
				maxRootStmtLen = stmtLen;
			}
		}

		return rootNode;
	}

	protected IRReteNode _buildVarChangeNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

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

		case 3:
			/*****************************************************/
			// (var-changed ?varName new-value)
			/*****************************************************/
			return _buildVarChangeNode3(varName, reteTree, tmpVarBuilder);

		default:
			throw new RException("invalid tree: " + reteTree);
		}

	}

	protected IRReteNode _buildVarChangeNode3(String varName, IRList reteTree, XTempVarBuilder tmpVarBuilder)
			throws RException {

		IRObject obj = reteTree.get(2);

		/*****************************************************/
		// (var-changed ?varName ?v2)
		/*****************************************************/
		if (RulpUtil.isVarAtom(obj)) {

			// (var-changed ?varName ?anyVar ?tmp)
			List<IRObject> list = new ArrayList<>();
			list.add(reteTree.get(0));
			list.add(reteTree.get(1));
			list.add(tmpVarBuilder.next());
			list.add(reteTree.get(2));

			InheritIndex[] inheritIndexs = new InheritIndex[2];
			inheritIndexs[0] = new InheritIndex(0, 0);
			inheritIndexs[1] = new InheritIndex(0, 2);

			IRReteNode parentNode = _findReteNode(RulpFactory.createExpression(list), tmpVarBuilder);

			XRReteNode1 alph0Node = RNodeFactory.createAlpha2Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
					2, entryTable, parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)), inheritIndexs);

			return alph0Node;
		}

		/*****************************************************/
		// (var-changed ?varName new-value)
		/*****************************************************/

		// (var-changed ?varName ?tmp1 ?tmp2)
		List<IRObject> list = new ArrayList<>();
		list.add(reteTree.get(0));
		list.add(reteTree.get(1));
		list.add(tmpVarBuilder.next());
//		list.add(tmpVarBuilder.next());

		IRReteNode parentNode = _findReteNode(RulpFactory.createExpression(list), tmpVarBuilder);

		XRReteNode1 alph0Node = RNodeFactory.createAlpha1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree), 2,
				parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));

		// (?varName a ?tmp)
		ModelUtil.addConstraint(model, alph0Node, ConstraintFactory.createConstraintEqualValue(2, obj));
		return alph0Node;
	}

	protected IRReteNode _buildVarChangeNode4(String varName, IRList reteTree, XTempVarBuilder tmpVarBuilder)
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

			XRReteNode1 alph0Node = RNodeFactory.createAlpha1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
					3, parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));

			// (?varName a ?tmp)
			ModelUtil.addConstraint(model, alph0Node, ConstraintFactory.createConstraintEqualValue(1, reteTree.get(2)));
			ModelUtil.addConstraint(model, alph0Node, ConstraintFactory.createConstraintEqualValue(2, reteTree.get(3)));

			return alph0Node;
		}

		/*****************************************************/
		// (var-changed ?varName ?v1 value2)
		// (var-changed ?varName value ?v2)
		/*****************************************************/
		if (lastValue != null) {

			// (var-changed ?varName a ?tmp)
			List<IRObject> list = RulpUtil.toArray(reteTree);
			list.set(lastVarPos, tmpVarBuilder.next());

			IRReteNode parentNode = _findReteNode(RulpFactory.createExpression(list), tmpVarBuilder);

			// (?varName a ?tmp)

			XRReteNode1 alph0Node = RNodeFactory.createAlpha1Node(model, _getNextNodeId(), ReteUtil.uniqName(reteTree),
					3, parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(reteTree)));

			// (?varName a ?tmp)
			ModelUtil.addConstraint(model, alph0Node,
					ConstraintFactory.createConstraintEqualValue(lastVarPos - 1, lastValue));
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
			if (!varNode.addReteEntry(reteEntry)) {
				entryTable.removeEntry(reteEntry);
				return;
			}

			model.addUpdateNode(varNode);
		});

		varNodeMap.put(var.getName(), varNode);
		return varNode;
	}

	protected IRReteNode _buildVarNode(IRList reteTree, XTempVarBuilder tmpVarBuilder) throws RException {

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
		IRReteNode node = nodeUniqNameMap.get(uniqName);

//		if (uniqName.equals("'(?0 ?1 200)")) {
//			System.out.println();
//		}

		if (node == null) {
			node = _buildReteNode(reteTree, tmpVarBuilder);
			_addReteNode(node);
			node.setReteTree(reteTree);
		}

		return node;
	}

	protected IRReteNode _findReteNode(List<IRList> matchStmtList) throws RException {
		return _findReteNode(MatchTree.build(matchStmtList), new XTempVarBuilder());
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

	protected IRList _getUniqStmt(String uniqName) throws RException {

		List<IRObject> objs = model.getInterpreter().getParser().parse(uniqName);
		if (objs.size() != 1) {
			throw new RException("invalid uniq name found: " + uniqName);
		}

		return RulpUtil.asList(objs.get(0));
	}

	protected Set<IRReteNode> _listAlphaSourceNodes(IRReteNode node) throws RException {

		XGraphInfo info = (XGraphInfo) node.getGraphInfo();

		if (node.getReteType() != RReteType.ALPH0) {
			throw new RException("not alpha node: " + node);
		}

		if (info.sourceNodes == null) {
			info.sourceNodes = new HashSet<>();
		}

		if (info.lastRuleIndex < 0) {
			info.alphaUniqStmt = _getUniqStmt(node.getUniqName());
			info.lastRuleIndex = 0;
		}

		/******************************************************/
		// Update rule's action nodes
		/******************************************************/
		NEXT_RULE: for (IRReteNode ruleNode : listNodes(RReteType.RULE)) {

			// the rule has been processed
			if (ruleNode.getNodeId() <= info.lastRuleIndex) {
				continue;
			}

			info.lastRuleIndex = ruleNode.getNodeId();

			XGraphInfo ruleNodeInfo = (XGraphInfo) ruleNode.getGraphInfo();

			for (IRList ruleActionUniqStmt : ruleNodeInfo.getRuleActionUniqStmtList(this)) {
				if (ReteUtil.matchUniqStmt(ruleActionUniqStmt, info.alphaUniqStmt)) {
					info.sourceNodes.add(ruleNode);
					continue NEXT_RULE;
				}
			}
		}

		return info.sourceNodes;
	}

	protected Set<IRReteNode> _listRootOrNamedSourceNodes(IRReteNode node) throws RException {

		XGraphInfo info = (XGraphInfo) node.getGraphInfo();

		if (!RReteType.isRootType(node.getReteType())) {
			throw new RException("not root node: " + node);
		}

		if (info.sourceNodes == null) {
			info.sourceNodes = new HashSet<>();
		}

		if (info.lastRuleIndex < 0) {
			info.lastRuleIndex = 0;
		}

		String namedName = null;
		if (node.getReteType() == RReteType.NAME0) {
			namedName = ((IRNamedNode) node).getNamedName();
		}

		/******************************************************/
		// Update rule's action nodes
		/******************************************************/
		NEXT_RULE: for (IRReteNode ruleNode : listNodes(RReteType.RULE)) {

			// the rule has been processed
			if (ruleNode.getNodeId() <= info.lastRuleIndex) {
				continue;
			}

			info.lastRuleIndex = ruleNode.getNodeId();

			XGraphInfo ruleNodeInfo = (XGraphInfo) ruleNode.getGraphInfo();

			for (IRList ruleActionUniqStmt : ruleNodeInfo.getRuleActionUniqStmtList(this)) {
				if (ruleActionUniqStmt.size() == node.getEntryLength()
						&& RuleUtil.equal(namedName, ruleActionUniqStmt.getNamedName())) {
					info.sourceNodes.add(ruleNode);
					continue NEXT_RULE;
				}
			}
		}

		return info.sourceNodes;
	}

	protected void _setNodeArray(IRReteNode node) {

		int nodeId = node.getNodeId();
		while (nodeInfoArray.size() <= nodeId) {
			nodeInfoArray.add(null);
		}

		nodeInfoArray.set(nodeId, node);
	}

	@Override
	public IRRule addRule(String ruleName, IRList condList, IRList actionList, int priority) throws RException {

		if (ruleName == null) {
			ruleName = String.format("RU%03d", anonymousRuleIndex++);

		} else if (getRule(ruleName) != null) {
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

		IRList matchTree = MatchTree.build(actualMatchStmtList);
		IRReteNode parentNode = _findReteNode(matchTree, new XTempVarBuilder());

		XRRuleNode ruleNode = RNodeFactory.createRuleNode(model, _getNextNodeId(), ruleName,
				parentNode.getEntryLength(), parentNode, ReteUtil._varEntry(ReteUtil.buildTreeVarList(matchTree)),
				actionStmtList);

		ruleNode.setMatchStmtList(actualMatchStmtList);
		ruleNode.setPriority(priority);

		for (IRExpr expr : indexExprList) {
			ModelUtil.addConstraint(model, ruleNode,
					ConstraintFactory.createConstraintExpr4Node(expr, actualMatchStmtList));
		}

		/******************************************************/
		// Update node rule & priority
		/******************************************************/
		ModelUtil.travelReteParentNodeByPostorder(parentNode, (node) -> {

			if (node.getReteType() != RReteType.ROOT0) {

				ruleNode.addNode(node);
				if (node != ruleNode) {
					((XGraphInfo) node.getGraphInfo()).addRule(ruleNode);
				}

				if (node.getPriority() < priority) {
					node.setPriority(priority);
				}
			}
			return false;
		});

		ruleNode.addNode(ruleNode);
		_addReteNode(ruleNode);

		if (parentNode.getEntryQueue().size() > 0) {
			model.addUpdateNode(ruleNode);
		}

		return ruleNode;
	}

	@Override
	public IRReteNode addWorker(String name, IRWorker worker) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addWorker: " + name);
		}

		if (name == null) {
			name = String.format("W%03d", anonymousWorkIndex++);
		}

		if (nodeUniqNameMap.containsKey(name)) {
			throw new RException("Duplicate worker name: " + name);
		}

		IRReteNode workNode = RNodeFactory.createWorkerNode(model, _getNextNodeId(), name, worker);
		_addReteNode(workNode);

		return workNode;
	}

	@Override
	public void bindNode(IRReteNode fromNode, IRReteNode toNode) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> bindNode: " + fromNode + ", " + toNode);
		}

		XGraphInfo fromNodeInfo = (XGraphInfo) fromNode.getGraphInfo();
		XGraphInfo toNodeInfo = (XGraphInfo) toNode.getGraphInfo();
		fromNodeInfo.bind(toNodeInfo);
	}

	@Override
	public int doOptimize() throws RException {

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

				XRReteNode1 exprParent1 = (XRReteNode1) node;
				XRReteNode1 exprChild1 = (XRReteNode1) child;
				IRReteNode parentNode = exprParent1.getParentNodes()[0];

				// Remove link
				exprParent1.removeChild(exprChild1);

				// Add new link
				parentNode.addChildNode(exprChild1);
				exprChild1.setParentNodes(ReteUtil.toNodesArray(parentNode));

				// Copy match node from parent to child
				int consSize = exprParent1.getConstraint1Count();
				for (int i = 0; i < consSize; ++i) {
					ModelUtil.addConstraint(model, exprChild1, exprParent1.getConstraint1(i));
				}

				// Update priority
				if (exprParent1.getPriority() > exprChild1.getPriority()) {
					exprChild1.setPriority(exprParent1.getPriority());
				}

				// disable parent node
				exprParent1.setPriority(0);

				++optCount;

				break;

			default:
			}
		}

		return optCount;
	}

	@Override
	public IRNamedNode findNamedNode(String name) throws RException {
		return namedNodeMap.get(name);
	}

	@Override
	public IRRootNode findRootNode(int stmtLen) throws RException {
		return rootNodeArray[stmtLen];
	}

	@Override
	public List<IRReteNode> getBindFromNodes(IRReteNode node) throws RException {

		XGraphInfo nodeInfo = (XGraphInfo) node.getGraphInfo();

		return nodeInfo.bindFromNodeList == null ? Collections.emptyList() : nodeInfo.bindFromNodeList;
	}

	@Override
	public List<IRReteNode> getBindToNodes(IRReteNode node) throws RException {

		XGraphInfo nodeInfo = (XGraphInfo) node.getGraphInfo();
		return nodeInfo.bindToNodeList == null ? Collections.emptyList() : nodeInfo.bindToNodeList;
	}

	@Override
	public int getMaxRootStmtLen() {
		return maxRootStmtLen;
	}

	@Override
	public IRNamedNode getNamedNode(String name, int stmtLen) throws RException {

		IRNamedNode namedNode = namedNodeMap.get(name);
		if (namedNode == null) {
			namedNode = _buildNamedNode(name, stmtLen);
			_addReteNode(namedNode);
			namedNode.setReteTree(_getUniqStmt(ReteUtil.getNamedUniqName(name, stmtLen)));
		}

		if (stmtLen != -1 && namedNode.getEntryLength() != stmtLen) {
			throw new RException(String.format("EntryLength not match: expect=%d, actual=%s, node=%s", stmtLen,
					namedNode.getEntryLength(), namedNode.getNodeName()));
		}

		return namedNode;
	}

	@Override
	public IRReteNode getNodeById(int nodeId) {
		return _getNodeInfo(nodeId);
	}

	@Override
	public IRReteNode getNodeByTree(IRList tree) throws RException {

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
	public IReteNodeMatrix getNodeMatrix() {

		return new IReteNodeMatrix() {

			protected List<IRReteNode> allNodeList = null;

			@Override
			public List<? extends IRReteNode> getAllNodes() {

				if (allNodeList == null) {
					allNodeList = new ArrayList<>();
					for (RReteType t : RReteType.ALL_RETE_TYPE) {
						allNodeList.addAll(getNodeList(t));
					}
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
	public List<IRRule> getRelatedRules(IRReteNode node) throws RException {

		XGraphInfo nodeInfo = (XGraphInfo) node.getGraphInfo();
		return nodeInfo.relatedRules == null ? Collections.emptyList() : nodeInfo.relatedRules;
	}

	@Override
	public IRRootNode getRootNode(int stmtLen) throws RException {

		IRRootNode rootNode = rootNodeArray[stmtLen];
		if (rootNode == null) {
			rootNode = (IRRootNode) _buildRootNode(stmtLen);
			_addReteNode(rootNode);
			rootNode.setReteTree(_getUniqStmt(ReteUtil.getRootUniqName(stmtLen)));
		}

		return rootNode;
	}

	@Override
	public IRRule getRule(String ruleName) {
		return ruleNodeMap.get(ruleName);
	}

	@Override
	public int getUniqueObjectCount() {
		return uniqBuilder.size();
	}

	@Override
	public List<? extends IRReteNode> listNodes(RReteType reteType) {
		return this.nodeListArray[reteType.getIndex()].nodes;
	}

	@Override
	public Collection<IRReteNode> listSourceNodes(IRReteNode node) throws RException {

		switch (node.getReteType()) {
		case ALPH0:
			return _listAlphaSourceNodes(node);

		case ROOT0:
		case NAME0:
			return _listRootOrNamedSourceNodes(node);

		default:
			return Collections.emptySet();
		}
	}

}
