package alpha.rulp.ximpl.node;

import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RRunState;
import alpha.rulp.rule.IRReteNode.InheritIndex;
import alpha.rulp.utils.DeCounter;
import alpha.rulp.ximpl.cache.IRCacheWorker;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.model.IGraphInfo;

public class XRNodeNamed extends XRNodeRete0 implements IRNamedNode {

	public XRNodeNamed(String instanceName) {
		super(instanceName);
	}

	protected IRCacheWorker cache;

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
