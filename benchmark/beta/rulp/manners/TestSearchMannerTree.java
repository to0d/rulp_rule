package beta.rulp.manners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import beta.rulp.manners.MannerUtil.GuessData;
import beta.rulp.manners.MannerUtil.SeatData;

public class TestSearchMannerTree {

	static class SearchResult {

		private GuessData guessData;

		public SearchResult(GuessData guessData) {
			super();
			this.guessData = guessData;
		}

		private boolean canSit(SeatData seatData, int seatIndex, String guessName) {

			// This guess is seating already
			if (seatData.seatingGuesses.contains(guessName)) {
				return false;
			}

			// Check whether can sit
			if (seatIndex > 0) {

				String leftGuessName = seatData.seatingGuessNames[seatIndex - 1];
				String rightGuessName = seatData.seatingGuessNames[(seatIndex + 1) % guessData.getTotalSeatCount()];

				if (leftGuessName != null) {
					if (!guessData.canSitTogether(leftGuessName, guessName)) {
						return false;
					}
				}

				if (rightGuessName != null) {
					if (!guessData.canSitTogether(guessName, rightGuessName)) {
						return false;
					}
				}
			}

			return true;
		}

		public SeatData findResult() {

			SeatData seatData = new SeatData(guessData);

			long time1 = System.currentTimeMillis();

			int guessIndex = 0;
			int seatCount = guessData.getTotalSeatCount();
			int guessCount = guessData.getGuessNames().size();

			int[] guessStack = new int[seatCount];

			while (seatData.searchSeatIndex < seatCount) {

				if (TRACE) {
					System.out.println(String.format("Search: seat=%2d, guess=%2d, seating=%s",
							seatData.searchSeatIndex, guessIndex, seatData.getSeating()));
				}

				++seatData.searchCount;

				String guessName = guessData.getGuessNames().get(guessIndex);

				if (canSit(seatData, seatData.searchSeatIndex, guessName)) {

					// Let this guess sit
					seatData.seatingGuessNames[seatData.searchSeatIndex] = guessName;
					seatData.seatingGuesses.add(guessName);

					guessStack[seatData.searchSeatIndex++] = guessIndex++;
					guessIndex %= guessCount;

				} else {

					guessIndex = (guessIndex + 1) % guessCount;

					// no result be found
					if (seatData.searchSeatIndex == 0 && guessIndex == 0) {
						break;
					}

					// back to last seat
					if (seatData.searchSeatIndex > 0 && guessIndex == guessStack[seatData.searchSeatIndex - 1]) {
						guessIndex = guessStack[--seatData.searchSeatIndex] + 1;
						guessIndex %= guessCount;
					}
				}

			}

			seatData.searchTime = System.currentTimeMillis() - time1;
			return seatData;
		}

	}

	static boolean TRACE = true;

// Result on laptop (todd-t4)
// 16 guesss, 16 seats, max 3 hobbies, min 2 hobbies, time=1, count=96, result=  1,  2,  3,  7,  8, 11,  4, 12,  5, 
// 32 guesss, 32 seats, max 3 hobbies, min 2 hobbies, time=0, count=87, result=dave,jane,scott,sue,john,hope,dan,car
// 64 guesss, 64 seats, max 3 hobbies, min 2 hobbies, time=0, count=768, result=  1,  2,  3,  7,  8, 12, 13, 16, 18,
// 128 guesss, 128 seats, max 5 hobbies, min 2 hobbies, time=2, count=1795, result=  1,  2,  4,  6,  8, 12, 14, 18, 
// 2048 guesss, 2048 seats, max 5 hobbies, min 2 hobbies, time=46, count=225871, result=  1,  2,  4,  5,  6,  7,  8,
// 3000 guesss, 3000 seats, max 5 hobbies, min 2 hobbies, time=42, count=510591, result=  1,  2,  4,  5,  6,  7,  8,
// 4096 guesss, 4096 seats, max 5 hobbies, min 2 hobbies, time=641, count=16777216, result=no result found

	public static void main(String[] args) throws IOException {

		TestSearchMannerTree.TRACE = false;
		String path = "C:\\data\\rs\\benchmark\\ops5-benchmark-suite\\manners\\";

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
		}
	}

}
