package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1OneOf extends AbsRConstraint1Index1 implements IRConstraint1 {

	private String _constraintExpression = null;

	private Set<String> valueUniqNameSet = null;

	public Set<String> getValueUniqNameSet() throws RException {
		if (valueUniqNameSet == null) {
			valueUniqNameSet = new HashSet<>();
			for (IRObject val : RulpUtil.toArray(valueList)) {
				valueUniqNameSet.add(RulpUtil.toUniqString(val));
			}
		}
		return valueUniqNameSet;
	}

	private IRList valueList;

	public XRConstraint1OneOf(int index, IRList valueList) {
		super(index);
		this.valueList = valueList;

	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		return getValueUniqNameSet().contains(RulpUtil.toUniqString(entry.get(index)));
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s ?%d %s)", A_One_Of, index, "" + valueList);
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_One_Of;
	}

}
