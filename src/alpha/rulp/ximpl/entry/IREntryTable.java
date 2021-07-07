package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.ximpl.node.IRReteNode;

public interface IREntryTable {

	public void addReference(IRReteEntry entry, IRReteNode node, IRReteEntry... parents) throws RException;

	public IRReteEntry createEntry(String namedName, IRObject elements[], RReteStatus status, boolean isStmt);

	public int doGC() throws RException;

	public IRReteEntry getEntry(int entryId) throws RException;

	public int getEntryCount();

	public IFixEntryArray<? extends IRReteEntry> getEntryFixArray();

	public int getEntryMaxId();

	public int getETAMaxActionSize();

	public int getETAQueueCapacity();

	public int getETAQueueExpendCount();

	public int getETAQueueMaxSize();

	public int getETATotalActionSize();

	public IFixEntryArray<? extends IRReference> getReferenceFixArray();

	public List<? extends IRReteEntry> listAllEntries();

	public void removeEntry(IRReteEntry entry) throws RException;

	public void removeEntryReference(IRReteEntry entry, IRReteNode node) throws RException;

	public void setEntryStatus(IRReteEntry entry, RReteStatus status) throws RException;
}
