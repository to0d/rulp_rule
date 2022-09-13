package alpha.rulp.ximpl.bs;

import static alpha.rulp.rule.Constant.O_QUERY_STMT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IREntryAction;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RefTreeUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBSNodeStmtQuery extends AbsBSNode implements IREntryAction {

	protected boolean queryBackward = false;

	protected boolean queryForward = false;

	protected Set<String> queryResultEntrySet = new HashSet<>();

	protected List<IRReteEntry> queryResultEntryList = new ArrayList<>();

	protected IRList queryReteNodeTree;

	@Override
	public boolean addEntry(IRReteEntry entry) throws RException {

		if (entry != null && !entry.isDroped()) {
			String uniqName = ReteUtil.uniqName(entry);
			if (!queryResultEntrySet.contains(uniqName)) {
				queryResultEntrySet.add(uniqName);
				queryResultEntryList.add(entry);
			}
		}

		return true;
	}

	public List<IRList> rebuildProveStmtList(IRReteEntry entry) throws RException {

		List<IRList> stmtList = new ArrayList<>();

		IRIterator<? extends IRObject> it = RefTreeUtil.buildReteEntryRefTree(this.engine.getModel(), entry).iterator();
		while (it.hasNext()) {
			stmtList.add(RulpUtil.asList(it.next()));
		}

		return stmtList;
	}

	public IRList buildResultTree(boolean explain) throws RException {

		if (!parentNode.isSucc()) {

			ArrayList<IRObject> list = new ArrayList<>();
			list.add(O_QUERY_STMT);
			RulpUtil.addAll(list, queryReteNodeTree);

			return RulpFactory.createExpression(list);
		}

		XRBSNodeStmtAnd parentAnd = (XRBSNodeStmtAnd) this.parentNode;
		ArrayList<String> brotherUniqList = new ArrayList<>();

		int parentChild = parentAnd.getChildCount();
		for (int i = 0; i < parentChild; ++i) {
			IRBSNode child = parentAnd.getChild(i);
			if (child != this) {
				brotherUniqList.add(ReteUtil.uniqName(((IRBSNodeStmt) child).getStmt()));
			}
		}

		int brotherCount = brotherUniqList.size();

		ArrayList<IRList> queryStmtList = new ArrayList<>();
		for (IRObject subStmt : RulpUtil.toArray(queryReteNodeTree)) {
			if (ReteUtil.isReteStmt(subStmt)) {
				queryStmtList.add((IRList) subStmt);
			}
		}

		int thisQueryCount = queryStmtList.size();
		int expectRefCount = brotherCount + thisQueryCount;
		IRList parentStmt = parentAnd.getStmt();

		ArrayList<String> queryStmtListUniqNames = new ArrayList<>();

		IRList refTree = RefTreeUtil.buildStmtRefTree(parentStmt, this.engine.getModel(), -1, 1);
		IRIterator<? extends IRObject> it = refTree.listIterator(1);

		NEXT_PROVE: while (it.hasNext()) {

			IRObject obj = it.next();

			if (obj.getType() == RType.EXPR) {

				IRExpr expr = RulpUtil.asExpression(obj);

				// check rule node
				if (parentAnd.getSourceNode().rule != expr.get(0)) {
					continue NEXT_PROVE;
				}

				// check stmt count
				if ((expectRefCount + 1) != expr.size()) {
					continue NEXT_PROVE;
				}

				ArrayList<IRList> proveList = new ArrayList<>();
				ArrayList<String> proveUniqList = new ArrayList<>();

				IRIterator<? extends IRObject> it2 = expr.listIterator(1);
				while (it2.hasNext()) {

					IRList list1 = RulpUtil.asList(it2.next());
					IRList proveStmt = RulpUtil.asList(list1.get(0));

					proveList.add(list1);
					proveUniqList.add(ReteUtil.uniqName(proveStmt));
				}

				for (String uniqBrotherName : brotherUniqList) {

					int pos = proveUniqList.indexOf(uniqBrotherName);

					// not found
					if (pos == -1) {
						continue NEXT_PROVE;
					}

					proveUniqList.set(pos, null);
					proveList.set(pos, null);
				}

				ArrayList<String> leftProveList = new ArrayList<>();
				for (IRList list : proveList) {
					if (list != null) {
						leftProveList.add(ReteUtil.uniqName(RulpUtil.asList(list.get(0))));
					}
				}

				if (thisQueryCount != leftProveList.size()) {
					continue NEXT_PROVE;
				}

				Collections.sort(leftProveList);
				String expectList = "" + leftProveList;

				for (int i = 0; i < queryResultEntryList.size(); ++i) {

					if (i >= queryStmtListUniqNames.size()) {

						IRList list = rebuildProveStmtList(queryResultEntryList.get(i)).get(0);
						List<String> listNameList = new ArrayList<>();
						for (IRObject obj2 : RulpUtil.toArray(list)) {
							listNameList.add(ReteUtil.uniqName(RulpUtil.asList(obj2)));
						}

						Collections.sort(leftProveList);
						queryStmtListUniqNames.add("" + leftProveList);

					}

					String uniqName = queryStmtListUniqNames.get(i);

					// found match stmt list
					if (expectList.equals(uniqName)) {

						ArrayList<IRObject> rtList = new ArrayList<>();
						rtList.add(O_QUERY_STMT);

						for (IRList list : proveList) {
							if (list != null) {
								rtList.add(RulpUtil.asList(list.get(0)));
							}
						}

						return RulpFactory.createExpression(rtList);
					}
				}
			}
		}

		ArrayList<IRObject> list = new ArrayList<>();
		list.add(O_QUERY_STMT);
		RulpUtil.addAll(list, queryReteNodeTree);

		return RulpFactory.createExpression(list);
	}

//	public List<IRList> buildResultTree(XRBSNodeStmtAnd andParent, boolean explain) throws RException {
//		return queryReteNodeTree;
//	}

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