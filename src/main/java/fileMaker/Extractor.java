package fileMaker;

import data.Link;
import data.Passenger;
import data.SimulationObject;
import data.Taxi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by christof on 18.11.16.
 */


public class Extractor {

    private List<SimulationObject> simulationObjectList;
    private IOHandler ioHandler;



    public Extractor(IOHandler ioHandler) {
        this.ioHandler = ioHandler;
        this.simulationObjectList = new ArrayList<SimulationObject>();
    }


    public List<SimulationObject> getSimulationObjectList() {
        return simulationObjectList;
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }


    public void extractAndPositionSimulationObject(Scanner scanner, Commander commander, String startTime, String endTime) throws IOException {
        System.out.println("Start extracting");
        Boolean begin = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splittedLine = line.split(",");
            String[] currentTime = splittedLine[5].split(":");
            if (currentTime[0].equals(endTime)) {
                break;
            }
            if (begin == false && currentTime[0].equals(startTime)) {
                begin = true;
            }
            if (begin == true && !((startTime+":00:00").equals(currentTime[0]+":"+currentTime[1]+":"+currentTime[2]))) {
                commander.execute(splittedLine);
            }
        }
        scanner.close();

        System.out.println(getSimulationObjectList().size() + " objects are extracted");

    }

    public void extractAndPositionPassengers() throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(new File(getIoHandler().getPassengerDataFile()));
        Commander commander = new Commander();
        Command addPassenger = new addPassengerCommand();
        commander.setCommand(addPassenger);
        extractAndPositionSimulationObject(scanner,commander,getIoHandler().getPassengerStartTime(),getIoHandler().getPassengerEndTime());

        Positioner positioner = new Positioner(getSimulationObjectList(),getIoHandler());
        positioner.setPassengerPositions();
    }

    public void extractAndPositionTaxis() throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(new File(getIoHandler().getTaxiDataFile()));
        Commander commander = new Commander();
        Command addTaxi = new addTaxiCommand();
        commander.setCommand(addTaxi);
        extractAndPositionSimulationObject(scanner,commander,getIoHandler().getTaxiStartTime(),getIoHandler().getTaxiEndTime());

        Positioner positioner = new Positioner(getSimulationObjectList(), getIoHandler());
        positioner.setTaxiPositions();
    }


//Command

    interface Command {

        void execute(String[] splittedLine)throws IOException;

    }

    //Concrete Command

    class addPassengerCommand implements Command {


        public addPassengerCommand() {
        }

        public void execute(String[] splittedLine)throws IOException{
            Passenger passenger = new Passenger(splittedLine[5], Double.valueOf(splittedLine[10]), -1*Double.valueOf(splittedLine[11]),
                    Double.valueOf(splittedLine[12]), -1*Double.valueOf(splittedLine[13]));
            getSimulationObjectList().add(passenger);
        }
    }

    class addTaxiCommand implements Command {

        public addTaxiCommand() {
        }

        public void execute(String[] splittedLine)throws IOException{
            Taxi taxi = new Taxi(splittedLine[5], Double.valueOf(splittedLine[10]), -1*Double.valueOf(splittedLine[11]));
            getSimulationObjectList().add(taxi);
        }
    }



//Commander
    class Commander {

        private Command command;

        public void setCommand(Command command) {

            this.command = command;

        }

        public void execute(String[] splittedLine) throws IOException {

            command.execute(splittedLine);

        }

    }

}