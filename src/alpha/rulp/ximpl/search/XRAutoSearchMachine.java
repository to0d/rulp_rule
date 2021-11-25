package alpha.rulp.ximpl.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.RRunState;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.ModelConstraintUtil;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.rclass.AbsRInstance;
import alpha.rulp.ximpl.scope.IVarConstraint;
import alpha.rulp.ximpl.scope.XRScope;
import alpha.rulp.ximpl.scope.XRScopeVar;

public class XRAutoSearchMachine extends AbsRInstance implements IRAutoSearchMachine {

	static class XSearchEntry {

		public IRNamedNode searchNode;

		public ArrayList<XSearchVar> searchVars;

		public XSearchEntry(IRNamedNode searchNode, ArrayList<String> varNames) {
			super();
			this.searchNode = searchNode;
			this.searchVars = new ArrayList<>();

			int index = 0;
			for (String varName : varNames) {
				this.searchVars.add(new XSearchVar(this, varName, index++));
			}
		}
	}

	static class XVarContext {

		XSearchVar searchVar;

		IRIterator<? extends IRObject> valueIt;

		IRObject curValue;
	}

//	static class XEntryContext implements IRIterator<IRList> {
//
//		IRList nextList = null;
//
//		Boolean queryCompleted = null;
//
//		XSearchEntry searchEntry;
//
//		XVarContext[] varContexts;
//
//		int varSize;
//
//		public IRList _createNextList() {
//
//			ArrayList<IRObject> valueList = new ArrayList<>();
//			for (XVarContext varContext : varContexts) {
//				valueList.add(varContext.curValue);
//			}
//
//			return RulpFactory.createNamedList(valueList, searchEntry.searchNode.getNamedName());
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
//
//					XVarContext varContext = varContexts[i];
//
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

	static class XSearchVar {

		public XSearchEntry searchNode;

		public String varName;

		public IValueList valueList;

		public int index;

		public XSearchVar(XSearchEntry searchNode, String varName, int index) {
			super();
			this.searchNode = searchNode;
			this.varName = varName;
			this.index = index;
		}
	}

	private Set<String> allNodeNames = new HashSet<>();

	private Set<String> allVarNames = new HashSet<>();

	private IRModel model;

	public void setModel(IRModel model) {
		this.model = model;
	}

	private IRList rstList;

	private ArrayList<XSearchEntry> searchEntrys = new ArrayList<>();

	public void addSearchEntry(IRList searchEntry) throws RException {

		/********************************************/
		// Check entry
		/********************************************/
		if (ReteUtil.getExprLevel(searchEntry) != 0) {
			throw new RException("invalid search entry: " + searchEntry);
		}

		/********************************************/
		// Check node name
		/********************************************/
		String nodeName = searchEntry.getNamedName();
		{
			if (nodeName == null) {
				throw new RException("invalid search entry: " + searchEntry);
			}

			if (allNodeNames.contains(nodeName)) {
				throw new RException("duplicated search entry: " + searchEntry);
			}

			allNodeNames.add(nodeName);
		}

		/********************************************/
		// Check node
		/********************************************/
		IRNamedNode searchNode = model.getNodeGraph().findNamedNode(nodeName);
		if (searchNode == null) {
			throw new RException("search node not found: " + searchEntry);
		}

		if (searchNode.getEntryLength() != searchEntry.size()) {
			throw new RException("search node entry-length not match: " + searchEntry + ", expect=" + searchEntry.size()
					+ ", actual=" + searchNode.getEntryLength());
		}

		/********************************************/
		// Check var names
		/********************************************/
		ArrayList<String> varNames = new ArrayList<>();
		for (IRObject varObj : RulpUtil.toArray(searchEntry)) {

			if (!RulpUtil.isVarAtom(varObj)) {
				throw new RException("not var<" + varObj + "> in node: " + searchEntry);
			}

			String varName = RulpUtil.asAtom(varObj).getName();
			if (allVarNames.contains(varName)) {
				throw new RException("duplicated var<" + varObj + "> in node: " + searchEntry);
			}

			allVarNames.add(varName);
			varNames.add(varName);
		}

		searchEntrys.add(new XSearchEntry(searchNode, varNames));
	}

	@Override
	public int getPriority() {
		return 0;
	}

	protected RRunState searchState = null;

	protected boolean _checkValueList() throws RException {

		ModelConstraintUtil constraintUtil = new ModelConstraintUtil(model);

		for (XSearchEntry searchEntry : searchEntrys) {

			NEXT_VAR: for (XSearchVar searchVar : searchEntry.searchVars) {

				if (searchVar.valueList != null) {
					continue NEXT_VAR;
				}

				RType varType = constraintUtil.getTypeConstraint(searchEntry.searchNode, searchVar.index);
				if (varType == RType.INT) {
					IRObject maxValue = constraintUtil.getMaxConstraint(searchEntry.searchNode, searchVar.index);
					IRObject minValue = constraintUtil.getMinConstraint(searchEntry.searchNode, searchVar.index);
					if (maxValue != null && minValue == null) {
						searchVar.valueList = SearchFactory.createIntValueList(minValue, maxValue, null);
						continue NEXT_VAR;
					}
				}

				return false;
			}
		}

		return true;
	}

	@Override
	public RRunState getRunState() throws RException {

		if (searchState == null || searchState == RRunState.Halting) {
			if (!_checkValueList()) {
				searchState = RRunState.Halting;
			}
			searchState = RRunState.Runnable;
		}

		return searchState;
	}

//	private IRFrame searchFrame;
//
//	public IRFrame getFrame() {
//
//		if (searchFrame == null) {
//			searchFrame = RulpFactory.createFrame(node.getFrame(), "QF-" + node.getNodeName());
//			RulpUtil.incRef(searchFrame);
//		}
//	}

	@Override
	public RRunState halt() throws RException {
		throw new RException("not support");
	}

	public void setRstList(IRList rstList) {
		this.rstList = rstList;
	}

	@Override
	public int start(int priority, int limit) throws RException {

		/********************************************/
		// Check value list
		/********************************************/

		return 0;
	}

}
