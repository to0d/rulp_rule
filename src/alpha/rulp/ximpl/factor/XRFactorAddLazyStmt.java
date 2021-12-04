package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorAddLazyStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorAddLazyStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		if (args.size() != 4) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		IRList condList = RuleUtil.toCondList(interpreter.compute(frame, args.get(2)));
		IRObject obj = args.get(3);

		model.getNodeGraph().bindNode(model.getNodeGraph().addWorker(null, (m) -> {
			RuleUtil.addStatements(model, RuleUtil.toStmtList(interpreter.compute(frame, obj)));
			return true;
		}), model.findNode(condList));

		return O_Nil;
	}
}
