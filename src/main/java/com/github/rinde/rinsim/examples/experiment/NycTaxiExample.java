package com.github.rinde.rinsim.examples.experiment;

import com.github.christof.experiment.ScenarioGenerator;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.ExperimentResults;
import com.github.rinde.rinsim.experiment.MASConfiguration;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.AddDepotEvent;
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.scenario.TimeOutEvent;
import com.github.rinde.rinsim.scenario.TimedEventHandler;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.google.common.base.Optional;
import org.eclipse.swt.graphics.RGB;

/**
 * Created by christof on 29.11.16.
 */
public final class NycTaxiExample {
    private static final Point RESOLUTION = new Point(800, 700);

    private NycTaxiExample() {
    }

    /**
     * It is possible to use the application arguments directly for configuring
     * the experiment. The '-h' or '--help' argument will show the list of
     * options.
     *
     * @param args The arguments supplied to the application.
     */
    public static void main(String[] args) throws Exception {
        ScenarioGenerator sg = new ScenarioGenerator();
        sg.generateTaxiScenario();


        final Optional<ExperimentResults> results;


        results = Experiment.builder()
//                    .addConfiguration(createOptaPlanner(enableTimeMeasurements))
//                    .computeLocal()
//                    .withThreads((int) Math
//                            .floor((Runtime.getRuntime().availableProcessors() - 1) / 2d))
//                    .withWarmup(30000)
//                    .addResultListener(new CommandLineProgress(System.out))
//                    .usePostProcessor(new JaamasPostProcessor(objFunc)

                // Adds a configuration to the experiment. A configuration configures an
                // algorithm that is supposed to handle or 'solve' a problem specified by
                // a scenario. A configuration can handle a scenario if it contains an
                // event handler for all events that occur in the scenario. The scenario
                // in this example contains four different events and registers an event
                // handler for each of them.
                .addConfiguration(MASConfiguration.builder()
                        .addEventHandler(AddDepotEvent.class, AddDepotEvent.defaultHandler())
                        .addEventHandler(AddParcelEvent.class, AddParcelEvent.defaultHandler())
                        // There is no default handle for vehicle events, here a non functioning
                        // handler is added, it can be changed to add a custom vehicle to the
                        // simulator.
                        .addEventHandler(AddVehicleEvent.class, CustomVehicleHandler.INSTANCE)
                        .addEventHandler(TimeOutEvent.class, TimeOutEvent.ignoreHandler())

                        // Note: if you multi-agent system requires the aid of a model (e.g.
                        // CommModel) it can be added directly in the configuration. Models that
                        // are only used for the solution side should not be added in the
                        // scenario as they are not part of the problem.
                        .build())

                // Adds the newly constructed scenario to the experiment. Every
                // configuration will be run on every scenario.
//                .addScenario(createScenario())
                .addScenario(sg.getIoHandler().readScenario())
//                .addScenario(ScenarioIO.read(Paths.get("src/main/resources/scenarios/taxi_scenario")))

                // The number of repetitions for each simulation. Each repetition will
                // have a unique random seed that is given to the simulator.
                .repeat(1)

                // The master random seed from which all random seeds for the
                // simulations will be drawn.
                .withRandomSeed(123)

                // The number of threads the experiment will use, this allows to run
                // several simulations in parallel. Note that when the GUI is used the
                // number of threads must be set to 1.
                .withThreads(1)

                // We add a post processor to the experiment. A post processor can read
                // the state of the simulator after it has finished. It can be used to
                // gather simulation results. The objects created by the post processor
                // end up in the ExperimentResults object that is returned by the
                // perform(..) method of Experiment.
                .usePostProcessor(new ExamplePostProcessor())

                // Adds the GUI just like it is added to a Simulator object.
                .showGui(View.builder()
                        .with(GraphRoadModelRenderer.builder())
//                        .with(PDPModelRenderer.builder())
                        .with(RoadUserRenderer.builder()
                                .withColorAssociation(Taxi.class, new RGB(0, 204, 0))
                                .withColorAssociation(Parcel.class, new RGB(255, 0, 0))
                                .withColorAssociation(Depot.class, new RGB(0, 0, 255)))
//                                .withImageAssociation(
//                                    Depot.class, "/graphics/perspective/tall-building-64.png")
//                                .withImageAssociation(
//                                        Taxi.class, "/graphics/flat/taxi-16.png")
//                                .withImageAssociation(
//                                        Parcel.class, "/graphics/flat/person-red-16.png"))
//                            .with(TimeLinePanel.builder())

                        .withResolution((int) RESOLUTION.x, (int) RESOLUTION.y)
                        .withAutoPlay()
                        .withAutoClose()
                        // For testing we allow to change the speed up via the args.
//                            .withSpeedUp(uiSpeedUp)
                        .withTitleAppendix("NYC taxi example"))

                // Starts the experiment, but first reads the command-line arguments
                // that are specified for this application. By supplying the '-h' option
                // you can see an overview of the supported options.
                .perform(System.out);

//            // Starts the experiment builder.
//            results = Experiment.builder()
//
//                    // Adds a configuration to the experiment. A configuration configures an
//                    // algorithm that is supposed to handle or 'solve' a problem specified by
//                    // a scenario. A configuration can handle a scenario if it contains an
//                    // event handler for all events that occur in the scenario. The scenario
//                    // in this example contains four different events and registers an event
//                    // handler for each of them.
//                    .addConfiguration(MASConfiguration.builder()
//                            .addEventHandler(AddDepotEvent.class, AddDepotEvent.defaultHandler())
//                            .addEventHandler(AddParcelEvent.class, AddParcelEvent.defaultHandler())
//                            // There is no default handle for vehicle events, here a non functioning
//                            // handler is added, it can be changed to add a custom vehicle to the
//                            // simulator.
//                            .addEventHandler(AddVehicleEvent.class, CustomVehicleHandler.INSTANCE)
//                            .addEventHandler(TimeOutEvent.class, TimeOutEvent.ignoreHandler())
//
//                            // Note: if you multi-agent system requires the aid of a model (e.g.
//                            // CommModel) it can be added directly in the configuration. Models that
//                            // are only used for the solution side should not be added in the
//                            // scenario as they are not part of the problem.
//                            .build())
//
//                    // Adds the newly constructed scenario to the experiment. Every
//                    // configuration will be run on every scenario.
////                .addScenario(createScenario())
//                    .addScenario(sg.getIoHandler().readScenario())
////                .addScenario(ScenarioIO.read(Paths.get("src/main/resources/scenarios/taxi_scenario")))
//
//                    // The number of repetitions for each simulation. Each repetition will
//                    // have a unique random seed that is given to the simulator.
//                    .repeat(1)
//
//                    // The master random seed from which all random seeds for the
//                    // simulations will be drawn.
//                    .withRandomSeed(0)
//
//                    // The number of threads the experiment will use, this allows to run
//                    // several simulations in parallel. Note that when the GUI is used the
//                    // number of threads must be set to 1.
//                    .withThreads(1)
//
//                    // We add a post processor to the experiment. A post processor can read
//                    // the state of the simulator after it has finished. It can be used to
//                    // gather simulation results. The objects created by the post processor
//                    // end up in the ExperimentResults object that is returned by the
//                    // perform(..) method of Experiment.
//                    .usePostProcessor(new ExamplePostProcessor())
//
//                    // Adds the GUI just like it is added to a Simulator object.
//                    .showGui(View.builder()
//                            .with(GraphRoadModelRenderer.builder())
////                        .with(PDPModelRenderer.builder())
//                            .with(RoadUserRenderer.builder()
//                                    .withColorAssociation(Taxi.class,new RGB(0,204,0))
//                                    .withColorAssociation(Parcel.class,new RGB(255,0,0))
//                                    .withColorAssociation(Depot.class,new RGB(0,0,255)))
////                                .withImageAssociation(
////                                    Depot.class, "/graphics/perspective/tall-building-64.png")
////                                .withImageAssociation(
////                                        Taxi.class, "/graphics/flat/taxi-16.png")
////                                .withImageAssociation(
////                                        Parcel.class, "/graphics/flat/person-red-16.png"))
//                            .with(TimeLinePanel.builder())
//
//                            .withResolution((int) RESOLUTION.x, (int) RESOLUTION.y)
//                            .withAutoPlay()
//                            .withAutoClose()
//                            // For testing we allow to change the speed up via the args.
////                            .withSpeedUp(uiSpeedUp)
//                            .withTitleAppendix("NYC taxi example"))
//
//                    // Starts the experiment, but first reads the command-line arguments
//                    // that are specified for this application. By supplying the '-h' option
//                    // you can see an overview of the supported options.
//                    .perform(System.out, arguments);

        if (results.isPresent()) {

            for (final Experiment.SimulationResult sr : results.get().getResults()) {
                // The SimulationResult contains all information about a specific
                // simulation, the result object is the object created by the post
                // processor, a String in this case.
                System.out.println(
                        sr.getSimArgs().getRandomSeed() + " " + sr.getResultObject());
            }
        } else {
            throw new IllegalStateException("Experiment did not complete.");
        }
    }


    enum CustomVehicleHandler implements TimedEventHandler<AddVehicleEvent> {
        INSTANCE {
            @Override
            public void handleTimedEvent(AddVehicleEvent event, SimulatorAPI sim) {
                sim.register(new Taxi(event.getVehicleDTO().getStartPosition(), event.getVehicleDTO().getCapacity(), event.getVehicleDTO().getSpeed()));
            }
        }
    }


}
