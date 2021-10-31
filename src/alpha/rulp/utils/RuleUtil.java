package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.O_False;
import static alpha.rulp.rule.Constant.A_DEFAULT_MODEL;
import static alpha.rulp.rule.Constant.A_M_TRACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RError;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IRParser;
import alpha.rulp.ximpl.error.RIException;
import alpha.rulp.ximpl.node.IRBetaNode;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.runtime.XRInterpreter;
import alpha.rulp.ximpl.scope.IRScope;

public class RuleUtil {

	static class MultiStmtItrator implements IRIterator<IRList> {

		LinkedList<IRIterator<? extends IRObject>> iteratorStack = new LinkedList<>();
		IRList nextStmt = null;

		public MultiStmtItrator(IRIterator<? extends IRObject> mainIterator) {
			super();
			this.iteratorStack.add(mainIterator);
		}

		@Override
		public boolean hasNext() throws RException {

			while (nextStmt == null && !iteratorStack.isEmpty()) {

				IRIterator<? extends IRObject> topIterator = iteratorStack.getLast();
				if (!topIterator.hasNext()) {
					iteratorStack.removeLast();
					continue;
				}

				IRObject nextObj = topIterator.next();
				if (ReteUtil.isReteStmt(nextObj)) {
					nextStmt = (IRList) nextObj;
					break;
				}

				if (nextObj.getType() != RType.LIST) {
					throw new RException("not list: " + nextObj);
				}

				IRIterator<? extends IRObject> subIterator = ((IRList) nextObj).iterator();
				iteratorStack.addLast(subIterator);
			}

			return nextStmt != null;
		}

		@Override
		public IRList next() throws RException {

			if (!hasNext()) {
				return null;
			}

			IRList rt = nextStmt;
			nextStmt = null;
			return rt;
		}

	}

	static class SingleStmtItrator implements IRIterator<IRList> {

		IRList stmt;

		public SingleStmtItrator(IRList stmt) {
			super();
			this.stmt = stmt;
		}

		@Override
		public boolean hasNext() throws RException {
			return stmt != null;
		}

		@Override
		public IRList next() throws RException {

			if (!hasNext()) {
				return null;
			}

			IRList rt = stmt;
			stmt = null;
			return rt;
		}
	}

	static IRParser parser = null;

	static StaticVar varTraceModel = new StaticVar(A_M_TRACE, O_False);

	public static IRBetaNode asBetaNode(IRReteNode node) throws RException {

		if (!RReteType.isBetaType(node.getReteType())) {
			throw new RException("Can't convert to beta node: " + node);
		}

		return (IRBetaNode) node;
	}

	public static IRModel asModel(IRObject obj) throws RException {

		if (!(obj instanceof IRModel)) {
			throw new RException("Can't convert to model: " + obj);
		}

		return (IRModel) obj;
	}

	public static IRNamedNode asNamedNode(IRReteNode node) throws RException {

		if (node.getReteType() != RReteType.NAME0) {
			throw new RException("Can't convert to named node: " + node);
		}

		return (IRNamedNode) node;
	}

	public static IRRule asRule(IRObject obj) throws RException {

		if (!(obj instanceof IRRule)) {
			throw new RException("Can't convert to rule: " + obj);
		}

		return (IRRule) obj;
	}

	public static IRScope asScope(IRObject obj) throws RException {

		if (!(obj instanceof IRScope)) {
			throw new RException("Can't convert to scope: " + obj);
		}

		return (IRScope) obj;
	}

