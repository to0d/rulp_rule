//package alpha.rulp.ximpl.model;
//
//import java.util.LinkedList;
//import java.util.List;
//
//import alpha.rulp.lang.RException;
//import alpha.rulp.rule.IRRule;
//import alpha.rulp.rule.IRRuleListener;
//
//public class XRRuleListenerDispatcher implements IRRuleListener {
//
//	private List<IRRuleListener> listenerList = null;
//
//	@Override
//	public void ruleAction(IRRule rule) throws RException {
//
//		if (listenerList == null) {
//			return;
//		}
//
//		for (IRRuleListener listener : listenerList) {
//			listener.ruleAction(rule);
//		}
//	}
//
//	public void addRuleListener(IRRuleListener listener) {
//
//		if (listenerList == null) {
//			listenerList = new LinkedList<>();
//		}
//
//		if (!listenerList.contains(listener)) {
//			listenerList.add(listener);
//		}
//	}
//
//}
