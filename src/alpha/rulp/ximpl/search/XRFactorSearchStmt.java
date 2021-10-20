package alpha.rulp.ximpl.search;
import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.rule.Constant.A_Limit;

import java.util.ArrayList;
import java.util.List;

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
import alpha.rulp.utils.ModifiterUtil.ModifiterData;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.model.ModelFactory;

public class XRFactorSearchStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorSearchStmt(String factorName) {
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

		IRObject rstExpr = args.get(argIndex++);
		if (rstExpr.getType() != RType.EXPR && rstExpr.getType() != RType.LIST && !RulpUtil.isVarAtom(rstExpr)) {
			throw new RException("unsupport rstExpr: " + rstExpr);
		}

		List<IRList> fromList = new ArrayList<>();

		int limit = -1; // 0: all, -1: default

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
				limit = data.limit;
				if (limit <= 0) {
					throw new RException(
							"invalid value<" + limit + "> for modifier: " + processingModifier + ", args=" + args);
				}

				break;

			default:
				throw new RException("unsupport modifier: " + processingModifier);
			}
		}

		/********************************************/
		// Run as rule group
		/********************************************/
		IRList condList = RulpFactory.createList(fromList);
		IRResultQueue resultQueue = ModelFactory.createResultQueue(model, rstExpr, condList);

		try {

			model.query(resultQueue, condList, limit);
			return RulpFactory.createList(resultQueue.getResultList());

		} finally {

			resultQueue.close();
		}
	}

}
