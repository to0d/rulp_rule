package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.RReteStatus.DEFINE;

import java.io.IOException;
import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.cache.IRBufferWorker;
import alpha.rulp.ximpl.cache.IRStmtLoader;
import alpha.rulp.ximpl.cache.IRStmtSaver;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.XRModel.RUpdateResult;

public class XRBufferWorker implements IRBufferWorker {

	private int bufferLastEntryId = 0;

	private int bufferStmtCount = 0;

	private int loadCount = 0;

	private IRStmtLoader loader;

	protected XRModel model;

	private IRReteNode node;

	private int saveCount = 0;

	private IRStmtSaver saver;

	private CacheStatus status = CacheStatus.UNLOAD;

	private int writeLines = 0;

	public XRBufferWorker(XRModel model, IRReteNode node) {
		super();
		this.model = model;
		this.node = node;
	}

	@Override
	public void cleanBuffer() throws RException {

		if (this.isDirty()) {
			throw new RException("Can't clean dirty buffer:" + this.getNode());
		}

		this.bufferLastEntryId = 0;
		this.bufferStmtCount = 0;
		this.status = CacheStatus.CLEAN;
	}

	@Override
	public int getCacheLastEntryId() {
		return bufferLastEntryId;
	}

	@Override
	public int getLoadCount() {
		return loadCount;
	}

	public IRStmtLoader getLoader() {
		return loader;
	}

	@Override
	public IRReteNode getNode() {
		return node;
	}

	@Override
	public int getReadCount() {
		return loader.getReadLines();
	}

	@Override
	public int getSaveCount() {
		return saveCount;
	}

	public IRStmtSaver getSaver() {
		return saver;
	}

	@Override
	public CacheStatus getStatus() {
		return status;
	}

	@Override
	public int getStmtCount() {
		return bufferStmtCount;
	}

	@Override
	public int getWriteCount() {
		return writeLines;
	}

	public boolean isDirty() throws RException {

		if (saver == null) {
			return false;
		}

		switch (status) {
		case LOADED:

			if (saver.needSave()) {
				return true;
			}

			if (node.getEntryQueue().size() != bufferStmtCount) {
				return true;
			}

			IRReteEntry lastEntry = ReteUtil.getLastEntry(node.getEntryQueue());
			int lastEntryId = lastEntry == null ? -1 : lastEntry.getEntryId();
			if (bufferLastEntryId != lastEntryId) {
				return true;
			}

			return false;

		case LOADING:
			return true;

		case UNLOAD:
		case CLEAN:
			return node.getEntryQueue().size() > 0;

		default:
			throw new RException("invalid status: " + status);
		}

	}

	public int load() throws RException {

		if (loader == null) {
			return 0;
		}

		if (status == CacheStatus.LOADING) {
			throw new RException("invalid status: " + status);
		}

		this.status = CacheStatus.LOADING;

		model._fireLoadNodeAction(node);

		IREntryQueue entryQueue = node.getEntryQueue();

		boolean pushEmptyNode = (entryQueue.size() == 0);
		int oldCacheStmtCount = this.bufferStmtCount;

		try {

			loader.load((stmt) -> {

				if (!ReteUtil.isValidNodeStmt(node, stmt)) {
					throw new RException(String.format("Invalid stmt for node<%s>: %s", "" + node, "" + stmt));
				}

				if (RUpdateResult.isValidUpdate(model._addStmt(node, stmt, DEFINE))) {
					model.cacheUpdateCount++;
					this.bufferStmtCount++;
				}
			});

		} catch (IOException e) {

			if (RuleUtil.isModelTrace()) {
				e.printStackTrace();
			}

			throw new RException(e.toString());
		}

		if (pushEmptyNode && (oldCacheStmtCount != this.bufferStmtCount)) {
			bufferLastEntryId = entryQueue.getEntryAt(entryQueue.size() - 1).getEntryId();
		} else {
			bufferLastEntryId = -1;
		}

		this.loadCount++;
		this.status = CacheStatus.LOADED;

		return bufferStmtCount;
	}

	public int save() throws RException, IOException {

		if (status == CacheStatus.LOADING) {
			throw new RException("invalid status: " + status);
		}

		IREntryQueue queue = node.getEntryQueue();
		List<IRReteEntry> entries = ReteUtil.getAllEntries(queue);
		IRReteEntry lastEntry = ReteUtil.getLastEntry(node.getEntryQueue());
		int lastEntryId = lastEntry == null ? -1 : lastEntry.getEntryId();

		model._fireSaveNodeAction(node);

		int saveLineCount = saver.save(entries);
		this.bufferLastEntryId = lastEntryId;
		this.bufferStmtCount = queue.size();
		this.saveCount++;
		this.writeLines += saveLineCount;
		this.status = CacheStatus.LOADED;

		return saveLineCount;
	}

	public void setLoader(IRStmtLoader loader) {
		this.loader = loader;
	}

	public void setSaver(IRStmtSaver saver) {
		this.saver = saver;
	}
}
