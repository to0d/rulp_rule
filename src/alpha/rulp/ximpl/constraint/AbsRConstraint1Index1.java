package alpha.rulp.ximpl.constraint;

public abstract class AbsRConstraint1Index1 extends AbsRConstraint1 implements IRConstraint1 {

	protected int[] constraintIndex;

	protected int index;

	public AbsRConstraint1Index1(int index) {
		super();
		this.index = index;
		this.constraintIndex = new int[1];
		this.constraintIndex[0] = index;
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}
}
