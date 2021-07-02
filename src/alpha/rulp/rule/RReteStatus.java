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
import alpha.rulp.lang.RType;

public enum RReteStatus {

	DEFINED(1), //
	REASONED(2), //
	ASSUMED(4), //
	REMOVED(8);

	public static final RReteStatus ALL_RETE_STATUS[] = { DEFINED, REASONED, ASSUMED, REMOVED };

	public static final int RETE_STATUS_NUM = 4;

	private int index;

	private RReteStatus(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public static IRObject toObject(RReteStatus state) {
		switch (state) {
		case ASSUMED:
			return O_Assumed;

		case DEFINED:
			return O_Defined;

		case REMOVED:
			return O_Removed;

		case REASONED:
			return O_Reasoned;

		default:
			return O_Nan;
		}
	}

	public static RReteStatus toStatus(IRObject obj) {

		if (obj.getType() == RType.ATOM) {

			switch (((IRAtom) obj).getName()) {
			case A_Defined:
				return DEFINED;

			case A_Reasoned:
				return REASONED;

			case A_Assumed:
				return ASSUMED;

			case A_Removed:
				return REMOVED;

			}
		}

		return null;
	}
}