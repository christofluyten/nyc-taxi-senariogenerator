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
    private static final String TAXI_DATA_DIRECTORY = " ";  //path to director with the FOIL-directories
    private static final String TRAVEL_TIMES_DIRECTORY = " "; //path to director with the travel_times
    private static final Date PASSENGER_START_TIME = new Date("2013-11-18 16:00:00");                   //format: "yyyy-mm-dd HH:mm:ss"
    private static final Date PASSENGER_END_TIME = new Date("2013-11-18 17:00:00");

    private static final Date TAXI_START_TIME = new Date("2013-11-18 16:00:00");
    private static final Date TAXI_END_TIME = new Date("2013-11-18 17:00:00");
    private static final double MAX_VEHICLE_SPEED_KMH = 120d;

    private static final long PICKUP_DURATION = 30 * 1000L;
    private static final long DELIVERY_DURATION = 30 * 1000L;


    private static final String ATTRIBUTE = "TimeWindow";
    private static final int CUT_LENGTH = 500;                                                  //maximum length in meters of a edge in the graph (or "link" in the "map")

    private static final long SCENARIO_DURATION = 4 * 60 * 60 * 1000L;


    private static final boolean TRAFFIC = true;


    private static final long TICK_SIZE = 250L;


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

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ScenarioGenerator sg = new ScenarioGenerator();
        sg.generateTaxiScenario();
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

    public Scenario generateTaxiScenario() throws IOException, ClassNotFoundException {
//        if(getIoHandler().fileExists(getIoHandler().getScenarioFileFullPath())) {
//           return getIoHandler().readScenario();
//        } else {
        Scenario.Builder builder = Scenario.builder();
        addGeneralProperties(builder);
        addTaxis(builder);
        addPassengers(builder);
//            addJFK(builder);
//            addManhattan(builder);
//            addNYC(builder);
        Scenario scenario = builder.build();
        getIoHandler().writeScenario(scenario);
        return scenario;
//        }
    }


    private void addGeneralProperties(Scenario.Builder builder) throws IOException {
        builder
                .addEvent(TimeOutEvent.create(SCENARIO_DURATION))
                .scenarioLength(SCENARIO_DURATION)
//                .addModel(PDPRoadModel.builder(RoadModelBuilders.staticGraph(DotGraphIO.getLengthDataGraphSupplier(Paths.get(getIoHandler().getMapFilePath())))
//                .withDistanceUnit(SI.METER)
//                .withSpeedUnit(NonSI.KILOMETERS_PER_HOUR)))
//                .addModel(PDPRoadModel.builder(RoadModelBuilders.staticGraph(ListenableGraph.supplier(
//                                        (Supplier<? extends Graph<MultiAttributeData>>) DotGraphIO.getLengthDataGraphSupplier(Paths.get(getIoHandler().getMapFilePath()))))
//                        .withDistanceUnit(SI.METER)
//                        .withSpeedUnit(NonSI.KILOMETERS_PER_HOUR)))
                .addModel(
                        PDPDynamicGraphRoadModel.builderForDynamicGraphRm(
                                RoadModelBuilders
                                        .dynamicGraph(ListenableGraph.supplier(
                                                DotGraphIO.getMultiAttributeDataGraphSupplier(Paths.get(getIoHandler().getMapFilePath()))))
                                        .withSpeedUnit(NonSI.KILOMETERS_PER_HOUR)
                                        .withDistanceUnit(SI.KILOMETER))
                                .withAllowVehicleDiversion(true))
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
        int count = 0;
        for (SimulationObject object : taxis) {
            Taxi taxi = (Taxi) object;
//            builder.addEvent(AddVehicleEvent.create(taxi.getStartTime(TAXI_START_TIME), VehicleDTO.builder()
            builder.addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder()
                    .speed(MAX_VEHICLE_SPEED_KMH)
                    .startPosition(taxi.getStartPoint())
                    .capacity(4)
                    .build()));
            count++;
            if (count > 200) {
                break;
            }
        }
    }

    private void addPassengers(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        if (!(getIoHandler().fileExists(ioHandler.getPositionedPassengersPath()))) {
            PassengerHandler pfm = new PassengerHandler(ioHandler);
            pfm.extractAndPositionPassengers();
        }
        List<SimulationObject> passengers = getIoHandler().readPositionedObjects(ioHandler.getPositionedPassengersPath());
        int count = 0;
        for (SimulationObject object : passengers) {
            Passenger passenger = (Passenger) object;
            if (true) {
                builder.addEvent(
                        AddParcelEvent.create(Parcel.builder(passenger.getStartPoint(), passenger.getEndPoint())
                                .neededCapacity(passenger.getAmount())
                                .orderAnnounceTime(passenger.getStartTime(PASSENGER_START_TIME))
                                .pickupTimeWindow(TimeWindow.create(passenger.getStartTime(PASSENGER_START_TIME), passenger.getStartTimeWindow(PASSENGER_START_TIME)))
                                .pickupDuration(PICKUP_DURATION)
                                .deliveryDuration(DELIVERY_DURATION)
                                .buildDTO()));
            }
            count++;
            if (count > 19) {
                break;
            }

        }
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