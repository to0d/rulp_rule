package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_CMP_ENTRY_VALUE;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1CompareEntryValue extends AbsRConstraint1Index1 implements IRConstraint1 {

	private String _constraintExpression = null;

	private IRObject obj;

	private RRelationalOperator op;

	public XRConstraint1CompareEntryValue(RRelationalOperator op, int index, IRObject obj) {
		super(index);
		this.op = op;
		this.obj = obj;
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {
		return RulpUtil.computeRelationalExpression(op, entry.get(index), obj);
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s %s ?%d %s)", getConstraintName(), op.getAtom(), index, "" + obj);
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_CMP_ENTRY_VALUE;
	}
}
