package data;

import com.github.rinde.rinsim.geom.Point;

/**
 * Created by christof on 15.02.17.
 */
public class JfkArea extends Area {
    Point[] points = {new Point(-73.828012, -40.635186), new Point(-73.828012, -40.670421),
            new Point(-73.772050, -40.670421), new Point(-73.772050, -40.635186)};

    public JfkArea() {
        super();
        setPoints(points);
    }
}
