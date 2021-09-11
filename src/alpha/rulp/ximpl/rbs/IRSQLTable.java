package alpha.rulp.ximpl.rbs;

import java.util.List;

import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;

public interface IRSQLTable extends IRInstance {

	public int getRowCount();

	public int getColumnCount();

	public String getTableName();

	public List<RSQLColumn> getColumns();

	public IRList getRow(int rowIndex) throws RException;

	public IRObject getValue(int rowIndex, int columnIndex) throws RException;

	public void insert(IRList row) throws RException;

	public void insert(IRList columns, IRList values) throws RException;
}
