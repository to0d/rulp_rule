package alpha.rulp.ximpl.mts;

import java.io.IOException;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.LoadUtil;

public class MTSUtil {

	public static void init(IRInterpreter interpreter, IRFrame frame) throws RException, IOException {
		LoadUtil.loadRulpFromJar(interpreter, frame, "alpha/resource/mts.rulp", "utf-8");
	}
}
