package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorBackSearch extends AbsAtomFactorAdapter implements IRuleFactor {

	public XRFactorBackSearch(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		// (back-search m '(a b c))
		int argSize = args.size();
		if (argSize != 2 && argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = StmtUtil.getStmtModel(args, interpreter, frame, 3);
		IRList stmt = RulpUtil.asList(interpreter.compute(frame, StmtUtil.getStmt3Object(args)));

		return RulpFactory.createBoolean(model.backSearch(stmt));
	}

}
