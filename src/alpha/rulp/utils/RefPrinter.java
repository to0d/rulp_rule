package alpha.rulp.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReference;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.node.RReteType;

public class RefPrinter {

	private IREntryTable entryTable;

	private IRModel model;

	private Set<Integer> visitEntryIds = new HashSet<>();

	public RefPrinter(IRModel model) {
		super();
		this.model = model;
		this.entryTable = model.getEntryTable();
	}

	public String head(int lvl) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lvl; ++i) {
			sb.append("    ");
		}

		return sb.toString();
	}

	public boolean isStmt(IRModel model, IRReteEntry entry) {

		Iterator<? extends IRReference> refIt = entry.getReferenceIterator();
		while (refIt.hasNext()) {
			IRReference ref = refIt.next();
			if (RReteType.isRootType(ref.getNode().getReteType())) {
				return true;
			}
		}

		return false;
	}

	public void printEntry(StringBuilder sb, int entryId, int lvl) throws RException {

		if (visitEntryIds.contains(entryId)) {
			return;
		}

		IRReteEntry entry = entryTable.getEntry(entryId);
		if (entry == null) {
			sb.append(head(lvl) + String.format("==> Entry<%d> not found", entryId));
			return;
		}

		String uniqName = RulpUtil.toString(entry);
		sb.append(head(lvl)
				+ String.format("%-5s: name=%s, id=%d, stats=%s, ref=%d\n", isStmt(model, entry) ? "Stmt" : "Entry",
						uniqName, entry.getEntryId(), entry.getStatus(), entry.getReferenceCount()));
		if (entry.isDroped() || entry.isDeleted()) {
			return;
		}

		Iterator<? extends IRReference> refIt = entry.getReferenceIterator();
		while (refIt.hasNext()) {

			IRReference ref = refIt.next();
			int pc = ref.getParentEntryCount();

			String line = head(lvl + 1)
					+ String.format("==> Ref: node=%s, parent=(%s)", ref.getNode().getNodeName(), pc);

			for (int i = 0; i < pc; ++i) {
				line += " " + ref.getParentEntry(i).getEntryId() + ",";
			}

			sb.append(line + "\n");
		}
	}

	public String printStmt(IRList stmt) throws RException {

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

		IRReteEntry entry = rootNode.getEntryQueue().getStmt(uniqName);
		if (entry == null) {
			return String.format("==> %s: entry not found\n", uniqName);
		}

		StringBuilder sb = new StringBuilder();
		printEntry(sb, entry.getEntryId(), 0);

		return sb.toString();
	}
}
