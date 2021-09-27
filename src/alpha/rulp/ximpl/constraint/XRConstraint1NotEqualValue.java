package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1NotEqualValue extends AbsRConstraint1 implements IRConstraint1 {

	private int index;

	private int[] constraintIndex;

	private IRObject obj;

	public XRConstraint1NotEqualValue(int index, IRObject obj) {
		super();
		this.index = index;
		this.obj = obj;
		this.constraintIndex = new int[1];
		this.constraintIndex[0] = index;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {
		return !ReteUtil.equal(entry.get(index), obj);
	}

	@Override
	public String getConstraintExpression() {
		return String.format("(not-qual ?%d %s)", index, "" + obj);
	}

	@Override
	public RConstraintType getConstraintType() {
		return RConstraintType.NOT_EQUAL_OBJ;
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}
}
