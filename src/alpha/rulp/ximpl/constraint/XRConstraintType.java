package alpha.rulp.ximpl.constraint;

import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraintType extends AbsRConstraint1 implements IRConstraint1 {

	public static boolean match(IRConstraint1 constraint, RType columnType, int columnIndex) {

		if (constraint.getConstraintType() != RConstraintType.TYPE) {
			return false;
		}

		XRConstraintType typeConstraint = (XRConstraintType) constraint;

		if (columnIndex != -1 && typeConstraint.getColumnIndex() != columnIndex) {
			return false;
		}

		if (columnType != null && typeConstraint.getColumnType() != columnType) {
			return false;
		}

		return true;
	}

	private String _constraintExpression = null;

	private int[] constraintIndex;

	private final int columnIndex;

	private final RType columnType;

	public XRConstraintType(int columnIndex, RType columnType) {
		super();
		this.columnIndex = columnIndex;
		this.columnType = columnType;
		this.constraintIndex = new int[1];
		this.constraintIndex[0] = columnIndex;
	}

	@Override
	public boolean addConstraint(List<IRConstraint1> constraints, List<IRConstraint1> incompatibleConstraints) {

		for (IRConstraint1 preCons : constraints) {

			if (preCons.getConstraintType() != RConstraintType.TYPE) {
				continue;
			}

			XRConstraintType preTypeCons = (XRConstraintType) preCons;
			if (preTypeCons.getColumnIndex() != this.getColumnIndex()) {
				continue;
			}

			if (preTypeCons.getColumnType() != this.getColumnType()) {
				incompatibleConstraints.add(preCons);
			}
			// else: Same type constraint, no need to

			return false;
		}

		return super.addConstraint(constraints, incompatibleConstraints);
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {

		IRObject columnValue = entry.get(columnIndex);
		if (columnValue == null) {
			return true;
		}

		return columnValue.getType() == columnType || columnValue.getType() == RType.NIL;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

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
	public RConstraintType getConstraintType() {
		return RConstraintType.TYPE;
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}

}
