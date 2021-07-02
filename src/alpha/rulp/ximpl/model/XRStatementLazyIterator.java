package alpha.rulp.ximpl.model;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.node.IRReteNode;
import alpha.rulp.ximpl.node.RReteType;

public class XRStatementLazyIterator extends XRNodeOrderedUpdater implements IRIterator<IRList> {

	private boolean isProcessing = false;

	private IRReteEntry lastEntry = null;

	private int lastStmtIndex = 0;

	protected IREntryQueue matchedEntryQueue;

	protected IRReteNode matchedNode;

	protected final XRModel model;

	protected IRReteNode rootNode;

	private int rootStmtCount = 0;

	public XRStatementLazyIterator(XRModel model, IRReteNode matchedNode) throws RException {
		super();
		this.model = model;
		this.matchedNode = matchedNode;
		this.matchedEntryQueue = matchedNode.getEntryQueue();
		this.addNodeUpdateList(matchedNode);
	}

	public void addNodeUpdateList(IRReteNode reteNode) throws RException {

		super.addNodeUpdateList(reteNode);

		if (reteNode.getReteType() == RReteType.ROOT0) {
			this.rootNode = reteNode;
		}

	};

	@Override
	public boolean hasNext() throws RException {

		if (lastEntry == null) {
			update();

			while (lastStmtIndex < matchedEntryQueue.size()) {
				lastEntry = matchedEntryQueue.getEntryAt(lastStmtIndex++);
				if (lastEntry != null && !lastEntry.isDroped()) {
					break;
				}
			}
		}

		return lastEntry != null;
	}

	@Override
	public IRList next() throws RException {

		if (!hasNext()) {
			return null;
		}

		IRReteEntry entry = lastEntry;
		lastEntry = null;
		return entry;
	}

	public void update() throws RException {

		if (rootStmtCount == rootNode.getEntryQueue().size()) {
			return;
		}

		// Let loop running in top level
		if (this.isProcessing) {
			return;
		}

		this.isProcessing = true;

		try {

			process(model, -1);

		} finally {
			this.isProcessing = false;
			this.rootStmtCount = rootNode.getEntryQueue().size();
		}
	}
}
