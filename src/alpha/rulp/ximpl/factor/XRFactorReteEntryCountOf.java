package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorReteEntryCountOf extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorReteEntryCountOf(String factorName) {
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

		IRReteNode node = RuleUtil.asNode(interpreter.compute(frame, args.get(1)));
		RReteStatus status = null;
		if (argSize == 3) {
			status = RReteStatus
					.getRetetStatus(RulpUtil.asInteger(interpreter.compute(frame, args.get(2))).asInteger());
		}

		return RulpFactory.createInteger(node.getEntryQueue().getEntryCounter().getEntryCount(status));
	}
}
