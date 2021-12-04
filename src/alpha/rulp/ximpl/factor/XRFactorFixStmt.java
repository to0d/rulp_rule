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

public class XRFactorFixStmt extends AbsRFactorAdapter implements IRuleFactor {

	public XRFactorFixStmt(String factorName) {
		super(factorName);
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
		IRObject mo = XRFactorAddStmt.getModelObject(args);
		if (mo == null) {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		} else {
			model = RuleUtil.asModel(interpreter.compute(frame, mo));
		}

		return RulpFactory.createBoolean(
				model.fixStatement(RulpUtil.asList(interpreter.compute(frame, XRFactorAddStmt.getStmtObject(args)))));
	}

}
