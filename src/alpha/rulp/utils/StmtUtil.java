package alpha.rulp.utils;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;

public class StmtUtil {

	public static IRModel getStmtModel(IRList args, IRInterpreter interpreter, IRFrame frame, int stmtLen)
			throws RException {

		IRModel model = null;

		if (args.size() == stmtLen) {
			model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		} else {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		}

		return model;
	}

	public static IRObject getStmt3Object(IRList args) throws RException {

		if (args.size() == 2) {
			return args.get(1);
		} else {
			return args.get(2);
		}
	}

}
