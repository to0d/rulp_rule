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
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RRunState;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.ModelConstraintUtil;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public class XRAutoSearchMachine extends AbsRInstance implements IRAutoSearchMachine {

	static class SEntry {

		public boolean build = false;

		public ISScope<List<IRObject>> scope;

		public IRReteNode searchNode;

		public ArrayList<SVar> searchVars;

		public SEntry(IRReteNode searchNode, ArrayList<String> varNames) {
			super();
			this.searchNode = searchNode;
			this.searchVars = new ArrayList<>();

			int index = 0;
			for (String varName : varNames) {
				this.searchVars.add(new SVar(this, varName, index++));
			}
		}
	}

	static class SVar {

		public boolean build = false;

		public int index;

		public IRVar resultVar;

		public ISScope<IRObject> scope = null;

		public SEntry searchNode;

		public IValueList valueList;

		public String varName;

		public SVar(SEntry searchNode, String varName, int index) {
			super();
			this.searchNode = searchNode;
			this.varName = varName;
			this.index = index;
		}
	}

	private List<String> allResultVarNames;

	private List<String> allSearchNodeNames = new ArrayList<>();

	private List<String> allSearchVarNames = new ArrayList<>();

	private ModelConstraintUtil constraintUtil;

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

	public static boolean TRACE = false;

	private IRFrame resultFrame;
//
//	public IRFrame getFrame() {
//
//		if (searchFrame == null) {
//			searchFrame = RulpFactory.createFrame(node.getFrame(), "QF-" + node.getNodeName());
//			RulpUtil.incRef(searchFrame);
//		}
//	}

	private IRList rstList;

	private ArrayList<SEntry> searchEntrys = new ArrayList<>();

	protected RRunState searchState = null;

	public XRAutoSearchMachine(IRModel model) {
		super();
		this.model = model;
		this.constraintUtil = new ModelConstraintUtil(model);
	}

	protected boolean _build() throws RException {

		int missCount = 0;

		NEXT_ENTRY: for (SEntry searchEntry : searchEntrys) {

			if (searchEntry.build) {
				continue NEXT_ENTRY;
			}

			NEXT_VAR: for (SVar searchVar : searchEntry.searchVars) {

				if (searchVar.build) {
					continue NEXT_VAR;
				}

				// Build value list
				{
					if (searchVar.valueList == null) {
						searchVar.valueList = _buildVarValueList(searchEntry, searchVar);
					}

					if (searchVar.valueList == null) {
						++missCount;
						continue NEXT_ENTRY;
					}
				}

				// Build value scope
				{
					if (searchVar.scope == null) {
						searchVar.scope = _buildVarScope(searchVar);
					}

					if (searchVar.scope == null) {
						++missCount;
						continue NEXT_ENTRY;
					}
				}

				searchVar.build = true;
			}

			// Build entry scope
			{
				if (searchEntry.scope == null) {
					searchEntry.scope = _buildEntryScope(searchEntry);
				}

				if (searchEntry.scope == null) {
					++missCount;
					continue NEXT_ENTRY;
				}
			}

			searchEntry.build = true;
		}

		return missCount == 0;
	}

	protected ISScope<List<IRObject>> _buildEntryScope(SEntry entry) throws RException {

		List<ISScope<IRObject>> elementScopes = new ArrayList<>();
		for (SVar svar : entry.searchVars) {
			elementScopes.add(svar.scope);
		}

		ISScope<List<IRObject>> entryScope = SearchFactory.createLinnerListScope(elementScopes);
		entryScope.setChecker((val) -> {
			return _checkEntry(entry, val);
		});

		return entryScope;
	}

	protected ISScope<List<List<IRObject>>> _buildGlobalScope(ArrayList<SEntry> searchEntrys) throws RException {

		List<ISScope<List<IRObject>>> entryScopes = new ArrayList<>();
		for (SEntry sentry : searchEntrys) {
			entryScopes.add(sentry.scope);
		}

		return SearchFactory.createLinnerListScope(entryScopes);
	}

	protected ISScope<IRObject> _buildVarScope(SVar svar) throws RException {
		return SearchFactory.createLinnerObjectScope(svar.valueList);
	}

	protected IValueList _buildVarValueList(SEntry entry, SVar searchVar) throws RException {

		RType varType = constraintUtil.getTypeConstraint(entry.searchNode, searchVar.index);
		if (varType == RType.INT) {

			IRObject maxValue = constraintUtil.getMaxConstraint(entry.searchNode, searchVar.index);
			IRObject minValue = constraintUtil.getMinConstraint(entry.searchNode, searchVar.index);
			if (maxValue != null && minValue != null) {
				return SearchFactory.createIntValueList(RulpUtil.asInteger(minValue), RulpUtil.asInteger(maxValue),
						null);
			}
		}

		return null;
	}

	protected ISScope<List<List<IRObject>>> _getGlobalScope() throws RException {

		if (globalScope == null) {
			globalScope = _buildGlobalScope(searchEntrys);
		}

		return globalScope;
	}

	protected boolean _checkEntry(SEntry entry, List<IRObject> obj) throws RException {
		return model.assumeStatement(RulpFactory.createNamedList(obj, entry.searchNode.getNamedName()));
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
		IRReteNode searchNode = model.getNodeGraph().findNamedNode(nodeName);
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

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public RRunState getRunState() throws RException {

		if (searchState == null || searchState == RRunState.Halting) {
			if (!_build()) {
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

	protected IRList _getResultEntry() throws RException {

		for (SEntry sentry : searchEntrys) {
			for (SVar svar : sentry.searchVars) {
				if (allResultVarNames.contains(svar.varName)) {
					svar.resultVar.setValue(svar.scope.curValue());
				}
			}
		}

		return RulpUtil.asList(model.getInterpreter().compute(resultFrame, rstList));
	}

	@Override
	public int start(int priority, int limit) throws RException {

		if (getRunState() != RRunState.Runnable) {
			return 0;
		}

		int count = 0;
		ISScope<List<List<IRObject>>> _scope = _getGlobalScope();
		while (_scope.moveNext()) {

			IRList rst = _getResultEntry();
			++count;

			if (TRACE) {
				System.out.println(rst);
			}

			model.addStatement(rst);

			if (limit > 0 && count >= limit) {
				break;
			}
		}

		return count;
	}
}
