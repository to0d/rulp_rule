package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeNamed extends XRNodeRete0 implements IRNamedNode {

	protected IRObject[] funcEntry;

	protected IRConstraint1Uniq funcUniqConstraint;

	protected IRVar[] _funcEntryVars;

	public XRNodeNamed(String instanceName) {
		super(instanceName);
	}

	@Override
	public void cleanCache() throws RException {

		if (cache == null) {
			throw new RException("not cacher found");
		}

		this.cache.cleanBuffer();

		IREntryTable entryTable = this.getModel().getEntryTable();
		for (IRReteEntry entry : ReteUtil.getAllEntries(this.getEntryQueue())) {
			entryTable.deleteEntryReference(entry, this);
		}

		this.entryQueue.cleanCache();
		this.lastEntryCount = 0;
		this.reteStage = RReteStage.InActive;
	}

	@Override
	public IRList computeFuncEntry(IRList stmt) throws RException {

		IRInterpreter interpreter = this.getInterpreter();
		IRFrame namedFrame = _createNodeFrame();

		int len = this.getEntryLength();
		IRObject[] elements = new IRObject[len];

		for (int i = 0; i < len; ++i) {

			IRObject obj = stmt.get(i);

			if (RulpUtil.isVarAtom(obj)) {

				if (_funcEntryVars[i] != null) {
					throw new RException("need var at: " + i);
				}

			} else {

				obj = interpreter.compute(namedFrame, obj);
				elements[i] = obj;

				IRVar _var = _funcEntryVars[i];
				if (_var != null) {
					_var.setValue(obj);
				}
			}
		}

		for (int i = 0; i < len; ++i) {

			IRObject obj = elements[i];
			if (obj == null) {
				obj = interpreter.compute(namedFrame, funcEntry[i]);
				elements[i] = obj;
			}
		}

		return RulpFactory.createNamedList(instanceName, elements);
	}

	@Override
	protected IRFrame _createNodeFrame() throws RException {

		IRFrame namedFrame = super._createNodeFrame();

		/***********************************************************/
		// Create var for func entry
		/***********************************************************/
		if (funcEntry != null) {

			int len = this.getEntryLength();
			_funcEntryVars = new IRVar[len];
			for (int i = 0; i < len; ++i) {
				IRObject obj = funcEntry[i];
				if (RulpUtil.isVarAtom(obj)) {
					_funcEntryVars[i] = RulpUtil.addVar(namedFrame, RulpUtil.asAtom(obj).getName());
				}
			}

			/***********************************************************/
			// Set default model
			/***********************************************************/
			RuleUtil.setDefaultModel(namedFrame, this.getModel());
		}

		return namedFrame;
	}

	@Override
	public IRObject[] getFuncEntry() {
		return funcEntry;
	}

	@Override
	public IRConstraint1Uniq getFuncUniqConstraint() {
		return funcUniqConstraint;
	}

	public void setFuncEntry(IRObject[] funcEntry) throws RException {

		if (funcEntry.length != this.getEntryLength()) {
			throw new RException(String.format("unmatch entry length: funcEntryLen=%d, NodeEntryLen=%d",
					funcEntry.length, this.getEntryLength()));
		}

		this.funcEntry = funcEntry;
	}

	public void setFuncUniqConstraint(IRConstraint1Uniq funcUniqConstraint) throws RException {
		this.funcUniqConstraint = funcUniqConstraint;
	}
}
