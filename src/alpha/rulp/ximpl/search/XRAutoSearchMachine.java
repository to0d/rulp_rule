package alpha.rulp.ximpl.search;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.RRunState;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.ModelConstraintUtil;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public class XRAutoSearchMachine extends AbsRInstance implements IRAutoSearchMachine {

	class SEntry implements ICheckValue<List<IRObject>> {

		private ISScope<List<IRObject>> entryScope;

		private IRNamedNode searchNode;

		private ArrayList<SVar> searchVars;

		public SEntry(IRNamedNode searchNode, ArrayList<String> varNames) {
			super();
			this.searchNode = searchNode;
			this.searchVars = new ArrayList<>();

			int index = 0;
			for (String varName : varNames) {
				this.searchVars.add(new SVar(this, varName, index++));
			}
		}

		public ISScope<List<IRObject>> getVarScope() throws RException {

			if (entryScope == null) {

				List<ISScope<IRObject>> elementScopes = new ArrayList<>();
				for (SVar svar : searchVars) {
					elementScopes.add(svar.getVarScope());
				}

				entryScope = SearchFactory.createLinnerListScope(elementScopes);
				entryScope.setChecker(this);

			}
			return entryScope;
		}

		@Override
		public boolean isValid(List<IRObject> obj) throws RException {
			return model.tryAddStatement(RulpFactory.createNamedList(obj, searchNode.getNamedName()));
		}
	}

	static class SVar {

		public int index;

		public SEntry searchNode;

		public IValueList valueList;

		public String varName;

		public IRVar resultVar;

		public ISScope<IRObject> varScope = null;

		public SVar(SEntry searchNode, String varName, int index) {
			super();
			this.searchNode = searchNode;
			this.varName = varName;
			this.index = index;
		}

		public ISScope<IRObject> getVarScope() {

			if (varScope == null) {
				varScope = SearchFactory.createLinnerObjectScope(valueList);
			}
			return varScope;
		}
	}

	private List<String> allSearchNodeNames = new ArrayList<>();

	private List<String> allSearchVarNames = new ArrayList<>();

	private List<String> allResultVarNames;

//	static class XEntryValeList {
//
//		IRModel model;
//
//		Boolean queryCompleted = null;
//
//		SEntry searchEntry;
//
//		int varSize;
//
//		public IRList _createNextList() {
//
//			ArrayList<IRObject> valueList = new ArrayList<>();
//			for (XLinnerObjectScope scope : varScopes) {
//				valueList.add(scope.curValue);
//			}
//
//			return RulpFactory.createNamedList(valueList, searchEntry.searchNode.getNamedName());
//		}
//
//		public boolean hasNext() throws RException {
//
//			if (queryCompleted) {
//				return false;
//			}
//
//			if (nextList != null) {
//				return true;
//			}
//
//			if (queryCompleted == null) {
//
//				// Initialize first value
//				for (int i = 0; i < varSize; ++i) {
//					XLinnerObjectScope scope = varScopes[i];
//					if (!scope.moveNext()) {
//						queryCompleted = true;
//						return false;
//					}
//				}
//				nextList = _createNextList();
//				queryCompleted = false;
//
//			} else {
//				nextList = moveNext();
//			}
//
//			while (nextList != null && !model.tryAddStatement(nextList)) {
//				nextList = moveNext();
//			}
//
//			if (nextList == null) {
//				queryCompleted = true;
//			}
//
//			return !queryCompleted;
//		}
//
//		public IRList moveNext() throws RException {
//
//			int varPos = 0;
//
//			while (varPos < varSize) {
//
////				++moveCount;
//
//				XLinnerObjectScope scope = varScopes[varPos];
//
//				if (scope.moveNext()) {
//					return _createNextList();
//				}
//
//				scope.reset();
//
//				// no first value
//				if (!scope.moveNext()) {
//					queryCompleted = true;
//					return null;
//				}
//
//				++varPos;
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

	private ISScope<List<List<IRObject>>> globalScope;

	private IRModel model;

	private IRList rstList;

	private ArrayList<SEntry> searchEntrys = new ArrayList<>();

	protected RRunState searchState = null;

	public XRAutoSearchMachine(IRModel model) {
		super();
		this.model = model;
	}

	protected boolean _checkValueList() throws RException {

		ModelConstraintUtil constraintUtil = new ModelConstraintUtil(model);

		for (SEntry searchEntry : searchEntrys) {

			NEXT_VAR: for (SVar searchVar : searchEntry.searchVars) {

				if (searchVar.valueList != null) {
					continue NEXT_VAR;
				}

				RType varType = constraintUtil.getTypeConstraint(searchEntry.searchNode, searchVar.index);
				if (varType == RType.INT) {
					IRObject maxValue = constraintUtil.getMaxConstraint(searchEntry.searchNode, searchVar.index);
					IRObject minValue = constraintUtil.getMinConstraint(searchEntry.searchNode, searchVar.index);
					if (maxValue != null && minValue == null) {
						searchVar.valueList = SearchFactory.createIntValueList(RulpUtil.asInteger(minValue),
								RulpUtil.asInteger(maxValue), null);
						continue NEXT_VAR;
					}
				}

				return false;
			}
		}

		return true;
	}

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

			if (allSearchNodeNames.contains(nodeName)) {
				throw new RException("duplicated search entry: " + searchEntry);
			}

			allSearchNodeNames.add(nodeName);
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
			if (allSearchVarNames.contains(varName)) {
				throw new RException("duplicated var<" + varObj + "> in node: " + searchEntry);
			}

			allSearchVarNames.add(varName);
			varNames.add(varName);
		}

		searchEntrys.add(new SEntry(searchNode, varNames));
	}

	public ISScope<List<List<IRObject>>> getGlobalScope() throws RException {

		if (globalScope == null) {

			List<ISScope<List<IRObject>>> entryScopes = new ArrayList<>();
			for (SEntry sentry : searchEntrys) {
				entryScopes.add(sentry.getVarScope());
			}

			globalScope = SearchFactory.createLinnerListScope(entryScopes);

		}

		return globalScope;
	}

	@Override
	public int getPriority() {
		return 0;
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

	@Override
	public RRunState halt() throws RException {
		throw new RException("not support");
	}

	private IRFrame resultFrame;
//
//	public IRFrame getFrame() {
//
//		if (searchFrame == null) {
//			searchFrame = RulpFactory.createFrame(node.getFrame(), "QF-" + node.getNodeName());
//			RulpUtil.incRef(searchFrame);
//		}
//	}

	public void setModel(IRModel model) {
		this.model = model;
	}

	public void setRstList(IRList rstList) throws RException {

		/********************************************/
		// Check entry
		/********************************************/
		if (ReteUtil.getExprLevel(rstList) != 0) {
			throw new RException("invalid result entry: " + rstList);
		}

		if (model.getNodeGraph().findNamedNode(rstList.getNamedName()) != null) {
			throw new RException("result node already exist: " + rstList);
		}

		allResultVarNames = new ArrayList<>();
		for (IRObject varObj : ReteUtil.buildVarList(rstList)) {

			String varName = RulpUtil.asAtom(varObj).getName();
			if (!allSearchVarNames.contains(varName)) {
				throw new RException("undefined result var: " + varName);
			}

			allResultVarNames.add(varName);
		}

		/********************************************/
		// Create result frame
		/********************************************/
		resultFrame = RulpFactory.createFrame(model.getFrame(), "SF-" + rstList.getNamedName());

		/********************************************/
		// Create result var
		/********************************************/
		for (SEntry sentry : searchEntrys) {
			for (SVar svar : sentry.searchVars) {
				if (allResultVarNames.contains(svar.varName)) {
					svar.resultVar = resultFrame.addVar(svar.varName);
				}
			}
		}

		this.rstList = rstList;
	}

	@Override
	public int start(int priority, int limit) throws RException {

		ISScope<List<List<IRObject>>> _scope = this.getGlobalScope();
		if (_scope.moveNext()) {

		}

		return 0;
	}

}
