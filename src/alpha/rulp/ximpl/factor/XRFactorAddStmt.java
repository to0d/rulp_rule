package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorAddStmt extends AbsRFactorAdapter implements IRuleFactor {

	public static IRObject getModelObject(IRList args) throws RException {

		IRObject obj = args.get(1);

		if (obj instanceof IRModel || obj instanceof IRAtom) {
			return obj;
		} else {
			return null;
		}
	}

	public static IRIterator<? extends IRObject> getStmtList(IRList args) throws RException {

		IRObject obj = args.get(1);
		if (obj instanceof IRModel || obj instanceof IRAtom) {
			return args.listIterator(2);
		} else {
			return args.listIterator(1);
		}
	}

	public XRFactorAddStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize < 2) {
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

		/**************************************************/
		// Process add statements
		/**************************************************/
		int count = 0;
		IRIterator<? extends IRObject> it = getStmtList(args);
		while (it.hasNext()) {
			count += model.addStatement(RulpUtil.asList(interpreter.compute(frame, it.next())));
		}

		return RulpFactory.createInteger(count);
	}

}
