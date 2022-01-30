//package alpha.rulp.ximpl.factor;
//
//import static alpha.rulp.lang.Constant.O_Nil;
//
//import alpha.rulp.lang.IRFrame;
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.RException;
//import alpha.rulp.rule.IRModel;
//import alpha.rulp.rule.IRRule;
//import alpha.rulp.runtime.IRInterpreter;
//import alpha.rulp.utils.RulpUtil;
//import alpha.rulp.utils.StmtUtil;
//import alpha.rulp.ximpl.model.IRuleFactor;
//
//public class XRFactorGetRule extends AbsAtomFactorAdapter implements IRuleFactor {
//
//	public XRFactorGetRule(String factorName) {
//		super(factorName);
//	}
//
//	@Override
//	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {
//
//		if (args.size() != 2 && args.size() != 3) {
//			throw new RException("Invalid parameters: " + args);
//		}
//
//		IRModel model = StmtUtil.getStmtModel(args, interpreter, frame, 3);
//		String ruleName = RulpUtil.asString(interpreter.compute(frame, StmtUtil.getStmt3Object(args))).asString();
//
//		/**************************************************/
//		// Check model object
//		/**************************************************/
//		IRRule rule = model.getNodeGraph().getRule(ruleName);
//		return rule == null ? O_Nil : rule;
//	}
//}
