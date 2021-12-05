package alpha.rulp.ximpl.factor;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintBuilder;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.model.IRuleFactor;

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
		IRReteNode node = ReteUtil.findNameNode(model.getNodeGraph(), namedList);
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

			IRObject obj = it.next();
			if (obj.getType() != RType.EXPR) {
				throw new RException("no constraint: " + obj);
			}

			IRConstraint1 cons = cb.build((IRExpr) obj, interpreter, frame);
			if (cons != null) {
				constraintList.add(cons);
			}
		}

		/********************************************/
		// Update Constraint
		/********************************************/
		int updateCount = 0;
		for (IRConstraint1 cons : constraintList) {
			if (model.addConstraint(node, cons)) {
				updateCount++;
			}
		}

		return RulpFactory.createInteger(updateCount);
	}

}
