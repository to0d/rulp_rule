//package alpha.rulp.ximpl.scope;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import alpha.rulp.lang.IRClass;
//import alpha.rulp.lang.IRExpr;
//import alpha.rulp.lang.IRFrame;
//import alpha.rulp.lang.IRList;
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.IRVar;
//import alpha.rulp.lang.RException;
//import alpha.rulp.lang.RType;
//import alpha.rulp.rule.IRModel;
//import alpha.rulp.runtime.IRIterator;
//import alpha.rulp.utils.ReteUtil;
//import alpha.rulp.utils.RulpFactory;
//import alpha.rulp.utils.RulpUtil;
//import alpha.rulp.ximpl.rclass.AbsRInstance;
//import alpha.rulp.ximpl.search.IValueList;
//import alpha.rulp.ximpl.search.SearchFactory;
//
//public class XRScope extends AbsRInstance implements IRScope {
//
//	class QueryContext implements IRIterator<IRList> {
//
//		IRFrame frame;
//
//		IRList nextList = null;
//
//		Boolean queryCompleted = null;
//
//		XRScopeVar[] varScopes;
//
//		int varSize;
//
//		public IRList _createNextList() throws RException {
//
//			/**********************************************/
//			// First matched value list
//			/**********************************************/
//			ArrayList<IRObject> valueList = new ArrayList<>();
//			for (XRScopeVar scope : varScopes) {
//				valueList.add(scope.var.getValue());
//			}
//
//			return RulpFactory.createList(valueList);
//		}
//
//		@Override
//		public boolean hasNext() throws RException {
//
//			if (isCompleted()) {
//				return false;
//			}
//
//			if (nextList == null) {
//				nextList = moveNext();
//			}
//
//			return nextList != null;
//		}
//
//		public boolean isCompleted() throws RException {
//
//			if (queryCompleted == null) {
//
//				// Initialize first value
//				for (int i = 0; i < varSize; ++i) {
//					XRScopeVar scope = varScopes[i];
//					if (!scope.moveNext()) {
//						queryCompleted = true;
//						return true;
//					}
//				}
//
//				boolean hasNext = true;
//
//				// Check cross constraint
//				CHECK_CORSS: for (IVarConstraint constraint : XRScope.this.constraintList) {
//
//					if (constraint.isSingleConstraint()) {
//						continue;
//					}
//
//					boolean hasScope = false;
//
//					for (XRScopeVar scope : varScopes) {
//						if (constraint.listVarScopes().contains(scope)) {
//							hasScope = true;
//							break;
//						}
//					}
//
//					if (!hasScope) {
//						continue;
//					}
//
//					// Invalid value
//					if (!constraint.checkConstraint()) {
//						hasNext = false;
//						break CHECK_CORSS;
//					}
//				}
//
//				queryCompleted = false;
//
//				// Check next
//				if (!hasNext) {
//
//					nextList = moveNext();
//					if (!hasNext()) {
//						queryCompleted = true;
//						return true;
//					}
//
//				} else {
//
//					nextList = _createNextList();
//				}
//			}
//
//			return queryCompleted;
//		}
//
//		public IRList moveNext() throws RException {
//
//			int varPos = 0;
//
//			while (varPos < varSize) {
//
//				++XRScope.this.moveCount;
//
//				XRScopeVar scope = varScopes[varPos];
//
//				if (scope.moveNext()) {
//
//					if (scope.checkCrossConstraint()) {
//						return _createNextList();
//					}
//
//					varPos = 0;
//				}
//				// no more move
//				else {
//
//					scope.resetValueIndex();
//
//					// no first value
//					if (!scope.moveNext()) {
//						queryCompleted = true;
//						return null;
//					}
//
//					++varPos;
//				}
//
//			}
//
//			queryCompleted = true;
//			return null;
//		}
//
//		@Override
//		public IRList next() throws RException {
//
//			if (!hasNext()) {
//				return null;
//			}
//
//			IRList val = nextList;
//			nextList = null;
//			return val;
//		}
//	}
//
//	public static boolean TRACE = false;
//
//	protected List<IVarConstraint> constraintList = new ArrayList<>();
//
//	protected IRModel model;
//
//	protected int moveCount = 0;
//
//	protected ArrayList<XRScopeVar> varScopeList = new ArrayList<>();
//
//	protected Map<String, XRScopeVar> varScopeMap = new HashMap<>();
//
//	public XRScope(String scopeName, IRFrame frame, IRModel model, IRClass noClass) {
//		super(noClass, scopeName, frame);
//		this.model = model;
//	}
//
//	@Override
//	public void addConstraint(IRExpr expr) throws RException {
//
//		IVarConstraint constraint = null;
//		List<String> varNameList = new ArrayList<>();
//
//		// Condition Constraint
//		if (ReteUtil.isCondList(expr)) {
//
//			List<IRList> matchStmtList = new LinkedList<>();
//
//			IRIterator<? extends IRObject> condIter = expr.iterator();
//			while (condIter.hasNext()) {
//
//				IRObject cond = condIter.next();
//
//				if (ReteUtil.isReteStmt(cond)) {
//					matchStmtList.add((IRList) cond);
//
//				} else if (RulpUtil.isExpression(cond)) {
//					matchStmtList.add((IRExpr) cond);
//
//				} else {
//					throw new RException("Invalid condition: " + cond);
//				}
//			}
//		}
//		// Expr Constraint
//		else {
//			varNameList.addAll(ReteUtil.varList(expr));
//			constraint = SearchFactory.createExprConstraint(model, expr);
//		}
//
//		/*********************************************/
//		// Link Var & Constraint
//		/*********************************************/
//		{
//			if (varNameList.size() == 0) {
//				throw new RException("not var found: " + expr);
//			}
//
//			for (String varName : varNameList) {
//
//				IRVar var = model.getVar(varName);
//				if (var == null) {
//					throw new RException("var not defined: " + varName);
//				}
//
//				XRScopeVar scope = getScope(var.getName());
//				if (scope == null) {
//					throw new RException("no var scope defined: " + var.getName());
//				} else if (scope.var != var) {
//					throw new RException("Duplicated var defined: " + var.getName());
//				}
//
//				constraint.addScope(scope);
//			}
//
//			for (IRScopeVar scope : constraint.listVarScopes()) {
//				scope.addConstraint(constraint);
//			}
//		}
//
//		this.constraintList.add(constraint);
//	}
//
//	@Override
//	public void addConstraint(IRList condList, IRExpr expr) throws RException {
//
//	}
//
//	@Override
//	public IRModel getModel() {
//		return model;
//	}
//
//	public XRScopeVar getScope(String varName) {
//		return varScopeMap.get(varName);
//	}
//
//	@Override
//	public IRFrame getScopeFrame() throws RException {
//		return this.getSubjectFrame();
//	}
//
//	@Override
//	public int getValueMoveCount() {
//		return moveCount;
//	}
//
//	@Override
//	public List<IVarConstraint> listVarConstraints() {
//		return constraintList;
//	}
//
//	public List<? extends IRScopeVar> listVarScopes() {
//		return varScopeList;
//	}
//
//	@Override
//	public IRIterator<? extends IRList> queryVar(IRList varList) throws RException {
//
//		QueryContext context = new QueryContext();
//
//		context.varSize = varList.size();
//		context.varScopes = new XRScopeVar[context.varSize];
//
//		// Add var scopes
//		{
//
//			int index = 0;
//			IRIterator<? extends IRObject> it = varList.iterator();
//			while (it.hasNext()) {
//
//				String varName = RulpUtil.asAtom(it.next()).getName();
//
//				XRScopeVar scope = getScope(varName);
//				if (scope == null) {
//					throw new RException("no var scope defined: " + varName);
//				}
//
//				if (!scope.hasValueScope()) {
//					throw new RException("no value scope defined: " + varName);
//				}
//
//				context.varScopes[index++] = scope;
//			}
//		}
//
//		context.frame = RulpFactory.createFrame(model.getFrame(), "VarScope-Frame");
//
//		/**********************************************/
//		// Check cross reference constraint
//		/**********************************************/
//
//		return context;
//	}
//
//	@Override
//	public void setVarScope(IRVar var, IRList values) throws RException {
//
//		XRScopeVar scope = getScope(var.getName());
//		if (scope != null) {
//			throw new RException("Duplicated scope defined: " + var.getName());
//		}
//
//		/********************************************/
//		// Check Scope
//		/********************************************/
//		if (values == null || values.size() == 0) {
//			throw new RException("invalid values: " + values);
//		}
//
//		IValueList valueFactory = SearchFactory.createValueListObjectFactory(values);
//
//		/********************************************/
//		// Create Scope
//		/********************************************/
//		if (scope == null) {
//			scope = new XRScopeVar();
//			scope.var = var;
//			varScopeMap.put(var.getName(), scope);
//			varScopeList.add(scope);
//		}
//
//		scope.varType = RType.LIST;
//		scope.valueFactory = valueFactory;
//	}
//
//	@Override
//	public void setVarScope(IRVar var, RType varType, IRObject fromValue, IRObject toValue, IRObject stepValue)
//			throws RException {
//
//		if (varType == null) {
//			throw new RException("null type");
//		}
//
//		XRScopeVar scope = getScope(var.getName());
//		if (scope != null) {
//			throw new RException("Duplicated scope defined: " + var.getName());
//		}
//
//		/********************************************/
//		// Check Scope
//		/********************************************/
//		if (fromValue == null || toValue == null) {
//			throw new RException(String.format("invalid value scope: from=%s, to=%s", fromValue, toValue));
//		}
//
//		if (fromValue.getType() != varType || toValue.getType() != varType) {
//			throw new RException(String.format("type not match: type=%s, from=%s, to=%s", varType, fromValue, toValue));
//		}
//
//		if (stepValue != null && stepValue.getType() != varType) {
//			throw new RException(String.format("type not match: step=%s", stepValue));
//		}
//
//		IValueList valueFactory = null;
//		/********************************************/
//		// Check type
//		/********************************************/
//		switch (varType) {
//		case INT:
////			valueFactory = SearchFactory.createIntValueList(fromValue, toValue, stepValue);
//			break;
//
//		default:
//			throw new RException("Not support type: " + varType);
//		}
//
//		/********************************************/
//		// Create Scope
//		/********************************************/
//		if (scope == null) {
//			scope = new XRScopeVar();
//			scope.var = var;
//			varScopeMap.put(var.getName(), scope);
//			varScopeList.add(scope);
//		}
//
//		scope.varType = varType;
//		scope.valueFactory = valueFactory;
//	}
//}
