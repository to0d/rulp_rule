package alpha.rulp.ximpl.entry;

import java.util.Iterator;

import alpha.rulp.lang.IRList;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRListener1;

public interface IRReteEntry extends IRList, IFixEntry {

	public void addEntryRemovedListener(IRListener1<IRReteEntry> listener);

	public int getChildCount();

	public Iterator<? extends IRReference> getChildIterator();

	public int getEntryId();

	public int getEntryIndex();

	public String getNamedName();

	public int getReferenceCount();

	public Iterator<? extends IRReference> getReferenceIterator();

	public RReteStatus getStatus();

	public boolean isStmt();

	public void removeEntryRemovedListener(IRListener1<IRReteEntry> listener);
}
