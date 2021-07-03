package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.ximpl.node.IRReteNode;

public interface IREntryTable {

	public void addReference(IRReteEntry entry, IRReteNode node, IRReteEntry... parents) throws RException;

	public IRReteEntry createEntry(String namedName, IRObject elements[], RReteStatus status);

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

	public List<? extends IRReteEntry> listAllEntries();

	public void removeEntry(IRReteEntry entry) throws RException;

	public void removeEntryReference(int entryId, int nodeId) throws RException;

	public void setEntryStatus(int entryId, RReteStatus status) throws RException;
}
