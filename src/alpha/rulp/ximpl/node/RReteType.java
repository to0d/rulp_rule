package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;

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
	ZETA0(16), //
	RULE(17), //
	WORK(18), //
	INDEX(19), //
	INHER(20), // inherit
	OR0(21), //
	DUP(22); //

	static final int RRT_ROOT0 = 0;
	static final int RRT_NAME0 = 1;
	static final int RRT_VAR = 2;
	static final int RRT_CONST = 3;
	static final int RRT_ALPH0 = 4;
	static final int RRT_ALPH1 = 5;
	static final int RRT_ALPH2 = 6;
	static final int RRT_EXPR0 = 7;
	static final int RRT_EXPR1 = 8;
	static final int RRT_EXPR2 = 9;
	static final int RRT_EXPR3 = 10;
	static final int RRT_EXPR4 = 11;
	static final int RRT_BETA0 = 12;
	static final int RRT_BETA1 = 13;
	static final int RRT_BETA2 = 14;
	static final int RRT_BETA3 = 15;
	static final int RRT_ZETA0 = 16;
	static final int RRT_RULE = 17;
	static final int RRT_WORK = 18;
	static final int RRT_INDEX = 19;
	static final int RRT_INHER = 20;
	static final int RRT_OR0 = 21;
	static final int RRT_DUP = 22;

	public static final RReteType ALL_RETE_TYPE[] = { ROOT0, NAME0, VAR, CONST, ALPH0, ALPH1, ALPH2, EXPR0, EXPR1,
			EXPR2, EXPR3, EXPR4, BETA0, BETA1, BETA2, BETA3, ZETA0, RULE, WORK, INDEX, INHER, OR0, DUP };

	public static final int RETE_TYPE_TOTAL = 23;

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

	public static RReteType getRetetType(int tv) throws RException {

		switch (tv) {
		case RRT_ROOT0:
			return ROOT0;

		case RRT_NAME0:
			return NAME0;

		case RRT_VAR:
			return VAR;

		case RRT_CONST:
			return CONST;

		case RRT_ALPH0:
			return ALPH0;

		case RRT_ALPH1:
			return ALPH1;

		case RRT_ALPH2:
			return ALPH2;

		case RRT_EXPR0:
			return EXPR0;

		case RRT_EXPR1:
			return EXPR1;

		case RRT_EXPR2:
			return EXPR2;

		case RRT_EXPR3:
			return EXPR3;

		case RRT_EXPR4:
			return EXPR4;

		case RRT_BETA0:
			return BETA0;

		case RRT_BETA1:
			return BETA1;

		case RRT_BETA2:
			return BETA2;

		case RRT_BETA3:
			return BETA3;

		case RRT_ZETA0:
			return ZETA0;

		case RRT_RULE:
			return RULE;

		case RRT_WORK:
			return WORK;

		case RRT_INDEX:
			return INDEX;

		case RRT_INHER:
			return INHER;

		case RRT_OR0:
			return OR0;

		case RRT_DUP:
			return DUP;

		default:
			throw new RException("invalid unknown RRT value: " + tv);
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