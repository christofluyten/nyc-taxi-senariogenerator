package com.github.christof.experiment;

import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.TimeWindowPolicy;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.RealtimeClockController;
import com.github.rinde.rinsim.core.model.time.TimeModel;
import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.RoutingTableSupplier;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.pdptw.common.*;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.TimeOutEvent;
import com.github.rinde.rinsim.util.TimeWindow;
import data.*;
import fileMaker.IOHandler;
import fileMaker.PassengerHandler;
import fileMaker.TaxiHandler;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by christof on 23.11.16.
 */
public class ScenarioGenerator {
    //    private static final String TAXI_DATA_DIRECTORY = "D:/Taxi_data/";    //path to director with the FOIL-directories
    private static final String TAXI_DATA_DIRECTORY = "/media/christof/Elements/Taxi_data/";
    private static final String TRAVEL_TIMES_DIRECTORY = "D:/Traffic_estimates/"; //path to director with the travel_times
    private static final Date PASSENGER_START_TIME = new Date("2013-11-18 16:00:00");                   //format: "yyyy-mm-dd HH:mm:ss"
    private static final Date PASSENGER_END_TIME = new Date("2013-11-18 17:00:00");

    private static final Date TAXI_START_TIME = new Date("2013-11-18 16:00:00");
    private static final Date TAXI_END_TIME = new Date("2013-11-18 17:00:00");
    private static final double MAX_VEHICLE_SPEED_KMH = 120d;

//    private static final long PICKUP_DURATION = 30 * 1000L;
//    private static final long DELIVERY_DURATION = 30 * 1000L;

    private static final long PICKUP_DURATION = 0L;
    private static final long DELIVERY_DURATION = 0L;


    private static final String ATTRIBUTE = "TimeWindow";
    private static final int CUT_LENGTH = 500;                                                  //maximum length in meters of a edge in the graph (or "link" in the "map")

    private static final long SCENARIO_DURATION = (1 * 60 * 60 * 1000L) + 1L;

    private static final long SCENARIO_DURATION_DEBUG = (100 * 1000L) + 1L;

    private static final boolean TRAFFIC = true;


    private static final long TICK_SIZE = 250L;

    private boolean ridesharing;
    private boolean debug;



    private IOHandler ioHandler;

    public ScenarioGenerator() {
        IOHandler ioHandler = new IOHandler();
        ioHandler.setTaxiDataDirectory(TAXI_DATA_DIRECTORY);
        ioHandler.setPassengerStartTime(PASSENGER_START_TIME);
        ioHandler.setPassengerEndTime(PASSENGER_END_TIME);
        ioHandler.setTaxiStartTime(TAXI_START_TIME);
        ioHandler.setTaxiEndTime(TAXI_END_TIME);
        ioHandler.setAttribute(ATTRIBUTE);
        ioHandler.setCutLength(CUT_LENGTH);
        if (TRAFFIC) {
            ioHandler.setTravelTimesDirectory(TRAVEL_TIMES_DIRECTORY);
            ioHandler.setWithTraffic();
        }
        this.ioHandler = ioHandler;
        setScenarioFileFullName();
        makeMap();
    }

    public static void main(String[] args) throws Exception {
        ScenarioGenerator sg = new ScenarioGenerator();
        sg.generateTaxiScenario(false, false);
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }


    private void setScenarioFileFullName() {
        getIoHandler().setScenarioFileFullName(getIoHandler().getScenarioFileName() + "_" + getIoHandler().getAttribute() + "_" + getIoHandler().getPassengerStartTime().getShortStringDateForPath() + "_"
                + getIoHandler().getPassengerEndTime().getShortStringDateForPath());
    }

