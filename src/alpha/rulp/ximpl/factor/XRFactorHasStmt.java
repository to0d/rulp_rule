package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorHasStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorHasStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		if (args.size() != 2 && args.size() != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = null;
		IRList filter = null;

		if (args.size() == 2) {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}

			filter = RulpUtil.asList(interpreter.compute(frame, args.get(1)));
		
		} else {
			model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
			filter = RulpUtil.asList(interpreter.compute(frame, args.get(2)));
		}

		return RulpFactory.createBoolean(model.hasStatement(filter));
	}
}
