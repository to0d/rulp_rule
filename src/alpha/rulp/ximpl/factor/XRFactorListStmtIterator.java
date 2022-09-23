package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorListStmtIterator extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorListStmtIterator(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {
		throw new RException("not support");
	}
}
