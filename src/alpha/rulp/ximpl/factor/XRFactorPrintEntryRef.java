package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IROut;
import alpha.rulp.utils.RefPrinter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorPrintEntryRef extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorPrintEntryRef(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 3 && args.size() != 2) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));

		RefPrinter refPrinter = new RefPrinter(model);
		IROut out = interpreter.getOut();

		if (args.size() == 2) {

			for (IRList stmt : model.listStatements(null, 0, 0)) {
				out.out(refPrinter.printStmt(stmt));
			}

		} else {

			IRIterator<? extends IRList> it = RuleUtil.toStmtList(interpreter.compute(frame, args.get(2)));
			while (it.hasNext()) {
				out.out(refPrinter.printStmt(it.next()));
			}

		}

		return O_Nil;
	}
}
