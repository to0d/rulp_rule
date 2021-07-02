package alpha.rulp.ximpl.model;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;

public interface IRObjBuilder {

	public IRObject build(IRObject obj) throws RException;

}
