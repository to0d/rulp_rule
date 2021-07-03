package alpha.rulp.rule;

public enum RCountType {

	FixedCount(0), //
	DefinedCount(1), //
	ReasonCount(2), //
	AssumeCount(3), //
	DropCount(4), //
	NullCount(5), //
	BindFromCount(6), //
	BindToCount(7), //
	NodeCount(8), //
	SourceCount(9), //
	ExecCount(10), //
	IdleCount(11), //
	FailedCount(12), //
	MatchCount(13), //
	UpdateCount(14), //
	RedundantCount(15), //
	MinLevel(16), //
	MaxLevel(17), //
	MinPriority(18), //
	MaxPriority(19), //
	QueryMatch(20), //
	QueryFetch(21); //

	public static final int COUNT_TYPE_NUM = 22;

	public static final RCountType ALL_COUNT_TYPE[] = { FixedCount, DefinedCount, ReasonCount, AssumeCount, DropCount,
			NullCount, BindFromCount, BindToCount, NodeCount, SourceCount, ExecCount, IdleCount, FailedCount,
			MatchCount, UpdateCount, RedundantCount, MinLevel, MaxLevel, MinPriority, MaxPriority, QueryMatch,
			QueryFetch };

	private int index;

	private RCountType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
