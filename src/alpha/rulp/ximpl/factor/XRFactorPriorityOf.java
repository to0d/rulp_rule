package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRunnable;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorPriorityOf extends AbsRFactorAdapter implements IRuleFactor {

	public XRFactorPriorityOf(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 2) {
			throw new RException("Invalid parameters: " + args);
		}

		IRObject obj = interpreter.compute(frame, args.get(1));
		if (!(obj instanceof IRRunnable)) {
			throw new RException("not runnable object");
		}

		return RulpFactory.createInteger(((IRRunnable) obj).getPriority());
	}
}
