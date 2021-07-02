package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.O_Nil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRFrameEntry;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.RModifiter;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;

public class ModifiterUtil {

	public static class ModifiterData {

		public ArrayList<IRExpr> doList = new ArrayList<>();
		public ArrayList<IRList> fromList = new ArrayList<>();
		public int limit = -1;
		public int priority = -1;
		public List<RModifiter> processedModifier = new LinkedList<>();
		public int state = 0;
		public RType type = null;
		public IRObject on = null;

		public String asString() throws RException {

			ArrayList<IRObject> list = new ArrayList<>();
			for (RModifiter modifiter : processedModifier) {

				list.add(RModifiter.toObject(modifiter));

				switch (modifiter) {
				case FROM:
					list.addAll(fromList);
					break;

				case LIMIT:
					list.add(RulpFactory.createInteger(limit));
					break;

				case STATE:
					for (RReteStatus status : RReteStatus.ALL_RETE_STATUS) {
						if (ReteUtil.matchReteStatus(status, state)) {
							list.add(RReteStatus.toObject(status));
						}
					}
					break;

				case TYPE:
					list.add(RType.toObject(type));
					break;

				case DO:
					list.addAll(doList);
					break;

				case PRIORITY:
					list.add(RulpFactory.createInteger(priority));
					break;

				case ON:
					list.add(on);
					break;

				default:
					throw new RException("Invalid Modifiter: " + modifiter);
				}

			}

			return RulpUtil.toString(RulpFactory.createList(list));
		}
	}

	static int[] MV_LEN = { -1, 1, -1, 1, -1, 1, 1 };

	static RType[] MV_TYP = { null, RType.INT, null, RType.ATOM, RType.EXPR, RType.INT, null };

	static IRObject _compute(IRObject obj, IRFrame frame) throws RException {

		if (obj == null) {
			return O_Nil;
		}

		RType rt = obj.getType();

		switch (rt) {
		case INT:
		case LONG:
		case FLOAT:
		case BOOL:
		case STRING:
		case INSTANCE:
		case NATIVE:
			return obj;

		case VAR: {
			IRVar var = (IRVar) obj;
			IRFrameEntry entry = frame.getEntry(var.getName());
			if (entry == null) {
				throw new RException("var entry not found: " + var);
			}

			return RulpUtil.asVar(entry.getObject());
		}

		case ATOM: {
			IRAtom atom = (IRAtom) obj;
			IRFrameEntry entry = frame.getEntry(atom.getName());
			IRObject rst = entry == null ? obj : entry.getObject();

			if (rst != null && rst.getType() == RType.VAR) {
				return ((IRVar) rst).getValue();
			}

			return rst;
		}

		case NIL:
			return O_Nil;

		case EXPR:
			ArrayList<IRObject> exprList = new ArrayList<>();
			IRIterator<? extends IRObject> exprIt = ((IRList) obj).iterator();
			while (exprIt.hasNext()) {
				exprList.add(_compute(exprIt.next(), frame));
			}

			return RulpFactory.createExpression(exprList);

		case LIST:

			if (!RuntimeUtil.isComputable(frame, obj)) {
				return obj;
			}

			IRList oldList = (IRList) obj;
			ArrayList<IRObject> rstList = new ArrayList<>();
			IRIterator<? extends IRObject> listIt = ((IRList) obj).iterator();
			while (listIt.hasNext()) {
				rstList.add(_compute(listIt.next(), frame));
			}

			if (oldList.getNamedName() == null)
				return RulpFactory.createList(rstList);
			else
				return RulpFactory.createNamedList(rstList, oldList.getNamedName());

		default:
			throw new RException("Invalid Type: " + rt + ", obj:" + obj.toString());
		}
	}

	static boolean _isComputable(IRFrame curFrame, IRObject obj) throws RException {

		if (obj == null) {
			return false;
		}

		switch (obj.getType()) {
		case INT:
		case LONG:
		case FLOAT:
		case BOOL:
		case STRING:
		case NIL:
		case FACTOR:
		case FUNC:
		case ARRAY:
			return false;

		case ATOM:
			return curFrame.getEntry(((IRAtom) obj).getName()) != null;

		case VAR:
		case EXPR:
		case MEMBER:
			return true;

		case LIST:

			IRIterator<? extends IRObject> iter = ((IRList) obj).iterator();
			while (iter.hasNext()) {
				if (_isComputable(curFrame, iter.next())) {
					return true;
				}
			}

			return false;

		default:
			return true;
		}
	}

	static int _updateStatus(int mask, IRObject obj) throws RException {

		RReteStatus status = RReteStatus.toStatus(obj);
		if (status == null) {
			throw new RException("Not ReteStatus: " + obj);
		}

		return ReteUtil.updateMask(status, mask);
	}

	public static ModifiterData parseModifiterList(IRIterator<? extends IRObject> iterator, IRInterpreter interpreter,
			IRFrame frame) throws RException {

		ModifiterData processData = new ModifiterData();
		RModifiter processingModifier = null;

		/********************************************/
		// Check modifier
		/********************************************/
		while (iterator.hasNext()) {

			IRObject obj = iterator.next();

			boolean isModifier = false;

			if (obj.getType() == RType.ATOM) {
				RModifiter aModifiter = RModifiter.toModifiter(RulpUtil.asAtom(obj).getName());
				if (aModifiter != null) {
					processingModifier = aModifiter;
					isModifier = true;
				}
			}

			if (isModifier) {

				if (processData.processedModifier.contains(processingModifier)) {
					throw new RException("duplicated modifier: " + obj);
				}

				processData.processedModifier.add(processingModifier);

				int modifiterValueMaxSize = MV_LEN[processingModifier.getIndex()];
				if (modifiterValueMaxSize == 0) {
					processingModifier = null;
					continue;
				}

				if (!iterator.hasNext()) {
					throw new RException("require value for modifier: " + obj);
				}

				if (modifiterValueMaxSize == 1) {

					RType type = MV_TYP[processingModifier.getIndex()];
					IRObject value = interpreter.compute(frame, iterator.next());
					if (type != null && value.getType() != type) {
						throw new RException(String.format("value<%s> type<%s:%s> not match for modifier: %s", value,
								value.getType(), type, processingModifier));
					}

					switch (processingModifier) {
					case LIMIT:
						processData.limit = RulpUtil.asInteger(interpreter.compute(frame, value)).asInteger();
						break;

					case TYPE:
						processData.type = RType.toType(RulpUtil.asAtom(value).asString());
						break;

					case PRIORITY:
						processData.priority = RulpUtil.asInteger(interpreter.compute(frame, value)).asInteger();
						break;

					case ON:
						processData.on = value;
						break;

					default:
						throw new RException("unsupport modifier: " + processingModifier);

					}

					processingModifier = null;
				}

			} else {

				if (processingModifier == null) {
					throw new RException("not modifier: " + obj);
				}

				switch (processingModifier) {

				// from '(a b c) (factor)
				case FROM:
					processData.fromList.add((IRList) _compute(obj, frame));
					break;

				// limit 1
				case STATE:
					processData.state = _updateStatus(processData.state, obj);
					break;

				// do
				case DO:
					processData.doList.add((IRExpr) _compute(obj, frame));
					break;

				default:
					throw new RException("unsupport modifier: " + processingModifier);
				}
			}

		}

		return processData;
	}

}