	public static List<IRObject> compute(IRModel model, String input) throws RException {

		IRInterpreter interpreter = model.getInterpreter();
		IRFrame modelFrame = model.getFrame();
		IRParser parser = interpreter.getParser();

		List<IRObject> objs;

		synchronized (parser) {
			objs = parser.parse(input);
		}

		try {

			List<IRObject> rsts = new LinkedList<>();

			for (IRObject obj : objs) {
				rsts.add(interpreter.compute(modelFrame, obj));
			}

			return rsts;

		} catch (RIException e) {

			if (XRInterpreter.TRACE) {
				e.printStackTrace();
			}

			throw new RException("Unhandled internal exception: " + e.toString());

		} catch (RError e) {

			if (XRInterpreter.TRACE) {
				e.printStackTrace();
			}

			RException newExp = new RException("" + e.getError());

			for (String addMsg : e.getAdditionalMessages()) {
				newExp.addMessage(addMsg);
			}

			throw newExp;
		}
	}

	public static boolean equal(String a, String b) throws RException {

		if (a == null) {
			return b == null;
		}

		return b != null && a.equals(b);
	}

	public static IRModel getDefaultModel(IRFrame frame) throws RException {

		IRObject mobj = frame.getObject(A_DEFAULT_MODEL);
		if (mobj == null) {
			return null;
		}

		return RuleUtil.asModel(mobj);
	}

	public static IRParser getParser() {
		if (parser == null) {
			parser = RulpFactory.createParser();
		}
		return parser;
	}

	public static void init(IRFrame frame) throws RException {
		varTraceModel.init(frame);
	}

	public static void invokeRule(IRModel model, String ruleName) throws RException {
		model.getNodeGraph().getRule(ruleName).start(-1, -1);
	}

	public static boolean isModelTrace() throws RException {
		return varTraceModel.getBoolValue();
	}

	public static void removeDefaultModel(IRFrame frame) throws RException {
		frame.removeEntry(A_DEFAULT_MODEL);
	}

	public static void setDefaultModel(IRFrame frame, IRModel model) throws RException {
		frame.setEntry(A_DEFAULT_MODEL, model);
	}

	public static void setModelTrace(boolean trace) throws RException {
		varTraceModel.setBoolValue(trace);
	}

	public static IRList toCondList(IRObject obj) throws RException {

		// '(a b c)
		if (ReteUtil.isReteStmt(obj)) {
			return RulpFactory.createList(obj);
		}
		// ('(a b c))
		else if (obj.getType() == RType.EXPR) {
			return RulpUtil.asExpression(obj);
		} else {
			return RulpFactory.createExpression(RulpUtil.asList(obj).iterator());
		}
	}

	public static IRList toCondList(String cond) throws RException {

		List<IRObject> stmts = getParser().parse(cond);
		if (stmts.size() == 1) {
			return toCondList(stmts.get(0));
		} else {
			return toCondList(RulpFactory.createList(stmts));
		}

	}

	public static List<Integer> toList(int[] ids) {

		if (ids == null || ids.length == 0) {
			return Collections.emptyList();
		}

		List<Integer> list = new ArrayList<Integer>();
		for (int id : ids) {
			list.add(id);
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(T... objs) {

		if (objs == null || objs.length == 0) {
			return Collections.emptyList();
		}

		List<T> list = new ArrayList<T>();
		for (T o : objs) {
			list.add(o);
		}

		return list;
	}

	public static IRList toStmtFilter(String filter) throws RException {

		List<IRObject> stmts = getParser().parse(filter);
		if (stmts.size() != 1) {
			throw new RException("invalid stmt filer: " + filter);
		}

		return RulpUtil.asList(stmts.get(0));
	}

	public static IRIterator<? extends IRList> toStmtList(IRIterator<? extends IRObject> stmtIterator)
			throws RException {
		return new MultiStmtItrator(stmtIterator);
	}

	public static IRIterator<? extends IRList> toStmtList(IRObject obj) throws RException {

		if (obj.getType() != RType.LIST) {
			throw new RException("not list: " + obj);
		}

		IRList list = (IRList) obj;

		if (ReteUtil.isReteStmt(list)) {
			return new SingleStmtItrator(list);
		}

		return new MultiStmtItrator(list.iterator());
	}

	public static IRIterator<? extends IRList> toStmtList(String stmts) throws RException {
		return RuleUtil.toStmtList(RulpFactory.createList(getParser().parse(stmts)));
	}

}
