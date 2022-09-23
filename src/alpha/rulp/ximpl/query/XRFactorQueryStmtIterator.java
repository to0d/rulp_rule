package alpha.rulp.ximpl.query;

import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.rule.Constant.A_Backward;
import static alpha.rulp.rule.Constant.A_Forward;
import static alpha.rulp.rule.Constant.A_Limit;

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
import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorQueryStmtIterator extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorQueryStmtIterator(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		// - (query-stmt m from '(stmt) limit 1)
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

		IRList condList = null;
		int limit = -1; // 0: all, -1: default

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

			// limit 1
			case A_Limit:
				limit = RulpUtil.asInteger(modifier.obj).asInteger();
				if (limit <= 0) {
					throw new RException("invalid value<" + modifier.obj + "> for modifier: " + modifier.name);
				}

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

		return RulpFactory.createObjectIterator(model.query(condList, limit, backward));
	}

}
