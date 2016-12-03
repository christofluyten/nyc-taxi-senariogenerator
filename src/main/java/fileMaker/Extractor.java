package fileMaker;

import data.Link;
import data.Passenger;
import data.SimulationObject;
import data.Taxi;

import java.io.File;
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


    public IOHandler getIoHandler() {
        return ioHandler;
    }

    private void extractAndPositionSimulationObject(Scanner scanner, Commander commander, String startTime, String endTime) throws IOException {
//        System.out.println("Start extracting");
        Boolean begin = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splittedLine = line.split(",");
            String[] currentTime = splittedLine[5].split(":");
            if (currentTime[0].equals(endTime)) {
                break;
            }
            if (!begin && currentTime[0].equals(startTime)) {
                begin = true;
            }
            if (begin) {
                commander.execute(splittedLine);
            }
        }
        scanner.close();

        System.out.println(simulationObjectList.size() + " objects are extracted");

    }

    void extractAndPositionPassengers() throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(new File(getIoHandler().getPassengerDataFile()));
        Commander commander = new Commander();
        Command addPassenger = new addPassengerCommand();
        commander.setCommand(addPassenger);
        extractAndPositionSimulationObject(scanner,commander,getIoHandler().getPassengerStartTime(),getIoHandler().getPassengerEndTime());

        Positioner positioner = new Positioner(simulationObjectList, getIoHandler());
        positioner.setPassengerPositions();
    }

    void extractAndPositionTaxis() throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(new File(getIoHandler().getTaxiDataFile()));
        Commander commander = new Commander();
        Command addTaxi = new addTaxiCommand();
        taxiCapacityMap = ioHandler.getTaxiCapacity();
        commander.setCommand(addTaxi);
        extractAndPositionSimulationObject(scanner,commander,getIoHandler().getTaxiStartTime(),getIoHandler().getTaxiEndTime());

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
            return Link.getHighestLatitude() >= -1 * Double.valueOf(line[13]) && -1 * Double.valueOf(line[10]) >= Link.getLowestLatitude();


    }

    //Concrete Command

    interface Command {

        void execute(String[] splittedLine) throws IOException;

    }

    private class addPassengerCommand implements Command {


        addPassengerCommand() {
        }

        public void execute(String[] splittedLine)throws IOException{
            if (cleanLine(splittedLine)) {
                Passenger passenger = new Passenger(Integer.valueOf(splittedLine[7]), splittedLine[5], Double.valueOf(splittedLine[10]), -1 * Double.valueOf(splittedLine[11]),
                        Double.valueOf(splittedLine[12]), -1 * Double.valueOf(splittedLine[13]));
                simulationObjectList.add(passenger);

            }
        }
    }

    private class addTaxiCommand implements Command {

        addTaxiCommand() {
        }

        public void execute(String[] splittedLine) throws IOException {
            if (!licenseNbSet.contains(splittedLine[0])) {
                if (cleanLine(splittedLine)) {
                    int capacity = 4;
                    try {
                        capacity = taxiCapacityMap.get(splittedLine[0]);
                    } catch (Exception e) {
                        System.out.println("Failed to find a capacity for " + splittedLine[0]);
                    }
                    Taxi taxi = new Taxi(splittedLine[0], capacity, splittedLine[5], Double.valueOf(splittedLine[10]), -1 * Double.valueOf(splittedLine[11]));
                    simulationObjectList.add(taxi);
                    licenseNbSet.add(splittedLine[0]);
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

    void execute(String[] splittedLine) throws IOException {

            command.execute(splittedLine);

        }

    }

}