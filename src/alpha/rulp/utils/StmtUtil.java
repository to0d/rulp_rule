package alpha.rulp.utils;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;

public class StmtUtil {

	private static IRObject _getStmt3ModelObject(IRList args) throws RException {

		if (args.size() == 2) {
			return null;
		} else {
			return args.get(1);
		}
	}

	public static IRModel getStmt3Model(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		IRModel model = null;
		IRObject mo = StmtUtil._getStmt3ModelObject(args);
		if (mo == null) {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		} else {
			model = RuleUtil.asModel(interpreter.compute(frame, mo));
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
