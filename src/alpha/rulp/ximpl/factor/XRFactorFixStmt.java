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

public class XRFactorFixStmt extends AbsAtomFactorAdapter implements IRuleFactor {

	public XRFactorFixStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		IRModel model = null;
		IRList stmt = null;

		switch (argSize) {
		case 2:
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
			stmt = RulpUtil.asList(interpreter.compute(frame, args.get(1)));
			break;

		case 3:
			model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
			stmt = RulpUtil.asList(interpreter.compute(frame, args.get(2)));
			break;

		case 4:
			model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
			stmt = RulpUtil.toNamedList(interpreter.compute(frame, args.get(2)),
					interpreter.compute(frame, args.get(3)), frame);
			break;

		default:
			throw new RException("Invalid parameters: " + args);
		}

		return RulpFactory.createBoolean(model.fixStatement(stmt));
	}

}
