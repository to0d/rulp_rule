package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RefTreeUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorMakeRefTree extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorMakeRefTree(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int size = args.size();
		if (size < 3 || size > 5) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		IRList stmt = RulpUtil.asList(interpreter.compute(frame, args.get(2)));

		int width = -1;
		if (size > 3) {
			width = RulpUtil.asInteger(interpreter.compute(frame, args.get(3))).asInteger();
		}

		int deep = -1;
		if (size > 4) {
			deep = RulpUtil.asInteger(interpreter.compute(frame, args.get(4))).asInteger();
		}

		return RefTreeUtil.buildStmtRefTree(stmt, model, width, deep);
	}
}
