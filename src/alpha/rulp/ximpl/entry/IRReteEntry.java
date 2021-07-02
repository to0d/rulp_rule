package alpha.rulp.ximpl.entry;

import java.util.Iterator;

import alpha.rulp.lang.IRList;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.rule.RReteStatus;

public interface IRReteEntry extends IRList {

	public void addEntryRemovedListener(IRRListener1<IRReteEntry> listener);

	public int getChildCount();

	public int getEntryId();

	public String getNamedName();

	public int getReferenceCount();

	public Iterator<? extends IRReference> getReferenceIterator();

	public RReteStatus getStatus();

	public boolean isDroped();

	public void removeEntryRemovedListener(IRRListener1<IRReteEntry> listener);
}
