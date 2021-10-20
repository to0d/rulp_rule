//package alpha.rulp.rule;
//
//import static alpha.rulp.lang.Constant.A_FROM;
//import static alpha.rulp.lang.Constant.F_DO;
//import static alpha.rulp.lang.Constant.O_From;
//import static alpha.rulp.lang.Constant.O_Nil;
//import static alpha.rulp.rule.Constant.A_Limit;
//import static alpha.rulp.rule.Constant.A_On;
//import static alpha.rulp.rule.Constant.A_Priority;
//import static alpha.rulp.rule.Constant.A_State;
//import static alpha.rulp.rule.Constant.A_Type;
//import static alpha.rulp.rule.Constant.A_Where;
//import static alpha.rulp.rule.Constant.O_Limit;
//import static alpha.rulp.rule.Constant.O_On;
//import static alpha.rulp.rule.Constant.O_Priority;
//import static alpha.rulp.rule.Constant.O_State;
//import static alpha.rulp.rule.Constant.O_Type;
//import static alpha.rulp.rule.Constant.O_Where;
//
//import alpha.rulp.lang.IRAtom;
//import alpha.rulp.utils.RulpFactory;
//
//public enum RModifiter {
//
//	FROM(0), // from
//	LIMIT(1), // limit
//	STATE(2), // state
//	TYPE(3), // type
//	DO(4), // do
//	PRIORITY(5), // priority
//	ON(6), // on
//	WHERE(7); // where
//
//	public static IRAtom toObject(RModifiter modifiter) {
//
//		switch (modifiter) {
//		case FROM:
//			return O_From;
//
//		case LIMIT:
//			return O_Limit;
//
//		case STATE:
//			return O_State;
//
//		case TYPE:
//			return O_Type;
//
//		case DO:
//			return RulpFactory.createAtom(F_DO);
//
//		case PRIORITY:
//			return O_Priority;
//
//		case ON:
//			return O_On;
//
//		case WHERE:
//			return O_Where;
//
//		default:
//			return O_Nil;
//		}
//	}
//
//	private int index;
//
//	private RModifiter(int index) {
//		this.index = index;
//	}
//
//	public int getIndex() {
//		return index;
//	}
//
//}
