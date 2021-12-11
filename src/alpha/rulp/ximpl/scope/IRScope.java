//package alpha.rulp.ximpl.scope;
//
//import java.util.List;
//
//import alpha.rulp.lang.IRExpr;
//import alpha.rulp.lang.IRFrame;
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.IRVar;
//import alpha.rulp.lang.RException;
//import alpha.rulp.lang.RType;
//import alpha.rulp.rule.IRModel;
//import alpha.rulp.runtime.IRIterator;
//
//public interface IRScope {
//
//	public void addConstraint(IRExpr expr) throws RException;
//
//	public void addConstraint(IRList condList, IRExpr expr) throws RException;
//
//	public IRModel getModel();
//
//	public IRFrame getScopeFrame() throws RException;
//
//	public int getValueMoveCount();
//
//	public List<? extends IVarConstraint> listVarConstraints();
//
//	public List<? extends IRScopeVar> listVarScopes();
//
//	public IRIterator<? extends IRList> queryVar(IRList varList) throws RException;
//
//	public void setVarScope(IRVar var, IRList values) throws RException;
//
//	public void setVarScope(IRVar var, RType varType, IRObject fromValue, IRObject toValue, IRObject stepValue)
//			throws RException;
//
//}
