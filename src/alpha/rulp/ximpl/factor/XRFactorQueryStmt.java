package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_Backward;
import static alpha.rulp.rule.Constant.A_Desc;
import static alpha.rulp.rule.Constant.A_Forward;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_Order_by;
import static alpha.rulp.rule.Constant.A_Reverse;
import static alpha.rulp.rule.Constant.A_Where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintBuilder;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryIteratorBuilder;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.entry.REntryFactory;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.model.ModelFactory;
import alpha.rulp.ximpl.node.IRNodeGraph.IRNodeSubGraph;

public class XRFactorQueryStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

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

		/**************************************************/
		// Check model object
		/**************************************************/
		int argIndex = 1;
		IRModel model = null;
		String ruleGroupName = null;

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

		IRList condList = null;
		IRList whereList = null;
		IRList doList = null;
		int limit = -1; // 0: all, -1: default

		IREntryIteratorBuilder orderBuilder = null;
		String orderBuilderName = null;

		Boolean backward = null;

		/********************************************/
		// Check modifier
		/********************************************/
		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame)) {

			switch (modifier.name) {

			// from '(a b c) (factor)
			case A_FROM:
				condList = RulpUtil.asList(modifier.obj);
				break;

			case A_Where:
				whereList = RulpUtil.asList(modifier.obj);
				break;

			case A_DO:
				doList = RulpUtil.asList(modifier.obj);
				break;

			// limit 1
			case A_Limit:
				limit = RulpUtil.asInteger(modifier.obj).asInteger();
				if (limit <= 0) {
					throw new RException("invalid value<" + modifier.obj + "> for modifier: " + modifier.name);
				}

				break;

			// reverse
			case A_Reverse:
				if (orderBuilder != null) {
					throw new RException(
							String.format("confilct modifier: %s and %s", modifier.name, orderBuilderName));
				}

				orderBuilder = REntryFactory.reverseBuilder();
				orderBuilderName = modifier.name;
				break;

			// order by
			case A_Order_by:
				if (orderBuilder != null) {
					throw new RException(
							String.format("confilct modifier: %s and %s", modifier.name, orderBuilderName));
				}

				if (condList == null) {
					throw new RException("need condList");
				}

				IRObject[] varEntry = ModelFactory.buildVarEntry(model, condList);
				Map<String, Integer> varIndexMap = new HashMap<>();
				int len = varEntry.length;
				for (int i = 0; i < len; ++i) {
					IRObject obj = varEntry[i];
					if (obj != null) {
						varIndexMap.put(obj.asString(), i);
					}
				}

				ArrayList<OrderEntry> orderEntrys = new ArrayList<>();
				Set<Integer> orderIndexs = new HashSet<>();

				for (IRObject o : RulpUtil.toList(RulpUtil.asList(modifier.obj).iterator())) {

					IRList ol = RulpUtil.asList(o);

					IRObject varObj = ol.get(0);
					IRObject ascObj = ol.get(1);

					if (!varIndexMap.containsKey(varObj.asString())) {
						throw new RException("unknown var: " + varObj);
					}

					int index = varIndexMap.get(varObj.asString());
					if (orderIndexs.contains(index)) {
						throw new RException("duplicated index: " + index);
					}

					boolean asc = true;
					switch (ascObj.asString()) {

					case A_Desc:
						asc = false;
						break;

					case A_Asc:
					case A_NIL:
						break;

					default:
						throw new RException("unknown asc: " + ascObj);
					}

					OrderEntry orderEntry = new OrderEntry();
					orderEntry.index = index;
					orderEntry.asc = asc;

					orderIndexs.add(index);
					orderEntrys.add(orderEntry);
				}

				if (orderEntrys.isEmpty()) {
					throw new RException("invalid order: " + modifier.obj);
				}

				orderBuilder = REntryFactory.orderByBuilder(orderEntrys);
				orderBuilderName = modifier.name;
				break;

			case A_Forward:
				if (backward != null && backward) {
					throw new RException(String.format("confilct modifier: %s", modifier.name));
				}

				backward = false;
				break;

			case A_Backward:
				if (backward != null && !backward) {
					throw new RException(String.format("confilct modifier: %s", modifier.name));
				}

				backward = true;
				break;

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		if (backward == null) {
			backward = true;
		}

		/********************************************/
		// Run as rule group
		/********************************************/
		IRNodeSubGraph subGraph = null;
		if (ruleGroupName != null) {
			subGraph = model.getNodeGraph().createSubGraphForRuleGroup(ruleGroupName);
		}

		/********************************************/
		// Build result queue
		/********************************************/
		IRResultQueue resultQueue = ModelFactory.createResultQueue(model, rstExpr, condList);

		// Add do expression
		if (doList != null) {
			for (IRObject doObj : RulpUtil.toArray(doList)) {
				resultQueue.addDoExpr(RulpUtil.asExpression(doObj));
			}
		}

		// Add constraint
		if (whereList != null) {

			IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(
					rstExpr.getType() == RType.LIST ? (IRList) rstExpr : RulpFactory.createList(rstExpr)));

			ConstraintBuilder cb = new ConstraintBuilder(varEntry);
			for (IRObject where : RulpUtil.toArray(whereList)) {
				IRConstraint1 cons = cb.build((IRExpr) where, interpreter, frame);
				if (cons != null) {
					resultQueue.addConstraint(cons);
				}
			}
		}

		// If there is an "order", which means query all possible result, and then order
		// the result, set the queryLimit to -1
		if (orderBuilder != null) {

			resultQueue.setOrderBuilder(orderBuilder);

			if (limit > 0) {
				resultQueue.setOrderLimit(limit);
				limit = -1;
			}
		}

		try {

			/********************************************/
			// Activate sub group
			/********************************************/
			if (subGraph != null) {
				subGraph.activate(model.getPriority());
			}

			model.query(resultQueue, condList, limit, backward);

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
