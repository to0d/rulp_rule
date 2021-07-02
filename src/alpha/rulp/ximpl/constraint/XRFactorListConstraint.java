package alpha.rulp.ximpl.constraint;

import java.util.ArrayList;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRNamedNode;

public class XRFactorListConstraint extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorListConstraint(String factorName) {
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

		IRModel model = null;
		int argIndex = 1;

		/**************************************************/
		// Check model
		/**************************************************/
		if (argSize == 3) {
			model = RuleUtil.asModel(interpreter.compute(frame, args.get(argIndex)));
			++argIndex;
		} else {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		}

		/**************************************************/
		// Check named list
		/**************************************************/
		IRList namedList = RulpUtil.asList(interpreter.compute(frame, args.get(argIndex++)));

		/**************************************************/
		// Find node
		/**************************************************/
		IRNamedNode node = ReteUtil.findNameNode(model.getNodeGraph(), namedList);
		if (node == null) {
			throw new RException(String.format("node not found: %s", namedList));
		}

		ArrayList<IRConstraint1> cons = new ArrayList<>();
		for (int i = 0; i < node.getConstraintCount(); ++i) {
			cons.add(node.getConstraint(i));
		}

		return RulpFactory.createList(cons);
	}
}
