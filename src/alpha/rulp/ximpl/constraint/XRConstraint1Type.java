package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_Type;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Type extends AbsRConstraint1Index1 implements IRConstraint1Type {

	private String _constraintExpression = null;

	private final RType columnType;

	public XRConstraint1Type(int columnIndex, RType columnType) {
		super(columnIndex);
		this.columnType = columnType;
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {
		IRObject columnValue = entry.get(index);
		if (columnValue == null) {
			return true;
		}

		return columnValue.getType() == columnType || columnValue.getType() == RType.NIL;
	}

	@Override
	public int getColumnIndex() {
		return index;
	}

	@Override
	public RType getColumnType() {
		return columnType;
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s %s on ?%d)", getConstraintName(),
					RType.toObject(columnType).asString(), index);
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_Type;
	}

}
