package alpha.rulp.ximpl.model;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryIteratorBuilder;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRMultiResultQueue implements IRResultQueue, IRContext {

	protected ArrayList<IRConstraint1> constraintList = null;

	protected ArrayList<IRExpr> doExprList = null;

	protected final IRInterpreter interpreter;

	protected IREntryIteratorBuilder orderBuilder;

	protected int orderLimit = -1;

	protected final IRFrame queryFrame;

	protected final IRObject rstExpr;

	protected final ArrayList<IRObject> rstList = new ArrayList<>();

	protected final IRVar[] vars;

	public XRMultiResultQueue(IRInterpreter interpreter, IRFrame queryFrame, IRObject rstExpr, IRVar[] vars) {
		super();
		this.interpreter = interpreter;
		this.queryFrame = queryFrame;
		this.rstExpr = rstExpr;
		this.vars = vars;
	}

	@Override
	public void addConstraint(IRConstraint1 con) {

		if (constraintList == null) {
			constraintList = new ArrayList<>();
		}

		constraintList.add(con);
	}

	@Override
	public void addDoExpr(IRExpr expr) {

		if (doExprList == null) {
			doExprList = new ArrayList<>();
		}

		doExprList.add(expr);
	}

	@Override
	public boolean addEntry(IRReteEntry entry) throws RException {

		if (entry == null || entry.isDroped()) {
			return false;
		}

		/******************************************************/
		// Update variable value
		/******************************************************/
		for (int j = 0; j < vars.length; ++j) {
			IRVar var = vars[j];
			if (var != null) {
				var.setValue(entry.get(j));
			}
		}

		if (constraintList != null) {
			for (IRConstraint1 constraint : constraintList) {
				if (!constraint.addEntry(entry, this)) {
					return false;
				}
			}
		}

		/******************************************************/
		// Exec do expression
		/******************************************************/
		if (doExprList != null) {
			for (IRExpr expr : doExprList) {
				interpreter.compute(queryFrame, expr);
			}
		}

		return addResult(interpreter.compute(queryFrame, rstExpr));
	}

	public boolean addResult(IRObject rst) {
		rstList.add(rst);
		return true;
	}

	@Override
	public void clean() throws RException {

	}

	@Override
	public void close() throws RException {

		if (queryFrame != null) {
			queryFrame.release();
			RulpUtil.decRef(queryFrame);
		}

	}

	@Override
	public IRFrame findFrame() {
		return queryFrame;
	}

	@Override
	public IRFrame getFrame() throws RException {
		return queryFrame;
	}

	@Override
	public IRInterpreter getInterpreter() {
		return interpreter;
	}

	@Override
	public IRModel getModel() {
		return null;
	}

	@Override
	public List<? extends IRObject> getResultList() {
		return rstList;
	}

	@Override
	public void setOrderBuilder(IREntryIteratorBuilder orderBuilder) {
		this.orderBuilder = orderBuilder;
	}

	@Override
	public void setOrderLimit(int orderLimit) {
		this.orderLimit = orderLimit;
	}

	@Override
	public int size() {
		return rstList.size();
	}
}
