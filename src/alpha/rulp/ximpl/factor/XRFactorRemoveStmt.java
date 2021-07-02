package alpha.rulp.ximpl.factor;

import java.util.ArrayList;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorRemoveStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorRemoveStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize < 2) {
			throw new RException("need more statmt");
		}

		IRModel model = null;
		IRList stmt = null;

		ArrayList<IRList> dropStmts = new ArrayList<>();

		IRIterator<? extends IRObject> iter = args.listIterator(1);

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

				obj = interpreter.compute(frame, obj);
				stmt = RulpUtil.asList(obj);

				dropStmts.addAll(model.removeStatement(stmt));
			}
		}

		/**************************************************/
		// Process other statements
		/**************************************************/
		while (iter.hasNext()) {
			IRObject obj = interpreter.compute(frame, iter.next());
			obj = interpreter.compute(frame, obj);
			stmt = RulpUtil.asList(obj);

			dropStmts.addAll(model.removeStatement(stmt));
		}

		return RulpFactory.createList(dropStmts);
	}

}
