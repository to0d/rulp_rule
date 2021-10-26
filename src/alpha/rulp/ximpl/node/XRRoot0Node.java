package alpha.rulp.ximpl.node;

import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel.RNodeContext;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRRoot0Node extends XRReteNode0 implements IRRootNode {

	protected IREntryTable entryTable;

	protected Map<String, IRReteEntry> stmtMap = new HashMap<>();

	public void addReference(IRReteEntry entry) throws RException {
		entryTable.addReference(entry, this);
	}

	@Override
	public int doGC() {

		Iterator<Entry<String, IRReteEntry>> it = stmtMap.entrySet().iterator();
		while (it.hasNext()) {

			Entry<String, IRReteEntry> e = it.next();

			IRReteEntry entry = e.getValue();
			if (entry == null || entry.getStatus() == null) {
				it.remove();
				continue;
			}
		}

		return super.doGC();
	}

	@Override
	public boolean addStmt(IRList stmt, RReteStatus newStatus, RNodeContext context) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println(String.format("%s: addEntry(%s, %s)", getNodeName(), "" + stmt, "" + newStatus));
		}

		String stmtUniqName = ReteUtil.uniqName(stmt);
		IRReteEntry oldEntry = getStmt(stmtUniqName);

		/*******************************************************/
		// Insert entry
		// - Entry not exist
		// - or marked as "drop" (removed by entry table automatically)
		/*******************************************************/
		if (oldEntry == null || oldEntry.getStatus() == null) {
			int stmtLen = stmt.size();

			IRObject[] newElements = new IRObject[stmtLen];
			for (int i = 0; i < stmtLen; ++i) {

				IRObject obj = stmt.get(i);
				if (obj == null) {
					obj = O_Nil;
				}

				newElements[i] = obj;
			}

			IRReteEntry newEntry = entryTable.createEntry(stmt.getNamedName(), newElements, newStatus, true);
			if (!addReteEntry(newEntry)) {
				entryTable.removeEntry(newEntry);
				return false;
			}

			/*******************************************************/
			// Add reference
			/*******************************************************/
			if (context != null) {
				entryTable.addReference(newEntry, context.currentNode, context.currentEntry);
			} else {
				entryTable.addReference(newEntry, this);
			}

			stmtMap.put(stmtUniqName, newEntry);
			return true;
		}

		/*******************************************************/
		// Entry needs be updated
		/*******************************************************/
		RReteStatus oldStatus = oldEntry.getStatus();
		RReteStatus finalStatus = ReteUtil.getReteStatus(oldStatus, newStatus);
		if (finalStatus == null) {
			throw new RException(String.format("Invalid status convert: from=%s, to=%s", oldStatus, newStatus));
		}

		if (finalStatus != oldStatus) {

			switch (finalStatus) {
			case ASSUME:
			case DEFINE:
			case REASON:
			case FIXED_:
				entryTable.setEntryStatus(oldEntry, finalStatus);
				break;
			case TEMP__:
				entryTable.setEntryStatus(oldEntry, finalStatus);
				break;

			case REMOVE:
				entryTable.removeEntryReference(oldEntry, this);
				break;
			default:
				throw new RException("Unknown status: " + finalStatus);
			}

		}
		// status not changed
		else {
			entryQueue.incEntryRedundant();
		}

		// Add this in this branch
		entryQueue.incNodeUpdateCount();

		/*******************************************************/
		// Add reference
		/*******************************************************/
		if (finalStatus != REMOVE && finalStatus != TEMP__ && context != null) {
			entryTable.addReference(oldEntry, context.currentNode, context.currentEntry);
		}

		return true;
	}

	@Override
	public IRReteEntry getStmt(String uniqName) throws RException {
		return stmtMap.get(uniqName);
	}

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}
}
