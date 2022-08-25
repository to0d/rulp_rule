package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.rule.Constant.A_Asc;
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
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.node.IRNodeGraph;

public class XRFactorAddIndex extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	static void _addOrderIndex(Modifier modifier, List<OrderEntry> orderList, IRList nodeExpr, IRObject orderObj,
			boolean asc) throws RException {

		int index = -1;

		if (!RulpUtil.isVarAtom(orderObj)) {
			throw new RException("unsupport modifier: " + modifier.obj);
		}

		for (int i = 0; i < nodeExpr.size(); ++i) {
			if (RulpUtil.equal(orderObj, nodeExpr.get(i))) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			throw new RException("unsupport modifier: " + modifier.obj);
		}

		OrderEntry order = new OrderEntry();
		order.index = index;
		order.asc = asc;
		orderList.add(order);
	}

	public XRFactorAddIndex(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		int argSize = args.size();

		// (add-index m '(?a ?b c) order by '(?b ?a))
		/********************************************/
		// Check parameters
		/********************************************/
		if (argSize < 5) {
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

		IRList nodeExpr = RulpUtil.asList(interpreter.compute(frame, args.get(argIndex++)));

		/********************************************/
		// Check index expression
		/********************************************/
		if (!ReteUtil.supportIndexStmt(nodeExpr)) {
			throw new RException(String.format("Invalid index expr: %s", args));
		}

		List<OrderEntry> orderList = new ArrayList<>();

		/********************************************/
		// Check modifier
		/********************************************/
		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame)) {

			switch (modifier.name) {
			case A_Order_by:

				IRList orderEntryObj = RulpUtil.asList(RulpUtil.asList(modifier.obj).get(0));

				boolean asc = true;
				switch (orderEntryObj.get(1).asString()) {
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

				IRObject orderObj = orderEntryObj.get(0);

				switch (orderObj.getType()) {
				case ATOM:
					_addOrderIndex(modifier, orderList, nodeExpr, orderObj, asc);
					break;

				case LIST:
					IRList orderObjList = (IRList) orderObj;
					IRIterator<? extends IRObject> it = orderObjList.iterator();
					while (it.hasNext()) {
						_addOrderIndex(modifier, orderList, nodeExpr, it.next(), asc);
					}
					break;

				default:
					throw new RException("unsupport modifier: " + modifier.obj);
				}

				break;

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		if (orderList.isEmpty()) {
			throw new RException(String.format("Invalid order expr: %s", args));
		}

		IRNodeGraph graph = model.getNodeGraph();
		IRReteNode node = graph.createNodeByTree(nodeExpr);

		return graph.createNodeIndex(node, orderList);
	}
}
