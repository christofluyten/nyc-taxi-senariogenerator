package data;

import com.github.rinde.rinsim.geom.Point;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by christof on 18.11.16.
 */
public class Taxi extends SimulationObject {

    public Taxi(String startTime, double startLon, double startLat) {
       super(startTime,startLon,startLat);
    }

    public static void writeTitles(FileWriter writer) throws IOException {
        writer.write("start_time,startX,startY \n");
    }

    public void write(FileWriter writer) throws IOException {
        writer.write(getStartTime());
        writer.write(",");
        writer.write(String.valueOf(getStartX()));
        writer.write(",");
        writer.write(String.valueOf(getStartY()));
        writer.write("\n");
    }
}
