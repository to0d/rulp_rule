package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_On;
import static alpha.rulp.rule.Constant.A_Priority;
import static alpha.rulp.rule.Constant.A_State;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRFrameEntry;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;

public class ModifiterUtil {

	public static class ModifiterData {

		public List<IRExpr> doList;
		public List<IRList> fromList;
		public int limit = -1;
		public IRObject on = null;
		public int priority = -1;
		public List<Modifier> processedModifier = new LinkedList<>();
		public int state = 0;
		public RType type = null;
		public ArrayList<IRObject> whereList;

		public String asString() throws RException {

			ArrayList<IRObject> list = new ArrayList<>();
			for (Modifier modifiter : processedModifier) {

				list.add(RulpFactory.createAtom(modifiter.name));

				switch (modifiter.name) {
				case A_FROM:
					list.addAll(fromList);
					break;

				case A_Limit:
					list.add(RulpFactory.createInteger(limit));
					break;

				case A_State:
					for (RReteStatus status : RReteStatus.ALL_RETE_STATUS) {
						if (ReteUtil.matchReteStatus(status, state)) {
							list.add(RReteStatus.toObject(status));
						}
					}
					break;

				case A_Type:
					list.add(RType.toObject(type));
					break;

				case A_DO:
					list.addAll(doList);
					break;

				case A_Priority:
					list.add(RulpFactory.createInteger(priority));
					break;

				case A_On:
					list.add(on);
					break;

				default:
					throw new RException("Invalid Modifiter: " + modifiter);
				}

			}

			return RulpUtil.toString(RulpFactory.createList(list));
		}
	}

	public static class Modifier {
		public String name;
		public RType valueType;
		public int valueLength;
	}

	static Map<String, Modifier> modifierMap = new HashMap<>();

	static void _registerModifier(String name, RType valueType, int valueLength) {

		Modifier modifiter = new Modifier();
		modifiter.name = name;
		modifiter.valueType = valueType;
		modifiter.valueLength = valueLength;

		modifierMap.put(name, modifiter);
	}

	static {
		_registerModifier(A_FROM, null, -1);
		_registerModifier(A_Limit, RType.INT, 1);
		_registerModifier(A_State, null, -1);
		_registerModifier(A_Type, RType.ATOM, 1);
		_registerModifier(A_DO, RType.EXPR, -1);
		_registerModifier(A_Priority, RType.INT, 1);
		_registerModifier(A_On, null, 1);
		_registerModifier(A_Where, RType.EXPR, -1);
	}

	public static Modifier getModifiter(String keyName) {
		return modifierMap.get(keyName);
	}

//	static int[] MV_LEN = { -1, 1, -1, 1, -1, 1, 1, -1 };
//
//	static RType[] MV_TYP = { null, RType.INT, null, RType.ATOM, RType.EXPR, RType.INT, null, RType.EXPR };

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
		Modifier processingModifier = null;

		/********************************************/
		// Check modifier
		/********************************************/
		while (iterator.hasNext()) {

			IRObject obj = iterator.next();

			boolean isModifier = false;

			if (obj.getType() == RType.ATOM) {

				Modifier modifiter = getModifiter(RulpUtil.asAtom(obj).getName());
				if (modifiter != null) {
					processingModifier = modifiter;
					isModifier = true;
				}
			}

			if (isModifier) {

				if (processData.processedModifier.contains(processingModifier)) {
					throw new RException("duplicated modifier: " + obj);
				}

				processData.processedModifier.add(processingModifier);

				int modifiterValueMaxSize = processingModifier.valueLength;
				if (modifiterValueMaxSize == 0) {
					processingModifier = null;
					continue;
				}

				if (!iterator.hasNext()) {
					throw new RException("require value for modifier: " + obj);
				}

				if (modifiterValueMaxSize == 1) {

					RType type = processingModifier.valueType;
					IRObject value = interpreter.compute(frame, iterator.next());
					if (type != null && value.getType() != type) {
						throw new RException(String.format("value<%s> type<%s:%s> not match for modifier: %s", value,
								value.getType(), type, processingModifier));
					}

					switch (processingModifier.name) {
					case A_Limit:
						processData.limit = RulpUtil.asInteger(interpreter.compute(frame, value)).asInteger();
						break;

					case A_Type:
						processData.type = RType.toType(RulpUtil.asAtom(value).asString());
						break;

					case A_Priority:
						processData.priority = RulpUtil.asInteger(interpreter.compute(frame, value)).asInteger();
						break;

					case A_On:
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

				switch (processingModifier.name) {

				// from '(a b c) (factor)
				case A_FROM:

					if (processData.fromList == null) {
						processData.fromList = new ArrayList<>();
					}

					processData.fromList.add((IRList) _compute(obj, frame));
					break;

				// limit 1
				case A_State:
					processData.state = _updateStatus(processData.state, obj);
					break;

				// do
				case A_DO:

					if (processData.doList == null) {
						processData.doList = new ArrayList<>();
					}

					processData.doList.add((IRExpr) _compute(obj, frame));
					break;

				case A_Where:

					if (processData.whereList == null) {
						processData.whereList = new ArrayList<>();
					}

					processData.whereList.add(_compute(obj, frame));
					break;

				default:
					throw new RException("unsupport modifier: " + processingModifier);
				}
			}

		}

		return processData;
	}

}
