package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.Constant.RETE_PRIORITY_ROOT;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.action.ActionUtil;
import alpha.rulp.ximpl.action.IActionNode1;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.XREntryQueueAction;
import alpha.rulp.ximpl.entry.XREntryQueueEmpty;
import alpha.rulp.ximpl.entry.XREntryQueueExecuteStmt;
import alpha.rulp.ximpl.entry.XREntryQueueMulitEntryList;
import alpha.rulp.ximpl.entry.XREntryQueueSingleEntryList;
import alpha.rulp.ximpl.node.IRReteNode.InheritIndex;
import alpha.rulp.ximpl.node.IRReteNode.JoinIndex;

public class RNodeFactory {

	public static XRReteNode1 createAlpha0Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry) throws RException {

		XRReteNode1 node = new XRReteNode1();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.ALPH0);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static XRReteNode1 createAlpha1Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry) throws RException {

		if (!(parentNode.getEntryQueue() instanceof XREntryQueueSingleEntryList)) {
			throw new RException("Invalid parent queue: " + parentNode);
		}

		XRReteNode1 node = new XRReteNode1();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.ALPH1);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry queue
		node.setEntryQueue(new XREntryQueueSingleEntryList(entryLength));

		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static XRReteNode1 createAlpha2Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IREntryTable entryTable, IRReteNode parentNode, IRObject[] varEntry, InheritIndex[] inheritIndexs)
			throws RException {

		if (!(parentNode.getEntryQueue() instanceof XREntryQueueSingleEntryList)) {
			throw new RException("Invalid parent queue: " + parentNode);
		}

		XRAlpha2Node node = new XRAlpha2Node();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.ALPH2);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry table
		node.setEntryTable(entryTable);

		// Entry queue
		node.setEntryQueue(new XREntryQueueSingleEntryList(entryLength));

		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// var entry
		node.setVarEntry(varEntry);

		// Inherit Index
		node.setInheritIndexs(inheritIndexs);

		return node;
	}

	public static IRReteNode createBeta0Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IREntryTable entryTable, IRReteNode leftNode, IRReteNode rightNode, IRObject[] varEntry,
			InheritIndex[] inheritIndexs, List<JoinIndex> joinIndexList) throws RException {

		XRBeta0Node node = new XRBeta0Node();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.BETA0);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry table
		node.setEntryTable(entryTable);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(leftNode, rightNode));
		leftNode.addChildNode(node);
		rightNode.addChildNode(node);

		// var entry
		node.setVarEntry(varEntry);

		// Inherit Index
		node.setInheritIndexs(inheritIndexs);

		// Join Index
		for (JoinIndex joinIndex : joinIndexList) {
			node.addJoinIndex(joinIndex);
		}

		return node;
	}

	public static IRReteNode createBeta1Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IREntryTable entryTable, IRReteNode leftNode, IRReteNode rightNode, IRObject[] varEntry,
			InheritIndex[] inheritIndexs, List<JoinIndex> joinIndexList) throws RException {

		if (!(rightNode.getEntryQueue() instanceof XREntryQueueSingleEntryList)) {
			throw new RException("Invalid right parent queue: " + rightNode);
		}

		XRBeta1Node node = new XRBeta1Node();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.BETA1);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry table
		node.setEntryTable(entryTable);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(leftNode, rightNode));
		leftNode.addChildNode(node);
		rightNode.addChildNode(node);
		leftNode.setChildNodeUpdateMode(node, false);

		// var entry
		node.setVarEntry(varEntry);

		// Inherit Index
		node.setInheritIndexs(inheritIndexs);

		// Join Index
		for (JoinIndex joinIndex : joinIndexList) {
			node.addJoinIndex(joinIndex);
		}

		return node;
	}

	public static IRReteNode createBeta2Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IREntryTable entryTable, IRReteNode leftNode, IRReteNode rightNode, IRObject[] varEntry,
			InheritIndex[] inheritIndexs, List<JoinIndex> joinIndexList) throws RException {

		XRBeta2Node node = new XRBeta2Node();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.BETA2);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry table
		node.setEntryTable(entryTable);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(leftNode, rightNode));
		leftNode.addChildNode(node);
		rightNode.addChildNode(node);

		// var entry
		node.setVarEntry(varEntry);

		// Inherit Index
		node.setInheritIndexs(inheritIndexs);

		// Join Index
		for (JoinIndex joinIndex : joinIndexList) {
			node.addJoinIndex(joinIndex);
		}

		return node;
	}

	public static IRReteNode createBeta3Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IREntryTable entryTable, IRReteNode leftNode, IRReteNode rightNode, IRObject[] varEntry,
			InheritIndex[] inheritIndexs, IRConstraint1 matchNode) throws RException {

		XRBeta3Node node = new XRBeta3Node();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.BETA3);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry table
		node.setEntryTable(entryTable);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(leftNode, rightNode));
		leftNode.addChildNode(node);
		rightNode.addChildNode(node);

		// var entry
		node.setVarEntry(varEntry);

		// Inherit Index
		node.setInheritIndexs(inheritIndexs);

		// match mode
		if (matchNode != null) {
			node.addConstraint(matchNode);
		}

		return node;
	}

	public static AbsReteNode createConstNode(IRModel model, int nodeId, IRList constStmt, IREntryTable entryTable,
			IRRootNode parentNode) throws RException {

		String constUniqName = ReteUtil.uniqName(constStmt);

		XRConstNode node = new XRConstNode();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.CONST);

		// Uniq name
		node.setUniqName(constUniqName);

		// Entry length
		node.setEntryLength(constStmt.size());

		// Entry queue
		{
			XREntryQueueSingleEntryList entryQueue = new XREntryQueueSingleEntryList(constStmt.size());
			entryQueue.setBindNode(node);
			entryQueue.setEntryTable(entryTable);
			node.setEntryQueue(entryQueue);
		}

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		return node;
	}

	public static IRReteNode createExpr0Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRConstraint1 matchNode, IRObject[] varEntry) throws RException {

		XRReteNode1 node = new XRReteNode1();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.EXPR0);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// constant
		node.addConstraint(matchNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static IRReteNode createExpr1Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRConstraint1 matchNode, IRObject[] varEntry) throws RException {

		XRReteNode1 node = new XRReteNode1();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.EXPR1);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// constant
		node.addConstraint(matchNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static IRReteNode createExpr2Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRConstraint1 matchNode, IRObject[] varEntry) throws RException {

		XRReteNode1 node = new XRReteNode1();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.EXPR2);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// constant
		node.addConstraint(matchNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static IRReteNode createExpr3Node(IRModel model, int nodeId, String uniqName, int entryLength,
			int leftEnryLength, IREntryTable entryTable, IRReteNode parentNode, IRConstraint1 matchNode,
			IRObject[] varEntry) throws RException {

		XRExprNode3 node = new XRExprNode3();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.EXPR3);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Left entry length
		node.setLeftEnryLength(leftEnryLength);

		// Entry table
		node.setEntryTable(entryTable);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// constant
		node.addConstraint(matchNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static IRReteNode createExpr4Node(IRModel model, int nodeId, String uniqName, int entryLength,
			int leftEnryLength, IREntryTable entryTable, IRReteNode parentNode, IRConstraint1 matchNode,
			IRObject[] varEntry) throws RException {

		XRExprNode3 node = new XRExprNode3();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.EXPR4);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Left entry length
		node.setLeftEnryLength(leftEnryLength);

		// Entry table
		node.setEntryTable(entryTable);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(entryLength));

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// constant
		node.addConstraint(matchNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static IRNamedNode createName0Node(IRModel model, int nodeId, String namedName, int stmtLen)
			throws RException {

		XRNamedNode node = new XRNamedNode();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.NAME0);

		// Entry Name
		node.setNamedName(namedName);

		// Uniq name
		node.setUniqName(ReteUtil.getNamedUniqName(namedName, stmtLen));

		// Entry length
		node.setEntryLength(stmtLen);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(stmtLen));

		// Entry table
		node.setEntryTable(model.getEntryTable());

		// Var entry
		node.setVarEntry(new IRObject[stmtLen]);

		// Node Default Priority
		node.setPriority(0);

		return node;
	}

	public static IRFrame createNodeFrame(IRReteNode node) throws RException {
		return RulpFactory.createFrame(node.getModel().getModelFrame(), "NF-" + node.getNodeName());
	}

	public static IRRootNode createRoot0Node(IRModel model, int nodeId, int stmtLen) throws RException {

		XRRoot0Node node = new XRRoot0Node();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.ROOT0);

		// Uniq name
		node.setUniqName(ReteUtil.getRootUniqName(stmtLen));

		// Entry length
		node.setEntryLength(stmtLen);

		// Entry queue
		node.setEntryQueue(new XREntryQueueMulitEntryList(stmtLen));

		// Entry table
		node.setEntryTable(model.getEntryTable());

		// Var entry
		node.setVarEntry(new IRObject[stmtLen]);

		// Node Default Priority
		node.setPriority(RETE_PRIORITY_ROOT);

		return node;
	}

	public static XRRuleNode createRuleNode(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry, List<IRExpr> actionStmtList) throws RException {

		XRRuleNode node = new XRRuleNode();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.RULE);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Try build action queue
		List<IActionNode1> actionNodes = null;

		if (!actionStmtList.isEmpty()) {

			boolean canBuildActionQueue = true;

			actionNodes = new ArrayList<>();

			for (IRExpr actionStmt : actionStmtList) {

				List<IActionNode1> acNodes = ActionUtil.tryBuildActionNodes(varEntry, actionStmt);
				if (acNodes == null) {
					canBuildActionQueue = false;
					break;
				}

				actionNodes.addAll(acNodes);
			}

			if (!canBuildActionQueue) {
				actionNodes = null;
			}
		}

		// Action entry queue
		if (actionNodes != null) {

			XREntryQueueAction entryQueue = new XREntryQueueAction(node);
			entryQueue.addActionNodes(actionNodes);
			node.setEntryQueue(entryQueue);

		}
		// Exec entry queue
		else {

			XREntryQueueExecuteStmt entryQueue = new XREntryQueueExecuteStmt(node);
			entryQueue.addActionStmts(actionStmtList);
			node.setEntryQueue(entryQueue);
		}

		// Var entry
		node.setVarEntry(varEntry);

		// Parent node
		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
		parentNode.addChildNode(node);

		// Actions
		node.setActionStmtList(actionStmtList);

		// Model
		node.setModel(model);

		return node;
	}

	public static AbsReteNode createVarNode(IRModel model, int nodeId, IRVar var, String reteExpression,
			IREntryTable entryTable) throws RException {

		XRReteNode0 node = new XRReteNode0();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.VAR);

		// Uniq name
		node.setUniqName(reteExpression);

		// Entry length
		node.setEntryLength(3);

		// Entry queue
		{
			XREntryQueueSingleEntryList entryQueue = new XREntryQueueSingleEntryList(3);
			entryQueue.setBindNode(node);
			entryQueue.setEntryTable(entryTable);
			node.setEntryQueue(entryQueue);
		}

		return node;
	}

	public static AbsReteNode createWorkerNode(IRModel model, int nodeId, String uniqName, IRWorker worker)
			throws RException {

		XRWorkerNode node = new XRWorkerNode();

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.WORK);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(0);

		// Entry queue
		node.setEntryQueue(new XREntryQueueEmpty());

		// Node worker
		node.setWorker(worker);

		return node;
	}
}
