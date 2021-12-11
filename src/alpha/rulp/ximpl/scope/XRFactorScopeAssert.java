//package alpha.rulp.ximpl.scope;
//
//import static alpha.rulp.lang.Constant.O_Nil;
//
//import alpha.rulp.lang.IRFrame;
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.RException;
//import alpha.rulp.runtime.IRInterpreter;
//import alpha.rulp.utils.RuleUtil;
//import alpha.rulp.utils.RulpUtil;
//import alpha.rulp.ximpl.factor.AbsRFactorAdapter;
//import alpha.rulp.ximpl.model.IRuleFactor;
//
//public class XRFactorScopeAssert extends AbsRFactorAdapter implements IRuleFactor {
//
//	public XRFactorScopeAssert(String factorName) {
//		super(factorName);
//	}
//
//	@Override
//	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {
//
//		/********************************************/
//		// Check parameters
//		// (scope::assert (= (% ?age 2) 0))
//		/********************************************/
//		int argSize = args.size();
//		if (argSize != 3 && argSize != 4) {
//			throw new RException("Invalid parameters: " + args);
//		}
//
//		IRScope scope = RuleUtil.asScope(interpreter.compute(frame, args.get(1)));
//
//		switch (argSize) {
//		case 3:
//			scope.addConstraint(RulpUtil.asExpression(interpreter.compute(frame, args.get(2))));
//			break;
//
//		case 4:
//			scope.addConstraint(RulpUtil.asExpression(interpreter.compute(frame, args.get(2))),
//					RulpUtil.asExpression(interpreter.compute(frame, args.get(3))));
//			break;
//
//		default:
//			throw new RException("Invalid parameters: " + args);
//		}
//
//		return O_Nil;
//	}
//
//}
