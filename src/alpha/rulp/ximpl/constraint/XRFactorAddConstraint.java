package alpha.rulp.ximpl.constraint;

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
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRNamedNode;

public class XRFactorAddConstraint extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorAddConstraint(String factorName) {
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

			int anyIndex = ReteUtil.indexOfVarArgStmt(namedList);
			if (anyIndex != -1) {
				throw new RException(String.format("Can't create var arg node: %s", namedList));
			}

			/**************************************************/
			// Create node
			/**************************************************/
			node = model.getNodeGraph().getNamedNode(namedList.getNamedName(), ReteUtil.getFilerEntryLength(namedList));
			if (node == null) {
				throw new RException(String.format("Fail to create named node: %s", namedList));
			}
		}

		List<IRConstraint1> constraintList = new ArrayList<>();
		IRIterator<? extends IRObject> it = args.listIterator(argIndex);
		ConstraintBuilder cb = new ConstraintBuilder(ReteUtil._varEntry(ReteUtil.buildTreeVarList(namedList)));
		while (it.hasNext()) {
			IRConstraint1 cons = cb.build(it.next(), interpreter, frame);
			if (cons != null) {
				constraintList.add(cons);
			}
		}

		if (constraintList.isEmpty()) {
			throw new RException("no constraint list: " + args);
		}

		/********************************************/
		// Update Constraint
		/********************************************/
		int updateCount = 0;
		for (IRConstraint1 cons : constraintList) {
			if (model.tryAddConstraint(node, cons)) {
				updateCount++;
			}
		}

		return RulpFactory.createInteger(updateCount);
	}

}
