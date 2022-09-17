package alpha.rulp.ximpl.constraint;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraintXExpr0 extends AbsRConstraintXExpr implements IRConstraintXExpr {

	protected List<IRVar[]> _varsList = null;

	protected List<IRObject[]> varEntryList;

	public XRConstraintXExpr0(IRExpr expr, List<IRObject[]> varEntryList) {

		super(varEntryList.size(), expr);
		this.varEntryList = varEntryList;
	}

	@Override
	protected boolean _addEntry(IRReteEntry[] entries, IRContext context) throws RException {

		List<IRVar[]> _varsList = getVarList(context.getFrame());

		/******************************************************/
		// Update variable value
		/******************************************************/
		for (int i = 0; i < entryLength; ++i) {

			IRReteEntry entry = entries[i];
			IRVar[] _vars = _varsList.get(i);

			for (int j = 0; j < entry.size(); ++j) {
				IRVar var = _vars[j];
				if (var != null) {
					var.setValue(entry.get(j));
				}
			}
		}

		IRObject rst = context.getInterpreter().compute(context.getFrame(), expr);

		return RulpUtil.asBoolean(rst).asBoolean();
	}

	public List<IRVar[]> getVarList(IRFrame frame) throws RException {

		if (_varsList == null) {

			_varsList = new ArrayList<>();

			for (IRObject[] varEntry : varEntryList) {

				IRVar[] _vars = new IRVar[varEntry.length];

				for (int i = 0; i < varEntry.length; ++i) {
					IRObject obj = varEntry[i];
					if (obj != null) {
						_vars[i] = RulpUtil.addVar(frame, RulpUtil.asAtom(obj).getName());
					}
				}

				_varsList.add(_vars);

			}
		}

		return _varsList;
	}

}
