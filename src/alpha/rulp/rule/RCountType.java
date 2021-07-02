package alpha.rulp.rule;

public enum RCountType {

	DefinedCount(0), //
	ReasonCount(1), //
	AssumeCount(2), //
	DropCount(3), //
	NullCount(4), //
	BindFromCount(5), //
	BindToCount(6), //
	NodeCount(7), //
	SourceCount(8), //
	ExecCount(9), //
	IdleCount(10), //
	FailedCount(11), //
	MatchCount(12), //
	UpdateCount(13), //
	RedundantCount(14), //
	MinLevel(15), //
	MaxLevel(16), //
	MinPriority(17), //
	MaxPriority(18), //
	QueryMatch(19), //
	QueryFetch(20); //

	public static final int COUNT_TYPE_NUM = 21;

	public static final RCountType ALL_COUNT_TYPE[] = { DefinedCount, ReasonCount, AssumeCount, DropCount, NullCount,
			BindFromCount, BindToCount, NodeCount, SourceCount, ExecCount, IdleCount, FailedCount, MatchCount,
			UpdateCount, RedundantCount, MinLevel, MaxLevel, MinPriority, MaxPriority, QueryMatch, QueryFetch };

	private int index;

	private RCountType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
