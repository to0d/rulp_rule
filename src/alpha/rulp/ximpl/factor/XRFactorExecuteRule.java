package alpha.rulp.ximpl.factor;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorExecuteRule extends AbsAtomFactorAdapter implements IRuleFactor {

	public XRFactorExecuteRule(String factorName) {
		super(factorName);
	}

	public static int executeRule(IRRule rule, List<IRList> stmtList) {
		return 0;
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRRule rule = RuleUtil.asRule(interpreter.compute(frame, args.get(1)));
		ArrayList<IRList> stmtList = new ArrayList<>();
		RulpUtil.addAll(stmtList, RuleUtil.toStmtList(RulpUtil.asList(interpreter.compute(frame, args.get(2)))));

		return RulpFactory.createInteger(executeRule(rule, stmtList));
	}

}
