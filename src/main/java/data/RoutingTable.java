package data;

import com.github.rinde.rinsim.core.model.road.RoadPath;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.collect.HashBasedTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christof on 1/04/2017.
 */
public class RoutingTable implements Serializable {
    private HashBasedTable<Point, Point, Route> table = HashBasedTable.create();

    public RoutingTable() {
    }

    public void addRoute(Point rowPoint, Point columnPoint, Route route) {
        table.put(rowPoint, columnPoint, route);
    }

    public Route getRoute(Point rowPoint, Point columnPoint) {
        return table.get(rowPoint, columnPoint);
    }

    public boolean containsRoute(Point rowPoint, Point columnPoint) {
        return table.contains(rowPoint, columnPoint);
    }

    public int size() {
        return table.size();
    }

    public String toString() {
        return table.toString();
    }

    public RoadPath getPathTo(Point from, Point to) {
        final List<Point> path = getPath(from, to);
        double travelTime = getRoute(from, to).getTravelTime();

        return RoadPath.create(path, travelTime, travelTime);
    }

    private List<Point> getPath(Point from, Point to) {
        List<Point> path = new ArrayList<>();
        path.add(from);
        if (from.equals(to)) {
            return path;
        }
        Point nextHop = table.get(from, to).getNextHop();
        while (!nextHop.equals(to)) {
            path.add(nextHop);
            nextHop = table.get(nextHop, to).getNextHop();
        }
        path.add(to);
        return path;
    }
}
