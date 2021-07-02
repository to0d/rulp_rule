package alpha.rulp.rule;

import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.lang.Constant.F_DO;
import static alpha.rulp.lang.Constant.O_From;
import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_On;
import static alpha.rulp.rule.Constant.A_Priority;
import static alpha.rulp.rule.Constant.A_State;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.O_Limit;
import static alpha.rulp.rule.Constant.O_On;
import static alpha.rulp.rule.Constant.O_Priority;
import static alpha.rulp.rule.Constant.O_State;
import static alpha.rulp.rule.Constant.O_Type;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.utils.RulpFactory;

public enum RModifiter {

	FROM(0), // from
	LIMIT(1), // limit
	STATE(2), // state
	TYPE(3), // type
	DO(4), // do
	PRIORITY(5), // priority
	ON(6);// on

	public static RModifiter toModifiter(String keyName) {

		switch (keyName) {
		case A_FROM:
			return RModifiter.FROM;

		case A_Limit:
			return RModifiter.LIMIT;

		case A_State:
			return RModifiter.STATE;

		case A_Type:
			return RModifiter.TYPE;

		case F_DO:
			return RModifiter.DO;

		case A_On:
			return RModifiter.ON;

		case A_Priority:
			return RModifiter.PRIORITY;

		default:
			return null;
		}
	}

	public static IRAtom toObject(RModifiter modifiter) {

		switch (modifiter) {
		case FROM:
			return O_From;

		case LIMIT:
			return O_Limit;

		case STATE:
			return O_State;

		case TYPE:
			return O_Type;

		case DO:
			return RulpFactory.createAtom(F_DO);

		case PRIORITY:
			return O_Priority;

		case ON:
			return O_On;

		default:
			return O_Nil;
		}
	}

	private int index;

	private RModifiter(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}
