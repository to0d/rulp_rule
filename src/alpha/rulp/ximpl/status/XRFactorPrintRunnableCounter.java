package alpha.rulp.ximpl.status;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRRunnable;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.StatsUtil;
import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorPrintRunnableCounter extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorPrintRunnableCounter(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize != 2) {
			throw new RException("Invalid parameters: " + args);
		}

		IRObject obj = interpreter.compute(frame, args.get(1));
		if (!(obj instanceof IRRunnable)) {
			throw new RException("not runnable object:" + obj);
		}

		if (obj instanceof IRModel) {
			return RulpFactory.createString(StatsUtil.formatModelCount(RuleUtil.asModel(obj).getCounter()));
		}

		if (obj instanceof IRRule) {
			return RulpFactory.createString(StatsUtil.formatRuleCount(RuleUtil.asRule(obj).getCounter()));
		}

		throw new RException("not support object");
	}
}