    private void makeMap() {
        try {
            getIoHandler().makeMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Scenario generateTaxiScenario(boolean ridesharing, boolean debug) throws Exception {
        this.ridesharing = ridesharing;
        this.debug = debug;
        Scenario.Builder builder = Scenario.builder();
        addGeneralProperties(builder);
        if (debug) {
            builder.addModel(
                    PDPGraphRoadModel.builderForGraphRm(
                            RoadModelBuilders
                                    .staticGraph(
                                            ListenableGraph.supplier(DotGraphIO.getMultiAttributeDataGraphSupplier(Paths.get(getIoHandler().getMapFilePath()))))
                                    .withSpeedUnit(NonSI.KILOMETERS_PER_HOUR)
                                    .withDistanceUnit(SI.KILOMETER)
                                    .withRoutingTable(true)
                    )
                            .withAllowVehicleDiversion(true))
//                    .addEvent(TimeOutEvent.create(SCENARIO_DURATION_DEBUG))
//                    .scenarioLength(SCENARIO_DURATION_DEBUG);
                    .addEvent(TimeOutEvent.create(SCENARIO_DURATION))
                    .scenarioLength(SCENARIO_DURATION);
//            addPassengersDebug(builder);
            addPassengers(builder);
        } else {
            builder.addModel(
                    PDPGraphRoadModel.builderForGraphRm(
                            RoadModelBuilders
                                    .staticGraph(
                                            ListenableGraph.supplier(DotGraphIO.getMultiAttributeDataGraphSupplier(Paths.get(getIoHandler().getMapFilePath()))))
                                    .withSpeedUnit(NonSI.KILOMETERS_PER_HOUR)
                                    .withDistanceUnit(SI.KILOMETER)
                                    .withRoutingTable(true)
                    )
                            .withAllowVehicleDiversion(true))
                    .addEvent(TimeOutEvent.create(SCENARIO_DURATION))
                    .scenarioLength(SCENARIO_DURATION);
            addPassengers(builder);
        }
        addTaxis(builder);
//            addJFK(builder);
//            addManhattan(builder);
//            addNYC(builder);
        Scenario scenario = builder.build();
        getIoHandler().writeScenario(scenario);
        return scenario;
//        }
    }


    private void addGeneralProperties(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        builder

//                .addModel(PDPRoadModel.builder(RoadModelBuilders.staticGraph(DotGraphIO.getLengthDataGraphSupplier(Paths.get(getIoHandler().getMapFilePath())))
//                .withDistanceUnit(SI.METER)
//                .withSpeedUnit(NonSI.KILOMETERS_PER_HOUR)))
//                .addModel(PDPRoadModel.builder(RoadModelBuilders.staticGraph(ListenableGraph.supplier(
//                                        (Supplier<? extends Graph<MultiAttributeData>>) DotGraphIO.getLengthDataGraphSupplier(Paths.get(getIoHandler().getMapFilePath()))))
//                        .withDistanceUnit(SI.METER)
//                        .withSpeedUnit(NonSI.KILOMETERS_PER_HOUR)))

                .addModel(TimeModel.builder()
                        .withRealTime()
                        .withStartInClockMode(RealtimeClockController.ClockMode.REAL_TIME)
                        .withTickLength(TICK_SIZE)
                        .withTimeUnit(SI.MILLI(SI.SECOND)))
                .addModel(
                        DefaultPDPModel.builder()
                                .withTimeWindowPolicy(TimeWindowPolicy.TimeWindowPolicies.TARDY_ALLOWED))
                .setStopCondition(StatsStopConditions.timeOutEvent())
                .addEvent(AddDepotEvent.create(-1, new Point(-73.9778627, -40.7888872)))
        ;
    }

    private void addTaxis(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        if (!(getIoHandler().fileExists(ioHandler.getPositionedTaxisPath()))) {
            TaxiHandler tfm = new TaxiHandler(ioHandler);
            tfm.extractAndPositionTaxis();
        }
        List<SimulationObject> taxis = getIoHandler().readPositionedObjects(ioHandler.getPositionedTaxisPath());
        int totalCount = 0;
        int addedCount = 0;
        for (SimulationObject object : taxis) {
            if (true && (totalCount % 20 == 0)) {
                addedCount++;
                Taxi taxi = (Taxi) object;
//            builder.addEvent(AddVehicleEvent.create(taxi.getStartTime(TAXI_START_TIME), VehicleDTO.builder()
                builder.addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder()
                        .speed(MAX_VEHICLE_SPEED_KMH)
                        .startPosition(taxi.getStartPoint())
                        .capacity(4)
                        .build()));
            }


            totalCount++;
            if (debug && addedCount >= 10) {
                break;
            }
        }
        System.out.println(addedCount + " taxi's added of the " + totalCount);
    }


