package alpha.rulp.ximpl.node;

public enum RReteType {

	ROOT0(0), //
	NAME0(1), //
	VAR(2), //
	CONST(3), //
	ALPH0(4), //
	ALPH1(5), //
	ALPH2(6), //
	EXPR0(7), //
	EXPR1(8), //
	EXPR2(9), //
	EXPR3(10), //
	EXPR4(11), //
	BETA0(12), //
	BETA1(13), //
	BETA2(14), //
	BETA3(15), //
	RULE(16), //
	WORK(17), //
	DROP(18); //

	public static final RReteType ALL_RETE_TYPE[] = { ROOT0, NAME0, VAR, CONST, ALPH0, ALPH1, ALPH2, EXPR0, EXPR1,
			EXPR2, EXPR3, EXPR4, BETA0, BETA1, BETA2, BETA3, RULE, WORK, DROP };

	public static final int RETE_TYPE_NUM = 19;

	public static boolean isAlphaType(RReteType type) {

		switch (type) {
		case ALPH0:
		case ALPH1:
		case ALPH2:
			return true;
		default:
			return false;
		}
	}

	public static boolean isRootType(RReteType type) {

		switch (type) {
		case ROOT0:
		case NAME0:
			return true;
		default:
			return false;
		}
	}

	private int index;

	private RReteType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}