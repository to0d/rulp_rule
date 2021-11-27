package alpha.rulp.ximpl.search;

import java.util.ArrayList;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;

public class XLinnerObjectScope implements ISScope<IRObject> {

	protected IRObject curValue;

	protected int curValueIndex = -1;

	protected int evalCount = 0;

	protected int moveCount = 0;

	protected ArrayList<IRObject> possibleValueList = null;

	protected int scanCount = 0;

	protected boolean scanValueCompled = false;

	protected IRIterator<? extends IRObject> valueIterator = null;

	protected IValueList valueList;

	public XLinnerObjectScope(IValueList valueList) {
		super();
		this.valueList = valueList;
	}

	public IRObject curValue() {
		return curValue;
	}

	public int getCurValueIndex() {
		return curValueIndex;
	}

	public int getValueEvalCount() {
		return evalCount;
	}

	public int getValueMoveCount() {
		return moveCount;
	}

	public int getValuePossibleCount() {
		return possibleValueList == null ? 0 : possibleValueList.size();
	}

	public int getValueScanedCount() {
		return scanCount;
	}

	public void incValueEvalCount() {
		this.evalCount++;
	}

	public boolean isReset() {
		return curValueIndex == -1;
	}

	public boolean isScanCompleted() {
		return scanValueCompled;
	}

	public boolean moveNext() throws RException {

		++XLinnerObjectScope.this.moveCount;
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
			++scanCount;

			findValidValue = true;
			possibleValueList.add(curValue);
			break;
		}

		if (!findValidValue) {
			scanValueCompled = true;
		}

		return findValidValue;
	}

	public void reset() {
		curValueIndex = -1;
	}

	@Override
	public void setChecker(ICheckValue<IRObject> checker) throws RException {
		throw new RException("not support");
	}

}