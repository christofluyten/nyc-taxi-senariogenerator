package data;

import com.github.rinde.rinsim.geom.Point;

/**
 * Created by christof on 15.02.17.
 */
public class NycArea extends Area {
    Point[] points = {new Point(-74.065894, -40.540961), new Point(-74.013583, -40.756804),
            new Point(-73.918480, -40.917166), new Point(-73.743769, -40.879278), new Point(-73.705917, -40.541482)};

    public NycArea() {
        super();
        setPoints(points);
    }


}
