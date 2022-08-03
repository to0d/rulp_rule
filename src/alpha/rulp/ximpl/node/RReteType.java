package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;

public enum RReteType {

	ROOT0(0), //
	NAME0(1), //
	VAR(2), //
	CONST(3), //
	ALPH0(4), //
	ALPH1(5), //
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
	WORK(16), //
	INDEX(17), //
	INHER(18), // inherit
	OR0(19);

	static final int RRT_ROOT0 = 0;
	static final int RRT_NAME0 = 1;
	static final int RRT_VAR = 2;
	static final int RRT_CONST = 3;
	static final int RRT_ALPH0 = 4;
	static final int RRT_ALPH1 = 5;
	static final int RRT_EXPR0 = 6;
	static final int RRT_EXPR1 = 7;
	static final int RRT_EXPR2 = 8;
	static final int RRT_EXPR3 = 9;
	static final int RRT_EXPR4 = 10;
	static final int RRT_BETA0 = 11;
	static final int RRT_BETA1 = 12;
	static final int RRT_BETA2 = 13;
	static final int RRT_BETA3 = 14;
	static final int RRT_RULE = 15;
	static final int RRT_WORK = 16;
	static final int RRT_INDEX = 17;
	static final int RRT_INHER = 18; 
	static final int RRT_OR0 = 19;

	public static final RReteType ALL_RETE_TYPE[] = { ROOT0, NAME0, VAR, CONST, ALPH0, ALPH1, EXPR0, EXPR1, EXPR2,
			EXPR3, EXPR4, BETA0, BETA1, BETA2, BETA3, RULE, WORK, INDEX, INHER, OR0 };

	public static final int RETE_TYPE_NUM = 20;

	public static boolean isAlphaType(RReteType type) {

		switch (type) {
		case ALPH0:
		case ALPH1:
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