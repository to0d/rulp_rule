package alpha.rulp.ximpl.scope;

import static alpha.rulp.rule.Constant.A_SCOPE;

import alpha.rulp.lang.IRClass;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RAccessType;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.factor.AbsRFactorAdapter;

public class ScopeFactory {

	static final String F_MBR_SCOPE_ASSERT = "_scope_assert";

	static final String F_MBR_SCOPE_DEFINE = "_scope_define";

	static final String F_MBR_SCOPE_INIT = "_scope_init";

	static final String F_MBR_SCOPE_MODEL = "?default-scope-model";

	static final String F_MBR_SCOPE_QUERY = "_scope_query";

	public static IVarConstraint createExprConstraint(IRModel model, IRExpr expr) {
		return new XVarConstraint(model, expr);
	}

	public static IValueListFactory createIntValueFactory(IRObject fromValue, IRObject toValue, IRObject stepValue)
			throws RException {

		int fromVal = RulpUtil.asInteger(fromValue).asInteger();
		int toVal = RulpUtil.asInteger(toValue).asInteger();

		if (fromVal == toVal) {
			return createSingleValueFactory(fromValue);
		}

		int stepVal;

		if (fromVal < toVal) {

			stepVal = 1;
			if (stepValue != null) {
				stepVal = RulpUtil.asInteger(stepValue).asInteger();
			}

			if (stepVal <= 0) {
				throw new RException(
						String.format("invalid step value: from=%s, to=%s, step=%s", fromValue, toValue, stepValue));
			}
		}
		// if (fromVal < toVal) {
		else {

			stepVal = -1;
			if (stepValue != null) {
				stepVal = RulpUtil.asInteger(stepValue).asInteger();
			}

			if (stepVal >= 0) {
				throw new RException(
						String.format("invalid step value: from=%s, to=%s, step=%s", fromValue, toValue, stepValue));
			}
		}

		if (stepVal == 0) {
			return createSingleValueFactory(fromValue);
		}

		return new XIntValueList(fromVal, toVal, stepVal);
	}

	public static IValueListFactory createListValueFactory(IRList values) throws RException {
		return new XListValueList(values);
	}

	public static IValueListFactory createSingleValueFactory(IRObject value) {
		return new XSingleValueList(value);
	}

	public static void initScopeClass(IRFrame mainFrame) throws RException {

		IRClass scopeClass = RulpUtil.asClass(mainFrame.getEntry(A_SCOPE).getValue());

		/********************************************************/
		// init factor
		/********************************************************/
		RulpUtil.setMember(scopeClass, F_MBR_SCOPE_INIT, new AbsRFactorAdapter(F_MBR_SCOPE_INIT) {

			@Override
			public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

				if (args.size() != 2 && args.size() != 3) {
					throw new RException("Invalid parameters: " + args);
				}

				String scopeName = RulpUtil.asInstance(interpreter.compute(frame, args.get(1))).getSubjectName();

				IRInstance modelInstance = null;

				if (args.size() == 3) {
					modelInstance = RulpUtil.asInstance(interpreter.compute(frame, args.get(2)));
				} else {
//					modelInstance = CLASS_MODEL.newInstance(scopeName, RulpFactory.createList(), interpreter,
//							RulpFactory.createFrame(frame, "SCOPE"));
				}

				IRModel model = RuleUtil.asModel(modelInstance);

				XRScope scope = new XRScope(scopeName, model.getFrame(), model, RuntimeUtil.getNoClass(interpreter));

				RulpUtil.setMember(scope, F_MBR_SCOPE_MODEL, modelInstance, RAccessType.PUBLIC);

				return scope;
			}

			@Override
			public boolean isThreadSafe() {
				return true;
			}

		}, RAccessType.PRIVATE);

		/**************************************************************/
		// add factor for define
		// - (scope::define ?var ?type ?from ?to)
		/**************************************************************/
		RulpUtil.setMember(scopeClass, F_MBR_SCOPE_DEFINE, new XRFactorScopeDefine(F_MBR_SCOPE_DEFINE),
				RAccessType.PRIVATE);

		/**************************************************************/
		// add factor for assert
		// - (scope::assert (a expression))
		/**************************************************************/
		RulpUtil.setMember(scopeClass, F_MBR_SCOPE_ASSERT, new XRFactorScopeAssert(F_MBR_SCOPE_ASSERT),
				RAccessType.PRIVATE);

		/**************************************************************/
		// add factor for query
		// - (scope::query '(a var list) query-count)
		/**************************************************************/
		RulpUtil.setMember(scopeClass, F_MBR_SCOPE_QUERY, new XRFactorScopeQuery(F_MBR_SCOPE_QUERY),
				RAccessType.PRIVATE);
	}

}
