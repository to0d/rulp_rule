package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.Constant.RETE_PRIORITY_ROOT;

import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRReteNode.InheritIndex;
import alpha.rulp.rule.IRReteNode.JoinIndex;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.action.ActionUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.REntryFactory;
import alpha.rulp.ximpl.entry.REntryQueueType;
import alpha.rulp.ximpl.entry.XREntryQueueSingle;

public class RNodeFactory {

	public static AbsReteNode createAlpha0Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry) throws RException {

		XRNodeRete1 node = new XRNodeRete1(ReteUtil.getNodeName(RReteType.ALPH0, nodeId));

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
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static AbsReteNode createAlpha1Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry) throws RException {

		if (!(parentNode.getEntryQueue() instanceof XREntryQueueSingle)) {
			throw new RException("Invalid parent queue: " + parentNode);
		}

		XRNodeRete1 node = new XRNodeRete1(ReteUtil.getNodeName(RReteType.ALPH1, nodeId));

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
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.SINGLE, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static AbsReteNode createBeta0Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode leftNode, IRReteNode rightNode, IRObject[] varEntry, InheritIndex[] inheritIndexs,
			List<JoinIndex> joinIndexList) throws RException {

		XRNodeBeta0 node = new XRNodeBeta0(ReteUtil.getNodeName(RReteType.BETA0, nodeId));

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
		node.setEntryTable(model.getEntryTable());

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, leftNode, rightNode);

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

