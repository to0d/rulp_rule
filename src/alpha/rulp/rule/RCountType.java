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
	NodeExistCount(12), //
	NodeSourceCount(13), //
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

	public static final RCountType ALL_COUNT_TYPE[] = { EntryCreateCount, EntryDeleteCount, EntryFixedCount,
			EntryDefinedCount, EntryReasonCount, EntryAssumeCount, EntryDropCount, EntryRemoveCount, EntryTempCount,
			EntryNullCount, NodeBindFromCount, NodeBindToCount, NodeExistCount, NodeSourceCount, ExecCount, IdleCount,
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
