package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_Order_by;
import static alpha.rulp.rule.Constant.A_Where;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRMember;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintBuilder;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.model.ModelFactory;
import alpha.rulp.ximpl.node.IRNodeGraph.IRNodeSubGraph;

public class XRFactorQueryStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorQueryStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		// - (query-stmt m '(?x) from '(stmt) limit 1)
		/********************************************/
		int argSize = args.size();
		if (argSize < 4) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = null;
		String ruleGroupName = null;

		int argIndex = 1;

		/**************************************************/
		// Check model object
		/**************************************************/
		{
			IRObject argObj = args.get(argIndex);

			if (argObj.getType() == RType.MEMBER) {
				IRMember mbr = RulpUtil.asMember(argObj);
				model = RuleUtil.asModel(interpreter.compute(frame, mbr.getSubject()));
				ruleGroupName = mbr.getName();
				++argIndex;

			} else if (argObj.getType() != RType.LIST) {

				IRObject obj = interpreter.compute(frame, argObj);
				if (obj instanceof IRModel) {
					model = (IRModel) obj;
					++argIndex;
				}
			}
		}

		if (model == null) {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		}

		IRObject rstExpr = args.get(argIndex++);
		if (rstExpr.getType() != RType.EXPR && rstExpr.getType() != RType.LIST && !RulpUtil.isVarAtom(rstExpr)) {
			throw new RException("unsupport rstExpr: " + rstExpr);
		}

		IRList fromList = null;
		IRList whereList = null;
		IRList doList = null;
		int queryLimit = -1; // 0: all, -1: default
		IRList orderByList = null;

		/********************************************/
		// Check modifier
		/********************************************/
		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame)) {

			switch (modifier.name) {

			// from '(a b c) (factor)
			case A_FROM:
				fromList = RulpUtil.asList(modifier.obj);
				break;

			case A_Where:
				whereList = RulpUtil.asList(modifier.obj);
				break;

			case A_DO:
				doList = RulpUtil.asList(modifier.obj);
				break;

			// limit 1
			case A_Limit:
				queryLimit = RulpUtil.asInteger(modifier.obj).asInteger();
				if (queryLimit <= 0) {
					throw new RException("invalid value<" + modifier.obj + "> for modifier: " + modifier.name);
				}

				break;

			case A_Order_by:
				orderByList = RulpUtil.asList(modifier.obj);
				break;

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		/********************************************/
		// Run as rule group
		/********************************************/
		IRNodeSubGraph subGraph = null;
		if (ruleGroupName != null) {
			subGraph = model.getNodeGraph().buildRuleGroupSubGraph(ruleGroupName);
		}

		IRResultQueue resultQueue = ModelFactory.createResultQueue(model, rstExpr, fromList);

		/********************************************/
		// Add do expression
		/********************************************/
		if (doList != null) {
			for (IRObject doObj : RulpUtil.toArray(doList)) {
				resultQueue.addDoExpr(RulpUtil.asExpression(doObj));
			}
		}

		/********************************************/
		// Add constraint
		/********************************************/
		if (whereList != null) {

			IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(
					rstExpr.getType() == RType.LIST ? (IRList) rstExpr : RulpFactory.createList(rstExpr)));

			ConstraintBuilder cb = new ConstraintBuilder(varEntry);
			for (IRObject where : RulpUtil.toArray(whereList)) {
				resultQueue.addConstraint(cb.build(where, interpreter, frame));
			}
		}

		/******************************************************************************/
		// If there is an "order", which means query all possible result, and then order
		// the result, set the queryLimit to -1
		/******************************************************************************/
		int finalLimit = -1;
		if (orderByList != null) {
			finalLimit = queryLimit;
			queryLimit = -1;

			// Check order format
			for (IRObject orderOption : RulpUtil.toArray(orderByList)) {

			}
		}

		try {

			/********************************************/
			// Activate sub group
			/********************************************/
			if (subGraph != null) {
				subGraph.activate(model.getPriority());
			}

			model.query(resultQueue, fromList, queryLimit);
			return RulpFactory.createList(resultQueue.getResultList());

		} finally {

			/********************************************/
			// Recovery sub group
			/********************************************/
			if (subGraph != null) {
				subGraph.rollback();
			}

			resultQueue.close();
		}
	}
}
