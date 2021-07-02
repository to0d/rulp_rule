package alpha.rulp.ximpl.model;

import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

public class XRUniqObjBuilder implements IRObjBuilder {

	protected Map<String, IRObject> objUniqMap = new HashMap<>();

	@Override
	public IRObject build(IRObject obj) throws RException {

		if (obj == null) {
			return null;
		}

		String uniqName = null;

		switch (obj.getType()) {
		case INT:
		case LONG:
		case BOOL:
		case FLOAT:
		case DOUBLE:
		case VAR:
		case FUNC:
		case STRING:
		case FACTOR:
			return obj;

		case ATOM:
			uniqName = RulpUtil.asAtom(obj).getName();
			IRObject uobj = objUniqMap.get(uniqName);
			if (uobj == null) {
				uobj = obj;
				objUniqMap.put(uniqName, uobj);
			}
			return uobj;

		// LIST & EXPR can't be indexed to save space
		case LIST:
			IRList oldList = (IRList) obj;
			return RulpFactory.createNamedList(_uniqIterator(oldList.iterator()), oldList.getNamedName());

		case EXPR:
			return RulpFactory.createExpression(_uniqIterator(((IRExpr) obj).iterator()));

		default:
			throw new RException("not support obj: " + obj);

		}
	}

	private IRIterator<? extends IRObject> _uniqIterator(IRIterator<? extends IRObject> iterator) {

		return new IRIterator<IRObject>() {

			@Override
			public boolean hasNext() throws RException {
				return iterator.hasNext();
			}

			@Override
			public IRObject next() throws RException {
				return build(iterator.next());
			}
		};
	}

	public int size() {
		return objUniqMap.size();
	}

}
