package com.github.christof.experiment;

import com.github.rinde.logistics.pdptw.mas.Truck;
import com.github.rinde.logistics.pdptw.mas.TruckFactory;
import com.github.rinde.logistics.pdptw.mas.comm.*;
import com.github.rinde.logistics.pdptw.mas.comm.RtSolverBidder.BidFunction;
import com.github.rinde.logistics.pdptw.mas.comm.RtSolverBidder.BidFunctions;
import com.github.rinde.logistics.pdptw.mas.route.RoutePlannerStatsLogger;
import com.github.rinde.logistics.pdptw.mas.route.RtSolverRoutePlanner;
import com.github.rinde.logistics.pdptw.solver.optaplanner.OptaplannerSolvers;
import com.github.rinde.rinsim.central.rt.RtCentral;
import com.github.rinde.rinsim.central.rt.RtSolverModel;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.time.RealtimeClockLogger;
import com.github.rinde.rinsim.core.model.time.RealtimeClockLogger.LogEntry;
import com.github.rinde.rinsim.core.model.time.RealtimeTickInfo;
import com.github.rinde.rinsim.experiment.CommandLineProgress;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.Experiment.SimArgs;
import com.github.rinde.rinsim.experiment.MASConfiguration;
import com.github.rinde.rinsim.experiment.PostProcessor;
import com.github.rinde.rinsim.geom.GeomHeuristics;
import com.github.rinde.rinsim.pdptw.common.*;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.TimeOutEvent;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06ObjectiveFunction;
import com.github.rinde.rinsim.scenario.measure.Metrics;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.eclipse.swt.graphics.RGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NycExperiment {
	final static long rpMs = 100L; //100
	final static long bMs = 20L; //20
    final static long maxAuctionDurationSoft = 20000L;  //10000L;
    final static long maxAuctionDurationHard = 30 * 60 * 1000L;
    final static long reactCooldownPeriodMs = 60*1000L;
	final static BidFunction bf = BidFunctions.BALANCED_HIGH;
	final static String masSolverName =
			"Step-counting-hill-climbing-with-entity-tabu-and-strategic-oscillation";
	final static ObjectiveFunction objFunc = Gendreau06ObjectiveFunction.instance(70);
	final static boolean enableReauctions = true;
	final static boolean computationsLogging = false;
	final static boolean ridesharing = false;
	static String attribute = "noRidesharing";
	static boolean debug = true;





	/**
	 * Usage: args = [ generate/experiment datasetID #buckets bucketID]
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		if (args.length > 0 && args[0].equals("false")) {
			debug = false;
		}
		if (debug) {
			System.out.println("++++++++++ DEBUGGING ++++++++++");
		}
        if (ridesharing) {
            attribute = "Ridesharing_noFilter";
        }
        performExperiment();
        System.out.println("THE END");
    }

	public static void performExperiment() throws Exception {
		System.out.println(System.getProperty("java.vm.name") + ", "
			      + System.getProperty("java.vm.vendor") + ", "
			      + System.getProperty("java.vm.version") + " (runtime version: "
			      + System.getProperty("java.runtime.version") + ")");
		System.out.println(System.getProperty("os.name") + " "
			      + System.getProperty("os.version") + " "
			      + System.getProperty("os.arch"));
		ScenarioGenerator sg = new ScenarioGenerator();
		sg.generateTaxiScenario(ridesharing, debug);
		
		List<Scenario> scenarios = new ArrayList<>();
		Scenario scenario = sg.getIoHandler().readScenario();
		scenarios.add(scenario);
		System.out.println("measureDynamism " + Metrics.measureDynamism(scenario));
		System.out.println("measureUrgency " + Metrics.measureUrgency(scenario));
		System.out.println("duration " + scenario.getTimeWindow().end());

//        if (!debug) {
//            sg.generateTaxiScenario(!ridesharing, debug);
//            scenarios.add(sg.getIoHandler().readScenario());
//        }
        System.out.println("There are " + scenarios.size() + " scenarios");

//
	    final OptaplannerSolvers.Builder opFfdFactory =
	    	      OptaplannerSolvers.builder()
	    	      .withSolverHeuristic(GeomHeuristics.time(70d))
	    	      .withSolverXmlResource(
	    	        "com/github/rinde/jaamas17/jaamas-solver.xml")
	    	      .withUnimprovedMsLimit(rpMs)
	    	      .withName(masSolverName)
	    	      .withObjectiveFunction(objFunc)
				;


		Experiment.Builder experimentBuilder = Experiment.builder();

		if (!debug) {
			experimentBuilder = experimentBuilder
//					.computeDistributed()
				 .computeLocal()
				.withRandomSeed(123)
				.withThreads((int) Math
						.floor((Runtime.getRuntime().availableProcessors() - 1) / 2d))
				.repeat(3)
					.withWarmup(30000);
		} else {
			experimentBuilder = experimentBuilder
					.withThreads(1)
					.showGui(View.builder()
							.with(RoadUserRenderer.builder()
//								.withToStringLabel()
									.withColorAssociation(Truck.class, new RGB(204, 0, 0))
									.withColorAssociation(Depot.class, new RGB(0, 0, 255)))
							.with(RouteRenderer.builder())
							.with(PDPModelRenderer.builder())
							.with(GraphRoadModelRenderer.builder()
//								.withDirectionArrows()
//								.withStaticRelativeSpeedVisualization()
//								.withDynamicRelativeSpeedVisualization()
							)
							.with(AuctionPanel.builder())
							.with(RoutePanel.builder())
                            .with(TimeLinePanel.builder())
//							.with(RtSolverPanel.builder())
                            .withResolution(12800, 10240)
                            .withAutoPlay()
							.withAutoClose());

		}


		experimentBuilder = experimentBuilder.addResultListener(new CommandLineProgress(System.out))
				.addResultListener(new LuytenResultWriter(
						new File("files/results/LUYTEN17"),
						(Gendreau06ObjectiveFunction) objFunc))
				.usePostProcessor(new LogProcessor(objFunc))
				.addConfigurations(mainConfigs(opFfdFactory, objFunc))
				.addScenarios(scenarios);
        experimentBuilder.perform();

	}

	static List<MASConfiguration> mainConfigs(
			OptaplannerSolvers.Builder opFfdFactory, ObjectiveFunction objFunc) {

		final List<MASConfiguration> configs = new ArrayList<>();
		configs.add(createMAS(opFfdFactory, objFunc, rpMs, bMs,
				maxAuctionDurationSoft, enableReauctions, reactCooldownPeriodMs, computationsLogging));
		
		final String solverKey =
				"Step-counting-hill-climbing-with-entity-tabu-and-strategic-oscillation";
        if (!debug) {
            final long centralUnimprovedMs = 10000L;
            configs.add(createCentral(
                    opFfdFactory.withSolverXmlResource(
                            "com/github/rinde/jaamas17/jaamas-solver.xml")
                            .withName("Central_" + attribute)
                            .withSolverHeuristic(GeomHeuristics.time(70d))
                            .withUnimprovedMsLimit(centralUnimprovedMs),
                    "Central_" + attribute));
        }
        return configs;
	}

	static MASConfiguration createMAS(OptaplannerSolvers.Builder opFfdFactory,
			ObjectiveFunction objFunc, long rpMs, long bMs,
			long maxAuctionDurationSoft, boolean enableReauctions,
			long reauctCooldownPeriodMs, boolean computationsLogging) {

		MASConfiguration.Builder b = MASConfiguration.pdptwBuilder()
				.setName("MAS_" + attribute)
				.addEventHandler(TimeOutEvent.class, TimeOutEvent.ignoreHandler())
				.addEventHandler(AddDepotEvent.class, AddDepotEvent.defaultHandler())
				.addEventHandler(AddParcelEvent.class, AddParcelEvent.defaultHandler())
				.addEventHandler(AddVehicleEvent.class,
						TruckFactory.DefaultTruckFactory.builder()
						.setRoutePlanner(RtSolverRoutePlanner.supplier(
								opFfdFactory.withSolverXmlResource(
										"com/github/rinde/jaamas17/jaamas-solver.xml")
								.withName(masSolverName)
								.withUnimprovedMsLimit(rpMs)
								.withTimeMeasurementsEnabled(computationsLogging)
                                .withSolverHeuristic(GeomHeuristics.time(70d))
                                .buildRealtimeSolverSupplier()))
						.setCommunicator(
								RtSolverBidder.realtimeBuilder(objFunc,
										opFfdFactory.withSolverXmlResource(
												"com/github/rinde/jaamas17/jaamas-solver.xml")
										.withName(masSolverName)
										.withUnimprovedMsLimit(bMs)
                                        .withSolverHeuristic(GeomHeuristics.time(70d))
                                        .withTimeMeasurementsEnabled(computationsLogging)
										.buildRealtimeSolverSupplier())
								.withBidFunction(bf)
								.withReauctionsEnabled(enableReauctions)
								.withReauctionCooldownPeriod(reauctCooldownPeriodMs))
						.setLazyComputation(false)
						.setRouteAdjuster(RouteFollowingVehicle.delayAdjuster())
						.build())
				.addModel(AuctionCommModel.builder(DoubleBid.class)
						.withStopCondition(
								AuctionStopConditions.and(
										AuctionStopConditions.<DoubleBid>atLeastNumBids(2),
										AuctionStopConditions.<DoubleBid>or(
												AuctionStopConditions.<DoubleBid>allBidders(),
												AuctionStopConditions
												.<DoubleBid>maxAuctionDuration(maxAuctionDurationSoft))))
						.withMaxAuctionDuration(maxAuctionDurationHard))
				.addModel(RealtimeClockLogger.builder())
				 ;
		if (debug) {
			b = b.addModel(RtSolverModel.builder());
		} else {
			b = b.addModel(RtSolverModel.builder()
					.withThreadPoolSize(3)
					.withThreadGrouping(true));
		}

		if (computationsLogging) {
			b = b.addModel(AuctionTimeStatsLogger.builder())
					.addModel(RoutePlannerStatsLogger.builder());
		}

		return b.build();
	}
	
	static void addCentral(Experiment.Builder experimentBuilder,
			OptaplannerSolvers.Builder opBuilder, String name) {
		experimentBuilder.addConfiguration(createCentral(opBuilder, name));
	}

	static MASConfiguration createCentral(OptaplannerSolvers.Builder opBuilder,
			String name) {
		MASConfiguration.Builder builder = MASConfiguration.pdptwBuilder()
				.addModel(RealtimeClockLogger.builder())
				.addEventHandler(TimeOutEvent.class, TimeOutEvent.ignoreHandler())
				.addEventHandler(AddDepotEvent.class, AddDepotEvent.defaultHandler())
				.addEventHandler(AddParcelEvent.class, AddParcelEvent.defaultHandler())
				.addEventHandler(AddVehicleEvent.class, RtCentral.vehicleHandler())
				.setName(name);

		if (debug) {
			builder = builder.addModel(RtCentral.builder(opBuilder.buildRealtimeSolverSupplier()));
		} else {
			builder = builder.addModel(RtCentral.builder(opBuilder.buildRealtimeSolverSupplier())
					.withContinuousUpdates(true)
					.withThreadGrouping(true));
		}

		return builder.build();
	}


	  @AutoValue
	  abstract static class AuctionStats implements Serializable{
          static AuctionStats create(int numP, int numR, int numUn, int numF) {
              return new AutoValue_NycExperiment_AuctionStats(numP, numR, numUn,
                      numF);
          }

	    abstract int getNumParcels();

	    abstract int getNumReauctions();

	    abstract int getNumUnsuccesfulReauctions();

	    abstract int getNumFailedReauctions();
	  }

	  @AutoValue
	  abstract static class ExperimentInfo implements Serializable {
	    private static final long serialVersionUID = 6324066851233398736L;

          static ExperimentInfo create(List<LogEntry> log, long rt, long st,
                                       StatisticsDTO stats, ImmutableList<RealtimeTickInfo> dev,
                                       Optional<AuctionStats> aStats) {
              return new AutoValue_NycExperiment_ExperimentInfo(log, rt, st, stats,
                      dev, aStats);
          }

	    abstract List<LogEntry> getLog();

	    abstract long getRtCount();

	    abstract long getStCount();

	    abstract StatisticsDTO getStats();

	    abstract ImmutableList<RealtimeTickInfo> getTickInfoList();

	    abstract Optional<AuctionStats> getAuctionStats();
	  }

	
	 static class LogProcessor
      implements PostProcessor<ExperimentInfo>, Serializable {
    private static final long serialVersionUID = 5997690791395717045L;
    ObjectiveFunction objectiveFunction;
    
	Logger LOGGER = LoggerFactory.getLogger("LogProcessor");

    LogProcessor(ObjectiveFunction objFunc) {
      objectiveFunction = objFunc;
    }

    @Override
    public ExperimentInfo collectResults(Simulator sim, SimArgs args) {

      @Nullable
      final RealtimeClockLogger logger =
        sim.getModelProvider().tryGetModel(RealtimeClockLogger.class);

      @Nullable
      final AuctionCommModel<?> auctionModel =
        sim.getModelProvider().tryGetModel(AuctionCommModel.class);

      final Optional<AuctionStats> aStats;
      if (auctionModel == null) {
        aStats = Optional.absent();
      } else {
        final int parcels = auctionModel.getNumParcels();
        final int reauctions = auctionModel.getNumAuctions() - parcels;
        final int unsuccessful = auctionModel.getNumUnsuccesfulAuctions();
        final int failed = auctionModel.getNumFailedAuctions();
        aStats = Optional
          .of(AuctionStats.create(parcels, reauctions, unsuccessful, failed));
      }

      final StatisticsDTO stats =
    	        sim.getModelProvider().getModel(StatsTracker.class).getStatistics();
//        PostProcessors.statisticsPostProcessor(objectiveFunction)
//          .collectResults(sim, args);

      LOGGER.info("success: {}", args);
      
      if(aStats.isPresent()) {
    	  System.out.println("Num Parcels: " + aStats.get().getNumParcels());
      	  System.out.println("Num Reauctions: " + aStats.get().getNumReauctions());
      	  System.out.println("Num Unsuccessful Reauctions: " + aStats.get().getNumUnsuccesfulReauctions());
      	  System.out.println("Num Failed Reauctions: " + aStats.get().getNumFailedReauctions());
      }
      
      System.out.println(stats.toString());
      
      if (logger == null) {
        return ExperimentInfo.create(new ArrayList<LogEntry>(), 0,
          sim.getCurrentTime() / sim.getTimeStep(), stats,
          ImmutableList.<RealtimeTickInfo>of(), aStats);
      }
      return ExperimentInfo.create(logger.getLog(), logger.getRtCount(),
        logger.getStCount(), stats, logger.getTickInfoList(), aStats);
    }

    @Override
    public FailureStrategy handleFailure(Exception e, Simulator sim,
        SimArgs args) {

      System.out.println("Fail: " + args);
      e.printStackTrace();
      // System.out.println(AffinityLock.dumpLocks());

      return FailureStrategy.RETRY;
      // return FailureStrategy.ABORT_EXPERIMENT_RUN;

    }
  }


}
