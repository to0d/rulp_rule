package alpha.rulp.rule;

public enum RCountType {

	EntryCreateCount(0), //
	EntryDeleteCount(1), //
	EntryFixedCount(2), //
	EntryDefinedCount(3), //
	EntryReasonCount(4), //
	EntryAssumeCount(5), //
	EntryDropCount(6), //
	EntryRemoveCount(7), //
	EntryTempCount(8), //
	EntryNullCount(9), //
	NodeBindFromCount(10), //
	NodeBindToCount(11), //
	NodeTotalCount(12), //
	NodeExistCount(13), //
	NodeSourceCount(14), //
	ExecCount(15), //
	IdleCount(16), //
	FailedCount(17), //
	MatchCount(18), //
	UpdateCount(19), //
	RedundantCount(20), //
	MinLevel(21), //
	MaxLevel(22), //
	MinPriority(23), //
	MaxPriority(24), //
	QueryMatch(25), //
	QueryFetch(26); //

	public static final int COUNT_TYPE_NUM = 27;

	public static final RCountType ALL_COUNT_TYPE[] = { EntryCreateCount, EntryDeleteCount, EntryFixedCount,
			EntryDefinedCount, EntryReasonCount, EntryAssumeCount, EntryDropCount, EntryRemoveCount, EntryTempCount,
			EntryNullCount, NodeBindFromCount, NodeBindToCount, NodeTotalCount, NodeExistCount, NodeSourceCount,
			ExecCount, IdleCount, FailedCount, MatchCount, UpdateCount, RedundantCount, MinLevel, MaxLevel, MinPriority,
			MaxPriority, QueryMatch, QueryFetch };

	private int index;

	private RCountType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
