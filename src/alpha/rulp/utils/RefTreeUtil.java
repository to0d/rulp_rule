package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.utils.AOTreeUtil.DLRVisitNode;
import alpha.rulp.utils.AOTreeUtil.IAOTreeNode;
import alpha.rulp.ximpl.entry.IRReference;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.node.RReteType;

public class RefTreeUtil {

	static class ProveEntry {

		public List<IRReteEntry> factEntryList = null;

		public Boolean isInvalid = null;

		public IRReteNode node = null;

		public boolean isInvalid() {

			if (isInvalid == null) {

				isInvalid = true;
				if (factEntryList != null) {
					for (IRReteEntry fact : factEntryList) {
						if (fact.isDroped()) {
							isInvalid = false;
						}
					}
				}
			}

			return isInvalid;
		}
	}

	static class ProveNode {

		public boolean hasMore = true;

		public boolean isDefinedStmt = false;

		public boolean isVisitCompleted = false;

		public final IRReteEntry stmtEntry;

		public DLRVisitNode<IRReteEntry> vistTree = null;

		public ProveNode(IRReteEntry stmtEntry) {
			super();
			this.stmtEntry = stmtEntry;
		}

		public ProveEntry nextProveEntry() throws RException {

			if (!hasMore) {
				return null;
			}

			if (vistTree == null) {

				IAOTreeNode<IRReteEntry> aoTree = new XEntryAOTreeNode(stmtEntry, true);
				vistTree = AOTreeUtil.getDLRVisitFirstTree(aoTree);
				return _toProveEntry(vistTree);
			}

			if (!AOTreeUtil.update(vistTree)) {
				hasMore = false;
				return null;
			}

			return _toProveEntry(vistTree);
		}

	}

	static class XEntryAOTreeNode implements IAOTreeNode<IRReteEntry> {

		private ArrayList<IAOTreeNode<IRReteEntry>> childs = null;

		private IRReteEntry entry;

		private boolean root;

		public XEntryAOTreeNode(IRReteEntry entry, boolean root) {
			super();
			this.entry = entry;
			this.root = root;
		}

		@Override
		public IAOTreeNode<IRReteEntry> getChild(int index) throws RException {
			return getChilds().get(index);
		}

		@Override
		public int getChildCount() throws RException {
			return getChilds().size();
		}

		public ArrayList<IAOTreeNode<IRReteEntry>> getChilds() throws RException {

			if (childs == null) {

				childs = new ArrayList<>();

				if (entry.getReferenceCount() > 0) {

					Iterator<? extends IRReference> it = entry.getReferenceIterator();
					while (it.hasNext()) {
						childs.add(new XRefAOTreeNode(it.next()));
					}
				}

			}

			return childs;
		}

		@Override
		public IRReteEntry getObj() {
			return entry;
		}

		@Override
		public boolean isAnd() throws RException {
			return false;
		}

		@Override
		public boolean isLeaf() throws RException {
			return !root && entry.isStmt();
		}

	}

	static class XRefAOTreeNode implements IAOTreeNode<IRReteEntry> {

		private ArrayList<IAOTreeNode<IRReteEntry>> childs = null;

		private IRReference ref;

		public XRefAOTreeNode(IRReference ref) {
			super();
			this.ref = ref;
		}

		@Override
		public IAOTreeNode<IRReteEntry> getChild(int index) throws RException {
			return getChilds().get(index);
		}

		@Override
		public int getChildCount() throws RException {
			return getChilds().size();
		}

		public ArrayList<IAOTreeNode<IRReteEntry>> getChilds() throws RException {

			if (childs == null) {

				childs = new ArrayList<>();

				int parentCount = ref.getParentEntryCount();
				for (int i = 0; i < parentCount; ++i) {
					childs.add(new XEntryAOTreeNode(ref.getParentEntry(i), false));
				}

			}

			return childs;
		}

		@Override
		public IRReteEntry getObj() {
			return null;
		}

		@Override
		public boolean isAnd() throws RException {
			return true;
		}

		@Override
		public boolean isLeaf() throws RException {
			return false;
		}

	}

	private static ProveEntry _toProveEntry(DLRVisitNode<IRReteEntry> vistTree) throws RException {

		List<IRReteEntry> vistEntries = AOTreeUtil.visit(vistTree);
		Collections.sort(vistEntries, (o1, o2) -> {
			return o1.toString().compareTo(o2.toString());
		});

		ProveEntry proveEntry = new ProveEntry();
		proveEntry.factEntryList = vistEntries;

		XRefAOTreeNode refChild = (XRefAOTreeNode) vistTree.aoNode.getChild(0);
		proveEntry.node = refChild.ref.getNode();

		return proveEntry;
	}

