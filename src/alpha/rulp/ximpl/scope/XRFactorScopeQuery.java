//package alpha.rulp.ximpl.scope;
//
//import static alpha.rulp.rule.Constant.A_Limit;
//
//import java.util.ArrayList;
//
//import alpha.rulp.lang.IRAtom;
//import alpha.rulp.lang.IRFrame;
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.RException;
//import alpha.rulp.runtime.IRInterpreter;
//import alpha.rulp.runtime.IRIterator;
//import alpha.rulp.utils.RuleUtil;
//import alpha.rulp.utils.RulpFactory;
//import alpha.rulp.utils.RulpUtil;
//import alpha.rulp.ximpl.factor.AbsAtomFactorAdapter;
//import alpha.rulp.ximpl.model.IRuleFactor;
//
//public class XRFactorScopeQuery extends AbsAtomFactorAdapter implements IRuleFactor {
//
//	public XRFactorScopeQuery(String factorName) {
//		super(factorName);
//	}
//
//	@Override
//	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {
//
//		/********************************************/
//		// Check parameters
//		// - (scope::query '(var-list) '(limit n))
//		// - (scope::query '(var-list) '() )
//		/********************************************/
//		int argSize = args.size();
//		if (argSize != 4) {
//			throw new RException("Invalid parameters: " + args);
//		}
//
//		// args(1) is the object itself
//		IRScope scope = RuleUtil.asScope(interpreter.compute(frame, args.get(1)));
//		IRList varList = RulpUtil.asList(interpreter.compute(frame, args.get(2)));
//		IRList modifierList = RulpUtil.asList(interpreter.compute(frame, args.get(3)));
//
//		int queryCount = -1; // 0: all, -1: default
//
//		/********************************************/
//		// Check modifier
//		/********************************************/
//		{
//			IRIterator<? extends IRObject> it = modifierList.iterator();
//			while (it.hasNext()) {
//
//				IRAtom keyObj = RulpUtil.asAtom(it.next());
//
//				switch (keyObj.getName()) {
//				case A_Limit:
//
//					if (queryCount != -1) {
//						throw new RException("duplicated modifier: " + keyObj.getName() + ", args=" + args);
//					}
//
//					if (!it.hasNext()) {
//						throw new RException("require value for " + keyObj.getName() + ", args=" + args);
//					}
//
//					queryCount = RulpUtil.asInteger(interpreter.compute(frame, it.next())).asInteger();
//					if (queryCount <= 0) {
//						throw new RException(
//								"invalid value<" + queryCount + "> for " + keyObj.getName() + ", args=" + args);
//					}
//
//					break;
//
//				default:
//
//					throw new RException("unsupport modifier: " + keyObj.getName() + ", args=" + args);
//				}
//			}
//		}
//
//		ArrayList<IRList> rsts = new ArrayList<>();
//
//		IRIterator<? extends IRList> it = scope.queryVar(varList);
//		if (queryCount <= 0) {
//			while (it.hasNext()) {
//				rsts.add(it.next());
//			}
//		} else {
//			while (queryCount-- > 0 && it.hasNext()) {
//				rsts.add(it.next());
//			}
//		}
//
//		return RulpFactory.createList(rsts);
//	}
//
//}
