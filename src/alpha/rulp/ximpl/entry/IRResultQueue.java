package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;

public interface IRResultQueue {

	public boolean addEntry(IRReteEntry entry) throws RException;

	public List<? extends IRObject> getResultList();

	public int size();

}
