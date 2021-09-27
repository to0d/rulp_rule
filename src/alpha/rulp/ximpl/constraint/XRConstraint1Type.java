package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_Type;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Type extends AbsRConstraint1 implements IRConstraint1Type {

	private String _constraintExpression = null;

	private final int columnIndex;

	private final RType columnType;

	private int[] constraintIndex;

	public XRConstraint1Type(int columnIndex, RType columnType) {
		super();
		this.columnIndex = columnIndex;
		this.columnType = columnType;
		this.constraintIndex = new int[1];
		this.constraintIndex[0] = columnIndex;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {

		IRObject columnValue = entry.get(columnIndex);
		if (columnValue == null) {
			return true;
		}

		return columnValue.getType() == columnType || columnValue.getType() == RType.NIL;
	}

	@Override
	public int getColumnIndex() {
		return columnIndex;
	}

	@Override
	public RType getColumnType() {
		return columnType;
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("'(type %s on %d)", RType.toObject(columnType).asString(),
					columnIndex);
		}

		return _constraintExpression;
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}

	@Override
	public String getConstraintName() {
		return A_Type;
	}

}
