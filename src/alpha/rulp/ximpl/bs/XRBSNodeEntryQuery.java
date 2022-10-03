package alpha.rulp.ximpl.bs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBSNodeEntryQuery extends AbsBSNode implements IRBSNodeQuery {

	protected int entryLen = 0;

	protected IRList queryReteNodeTree;

	protected List<IRList> queryStmtList;

	protected int size = 0;

	protected IRIterator<IRReteEntry> stmtIterator;

	protected ArrayList<String> varList = new ArrayList<>();

	protected Map<String, IRObject> varReplaceMap = new HashMap<>();

	public XRBSNodeEntryQuery(List<IRList> queryStmtList) throws RException {
		super();
		this.queryStmtList = queryStmtList;
		this.queryReteNodeTree = RulpFactory.createList(queryStmtList);
	}

	@Override
	public IRList buildResultTree(boolean explain) throws RException {
		throw new RException("invalid operation");
	}

	@Override
	public void complete() throws RException {

	}

	public String getStatusString() throws RException {
		return String.format("size=%d", size);
	}

	@Override
	public BSNodeType getType() {
		return BSNodeType.ENTRY_QUERY;
	}

	public boolean hasMore() throws RException {
		return stmtIterator.hasNext();
	}

	@Override
	public void init() throws RException {

		for (IRObject var : ReteUtil.buildVarList(queryReteNodeTree)) {
			this.varList.add(var.asString());
		}

		this.entryLen = this.varList.size();
		this.status = BSStats.PROCESS;
	}

	@Override
	public boolean isSucc() {
		return this.stmtIterator != null;
	}

	@Override
	public boolean needComplete() {
		return false;
	}

	@Override
	public List<IRList> next() throws RException {

		IRReteEntry entry = stmtIterator.next();
		if (entry == null) {
			return Collections.emptyList();
		}

		++size;

		for (int i = 0; i < this.entryLen; ++i) {
			varReplaceMap.put(varList.get(i), entry.get(i));
		}

		ArrayList<IRList> newStmtList = new ArrayList<>();

		for (IRList stmt : queryStmtList) {
			newStmtList.add((IRList) RuntimeUtil.rebuild(stmt, varReplaceMap));
		}

		return newStmtList;
	}

	public void process(IRBSNode lastNode) throws RException {

		this.stmtIterator = engine.getModel().query(queryReteNodeTree, -1, true);
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