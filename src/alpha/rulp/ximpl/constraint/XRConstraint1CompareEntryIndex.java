package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_CMP_ENTRY_INDEX;

import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.MathUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1CompareEntryIndex extends AbsRConstraint1 implements IRConstraint1 {

	private String _constraintExpression = null;

	private int[] constraintIndex;

	private RRelationalOperator op;

	public XRConstraint1CompareEntryIndex(RRelationalOperator op, int idx1, int idx2) {
		super();
		this.op = op;
		this.constraintIndex = new int[2];
		this.constraintIndex[0] = idx1;
		this.constraintIndex[1] = idx2;
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {
		return MathUtil.computeRelationalExpression(op, entry.get(constraintIndex[0]), entry.get(constraintIndex[1]));
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s %s ?%d ?%d)", getConstraintName(), op.getAtom(),
					constraintIndex[0], constraintIndex[1]);
		}

		return _constraintExpression;
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}

	@Override
	public String getConstraintName() {
		return A_CMP_ENTRY_INDEX;
	}
}
