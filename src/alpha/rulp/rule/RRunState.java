package alpha.rulp.rule;

import static alpha.rulp.lang.Constant.O_Nan;
import static alpha.rulp.rule.Constant.O_Completed;
import static alpha.rulp.rule.Constant.O_Failed;
import static alpha.rulp.rule.Constant.O_Halting;
import static alpha.rulp.rule.Constant.O_Partial;
import static alpha.rulp.rule.Constant.O_Runnable;
import static alpha.rulp.rule.Constant.O_Running;

import alpha.rulp.lang.IRObject;

public enum RRunState {

	Completed(0), Runnable(1), Running(2), Halting(3), Failed(4), Partial(5);

	private int index;

	public int getIndex() {
		return index;
	}

	private RRunState(int index) {
		this.index = index;
	}

	public static IRObject toObject(RRunState state) {
		switch (state) {
		case Completed:
			return O_Completed;

		case Failed:
			return O_Failed;

		case Halting:
			return O_Halting;

		case Runnable:
			return O_Runnable;

		case Running:
			return O_Running;

		case Partial:
			return O_Partial;

		default:
			return O_Nan;
		}
	}
}
