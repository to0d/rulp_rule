package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nan;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorSetModelCachePath extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorSetModelCachePath(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		String cachePath = RulpUtil.asString(interpreter.compute(frame, args.get(2))).asString();
		model.setModelCachePath(cachePath);
		return O_Nan;
	}

}
