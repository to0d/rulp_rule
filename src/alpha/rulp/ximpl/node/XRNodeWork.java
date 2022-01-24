package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEAD;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRWorker;

public class XRNodeWork extends XRNodeRete0 {

	public XRNodeWork(String instanceName) {
		super(instanceName);
	}

	protected IRWorker worker;

	protected boolean workOnce = false;

	public void setWorker(IRWorker worker) {
		this.worker = worker;
	}

	@Override
	public int update() throws RException {

		nodeExecCount++;

		// work is completed, the node is dead
		if (!this.workOnce && worker.work(model)) {
			this.priority = RETE_PRIORITY_DEAD;
			this.workOnce = true;
			return 1;
		}

		return 0;
	}
}
