package beta.test.manners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.StringUtil;

public class MannerUtil {

	public static class SeatData {

		private GuessData guessData;

		public int searchCount = 0;

		public int searchSeatIndex = 0;

		public long searchTime = 0;

		public Set<String> seatingGuesses = new HashSet<>();

		public final String seatingGuessNames[];

		public SeatData(GuessData guessData) {
			super();
			this.guessData = guessData;
			this.seatingGuessNames = new String[guessData.getTotalSeatCount()];
		}

		public String getSeating() {

			String seating = "";

			for (String seatingGuessName : seatingGuessNames) {
				if (seatingGuessName == null) {
					seating += "N/A,";
				} else {
					seating += String.format("%3s,", seatingGuessName);
				}

			}

			return seating;
		}

		public void print() {
			System.out.println(
					String.format("%d guesss, %d seats, max %d hobbies, min %d hobbies, time=%d, count=%d, result=%s",
							guessData.getTotalGuessCount(), guessData.getTotalSeatCount(), guessData.getMaxHobbyCount(),
							guessData.getMinHobbyCount(), searchTime, searchCount,
							(searchSeatIndex == 0 ? "no result found" : getSeating())));
		}

	}

	public static class GuessData {

		private Map<String, Boolean> canSitMap = new HashMap<>();

		private Map<String, Set<String>> guessHobbyMap = new HashMap<>();

		private ArrayList<String> guessNames = new ArrayList<>();

		private Map<String, String> guessSexMap = new HashMap<>();

		private int maxHobbyCount = -1;

		private int minHobbyCount = -1;

		private int totalSeatCount = -1;

		public void addFavor(String name, String sex, String hobby) throws IOException {

			{
				if (!guessNames.contains(name)) {
					guessNames.add(name);
				}
			}

			{
				String oldsex = guessSexMap.get(name);
				if (oldsex == null) {
					oldsex = sex;
					guessSexMap.put(name, oldsex);

				} else if (!oldsex.equals(sex)) {
					throw new IOException("Invalid guess sex: " + sex);
				}
			}

			{
				Set<String> hobbys = guessHobbyMap.get(name);
				if (hobbys == null) {
					hobbys = new HashSet<>();
					guessHobbyMap.put(name, hobbys);
				}

				hobbys.add(hobby);
			}

		}

		public boolean canSitTogether(String guessA, String guessB) {

			if (guessA.equals(guessB)) {
				return false;
			}

			String key = guessA.compareTo(guessB) < 0 ? "from_" + guessA + "_to_" + guessB
					: "from_" + guessB + "_to_" + guessA;

			Boolean canSit = canSitMap.get(key);
			if (canSit == null) {

				// same sex guess can't sit together
				if (getGuessSex(guessA).equals(getGuessSex(guessB))) {

					canSit = false;

				} else {

					Set<String> guessAHobbys = getGuessHobby(guessA);
					Set<String> guessBHobbys = getGuessHobby(guessB);

					Set<String> commonHobbys = new HashSet<>(guessAHobbys);
					commonHobbys.retainAll(guessBHobbys);

					// No same hobby
					if (commonHobbys.isEmpty()) {
						canSit = false;

					} else {
						canSit = true;
					}
				}

				canSitMap.put(key, canSit);

			}

			return canSit;
		}

		public Set<String> getGuessHobby(String guessName) {
			return guessHobbyMap.get(guessName);
		}

		public List<String> getGuessNames() {
			return guessNames;
		}

		public String getGuessSex(String guessName) {
			return guessSexMap.get(guessName);
		}

		public int getMaxHobbyCount() {

			if (maxHobbyCount == -1) {
				for (Set<String> hobbys : guessHobbyMap.values()) {
					if (maxHobbyCount < hobbys.size()) {
						maxHobbyCount = hobbys.size();
					}
				}
			}

			return maxHobbyCount;
		}

		public int getMinHobbyCount() {

			if (minHobbyCount == -1) {

				minHobbyCount = Integer.MAX_VALUE;

				for (Set<String> hobbys : guessHobbyMap.values()) {
					if (minHobbyCount > hobbys.size()) {
						minHobbyCount = hobbys.size();
					}
				}

			}

			return minHobbyCount;
		}

		public int getTotalGuessCount() {
			return guessNames.size();
		}

		public int getTotalSeatCount() {
			return totalSeatCount;
		}

		public void print() {

			System.out.println("Total seat count: " + getTotalSeatCount());
			System.out.println("Total guess count: " + getTotalGuessCount());
			System.out.println("All guess: ");

			for (String guess : this.getGuessNames()) {

				ArrayList<String> hobbys = new ArrayList<>(getGuessHobby(guess));
				Collections.sort(hobbys);

				System.out.println(String.format("\t %s: sex=%s, hobby=%s", guess, getGuessSex(guess), hobbys));
			}

		}

		public void setTotalSeatCount(int totalSeatCount) {
			this.totalSeatCount = totalSeatCount;
		}
	}

	public static GuessData loadGuessData(String path) throws IOException {

		GuessData data = new GuessData();

		ArrayList<String> values = new ArrayList<>();

		for (String line : FileUtil.openTxtFile(path)) {

			if (StringUtil.matchFormat("(make guest ^name %? ^sex %? ^hobby %?)", line, values) && values.size() == 3) {

				String name = values.get(0);
				String sex = values.get(1);
				String hobby = values.get(2);

				if (!sex.equals("m") && !sex.equals("f")) {
					throw new IOException("Invalid sex: " + line);
				}

				data.addFavor(name, sex, hobby);
			}

			if (StringUtil.matchFormat("(make last_seat %?)", line, values) && values.size() == 1) {

				if (data.totalSeatCount != -1) {
					throw new IOException("duplicated seat line found: " + line);
				}

				data.setTotalSeatCount(Integer.valueOf(values.get(0)));
			}
		}

		if (data.getTotalSeatCount() != data.getTotalGuessCount()) {
			throw new IOException("Incorrect seat count");
		}

		return data;
	}

}
