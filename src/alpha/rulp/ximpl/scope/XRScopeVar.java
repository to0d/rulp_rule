//package alpha.rulp.ximpl.scope;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//
//import alpha.rulp.lang.IRObject;
//import alpha.rulp.lang.IRVar;
//import alpha.rulp.lang.RException;
//import alpha.rulp.lang.RType;
//import alpha.rulp.runtime.IRIterator;
//import alpha.rulp.ximpl.search.IValueList;
//
//public class XRScopeVar implements IRScopeVar {
//
//	protected IRVar var;
//
//	protected RType varType;
//
//	protected IValueList valueFactory;
//
//	protected List<IVarConstraint> singleConstraintExprList = null;
//
//	protected List<IVarConstraint> crossConstraintExprList = null;
//
//	protected IRIterator<? extends IRObject> valueIterator = null;
//
//	protected ArrayList<IRObject> possibleValueList = null;
//
//	protected int curValueIndex = -1;
//
//	protected boolean scanValueCompled = false;
//
//	protected int evalCount = 0;
//
//	protected int scanCount = 0;
//
//	protected int moveCount = 0;
//
//	@Override
//	public void addConstraint(IVarConstraint constraint) {
//
//		if (constraint.isSingleConstraint()) {
//
//			if (singleConstraintExprList == null) {
//				singleConstraintExprList = new LinkedList<>();
//			}
//
//			singleConstraintExprList.add(constraint);
//
//		} else {
//
//			if (crossConstraintExprList == null) {
//				crossConstraintExprList = new LinkedList<>();
//			}
//
//			crossConstraintExprList.add(constraint);
//		}
//
//	}
//
//	public boolean checkCrossConstraint() throws RException {
//
//		if (crossConstraintExprList != null) {
//
//			for (IVarConstraint constraint : crossConstraintExprList) {
//
//				// Invalid value
//				if (!constraint.checkConstraint()) {
//					return false;
//				}
//			}
//		}
//
//		return true;
//	}
//
//	public boolean checkSingleConstraint() throws RException {
//
//		if (singleConstraintExprList != null) {
//
//			for (IVarConstraint constraint : singleConstraintExprList) {
//
//				// Invalid value
//				if (!constraint.checkConstraint()) {
//					return false;
//				}
//			}
//		}
//
//		return true;
//	}
//
//	@Override
//	public int getConstraintCount() {
//
//		int count = 0;
//
//		if (singleConstraintExprList != null) {
//			count += singleConstraintExprList.size();
//		}
//
//		if (crossConstraintExprList != null) {
//			count += crossConstraintExprList.size();
//		}
//
//		return count;
//	}
//
//	@Override
//	public int getCurValueIndex() {
//		return curValueIndex;
//	}
//
//	@Override
//	public String getDescription() {
//
//		String out = "";
//
//		if (valueFactory == null) {
//			out += "scope=null";
//		} else {
//			out += valueFactory.toString();
//		}
//
//		return out;
//	}
//
//	@Override
//	public int getValueEvalCount() {
//		return evalCount;
//	}
//
//	@Override
//	public int getValueMoveCount() {
//		return XRScopeVar.this.moveCount;
//	}
//
//	@Override
//	public int getValuePossibleCount() {
//		return possibleValueList == null ? 0 : possibleValueList.size();
//	}
//
//	@Override
//	public int getValueScanedCount() {
//		return scanCount;
//	}
//
//	@Override
//	public IRVar getVar() {
//		return var;
//	}
//
//	@Override
//	public RType getVarType() {
//		return varType;
//	}
//
//	public boolean hasValueScope() {
//
//		switch (varType) {
//		case INT:
//			return valueFactory != null;
//
//		default:
//			return false;
//		}
//	}
//
//	public boolean isReset() {
//		return curValueIndex == -1;
//	}
//
//	@Override
//	public boolean isScanCompleted() {
//		return scanValueCompled;
//	}
//
//	public boolean moveNext() throws RException {
//
//		++XRScopeVar.this.moveCount;
//
//		if (possibleValueList == null) {
//			possibleValueList = new ArrayList<>();
//			curValueIndex = -1;
//		}
//
//		// Move to next index
//		curValueIndex++;
//
//		if (curValueIndex < possibleValueList.size()) {
//
//			IRObject val = possibleValueList.get(curValueIndex);
//
//			if (XRScope.TRACE) {
//				System.out.println(String.format("set var(%s)=%s", var.getName(), "" + val));
//			}
//
//			var.setValue(val);
//			return true;
//		}
//
//		if (scanValueCompled) {
//			return false;
//		}
//
//		if (valueIterator == null) {
//			valueIterator = valueFactory.iterator();
//		}
//
//		boolean findValidValue = false;
//
//		NEXT_VAL: while (valueIterator.hasNext()) {
//
//			IRObject val = valueIterator.next();
//			++scanCount;
//
//			if (XRScope.TRACE) {
//				System.out.println(String.format("set var(%s)=%s", var.getName(), "" + val));
//			}
//
//			var.setValue(val);
//
//			if (!checkSingleConstraint()) {
//				continue NEXT_VAL;
//			}
//
//			findValidValue = true;
//			possibleValueList.add(val);
//			break;
//		}
//
//		if (!findValidValue) {
//			scanValueCompled = true;
//		}
//
//		return findValidValue;
//	}
//
//	public void resetValueIndex() {
//		curValueIndex = -1;
//	}
//
//	@Override
//	public void incValueEvalCount() {
//		this.evalCount++;
//	}
//}
