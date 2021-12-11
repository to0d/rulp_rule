//package alpha.rulp.ximpl.scope;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import alpha.rulp.lang.IRExpr;
//import alpha.rulp.lang.IRFrame;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.RException;
//import alpha.rulp.rule.IRModel;
//import alpha.rulp.utils.RulpFactory;
//import alpha.rulp.utils.RulpUtil;
//
//public class XVarConstraint implements IVarConstraint {
//
//	protected IRModel model;
//
//	protected IRExpr expr;
//
//	protected String _toString = null;
//
//	protected ArrayList<IRScopeVar> varScopeList = new ArrayList<>();
//
//	protected IRFrame frame;
//
//	public XVarConstraint(IRModel model, IRExpr expr) {
//		super();
//		this.model = model;
//		this.expr = expr;
//	}
//
//	@Override
//	public void addScope(IRScopeVar scope) {
//		varScopeList.add(scope);
//	}
//
//	@Override
//	public boolean checkConstraint() throws RException {
//
//		for (IRScopeVar scope : varScopeList) {
//			scope.incValueEvalCount();
//		}
//
//		IRObject rst = model.getInterpreter().compute(getFrame(), expr);
//
//		if (XRScope.TRACE) {
//
//			System.out.print(String.format("checkCrossConstraint: expr=%s", "" + expr));
//			for (IRScopeVar scope : varScopeList) {
//				System.out
//						.print(String.format(", var(%s)=%s", scope.getVar().getName(), "" + scope.getVar().getValue()));
//			}
//
//			System.out.println(", rst=" + rst);
//		}
//
//		return RulpUtil.asBoolean(rst).asBoolean();
//	}
//
//	@Override
//	public String getDescription() {
//		return toString();
//	}
//
//	public IRFrame getFrame() throws RException {
//
//		if (frame == null) {
//			frame = RulpFactory.createFrame(model.getFrame(), "constraint-Frame");
//		}
//
//		return frame;
//	}
//
//	@Override
//	public boolean isSingleConstraint() {
//		return varScopeList.size() == 1;
//	}
//
//	@Override
//	public List<? extends IRScopeVar> listVarScopes() {
//		return varScopeList;
//	}
//
//	public String toString() {
//
//		if (_toString == null) {
//
//			_toString = "" + expr + ":";
//
//			for (int i = 0; i < varScopeList.size(); ++i) {
//
//				if (i != 0) {
//					_toString += ",";
//				}
//
//				_toString += varScopeList.get(i).getVar().getName();
//			}
//		}
//
//		return _toString;
//	}
//}