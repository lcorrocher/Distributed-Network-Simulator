package utils;

import DataStructures.HashTable;
import IPSwitch.Coordinates;
import IPSwitch.InterContinentalRouter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class CSVReader {
    public static HashTable<String, Coordinates> cityCoordinates;

    public static HashTable<String, Coordinates> readCoordinatesCSV() throws IOException {
        // TODO: change to correct path. Not sure how not working with correct relative path
        System.out.println("Loading coordinates from CSV file...\n");
        String filePath = "server/io/lans/coordinates.csv";
        cityCoordinates = new HashTable<>();

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


    public static Map<String, InterContinentalRouter> readIntercontinentalRoutersCSV() throws IOException {
        String filePath = "otherWanCoordinates/internet.csv";
        Map<String, InterContinentalRouter> interRouters = new HashMap<>();

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = CSVFormat.DEFAULT.parse(reader)) {

            boolean headerSkipped = false;
            for (CSVRecord csvRecord : csvParser) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String cityName = csvRecord.get(0); // convert city name to lowercase
                String name = cityName.toLowerCase();
                int id = Integer.parseInt(csvRecord.get(1)); // first column
                double latitude = Double.parseDouble(csvRecord.get(2)); // third column
                double longitude = Double.parseDouble(csvRecord.get(3)); // fourth column
                String continent = csvRecord.get(4); // fifth column
                Coordinates coordinates = new Coordinates(latitude, longitude);
                InterContinentalRouter router = new InterContinentalRouter(id, cityName, coordinates, continent);
                interRouters.put(name, router);
            }
        }
        return interRouters;
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
