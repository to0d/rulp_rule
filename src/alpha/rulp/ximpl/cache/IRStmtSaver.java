package alpha.rulp.ximpl.cache;

import java.io.IOException;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;

public interface IRStmtSaver {

	public int save(List<? extends IRList> stmtList) throws RException, IOException;

	public boolean needSave();
}