package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorSetPriority extends AbsAtomFactorAdapter implements IRuleFactor {

	public XRFactorSetPriority(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRRule rule = RuleUtil.asRule(interpreter.compute(frame, args.get(1)));
		int priority = RulpUtil.asInteger(interpreter.compute(frame, args.get(2))).asInteger();
		rule.getModel().getNodeGraph().setRulePriority(rule, priority);

		return rule;
	}
}
