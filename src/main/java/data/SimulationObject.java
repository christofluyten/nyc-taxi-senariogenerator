package data;

import com.github.rinde.rinsim.geom.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by christof on 19.11.16.
 */
public abstract class SimulationObject implements Serializable {
    private String startTime;
    private double startLon;
    private double startLat;
    private double startX = -1;
    private double startY = -1;

    public SimulationObject(String startTime, double startLon, double startLat) {
        this.startTime = startTime;
        this.startLon = startLon;
        this.startLat = startLat;
    }

    public String getStartTime() {
        return startTime;
    }

    public long getStartTime(String refTime) {
        long refHours = Long.valueOf(refTime.split(" ")[1]);
        String temp = getStartTime().split(" ")[1];
        String[] time = temp.split(":");
        long hours = Long.valueOf(time[0]);
        long minutes = Long.valueOf(time[1]);
        long secondes = Long.valueOf(time[2]);
        return (((((hours - refHours) * 60) + minutes) * 60) + secondes) * 1000L;
    }

    public double getStartLon() {
        return startLon;
    }

    public double getStartLat() {
        return startLat;
    }

    public double getStartX() {

        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public Point getStartPoint() {
        return new Point(getStartX(), getStartY());
    }

    public abstract void write(FileWriter writer)  throws IOException;

}
