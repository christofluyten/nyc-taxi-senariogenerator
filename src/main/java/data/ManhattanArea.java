package data;

import com.github.rinde.rinsim.geom.Point;

/**
 * Created by christof on 15.02.17.
 */
public class ManhattanArea extends Area {
    Point[] points = {new Point(-74.026583, -40.692996), new Point(-74.013583, -40.756804),
            new Point(-73.933184, -40.881932), new Point(-73.909665, -40.881932), new Point(-73.909665, -40.791690),
            new Point(-73.979016, -40.692996)};

    public ManhattanArea() {
        super();
        setPoints(points);
    }
}

