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

public class XRFactorFixStmt extends AbsRFactorAdapter implements IRuleFactor {

	public static IRModel getModel(IRList args) throws RException {

		IRObject obj = args.get(1);
		if (obj instanceof IRModel) {
			return (IRModel) obj;
		}

		return null;
	}

	public static IRIterator<? extends IRObject> getStmtList(IRList args) throws RException {

		if (args.get(1) instanceof IRModel || args.get(1) instanceof IRAtom) {
			return args.listIterator(2);
		} else {
			return args.listIterator(1);
		}
	}

	public XRFactorFixStmt(String factorName) {
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
		IRIterator<? extends IRObject> iter = args.listIterator(1);

		int count = 0;

		/**************************************************/
		// Check model object
		/**************************************************/
		{
			IRObject obj = interpreter.compute(frame, iter.next());
			if (obj instanceof IRModel) {

				model = (IRModel) obj;

			} else {

				model = RuleUtil.getDefaultModel(frame);
				if (model == null) {
					throw new RException("no model be specified");
				}

				count += model.fixStatement(RulpUtil.asList(obj));
			}
		}

		/**************************************************/
		// Process other statements
		/**************************************************/
		while (iter.hasNext()) {
			count += model.fixStatements(RuleUtil.toStmtList(interpreter.compute(frame, iter.next())));
		}

		return RulpFactory.createInteger(count);
	}

}
