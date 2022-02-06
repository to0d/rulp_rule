package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorGetReteEntry extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorGetReteEntry(String factorName) {
		super(factorName);
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

		IRReteNode node = RuleUtil.asNode(interpreter.compute(frame, args.get(1)));
		int index = RulpUtil.asInteger(interpreter.compute(frame, args.get(2))).asInteger();

		IRReteEntry entry = node.getEntryQueue().getEntryAt(index);
		if (entry == null || entry.isDroped()) {
			return O_Nil;
		}

		return entry;
	}
}
