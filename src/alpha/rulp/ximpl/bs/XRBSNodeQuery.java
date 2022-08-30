package alpha.rulp.ximpl.bs;

import java.util.HashSet;
import java.util.Set;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IREntryAction;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBSNodeQuery extends AbsBSNode implements IREntryAction {

	protected boolean queryBackward = false;

	protected boolean queryForward = false;

	protected Set<String> queryResultEntrySet = new HashSet<>();

	protected IRList queryReteNodeTree;

	public XRBSNodeQuery(XRBackSearcher bs, int nodeId, String nodeName) {
		super(bs, nodeId, nodeName);
	}

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
	public BSType getType() {
		return BSType.QUERY;
	}

	public boolean hasMore() throws RException {

		if (queryBackward) {
			return false;
		}

		int oldSize = queryResultEntrySet.size();
		bs.model.query(this, queryReteNodeTree, -1, true);
		queryBackward = true;

		return queryResultEntrySet.size() > oldSize;
	}

	@Override
	public void init() throws RException {

//		if (trace) {
//			_outln(this, String.format("init, stmt-list=%s", queryStmtList));
//		}

//		queryReteNodeTree = RulpFactory.createList(queryStmtList);
		this.status = BSStats.PROCESS;
	}

	@Override
	public boolean isSucc() {
		return queryResultEntrySet.size() > 0;
	}

	@Override
	public void process(IRBSNode lastNode) throws RException {

		if (!queryForward) {

			// query forward
			bs.model.query(this, queryReteNodeTree, -1, false);
			queryForward = true;
		}

		if (!queryBackward && !isSucc()) {

			// query forward
			bs.model.query(this, queryReteNodeTree, -1, true);
			queryBackward = true;
		}

		this.status = BSStats.COMPLETE;
	}

	public String toString() {
		return String.format("tree=%s, type=%s, status=%s", "" + queryReteNodeTree, "" + this.getType(),
				"" + this.status);
	}
}