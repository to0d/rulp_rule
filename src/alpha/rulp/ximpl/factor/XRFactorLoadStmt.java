package alpha.rulp.ximpl.factor;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.LoadUtil;
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

		int count = RuleUtil.addStatements(model, loadStmt(absPath, interpreter).iterator());
		return RulpFactory.createInteger(count);
	}

	public static List<? extends IRList> loadStmt(String path, IRInterpreter interpreter) throws RException {

		ArrayList<IRList> stmts = new ArrayList<>();

		IRList stmtList = LoadUtil.loadRulp(interpreter, path, "utf-8");
		IRIterator<? extends IRObject> it = stmtList.iterator();
		while (it.hasNext()) {
			stmts.add(RulpUtil.asList(it.next()));
		}

		return stmts;
	}
}
