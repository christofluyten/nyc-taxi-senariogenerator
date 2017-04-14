package data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
    private Map<Date,Double> travelTimesMap = new HashMap<>();
    private int amountOfCuts = 0;

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

    public double getLengthInM() {
        return length;
    }

    public double getLengthInKm() {
        return length/1000;
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

    public Map<Date, Double> getTravelTimesMap() {
        return travelTimesMap;
    }



    public Node getStartNode(){
        return new Node(startX,startY);
    }

    public Node getEndNode(){
        return new Node(endX,endY);
    }

    public void setTravelTimesMap(Map<Date, Double> travelTimesMap) {
        this.travelTimesMap = travelTimesMap;
    }

    public void printTravelTimesMap(){
        for(Date date:travelTimesMap.keySet()){
            System.out.print("TravelTimesMap: " +date.getStringDate()+" "+travelTimesMap.get(date));
        }
        System.out.print("\n");
    }

    public void setAmountOfCuts(int amountOfCuts) {
        this.amountOfCuts = amountOfCuts;
    }

    @Override
    public String toString() {
        return "Link " + getId() + " " + getStartX() + " " + getStartY() + " " + getEndX() + " " + getEndY();
    }

    public static void writeTitles(FileWriter writer) throws IOException {
        writer.write("id,length,startX,startY,endX,endY\n");
    }

    public void write(FileWriter writer) throws IOException {
        writer.write(getId());
        writer.write(",");
        writer.write(String.valueOf(getLengthInM()));
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
        return this.endY == other.getEndY();
    }

    public int hashCode(){
        return (String.valueOf(startX).hashCode() ^ String.valueOf(startY).hashCode())*(String.valueOf(endX).hashCode() ^ String.valueOf(endY).hashCode());
    }

    public double getSpeed(Date date) {

        return ((length*(amountOfCuts+1))/travelTimesMap.get(date))*3.6;
    }
}
