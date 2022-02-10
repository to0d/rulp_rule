package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_Desc;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_Order_by;
import static alpha.rulp.rule.Constant.A_Reverse;
import static alpha.rulp.rule.Constant.A_State;

import java.util.ArrayList;
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
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IREntryIteratorBuilder;
import alpha.rulp.ximpl.entry.REntryFactory;
import alpha.rulp.ximpl.entry.XREntryIteratorBuilderOrderBy.OrderEntry;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorListStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorListStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		/********************************************/
		int argSize = args.size();

		IRModel model = null;
		IRList filter = null;
		int statusMask = 0;
		int limit = 0; // 0: all, -1: default
		int fromArgIndex = 1;
		boolean reverse = false;
		IRList orderByList = null;

		IREntryIteratorBuilder builder = null;
		String builderName = null;

		/**************************************************/
		// Check model object
		/**************************************************/
		if (argSize >= 2) {
			IRObject obj = interpreter.compute(frame, args.get(1));
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

		/********************************************/
		// Check modifier
		/********************************************/
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

			case A_State:
				statusMask = RulpUtil.asInteger(modifier.obj).asInteger();
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

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		return RulpFactory.createList(model.listStatements(filter, statusMask, limit, reverse, builder));
	}
}
