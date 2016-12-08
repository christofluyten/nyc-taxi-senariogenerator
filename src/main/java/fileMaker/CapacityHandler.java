package fileMaker;

import data.Date;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by christof on 02.12.16.
 */


public class CapacityHandler {

    private static final String DATA_FILE_DIRECTORY = "/media/christof/Elements/Data/FOIL2013/";
    private static final Date TAXI_START_TIME = new Date("2013-11-18 15:00:00");


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        IOHandler ioHandler = new IOHandler();
        ioHandler.setTaxiDataDirectory(DATA_FILE_DIRECTORY);
        ioHandler.setTaxiStartTime(TAXI_START_TIME);
        CapacityHandler.create(ioHandler);
    }

    //TODO over een heel jaar lopen

    public static void create(IOHandler ioHandler) throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(new File(ioHandler.getTaxiDataFile()));
        scanner.nextLine();
        HashMap<String, Integer> map = new HashMap<>();
        FileWriter writer = new FileWriter(ioHandler.getTaxiCapacityPath() + ".csv");
        writer.write("licenseNb,capacity\n");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splitLine = line.split(",");
            String licenseNb = splitLine[0];
            int capacity = Integer.valueOf(splitLine[7]);
            if ((capacity > 0 && capacity < 25)) {
                int oldCapacity = 0;
                try {
                    oldCapacity = map.get(licenseNb);
                } catch (Exception e) {
                }
                if (capacity > oldCapacity) {
                    map.put(licenseNb, capacity);
                }
            }
        }

        for (String lic : map.keySet()) {
            writer.write(lic + "," + map.get(lic) + "\n");
        }

        ioHandler.writeFile(map, ioHandler.getTaxiCapacityPath());

        scanner.close();
        writer.close();
    }
}
