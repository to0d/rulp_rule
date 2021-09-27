package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_EQUAL_VALUE;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;
public class XRConstraint1EqualValue extends AbsRConstraint1 implements IRConstraint1 {

	private int[] constraintIndex;

	private int index;

	private IRObject obj;

	public XRConstraint1EqualValue(int index, IRObject obj) {
		super();
		this.index = index;
		this.obj = obj;
		this.constraintIndex = new int[1];
		this.constraintIndex[0] = index;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {
		return ReteUtil.equal(entry.get(index), obj);
	}

	@Override
	public String getConstraintExpression() {
		return String.format("(equal ?%d %s)", index, "" + obj);
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}

	@Override
	public String getConstraintName() {
		return A_EQUAL_VALUE;
	}
}