package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;

public interface IREntryList {

	public IRReteEntry getEntryAt(int index) throws RException;

	public int size();

}
