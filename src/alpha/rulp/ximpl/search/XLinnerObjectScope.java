package alpha.rulp.ximpl.search;

import java.util.ArrayList;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;

public class XLinnerObjectScope implements ISScope<IRObject> {

	protected IRObject curValue;

	protected int curValueIndex = -1;

	protected int moveCount = 0;

	protected ArrayList<IRObject> possibleValueList = null;

	protected int resetCount = 0;

	protected int scanCount = 0;

	protected boolean scanValueCompled = false;

	protected IRIterator<? extends IRObject> valueIterator = null;

	protected IValueList valueList;

	public XLinnerObjectScope(IValueList valueList) {
		super();
		this.valueList = valueList;
	}

	@Override
	public IRObject curValue() {
		return curValue;
	}

	public int getCurValueIndex() {
		return curValueIndex;
	}

	@Override
	public int getEvalCount() {
		return 0;
	}

	@Override
	public int getMoveCount() {
		return moveCount;
	}

	@Override
	public int getResetCount() {
		return resetCount;
	}

	public int getValuePossibleCount() {
		return possibleValueList == null ? 0 : possibleValueList.size();
	}

	public int getValueScanedCount() {
		return scanCount;
	}

	@Override
	public boolean isCompleted() {
		return scanValueCompled;
	}

	public boolean isReset() {
		return curValueIndex == -1;
	}

	public boolean isScanCompleted() {
		return scanValueCompled;
	}

	@Override
	public boolean moveNext() throws RException {

		this.moveCount++;
		this.curValue = null;

		if (possibleValueList == null) {
			possibleValueList = new ArrayList<>();
			curValueIndex = -1;
		}

		// Move to next index
		curValueIndex++;

		if (curValueIndex < possibleValueList.size()) {
			this.curValue = possibleValueList.get(curValueIndex);
			return true;
		}

		if (scanValueCompled) {
			return false;
		}

		if (valueIterator == null) {
			valueIterator = valueList.iterator();
		}

		boolean findValidValue = false;

		while (valueIterator.hasNext()) {

			this.curValue = valueIterator.next();
			this.scanCount++;

			findValidValue = true;
			possibleValueList.add(curValue);
			break;
		}

		if (!findValidValue) {
			scanValueCompled = true;
		}

		return findValidValue;
	}

	@Override
	public void reset() {
		curValueIndex = -1;
		resetCount++;
	}

	@Override
	public void setChecker(ICheckValue<IRObject> checker) throws RException {
		throw new RException("not support");
	}
}