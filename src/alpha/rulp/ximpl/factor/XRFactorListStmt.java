package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_Order_by;
import static alpha.rulp.rule.Constant.A_Reverse;
import static alpha.rulp.rule.Constant.A_State;

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
				reverse = true;
				break;
				
			case A_Order_by:
				orderByList = RulpUtil.asList(modifier.obj);
				break;

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		return RulpFactory.createList(model.listStatements(filter, statusMask, limit, reverse));
	}
}
