package alpha.rulp.ximpl.factor;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.RReteType;

public class XRFactorReteNodeOf extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorReteNodeOf(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		if (argSize != 2 && argSize != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = RuleUtil.asModel(interpreter.compute(frame, args.get(1)));
		RReteType type = null;
		if (argSize == 3) {
			type = RReteType.getRetetType(RulpUtil.asInteger(interpreter.compute(frame, args.get(2))).asInteger());
		}

		if (type == null) {
			return RulpFactory.createList(model.getNodeGraph().getNodeMatrix().getAllNodes());
		}

		return RulpFactory.createList(model.getNodeGraph().listNodes(type));
	}
}
