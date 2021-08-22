package alpha.rulp.ximpl.sql;

import alpha.rulp.lang.RType;

public class RSQLColumn {

	public final String columnName;

	public final RType columnType;

	public RSQLColumn(String columnName, RType columnType) {
		super();
		this.columnName = columnName;
		this.columnType = columnType;
	}
}
