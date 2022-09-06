package alpha.rulp.ximpl.bs;

import java.util.HashSet;
import java.util.Set;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IREntryAction;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBSNodeStmtQuery extends AbsBSNode implements IREntryAction {

	protected boolean queryBackward = false;

	protected boolean queryForward = false;

	protected Set<String> queryResultEntrySet = new HashSet<>();

	protected IRList queryReteNodeTree;

	@Override
	public boolean addEntry(IRReteEntry entry) throws RException {

		if (entry != null && !entry.isDroped()) {
			queryResultEntrySet.add(ReteUtil.uniqName(entry));
		}

		return true;
	}

	public IRList buildResultTree(boolean explain) throws RException {
		return queryReteNodeTree;
	}

	@Override
	public void complete() throws RException {

	}

	public String getStatusString() {
		return String.format("forward=%s, backward=%s, query=%d", queryForward, queryBackward,
				queryResultEntrySet.size());
	}

	@Override
	public BSNodeType getType() {
		return BSNodeType.STMT_QUERY;
	}

	public boolean hasMore() throws RException {

		if (queryBackward) {
			return false;
		}

		int oldSize = queryResultEntrySet.size();
		engine.getModel().query(this, queryReteNodeTree, -1, true);
		queryBackward = true;

		return queryResultEntrySet.size() > oldSize;
	}

	@Override
	public void init() throws RException {
		this.status = BSStats.PROCESS;
	}

	@Override
	public boolean isSucc() {
		return queryResultEntrySet.size() > 0;
	}

	@Override
	public boolean needComplete() {
		return false;
	}

	public void process(IRBSNode lastNode) throws RException {

		if (!queryForward) {

			// query forward
			engine.getModel().query(this, queryReteNodeTree, -1, false);
			queryForward = true;
		}

		if (!queryBackward && !isSucc()) {

			// query forward
			engine.getModel().query(this, queryReteNodeTree, -1, true);
			queryBackward = true;
		}

		this.status = BSStats.COMPLETE;
	}

	@Override
	public void setSucc(boolean succ) throws RException {
		throw new RException("invalid operation");
	}

	public String toString() {
		return String.format("tree=%s, type=%s, status=%s", "" + queryReteNodeTree, "" + this.getType(),
				"" + this.status);
	}
}