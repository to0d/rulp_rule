package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IREntryAction;
import alpha.rulp.ximpl.constraint.IRConstraint1;

public interface IRResultQueue extends IREntryAction {

	public void addConstraint(IRConstraint1 con);

	public void addDoExpr(IRExpr expr);

	public void close() throws RException;

	public List<? extends IRObject> getResultList() throws RException;

	public void setOrderBuilder(IREntryIteratorBuilder orderBuilder);

	public void setOrderLimit(int orderLimit);

	public int size();
}
