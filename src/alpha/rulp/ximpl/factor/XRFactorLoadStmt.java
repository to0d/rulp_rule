package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.LoadUtil;
import alpha.rulp.utils.RuleIOUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorLoadStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorLoadStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();
		if (argSize != 2 && argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = StmtUtil.getStmtModel(args, interpreter, frame, 3);
		String path = RulpUtil.asString(interpreter.compute(frame, StmtUtil.getStmt3Object(args))).asString();
		String absPath = RulpUtil.lookupFile(path, interpreter, frame);
		if (absPath == null) {
			throw new RException("file not exist: " + path);
		}

		int count = RuleUtil.addStatements(model, RuleIOUtil.loadStmt(absPath, interpreter).iterator());
		return RulpFactory.createInteger(count);
	}
}
