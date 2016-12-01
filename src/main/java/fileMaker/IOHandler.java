package fileMaker;

import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.ScenarioIO;
import data.Link;
import data.SimulationObject;
import map.CsvConverter;
import map.LinkCutter;
import map.LinkMapMaker;
import map.PositionToClosestLinksMaker;

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
    private String passengerDataFile;
    private String passengerStartTime;
    private String passengerEndTime;

    private String taxiDataFile;
    private String taxiStartTime;
    private String taxiEndTime;

    private String attribute = "";

    private static final String mapFileName="map";
    private static final String linkMapFileName ="linkMap";
    private static final String ptclFileName = "posToClosestLink";
    private static final String scenarioFileName = "scenario";
    private static final String positionedPassengersFileName = "positionedPassengers";
    private static final String positionedTaxisFileName = "positionedTaxis";
    private static final String linksFileName="links.csv";

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


    public String getAttribute() {
        return attribute;
    }

    public void makeMap() throws IOException {
        if(!fileExists(getMapFilePath())) CsvConverter.convertLinkMap(this);
    }

    public String getMapFilePath(){
        return mapsDirectory+mapFileName+getCutLength()+attribute+".dot";
    }

    public String getCutLength(){
        if(cutLength == -1){
            return "";
        } else return String.valueOf(cutLength);
    }

    public String getLinksFilePath(){
        return linksDirectory+linksFileName;
    }

    public void setScenarioFileFullName(String scenarioFileFullName) {
        this.scenarioFileFullName = scenarioFileFullName;
    }

    public String getPassengerDataFile() {
        return passengerDataFile;
    }

    public void setPassengerDataFile(String passengerDataFile) {
        this.passengerDataFile = passengerDataFile;
    }

    public String getPassengerStartTime() {
        return passengerStartTime;
    }

    public void setPassengerStartTime(String passengerStartTime) {
        this.passengerStartTime = passengerStartTime;
    }

    public String getPassengerEndTime() {
        return passengerEndTime;
    }

    public void setPassengerEndTime(String passengerEndTime) {
        this.passengerEndTime = passengerEndTime;
    }

    public String getTaxiDataFile() {
        return taxiDataFile;
    }

    public void setTaxiDataFile(String taxiDataFile) {
        this.taxiDataFile = taxiDataFile;
    }

    public String getTaxiStartTime() {
        return taxiStartTime;
    }

    public void setTaxiStartTime(String taxiStartTime) {
        this.taxiStartTime = taxiStartTime;
    }

    public String getTaxiEndTime() {
        return taxiEndTime;
    }

    public void setTaxiEndTime(String taxiEndTime) {
        this.taxiEndTime = taxiEndTime;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public static String getLinkMapFileName() {
        return linkMapFileName;
    }

    public static String getLinksDirectory() {
        return linksDirectory;
    }

    public String getLinkMapPath(){return linksDirectory+linkMapFileName+getCutLength();}

    public String getScenarioFileFullPath() {
        return scenariosDirectory + scenarioFileFullName;
    }

    public String getPtclPath() {
        return linksDirectory+ptclFileName+ getCutLength() + attribute;
    }

    public String getPositionedPassengersPath(){
        return passengersDirectory+positionedPassengersFileName+getCutLength() + attribute+"_"+passengerStartTime+"_"+passengerEndTime;
    }

    public String getPositionedTaxisPath(){
        return taxisDirectory+positionedTaxisFileName+getCutLength() + attribute+"_"+taxiStartTime+"_"+taxiEndTime;
    }


    public Boolean getPtclIsNotSet() {
        return ptclIsNotSet;
    }

    public void ptclIsSet() {
        this.ptclIsNotSet = false;
    }

    public Boolean getLinkMapIsNotSet() {
        return linkMapIsNotSet;
    }

    public void linkMapIsSet() {
        this.linkMapIsNotSet = false;
    }

    public Map<Integer, Map<Integer, Set<Link>>> getPositionToClosestLinks() throws IOException, ClassNotFoundException {
        if(getPtclIsNotSet()){
            try{
                readPtclMap();
            } catch (Exception e1){
                try {
                    PositionToClosestLinksMaker.makeMap(this);
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

    public Map<String, Link> getLinkMap() throws IOException {
        if(getLinkMapIsNotSet()){
            try{
                readLinkMap();
            } catch (Exception e1){
                System.out.println("geen linkMap: "+getLinkMapPath());
                System.out.println("poging om linkMap te maken");
                try {
                    if (!fileExists(linksDirectory+linkMapFileName)) {
                        LinkMapMaker.makeLinkMap(getLinksFilePath());
                    }
                    LinkCutter.cut(linksDirectory+linkMapFileName,cutLength);
                    readLinkMap();
                } catch (Exception e2){
                    System.out.println("geen links.csv: "+getLinksFilePath());
                    throw new IOException();
                }
            }
        }
        System.out.println("linkMap gevonden "+getLinkMapPath());
        return this.linkMap;
    }

    public void readLinkMap() throws IOException, ClassNotFoundException {
        Map<String,Link> map = (HashMap<String, Link>) readFile(getLinkMapPath());
        this.linkMap = map;
        linkMapIsSet();
    }
    public void readPtclMap() throws IOException, ClassNotFoundException {
        Map<Integer, Map<Integer, Set<Link>>> map = (HashMap<Integer, Map<Integer, Set<Link>>>) readFile(getPtclPath());
        this.positionToClosestLinks = map;
        ptclIsSet();
    }


    public static void writeLinkMap(Map<String,Link> map, String cutLength) throws IOException, ClassNotFoundException {
        System.out.println("writeLinkMap with "+cutLength);

        writeFile(map,linksDirectory+linkMapFileName+ cutLength);
    }

    public Object readFile(String path) throws IOException, ClassNotFoundException {
        File file = new File(path);
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        Object object = s.readObject();
        s.close();
        return object;
    }

    public static void writeFile(Object object, String path) throws IOException, ClassNotFoundException {
        File file = new File(path);
        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream s = new ObjectOutputStream(f);
        s.writeObject(object);
        s.close();
    }

    public void writePositionedObjects(List<SimulationObject> positionedObjects, String path) throws IOException, ClassNotFoundException {
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
}
