package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.F_IF;

import java.util.ArrayList;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRMember;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorAddRule extends AbsRFactorAdapter implements IRuleFactor {

	public XRFactorAddRule(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		// syntax: (add-rule ["name"] model rule-body)
		// rule-body: if cond1 cond2 .. do action1 action2
		int size = args.size();
		if (size < 5) {
			throw new RException("Invalid parameters: " + args);
		}

		try {

			String ruleName = null;
			String ruleGroupName = null;
			IRModel model = null;
			int argIndex = 1;

			IRObject argObj = args.get(argIndex);
			/**************************************************/
			// Check rule name
			/**************************************************/
			if (argObj.getType() == RType.STRING) {
				ruleName = RulpUtil.asString(argObj).asString();
				++argIndex;
			}

			/**************************************************/
			// Check model object
			/**************************************************/
			argObj = args.get(argIndex);
			if (argObj.getType() == RType.MEMBER) {

				IRMember mbr = RulpUtil.asMember(argObj);
				model = RuleUtil.asModel(interpreter.compute(frame, mbr.getSubject()));
				ruleGroupName = mbr.getName();

				++argIndex;

			} else {

				argObj = interpreter.compute(frame, argObj);
				if (argObj instanceof IRModel) {
					model = (IRModel) argObj;
					++argIndex;
				} else {
					model = RuleUtil.getDefaultModel(frame);
				}

				if (model == null) {
					throw new RException("no model be specified");
				}
			}

			/**************************************************/
			// Check "IF"
			/**************************************************/
			argObj = interpreter.compute(frame, args.get(argIndex++));
			if (!argObj.asString().equals(F_IF)) {
				throw new RException("expect if: " + argObj);
			}

			/**************************************************/
			// Check condition tree
			/**************************************************/
			ArrayList<IRObject> condObjs = new ArrayList<>();
			while (argIndex < size) {
				IRObject cond = args.get(argIndex++);
				if (RulpUtil.isAtom(cond, A_DO)) {
					break;
				}
				condObjs.add(cond);
			}

			IRList condList = RulpFactory.createExpression(condObjs);
			IRList actionList = RulpFactory.createList(args.listIterator(argIndex));
			IRRule rule = model.addRule(ruleName, condList, actionList);
			rule.setRuleDecription(RulpUtil.toString(args));

			/**************************************************/
			// Set rule group
			/**************************************************/
			if (ruleGroupName != null) {
				RuleUtil.addRuleToGroup(model, rule, ruleGroupName);
				model.getNodeGraph().setRulePriority(rule, 0);
			}

			return rule;

		} catch (RException e) {
			e.addMessage(String.format("fail to add rule<%s>", "" + args));
			throw e;
		}

	}
}
