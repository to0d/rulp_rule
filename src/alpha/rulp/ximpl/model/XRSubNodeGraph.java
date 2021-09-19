package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEAD;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_INACTIVE;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MAX;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MIN;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRule;
import alpha.rulp.utils.ModelUtil;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.IRReteNode;
import alpha.rulp.ximpl.node.RReteType;

public class XRSubNodeGraph {

	static class QuerySourceEntry {

		IRReteNode fromNode;
		IRReteNode sourceNode;

		public QuerySourceEntry(IRReteNode fromNode, IRReteNode sourceNode) {
			super();
			this.fromNode = fromNode;
			this.sourceNode = sourceNode;
		}
	}

	static class QuerySourceInfo {
		public IRReteNode node;
		public int oldPriority = -1;
	}

	private List<IRReteNode> allNodes = new LinkedList<>();

	private final IRNodeGraph nodeGraph;

	private Map<IRReteNode, QuerySourceInfo> sourceMap = new HashMap<>();

	public XRSubNodeGraph(IRNodeGraph nodeGraph) {
		super();
		this.nodeGraph = nodeGraph;
	}

	public void addNode(IRReteNode node, int newPriority) throws RException {

		if (node.getPriority() <= RETE_PRIORITY_DEAD) {
			return;
		}

		QuerySourceInfo info = sourceMap.get(node);
		if (info == null) {
			info = new QuerySourceInfo();
			info.node = node;
			sourceMap.put(node, info);
			allNodes.add(node);
		}

		if (newPriority > node.getPriority()) {

			if (info.oldPriority == -1) {
				info.oldPriority = node.getPriority();
			}

			node.setPriority(newPriority);
		}
	}

	public void disableAllOtherNodes(int minPriority) throws RException {

		for (IRReteNode node : nodeGraph.getNodeMatrix().getAllNodes()) {

			if (RReteType.isRootType(node.getReteType())) {
				continue;
			}

			if (sourceMap.containsKey(node)) {
				continue;
			}

			if (node.getPriority() < minPriority) {
				continue;
			}

			QuerySourceInfo info = new QuerySourceInfo();
			info.node = node;
			info.oldPriority = node.getPriority();
			node.setPriority(RETE_PRIORITY_INACTIVE);

			sourceMap.put(node, info);
		}
	}

	public void addRule(IRRule ruleNode, int priority) throws RException {

		ModelUtil.travelReteParentNodeByPostorder(ruleNode, (node) -> {

			if (!containNode(node) && node.getPriority() < priority) {
				addNode(node, priority);
			}

			return false;
		});
	}

	public void buildSourceNodeGraph(IRReteNode queryNode) throws RException {

		boolean isRootMode = RReteType.isRootType(queryNode.getReteType());

		/******************************************************/
		// Build source graph
		/******************************************************/
		LinkedList<QuerySourceEntry> visitStack = new LinkedList<>();
		visitStack.add(new QuerySourceEntry(null, queryNode));
		Set<IRReteNode> visitedNodes = new HashSet<>();

		while (!visitStack.isEmpty()) {

			QuerySourceEntry entry = visitStack.pop();
			IRReteNode fromNode = entry.fromNode;
			IRReteNode sourceNode = entry.sourceNode;

			// ignore visited node
			if (visitedNodes.contains(sourceNode)) {
				continue;
			}

			visitedNodes.add(sourceNode);

//			if (sourceNode.getPriority() < minPriority) {
//				continue;
//			}

			if (!isRootMode && RReteType.isRootType(sourceNode.getReteType())) {
				continue;
			}

			int new_priority = RETE_PRIORITY_PARTIAL_MAX;
			if (sourceNode != queryNode) {
				new_priority = Math.min(sourceNode.getPriority(), fromNode.getPriority()) - 1;
				if (new_priority < RETE_PRIORITY_PARTIAL_MIN) {
					new_priority = RETE_PRIORITY_PARTIAL_MIN;
				}
			}

			addNode(sourceNode, new_priority);

			if (sourceNode.getParentNodes() != null) {
				for (IRReteNode newSrcNode : sourceNode.getParentNodes()) {
					visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
				}
			}

			for (IRReteNode newSrcNode : nodeGraph.getBindFromNodes(sourceNode)) {
				visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
			}

			for (IRReteNode newSrcNode : nodeGraph.listSourceNodes(sourceNode)) {
				visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
			}
		}

		ModelUtil.travelReteParentNodeByPostorder(queryNode, (node) -> {

			if (!containNode(node)) {
				addNode(node, RETE_PRIORITY_PARTIAL_MIN);
				visitStack.add(new QuerySourceEntry(null, node));
				visitedNodes.add(node);
			}

			return false;
		});
	}

	public boolean containNode(IRReteNode node) {
		return sourceMap.containsKey(node);
	}

	public List<IRReteNode> getAllNodes() {
		return allNodes;
	}

	public void rollback() throws RException {

		// recovery priority
		for (QuerySourceInfo changeInfo : sourceMap.values()) {

			if (changeInfo.oldPriority != -1) {

				// ignore dead node
				if (changeInfo.node.getPriority() >= 0) {
					changeInfo.node.setPriority(changeInfo.oldPriority);
				}

				changeInfo.oldPriority = -1;
			}

		}

		sourceMap.clear();
	}
}
