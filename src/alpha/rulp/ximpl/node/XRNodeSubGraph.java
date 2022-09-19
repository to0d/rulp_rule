package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEAD;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DISABLED;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.node.IRNodeGraph.IRNodeSubGraph;
import alpha.rulp.ximpl.node.XRNodeGraph.ActivateInfo;

public class XRNodeSubGraph implements IRNodeSubGraph {

	private Map<IRReteNode, ActivateInfo> activateMap = null;

	private XRNodeGraph graph;

	private List<IRReteNode> subNodeList = new LinkedList<>();

	private Set<IRReteNode> subNodeSet = new HashSet<>();

	public XRNodeSubGraph(XRNodeGraph graph) {
		super();
		this.graph = graph;
	}

	@Override
	public void activate(int priority) throws RException {

		// Disable other nodes
		for (IRReteNode node : graph.getNodeMatrix().getAllNodes()) {

			if (RReteType.isRootType(node.getReteType())) {
				continue;
			}

			if (containNode(node)) {
				continue;
			}

			if (node.getPriority() < priority) {
				continue;
			}

			changeNodePriority(node, RETE_PRIORITY_DISABLED);
		}

		for (IRReteNode node : getNodes()) {

			if (node.getPriority() <= RETE_PRIORITY_DISABLED) {
				continue;
			}

			if (node.getPriority() < priority) {
				changeNodePriority(node, priority);
			}

			graph.model.addUpdateNode(node);
		}
	}

	public void addNode(IRReteNode node) throws RException {
		if (!subNodeSet.contains(node)) {
			subNodeSet.add(node);
			subNodeList.add(node);
		}
	}

	public void changeNodePriority(IRReteNode node, int priority) throws RException {

		if (activateMap == null) {
			activateMap = new HashMap<>();
		}

		ActivateInfo info = activateMap.get(node);
		if (info == null) {
			info = new ActivateInfo();
			info.node = node;
			info.oldPriority = node.getPriority();
			activateMap.put(node, info);
		}

		graph.setNodePriority(node, priority);
	}

	public boolean containNode(IRReteNode node) {
		return subNodeSet.contains(node);
	}

	@Override
	public List<IRReteNode> getNodes() {
		return subNodeList;
	}

	@Override
	public boolean isEmpty() {
		return subNodeSet.isEmpty();
	}

	@Override
	public void rollback() throws RException {

		if (activateMap == null) {
			return;
		}

		// recovery old priority
		for (ActivateInfo changeInfo : activateMap.values()) {

			// ignore dead node
			if (changeInfo.node.getPriority() == RETE_PRIORITY_DEAD) {
				continue;
			}

			graph.setNodePriority(changeInfo.node, changeInfo.oldPriority);
		}

		activateMap = null;
	}
}
