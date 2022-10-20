package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_True;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorAddNodeFunc extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorAddNodeFunc(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		/********************************************/
		// Check parameters
		/********************************************/
		if (argSize != 2 && argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = StmtUtil.getStmtModel(args, interpreter, frame, 3);
		IRList funcExprList = RulpUtil.asList(StmtUtil.getStmt3Object(args));
		RuleUtil.addNodeFunc(model, funcExprList);
		
		return O_True;
	}
}
