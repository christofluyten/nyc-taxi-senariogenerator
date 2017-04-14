package fileMaker;

import com.github.rinde.rinsim.core.model.road.RoutingTableHandler;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.ScenarioIO;
import data.Date;
import data.Link;
import data.RoutingTable;
import data.SimulationObject;
import map.CsvConverter;
import map.LinkMapHandler;
import map.PositionToClosestLinksHandler;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by christof on 25.11.16.
 */
public class IOHandler {
    private String taxiDataDirectory;
    private String travelTimesDirectory;
    private Date passengerStartTime;
    private Date passengerEndTime;

    private Date taxiStartTime;
    private Date taxiEndTime;

    private boolean withTraffic = false;

    private String attribute = "";

    private static final String mapFileName="map";
    private static final String linkMapFileName ="linkMap";
    private static final String ptclFileName = "posToClosestLink";
    private static final String scenarioFileName = "scenario";
    private static final String positionedPassengersFileName = "passengers";
    private static final String positionedTaxisFileName = "taxis";
    private static final String linksFileName="links.csv";
    private static final String linkToMinAndAverageTravelTimes = "linkToMinAndAverageTravelTimes";
    private static final String nodesIdToLinkId = "nodesIdToLinkId";


    private static final String linksDirectory = "src/main/resources/links/";
    private static final String mapsDirectory = "src/main/resources/maps/";
    private static final String passengersDirectory = "src/main/resources/passengers/";
    private static final String scenariosDirectory = "src/main/resources/scenarios/";
    private static final String taxisDirectory = "src/main/resources/taxis/";

    private Map<Integer, Map<Integer, Set<Link>>> positionToClosestLinks;
    private Boolean ptclIsNotSet = true;
    private Map<String, Link> linkMap;
    private Boolean linkMapIsNotSet = true;

    private String scenarioFileFullName;
    private int cutLength = -1;


    public IOHandler(){}

    public String getScenarioFileName() {
        return scenarioFileName;
    }

    String getTaxiCapacityPath() {
        return taxisDirectory+"capacity_"+taxiStartTime.getYear();
    }

    public String getAttribute() {
        return attribute;
    }

    public void makeMap() throws IOException {
        if(!fileExists(getMapFilePath())) CsvConverter.convertLinkMap(this);
    }

    public String getMapFilePath(){
        return mapsDirectory+mapFileName+getCutLength()+".dot";
    }

    private String getCutLength(){
        if(cutLength == -1){
            return "";
        } else return String.valueOf(cutLength);
    }

    String getLinksFilePath(){
        return linksDirectory+linksFileName;
    }

    public void setScenarioFileFullName(String scenarioFileFullName) {
        this.scenarioFileFullName = scenarioFileFullName;
    }

    public boolean getWithTraffic() {
        return withTraffic;
    }

    public void setWithTraffic() {
        this.withTraffic = true;
    }

    String getPassengerDataFile() {
        return taxiDataDirectory +"FOIL"+passengerStartTime.getYear()+"/trip_data_"+passengerStartTime.getMonth()+".csv";
    }

    public void setTaxiDataDirectory(String taxiDataDirectory) {
        this.taxiDataDirectory = taxiDataDirectory;
    }

    String getTravelTimesPath(String year) {
        return travelTimesDirectory+"travel_times_"+year+".csv";
    }

    public void setTravelTimesDirectory(String travelTimesDirectory) {
        this.travelTimesDirectory = travelTimesDirectory;
    }

    public Date getPassengerStartTime() {
        return passengerStartTime;
    }

    public void setPassengerStartTime(Date passengerStartTime) {
        this.passengerStartTime = passengerStartTime;
    }

    public Date getPassengerEndTime() {
        return passengerEndTime;
    }

    public void setPassengerEndTime(Date passengerEndTime) {
        this.passengerEndTime = passengerEndTime;
    }

