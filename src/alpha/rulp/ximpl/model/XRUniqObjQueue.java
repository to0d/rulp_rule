package alpha.rulp.ximpl.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.utils.MatchTree;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRUniqObjQueue {

	private IRVar[] _vars;

	private IRList condList;

	private IRModel model;

	private IRFrame queryFrame;

	private IRObject rstExpr;

	private ArrayList<IRObject> rstList = new ArrayList<>();

	private Set<String> uniqNames = new HashSet<>();

	public XRUniqObjQueue(IRModel model, IRObject rstExpr, IRList condList) {
		super();
		this.model = model;
		this.rstExpr = rstExpr;
		this.condList = condList;
	}

	public boolean addEntry(IRReteEntry entry) throws RException {

		if (entry == null || entry.isDroped()) {
			return false;
		}

		/******************************************************/
		// Update variable value
		/******************************************************/
		for (int j = 0; j < getVars().length; ++j) {
			IRVar var = _vars[j];
			if (var != null) {
				var.setValue(entry.get(j));
			}
		}

		IRObject rstObj = model.getInterpreter().compute(getQueryFrame(), rstExpr);
		String uniqName = ReteUtil.uniqName(rstObj);
		if (uniqNames.contains(uniqName)) {
			return false;
		}

		uniqNames.add(uniqName);
		rstList.add(rstObj);
		return true;
	}

	public IRFrame getQueryFrame() throws RException {

		if (queryFrame == null) {
			queryFrame = RulpFactory.createFrame(model.getModelFrame(), "QUERY");
			RuleUtil.setDefaultModel(queryFrame, model);
		}

		return queryFrame;
	}

	public List<? extends IRObject> getRstList() {
		return rstList;
	}

	public IRVar[] getVars() throws RException {

		if (_vars == null) {

			/******************************************************/
			// Build var list
			/******************************************************/
			List<IRList> matchStmtList = ReteUtil.toCondList(condList, model.getNodeGraph());
			IRList matchTree = MatchTree.build(matchStmtList);
			IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(matchTree));
			_vars = new IRVar[varEntry.length];

			for (int i = 0; i < varEntry.length; ++i) {
				IRObject obj = varEntry[i];
				if (obj != null) {
					_vars[i] = getQueryFrame().addVar(RulpUtil.asAtom(obj).getName());
				}
			}
		}

		return _vars;
	}

	public int size() {
		return rstList.size();
	}
}