	private int maxDeep = -1;

	private int maxWidth = -1;

	private IRModel model;

	private Map<String, ProveNode> proveMap = new HashMap<>();

	private Set<String> visitStmtSet = new HashSet<>();

	public RefTreeUtil(IRModel model) {
		super();
		this.model = model;
	}

	public RefTreeUtil(IRModel model, int maxWidth, int maxDeep) {
		super();
		this.model = model;
		this.maxWidth = maxWidth;
		this.maxDeep = maxDeep;
	}

	private IRList _build(IRReteEntry entry, int deep) throws RException {

		String uniqName = ReteUtil.uniqName(entry);
		if (visitStmtSet.contains(uniqName)) {
			return RulpFactory.createList(entry);
		}

		visitStmtSet.add(uniqName);

		ProveNode proveNode = _getProveNode(entry);
		if (proveNode.isDefinedStmt || deep == maxDeep) {
			return RulpFactory.createList(entry, RReteStatus.toObject(entry.getStatus()));
		}

		ArrayList<IRObject> proveList = new ArrayList<>();
		proveList.add(entry);

		ProveEntry proveEntry = null;

		int width = 0;

		while ((maxWidth == -1 || width < maxWidth) && (proveEntry = proveNode.nextProveEntry()) != null) {
			proveList.add(_build(proveEntry, deep));
			width++;
		}

		return RulpFactory.createList(proveList);
	}

	private IRList _build(ProveEntry proveEntry, int deep) throws RException {

		ArrayList<IRObject> proveList = new ArrayList<>();

		proveList.add(proveEntry.node);

		for (IRReteEntry factEntry : proveEntry.factEntryList) {
			proveList.add(_build(factEntry, deep + 1));
		}

		return RulpFactory.createExpression(proveList);
	}

	private ProveNode _getProveNode(IRReteEntry entry) throws RException {

		String uniqName = entry.toString();

		ProveNode proveNode = proveMap.get(uniqName);
		if (proveNode == null) {

			proveNode = new ProveNode(entry);

			if (!entry.isStmt()) {
				throw new RException("not stmt entry: " + entry);
			}

			if (entry.getReferenceCount() <= 0) {
				throw new RException("no ref found: " + entry);
			}

			if (entry.getStatus() == RReteStatus.DEFINE || entry.getStatus() == RReteStatus.FIXED_) {

				Iterator<? extends IRReference> it = entry.getReferenceIterator();
				while (it.hasNext()) {

					IRReference ref = it.next();
					if (RReteType.isRootType(ref.getNode().getReteType())) {
						proveNode.isDefinedStmt = true;
						proveNode.isVisitCompleted = true;
					}
				}

			} else if (entry.getStatus() == RReteStatus.REASON) {
				// we should find rule node
			} else {
				throw new RException("not support status: " + entry.getStatus() + ", for entry: " + entry);
			}

			proveMap.put(uniqName, proveNode);
		}

		return proveNode;
	}

	private IRList _buildReteEntryRefTree(IRReteEntry entry) throws RException {

		ArrayList<IRObject> refTree = new ArrayList<>();

		IAOTreeNode<IRReteEntry> aoTree = new XEntryAOTreeNode(entry, false);
		DLRVisitNode<IRReteEntry> vistTree = AOTreeUtil.getDLRVisitFirstTree(aoTree);
		ProveEntry provEntry = _toProveEntry(vistTree);
		refTree.add(RulpFactory.createList(provEntry.factEntryList));

		while (AOTreeUtil.update(vistTree)) {
			provEntry = _toProveEntry(vistTree);
			refTree.add(RulpFactory.createList(provEntry.factEntryList));
		}

		return RulpFactory.createList(refTree);
	}

	public static IRList buildReteEntryRefTree(IRModel model, IRReteEntry entry) throws RException {
		return new RefTreeUtil(model)._buildReteEntryRefTree(entry);
	}

	public static IRList buildStmtRefTree(IRList stmt, IRModel model, int maxWidth, int maxDeep) throws RException {
		return new RefTreeUtil(model, maxWidth, maxDeep)._buildStmtRefTree(stmt);
	}

	private IRList _buildStmtRefTree(IRList stmt) throws RException {

		/****************************************************/
		// Find root node
		/****************************************************/
		IRReteNode rootNode = model.getNodeGraph().findRootNode(stmt.getNamedName(), stmt.size());
		if (rootNode == null) {
			throw new RException("root node not found: " + stmt);
		}

		/****************************************************/
		// Find entry
		/****************************************************/
		IRReteEntry entry = ReteUtil.getStmt(rootNode, stmt);
		if (entry == null) {
			return RulpFactory.createList();
		}

		return _build(entry, 0);
	}

}
