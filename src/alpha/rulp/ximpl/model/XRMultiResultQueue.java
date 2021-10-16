package alpha.rulp.ximpl.model;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRMultiResultQueue implements IRResultQueue {

	protected final IRInterpreter interpreter;

	public XRMultiResultQueue(IRInterpreter interpreter, IRFrame queryFrame, IRObject rstExpr, IRVar[] vars) {
		super();
		this.interpreter = interpreter;
		this.queryFrame = queryFrame;
		this.rstExpr = rstExpr;
		this.vars = vars;
	}

	protected final IRFrame queryFrame;

	protected final IRObject rstExpr;

	protected final IRVar[] vars;

	protected ArrayList<IRObject> rstList = new ArrayList<>();

	public boolean addResult(IRObject rst) {
		rstList.add(rst);
		return true;
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

		return addResult(interpreter.compute(queryFrame, rstExpr));
	}

	@Override
	public List<? extends IRObject> getResultList() {
		return rstList;
	}

	@Override
	public int size() {
		return rstList.size();
	}
}
