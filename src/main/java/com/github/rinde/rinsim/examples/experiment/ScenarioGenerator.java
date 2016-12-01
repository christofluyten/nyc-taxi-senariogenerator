package com.github.rinde.rinsim.examples.experiment;

import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.TimeWindowPolicy;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.pdptw.common.StatsStopConditions;
import com.github.rinde.rinsim.scenario.*;
import com.github.rinde.rinsim.util.TimeWindow;
import data.*;
import data.Taxi;
import fileMaker.IOHandler;
import fileMaker.PassengerFileMaker;
import fileMaker.TaxiFileMaker;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by christof on 23.11.16.
 */
public class ScenarioGenerator {
    private static final String PASSENGER_DATA_FILE = "/media/christof/Elements/Data/FOIL2013/trip_data_11_clean.csv";;
    private static final String PASSENGER_START_TIME = "2013-11-18 16";
    private static final String PASSENGER_END_TIME = "2013-11-18 17";
    private static final int PASSENGER_MAX_AMOUNT = 2000;

    private static final String TAXI_DATA_FILE = "/media/christof/Elements/Data/FOIL2013/trip_data_11_clean.csv";
    private static final String TAXI_START_TIME = "2013-11-18 15";
    private static final String TAXI_END_TIME = "2013-11-18 16";
    private static final int TAXI_MAX_AMOUNT = 500;
    private static final double MAX_VEHICLE_SPEED_KMH = 3d;


    private static final String ATTRIBUTE = "";
    private static final int CUT_LENGTH = 500;

    private static final long SCENARIO_DURATION = 10* 60 * 60 * 1000L;




    private static final long M1 = 60 * 1000L;
    private static final long M10 = 10 * 60 * 1000L;

    private IOHandler ioHandler;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ScenarioGenerator sg = new ScenarioGenerator();
        sg.generateTaxiScenario();
    }

    public ScenarioGenerator(){
        IOHandler ioHandler = new IOHandler();
        ioHandler.setPassengerDataFile(PASSENGER_DATA_FILE);
        ioHandler.setPassengerStartTime(PASSENGER_START_TIME);
        ioHandler.setPassengerEndTime(PASSENGER_END_TIME);
        ioHandler.setTaxiDataFile(TAXI_DATA_FILE);
        ioHandler.setTaxiStartTime(TAXI_START_TIME);
        ioHandler.setTaxiEndTime(TAXI_END_TIME);
        ioHandler.setAttribute(ATTRIBUTE);
        ioHandler.setCutLength(CUT_LENGTH);
        this.ioHandler = ioHandler;
        setScenarioFileFullName();
        makeMap();
    }


    public IOHandler getIoHandler() {
        return ioHandler;
    }


    public void setScenarioFileFullName() {
        getIoHandler().setScenarioFileFullName(getIoHandler().getScenarioFileName()+ getIoHandler().getAttribute()+"_"+getIoHandler().getPassengerStartTime()+":00:00-"
                +getIoHandler().getPassengerEndTime()+":00:00"+"_"+PASSENGER_MAX_AMOUNT+"_"+TAXI_MAX_AMOUNT);
    }

    private void makeMap() {
        try {
            getIoHandler().makeMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Scenario generateTaxiScenario() throws IOException, ClassNotFoundException {
        if(getIoHandler().fileExists(getIoHandler().getScenarioFileFullPath())) {
           return getIoHandler().readScenario();
        } else {
            Scenario.Builder builder = Scenario.builder();
            addTaxis(builder);
            addPassengers(builder);
            addGeneralProperties(builder);
            Scenario scenario = builder.build();
            getIoHandler().writeScenario(scenario);
            return scenario;
        }
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
            TaxiFileMaker tfm = new TaxiFileMaker(ioHandler);
            tfm.extractAndPositionTaxis();
        }
        List<SimulationObject> taxis = getIoHandler().readPositionedObjects(ioHandler.getPositionedTaxisPath());
        int count = TAXI_MAX_AMOUNT;
        for(int i=0; i<taxis.size();i++){
            Taxi taxi = (Taxi) taxis.get(i);
            builder.addEvent(AddVehicleEvent.create(taxi.getStartTime(TAXI_START_TIME), VehicleDTO.builder()
                    .speed(MAX_VEHICLE_SPEED_KMH)
                    .startPosition(taxi.getStartPoint())
                    .build()));
            count--;
            if (count == 0){
                break;
            }
        }
    }

    private void addPassengers(Scenario.Builder builder) throws IOException, ClassNotFoundException {
        if(!(getIoHandler().fileExists(ioHandler.getPositionedPassengersPath()))) {
            PassengerFileMaker pfm = new PassengerFileMaker(ioHandler);
            pfm.extractAndPositionPassengers();
        }
        List<SimulationObject> passengers = getIoHandler().readPositionedObjects(ioHandler.getPositionedPassengersPath());
        int count = PASSENGER_MAX_AMOUNT;
        for(int i=0; i<passengers.size();i++){
            Passenger passenger = (Passenger) passengers.get(i);
            builder.addEvent(
                    AddParcelEvent.create(Parcel.builder(passenger.getStartPoint(), passenger.getEndPoint())
                            .neededCapacity(0)
                            .orderAnnounceTime(passenger.getStartTime(PASSENGER_START_TIME))
                            .pickupTimeWindow(TimeWindow.create(passenger.getStartTime(PASSENGER_START_TIME), M10 + passenger.getStartTime(PASSENGER_START_TIME)))
                            .deliveryTimeWindow(
                                    TimeWindow.create(M1+M10+passenger.getStartTime(PASSENGER_START_TIME), 2*M10+passenger.getStartTime(PASSENGER_START_TIME)))
                            .buildDTO()));
            count--;
            if (count == 0){
                break;
            }
        }
    }



}
