package alpha.rulp.rule;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;

public interface IRContext {

	public IRFrame findFrame();

	public IRFrame getFrame() throws RException;

	public IRInterpreter getInterpreter();

	public IRModel getModel();
}
