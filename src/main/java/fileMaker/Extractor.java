package fileMaker;

import com.github.rinde.rinsim.geom.Point;
import data.*;
import data.Date;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by christof on 18.11.16.
 */


class Extractor {

    private List<SimulationObject> simulationObjectList;
    private IOHandler ioHandler;
    private Map<String,Integer> taxiCapacityMap;
    private Set<String> licenseNbSet;
    private Map<String,String[]> positioningMap;
    private int count1 = 0;
    private int count2 = 0;

    private Area Nyc = new NycArea();




    Extractor(IOHandler ioHandler) {
        this.ioHandler = ioHandler;
        this.simulationObjectList = new ArrayList<>();
        this.licenseNbSet = new HashSet<>();
    }


    public IOHandler getIoHandler() {
        return ioHandler;
    }

    private void extractSimulationObjects(String path, Commander commander, Date startTime, Date endTime) throws IOException {
        List<String[]> splitLines = extractLines(path,startTime,endTime);

        for(String[] splitLine: splitLines){
            commander.execute(splitLine);
        }

        System.out.println(simulationObjectList.size() + " objects are extracted");

    }

    void extractAndPositionPassengers() throws IOException, ClassNotFoundException {
        Commander commander = new Commander();
        Command addPassenger = new addPassengerCommand();
        commander.setCommand(addPassenger);
        extractSimulationObjects(getIoHandler().getPassengerDataFile(),commander,getIoHandler().getPassengerStartTime(),getIoHandler().getPassengerEndTime());

        Positioner positioner = new Positioner(simulationObjectList,getIoHandler());
        positioner.setPassengerPositions();
    }

    void extractAndPositionTaxis() throws IOException, ClassNotFoundException {
        Commander commander = new Commander();
        Command addTaxi = new addTaxiCommand();
        taxiCapacityMap = ioHandler.getTaxiCapacity();
        setPositioningMap();
        commander.setCommand(addTaxi);
        extractSimulationObjects(getIoHandler().getTaxiDataFile(),commander,getIoHandler().getTaxiStartTime(),getIoHandler().getTaxiEndTime());
        System.out.println("taxi's found in map " + count1);
        System.out.println("taxi's not in map " + count2);
        Positioner positioner = new Positioner(simulationObjectList, getIoHandler());
        positioner.setTaxiPositions();
    }

    private Map<String,String[]> setPositioningMap() throws FileNotFoundException {
        this.positioningMap = new HashMap<>();
        List<String[]> lines = extractLines(getIoHandler().getPassengerDataFile(),Date.getNextHour(getIoHandler().getPassengerStartTime(),-2)
                ,getIoHandler().getPassengerStartTime());
        for(String[] line : lines){
            if(cleanLine(line)) {
                if(!positioningMap.containsKey(line[0]) || new Date(positioningMap.get(line[0])[6]).diff(new Date(line[6])) < 0 ) {
                    positioningMap.put(line[0],line);
                }
            }
        }
        System.out.println("size positioningMap " + positioningMap.size());
        return positioningMap;
    }


    private static List<String[]> extractLines(String path, Date startTime, Date endTime) throws FileNotFoundException {
        System.out.println("extractLines " + startTime.getStringDate() + " " + endTime.getStringDate());
        Scanner scanner = new Scanner(new File(path));
        scanner.nextLine();
        List<String[]> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splitLine = line.split(",");
            String stringTime = splitLine[5];
            Date currentTime = new Date(stringTime);
            if (endTime.lessThan(currentTime)) {
                break;
            }
            if (!currentTime.lessThan(startTime)) {
                lines.add(splitLine);
            }
        }
        scanner.close();
        return lines;
    }

    static List<String[]> extractLinesFromTravelTimes(String path, Date startTime, Date endTime) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        scanner.nextLine();
        List<String[]> lines = new ArrayList<>();
        List<String> times = new ArrayList<>();
        while (startTime.lessThan(endTime)){
            times.add(startTime.getStringDate());
            startTime = Date.getNextHour(startTime,1);
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splitLine = line.split(",");
            String stringTime = splitLine[2];
//            Date currentTime = new Date(stringTime);
            if (times.contains(stringTime)) {
                lines.add(splitLine);
            }
        }
        scanner.close();
        return lines;
    }





