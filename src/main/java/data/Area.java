package data;

import com.github.rinde.rinsim.geom.Point;

/**
 * Created by christof on 15.02.17.
 */
public class Area {



    private Point[] points;

    public Area(Point[] points) {
        this.points = points;
    }

    public Area() {
        Point[] points = {new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 0)};
        setPoints(points);
    }

    public static void main(String[] args) {
//        Point[] points = {new Point(0,0),new Point(0,5),new Point(5,5),new Point(5,0)};
        Point[] points = {new Point(0, 0), new Point(5, 5), new Point(0, 5), new Point(5, 0)};
        Area area = new Area(points);
        Point p1 = new Point(1, 2.5);
        System.out.println(area.contains(p1));
        Point p2 = new Point(10, 10);
        System.out.println(area.contains(p2));
        Point p3 = new Point(2.5, 3);
        System.out.println(area.contains(p3));
        Point p4 = new Point(2.5, 2.5);
        System.out.println(area.contains(p4));
        Point p5 = new Point(0, 1);
        System.out.println(area.contains(p5));
        Point p6 = new Point(0, 0);
        System.out.println(area.contains(p6));
    }

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public boolean contains(Point point) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].y > point.y) != (points[j].y > point.y) &&
                    (point.x < (points[j].x - points[i].x) * (point.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
                result = !result;
            }
        }
        return result;
    }
}
