package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_NOT_EQUAL_VALUE;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1NotEqualValue extends AbsRConstraint1Index1 implements IRConstraint1 {

	private IRObject obj;

	public XRConstraint1NotEqualValue(int index, IRObject obj) {
		super(index);
		this.obj = obj;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		return !ReteUtil.equal(entry.get(index), obj);
	}

	@Override
	public String getConstraintExpression() {
		return String.format("(not-qual ?%d %s)", index, "" + obj);
	}

	@Override
	public String getConstraintName() {
		return A_NOT_EQUAL_VALUE;
	}
}
