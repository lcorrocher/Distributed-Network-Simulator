package utils;

import DataStructures.HashTable;
import IPSwitch.Coordinates;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CSVReader {
    public static HashTable<String, Coordinates> readCoordinatesCSV() throws IOException {
        // TODO: change to correct path. Not sure how not working with correct relative path
        String filePath = "server/io/lans/coordinates.csv";
        HashTable<String, Coordinates> cityCoordinates = new HashTable<>();

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = CSVFormat.DEFAULT.parse(reader)) {

            boolean headerSkipped = false;
            for (CSVRecord csvRecord : csvParser) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Skip the header record
                }

                String cityName = csvRecord.get(0); // first column
                double latitude = Double.parseDouble(csvRecord.get(1)); // second column
                double longitude = Double.parseDouble(csvRecord.get(2)); // third column
                Coordinates coordinates = new Coordinates(latitude, longitude);
                cityCoordinates.put(cityName, coordinates);
            }

        }

        return cityCoordinates;
    }

    public static void main(String[] args) {
        try {
            HashTable<String, Coordinates> cityCoordinates = readCoordinatesCSV();
            System.out.println(cityCoordinates);
            for (String city : cityCoordinates.keySet()) {
                System.out.println(city + ": " + cityCoordinates.get(city));
            }
            Coordinates val  = cityCoordinates.get("NEW YORK CITY");
            System.out.println(val);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
