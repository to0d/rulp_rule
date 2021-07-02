package beta.rulp.manners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import beta.rulp.manners.TestSearchMannerTree.SearchResult;

public class TestSearchMannerRule {

	public static void main(String[] args) throws IOException {

		TestSearchMannerTree.TRACE = false;
		String path = "D:\\data\\rs\\benchmark\\ops5-benchmark-suite\\manners\\";

		ArrayList<String> datPaths = new ArrayList<>();

		for (File file : new File(path).listFiles()) {

			if (!file.isFile()) {
				continue;
			}

			String filePath = file.getAbsolutePath();
			if (!filePath.endsWith(".dat")) {
				continue;
			}

			datPaths.add(filePath);
		}

		Collections.sort(datPaths, (p1, p2) -> {
			int d = p1.length() - p2.length();
			if (d == 0) {
				d = p1.compareTo(p2);
			}
			return d;
		});

		for (String datPath : datPaths) {
			new SearchResult(MannerUtil.loadGuessData(datPath)).findResult().print();
			break;
		}
	}
}
