package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorGetRule extends AbsAtomFactorAdapter implements IRuleFactor {

	public XRFactorGetRule(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() < 2) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = null;
		String ruleName = null;
		IRObject arg = null;

		IRIterator<? extends IRObject> iter = args.listIterator(1);

		/**************************************************/
		// Check model object
		/**************************************************/
		arg = interpreter.compute(frame, iter.next());
		if (arg instanceof IRModel) {
			model = (IRModel) arg;
			arg = interpreter.compute(frame, iter.next());
		} else {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		}

		ruleName = RulpUtil.asString(arg).asString();
		if (iter.hasNext()) {
			throw new RException("Invalid parameters: " + args);
		}

		IRRule rule = model.getNodeGraph().getRule(ruleName);
		return rule == null ? O_Nil : rule;
	}
}
