package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;

public class XRNodeNamed extends XRNodeRete0 implements IRNamedNode {

	public XRNodeNamed(String instanceName) {
		super(instanceName);
	}

	public void cleanCache() throws RException {

		if (cache == null) {
			throw new RException("not cacher found");
		}

		this.cache.cleanCache();
		this.entryQueue.cleanCache();
		this.lastEntryCount = 0;
		this.reteStage = RReteStage.InActive;
	}
}
