package data;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Christof on 8/11/2016.
 */
public class Link implements Serializable {

    private final String id;
    private final double length;
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;

    private static double highestLongitude = -73.696494;
    private static double lowestLongitude = -74.203028;
    private static double highestLatitude = -40.5116616;
    private static double lowestLatitude = -41.002768;

    private static double nbOfLonStep = 41;
    private static double nbOfLatStep = 40;
    private static double longitudeStep = (getHighestLongitude() - getLowestLongitude()) / getNbOfLonStep();
    private static double latitudeStep = (getHighestLatitude() - getLowestLatitude()) / getNbOfLatStep();

    public Link(String id, double length, double startX, double startY, double endX, double endY) {
        this.id = id;
        this.length = length;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }


    public String getId() {
        return id;
    }

    public double getLength() {
        return length;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public static double getHighestLongitude() {
        return highestLongitude;
    }

    public static double getLowestLongitude() {
        return lowestLongitude;
    }

    public static double getHighestLatitude() {
        return highestLatitude;
    }

    public static double getLowestLatitude() {
        return lowestLatitude;
    }

    public static double getNbOfLonStep() {
        return nbOfLonStep;
    }

    public static double getNbOfLatStep() {
        return nbOfLatStep;
    }

    public static double getLongitudeStep() {
        return longitudeStep;
    }

    public static double getLatitudeStep() {
        return latitudeStep;
    }

    public Node getStartNode(){
        return new Node(startX,startY);
    }

    public Node getEndNode(){
        return new Node(endX,endY);
    }


    public String toString() {
        return "Link " + getId() + " " + getStartX() + " " + getStartY() + " " + getEndX() + " " + getEndY();
    }

    public static void writeTitles(FileWriter writer) throws IOException {
        writer.write("id,length,startX,startY,endX,endY\n");
    }

    public void write(FileWriter writer) throws IOException {
        writer.write(getId());
        writer.write(",");
        writer.write(String.valueOf(getLength()));
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Link.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Link other = (Link) obj;
        if (this.startX != other.getStartX()){
            return false;
        }
        if (this.startY != other.getStartY()){
            return false;
        }
        if (this.endX != other.getEndX()){
            return false;
        }
        if (this.endY != other.getEndY()){
            return false;
        }
        return true;
    }

    public int hashCode(){
        return (String.valueOf(startX).hashCode() ^ String.valueOf(startY).hashCode())*(String.valueOf(endX).hashCode() ^ String.valueOf(endY).hashCode());
    }
}
