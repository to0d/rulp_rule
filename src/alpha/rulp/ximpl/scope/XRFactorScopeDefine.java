package alpha.rulp.ximpl.scope;

import static alpha.rulp.lang.Constant.A_INTEGER;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;

public class XRFactorScopeDefine extends AbsRFactorAdapter {

	public XRFactorScopeDefine(String factorName) {
		super(factorName);
	}

	public IRObject addStepValueVar(String varName, IRScope scope, RType varType, IRList args,
			IRInterpreter interpreter, IRFrame frame) throws RException {

		IRObject fromValue = null;
		IRObject toValue = null;
		IRObject stepValue = null;

		if (args.size() >= 5) {
			fromValue = interpreter.compute(frame, args.get(4));
		}

		if (args.size() >= 6) {
			toValue = interpreter.compute(frame, args.get(5));
		}

		if (args.size() >= 7) {
			stepValue = interpreter.compute(frame, args.get(6));
		}

		return ScopeUtils.addVar(scope, varName, varType, fromValue, toValue, stepValue);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		// (scope::define ?age int 37 40)
		/********************************************/
		int argSize = args.size();
		if (argSize < 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRScope scope = RuleUtil.asScope(interpreter.compute(frame, args.get(1)));
		String varName = RulpUtil.asAtom(interpreter.compute(frame, args.get(2))).getName();
		if (!RulpUtil.isVarName(varName)) {
			throw new RException("Invalid var name: " + varName);
		}

		IRObject arg3 = interpreter.compute(frame, args.get(3));
		switch (arg3.getType()) {
		case ATOM:

			IRAtom atom3 = RulpUtil.asAtom(arg3);

			switch (atom3.getName()) {
			case A_INTEGER:
				return addStepValueVar(varName, scope, RType.toType(atom3.getName()), args, interpreter, frame);

			default:
				break;
			}

			break;

		case LIST:

			// add atom list var
			if (argSize == 4) {
				return ScopeUtils.addVar(scope, varName, RulpUtil.asList(arg3));
			}

			break;

		default:
			break;
		}

		throw new RException("Invalid parameters: " + args);
	}
}