    String getTaxiDataFile() {
        return taxiDataDirectory +"FOIL"+taxiStartTime.getYear()+"/trip_data_"+taxiStartTime.getMonth()+".csv";
    }

    String getTaxiDataYearPath() {
        return taxiDataDirectory +"FOIL"+taxiStartTime.getYear()+"/trip_data_";
    }

    Date getTaxiStartTime() {
        return taxiStartTime;
    }

    public void setTaxiStartTime(Date taxiStartTime) {
        this.taxiStartTime = taxiStartTime;
    }

    Date getTaxiEndTime() {
        return taxiEndTime;
    }

    public void setTaxiEndTime(Date taxiEndTime) {
        this.taxiEndTime = taxiEndTime;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

//    public static String getLinkMapFileName() {
//        return linkMapFileName;
//    }

//    public static String getLinksDirectory() {
//        return linksDirectory;
//    }

    private String getLinkMapPath(){return linksDirectory+linkMapFileName+getCutLength();}

    public String getScenarioFileFullPath() {
        return scenariosDirectory + scenarioFileFullName;
    }

    private String getPtclPath() {
        return linksDirectory+ptclFileName+ getCutLength();
    }

    public String getPositionedPassengersPath(){
        return passengersDirectory+positionedPassengersFileName+getCutLength() + attribute+"_"+passengerStartTime.getShortStringDateForPath()+"_"+passengerEndTime.getShortStringDateForPath();
    }

    public String getPositionedTaxisPath(){
    return taxisDirectory+positionedTaxisFileName+getCutLength() + attribute+"_"+taxiStartTime.getShortStringDateForPath()+"_"+taxiEndTime.getShortStringDateForPath();
    }

    static String getNodesIdToLinkIdPath(){
        return linksDirectory + nodesIdToLinkId;
    }


    private Boolean getPtclIsNotSet() {
        return ptclIsNotSet;
    }

    private void ptclIsSet() {
        this.ptclIsNotSet = false;
    }

    private Boolean getLinkMapIsNotSet() {
        return linkMapIsNotSet;
    }

    private void linkMapIsSet() {
        this.linkMapIsNotSet = false;
    }

    public Map<Integer, Map<Integer, Set<Link>>> getPositionToClosestLinks() throws IOException, ClassNotFoundException {
        if(getPtclIsNotSet()){
            try{
                readPtclMap();
            } catch (Exception e1){
                try {
                    PositionToClosestLinksHandler.makeMap(this);
                    readPtclMap();
                } catch (Exception e2){
                    System.out.println("geen ptclMap: "+getPtclPath());
                }
            }
        }
        return this.positionToClosestLinks;
    }

    public void writePositionToClosestLinks(Map<Integer, Map<Integer, Set<Link>>> positionToClosestLinks) throws IOException, ClassNotFoundException {
        writeFile(positionToClosestLinks,getPtclPath());
    }

    Map<String,Integer> getTaxiCapacity() throws IOException, ClassNotFoundException {
        if(!fileExists(getTaxiCapacityPath())){
            CapacityHandler.create(this);
        }
        return (Map<String, Integer>) readFile(getTaxiCapacityPath());
    }

    Map<String,List<Double>> getLinkToMinAndAverageTravelTimes() throws IOException, ClassNotFoundException {
        if(!fileExists(getLinkToMinAndAverageTravelTimesPath())){
            TravelTimesHandler.makeLinkToMinAndAverageTravelTimes(this);
        }
        return (Map<String,List<Double>>) readFile(getLinkToMinAndAverageTravelTimesPath());
    }

    static String getLinkToMinAndAverageTravelTimesPath(){
        return linksDirectory+ linkToMinAndAverageTravelTimes;
    }


    Map<String,Map<data.Date,Double>> getDateToTravelTimes() throws IOException, ClassNotFoundException {
        if(!fileExists(getDateToTravelTimesMapPath(passengerStartTime.getShortStringDateForPath(),passengerEndTime.getShortStringDateForPath()))){
            TravelTimesHandler.makeDateToTravelTimes(passengerStartTime,passengerEndTime,this);
        }
        return (Map<String,Map<data.Date,Double>>) readFile(getDateToTravelTimesMapPath(passengerStartTime.getShortStringDateForPath(),passengerEndTime.getShortStringDateForPath()));
    }


    public Map<String, Link> getLinkMap() throws IOException {
        if(getLinkMapIsNotSet()){
            try{
                readLinkMap();
            } catch (Exception e1){
                System.out.print("geen linkMap: "+getLinkMapPath());
                System.out.println("   poging om linkMap te maken");
                try {
                    if (!fileExists(linksDirectory+linkMapFileName)) {
                        LinkMapHandler.makeLinkMap(getLinksFilePath(),this);
                    }
                    LinkMapHandler.cut(linksDirectory+linkMapFileName,cutLength);
                    readLinkMap();
                } catch (Exception e2){
                    System.out.println("geen links.csv: "+getLinksFilePath() +"of iets veranderd aan link");
                    throw new IOException();
                }
            }
        }
//        System.out.println("linkMap gevonden "+getLinkMapPath());
        return this.linkMap;
    }

    private void readLinkMap() throws IOException, ClassNotFoundException {
        this.linkMap = (HashMap<String, Link>) readFile(getLinkMapPath());
        linkMapIsSet();
    }
    private void readPtclMap() throws IOException, ClassNotFoundException {
        this.positionToClosestLinks = (HashMap<Integer, Map<Integer, Set<Link>>>) readFile(getPtclPath());
        ptclIsSet();
    }


    public static void writeLinkMap(Map<String,Link> map, String cutLength) throws IOException, ClassNotFoundException {
        writeFile(map,linksDirectory+linkMapFileName+ cutLength);
    }

    public static Object readFile(String path) throws IOException, ClassNotFoundException {
        System.out.println("start reading "+path);
        File file = new File(path);
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        Object object = s.readObject();
        f.close();
        s.close();
        System.out.println("done reading "+path);
        return object;
    }

    public static void writeFile(Object object, String path) throws IOException, ClassNotFoundException {
        File file = new File(path);
        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream s = new ObjectOutputStream(f);
        s.writeObject(object);
        f.close();
        s.close();
        System.out.println(path+" added");
    }

    void writePositionedObjects(List<SimulationObject> positionedObjects, String path) throws IOException, ClassNotFoundException {
        writeFile(positionedObjects,path);
    }

    public List<SimulationObject> readPositionedObjects(String path) throws IOException, ClassNotFoundException {
        return (List<SimulationObject>)readFile(path);
    }


    public void writeScenario(Scenario scenario) throws IOException {
        ScenarioIO.write(scenario,Paths.get(getScenarioFileFullPath()));
    }

    public Scenario readScenario() throws IOException {
        return ScenarioIO.read(Paths.get(getScenarioFileFullPath()));
    }

    public Boolean fileExists(String filePath){
        File f = new File(filePath);
        return f.exists() && !f.isDirectory();
    }

    public void setCutLength(int cutLength) {
        this.cutLength = cutLength;
    }


    static String getDateToTravelTimesMapPath(String startDate, String endDate){
        return linksDirectory+"dateToTravelTimes_"+startDate+"_"+endDate;
    }

    public String getRoutingTablePath() {
        return mapsDirectory+"RoutingTable_"+cutLength+"_"+passengerStartTime.getShortStringDateForPath()+"_"+passengerEndTime.getShortStringDateForPath();
    }

    public RoutingTable getRoutingTable() throws IOException, ClassNotFoundException {
        if (!fileExists(getRoutingTablePath())){
            RoutingTableHandler handler = new RoutingTableHandler();
            handler.createTable(this);
        }
        return (RoutingTable) readFile(getRoutingTablePath());
    }
}