//Command

    interface Command {

        void execute(String[] splitLine) throws IOException;

    }

    //Concrete Command

    private class addPassengerCommand implements Command {


        addPassengerCommand() {
        }

        public void execute(String[] splitLine)throws IOException{
            Area manhattan = new ManhattanArea();
            if(cleanLine(splitLine)){
                Passenger passenger = new Passenger(Math.min(Integer.valueOf(splitLine[7]),4),new Date(splitLine[5]), Double.valueOf(splitLine[10]), -1*Double.valueOf(splitLine[11]),
                        Double.valueOf(splitLine[12]), -1*Double.valueOf(splitLine[13]));
                if(manhattan.contains(new Point(passenger.getStartLon(),passenger.getStartLat()))
                        && manhattan.contains(new Point(passenger.getEndLon(),passenger.getEndLat()))){
                    simulationObjectList.add(passenger);
//                    System.out.println("added");
                }
//                System.out.println(passenger.getStartLon()+" "+passenger.getStartLat()+" "+
//                        passenger.getEndLon()+" "+passenger.getEndLat());
            }
        }
    }

    private boolean cleanLine(String[] line) {
        Point startPoint = new Point(Double.valueOf(line[10]),-1*Double.valueOf(line[11]));
        if (!(Integer.valueOf(line[7]) > 0)){
            return false;
        } else if (! (Integer.valueOf(line[8]) > 0)){
            return false;
        } else if (! (Double.valueOf(line[9]) > 0)){
            return false;
//        } else if (! (Link.getHighestLongitude() >= Double.valueOf(line[10]) && Double.valueOf(line[10]) >= Link.getLowestLongitude())){
//            return false;
        } else if (! (Link.getHighestLongitude() >= Double.valueOf(line[12]) && Double.valueOf(line[12]) >= Link.getLowestLongitude())){
            return false;
//        } else if (! (Link.getHighestLatitude() >= -1*Double.valueOf(line[11]) && -1*Double.valueOf(line[11]) >= Link.getLowestLatitude())) {
//            return false;
        } else if (!Nyc.contains(startPoint)){
            return false;
        } else
        return Link.getHighestLatitude() >= -1 * Double.valueOf(line[13]) && -1 * Double.valueOf(line[13]) >= Link.getLowestLatitude();

    }

    private class addTaxiCommand implements Command {

        addTaxiCommand() {
        }

        public void execute(String[] splitLine) throws IOException {
            if(!licenseNbSet.contains(splitLine[0])){
                if(cleanLine(splitLine)) {
                    int capacity = 4;
                    try {
                        capacity = taxiCapacityMap.get(splitLine[0]);
                    } catch (Exception e) {
                        System.out.println("Failed to find a capacity for " + splitLine[0]);
                    }
                    String[] line;
                    Taxi taxi;
                    if(positioningMap.containsKey(splitLine[0])) {
                        line = positioningMap.get(splitLine[0]);
                        taxi = new Taxi(line[0], capacity, new Date(line[6]), Double.valueOf(line[12]), -1 * Double.valueOf(line[13]));
                        count1++;
                    } else {
                        taxi = new Taxi(splitLine[0], capacity, new Date(splitLine[5]), Double.valueOf(splitLine[10]), -1 * Double.valueOf(splitLine[11]));
                        count2++;
                    }
                    simulationObjectList.add(taxi);
                    licenseNbSet.add(splitLine[0]);
                }
            }
        }
    }



//Commander
private class Commander {

        private Command command;

        void setCommand(Command command) {

            this.command = command;

        }

        void execute(String[] splitLine) throws IOException {

            command.execute(splitLine);

        }

    }

}