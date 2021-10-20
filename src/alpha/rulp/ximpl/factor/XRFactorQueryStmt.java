package alpha.rulp.ximpl.factor;
import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_Where;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRMember;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ModelUtil;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.ModifiterUtil.ModifiterData;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintBuilder;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.model.ModelFactory;
import alpha.rulp.ximpl.model.XRSubNodeGraph;

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

		List<IRList> fromList = new ArrayList<>();
		List<IRExpr> doList = null;
		List<IRConstraint1> constraintList = null;

		int queryLimit = -1; // 0: all, -1: default

		/********************************************/
		// Check modifier
		/********************************************/
		ModifiterData data = ModifiterUtil.parseModifiterList(args.listIterator(argIndex), interpreter, frame);

		for (Modifier processingModifier : data.processedModifier) {

			switch (processingModifier.name) {

			// from '(a b c) (factor)
			case A_FROM:
				if (data.fromList == null || data.fromList.isEmpty()) {
					throw new RException("require condList for modifier: " + processingModifier + ", args=" + args);
				}

				fromList.addAll(data.fromList);
				break;

			// limit 1
			case A_Limit:
				queryLimit = data.limit;
				if (queryLimit <= 0) {
					throw new RException(
							"invalid value<" + queryLimit + "> for modifier: " + processingModifier + ", args=" + args);
				}

				break;

			case A_DO:

				if (data.doList == null || data.doList.isEmpty()) {
					throw new RException("invalid do actions<" + doList + "> for modifier: " + processingModifier
							+ ", args=" + args);
				}

				doList = data.doList;
				break;

			case A_Where:

				if (data.whereList.isEmpty()) {
					throw new RException("require whereList for modifier: " + processingModifier + ", args=" + args);
				}

				if (constraintList == null) {
					constraintList = new ArrayList<>();
				}

				IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(
						rstExpr.getType() == RType.LIST ? (IRList) rstExpr : RulpFactory.createList(rstExpr)));

				ConstraintBuilder cb = new ConstraintBuilder(varEntry);
				for (IRObject where : data.whereList) {
					constraintList.add(cb.build(where, interpreter, frame));
				}

				break;

			default:
				throw new RException("unsupport modifier: " + processingModifier);
			}
		}

		/********************************************/
		// Run as rule group
		/********************************************/
		XRSubNodeGraph subGraph = null;
		if (ruleGroupName != null) {
			subGraph = ModelUtil.activeRuleGroup(model, ruleGroupName);
		}

		IRList condList = RulpFactory.createList(fromList);
		IRResultQueue resultQueue = ModelFactory.createResultQueue(model, rstExpr, condList);

		/********************************************/
		// Add do expression
		/********************************************/
		if (doList != null) {
			for (IRExpr expr : doList) {
				resultQueue.addDoExpr(expr);
			}
		}

		/********************************************/
		// Add constraint
		/********************************************/
		if (constraintList != null) {
			for (IRConstraint1 c : constraintList) {
				resultQueue.addConstraint(c);
			}
		}

		try {

			model.query(resultQueue, condList, queryLimit);
			return RulpFactory.createList(resultQueue.getResultList());

		} finally {

			/********************************************/
			// Recovery all nodes' priority
			/********************************************/
			if (subGraph != null) {
				subGraph.rollback();
			}

			resultQueue.close();
		}
	}
}
