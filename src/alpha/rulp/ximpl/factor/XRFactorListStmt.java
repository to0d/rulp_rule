package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.rule.Constant.A_Limit;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.RModifiter;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.ModifiterData;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorListStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

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
		IRList stmtFilter = null;
		int statusMask = 0;
		int queryLimit = 0; // 0: all, -1: default
		int fromArgIndex = 1;

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
		ModifiterData data = ModifiterUtil.parseModifiterList(args.listIterator(fromArgIndex), interpreter, frame);
		for (RModifiter processingModifier : data.processedModifier) {

			switch (processingModifier) {

			// from '(a b c)
			case FROM:
				if (data.fromList.size() != 1 || data.fromList.get(0).getType() != RType.LIST) {
					throw new RException("invalid value<" + data.fromList + "> for modifier: " + A_FROM);
				}

				stmtFilter = data.fromList.get(0);
				break;

			case STATE:
				statusMask = data.state;
				break;

			// limit 1
			case LIMIT:
				queryLimit = data.limit;
				if (queryLimit <= 0) {
					throw new RException("invalid value<" + queryLimit + "> for modifier: " + A_Limit);
				}

				break;

			default:
				throw new RException("unsupport modifier: " + processingModifier);
			}
		}

		return RulpFactory.createList(model.listStatements(stmtFilter, statusMask, queryLimit));
	}
}
