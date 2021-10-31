package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEAD;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEFAULT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DISABLED;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MAX;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MIN;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.ximpl.model.XRSubNodeGraph.QuerySourceInfo;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.RReteType;

public class XRSubNodeGraph implements IRSubNodeGraph {

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

	public boolean containNode(IRReteNode node) {
		return sourceMap.containsKey(node);
	}

	public void disableAllOtherNodes(int minPriority, int toPriority) throws RException {

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
			node.setPriority(toPriority);

			sourceMap.put(node, info);
		}
		
	}

	@Override
	public List<IRReteNode> getAllNodes() {
		return allNodes;
	}

	@Override
	public void rollback() throws RException {

		// recovery priority
		for (QuerySourceInfo changeInfo : sourceMap.values()) {

			if (changeInfo.oldPriority != -1) {

				// ignore dead node
				if (changeInfo.node.getPriority() != RETE_PRIORITY_DEAD) {
					changeInfo.node.setPriority(changeInfo.oldPriority);
				}

				changeInfo.oldPriority = -1;
			}

		}

		sourceMap.clear();
	}
}
