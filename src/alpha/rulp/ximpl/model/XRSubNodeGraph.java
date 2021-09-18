package alpha.rulp.ximpl.model;

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

	private List<QuerySourceInfo> changeList = new LinkedList<>();

	private List<IRReteNode> changeNodes = new LinkedList<>();

	private Set<IRReteNode> visitedNodes = new HashSet<>();

	private final IRNodeGraph nodeGraph;

	public XRSubNodeGraph(IRNodeGraph nodeGraph) {
		super();
		this.nodeGraph = nodeGraph;
	}

//	public void buildSourceNodeGraph(IRReteNode queryNode) throws RException {
//
//	}

	public void buildSourceNodeGraph(IRReteNode queryNode) throws RException {

		boolean isRootMode = RReteType.isRootType(queryNode.getReteType());

		/******************************************************/
		// Build source graph
		/******************************************************/
		Map<IRReteNode, QuerySourceInfo> sourceMap = new HashMap<>();
		LinkedList<QuerySourceEntry> visitStack = new LinkedList<>();
		visitStack.add(new QuerySourceEntry(null, queryNode));

		while (!visitStack.isEmpty()) {

			QuerySourceEntry entry = visitStack.pop();
			IRReteNode fromNode = entry.fromNode;
			IRReteNode sourceNode = entry.sourceNode;

			if (sourceNode.getPriority() < RETE_PRIORITY_INACTIVE) {
				continue;
			}

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

			// source node is not visited before
			QuerySourceInfo info = sourceMap.get(sourceNode);
			if (info == null) {
				info = new QuerySourceInfo();
				info.node = sourceNode;
				info.oldPriority = sourceNode.getPriority();
				sourceNode.setPriority(new_priority);
				changeList.add(info);
			}

			// ignore visited node
			if (visitedNodes.contains(sourceNode)) {
				continue;
			}

			// add all source nodes
			visitedNodes.add(sourceNode);

			// add changed node
			changeNodes.add(sourceNode);

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
	}

	public List<IRReteNode> getAllNodes() {
		return changeNodes;
	}

	public void rollback() throws RException {

		// recovery priority
		for (QuerySourceInfo changeInfo : changeList) {

			if (changeInfo.oldPriority == -1) {
				throw new RException("partial recovery priority invalid: " + changeInfo.node);
			}

			// ignore dead node
			if (changeInfo.node.getPriority() >= 0) {
				changeInfo.node.setPriority(changeInfo.oldPriority);
			}

			changeInfo.oldPriority = -1;
		}
	}
}