    private void addPassengers(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        if (!(getIoHandler().fileExists(ioHandler.getPositionedPassengersPath()))) {
            PassengerHandler pfm = new PassengerHandler(ioHandler);
            pfm.extractAndPositionPassengers();
        }
        List<SimulationObject> passengers = getIoHandler().readPositionedObjects(ioHandler.getPositionedPassengersPath());
        int totalCount = 0;
        int addedCount = 0;
        RoutingTable routingTable = RoutingTableSupplier.getRoutingTable();
        for (SimulationObject object : passengers) {
            if (true && (totalCount % 20 == 0)) {
                addedCount++;
                Passenger passenger = (Passenger) object;
                long pickupStartTime = passenger.getStartTime(PASSENGER_START_TIME);
                long pickupTimeWindow = passenger.getStartTimeWindow(PASSENGER_START_TIME);
                long deliveryStartTime = getDeliveryStartTime(passenger, routingTable);
                Parcel.Builder parcelBuilder = Parcel.builder(passenger.getStartPoint(), passenger.getEndPoint())
                        .orderAnnounceTime(pickupStartTime)
                        .pickupTimeWindow(TimeWindow.create(pickupStartTime, pickupStartTime + pickupTimeWindow))
                        .pickupDuration(PICKUP_DURATION)
                        .deliveryDuration(DELIVERY_DURATION);
                if (ridesharing) {
                    parcelBuilder = parcelBuilder
                            .deliveryTimeWindow(TimeWindow.create(deliveryStartTime, deliveryStartTime + (pickupTimeWindow * 2)))
                            .neededCapacity(passenger.getAmount());
                } else {
                    parcelBuilder = parcelBuilder
                            .deliveryTimeWindow(TimeWindow.create(deliveryStartTime, deliveryStartTime + (pickupTimeWindow)))
                            .neededCapacity(4);
                }
                builder.addEvent(
                        AddParcelEvent.create(parcelBuilder.buildDTO()));
                long travelTime = (long) routingTable.getRoute(passenger.getStartPoint(), passenger.getEndPoint()).getTravelTime();
                System.out.println("+++++++++++++++++++++++++++++++");
                System.out.println("pickupStartTime " + pickupStartTime);
                System.out.println("pickupTimeWindow " + pickupTimeWindow);
                System.out.println("travelTime " + travelTime);
                System.out.println("deliveryStartTime " + deliveryStartTime);

                System.out.println("+++++++++++++++++++++++++++++++");
                System.out.println();

            }
            totalCount++;
            if (addedCount >= 12) {
                break;
            }

        }
        System.out.println(addedCount + " passengers added of the " + totalCount);
    }

    private void addPassengersDebug(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        if (!(getIoHandler().fileExists(ioHandler.getPositionedPassengersPath()))) {
            PassengerHandler pfm = new PassengerHandler(ioHandler);
            pfm.extractAndPositionPassengers();
        }
        List<SimulationObject> passengers = getIoHandler().readPositionedObjects(ioHandler.getPositionedPassengersPath());
        int totalCount = 0;
        int addedCount = 0;
        for (SimulationObject object : passengers) {
            addedCount++;
            Passenger passenger = (Passenger) object;
            long pickupStartTime = passenger.getStartTime(PASSENGER_START_TIME);
            long pickupTimeWindow = passenger.getStartTimeWindow(PASSENGER_START_TIME);
            Parcel.Builder parcelBuilder = Parcel.builder(passenger.getStartPoint(), passenger.getEndPoint())
                    .orderAnnounceTime(pickupStartTime)
                    .pickupTimeWindow(TimeWindow.create(pickupStartTime, pickupStartTime + pickupTimeWindow))
                    .pickupDuration(PICKUP_DURATION)
                    .deliveryDuration(DELIVERY_DURATION);
            if (ridesharing) {
                parcelBuilder = parcelBuilder
                        .neededCapacity(passenger.getAmount());
            } else {
                parcelBuilder = parcelBuilder
                        .neededCapacity(4);
            }
            builder.addEvent(
                    AddParcelEvent.create(parcelBuilder.buildDTO()));
            totalCount++;

            if (addedCount >= 5) {
                break;
            }

        }
        System.out.println(addedCount + " passengers added of the " + totalCount);
    }

    private long getDeliveryStartTime(Passenger passenger, RoutingTable routingTable) {
        long startTime = passenger.getStartTime(PASSENGER_START_TIME);
        long travelTime = (long) routingTable.getRoute(passenger.getStartPoint(), passenger.getEndPoint()).getTravelTime();
        return startTime + travelTime + PICKUP_DURATION;
    }

    private void addNYC(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        Area area = new NycArea();
        for (Point point : area.getPoints()) {
            builder.addEvent(AddDepotEvent.create(-1, point));
        }
    }

    private void addManhattan(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        Area area = new ManhattanArea();
        for (Point point : area.getPoints()) {
            builder.addEvent(AddDepotEvent.create(-1, point));
        }

    }

    private void addJFK(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        Area area = new JfkArea();
        for (Point point : area.getPoints()) {
            builder.addEvent(AddDepotEvent.create(-1, point));
        }

    }


}
