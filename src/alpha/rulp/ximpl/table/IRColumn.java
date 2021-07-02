package alpha.rulp.ximpl.table;

import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.RType;

public interface IRColumn extends IRInstance {

	public String getColumnName();

	public RType getColumnType();
}
