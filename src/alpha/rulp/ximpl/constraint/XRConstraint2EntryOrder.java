package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_ENTRY_ORDER;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRReteEntry;
public class XRConstraint2EntryOrder extends AbsRConstraint2 {

	@Override
	public boolean addEntry(IRReteEntry left, IRReteEntry right, IRInterpreter interpreter, IRFrame frame)
			throws RException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getConstraintExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConstraintName() {
		return A_ENTRY_ORDER;
	}

}
