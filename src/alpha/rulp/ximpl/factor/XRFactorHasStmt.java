package alpha.rulp.ximpl.factor;

import static alpha.rulp.rule.Constant.A_BackSearch;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorHasStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorHasStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		// - (has-stmt m '(a b c) full-search)
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

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		/********************************************/
		// Simple list
		/********************************************/
		if (!backSearch) {
			return RulpFactory.createBoolean(model.hasStatement(stmt));
		}

		return RulpFactory.createBoolean(model.hasStatement(stmt));
	}
}
