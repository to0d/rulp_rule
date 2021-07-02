package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.RCountType;

public interface IRReteNodeCounter {

	public long getCount(RReteType reteType, RCountType countType) throws RException;
}
