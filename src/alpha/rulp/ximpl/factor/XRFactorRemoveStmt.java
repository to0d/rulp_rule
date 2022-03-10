package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.lang.Constant.O_QUESTION_LIST;
import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_Desc;
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
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.ReteUtil.OrderEntry;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.ConstraintBuilder;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryIteratorBuilder;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.REntryFactory;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.model.ModelFactory;

public class XRFactorRemoveStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	static IRList _remove(IRModel model, IRList filter, int limit, boolean reverse, IREntryIteratorBuilder builder)
			throws RException {

		IREntryTable entryTable = model.getEntryTable();
		ArrayList<IRReteEntry> list = new ArrayList<>();

		model.listStatements(filter, RReteStatus.RETE_STATUS_MASK_NOT_DELETED, limit, reverse, builder, (entry) -> {
			list.add(entry);
			entryTable.removeEntry(entry);
			return true;
		});

		return RulpFactory.createList(list);
	}

	static IRList _remove(IRModel model, IRList filter, int limit, boolean reverse, IREntryIteratorBuilder builder,
			IRResultQueue resultQueue) throws RException {

		IREntryTable entryTable = model.getEntryTable();

		model.listStatements(filter, RReteStatus.RETE_STATUS_MASK_NOT_DELETED, limit, reverse, builder, (entry) -> {

			if (!resultQueue.addEntry(entry)) {
				return false;
			}

			entryTable.removeEntry(entry);
			return true;
		});

		return RulpFactory.createList(resultQueue.getResultList());
	}

	public XRFactorRemoveStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		// (remove-stmt m '(a b c)), return true or false
		// (remove-stmt m from '(?a ?b ?c) where (> ?a 10)), return the deleted list

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();
		IRModel model = null;
		int fromArgIndex = 1;

		/**************************************************/
		// Check model object
		/**************************************************/
		if (argSize >= 2) {
			IRObject obj = interpreter.compute(frame, args.get(fromArgIndex));
			if (obj instanceof IRModel) {
				model = (IRModel) obj;
				fromArgIndex++;
			}
		}

		/********************************************/
		// Check default model
		/********************************************/
		if (model == null) {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		}

		// Remove single statement, (remove-stmt m '(a b c))
		if ((fromArgIndex + 1) == argSize) {

			IRList stmt = RulpUtil.asList(interpreter.compute(frame, args.get(fromArgIndex)));

			if (!ReteUtil.isReteStmtNoVar(stmt)) {

				// there maybe external var in the expression
				stmt = RulpUtil.asList(interpreter.compute(frame, stmt));
				if (!ReteUtil.isReteStmtNoVar(stmt)) {
					throw new RException("invalid stmt: " + stmt);
				}
			}

			return RulpFactory.createBoolean(model.removeStatement(stmt));
		}

		/********************************************/
		// Check modifier
		/********************************************/
		IRList filter = null;
		int limit = 0; // 0: all, -1: default
		boolean reverse = false;
		IREntryIteratorBuilder builder = null;
		String builderName = null;
		IRList whereList = null;

		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(fromArgIndex), frame)) {

			switch (modifier.name) {

			// from '(a b c)
			case A_FROM:
				IRList fromList = RulpUtil.asList(modifier.obj);
				if (fromList.size() != 1 || fromList.get(0).getType() != RType.LIST) {
					throw new RException("invalid value<" + modifier.obj + "> for modifier: " + modifier.name);
				}

				filter = RulpUtil.asList(fromList.get(0));
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
				if (builder != null) {
					throw new RException(String.format("confilct modifier: %s and %s", modifier.name, builderName));
				}

				reverse = true;
				builder = REntryFactory.reverseBuilder();
				builderName = modifier.name;
				break;

			// order by
			case A_Order_by:
				if (builder != null) {
					throw new RException(String.format("confilct modifier: %s and %s", modifier.name, builderName));
				}

				if (filter == null) {
					throw new RException("need filter modifier");
				}

				Map<String, Integer> varIndexMap = new HashMap<>();
				int len = filter.size();
				for (int i = 0; i < len; ++i) {
					IRObject obj = filter.get(i);
					if (RulpUtil.isVarAtom(obj)) {
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

				builder = REntryFactory.orderByBuilder(orderEntrys);
				builderName = modifier.name;
				break;

			case A_Where:
				whereList = RulpUtil.asList(modifier.obj);
				break;

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		/********************************************/
		// Build result queue
		/********************************************/
		IRResultQueue resultQueue = null;
		if (whereList != null) {
			resultQueue = ModelFactory.createResultQueue(model, O_QUESTION_LIST, RulpFactory.createList(filter));

			IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(
					filter.getType() == RType.LIST ? (IRList) filter : RulpFactory.createList(filter)));

			ConstraintBuilder cb = new ConstraintBuilder(varEntry);
			for (IRObject where : RulpUtil.toArray(whereList)) {
				IRConstraint1 cons = cb.build((IRExpr) where, interpreter, frame);
				if (cons != null) {
					resultQueue.addConstraint(cons);
				}
			}
		}

		if (resultQueue != null) {
			return _remove(model, filter, limit, reverse, builder, resultQueue);
		} else {
			return _remove(model, filter, limit, reverse, builder);
		}

	}
}
