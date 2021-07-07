package alpha.rulp.ximpl.entry;

import java.util.Iterator;

import alpha.rulp.lang.IRList;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.rule.RReteStatus;

public interface IRReteEntry extends IRList, IFixEntry {

	public void addEntryRemovedListener(IRRListener1<IRReteEntry> listener);

	public int getChildCount();

	public Iterator<? extends IRReference> getChildIterator();

	public int getEntryId();

	public String getNamedName();

	public int getReferenceCount();

	public Iterator<? extends IRReference> getReferenceIterator();

	public RReteStatus getStatus();
	
	public boolean isStmt();

	public void removeEntryRemovedListener(IRRListener1<IRReteEntry> listener);
}
