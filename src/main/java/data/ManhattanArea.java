package data;

import com.github.rinde.rinsim.geom.Point;

/**
 * Created by christof on 15.02.17.
 */
public class ManhattanArea extends Area {
    Point[] points = {new Point(-73.933525, -40.881823), new Point(-73.915321, -40.875498),
            new Point(-73.911202, -40.879214), new Point(-73.907425, -40.876683), new Point(-73.906996, -40.872983),
            new Point(-73.909056, -40.871620), new Point(-73.914377, -40.862468), new Point(-73.927166, -40.846579),
            new Point(-73.933003, -40.835670), new Point(-73.932359, -40.814156), new Point(-73.931758, -40.808050),
            new Point(-73.927209, -40.802137), new Point(-73.922145, -40.802137), new Point(-73.916137, -40.797719),
            new Point(-73.910472, -40.791026), new Point(-73.925836, -40.778874), new Point(-73.936908, -40.779459),
            new Point(-73.939054, -40.774064), new Point(-73.939311, -40.769774), new Point(-73.956477, -40.750205),
            new Point(-73.961627, -40.743117), new Point(-73.969009, -40.712872), new Point(-73.995187, -40.704609),
            new Point(-74.021194, -40.680400), new Point(-74.030549, -40.684826), new Point(-74.020850, -40.727378),
            new Point(-74.013898, -40.756642), new Point(-73.986388, -40.795203), new Point(-73.952141, -40.850995)};

    public ManhattanArea() {
        super();
        setPoints(points);
    }
}

