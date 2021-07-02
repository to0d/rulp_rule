package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.RuleFactory;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRReteNodeCounter;

public class XRFactorPrintModelStatus extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorPrintModelStatus(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 2) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		IRReteNodeCounter counter = RuleFactory.createReteCounter(model.getNodeGraph().getNodeMatrix());

		return RulpFactory.createString(OptimizeUtil.formatNodeCount(counter));
	}
}
