package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.constraint.IRConstraint1;

public interface IRResultQueue {

	public boolean addEntry(IRReteEntry entry) throws RException;

	public List<? extends IRObject> getResultList();

	public int size();

	public void addConstraint(IRConstraint1 con);

	public void addDoExpr(IRExpr expr);

	public void close() throws RException;
}
