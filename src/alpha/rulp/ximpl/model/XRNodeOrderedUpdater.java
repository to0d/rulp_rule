package alpha.rulp.ximpl.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.ximpl.node.IRReteNode;

public class XRNodeOrderedUpdater {

	protected List<IRReteNode> updateNodeList = new ArrayList<>();

	protected Set<IRReteNode> updateNodeSet = new HashSet<>();

	public void addNodeUpdateList(IRReteNode reteNode) throws RException {

		if (updateNodeSet.contains(reteNode)) {
			return;
		}

		if (reteNode.getParentNodes() != null) {
			for (IRReteNode parentNode : reteNode.getParentNodes()) {
				addNodeUpdateList(parentNode);
			}
		}

		updateNodeSet.add(reteNode);
		updateNodeList.add(reteNode);
	}

	public List<IRReteNode> getNodeList() {
		return updateNodeList;
	}

	public int process(IRModel model, int maxStep) throws RException {

		int runTimes = 0;

		for (IRReteNode node : updateNodeList) {

			runTimes++;

			// need exit with runnable status
			if (maxStep > 0 && runTimes > maxStep) {
				--runTimes;
				break;
			}

			switch (node.getRunState()) {
			case Completed:
			case Runnable:
			case Running:
				model.execute(node);
				break;

			// don't process 'Failed' or 'Halting' node
			case Failed:
			case Halting:
				continue;

			default:
				throw new RException("unknown state: " + node.getRunState());
			}
		}

		return runTimes;
	}
}
