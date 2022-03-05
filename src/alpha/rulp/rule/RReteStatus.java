package alpha.rulp.rule;

import static alpha.rulp.lang.Constant.O_Nan;
import static alpha.rulp.rule.Constant.A_Assumed;
import static alpha.rulp.rule.Constant.A_Defined;
import static alpha.rulp.rule.Constant.A_Reasoned;
import static alpha.rulp.rule.Constant.A_Removed;
import static alpha.rulp.rule.Constant.O_Assumed;
import static alpha.rulp.rule.Constant.O_Defined;
import static alpha.rulp.rule.Constant.O_Reasoned;
import static alpha.rulp.rule.Constant.O_Removed;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;

public enum RReteStatus {

	DEFINE(0, 1, "define"), //
	REASON(1, 2, "reason"), //
	ASSUME(2, 4, "assume"), //
	REMOVE(3, 8, "remove"), //
	FIXED_(4, 16, "fixed"), //
	TEMP__(5, 32, "temp"), //
	CLEAN(6, 64, "clean"); //

	static final int RRS_DEFINE = 0;
	static final int RRS_REASON = 1;
	static final int RRS_ASSUME = 2;
	static final int RRS_REMOVE = 3;
	static final int RRS_FIXED = 4;
	static final int RRS_TEMP = 5;
	static final int RRS_CLEAN = 6;

	public static final RReteStatus ALL_RETE_STATUS[] = { DEFINE, REASON, ASSUME, REMOVE, FIXED_, TEMP__, CLEAN };

	public static final int RETE_STATUS_NUM = 7;

	public static RReteStatus getRetetStatus(int ts) throws RException {

		switch (ts) {
		case RRS_DEFINE:
			return DEFINE;

		case RRS_REASON:
			return REASON;

		case RRS_ASSUME:
			return ASSUME;

		case RRS_REMOVE:
			return REMOVE;

		case RRS_FIXED:
			return FIXED_;

		case RRS_TEMP:
			return TEMP__;

		case RRS_CLEAN:
			return CLEAN;

		default:
			throw new RException("invalid unknown RRS value: " + ts);
		}
	}

	public static IRObject toObject(RReteStatus state) {
		switch (state) {
		case ASSUME:
			return O_Assumed;

		case DEFINE:
			return O_Defined;

		case REMOVE:
			return O_Removed;

		case REASON:
			return O_Reasoned;

		default:
			return O_Nan;
		}
	}

	public static RReteStatus toStatus(IRObject obj) {

		if (obj.getType() == RType.ATOM) {

			switch (((IRAtom) obj).getName()) {
			case A_Defined:
				return DEFINE;

			case A_Reasoned:
				return REASON;

			case A_Assumed:
				return ASSUME;

			case A_Removed:
				return REMOVE;

			}
		}

		return null;
	}

	private final int mask;

	private final int index;

	private final String toString;

	public int getIndex() {
		return index;
	}

	public String toString() {
		return toString;
	}

	private RReteStatus(int index, int mask, String toString) {
		this.index = index;
		this.mask = mask;
		this.toString = toString;
	}

	public int getMask() {
		return mask;
	}
}