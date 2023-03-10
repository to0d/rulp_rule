package alpha.rulp.ximpl.bs;

import static alpha.rulp.rule.Constant.A_DEEP_FIRST;
import static alpha.rulp.rule.Constant.A_Explain;
import static alpha.rulp.rule.Constant.A_Limit;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorBackSearch extends AbsAtomFactorAdapter implements IRuleFactor {

	public XRFactorBackSearch(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		// (back-search m '(a b c))
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
			obj = args.get(argIndex);
		}

		if (!BSUtil.isBSTree(obj)) {
			throw new RException("Invalid parameters: " + args);
		}

		argIndex++;

		boolean explain = false;
		BSSearchType st = null;
		int limit = -1; // 0: all, -1: default

		/********************************************/
		// Check modifier
		/********************************************/
		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame)) {

			switch (modifier.name) {

			case A_Explain:
				explain = true;
				break;

			case A_DEEP_FIRST:
				st = BSSearchType.DFS;
				break;

			// limit 1
			case A_Limit:
				limit = RulpUtil.asInteger(modifier.obj).asInteger();
				if (limit <= 0) {
					throw new RException("invalid value<" + modifier.obj + "> for modifier: " + modifier.name);
				}

				break;

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		return BSUtil.backSearch(model, (IRList) obj, st, explain);
	}

}
