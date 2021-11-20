package alpha.rulp.ximpl.mts;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.RRunState;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public class XRSearchModel extends AbsRInstance implements IRSearchModel {

	private IRModel model;

	private IRList rstList;

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RRunState getRunState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RRunState halt() throws RException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int start(int priority, int limit) throws RException {
		// TODO Auto-generated method stub
		return 0;
	}

}
