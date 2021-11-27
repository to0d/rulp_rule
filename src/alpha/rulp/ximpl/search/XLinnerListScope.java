package alpha.rulp.ximpl.search;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.RException;

public class XLinnerListScope<T> implements ISScope<List<T>> {

	private ICheckValue<List<T>> checker;

	private List<T> curList = null;

	private ArrayList<ISScope<T>> elementScopes;

	private boolean queryCompleted = false;

	public XLinnerListScope(List<ISScope<T>> elementScopes) {
		super();
		this.elementScopes = new ArrayList<>(elementScopes);
	}

	protected boolean _can_move() throws RException {

		int varPos = 0;

		int elementCount = elementScopes.size();

		while (varPos < elementCount) {

//			++moveCount;

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
	public boolean moveNext() throws RException {

		if (queryCompleted) {
			return false;
		}

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

			while (!checker.isValid(curList)) {

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
		for (ISScope<T> scope : elementScopes) {
			scope.reset();
		}
	}

	public void setChecker(ICheckValue<List<T>> checker) {
		this.checker = checker;
	}
}
