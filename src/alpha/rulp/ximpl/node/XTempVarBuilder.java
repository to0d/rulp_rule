package alpha.rulp.ximpl.node;

import java.util.ArrayList;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.utils.RulpFactory;

public class XTempVarBuilder {

	static final String TEMP_PRE = "?_tmp_";

	private String tempName;

	private int tempVarIndex = 0;

	private ArrayList<IRAtom> tempVarList = new ArrayList<>();

	public XTempVarBuilder(String tempName) {
		super();
		this.tempName = TEMP_PRE + tempName.toLowerCase() + "_";
	}

	public IRAtom next() {

		while (tempVarIndex >= tempVarList.size()) {
			tempVarList.add(RulpFactory.createAtom(tempName + tempVarList.size()));
		}

		return tempVarList.get(tempVarIndex++);
	}

	public void reset() {
		this.tempVarIndex = 0;
	}
}
