package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.A_FUNCTION;
import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.runtime.IRFunction;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Func extends AbsRConstraint1 implements IRConstraint1Func {

	static IRObject asRObject(Object obj) {

		if (obj instanceof IRObject) {
			return (IRObject) obj;
		}

		return O_Nil;
	}

	protected IRFunction func;

	public XRConstraint1Func(IRFunction func) throws RException {
		super();
		this.func = func;

		RulpUtil.incRef(func);
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {

		IRObject rst = context.getInterpreter().compute(context.getFrame(),
				RulpFactory.createExpression(func, asRObject(context), entry));
		
		return RulpUtil.asBoolean(rst).asBoolean();
	}

	@Override
	protected void _delete() throws RException {

		if (func != null) {
			RulpUtil.decRef(func);
			func = null;
		}

		super._delete();
	}

	@Override
	public String getConstraintExpression() {
		return "(" + func.getName() + ")";
	}

	@Override
	public int[] getConstraintIndex() {
		return null;
	}

	@Override
	public String getConstraintName() {
		return A_FUNCTION;
	}

	@Override
	public IRFunction getFunc() {
		return func;
	}
}
