package alpha.rulp.rule;

public enum RCountType {

	FixedCount(0), //
	DefinedCount(1), //
	ReasonCount(2), //
	AssumeCount(3), //
	DropCount(4), //
	RemoveCount(5), //
	TempCount(6), //
	NullCount(7), //
	BindFromCount(8), //
	BindToCount(9), //
	NodeCount(10), //
	SourceCount(11), //
	ExecCount(12), //
	IdleCount(13), //
	FailedCount(14), //
	MatchCount(15), //
	UpdateCount(16), //
	RedundantCount(17), //
	MinLevel(18), //
	MaxLevel(19), //
	MinPriority(20), //
	MaxPriority(21), //
	QueryMatch(22), //
	QueryFetch(23); //

	public static final int COUNT_TYPE_NUM = 24;

	public static final RCountType ALL_COUNT_TYPE[] = { FixedCount, DefinedCount, ReasonCount, AssumeCount, DropCount,
			RemoveCount, TempCount, NullCount, BindFromCount, BindToCount, NodeCount, SourceCount, ExecCount, IdleCount,
			FailedCount, MatchCount, UpdateCount, RedundantCount, MinLevel, MaxLevel, MinPriority, MaxPriority,
			QueryMatch, QueryFetch };

	private int index;

	private RCountType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
