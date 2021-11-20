package alpha.rulp.ximpl.mts;

import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_Order_by;

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
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.model.ModelFactory;
import alpha.rulp.ximpl.node.IRNamedNode;

public class XRFactorSearchModel extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorSearchModel(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		// - (search-stmt m '(?x) from '(stmt) limit 1)
		/********************************************/
		int argSize = args.size();
		if (argSize < 5) {
			throw new RException("Invalid parameters: " + args);
		}

		IRModel model = null;
		int argIndex = 1;

		/**************************************************/
		// Check model object
		/**************************************************/
		{
			IRObject argObj = args.get(argIndex);
			if (argObj.getType() != RType.LIST) {
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

		/********************************************/
		// Check result expression
		/********************************************/
		IRObject rstExpr = args.get(argIndex++);
		if (rstExpr.getType() != RType.LIST || RulpUtil.asList(rstExpr).getNamedName() == null) {
			throw new RException("unsupport search expr: " + rstExpr);
		}

		IRList fromList = null;
		IRList orderByList = null;
		int limit = -1; // 0: all, -1: default

		/********************************************/
		// Check modifier
		/********************************************/

		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame)) {

			switch (modifier.name) {

			// from '(a b c) (factor)
			case A_FROM:
				fromList = RulpUtil.asList(modifier.obj);
				break;

			// limit 1
			case A_Limit:
				limit = RulpUtil.asInteger(modifier.obj).asInteger();
				if (limit <= 0) {
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

		return MTSUtil.createSearchModel(model, (IRList) rstExpr, fromList);

		/********************************************/
		// Run as rule group
		/********************************************/
//		IRResultQueue resultQueue = ModelFactory.createResultQueue(model, rstExpr, fromList);

//		try {
//
////			model.query(resultQueue, fromList, limit);
////			return RulpFactory.createList(resultQueue.getResultList());
//
//		} finally {
//
//			resultQueue.close();
//		}
	}

}
