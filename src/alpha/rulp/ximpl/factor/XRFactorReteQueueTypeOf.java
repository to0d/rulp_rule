package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorReteQueueTypeOf extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorReteQueueTypeOf(String factorName) {
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

		IRReteNode node = RuleUtil.asNode(interpreter.compute(frame, args.get(1)));

		return RulpFactory.createInteger(node.getEntryQueue().getQueueType().getIndex());
	}
}
