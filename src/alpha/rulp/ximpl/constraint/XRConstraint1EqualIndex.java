package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_EQUAL_INDEX;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1EqualIndex extends AbsRConstraint1 implements IRConstraint1 {

	private int[] constraintIndex;

	private int idx1;

	private int idx2;

	public XRConstraint1EqualIndex(int idx1, int idx2) {
		super();
		this.idx1 = idx1;
		this.idx2 = idx2;

		this.constraintIndex = new int[2];

		if (idx1 > idx2) {
			this.constraintIndex[0] = idx2;
			this.constraintIndex[1] = idx1;
		} else {
			this.constraintIndex[0] = idx1;
			this.constraintIndex[1] = idx2;
		}
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		return ReteUtil.equal(entry.get(idx1), entry.get(idx2));
	}

	@Override
	public String getConstraintExpression() {
		return String.format("(equal ?%d ?%d)", idx1, idx2);
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}

	@Override
	public String getConstraintName() {
		return A_EQUAL_INDEX;
	}
}
