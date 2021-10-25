package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorTryAddStmt extends AbsRFactorAdapter implements IRuleFactor {

	public XRFactorTryAddStmt(String factorName) {
		super(factorName);
	}

	public static IRObject getStmtObject(IRList args) throws RException {

		if (args.size() == 2) {
			return args.get(1);
		} else {
			return args.get(2);
		}
	}

	public static IRObject getModelObject(IRList args) throws RException {

		if (args.size() == 2) {
			return null;
		} else {
			return args.get(1);
		}
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize != 2 && argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = null;

		/**************************************************/
		// Check model object
		/**************************************************/
		IRObject mo = getModelObject(args);
		if (mo == null) {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		} else {
			model = RuleUtil.asModel(interpreter.compute(frame, mo));
		}

		IRList stmt = RulpUtil.asList(interpreter.compute(frame, getStmtObject(args)));

		return RulpFactory.createBoolean(model.tryAddStatement(stmt));
	}
}
