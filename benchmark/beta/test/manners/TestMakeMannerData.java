package beta.test.manners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import alpha.rulp.utils.RulpUtil;

public class TestMakeMannerData {

	public static void main(String[] args) {

		ArrayList<String> guessList = new ArrayList<>();
		ArrayList<String> hobbyList = new ArrayList<>();

		int len = 32;
		int maxHobby = 2;
		int maxHobbyCount = 2;

		int[] guess = new int[len];
		Random random = new Random();
		Set<Integer> guessSet = new HashSet<>();

		for (int i = 0; i < len; ++i) {

			int v = random.nextInt(len);
			while (guessSet.contains(v)) {
				v = random.nextInt(len);
			}

			guess[i] = v + 1;
			guessSet.add(v);
		}

		System.out.println("; guess order: " + RulpUtil.toArray2(guess));

		for (int i = 0; i < len; ++i) {
			guessList.add(String.format("(add-stmt mm guess:'(%04d %s))", guess[i], i % 2 == 0 ? "m" : "f"));
		}

		int lasthobby = 0;
		for (int i = 0; i < len; ++i) {

			int hobbyCount = random.nextInt(maxHobbyCount + 1);
			while (hobbyCount <= 0) {
				hobbyCount = random.nextInt(maxHobbyCount + 1);
			}

			hobbyList.add(String.format("(add-stmt mm hobby:'(%04d %d))", guess[i], lasthobby));
			if (hobbyCount == 1) {
				continue;
			}

			Set<Integer> HobbySet = new HashSet<>();
			HobbySet.add(lasthobby);

			for (int j = 1; j < hobbyCount; ++j) {

				int hobby = random.nextInt(maxHobby + 1);
				while (HobbySet.contains(hobby)) {
					hobby = random.nextInt(maxHobby + 1);
				}

				hobbyList.add(String.format("(add-stmt mm hobby:'(%04d 1))", guess[i], hobby));
				lasthobby = hobby;
				HobbySet.add(hobby);
			}
		}

		Collections.sort(guessList);
		Collections.sort(hobbyList);

		for (String line : guessList) {
			System.out.println(line);
		}

		for (String line : hobbyList) {
			System.out.println(line);
		}
	}
}
