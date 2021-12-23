package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StmtUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorRemoveStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorRemoveStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();
		if (argSize != 2 && argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = StmtUtil.getStmtModel(args, interpreter, frame, 3);
		IRObject obj = StmtUtil.getStmt3Object(args);

		// ?0 ?1
		if (ReteUtil.isIndexVarAtom(obj)) {
			obj = interpreter.compute(frame, obj);
		}

		IRList stmt = RulpUtil.asList(interpreter.compute(frame, obj));

		return RulpFactory.createList(model.removeStatement(stmt));
	}

}
