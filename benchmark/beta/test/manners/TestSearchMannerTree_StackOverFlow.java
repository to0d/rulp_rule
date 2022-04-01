package beta.test.manners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import beta.test.manners.MannerUtil.GuessData;
import beta.test.manners.MannerUtil.SeatData;

public class TestSearchMannerTree_StackOverFlow {

	static class SearchResult {

		private GuessData guessData;

		public SearchResult(GuessData guessData) {
			super();
			this.guessData = guessData;
		}

		private boolean _searchSeatingResult(SeatData seatData, int seatIndex, int guessIndex) {

			if (TRACE) {
				System.out.println(String.format("Search: seat=%2d, guess=%2d, seating=%s", seatIndex, guessIndex,
						seatData.getSeating()));
			}

			// find end
			if (seatIndex >= guessData.getTotalSeatCount()) {
				return true;
			}

			++seatData.searchCount;

			String guessName = guessData.getGuessNames().get(guessIndex);

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

			int totalGuessCount = guessData.getGuessNames().size();

			for (int i = 1; i < totalGuessCount; ++i) {

				int nextGuessIndex = (i + guessIndex) % totalGuessCount;

				if (_searchSeatingResult(seatData, seatIndex + 1, nextGuessIndex)) {
					return true;
				}
			}

			// no result found
			seatData.seatingGuesses.remove(guessName);
			seatData.seatingGuessNames[seatIndex] = null;
			return false;
		}

		public SeatData findResult() {

			SeatData seatData = new SeatData(guessData);

			long time1 = System.currentTimeMillis();

			if (!_searchSeatingResult(seatData, 0, 0)) {
				seatData.searchSeatIndex = 0;
			}

			seatData.searchTime = System.currentTimeMillis() - time1;
			return seatData;
		}

	}

	static boolean TRACE = false;

	public static void main(String[] args) throws IOException {

		TestSearchMannerTree_StackOverFlow.TRACE = false;
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
