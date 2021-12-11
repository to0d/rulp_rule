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

public class XRASMachine extends AbsRInstance implements IRASMachine {

	static class SEntry implements IRSEntry {

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

		@Override
		public ISScope<List<IRObject>> getScope() {
			return scope;
		}

		@Override
		public IRReteNode getSearchNode() {
			return searchNode;
		}

		@Override
		public List<? extends IRSVar> listSVar() {
			return searchVars;
		}
	}

	static class SVar implements IRSVar {

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

		@Override
		public ISScope<IRObject> getScope() {
			return scope;
		}

		@Override
		public IValueList getValueList() {
			return valueList;
		}

		@Override
		public String getVarName() {
			return varName;
		}
	}

	public static boolean TRACE = false;

	protected List<String> allResultVarNames;

	protected List<String> allSearchNodeNames = new ArrayList<>();

	protected List<String> allSearchVarNames = new ArrayList<>();

	protected ModelConstraintUtil constraintUtil;

	protected ISScope<List<List<IRObject>>> globalScope;

	protected IRModel model;

	protected IRFrame resultFrame;

	protected IRList rstList;

	protected ArrayList<SEntry> searchEntrys = new ArrayList<>();

	protected RRunState searchState = null;

	public XRASMachine(IRModel model) {
		super();
		this.model = model;
		this.constraintUtil = new ModelConstraintUtil(model);
	}

	protected boolean _buildValueList() throws RException {

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
				return SearchFactory.createValueListInt(RulpUtil.asInteger(minValue), RulpUtil.asInteger(maxValue),
						null);
			}
		}

		IRList atomValueList = constraintUtil.getOneOfConstraint(entry.searchNode, searchVar.index);
		if (varType == null && atomValueList != null) {
			return SearchFactory.createValueListObjectFactory(atomValueList);
		}

		return null;
	}

	protected boolean _checkEntry(SEntry entry, List<IRObject> obj) throws RException {
		return model.assumeStatement(RulpFactory.createNamedList(obj, entry.searchNode.getNamedName()));
	}

	protected ISScope<List<List<IRObject>>> _getGlobalScope() throws RException {

		if (globalScope == null) {
			globalScope = _buildGlobalScope(searchEntrys);
		}

		return globalScope;
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
	public List<String> getAllResultVarNames() {
		return allResultVarNames;
	}

	@Override
	public List<String> getAllSearchNodeNames() {
		return allSearchNodeNames;
	}

	@Override
	public List<String> getAllSearchVarNames() {
		return allSearchVarNames;
	}

	@Override
	public IRModel getModel() {
		return model;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public IRFrame getResultFrame() {
		return resultFrame;
	}

	@Override
	public IRList getRstList() {
		return rstList;
	}

	@Override
	public RRunState getRunState() throws RException {

		if (searchState == null || searchState == RRunState.Halting) {
			if (!_buildValueList()) {
				searchState = RRunState.Halting;
			} else {
				searchState = RRunState.Runnable;
			}
		}

		return searchState;
	}

	@Override
	public ISScope<List<List<IRObject>>> getScope() {
		return globalScope;
	}

	@Override
	public RRunState halt() throws RException {
		throw new RException("not support");
	}

	@Override
	public List<? extends IRSEntry> listSEntry() {
		return searchEntrys;
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

		if (_scope.isCompleted()) {
			searchState = RRunState.Completed;
		} else {
			searchState = RRunState.Partial;
		}

		return count;
	}
}
