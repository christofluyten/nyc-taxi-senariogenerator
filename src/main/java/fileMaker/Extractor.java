package fileMaker;

import data.Date;
import data.*;

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
    private Map<String, Integer> taxiCapacityMap;
    private Set<String> licenseNbSet;



    Extractor(IOHandler ioHandler) {
        this.ioHandler = ioHandler;
        this.simulationObjectList = new ArrayList<>();
        this.licenseNbSet = new HashSet<>();
    }

    static List<String[]> extractLines(String path, Date startTime, Date endTime, int timePosition) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        scanner.nextLine();
        List<String[]> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splitLine = line.split(",");
            String stringTime = splitLine[timePosition];
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

    static List<String[]> extractLinesFromTravelTimes(String path, Date startTime, Date endTime, int timePosition) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        scanner.nextLine();
        List<String[]> lines = new ArrayList<>();
        List<String> times = new ArrayList<>();
        while (startTime.lessThan(endTime)) {
            times.add(startTime.getStringDate());
            startTime = Date.getNextHour(startTime);
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splitLine = line.split(",");
            String stringTime = splitLine[timePosition];
            Date currentTime = new Date(stringTime);
            if (times.contains(stringTime)) {
                lines.add(splitLine);
            }
        }
        scanner.close();
        return lines;
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }

    private void extractAndPositionSimulationObject(String path, Commander commander, Date startTime, Date endTime) throws IOException {
        List<String[]> splitLines = extractLines(path, startTime, endTime, 5);

        for (String[] splitLine : splitLines) {
            commander.execute(splitLine);
        }

        System.out.println(simulationObjectList.size() + " objects are extracted");

    }

    void extractAndPositionPassengers() throws IOException, ClassNotFoundException {
        Commander commander = new Commander();
        Command addPassenger = new addPassengerCommand();
        commander.setCommand(addPassenger);
        extractAndPositionSimulationObject(getIoHandler().getPassengerDataFile(), commander, getIoHandler().getPassengerStartTime(), getIoHandler().getPassengerEndTime());

        Positioner positioner = new Positioner(simulationObjectList, getIoHandler());
        positioner.setPassengerPositions();
    }

    void extractAndPositionTaxis() throws IOException, ClassNotFoundException {
        Commander commander = new Commander();
        Command addTaxi = new addTaxiCommand();
        taxiCapacityMap = ioHandler.getTaxiCapacity();
        commander.setCommand(addTaxi);
        extractAndPositionSimulationObject(getIoHandler().getTaxiDataFile(), commander, getIoHandler().getTaxiStartTime(), getIoHandler().getTaxiEndTime());

        Positioner positioner = new Positioner(simulationObjectList, getIoHandler());
        positioner.setTaxiPositions();
    }


//Command

    private boolean cleanLine(String[] line) {
        if (!(Integer.valueOf(line[7]) > 0)) {
            return false;
        } else if (!(Integer.valueOf(line[8]) > 0)) {
            return false;
        } else if (!(Double.valueOf(line[9]) > 0)) {
            return false;
        } else if (!(Link.getHighestLongitude() >= Double.valueOf(line[10]) && Double.valueOf(line[10]) >= Link.getLowestLongitude())) {
            return false;
        } else if (!(Link.getHighestLongitude() >= Double.valueOf(line[12]) && Double.valueOf(line[12]) >= Link.getLowestLongitude())) {
            return false;
        } else if (!(Link.getHighestLatitude() >= -1 * Double.valueOf(line[11]) && -1 * Double.valueOf(line[11]) >= Link.getLowestLatitude())) {
            return false;
        } else
            return Link.getHighestLatitude() >= -1 * Double.valueOf(line[13]) && -1 * Double.valueOf(line[13]) >= Link.getLowestLatitude();


    }

    //Concrete Command

    interface Command {

        void execute(String[] splitLine) throws IOException;

    }

    private class addPassengerCommand implements Command {


        addPassengerCommand() {
        }

        public void execute(String[] splitLine) throws IOException {
            if (cleanLine(splitLine)) {
                Passenger passenger = new Passenger(Integer.valueOf(splitLine[7]), new Date(splitLine[5]), Double.valueOf(splitLine[10]), -1 * Double.valueOf(splitLine[11]),
                        Double.valueOf(splitLine[12]), -1 * Double.valueOf(splitLine[13]));
                simulationObjectList.add(passenger);
            }
        }
    }

    private class addTaxiCommand implements Command {

        addTaxiCommand() {
        }

        public void execute(String[] splitLine) throws IOException {
            if (!licenseNbSet.contains(splitLine[0])) {
                if (cleanLine(splitLine)) {
                    int capacity = 4;
                    try {
                        capacity = taxiCapacityMap.get(splitLine[0]);
                    } catch (Exception e) {
                        System.out.println("Failed to find a capacity for " + splitLine[0]);
                    }
                    Taxi taxi = new Taxi(splitLine[0], capacity, new Date(splitLine[5]), Double.valueOf(splitLine[10]), -1 * Double.valueOf(splitLine[11]));
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