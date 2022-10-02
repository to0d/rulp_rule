package alpha.rulp.ximpl.bs;

import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;

public interface IRBSNodeQuery extends IRBSNode {

	public boolean hasMore() throws RException;

	public List<IRList> next() throws RException;
}
