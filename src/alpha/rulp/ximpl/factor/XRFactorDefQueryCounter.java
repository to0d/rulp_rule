package alpha.rulp.ximpl.factor;

//package alpha.rulp.ximpl.rule.factor;
//
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRNative;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.rule.IRModel;
//import alpha.rulp.runtime.IRFactor;
//import alpha.rulp.runtime.IRFrame;
//import alpha.rulp.runtime.IRInterpreter;
//import alpha.rulp.runtime.IRVar;
//import alpha.rulp.runtime.RException;
//import alpha.rulp.utility.RulpFactory;
//import alpha.rulp.utils.*;
//import alpha.rulp.ximpl.rule.IRuleFactor;
//import alpha.rulp.ximpl.runtime.factor.AbsRFactorAdapter;
//
//public class XRFactorDefQueryCounter extends AbsRFactorAdapter implements IRFactor, IRuleFactor {
//
//	public XRFactorDefQueryCounter(String factorName) {
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
//			throw new RException("Invalid parameters: " + args);
//		}
//
//		IRModel model = RulpUtil.asModel(interpreter.compute(frame, args.get(1)));
//		String varName = RulpUtil.asAtom(args.get(2)).getName();
//
//		IRNative counter = RulpFactory.createNative(model.createQueryCounter());
//
//		/********************************************/
//		// defvar counter as native object
//		/********************************************/
//		IRVar var = RulpFactory.createVar(varName);
//		var.setValue(counter);
//		frame.setEntry(varName, var);
//
//		return counter;
//	}
//}
