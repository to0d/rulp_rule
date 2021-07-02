package alpha.rulp.ximpl.table;

import java.util.List;

import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;

public interface IRTable extends IRInstance {

	public int getRowCount();

	public int getColumnCount();

	public String getTableName();

	public List<IRColumn> getColumnMetas();

	public IRList getRow(int rowIndex) throws RException;

	public IRObject getValue(int rowIndex, int columnIndex) throws RException;

	public void insert(IRList row) throws RException;

	public void insert(IRList columns, IRList values) throws RException;
}
