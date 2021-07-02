//package alpha.rulp.ximpl.factor;
//
//import alpha.rulp.lang.IRFrame;
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.RException;
//import alpha.rulp.rule.IRModel;
//import alpha.rulp.runtime.IRFactor;
//import alpha.rulp.runtime.IRInterpreter;
//import alpha.rulp.utility.RuleUtility;
//import alpha.rulp.utility.RulpFactory;
//import alpha.rulp.utils.*;
//import alpha.rulp.ximpl.model.IRuleFactor;
//
//public class XRFactorUpdateStmt extends AbsRFactorAdapter implements IRFactor, IRuleFactor {
//
//	public XRFactorUpdateStmt(String factorName) {
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
//		if (argSize != 3) {
//			throw new RException("need more statmt");
//		}
//
//		IRModel model = RuleUtility.asModel(interpreter.compute(frame, args.get(1)));
//
//		return RulpFactory.createInteger(model.updateStatements(RulpUtil.asExpression(args.get(2))));
//	}
//}
