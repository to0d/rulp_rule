//package alpha.rulp.ximpl.factor;
//
//import alpha.rulp.lang.IRFrame;
//import alpha.rulp.lang.IRFrameEntry;
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.RException;
//import alpha.rulp.rule.IRModel;
//import alpha.rulp.runtime.IRFactor;
//import alpha.rulp.runtime.IRInterpreter;
//import alpha.rulp.utils.RuleFactory;
//import alpha.rulp.utils.RulpUtil;
//import alpha.rulp.ximpl.model.IRuleFactor;
//
//public class XRFactorDefModel extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {
//
//	public XRFactorDefModel(String factorName) {
//		super(factorName);
//	}
//
//	@Override
//	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {
//
//		if (args.size() != 2) {
//			throw new RException("Invalid parameters: " + args);
//		}
//
//		String modelName = RulpUtil.asAtom(args.get(1)).getName();
//
//		/******************************************/
//		// Check instance exist
//		/******************************************/
//		{
//			IRFrameEntry oldEntry = frame.getEntry(modelName);
//			if (oldEntry != null) {
//
//				IRObject val = oldEntry.getObject();
//				if (val instanceof IRModel) {
//					return val;
//				}
//
//				throw new RException(String.format("duplicate name<%s> found, unable to create model: %s", modelName,
//						oldEntry.getObject()));
//			}
//		}
//
//		IRModel model = RuleFactory.createModel(modelName, interpreter, frame);
//
//		/******************************************/
//		// Add into frame
//		/******************************************/
//		frame.setEntry(modelName, model);
//
//		return model;
//	}
//
//}
