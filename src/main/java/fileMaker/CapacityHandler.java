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

    private static final String TAXI_DATA_DIRECTORY = "/media/christof/Elements/Taxi_data/";    //path to director with the FOIL-directories
    private static final Date TAXI_START_TIME = new Date("2013-11-18 15:00:00");


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        IOHandler ioHandler = new IOHandler();
        ioHandler.setTaxiDataDirectory(TAXI_DATA_DIRECTORY);
        ioHandler.setTaxiStartTime(TAXI_START_TIME);
        CapacityHandler.create(ioHandler);
    }

    public static void create(IOHandler ioHandler) throws IOException, ClassNotFoundException {
        HashMap<String, Integer> map = new HashMap<>();

        for (int month = 1; month < 13; month++) {
            Scanner scanner = new Scanner(new File(ioHandler.getTaxiDataYearPath() + String.valueOf(month) + ".csv"));
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitLine = line.split(",");
                String licenseNb = splitLine[0];
                int capacity = Integer.valueOf(splitLine[7]);
                if ((capacity == 5)) {
                    map.put(licenseNb, 5);
                } else if (!map.containsKey(licenseNb)) {
                    map.put(licenseNb, 4);
                }
            }
            scanner.close();
            System.out.println("Month " + month + " is done");
        }


        FileWriter writer = new FileWriter(ioHandler.getTaxiCapacityPath() + ".csv");
        writer.write("licenseNb,capacity\n");
        int fiveCap = 0;
        int fourCap = 0;
        int error = 0;
        for (String lic : map.keySet()) {
            int cap = map.get(lic);
            writer.write(lic + "," + String.valueOf(cap) + "\n");
            if (cap == 4) {
                fourCap++;
            } else if (cap == 5) {
                fiveCap++;
            } else {
                error++;
            }
        }
        int total = fiveCap + fourCap + error;

        System.out.println("total amount of licenseNb: " + total);
        System.out.println("cars with 5 passengers capacity: " + fiveCap);
        System.out.println("percentage cars with 5 passengers capacity: " + (fiveCap / total) * 100);
        System.out.println("cars with 4 passengers capacity: " + fourCap);
        System.out.println("percentage cars with 4 passengers capacity: " + (fourCap / total) * 100);
        System.out.println("cars with error capacity: " + error);
        System.out.println("percentage cars with error capacity: " + (error / total) * 100);


        ioHandler.writeFile(map, ioHandler.getTaxiCapacityPath());

        writer.close();
    }
}
