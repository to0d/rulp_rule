package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_EQUAL_VALUE;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1EqualValue extends AbsRConstraint1Index1 implements IRConstraint1 {

	private String _constraintExpression = null;

	private IRObject obj;

	public XRConstraint1EqualValue(int index, IRObject obj) {
		super(index);
		this.obj = obj;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		return RulpUtil.equal(entry.get(index), obj);
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s ?%d %s)", getConstraintName(), index, "" + obj);
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_EQUAL_VALUE;
	}
}