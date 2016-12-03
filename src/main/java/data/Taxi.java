package data;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by christof on 18.11.16.
 */
public class Taxi extends SimulationObject {

    private String licenseNb;
    private int capacity;

    public Taxi(String licenceNb, int capacity, String startTime, double startLon, double startLat) {
       super(startTime,startLon,startLat);
        this.licenseNb = licenceNb;
        this.capacity = capacity;
    }

    public static void writeTitles(FileWriter writer) throws IOException {
        writer.write("licence,capacity,start_time,startX,startY \n");
    }

    public void write(FileWriter writer) throws IOException {
        writer.write(getLicenseNb());
        writer.write(",");
        writer.write(String.valueOf(getCapacity()));
        writer.write(",");
        writer.write(getStartTime());
        writer.write(",");
        writer.write(String.valueOf(getStartX()));
        writer.write(",");
        writer.write(String.valueOf(getStartY()));
        writer.write("\n");
    }

    public String getLicenseNb() {
        return licenseNb;
    }

    public int getCapacity() {
        return capacity;
    }
}
