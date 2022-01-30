package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorSetPriority extends AbsAtomFactorAdapter implements IRuleFactor {

	public XRFactorSetPriority(String factorName) {
		super(factorName);
	}

	static void setPriority(IRRule rule, int priority) throws RException {
		rule.getModel().getNodeGraph().setRulePriority(rule, priority);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRObject obj = interpreter.compute(frame, args.get(1));
		int priority = RulpUtil.asInteger(interpreter.compute(frame, args.get(2))).asInteger();

		if (obj instanceof IRRule) {
			setPriority((IRRule) obj, priority);
			return O_Nil;
		}

		throw new RException("unsupport object: " + obj);
	}
}
