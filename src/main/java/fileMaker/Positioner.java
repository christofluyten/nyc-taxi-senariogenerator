package fileMaker;

import data.Link;
import data.Passenger;
import data.SimulationObject;
import data.Taxi;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by christof on 18.11.16.
 */
class Positioner {

    private List<SimulationObject> simulationObjects;
    private IOHandler ioHandler;
    private List<SimulationObject> positionedObjects;

    Positioner(List<SimulationObject> simulationObjects, IOHandler ioHandler) {
        this.simulationObjects = simulationObjects;
        this.ioHandler = ioHandler;
        this.positionedObjects = new ArrayList<>();
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }

    private List<SimulationObject> getSimulationObjects() {
        return simulationObjects;
    }

    private List<SimulationObject> getPositionedObjects() {
        return positionedObjects;
    }

    void setPassengerPositions() throws IOException, ClassNotFoundException {
        FileWriter writer = new FileWriter(getIoHandler().getPositionedPassengersPath()+".csv");
        Passenger.writeTitles(writer);
        Commander commander = new Commander();
        Command addPassenger = new addPassengerCommand();
        commander.setCommand(addPassenger);
        setPositions(writer, commander);
        getIoHandler().writePositionedObjects(getPositionedObjects(),getIoHandler().getPositionedPassengersPath());
    }

    void setTaxiPositions() throws IOException, ClassNotFoundException {
        FileWriter writer = new FileWriter(getIoHandler().getPositionedTaxisPath()+".csv");
        Taxi.writeTitles(writer);
        Commander commander = new Commander();
        Command addTaxi = new addTaxiCommand();
        commander.setCommand(addTaxi);
        setPositions(writer, commander);
        getIoHandler().writePositionedObjects(getPositionedObjects(),getIoHandler().getPositionedTaxisPath());
    }


    private void setPositions(FileWriter writer, Commander commander) throws IOException, ClassNotFoundException {
//        System.out.println("Start positioning");
        int count = 0;
        for(SimulationObject object : getSimulationObjects()){
            int x = (int) Math.floor((object.getStartLon() - Link.getLowestLongitude()) / Link.getLongitudeStep());
            int y = (int) Math.floor((object.getStartLat() - Link.getLowestLatitude()) / Link.getLatitudeStep());
            Link closestLink = null;
            double smallestDistance = 100000;

            Set<Link> linkSet = getIoHandler().getPositionToClosestLinks().get(x).get(y);

            try {


                for (Link link : linkSet) {
                    double startX = link.getStartX();
                    double startY = link.getStartY();
                    double endX = link.getEndX();
                    double endY = link.getEndY();

                    double distance = shortestDistanceToSegment(startX, startY, endX, endY, object.getStartLon(), object.getStartLat());

                    if (distance < smallestDistance) {
                        smallestDistance = distance;
                        closestLink = link;
                    }
                }

                if (smallestDistance < 1000) {
                    count++;
                    double distToStart = getDistance(closestLink.getStartX(), closestLink.getStartY(), object.getStartLon(), object.getStartLat());
                    double distToEnd = getDistance(closestLink.getEndX(), closestLink.getEndY(), object.getStartLon(), object.getStartLat());
                    if (distToStart < distToEnd) {
                        object.setStartX(closestLink.getStartX());
                        object.setStartY(closestLink.getStartY());
                    } else {
                        object.setStartX(closestLink.getEndX());
                        object.setStartY(closestLink.getEndY());
                    }

                    if (commander.execute(object)) {
                        object.write(writer);
                        getPositionedObjects().add(object);
                    }
                } else {
                    System.out.println("objects's pickup location too far from the street " + object.getStartTime() + " " + smallestDistance);
                }

            } catch (NullPointerException e) {
                System.out.println("no links were found in de ptclMap for " + object.getStartLon() + " " + object.getStartLat());
            }
        }
        writer.close();
        System.out.println(count + " objects were repositioned");

    }


    private double shortestDistanceToSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        double px = x2 - x1;
        double py = y2 - y1;
        double temp = (px * px) + (py * py);
        double u = ((x3 - x1) * px + (y3 - y1) * py) / (temp);
        if (u > 1) {
            u = 1;
        } else if (u < 0) {
            u = 0;
        }
        double x = x1 + u * px;
        double y = y1 + u * py;

        double dx = x - x3;
        double dy = y - y3;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    //Command

    interface Command {

        boolean execute(SimulationObject object) throws IOException, ClassNotFoundException;

    }

    //Concrete Command

    private class addPassengerCommand implements Command {


        addPassengerCommand() {
        }

        public boolean execute(SimulationObject object) throws IOException, ClassNotFoundException {
            Passenger passenger = (Passenger) object;
            int x = (int) Math.floor((passenger.getEndLon() - Link.getLowestLongitude()) / Link.getLongitudeStep());
            int y = (int) Math.floor((passenger.getEndLat() - Link.getLowestLatitude()) / Link.getLatitudeStep());
            Link closestLink = null;
            double smallestDistance = 100000;

            Set<Link> linkSet = getIoHandler().getPositionToClosestLinks().get(x).get(y);

            try {
                for (Link link : linkSet) {
                    double startX = link.getStartX();
                    double startY = link.getStartY();
                    double endX = link.getEndX();
                    double endY = link.getEndY();

                    double distance = shortestDistanceToSegment(startX, startY, endX, endY, passenger.getEndLon(), passenger.getEndLat());

                    if (distance < smallestDistance) {
                        smallestDistance = distance;
                        closestLink = link;
                    }
                }

                if (smallestDistance < 1000) {
                    double distToStart = getDistance(closestLink.getStartX(), closestLink.getStartY(), passenger.getEndLon(), passenger.getEndLat());
                    double distToEnd = getDistance(closestLink.getEndX(), closestLink.getEndY(), passenger.getEndLon(), passenger.getEndLat());
                    if (distToStart < distToEnd) {
                        passenger.setEndX(closestLink.getStartX());
                        passenger.setEndY(closestLink.getStartY());
                    } else {
                        passenger.setEndX(closestLink.getEndX());
                        passenger.setEndY(closestLink.getEndY());
                    }
                    return true;
                } else {
                    System.out.println("objects's drop-off location too far from the street" + passenger.getStartTime() + " " + smallestDistance);
                    return false;
                }
            } catch (NullPointerException e) {
                System.out.println("no links were found in de ptclMap for " + passenger.getEndLon() + " " + passenger.getEndLat());
                return false;
            }
        }
    }

    private class addTaxiCommand implements Command {


        addTaxiCommand() {
        }

        public boolean execute(SimulationObject object) {
            return true;
        }
    }


    //Commander
    private class Commander {

        private Command command;

        void setCommand(Command command) {

            this.command = command;

        }

        boolean execute(SimulationObject object) throws IOException, ClassNotFoundException {

            return this.command.execute(object);

        }

    }

}


