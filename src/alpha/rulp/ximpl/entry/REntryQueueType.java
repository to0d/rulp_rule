package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;

public enum REntryQueueType {

	EMPTY(0), MULTI(1), SINGLE(2), UNIQ(3), ACTION(4);

	public final static int RETE_QUEUE_EMPTY = 0;

	public final static int RETE_QUEUE_MULTI = 1;

	public final static int RETE_QUEUE_SINGLE = 2;

	public final static int RETE_QUEUE_UNIQ = 3;

	public final static int RETE_QUEUE_ACTION = 4;

	private int index;

	private REntryQueueType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public static REntryQueueType toEntryQueueType(int tv) throws RException {

		switch (tv) {
		case RETE_QUEUE_EMPTY:
			return EMPTY;

		case RETE_QUEUE_MULTI:
			return MULTI;

		case RETE_QUEUE_SINGLE:
			return SINGLE;

		case RETE_QUEUE_UNIQ:
			return UNIQ;

		case RETE_QUEUE_ACTION:
			return ACTION;

		default:
			throw new RException("unknown queue type: " + tv);
		}
	}
}
