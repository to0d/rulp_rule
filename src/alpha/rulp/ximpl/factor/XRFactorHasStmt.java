package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_BackSearch;
import static alpha.rulp.rule.Constant.A_Desc;
import static alpha.rulp.rule.Constant.A_Order_by;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.XREntryQueueOrder;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.XTempVarBuilder;

public class XRFactorHasStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorHasStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		// - (has-stmt m '(a b c) back-search order by 1)
		/********************************************/
		int argSize = args.size();
		if (argSize < 2) {
			throw new RException("Invalid parameters: " + args);
		}

		/**************************************************/
		// Check model object
		/**************************************************/
		int argIndex = 1;
		IRModel model = null;
		IRObject obj = interpreter.compute(frame, args.get(argIndex));
		if (obj instanceof IRModel) {
			model = (IRModel) obj;
			obj = null;
			argIndex++;
		} else {
			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}
		}

		/**************************************************/
		// Check statement
		/**************************************************/
		if (obj == null) {
			obj = interpreter.compute(frame, args.get(argIndex));
		}

		IRList stmt = RulpUtil.asList(obj);
		argIndex++;

		boolean backSearch = false;
		List<OrderEntry> orderList = null;

		/********************************************/
		// Check modifier
		/********************************************/
		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame)) {

			switch (modifier.name) {
			case A_BackSearch:
				backSearch = true;
				if (!ReteUtil.isReteStmtNoVar(stmt)) {
					throw new RException("unsupport var in back search: " + stmt);
				}
				break;

			case A_Order_by:

				IRList orderObj = RulpUtil.asList(RulpUtil.asList(modifier.obj).get(0));

				int index = RulpUtil.asInteger(orderObj.get(0)).asInteger();
				boolean asc = true;

				switch (orderObj.get(1).asString()) {
				case A_Asc:
					asc = true;
					break;

				case A_Desc:
					asc = false;
					break;

				case A_NIL:
					asc = true;
					break;

				default:
					throw new RException("unsupport modifier: " + modifier.obj);
				}

				OrderEntry order = new OrderEntry();
				order.index = index;
				order.asc = asc;

				if (orderList == null) {
					orderList = new ArrayList<>();
				}
				orderList.add(order);

				break;

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		/********************************************/
		// back search
		/********************************************/
		if (backSearch) {

			if (orderList != null) {
				throw new RException("does not support order in back search: " + args);
			}

			return RulpFactory.createBoolean(model.hasStatement(stmt));
		}

		/********************************************/
		// create index and query
		/********************************************/
		if (orderList != null && ReteUtil.isIndexStmt(stmt)) {
			return RulpFactory.createBoolean(hasStatementInIndex(model, stmt, orderList));
		}

		return RulpFactory.createBoolean(model.hasStatement(stmt));
	}

	static class XCount {
		public int count = 0;
	}

	static boolean hasStatementInIndex(IRModel model, IRList stmt, List<OrderEntry> orderList) throws RException {

		List<IRObject> newStmtArr = RulpUtil.toArray(stmt);
		XTempVarBuilder varBuilder = new XTempVarBuilder();

		for (OrderEntry order : orderList) {

			int index = order.index;
			IRObject oldObj = newStmtArr.get(index);

			if (RulpUtil.isVarAtom(oldObj)) {
				throw new RException("invalid index index: " + index + ", stmt=" + stmt);
			}

			newStmtArr.set(index, varBuilder.next());
		}

		IRList newStmt = null;
		if (stmt.getNamedName() == null) {
			newStmt = RulpFactory.createList(newStmtArr);
		} else {
			newStmt = RulpFactory.createNamedList(newStmtArr, stmt.getNamedName());
		}

		IRNodeGraph graph = model.getNodeGraph();
		IRReteNode indexNode = graph.buildIndex(graph.getNodeByTree(newStmt), orderList);

		// '(?a b ?c) ==> (a b ?c)
		XREntryQueueOrder orderQueue = (XREntryQueueOrder) indexNode.getEntryQueue();
		IRReteEntry entry = orderQueue.find(stmt);
		if (entry != null) {
			return true;
		}

		XCount xcount = new XCount();

		RuleUtil.travelReteParentNodeByPostorder(indexNode, (node) -> {
			if (model.execute(node) > 0) {
				xcount.count++;
			}
			return false;
		});

		if (xcount.count == 0) {
			return false;
		}

		entry = orderQueue.find(stmt);
		return entry != null;
	}

}
