package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.lang.Constant.O_Nil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorComputeStmt extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorComputeStmt(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		/********************************************/
		// Check parameters
		// - (compute-stmt m ?v from n1:'(a ?v) backward)
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

		IRObject varObj = interpreter.compute(frame, args.get(argIndex++));
		if (varObj instanceof IRModel) {

			model = (IRModel) varObj;
			varObj = args.get(argIndex++);

		} else {

			model = RuleUtil.getDefaultModel(frame);
			if (model == null) {
				throw new RException("no model be specified");
			}

			varObj = args.get(1);
		}

		if (!RulpUtil.isVarAtom(varObj)) {
			throw new RException("unsupport var object: " + varObj);
		}

		IRList condList = null;
		Set<String> varUniqNames = new HashSet<>();
		ReteUtil.buildVarList(varObj, new ArrayList<>(), varUniqNames);

		/********************************************/
		// Check modifier
		/********************************************/
		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame, varUniqNames)) {

			switch (modifier.name) {

			// from '(a b c) (factor)
			case A_FROM:
				condList = RulpUtil.asList(modifier.obj);
				break;

			default:
				throw new RException("unsupport modifier: " + modifier.name);
			}
		}

		/********************************************/
		// Check root, named, alpha node directly
		/********************************************/
		if (condList.size() == 1 && ReteUtil.isReteStmt(condList.get(0))) {

			IRList cond = RulpUtil.asList(condList.get(0));

			/********************************************/
			// Locate the var index
			/********************************************/
			int size = cond.size();
			int varIndex = -1;

			for (int i = 0; i < size; ++i) {
				if (RulpUtil.equal(cond.get(i), varObj)) {
					varIndex = i;
					break;
				}
			}

			if (varIndex == -1) {
				throw new RException("invalid var: " + varObj);
			}

			IRReteEntry entry = model.findReteEntry(cond);
			if (entry != null) {
				return entry.get(varIndex);
			}

			return O_Nil;
		}

		throw new RException("unsupport yet");
	}
}
