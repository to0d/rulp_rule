package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.RReteType;

public class XRFactorTraceRule extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorTraceRule(String factorName) {
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
		if (!(obj instanceof IRRule)) {
			throw new RException("not rule: " + obj);
		}

		for (IRReteNode node : RuleUtil.asRule(obj).getAllNodes()) {
			if (!RReteType.isRootType(node.getReteType())) {
				node.setTrace(true);
			}
		}

		return O_Nil;
	}
}
