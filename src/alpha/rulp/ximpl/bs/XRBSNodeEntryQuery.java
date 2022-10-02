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
import alpha.rulp.utils.RefTreeUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBSNodeEntryQuery extends AbsBSNode implements IRBSNodeQuery {

//	protected List<IRReteEntry> queryResultEntryList = new ArrayList<>();

	protected IRList queryReteNodeTree;

	protected int size = 0;

	protected int entryLen = 0;

	protected IRIterator<IRReteEntry> stmtIterator;

	protected ArrayList<String> varList = new ArrayList<>();

	protected Map<String, IRObject> varReplaceMap = new HashMap<>();

	protected List<IRList> queryStmtList;

	public XRBSNodeEntryQuery(List<IRList> queryStmtList) throws RException {
		super();
		this.queryStmtList = queryStmtList;
		this.queryReteNodeTree = RulpFactory.createList(queryStmtList);
	}

	protected List<IRList> _rebuildProveStmtList(IRReteEntry entry) throws RException {

		List<IRList> stmtList = new ArrayList<>();

		IRIterator<? extends IRObject> it = RefTreeUtil.buildReteEntryRefTree(this.engine.getModel(), entry).iterator();
		while (it.hasNext()) {
			stmtList.add(RulpUtil.asList(it.next()));
		}

		return stmtList;
	}

//	public IRList buildResultTree(boolean explain) throws RException {
//
//		if (!parentNode.isSucc()) {
//
//			ArrayList<IRObject> list = new ArrayList<>();
//			list.add(O_QUERY_STMT);
//			RulpUtil.addAll(list, queryReteNodeTree);
//
//			return RulpFactory.createExpression(list);
//		}
//
//		XRBSNodeStmtAnd parentAnd = (XRBSNodeStmtAnd) this.parentNode;
//		ArrayList<String> brotherUniqList = new ArrayList<>();
//
//		int parentChild = parentAnd.getChildCount();
//		for (int i = 0; i < parentChild; ++i) {
//			IRBSNode child = parentAnd.getChild(i);
//			if (child != this) {
//				brotherUniqList.add(ReteUtil.uniqName(((IRBSNodeStmt) child).getStmt()));
//			}
//		}
//
//		int brotherCount = brotherUniqList.size();
//
//		ArrayList<IRList> queryStmtList = new ArrayList<>();
//		for (IRObject subStmt : RulpUtil.toArray(queryReteNodeTree)) {
//			if (ReteUtil.isReteStmt(subStmt)) {
//				queryStmtList.add((IRList) subStmt);
//			}
//		}
//
//		int thisQueryCount = queryStmtList.size();
//		int expectRefCount = brotherCount + thisQueryCount;
//		IRList parentStmt = parentAnd.getStmt();
//
//		ArrayList<String> queryStmtListUniqNames = new ArrayList<>();
//
//		IRList refTree = RefTreeUtil.buildStmtRefTree(parentStmt, this.engine.getModel(), -1, 1);
//		IRIterator<? extends IRObject> it = refTree.listIterator(1);
//
//		NEXT_PROVE: while (it.hasNext()) {
//
//			IRObject obj = it.next();
//
//			if (obj.getType() == RType.EXPR) {
//
//				IRExpr expr = RulpUtil.asExpression(obj);
//
//				// check rule node
//				if (parentAnd.getSourceNode().rule != expr.get(0)) {
//					continue NEXT_PROVE;
//				}
//
//				// check stmt count
//				if ((expectRefCount + 1) != expr.size()) {
//					continue NEXT_PROVE;
//				}
//
//				ArrayList<IRList> proveList = new ArrayList<>();
//				ArrayList<String> proveUniqList = new ArrayList<>();
//
//				IRIterator<? extends IRObject> it2 = expr.listIterator(1);
//				while (it2.hasNext()) {
//
//					IRList list1 = RulpUtil.asList(it2.next());
//					IRList proveStmt = RulpUtil.asList(list1.get(0));
//
//					proveList.add(list1);
//					proveUniqList.add(ReteUtil.uniqName(proveStmt));
//				}
//
//				for (String uniqBrotherName : brotherUniqList) {
//
//					int pos = proveUniqList.indexOf(uniqBrotherName);
//
//					// not found
//					if (pos == -1) {
//						continue NEXT_PROVE;
//					}
//
//					proveUniqList.set(pos, null);
//					proveList.set(pos, null);
//				}
//
//				ArrayList<String> leftProveList = new ArrayList<>();
//				for (IRList list : proveList) {
//					if (list != null) {
//						leftProveList.add(ReteUtil.uniqName(RulpUtil.asList(list.get(0))));
//					}
//				}
//
//				if (thisQueryCount != leftProveList.size()) {
//					continue NEXT_PROVE;
//				}
//
//				Collections.sort(leftProveList);
//				String expectList = "" + leftProveList;
//
//				for (int i = 0; i < queryResultEntryList.size(); ++i) {
//
//					if (i >= queryStmtListUniqNames.size()) {
//
//						IRList list = _rebuildProveStmtList(queryResultEntryList.get(i)).get(0);
//						List<String> listNameList = new ArrayList<>();
//						for (IRObject obj2 : RulpUtil.toArray(list)) {
//							listNameList.add(ReteUtil.uniqName(RulpUtil.asList(obj2)));
//						}
//
//						Collections.sort(leftProveList);
//						queryStmtListUniqNames.add("" + leftProveList);
//
//					}
//
//					String uniqName = queryStmtListUniqNames.get(i);
//
//					// found match stmt list
//					if (expectList.equals(uniqName)) {
//
//						ArrayList<IRObject> rtList = new ArrayList<>();
//						rtList.add(O_QUERY_STMT);
//
//						for (IRList list : proveList) {
//							if (list != null) {
//								rtList.add(RulpUtil.asList(list.get(0)));
//							}
//						}
//
//						return RulpFactory.createExpression(rtList);
//					}
//				}
//			}
//		}
//
//		ArrayList<IRObject> list = new ArrayList<>();
//		list.add(O_QUERY_STMT);
//		RulpUtil.addAll(list, queryReteNodeTree);
//
//		return RulpFactory.createExpression(list);
//	}

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

////		if (stmtIterator.hasNext()) {
////			queryResultEntryList.add(stmtIterator.next());
////		} else {
////			this.status = BSStats.COMPLETE;
////		}
////		
//		throw new RException("invalid operation");

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

	@Override
	public IRList buildResultTree(boolean explain) throws RException {
		throw new RException("invalid operation");
	}
}