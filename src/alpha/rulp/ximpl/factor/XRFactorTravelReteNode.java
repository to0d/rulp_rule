package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRFunction;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorTravelReteNode extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorTravelReteNode(String factorName) {
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
		IRFunction func = RulpUtil.asFunction(interpreter.compute(frame, args.get(2)));

		IRReteNode rtNode = RuleUtil.travelReteParentNodeByPostorder(node, (_node) -> {

			IRObject rst = interpreter.compute(frame, RulpFactory.createExpression(func, _node));
			if (rst == null || rst.getType() == RType.NIL) {
				return false;
			}

			return RulpUtil.asBoolean(rst).asBoolean();
		});

		return rtNode == null ? O_Nil : rtNode;
	}
}
