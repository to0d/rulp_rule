package alpha.rulp.ximpl.search;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.RException;

public class XLinnerListScope<T> implements ISScope<List<T>> {

	protected ICheckValue<List<T>> checker;

	protected List<T> curList = null;

	protected ArrayList<ISScope<T>> elementScopes;

	protected int evalCount = 0;

	protected int moveCount = 0;

	protected boolean queryCompleted = false;

	protected int resetCount = 0;

	public XLinnerListScope(List<ISScope<T>> elementScopes) {
		super();
		this.elementScopes = new ArrayList<>(elementScopes);
	}

	protected boolean _can_move() throws RException {

		int varPos = 0;
		int elementCount = elementScopes.size();

		while (varPos < elementCount) {

			ISScope<T> scope = elementScopes.get(varPos);
			if (scope.moveNext()) {
				return true;
			}

			scope.reset();

			// no first value
			if (!scope.moveNext()) {
				return false;
			}

			++varPos;
		}

		return false;
	}

	protected boolean _isValid(List<T> value) throws RException {
		evalCount++;
		return checker.isValid(value);
	}

	protected void _updateList() {

		int elementCount = elementScopes.size();
		for (int i = 0; i < elementCount; ++i) {
			curList.set(i, elementScopes.get(i).curValue());
		}
	}

	@Override
	public List<T> curValue() {
		return curList;
	}

	@Override
	public int getEvalCount() {
		return evalCount;
	}

	@Override
	public int getMoveCount() {
		return moveCount;
	}

	@Override
	public int getResetCount() {
		return resetCount;
	}

	@Override
	public boolean moveNext() throws RException {

		if (queryCompleted) {
			return false;
		}

		this.moveCount++;

		if (curList == null) {

			// Initialize first value
			for (ISScope<T> scope : elementScopes) {
				if (!scope.moveNext()) {
					queryCompleted = true;
					return false;
				}
			}

			curList = new ArrayList<>();
			for (ISScope<T> scope : elementScopes) {
				curList.add(scope.curValue());
			}

		} else {

			if (!_can_move()) {
				queryCompleted = true;
				return false;
			}

			_updateList();
		}

		if (checker != null) {

			while (!_isValid(curList)) {

				if (!_can_move()) {
					queryCompleted = true;
					return false;
				}

				_updateList();
			}
		}

		return true;
	}

	@Override
	public void reset() {

		queryCompleted = false;
		curList = null;
		resetCount++;

		for (ISScope<T> scope : elementScopes) {
			scope.reset();
		}
	}

	public void setChecker(ICheckValue<List<T>> checker) {
		this.checker = checker;
	}
}
