package alpha.rulp.ximpl.node;

public enum RReteType {

	ROOT0(0), //
	NAME0(1), //
	VAR(2), //
	CONST(3), //
	ALPH0(4), //
	ALPH1(5), //
//	ALPH2(6), //
	EXPR0(6), //
	EXPR1(7), //
	EXPR2(8), //
	EXPR3(9), //
	EXPR4(10), //
	BETA0(11), //
	BETA1(12), //
	BETA2(13), //
	BETA3(14), //
	RULE(15), //
	WORK(16); //

	public static final RReteType ALL_RETE_TYPE[] = { ROOT0, NAME0, VAR, CONST, ALPH0, ALPH1, EXPR0, EXPR1, EXPR2,
			EXPR3, EXPR4, BETA0, BETA1, BETA2, BETA3, RULE, WORK };

	public static final int RETE_TYPE_NUM = 17;

	public static boolean isAlphaType(RReteType type) {

		switch (type) {
		case ALPH0:
		case ALPH1:
//		case ALPH2:
			return true;
		default:
			return false;
		}
	}

	public static boolean isBetaType(RReteType type) {

		switch (type) {
		case BETA0:
		case BETA1:
		case BETA2:
		case BETA3:
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