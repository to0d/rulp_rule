package alpha.rulp.rule;

public enum RCountType {

	CreateEntry(0), //
	DeleteEntry(1), //
	FixedCount(2), //
	DefinedCount(3), //
	ReasonCount(4), //
	AssumeCount(5), //
	DropCount(6), //
	RemoveCount(7), //
	TempCount(8), //
	NullCount(9), //
	BindFromCount(10), //
	BindToCount(11), //
	NodeCount(12), //
	SourceCount(13), //
	ExecCount(14), //
	IdleCount(15), //
	FailedCount(16), //
	MatchCount(17), //
	UpdateCount(18), //
	RedundantCount(19), //
	MinLevel(20), //
	MaxLevel(21), //
	MinPriority(22), //
	MaxPriority(23), //
	QueryMatch(24), //
	QueryFetch(25); //

	public static final int COUNT_TYPE_NUM = 26;

	public static final RCountType ALL_COUNT_TYPE[] = { CreateEntry, DeleteEntry, FixedCount, DefinedCount, ReasonCount,
			AssumeCount, DropCount, RemoveCount, TempCount, NullCount, BindFromCount, BindToCount, NodeCount,
			SourceCount, ExecCount, IdleCount, FailedCount, MatchCount, UpdateCount, RedundantCount, MinLevel, MaxLevel,
			MinPriority, MaxPriority, QueryMatch, QueryFetch };

	private int index;

	private RCountType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
