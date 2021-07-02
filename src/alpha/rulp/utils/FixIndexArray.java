package alpha.rulp.utils;

public class FixIndexArray {

	static final int DEFAULT_CAPACITY = 16;

	static final int ENTRY_FIX_LEN = 3;

	static final int ENTRY_LOC_FRONT = 1;

	static final int ENTRY_LOC_MALLOC = 2;

	static final int ENTRY_LOC_NEXT = 0;

	static final int VALUE_FREE = 0;

	static final int VALUE_USED = 1;

	private int capacity;

	private int[] data;

	private final int dataLen;

	private final int entryLen;

	private int freeIndex;

	private int freeSize;

	private int maxIndex;

	private int usedSize;

	public FixIndexArray(int dataLen) {
		this.dataLen = dataLen;
		this.entryLen = dataLen + ENTRY_FIX_LEN;
		_int(DEFAULT_CAPACITY);
	}

	public FixIndexArray(int dataLen, int intCapacity) {
		this.dataLen = dataLen;
		this.entryLen = dataLen + ENTRY_FIX_LEN;
		_int(intCapacity);
	}

	private void _ensureCapacity(int minCapacity) {

		if (this.capacity >= minCapacity) {
			return;
		}

		int newCapacity = capacity * 2;
		while (newCapacity < minCapacity) {
			newCapacity *= 2;
		}

		int[] new_data = new int[entryLen * newCapacity];
		System.arraycopy(data, 0, new_data, 0, data.length);

		this.capacity = newCapacity;
		this.data = new_data;
	}

	private void _free(int index) {

		/*****************************************/
		// unLink
		/*****************************************/
		_unLink(index);

		/*****************************************/
		// free node
		/*****************************************/
		if (freeIndex != 0) {
			_link(index, freeIndex);
		}

		freeIndex = index;
		++freeSize;
		--usedSize;
	}

	private int _frontII(int index) {
		return index * entryLen + ENTRY_LOC_FRONT;
	}

	private void _int(int intCapacity) {

		this.freeIndex = 0;
		this.freeSize = 0;
		this.usedSize = 0;
		this.maxIndex = 0;
		this.capacity = intCapacity;
		this.data = new int[entryLen * intCapacity];
	}

	private void _link(int fromIndex, int toIndex) {
		data[_nextII(fromIndex)] = toIndex;
		data[_frontII(toIndex)] = fromIndex;
	}

	private int _malloc() {

		int mallocIndex = 0;

		if (freeIndex != 0) {

			mallocIndex = freeIndex;
			int nextIndex = data[_nextII(freeIndex)];

			_unLink(freeIndex);
			freeIndex = nextIndex;
			--freeSize;

		} else {

			mallocIndex = ++maxIndex;
			_ensureCapacity(mallocIndex + 1);
		}

		++usedSize;

		return mallocIndex;
	}

	private int _mallocII(int index) {
		return index * entryLen + ENTRY_LOC_MALLOC;
	}

	private int _nextII(int index) {
		return index * entryLen + ENTRY_LOC_NEXT;
	}

	private void _unLink(int index) {

		int frontII = _frontII(index);
		int nextII = _nextII(index);

		int frontIndex = data[frontII];
		int nextIndex = data[nextII];

		if (frontIndex != 0) {
			data[_nextII(frontIndex)] = nextIndex;
		}

		if (nextIndex != 0) {
			data[_frontII(nextIndex)] = frontIndex;
		}

		data[frontII] = 0;
		data[nextII] = 0;
	}

	public int free(int index) {

		int mallocAddr = _mallocII(index);
		if (data[mallocAddr] != VALUE_USED) {
			throw new RuntimeException(
					String.format("Invalid malloc value: index=%d, value=%d", index, data[mallocAddr]));
		}

		data[mallocAddr] = VALUE_FREE;
		int nextIndex = data[_nextII(index)];
		_free(index);

		return nextIndex;
	}

	public int getCapacity() {
		return capacity;
	}

	public int[] getData() {
		return data;
	}

	public int getDataAddr(int index) {
		return index * entryLen + ENTRY_FIX_LEN;
	}

	public int getDataLen() {
		return dataLen;
	}

	public int getFreeIndex() {
		return freeIndex;
	}

	public int getFreeSize() {
		return freeSize;
	}

	public int getFrontIndex(int index) {
		return data[_frontII(index)];
	}

	public int getMaxIndex() {
		return maxIndex;
	}

	public int getNextIndex(int index) {
		return data[_nextII(index)];
	}

	public int getUsedSize() {
		return usedSize;
	}

	public boolean isUsed(int index) {
		return data[_mallocII(index)] == VALUE_USED;
	}

	public int malloc() {

		int mallocIndex = _malloc();

		int addr = _mallocII(mallocIndex);

		if (data[addr] != VALUE_FREE) {
			throw new RuntimeException(
					String.format("Invalid malloc value: index=%d, value=%d", mallocIndex, data[addr]));
		}

		// Mark it as used
		data[addr++] = VALUE_USED;

		// Clear data
		for (int i = 0; i < dataLen; ++i, ++addr) {
			data[addr] = 0;
		}

		return mallocIndex;
	}

	public int malloc(int frontIndex) {

		int mallocIndex = malloc();

		if (frontIndex != 0) {
			_link(mallocIndex, frontIndex);
		}

		return mallocIndex;
	}
}
