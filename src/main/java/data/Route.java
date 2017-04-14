package data;

import com.github.rinde.rinsim.geom.Point;

import java.io.Serializable;

/**
 * Created by Christof on 1/04/2017.
 */
public class Route implements Serializable{
    private Point nextHop;
    private double travelTime;

    public Route(Point nextHop, double travelTime) {
        this.nextHop = nextHop;
        this.travelTime = travelTime;
    }

    public Point getNextHop() {
        return nextHop;
    }

    public double getTravelTime() {
        return travelTime;
    }

    @Override
    public String toString() {
        return nextHop +", "+ travelTime;
    }
}
