package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.A_By;
import static alpha.rulp.lang.Constant.A_DO;
import static alpha.rulp.lang.Constant.A_FROM;
import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_Desc;
import static alpha.rulp.rule.Constant.A_Limit;
import static alpha.rulp.rule.Constant.A_On;
import static alpha.rulp.rule.Constant.A_Order;
import static alpha.rulp.rule.Constant.A_Order_by;
import static alpha.rulp.rule.Constant.A_Priority;
import static alpha.rulp.rule.Constant.*;
import static alpha.rulp.rule.Constant.A_State;
import static alpha.rulp.rule.Constant.A_Type;
import static alpha.rulp.rule.Constant.A_Where;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRFrameEntry;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRIterator;

public class ModifiterUtil {

	static interface IModifier {

		public String getName();

		public Modifier process(ModifierData list) throws RException;
	}

	public static class Modifier {

		public String name;
		public IRObject obj;

		public String toString() {
			return String.format("(%s %s)", name, "" + obj);
		}
	}

	static class ModifierData {
		public int fromIndex;
		public List<IRObject> list;
	}

	static class XModifier0 implements IModifier {

		protected final String name;

		public XModifier0(String name) {
			super();
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Modifier process(ModifierData list) throws RException {

			Modifier modifer = new Modifier();
			modifer.name = this.getName();

			return modifer;
		}

	}

	static class XModifier1 implements IModifier {

		protected final String name;

		protected final RType[] types;

		public XModifier1(String name, RType... types) {
			super();
			this.name = name;
			this.types = types;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Modifier process(ModifierData list) throws RException {

			if (list.fromIndex >= list.list.size()) {
				throw new RException("not enough object for modifier: " + getName());
			}

			IRObject obj = list.list.get(list.fromIndex);
			if (typeIndex(obj) == -1) {
				throw new RException("invalid value '" + obj + "' specified for modifier: " + getName());
			}

			list.fromIndex++;

			Modifier modifer = new Modifier();
			modifer.name = this.getName();
			modifer.obj = obj;

			return modifer;
		}

		public int typeIndex(IRObject obj) {

			for (int i = 0; i < types.length; ++i) {
				if (obj.getType() == types[i]) {
					return i;
				}
			}

			return -1;
		}

	}

	static class XModifier2 extends XModifier1 implements IModifier {

		public XModifier2(String name, RType... types) {
			super(name, types);
		}

		@Override
		public Modifier process(ModifierData list) throws RException {

			ArrayList<IRObject> objList = new ArrayList<>();

			while (list.fromIndex < list.list.size()) {

				IRObject obj = list.list.get(list.fromIndex);
				if (typeIndex(obj) == -1) {
					break;
				}

				objList.add(obj);
				list.fromIndex++;
			}

			if (objList.isEmpty()) {
				throw new RException("no value specified for modifier: " + getName());
			}

			Modifier modifer = new Modifier();
			modifer.name = this.getName();
			modifer.obj = RulpFactory.createList(objList);

			return modifer;
		}

	}

	static class XModifierOrderBy implements IModifier {

		@Override
		public String getName() {
			return A_Order_by;
		}

		@Override
		public Modifier process(ModifierData list) throws RException {

			// order by ?x
			// order by ?x asc
			// order by '(?x ?y) desc
			// order by ?x asc ?y desc

			// by
			{
				if (list.fromIndex >= list.list.size()) {
					throw new RException("not enough object for modifier: " + getName());
				}

				IRObject by = list.list.get(list.fromIndex);
				if (!RulpUtil.isAtom(by) && !RulpUtil.asAtom(by).asString().equals(A_By)) {
					throw new RException("unexpect object<" + by + "> for modifier: " + getName());
				}

				list.fromIndex++;
			}

			ArrayList<IRObject> objList = new ArrayList<>();

			// ?x
			while (list.fromIndex < list.list.size()) {

				IRObject obj = list.list.get(list.fromIndex);
				if (obj.getType() != RType.LIST && !RulpUtil.isVarAtom(obj)) {
					break;
				}

				list.fromIndex++;
				IRObject order = O_Nil;

				if (list.fromIndex < list.list.size()) {
					IRObject obj2 = list.list.get(list.fromIndex);
					if (RulpUtil.isAtom(obj2, A_Desc) || RulpUtil.isAtom(obj2, A_Asc)) {
						order = obj2;
						list.fromIndex++;
					}
				}

				objList.add(RulpFactory.createList(obj, order));
			}

			if (objList.isEmpty()) {
				throw new RException("no value specified for modifier: " + getName());
			}

			Modifier modifer = new Modifier();
			modifer.name = this.getName();
			modifer.obj = RulpFactory.createList(objList);
			return modifer;
		}

	}

	static class XModifierState implements IModifier {

		protected final String name;

		public XModifierState(String name) {
			super();
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Modifier process(ModifierData list) throws RException {

			int mask = 0;
			int num = 0;

			while (list.fromIndex < list.list.size()) {

				IRObject obj = list.list.get(list.fromIndex);
				RReteStatus status = RReteStatus.toStatus(obj);
				if (status == null) {
					break;
				}

				mask = ReteUtil.updateMask(status, mask);
				list.fromIndex++;
				num++;
			}

			if (num == 0) {
				throw new RException("no value specified for modifier: " + getName());
			}

			Modifier modifer = new Modifier();
			modifer.name = this.getName();
			modifer.obj = RulpFactory.createInteger(mask);

			return modifer;
		}

	}

	static Map<String, IModifier> modifierMap = new HashMap<>();

	static {
		modifierMap.put(A_FROM, new XModifier2(A_FROM, RType.LIST, RType.EXPR));
		modifierMap.put(A_Where, new XModifier2(A_Where, RType.LIST, RType.EXPR));
		modifierMap.put(A_DO, new XModifier2(A_DO, RType.EXPR));
		modifierMap.put(A_Limit, new XModifier1(A_Limit, RType.INT));
		modifierMap.put(A_Priority, new XModifier1(A_Priority, RType.INT));
		modifierMap.put(A_On, new XModifier1(A_On, RType.ATOM, RType.LIST));
		modifierMap.put(A_Type, new XModifier1(A_Type, RType.ATOM));
		modifierMap.put(A_State, new XModifierState(A_State));
		modifierMap.put(A_Order, new XModifierOrderBy());
		modifierMap.put(A_Reverse, new XModifier0(A_Reverse));
		modifierMap.put(A_BackSearch, new XModifier0(A_BackSearch));
	}

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

	static IModifier _getModifiter(String keyName) {
		return modifierMap.get(keyName);
	}

	public static List<Modifier> parseModifiterList(IRIterator<? extends IRObject> iterator, IRFrame frame)
			throws RException {

		ArrayList<IRObject> objList = new ArrayList<>();
		while (iterator.hasNext()) {
			objList.add(_compute(iterator.next(), frame));
		}

		ModifierData list = new ModifierData();
		list.list = objList;
		list.fromIndex = 0;

		ArrayList<Modifier> mdata = new ArrayList<>();

		while (list.fromIndex < list.list.size()) {

			IRObject obj = list.list.get(list.fromIndex++);
			if (obj.getType() != RType.ATOM && obj.getType() != RType.FACTOR) {
				throw new RException("unsupport modifier: " + obj);
			}

			IModifier modifiter = _getModifiter(obj.asString());
			if (modifiter == null) {
				throw new RException("unsupport modifier: " + obj);
			}

			mdata.add(modifiter.process(list));
		}

		return mdata;

	}
}
