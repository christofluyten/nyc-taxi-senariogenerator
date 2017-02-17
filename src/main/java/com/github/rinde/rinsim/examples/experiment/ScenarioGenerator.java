package com.github.rinde.rinsim.examples.experiment;

import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.TimeWindowPolicy;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TimeModel;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.pdptw.common.StatsStopConditions;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.TimeOutEvent;
import com.github.rinde.rinsim.util.TimeWindow;
import data.Date;
import data.Passenger;
import data.SimulationObject;
import data.Taxi;
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
    private static final String TAXI_DATA_DIRECTORY = "";    //path to director with the FOIL-directories
    private static final String TRAVEL_TIMES_DIRECTORY = ""; //path to director with the travel_times
    private static final Date PASSENGER_START_TIME = new Date("2013-11-18 16:00:00");                   //format: "yyyy-mm-dd HH:mm:ss"
    private static final Date PASSENGER_END_TIME = new Date("2013-11-18 17:00:00");
    private static final int PASSENGER_MAX_AMOUNT = -1;                                         //a negative number means that there is no limit

    private static final Date TAXI_START_TIME = new Date("2013-11-18 16:00:00");
    private static final Date TAXI_END_TIME = new Date("2013-11-18 17:00:00");
    private static final int TAXI_MAX_AMOUNT = -1;
    private static final double MAX_VEHICLE_SPEED_KMH = 50d;


    private static final String ATTRIBUTE = "Testing";
    private static final int CUT_LENGTH = 500;                                                  //maximum length in meters of a edge in the graph (or "link" in the "map")

    private static final long SCENARIO_DURATION = 10* 60 * 60 * 1000L;


    private static final boolean TRAFFIC = true;


    private IOHandler ioHandler;

    ScenarioGenerator() {
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
                + getIoHandler().getPassengerEndTime().getShortStringDateForPath() + "_" + PASSENGER_MAX_AMOUNT + "_" + TAXI_MAX_AMOUNT);
    }

    private void makeMap() {
        try {
            getIoHandler().makeMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Scenario generateTaxiScenario() throws IOException, ClassNotFoundException {
//        if(getIoHandler().fileExists(getIoHandler().getScenarioFileFullPath())) {
//           return getIoHandler().readScenario();
//        } else {
        Scenario.Builder builder = Scenario.builder();
        addTaxis(builder);
        addPassengers(builder);
        addGeneralProperties(builder);
        Scenario scenario = builder.build();
        getIoHandler().writeScenario(scenario);
        return scenario;
//        }
    }


    private void addGeneralProperties(Scenario.Builder builder) throws IOException {
        builder
                // Signals the end of the scenario. Note that it is possible to stop the
                // simulation before or after this event is dispatched, that depends on
                // the stop condition (see below).
                .addEvent(TimeOutEvent.create(SCENARIO_DURATION))
                .scenarioLength(SCENARIO_DURATION)

                // Adds a plane road model as this is part of the problem
                .addModel(RoadModelBuilders.staticGraph(DotGraphIO.getLengthDataGraphSupplier(Paths.get(getIoHandler().getMapFilePath())))
                        .withDistanceUnit(SI.METER)
                        .withSpeedUnit(NonSI.KILOMETERS_PER_HOUR)
                )
                .addModel(TimeModel.builder().withRealTime())

                // Adds the pdp model
                .addModel(
                        DefaultPDPModel.builder()
                                .withTimeWindowPolicy(TimeWindowPolicy.TimeWindowPolicies.LIBERAL))

                // The stop condition indicates when the simulator should stop the
                // simulation. Typically this is the moment when all tasks are performed.
                // Custom stop conditions can be created by implementing the StopCondition
                // interface.
                .setStopCondition(StatsStopConditions.timeOutEvent());
    }

    private void addTaxis(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        if(!(getIoHandler().fileExists(ioHandler.getPositionedTaxisPath()))) {
            TaxiHandler tfm = new TaxiHandler(ioHandler);
            tfm.extractAndPositionTaxis();
        }
        List<SimulationObject> taxis = getIoHandler().readPositionedObjects(ioHandler.getPositionedTaxisPath());
        int count = TAXI_MAX_AMOUNT;
        for (SimulationObject object : taxis) {
            Taxi taxi = (Taxi) object;
            builder.addEvent(AddVehicleEvent.create(taxi.getStartTime(TAXI_START_TIME), VehicleDTO.builder()
                    .speed(MAX_VEHICLE_SPEED_KMH)
                    .startPosition(taxi.getStartPoint())
                    .build()));
            count--;
            if (count == 0) {
                break;
            }
        }
    }

    private void addPassengers(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        if(!(getIoHandler().fileExists(ioHandler.getPositionedPassengersPath()))) {
            PassengerHandler pfm = new PassengerHandler(ioHandler);
            pfm.extractAndPositionPassengers();
        }
        List<SimulationObject> passengers = getIoHandler().readPositionedObjects(ioHandler.getPositionedPassengersPath());
        int count = PASSENGER_MAX_AMOUNT;
        for (SimulationObject object : passengers) {
            Passenger passenger = (Passenger) object;
            builder.addEvent(
                    AddParcelEvent.create(Parcel.builder(passenger.getStartPoint(), passenger.getEndPoint())
                            .neededCapacity(0)
                            .orderAnnounceTime(passenger.getStartTime(PASSENGER_START_TIME))
                            .pickupTimeWindow(TimeWindow.create(passenger.getStartTime(PASSENGER_START_TIME), passenger.getStartTimeWindow(PASSENGER_START_TIME)))
//                                    .deliveryTimeWindow(
//                                            TimeWindow.create(M1 + M10 + passenger.getStartTime(PASSENGER_START_TIME), 2 * M10 + passenger.getStartTime(PASSENGER_START_TIME)))
                            .buildDTO()));
            count--;
            if (count == 0) {
                break;
            }
        }
    }

}
