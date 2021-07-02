package alpha.rulp.ximpl.cache;

import java.io.IOException;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;

public interface IRStmtLoader {

	public IRIterator<? extends IRList> load(IRObject key) throws RException, IOException;

}