//	public static AbsReteNode createAlpha2Node(IRModel model, int nodeId, String uniqName, int entryLength,
//			IREntryTable entryTable, IRReteNode parentNode, IRObject[] varEntry, InheritIndex[] inheritIndexs)
//			throws RException {
//
//		if (!(parentNode.getEntryQueue() instanceof XREntryQueueSingle)) {
//			throw new RException("Invalid parent queue: " + parentNode);
//		}
//
//		XRNodeAlph2 node = new XRNodeAlph2();
//
//		// Model
//		node.setModel(model);
//
//		// Node id
//		node.setNodeId(nodeId);
//
//		// Node type
//		node.setReteType(RReteType.ALPH2);
//
//		// Uniq name
//		node.setUniqName(uniqName);
//
//		// Entry length
//		node.setEntryLength(entryLength);
//
//		// Entry table
//		node.setEntryTable(entryTable);
//
//		// Entry queue
//		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.SINGLE, entryLength));
//
//		node.setParentNodes(ReteUtil.toNodesArray(parentNode));
//		parentNode.addChildNode(node);
//
//		// var entry
//		node.setVarEntry(varEntry);
//
//		// Inherit Index
//		node.setInheritIndexs(inheritIndexs);
//
//		return node;
//	}

	public static AbsReteNode createBeta1Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode leftNode, IRReteNode rightNode, IRObject[] varEntry, InheritIndex[] inheritIndexs,
			List<JoinIndex> joinIndexList) throws RException {

		if (!(rightNode.getEntryQueue() instanceof XREntryQueueSingle)) {
			throw new RException("Invalid right parent queue: " + rightNode);
		}

		XRNodeBeta1 node = new XRNodeBeta1(ReteUtil.getNodeName(RReteType.BETA1, nodeId));

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
		node.setEntryTable(model.getEntryTable());

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, leftNode, rightNode);
//		leftNode.setChildNodeUpdateMode(node, false);

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

	public static AbsReteNode createBeta2Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode leftNode, IRReteNode rightNode, IRObject[] varEntry, InheritIndex[] inheritIndexs,
			List<JoinIndex> joinIndexList) throws RException {

		XRNodeBeta2 node = new XRNodeBeta2(ReteUtil.getNodeName(RReteType.BETA2, nodeId));

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
		node.setEntryTable(model.getEntryTable());

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, leftNode, rightNode);

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

	public static AbsReteNode createBeta3Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode leftNode, IRReteNode rightNode, IRObject[] varEntry, InheritIndex[] inheritIndexs)
			throws RException {

		XRNodeBeta3 node = new XRNodeBeta3(ReteUtil.getNodeName(RReteType.BETA3, nodeId));

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
		node.setEntryTable(model.getEntryTable());

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, leftNode, rightNode);

		// var entry
		node.setVarEntry(varEntry);

		// Inherit Index
		node.setInheritIndexs(inheritIndexs);

		return node;
	}

	public static AbsReteNode createConstNode(IRModel model, int nodeId, IRList constStmt, IREntryTable entryTable,
			IRReteNode parentNode) throws RException {

		String constUniqName = ReteUtil.uniqName(constStmt);

		XRNodeConst node = new XRNodeConst(ReteUtil.getNodeName(RReteType.CONST, nodeId));

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
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.SINGLE, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		return node;
	}

	public static XRNodeRete1 createDupNode(IRModel model, int nodeId, String uniqName, IRReteNode parentNode)
			throws RException {

		XRNodeRete1 node = new XRNodeRete1(ReteUtil.getNodeName(RReteType.DUP, nodeId));

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.DUP);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(parentNode.getEntryLength());

		// Entry table
		node.setEntryTable(model.getEntryTable());

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(parentNode.getVarEntry());

		return node;

	}

	public static AbsReteNode createExpr0Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry) throws RException {

		XRNodeRete1 node = new XRNodeRete1(ReteUtil.getNodeName(RReteType.EXPR0, nodeId));

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
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static AbsReteNode createExpr1Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry) throws RException {

		XRNodeRete1 node = new XRNodeRete1(ReteUtil.getNodeName(RReteType.EXPR1, nodeId));

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
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static AbsReteNode createExpr2Node(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry) throws RException {

		XRNodeRete1 node = new XRNodeRete1(ReteUtil.getNodeName(RReteType.EXPR2, nodeId));

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
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static AbsReteNode createExpr3Node(IRModel model, int nodeId, String uniqName, int entryLength,
			int leftEnryLength, IRReteNode parentNode, IRObject[] varEntry) throws RException {

		XRNodeExpr3 node = new XRNodeExpr3(ReteUtil.getNodeName(RReteType.EXPR3, nodeId));

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
		node.setEntryTable(model.getEntryTable());

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static AbsReteNode createExpr4Node(IRModel model, int nodeId, String uniqName, int entryLength,
			int leftEnryLength, IRReteNode parentNode, IRConstraint1 matchNode, IRObject[] varEntry) throws RException {

		XRNodeExpr3 node = new XRNodeExpr3(ReteUtil.getNodeName(RReteType.EXPR4, nodeId));

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
		node.setEntryTable(model.getEntryTable());

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

//		// constant
//		ModelUtil.addConstraint(model, node, matchNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static AbsReteNode createIndexNode(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry, List<OrderEntry> orderList) throws RException {

		XRNodeIndex node = new XRNodeIndex(ReteUtil.getNodeName(RReteType.INDEX, nodeId));

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.INDEX);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Order list
		node.setOrderList(orderList);

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.ORDER, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;
	}

	public static AbsReteNode createInheritNode(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry, int[] inheritIndexs) throws RException {

		XRNodeInher node = new XRNodeInher(ReteUtil.getNodeName(RReteType.INHER, nodeId));

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.INHER);

		// Uniq name
		node.setUniqName(uniqName);

		// Entry length
		node.setEntryLength(entryLength);

		// Entry table
		node.setEntryTable(model.getEntryTable());

		// Inherit index list
		node.setInheritIndexs(inheritIndexs);

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.MULTI, node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		// var entry
		node.setVarEntry(varEntry);

		return node;

	}

	public static XRNodeNamed createName0Node(IRModel model, int nodeId, String namedName, int stmtLen)
			throws RException {

		XRNodeNamed node = new XRNodeNamed(namedName);

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.NAME0);

		// named name
		node.setNamedName(namedName);

		// Uniq name
		node.setUniqName(ReteUtil.getRootUniqName(namedName, stmtLen));

		// Entry length
		node.setEntryLength(stmtLen);

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.UNIQ, node));

		// Var entry
		node.setVarEntry(new IRObject[stmtLen]);

		// Node priority
		node.setPriority(0);

		return node;
	}

	public static IRFrame createNodeFrame(IRReteNode node) throws RException {
		return RulpFactory.createFrame(node.getModel().getFrame(), "NF-" + node.getNodeName());
	}

	public static AbsReteNode createRoot0Node(IRModel model, int nodeId, int stmtLen) throws RException {

		XRNodeRete0 node = new XRNodeRete0("root" + stmtLen);

		// Model
		node.setModel(model);

		// Node id
		node.setNodeId(nodeId);

		// Node type
		node.setReteType(RReteType.ROOT0);

		// Uniq name
		node.setUniqName(ReteUtil.getRootUniqName(null, stmtLen));

		// Entry length
		node.setEntryLength(stmtLen);

		// Entry queue
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.UNIQ, node));

		// Var entry
		node.setVarEntry(new IRObject[stmtLen]);

		// Node priority
		node.setPriority(RETE_PRIORITY_ROOT);

		return node;
	}

	public static XRNodeRule0 createRuleNode(IRModel model, int nodeId, String uniqName, int entryLength,
			IRReteNode parentNode, IRObject[] varEntry, List<IRExpr> actionStmtList) throws RException {

		XRNodeRule0 node = new XRNodeRule0(uniqName);

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

		// Var entry
		node.setVarEntry(varEntry);

		// Actions
		for (IAction action : ActionUtil.buildActions(model, varEntry, actionStmtList)) {
			node.addAction(action);
		}

		// Build action nodes
		node.setEntryQueue(REntryFactory.createActionQueue(node));

		// Parent node
		ReteUtil.setParentNodes(node, parentNode);

		return node;
	}

	public static AbsReteNode createVarNode(IRModel model, int nodeId, IRVar var, String reteExpression,
			IREntryTable entryTable) throws RException {

		XRNodeRete0 node = new XRNodeRete0(ReteUtil.getNodeName(RReteType.VAR, nodeId));

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
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.SINGLE, node));

		return node;
	}

	public static AbsReteNode createWorkerNode(IRModel model, int nodeId, String uniqName, IRWorker worker)
			throws RException {

		XRNodeWork node = new XRNodeWork(ReteUtil.getNodeName(RReteType.WORK, nodeId));

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
		node.setEntryQueue(REntryFactory.createQueue(REntryQueueType.EMPTY, node));

		// Node worker
		node.setWorker(worker);

		return node;
	}
}
