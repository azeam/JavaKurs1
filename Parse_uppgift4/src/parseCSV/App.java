package parseCSV;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
	private static String COMMA_DELIMITER = ",";
	public static void main(String[] args) {
		List<List<String>> records = new ArrayList<>();
		List<String> nameColumn = new ArrayList<String>();
		try (Scanner scanner = new Scanner(new File("sample.csv"));) {
			while (scanner.hasNextLine()) {
				records.add(getRecordFromLine(scanner.nextLine()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for(List<String> row:records){ 
			nameColumn.add(row.get(1));
		}
		countAs(nameColumn);
		findDuplicateDates(records, nameColumn);
		countAndroids(records);
	}

	private static void countAndroids(List<List<String>> records) {
		List<String> androidColumn = new ArrayList<String>();
		int count = 0;
		for(List<String> row : records){ 
			androidColumn.add(row.get(6));
		}
		for(String android : androidColumn) {
			if (android.equals("Android App")){
				count++;
			}	
		}
		System.out.println(count + " will work with Android");
	}

	private static void findDuplicateDates(List<List<String>> records, List<String> nameColumn) {
		// build list with index as key
		// build list if it doesn't already contain values
		// remove from key list from second list
		// print names with index

		Map<Integer, String> uniqueDates = new HashMap<>();
		Map<Integer, String> duplicateDates = new HashMap<>();

		List<String> dateColumn = new ArrayList<String>();
		List<Integer> indexMatches = new ArrayList<Integer>();
		
		int z = 0;
		for(List<String> row:records){ 
			dateColumn.add(row.get(0));
			indexMatches.add(z);
			if(uniqueDates.containsValue(dateColumn.get(z)) && !dateColumn.get(z).equals("")) {
				duplicateDates.put(z, dateColumn.get(z));
			}
			else {
				uniqueDates.put(z, dateColumn.get(z));
			}
			z++;
		}

		for (Map.Entry<Integer, String> entry : duplicateDates.entrySet()) {
			for(int i=1; i<dateColumn.size(); i++) {
				if (duplicateDates.get(entry.getKey()).equals(dateColumn.get(i)) && !dateColumn.get(i).equals("") && !nameColumn.get(entry.getKey()).equals(nameColumn.get(i))) {
					System.out.println(nameColumn.get(entry.getKey()) + " has same date as " + nameColumn.get(i));
				}
			}
		}
	}

	private static void countAs(List<String> nameColumn) {
		int count = 0;
		for(int i=1; i<nameColumn.size(); i++) {
			if (nameColumn.get(i).contains("a") || nameColumn.get(i).contains("A")) {
				count++;
			}
		}
		System.out.println(count + " names contain \"a\".");
	}

	private static List<String> getRecordFromLine(String line) {
		List<String> values = new ArrayList<String>();
		try (Scanner rowScanner = new Scanner(line)) {
			rowScanner.useDelimiter(COMMA_DELIMITER);
			while (rowScanner.hasNext()) {
				values.add(rowScanner.next());
			}
		}
		return values;
	}

}