package alpha.rulp.ximpl.cache;

import java.io.IOException;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;

public interface IRStmtSaver {

	public int save(List<IRList> stmtList, IRObject key) throws RException, IOException;
}
