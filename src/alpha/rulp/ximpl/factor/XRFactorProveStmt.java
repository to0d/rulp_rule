package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.AndOrTreeUtil;
import alpha.rulp.utils.AndOrTreeUtil.DLRVisitNode;
import alpha.rulp.utils.AndOrTreeUtil.IAOTreeNode;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReference;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.RReteType;

public class XRFactorProveStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	static class StmtProveUtil {

		static class ProveEntry {

			public String _str = null;

			public List<IRReteEntry> factEntryList = null;

			public Boolean isInvalid = null;

			public IRReteNode node = null;

			public String getRefPathString() {

				if (_str == null) {

					_str = "";

					if (node != null) {
						_str += node.getNodeName() + ":";
					}

					if (factEntryList != null) {
						for (IRReteEntry fact : factEntryList) {
							_str += " " + fact;
						}
					}
				}

				return _str;
			}

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

			public static ProveEntry toProveEntry(DLRVisitNode<IRReteEntry> vistTree) throws RException {

				List<IRReteEntry> vistEntries = AndOrTreeUtil.visit(vistTree);
				Collections.sort(vistEntries, (o1, o2) -> {
					return o1.toString().compareTo(o2.toString());
				});

				ProveEntry proveEntry = new ProveEntry();
				proveEntry.factEntryList = vistEntries;

				XRefAOTreeNode refChild = (XRefAOTreeNode) vistTree.aoNode.getChild(0);
				proveEntry.node = refChild.ref.getNode();

				return proveEntry;
			}

			public boolean isDefinedStmt = false;

			public boolean isVisitCompleted = false;

			public List<ProveEntry> proveEntryList = null;

			public final IRReteEntry stmtEntry;

			public DLRVisitNode<IRReteEntry> vistTree = null;

			public ProveNode(IRReteEntry stmtEntry) {
				super();
				this.stmtEntry = stmtEntry;
			}

			public ProveEntry getFirstProve() throws RException {

				if (proveEntryList == null) {

					proveEntryList = new ArrayList<>();

					IAOTreeNode<IRReteEntry> aoTree = new XEntryAOTreeNode(stmtEntry, true);
					vistTree = AndOrTreeUtil.getDLRVisitFirstTree(aoTree);

					proveEntryList.add(toProveEntry(vistTree));
				}

				return proveEntryList.get(0);
			}

			public int getProveEntryCount() {
				return proveEntryList == null ? 0 : proveEntryList.size();
			}
		}

		private IRModel model;

		private Map<String, ProveNode> proveMap = new HashMap<>();

		public StmtProveUtil(IRModel model) {
			super();
			this.model = model;
		}

		public ProveNode getProveNode(IRReteEntry entry) throws RException {

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

		public String proveStmt(IRList stmt) throws RException {

			/****************************************************/
			// Find root node
			/****************************************************/
			IRReteNode rootNode = null;
			if (stmt.getNamedName() == null) {
				rootNode = model.getNodeGraph().findRootNode(stmt.size());
			} else {
				rootNode = model.getNodeGraph().findNamedNode(stmt.getNamedName());
				if (rootNode != null && rootNode.getEntryLength() != stmt.size()) {
					rootNode = null;
				}
			}

			String uniqName = RulpUtil.toString(stmt);
			if (rootNode == null) {
				return String.format("==> %s: node not found\n", uniqName);
			}

			StringBuilder sb = new StringBuilder();
			Queue<IRReteEntry> needProveStmt = new LinkedList<>();
			Set<String> provedStmt = new HashSet<>();

			{
				IRReteEntry entry = rootNode.getEntryQueue().getStmt(uniqName);
				if (entry == null) {
					return String.format("%s: not found\n", uniqName);
				}

				needProveStmt.add(entry);
			}

			while (!needProveStmt.isEmpty()) {

				IRReteEntry entry = needProveStmt.remove();
				if (provedStmt.contains(entry.toString())) {
					continue;
				}

				provedStmt.add(entry.toString());

				ProveNode proveNode = getProveNode(entry);
				if (proveNode.isDefinedStmt) {
					sb.append(String.format("%s : %s\n", entry.toString(), entry.getStatus()));

				} else {

					ProveEntry proveEntry = proveNode.getFirstProve();

					sb.append(String.format("%s : %s", entry.toString(), proveEntry.node.getNodeName()));
					for (IRReteEntry factEntry : proveEntry.factEntryList) {
						needProveStmt.add(factEntry);
						sb.append(" " + factEntry.toString());
					}
					sb.append("\n");
				}

			}

//			printEntry(sb, entry.getEntryId(), 0);

			return sb.toString();
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

	public XRFactorProveStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() < 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		IRList stmt = RulpUtil.asList(interpreter.compute(frame, args.get(2)));
		interpreter.getOut().out(new StmtProveUtil(model).proveStmt(stmt));

		return O_Nil;
	}
}
