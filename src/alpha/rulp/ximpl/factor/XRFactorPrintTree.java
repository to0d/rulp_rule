package alpha.rulp.ximpl.factor;

import static alpha.rulp.lang.Constant.O_Nil;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.runtime.IRFactor;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.model.IRuleFactor;

public class XRFactorPrintTree extends AbsAtomFactorAdapter implements IRFactor, IRuleFactor {

	static class TreePrinter {

		static final int MAX_DEEP = 256;

		static final int PRE_PATH_LEAF = 2; // "`-"

		static final int PRE_PATH_LINE = 1; // "|-"

		static final int PRE_PATH_LIXX = 3; // "| "

		static final int PRE_PATH_NONE = 0; // " "

		private int anonymousCount = 0;

		private boolean leftAsTop = false;

		private int[] paths = new int[MAX_DEEP];

		public TreePrinter(boolean leftAsTop) {
			super();
			this.leftAsTop = leftAsTop;
		}

		public String print(IRObject obj) throws RException {

			StringBuffer sb = new StringBuffer();
			printTree(sb, obj, 0);
			return sb.toString();
		}

		public void printTree(StringBuffer sb, IRObject obj, int column) throws RException {

			for (int i = 0; i < column; ++i) {

				int v = paths[i];
				switch (v) {
				case PRE_PATH_LINE:
					sb.append("|-");
					break;

				case PRE_PATH_LEAF:
					sb.append("`-");
					break;

				case PRE_PATH_LIXX:
					sb.append("| ");
					break;

				case PRE_PATH_NONE:
				default:
					sb.append("  ");
					break;
				}
			}

			if (column > 0) {

				int v = paths[column - 1];
				switch (v) {
				case PRE_PATH_LINE:
					paths[column - 1] = PRE_PATH_LIXX;
					break;

				case PRE_PATH_LEAF:
					paths[column - 1] = PRE_PATH_NONE;
					break;
				}
			}

			if (!_isList(obj) || _isSimpleList(obj)) {
				sb.append(RulpUtil.toString(obj));
				sb.append("\n");
				return;
			}

			IRList list = (IRList) obj;
			int size = list.size();
			int fromIndex = 0;

			if (leftAsTop) {

				IRObject e0 = list.get(0);
				if (!_isList(e0) || _isSimpleList(e0)) {

					sb.append(RulpUtil.toString(e0));

					if (list.getType() == RType.LIST && list.getNamedName() != null) {
						sb.append(String.format(" (%s:)", list.getNamedName()));
					}

					if (list.getType() == RType.EXPR) {
						sb.append(" (expr)");
					}

					sb.append("\n");
					fromIndex++;
				}
			}

			if (fromIndex == 0) {

				sb.append(String.format("%s%d", obj.getType() == RType.LIST ? "L" : "E", anonymousCount++));
				if (list.getType() == RType.LIST && list.getNamedName() != null) {
					sb.append(String.format(" (%s:)", list.getNamedName()));
				}

				sb.append("\n");
			}

			// output other elements
			for (int i = fromIndex; i < size; ++i) {
				paths[column] = ((i + 1) < size) ? PRE_PATH_LINE : PRE_PATH_LEAF;
				printTree(sb, list.get(i), column + 1);
			}

			return;
		}
	}

	static boolean _isList(IRObject obj) {
		return obj.getType() == RType.LIST || obj.getType() == RType.EXPR;
	}

	static boolean _isSimpleList(IRObject obj) throws RException {

		if (!_isList(obj)) {
			return false;
		}

		IRIterator<? extends IRObject> it = ((IRList) obj).iterator();
		while (it.hasNext()) {
			if (_isList(it.next())) {
				return false;
			}
		}

		return true;
	}

	public XRFactorPrintTree(String factorName) {
		super(factorName);
	}

	@Override
	public IRObject compute(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (args.size() != 2 && args.size() != 3) {
			throw new RException("Invalid parameters: " + args);
		}

		IRObject obj = args.get(1);
		if (!_isList(obj)) {
			obj = interpreter.compute(frame, obj);
			if (!_isList(obj)) {
				throw new RException("Invalid parameters: " + args);
			}
		}

		boolean leftAsTop = false;

		if (args.size() > 2) {
			leftAsTop = RulpUtil.asBoolean(interpreter.compute(frame, args.get(2))).asBoolean();
		}

		interpreter.out(new TreePrinter(leftAsTop).print(obj));
		return O_Nil;
	}
}
