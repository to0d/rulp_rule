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

public class XRNodeSubGraph implements IRNodeSubGraph {

	static class ActivateInfo {

		public int newPriority = RETE_PRIORITY_DEAD;

		public IRReteNode node;

		public int oldPriority = RETE_PRIORITY_DEAD;

		public RReteStage oldStage;

		public boolean update = false;
	}

	private boolean activate = false;

	private Map<IRReteNode, ActivateInfo> activateMap = null;

	private List<ActivateInfo> activeInfoList = new LinkedList<>();

	private int cacheCount = 0;

	private XRNodeGraph graph;

	private List<IRReteNode> subNodeList = new LinkedList<>();

	private Set<IRReteNode> subNodeSet = new HashSet<>();

	public XRNodeSubGraph(XRNodeGraph graph) {
		super();
		this.graph = graph;
	}

	@Override
	public void activate() throws RException {

		if (activate) {
			return;
		}

		for (ActivateInfo info : activeInfoList) {

			if (info.node.getPriority() == RETE_PRIORITY_DEAD) {
				continue;
			}

			info.oldPriority = info.node.getPriority();
			info.oldStage = info.node.getReteStage();

			graph.setNodePriority(info.node, info.newPriority);
			info.update = true;

			if (info.newPriority >= graph.model.getPriority() && info.node.getReteStage() != RReteStage.InQueue) {
				graph.model.addUpdateNode(info.node);
			}

		}

		activate = true;
	}

	@Override
	public void addNode(IRReteNode node) throws RException {

		if (!subNodeSet.contains(node)) {
			subNodeSet.add(node);
			subNodeList.add(node);
		}
	}

	@Override
	public boolean containNode(IRReteNode node) {
		return subNodeSet.contains(node);
	}

	@Override
	public int getCacheCount() {
		return cacheCount;
	}

	@Override
	public int getNewPriority(IRReteNode node) {
		return activateMap.get(node).newPriority;
	}

	@Override
	public List<IRReteNode> getNodes() {
		return subNodeList;
	}

	@Override
	public void incCacheCount() {
		cacheCount++;
	}

	@Override
	public boolean isEmpty() {
		return subNodeSet.isEmpty();
	}

	@Override
	public void rollback() throws RException {

		if (!activate) {
			return;
		}

		// recovery old priority
		for (ActivateInfo info : activeInfoList) {

			// ignore dead node
			if (!info.update || info.node.getPriority() == RETE_PRIORITY_DEAD) {
				continue;
			}

			if (info.node.getPriority() != info.oldPriority) {

				graph.setNodePriority(info.node, info.oldPriority);

				if (info.oldPriority >= graph.model.getPriority() && info.oldStage == RReteStage.InQueue) {
					graph.model.addUpdateNode(info.node);
				} else {
					info.node.setReteStage(info.oldStage);
				}

				info.oldPriority = RETE_PRIORITY_DEAD;
			}

			info.update = false;
		}

		activate = false;
	}

	@Override
	public void setGraphPriority(int newPriority) throws RException {

		// Disable other nodes
		for (IRReteNode node : graph.getNodeMatrix().getAllNodes()) {

			if (RReteType.isRootType(node.getReteType())) {
				continue;
			}

			if (containNode(node)) {
				continue;
			}

			if (node.getPriority() < newPriority) {
				continue;
			}

			setNodePriority(node, RETE_PRIORITY_DISABLED);
		}

		for (IRReteNode node : getNodes()) {

			if (node.getPriority() <= RETE_PRIORITY_DISABLED) {
				continue;
			}

			if (node.getPriority() < newPriority) {
				setNodePriority(node, newPriority);
			}
		}
	}

	@Override
	public void setNodePriority(IRReteNode node, int newPriority) throws RException {

		if (activateMap == null) {
			activateMap = new HashMap<>();
		}

		ActivateInfo info = activateMap.get(node);
		if (info == null) {
			info = new ActivateInfo();
			info.node = node;
			activateMap.put(node, info);
			activeInfoList.add(info);
		}

		info.newPriority = newPriority;
	}

}
