package alpha.rulp.ximpl.factor;

import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_Priority;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRMember;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRRunnable;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRSubNodeGraph;
import alpha.rulp.ximpl.model.IRuleFactor;
import alpha.rulp.ximpl.model.ModelUtil;

public class XRFactorStart extends AbsRFactorAdapter implements IRFactor, IRuleFactor {

	public XRFactorStart(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		IRRunnable runObj = null;

		int argIndex = 1;
		int argSize = args.size();
		int priority = -1;
		int limit = -1;
		String ruleGroupName = null;

		/**************************************************/
		// Check model object
		/**************************************************/
		if (argIndex < argSize) {

			IRObject argObj = args.get(argIndex);

			if (argObj.getType() == RType.MEMBER) {
				IRMember mbr = RulpUtil.asMember(argObj);
				runObj = RuleUtil.asModel(interpreter.compute(frame, mbr.getSubject()));
				ruleGroupName = mbr.getName();
				++argIndex;

			} else {
				argObj = interpreter.compute(frame, argObj);
				if (argObj instanceof IRRunnable) {
					runObj = (IRRunnable) argObj;
					++argIndex;
				}
			}

		}

		if (runObj == null) {
			runObj = RuleUtil.getDefaultModel(frame);
			if (runObj == null) {
				throw new RException("no model be specified");
			}
		}

		/********************************************/
		// Check modifier
		/********************************************/
		for (Modifier modifier : ModifiterUtil.parseModifiterList(args.listIterator(argIndex), frame)) {

			switch (modifier.name) {

			// priority 1
			case A_Priority:
				priority = RulpUtil.asInteger(modifier.obj).asInteger();
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

		/********************************************/
		// Run as rule group
		/********************************************/
		IRSubNodeGraph subGraph = null;
		if (ruleGroupName != null) {
			subGraph = ModelUtil.buildSubNodeGroupForRuleGroup((IRModel) runObj, ruleGroupName);
		}

		try {

			int step = runObj.start(priority, limit);
			return RulpFactory.createInteger(step);

		} finally {

			/********************************************/
			// Recovery all nodes' priority
			/********************************************/
			if (subGraph != null) {
				subGraph.rollback();
			}
		}

	}

}
