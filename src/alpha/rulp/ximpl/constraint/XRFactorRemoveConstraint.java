package alpha.rulp.ximpl.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class XRFactorRemoveConstraint extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorRemoveConstraint(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		/********************************************/
		// Check parameters
		/********************************************/
		if (argSize < 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = null;
		int argIndex = 1;

		/**************************************************/
		// Check model
		/**************************************************/
		{
			IRObject obj = interpreter.compute(frame, args.get(argIndex));
			if (obj instanceof IRModel) {
				model = (IRModel) obj;
				++argIndex;
			} else {
				model = RuleUtil.getDefaultModel(frame);
				if (model == null) {
					throw new RException("no model be specified");
				}
			}
		}

		/**************************************************/
		// Check named list
		/**************************************************/
		IRList namedList = RulpUtil.asList(interpreter.compute(frame, args.get(argIndex++)));
		IRNamedNode node = ReteUtil.findNameNode(model.getNodeGraph(), namedList);
		if (node == null) {
			throw new RException("named node not found: " + namedList);
		}

		List<String> constraintList = new ConstraintBuilder(ReteUtil._varEntry(ReteUtil.buildTreeVarList(namedList)))
				.match(node, RulpFactory.createList(args.listIterator(argIndex)), interpreter, frame);
		if (constraintList.isEmpty()) {
			return RulpFactory.createList();
		}

		/********************************************/
		// Remove matched Constraint
		/********************************************/
		ArrayList<IRConstraint1> removedConstraintList = new ArrayList<>();
		for (String des : constraintList) {
			IRConstraint1 removedCons = node.removeConstraint(des);
			if (removedCons == null) {
				throw new RException("constraint not found: " + des);
			}
			removedConstraintList.add(removedCons);
		}

		Collections.sort(removedConstraintList, (c1, c2) -> {
			return c1.getConstraintExpression().compareTo(c2.getConstraintExpression());
		});

		return RulpFactory.createList(removedConstraintList);
	}
}
