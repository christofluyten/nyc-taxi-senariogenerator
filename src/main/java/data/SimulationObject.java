package data;

import com.github.rinde.rinsim.geom.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by christof on 19.11.16.
 */
public abstract class SimulationObject implements Serializable {
    private Date startTime;
    private double startLon;
    private double startLat;
    private double startX = -1;
    private double startY = -1;

    SimulationObject(Date startTime, double startLon, double startLat) {
        this.startTime = startTime;
        this.startLon = startLon;
        this.startLat = startLat;
    }

    public String getStartTime() {
        return startTime.getStringDate();
    }

    public long getStartTime(Date refTime) {
        long time = startTime.diff(refTime);
        if (time >= 0){
            return time;
        }
        return -1;
    }

    public double getStartLon() {
        return startLon;
    }

    public double getStartLat() {
        return startLat;
    }

    double getStartX() {

        return startX;
    }

    double getStartY() {
        return startY;
    }

    public Point getStartPoint() {
        return new Point(getStartX(),getStartY());
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public abstract void write(FileWriter writer)  throws IOException;

}
