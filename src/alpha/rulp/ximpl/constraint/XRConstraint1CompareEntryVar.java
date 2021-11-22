package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_CMP_ENTRY_VAR;

import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1CompareEntryVar extends AbsRConstraint1Index1 implements IRConstraint1 {

	private String _constraintExpression = null;

	private RRelationalOperator op;

	private IRVar var;

	public XRConstraint1CompareEntryVar(RRelationalOperator op, int index, IRVar var) {
		super(index);
		this.op = op;
		this.var = var;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		return RulpUtil.computeRelationalExpression(op, entry.get(index), var.getValue());
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s %s ?%d %s)", getConstraintName(), op.getAtom(), index,
					"" + var.getName());
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_CMP_ENTRY_VAR;
	}
}
