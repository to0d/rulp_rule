package alpha.rulp.ximpl.factor;

//package alpha.rulp.ximpl.rule.factor;
//
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.RException;
//import alpha.rulp.rule.IRModel;
//import alpha.rulp.runtime.IRFactor;
//import alpha.rulp.runtime.IRFrame;
//import alpha.rulp.runtime.IRInterpreter;
//import alpha.rulp.utility.RuleUtility;
//import alpha.rulp.utility.RulpFactory;
//import alpha.rulp.utils.*;
//import alpha.rulp.ximpl.model.IRuleFactor;
//import alpha.rulp.ximpl.runtime.factor.AbsRFactorAdapter;
//
//public class XRFactorQueryFirstStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {
//
//	public XRFactorQueryFirstStmt(String factorName) {
//		super(factorName);
//	}
//
//	@Override
//	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {
//
//		/********************************************/
//		// Check parameters
//		/********************************************/
//		int argSize = args.size();
//		if (argSize != 4) {
//			throw new RException("need more stmt");
//		}
//
//		IRModel model = RuleUtility.asModel(interpreter.compute(frame, args.get(1)));
//		IRObject rst = args.get(2);
//		IRList condList = RulpUtil.asExpression(args.get(3));
//
//		return RulpFactory.createList(model.queryFirstStatement(condList, rst));
//	}
//}
