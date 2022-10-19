package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_NIL;
import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_Desc;
import static alpha.rulp.rule.Constant.A_Order_by;
import static alpha.rulp.rule.Constant.F_HAS_STMT;

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
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorHasStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public static boolean isSimpleHasStmtExpr(IRExpr expr) throws RException {

		int size = expr.size();
		if (!RulpUtil.isFactor(expr.get(0), F_HAS_STMT)) {
			return false;
		}

		// (has-stmt '(?a ?b ?c))
		if (size == 2) {
			return expr.get(1).getType() == RType.LIST;
		}

		if (size == 3) {
			return expr.get(2).getType() == RType.LIST;
		}

		return false;
	}

	public XRFactorHasStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		// - (has-stmt m '(a b c) order by 1)
		/********************************************/
		int argSize = args.size();
		if (argSize < 2) {
			throw new RException("Invalid parameters: " + args);
		}

		/**************************************************/
		// Check model object
		/**************************************************/
		boolean useDefaultModel = false;
		int argIndex = 1;
		IRModel model = null;
		IRObject obj = interpreter.compute(frame, args.get(argIndex));
		if (obj instanceof IRModel) {
			model = (IRModel) obj;
			obj = null;
			argIndex++;
		} else {
			model = RuleUtil.getDefaultModel(frame);
			useDefaultModel = true;
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

//		boolean backSearch = false;
		List<OrderEntry> orderList = null;

		/********************************************/
		// Check modifier
		/********************************************/
		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame)) {

			switch (modifier.name) {
//			case A_BackSearch:
//				backSearch = true;
//				if (!ReteUtil.isReteStmtNoVar(stmt)) {
//					throw new RException("unsupport var in back search: " + stmt);
//				}
//				break;

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

//		/********************************************/
//		// back search
//		/********************************************/
//		if (backSearch) {
//
//			if (orderList != null) {
//				throw new RException("does not support order in back search: " + args);
//			}
//
//			return RulpFactory.createBoolean(model.hasStatement(stmt));
//		}

		/********************************************/
		// before: (has-stmt '(?x p1 ?y))
		// after : (has-stmt '(a p1 ?y))
		//
		/********************************************/
		if (orderList == null && model.getTopExecuteNode() != null) {

			IRObject oldStmtObj = useDefaultModel ? args.get(1) : args.get(2);
			if (oldStmtObj != stmt && oldStmtObj.getType() == RType.LIST) {

				IRList oldStmt = RulpUtil.asList(oldStmtObj);
				if (oldStmt.size() == stmt.size()) {

				}

				System.out.println();

				// (defun f1 (?x)
				// (return (has-stmt '(?x p1 ?y)))
				// )

			}
		}

		/********************************************/
		// create index and query
		/********************************************/
		if (orderList != null && ReteUtil.supportIndexStmt(stmt)) {
			return RulpFactory.createBoolean(model.hasStatement(stmt, orderList));
		}

		return RulpFactory.createBoolean(model.hasStatement(stmt));
	}

}
