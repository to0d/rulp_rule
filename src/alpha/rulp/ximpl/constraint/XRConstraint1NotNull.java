package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_NOT_NULL;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRContext;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1NotNull extends AbsRConstraint1 implements IRConstraint1 {

	private int index;

	private int[] constraintIndex;

	public XRConstraint1NotNull(int index) {
		super();
		this.index = index;
		this.constraintIndex = new int[1];
		this.constraintIndex[0] = index;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		IRObject obj = entry.get(index);
		return obj != null && obj.getType() != RType.NIL;
	}

	@Override
	public String getConstraintExpression() {
		return String.format("(not-null ?%d)", index);
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}

	@Override
	public String getConstraintName() {
		return A_NOT_NULL;
	}
}
