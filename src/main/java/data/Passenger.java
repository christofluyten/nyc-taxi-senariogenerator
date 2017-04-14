package data;

import com.github.rinde.rinsim.geom.Point;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by christof on 18.11.16.
 */
public class Passenger extends SimulationObject {
    private double endLon;
    private double endLat;
    private double endX = -1;
    private double endY = -1;
    private int amount;
    private long timeWindow;

    public Passenger(int amount, Date pickupTime, double pickupLon, double pickupLat, double dropoffLon, double dropoffLat) {
        super(pickupTime,pickupLon,pickupLat);
        this.endLon = dropoffLon;
        this.endLat = dropoffLat;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public double getEndLon() {
        return endLon;
    }

    public double getEndLat() {
        return endLat;
    }

    public Point getEndPoint() {
        return new Point(getEndX(),getEndY());
    }

    private double getEndX() {
        return endX;
    }

    private double getEndY() {
        return endY;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    public void setTimeWindow(long timeWindow) {
        this.timeWindow = timeWindow;
    }

    public long getStartTimeWindow(Date refTime) {
        return getStartTime(refTime)+timeWindow;
    }


    public static void writeTitles(FileWriter writer) throws IOException {
        writer.write("amount,pickup_time,startX,startY,endX,endY \n");
    }

    public void write(FileWriter writer) throws IOException {
        writer.write(String.valueOf(getAmount()));
        writer.write(",");
        writer.write(getStartTime());
        writer.write(",");
        writer.write(String.valueOf(getStartX()));
        writer.write(",");
        writer.write(String.valueOf(getStartY()));
        writer.write(",");
        writer.write(String.valueOf(getEndX()));
        writer.write(",");
        writer.write(String.valueOf(getEndY()));
        writer.write("\n");
    }
}
