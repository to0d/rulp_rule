package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.S_QUESTION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintFactory.RConstraint;
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

		int totalConstraintCount = node.getConstraintCount();
		if (totalConstraintCount == 0) {
			return RulpFactory.createList();
		}

		Map<String, Integer> varIndexMap = new HashMap<>();
		ReteUtil.buildTreeVarList(namedList, varIndexMap);

		/********************************************/
		// Check constraint list
		/********************************************/
		Set<String> matchConstraintSet = new HashSet<>();

		IRIterator<? extends IRObject> it = args.listIterator(argIndex);
		while (it.hasNext()) {

			IRObject obj = it.next();

			switch (obj.getType()) {

			case LIST:

				RConstraint cons = ConstraintFactory.toConstraint((IRList) obj, interpreter, frame);
				switch (cons.constraintType) {
				case TYPE:

					RType columnType = null;
					if (!ReteUtil.isAnyVar(cons.constraintValue)) {
						columnType = RType.toType(RulpUtil.asAtom(cons.constraintValue).asString());
						if (columnType == null || !ReteUtil.isEntryValueType(columnType)) {
							throw new RException("Invalid column type: " + columnType);
						}
					}

					int columnIndex = -1;

					String columnName = RulpUtil.asAtom(cons.onObject).getName();
					if (!columnName.equals(S_QUESTION)) {

						if (!varIndexMap.containsKey(columnName)) {
							throw new RException("invalid column: " + columnName);
						}

						columnIndex = varIndexMap.get(columnName);
					}

					for (int i = 0; i < totalConstraintCount; ++i) {

						IRConstraint1 constraint = node.getConstraint(i);
						if (XRConstraintType.match(constraint, columnType, columnIndex)) {
							matchConstraintSet.add(constraint.getConstraintExpression());
						}
					}

					break;

				default:
					throw new RException("unsupport constraint: " + cons.constraintType);
				}

				break;

			case EXPR:

				break;

			default:
				throw new RException("no constraint list: " + obj);
			}

		}

		if (matchConstraintSet.isEmpty()) {
			return RulpFactory.createList();
		}

		/********************************************/
		// Remove matched Constraint
		/********************************************/
		ArrayList<String> matchConstraintList = new ArrayList<>(matchConstraintSet);
		ArrayList<IRConstraint1> removedConstraintList = new ArrayList<>();
		Collections.sort(matchConstraintList);

		for (String constraintExpression : matchConstraintList) {
			IRConstraint1 cons = node.removeConstraint(constraintExpression);
			if (cons != null) {
				removedConstraintList.add(cons);
			}
		}

		return RulpFactory.createList(removedConstraintList);
	}
}
