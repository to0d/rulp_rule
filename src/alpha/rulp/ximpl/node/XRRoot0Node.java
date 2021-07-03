package alpha.rulp.ximpl.node;

import static alpha.rulp.lang.Constant.O_Nil;

import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRRoot0Node extends XRReteNode0 implements IRRootNode {

	protected IREntryTable entryTable;

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}

	@Override
	public IRReteEntry getStmt(String uniqName) throws RException {
		return stmtMap.get(uniqName);
	}

	protected Map<String, IRReteEntry> stmtMap = new HashMap<>();

	@Override
	public boolean addStmt(IRList stmt, RReteStatus newStatus) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println(String.format("%s: addEntry(%s, %s)", getNodeName(), "" + stmt, "" + newStatus));
		}

		String uniqName = ReteUtil.uniqName(stmt);
		IRReteEntry oldEntry = getStmt(uniqName);

		/*******************************************************/
		// Entry not exist
		/*******************************************************/
		if (oldEntry == null) {

			int stmtLen = stmt.size();

			IRObject[] newElements = new IRObject[stmtLen];
			for (int i = 0; i < stmtLen; ++i) {

				IRObject obj = stmt.get(i);
				if (obj == null) {
					obj = O_Nil;
				}

				newElements[i] = obj;
			}

			IRReteEntry newEntry = entryTable.createEntry(stmt.getNamedName(), newElements, newStatus);
			entryTable.addReference(newEntry, this);
//			entryTable.setEntryLife(newEntry.getEntryId(), newStatus == DEFINED ? 1 : 0);

			if (!addReteEntry(newEntry)) {
				entryTable.removeEntry(newEntry);
				return false;
			}

			stmtMap.put(uniqName, newEntry);
			return true;

		} else {

			// Add this in this branch, the value will be updated in addEntry() in previous
			// branch
			entryQueue.incNodeUpdateCount();

			/*******************************************************/
			// Entry needs be updated
			/*******************************************************/
			RReteStatus oldStatus = oldEntry.getStatus();
			RReteStatus finalStatus = ReteUtil.getReteStatus(oldStatus, newStatus);

			if (finalStatus != oldStatus) {

				int oldEntryId = oldEntry.getEntryId();

				switch (finalStatus) {
				case ASSUMED:
					entryTable.setEntryStatus(oldEntryId, finalStatus);
//					entryTable.setEntryLife(oldEntryId, 0);
					break;

				case DEFINED:
					entryTable.setEntryStatus(oldEntryId, finalStatus);
//					entryTable.setEntryLife(oldEntryId, 1);
					break;

				case REMOVED:
					entryTable.removeEntryReference(oldEntryId, getNodeId());
					break;

				case REASONED:
					// TODO
					entryTable.setEntryStatus(oldEntryId, finalStatus);
//					entryTable.setEntryLife(oldEntryId, 0);
					break;
				default:
					throw new RException("Unknown status: " + finalStatus);
				}

			}
			// status not changed
			else {
				entryQueue.incEntryRedundant();
			}

			return false;
		}

	}
}
