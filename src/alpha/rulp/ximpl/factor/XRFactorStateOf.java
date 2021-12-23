package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nan;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRunnable;
import alpha.rulp.rule.RRunState;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorStateOf extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorStateOf(String factorName) {
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
		if (obj instanceof IRRunnable) {
			return RRunState.toObject(((IRRunnable) obj).getRunState());
		}

		return O_Nan;
	}

}
