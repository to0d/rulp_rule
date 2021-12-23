package alpha.rulp.ximpl.rbs;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRArray;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;

public class XRFactorPrintTable extends AbsAtomFactorAdapter implements IRFactor {

	public XRFactorPrintTable(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 2) {
			throw new RException("Invalid parameters: " + args);
		}

		IRArray table = RulpUtil.asArray(interpreter.compute(frame, args.get(1)));
		interpreter.out(RBSUtil.printRBSTable(table));
		return O_Nil;
	}

}