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
		return left.getEntryIndex() < right.getEntryIndex();
	}

	@Override
	public String getConstraintExpression() {
		return null;
	}

	@Override
	public String getConstraintName() {
		return A_ENTRY_ORDER;
	}

}
