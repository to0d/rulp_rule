package beta.test.manners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import beta.test.manners.MannerUtil.GuessData;

public class TestPrintMannerData {

	public static void main(String[] args) throws IOException {

		TestSearchMannerTree.TRACE = false;
		String path = "C:\\data\\rs\\benchmark\\ops5-benchmark-suite\\manners\\manners_4.dat";

		GuessData gd = MannerUtil.loadGuessData(path);

		for (String name : gd.getGuessNames()) {
			System.out.println(String.format("(add-stmt mm guess:'(%s %s))", name, gd.getGuessSex(name)));
		}

		for (String name : gd.getGuessNames()) {

			ArrayList<String> hobbys = new ArrayList<>(gd.getGuessHobby(name));
			Collections.sort(hobbys);

			for (String hobby : hobbys) {
				System.out.println(String.format("(add-stmt mm hobby:'(%s %s))", name, hobby));
			}
		}
	}
}
