///*
// * Copyright (C) 2011-2017 Rinde van Lon, imec-DistriNet, KU Leuven
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *         http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.github.rinde.rinsim.pdptw.common;
//
//import com.github.rinde.rinsim.core.model.DependencyProvider;
//import com.github.rinde.rinsim.core.model.Model.AbstractModelVoid;
//import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
//import com.github.rinde.rinsim.core.model.pdp.PDPModel;
//import com.github.rinde.rinsim.core.model.pdp.PDPModel.PDPModelEventType;
//import com.github.rinde.rinsim.core.model.pdp.PDPModelEvent;
//import com.github.rinde.rinsim.core.model.pdp.Parcel;
//import com.github.rinde.rinsim.core.model.pdp.Vehicle;
//import com.github.rinde.rinsim.core.model.road.GenericRoadModel.RoadEventType;
//import com.github.rinde.rinsim.core.model.road.MoveEvent;
//import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
//import com.github.rinde.rinsim.core.model.road.RoadModel;
//import com.github.rinde.rinsim.core.model.road.RoadPath;
//import com.github.rinde.rinsim.core.model.time.Clock;
//import com.github.rinde.rinsim.core.model.time.Clock.ClockEventType;
//import com.github.rinde.rinsim.event.Event;
//import com.github.rinde.rinsim.event.EventAPI;
//import com.github.rinde.rinsim.event.EventDispatcher;
//import com.github.rinde.rinsim.event.Listener;
//import com.github.rinde.rinsim.geom.Graphs;
//import com.github.rinde.rinsim.geom.Point;
//import com.github.rinde.rinsim.geom.RoutingTableSupplier;
//import com.github.rinde.rinsim.scenario.ScenarioController;
//import com.github.rinde.rinsim.scenario.ScenarioController.ScenarioEvent;
//import com.github.rinde.rinsim.scenario.TimeOutEvent;
//import com.google.auto.value.AutoValue;
//import data.RoutingTable;
//
//import javax.measure.unit.NonSI;
//import javax.measure.unit.SI;
//import java.util.*;
//
//import static com.github.rinde.rinsim.core.model.pdp.PDPModel.PDPModelEventType.*;
//import static com.github.rinde.rinsim.core.model.road.GenericRoadModel.RoadEventType.MOVE;
//import static com.github.rinde.rinsim.core.model.time.Clock.ClockEventType.STARTED;
//import static com.github.rinde.rinsim.core.model.time.Clock.ClockEventType.STOPPED;
//import static com.github.rinde.rinsim.scenario.ScenarioController.EventType.*;
//import static com.google.common.base.Verify.verify;
//import static com.google.common.collect.Maps.newLinkedHashMap;
//
///**
// * This class tracks statistics in a simulation.
// *
// * <p>
// * <b>Model properties</b>
// * <ul>
// * <li><i>Provides:</i> {@link StatisticsProvider}.</li>
// * <li><i>Dependencies:</i> {@link ScenarioController}, {@link Clock},
// * {@link RoadModel}, {@link PDPModel}.</li>
// * </ul>
// * @author Rinde van Lon
// */
//public final class StatsTracker extends AbstractModelVoid implements
//    StatisticsProvider {
//  final EventDispatcher eventDispatcher;
//  final TheListener theListener;
//  final Clock clock;
//  final RoadModel roadModel;
//  private Map<Parcel, Long> parcelToPickupTimeMap = new HashMap<>();
//  private Map<Vehicle, Long> vehicleToTimeMap = new HashMap<>();
//  private Map<Vehicle, Double> vehicleToDistanceMap = new HashMap<>();
//
//
//  private List<Vehicle> vehicleList = new ArrayList<>();
//
//
//  enum StatisticsEventType {
//    PICKUP_TARDINESS, DELIVERY_TARDINESS, ALL_VEHICLES_AT_DEPOT;
//  }
//
//  StatsTracker(ScenarioController scenContr, Clock c, RoadModel rm,
//               PDPModel pm) {
//    clock = c;
//    roadModel = rm;
//
//    eventDispatcher = new EventDispatcher(StatisticsEventType.values());
//    theListener = new TheListener();
//    scenContr.getEventAPI().addListener(theListener, SCENARIO_STARTED,
//      SCENARIO_FINISHED, SCENARIO_EVENT);
//
//    roadModel.getEventAPI().addListener(theListener, MOVE);
//    clock.getEventAPI().addListener(theListener, STARTED, STOPPED);
//
//    pm.getEventAPI()
//      .addListener(theListener, START_PICKUP, END_PICKUP, START_DELIVERY,
//        END_DELIVERY, NEW_PARCEL, NEW_VEHICLE);
//  }
//
//  EventAPI getEventAPI() {
//    return eventDispatcher.getPublicEventAPI();
//  }
//
//  /**
//   * @return A {@link StatisticsDTO} with the current simulation stats.
//   */
//  @Override
//  public StatisticsDTO getStatistics() {
//    final int vehicleBack = theListener.lastArrivalTimeAtDepot.size();
//    long overTime = 0;
//    if (theListener.simFinish) {
//      for (final Long time : theListener.lastArrivalTimeAtDepot.values()) {
//        if (time - theListener.scenarioEndTime > 0) {
//          overTime += time - theListener.scenarioEndTime;
//        }
//      }
//    }
//
//    long compTime = theListener.computationTime;
//    if (compTime == 0) {
//      compTime = System.currentTimeMillis() - theListener.startTimeReal;
//    }
//
//    return new StatisticsDTO(theListener.totalDistance, theListener.totalTime,
//      theListener.totalPickups, theListener.totalDeliveries,
//      theListener.totalParcels, theListener.acceptedParcels,
//      theListener.pickupTardiness, theListener.deliveryTardiness, compTime,
//      clock.getCurrentTime(), theListener.simFinish, vehicleBack,
//      overTime, theListener.totalVehicles, theListener.distanceMap.size(),
//      clock.getTimeUnit(), roadModel.getDistanceUnit(),
//      roadModel.getSpeedUnit());
//  }
//
//  @Override
//  public <U> U get(Class<U> clazz) {
//    return clazz.cast(this);
//  }
//
//  /**
//   * @return A new {@link Builder} instance.
//   */
//  public static Builder builder() {
//    return new AutoValue_StatsTracker_Builder();
//  }
//
//  class TheListener implements Listener {
//
//    private static final double MOVE_THRESHOLD = 0.0001;
//    // parcels
//    protected int totalParcels;
//    protected int acceptedParcels;
//
//    // vehicles
//    protected int totalVehicles;
//    protected final Map<MovingRoadUser, Double> distanceMap;
//    protected double totalDistance;
//    protected long totalTime;
//    protected final Map<MovingRoadUser, Long> lastArrivalTimeAtDepot;
//
//    protected int totalPickups;
//    protected int totalDeliveries;
//    protected long pickupTardiness;
//    protected long deliveryTardiness;
//
//    // simulation
//    protected long startTimeReal;
//    protected long startTimeSim;
//    protected long computationTime;
//    protected long simulationTime;
//
//    protected boolean simFinish;
//    protected long scenarioEndTime;
//
//    TheListener() {
//      totalParcels = 0;
//      acceptedParcels = 0;
//
//      totalVehicles = 0;
//      distanceMap = newLinkedHashMap();
//      totalDistance = 0d;
//      totalTime = 0L;
//      lastArrivalTimeAtDepot = newLinkedHashMap();
//
//      totalPickups = 0;
//      totalDeliveries = 0;
//      pickupTardiness = 0;
//      deliveryTardiness = 0;
//
//      simFinish = false;
//    }
//
//    @Override
//    public void handleEvent(Event e) {
//      if (e.getEventType() == ClockEventType.STARTED) {
//        startTimeReal = System.currentTimeMillis();
//        startTimeSim = clock.getCurrentTime();
//        computationTime = 0;
//
//      } else if (e.getEventType() == ClockEventType.STOPPED) {
//        computationTime = System.currentTimeMillis() - startTimeReal;
//        simulationTime = clock.getCurrentTime() - startTimeSim;
//      } else if (e.getEventType() == RoadEventType.MOVE) {
//        verify(e instanceof MoveEvent);
//        final MoveEvent me = (MoveEvent) e;
//        increment((MovingRoadUser) me.roadUser, me.pathProgress.distance()
//          .getValue()
//          .doubleValue());
//        totalDistance += me.pathProgress.distance().getValue().doubleValue();
//        totalTime += me.pathProgress.time().getValue();
//        Vehicle vehicle = (Vehicle) me.roadUser;
//        if(vehicleToDistanceMap.containsKey(vehicle)){
//          double time = vehicleToTimeMap.get(vehicle);
////          System.out.println("time "+time +" extra time "+me.pathProgress.time().doubleValue(SI.SECOND));
//
//          double distance = vehicleToDistanceMap.get(vehicle);
//          long currentTime = clock.getCurrentTime();
//          long oldTime = vehicleToTimeMap.get(vehicle);
//          if(currentTime-oldTime != 250){
//            System.out.println("oldTime "+oldTime+" currentTime "+currentTime+" diff "+(currentTime-oldTime));
//          }
//
//          vehicleToTimeMap.put(vehicle,currentTime);
//          vehicleToDistanceMap.put(vehicle,distance+me.pathProgress.distance().doubleValue(SI.KILOMETER));
//        }
////        System.out.println("vehicle "+vehicle.toString()+" "+me.pathProgress.distance().longValue(SI.KILOMETER)+" "+me.pathProgress.travelledNodes()+" "+me.pathProgress.time());
//        // if we are closer than 10 cm to the depot, we say we are 'at'
//        // the depot
//        if (Point.distance(me.roadModel.getPosition(me.roadUser),
//          ((Vehicle) me.roadUser).getStartPosition()) < MOVE_THRESHOLD) {
//          // only override time if the vehicle did actually move
//          if (me.pathProgress.distance().getValue()
//            .doubleValue() > MOVE_THRESHOLD) {
//            lastArrivalTimeAtDepot.put((MovingRoadUser) me.roadUser,
//              clock.getCurrentTime());
//            if (totalVehicles == lastArrivalTimeAtDepot.size()) {
//              eventDispatcher.dispatchEvent(new Event(
//                StatisticsEventType.ALL_VEHICLES_AT_DEPOT, this));
//            }
//          }
//        } else {
//          lastArrivalTimeAtDepot.remove(me.roadUser);
//        }
//
//      } else if (e.getEventType() == PDPModelEventType.START_PICKUP) {
//        verify(e instanceof PDPModelEvent);
//        final PDPModelEvent pme = (PDPModelEvent) e;
//        final Parcel p = pme.parcel;
//        final Vehicle v = pme.vehicle;
//        assert p != null;
//        assert v != null;
//        if(parcelToPickupTimeMap.containsKey(p)){
//          System.out.println("Error: parcel "+p.toString()+" is already picked up");
//        }
//
//
//        parcelToPickupTimeMap.put(p,pme.time);
//        vehicleToTimeMap.put(v,clock.getCurrentTime());
//        vehicleToDistanceMap.put(v,0D);
//
//
//        final long latestBeginTime = p.getPickupTimeWindow().end()
//          - p.getPickupDuration();
//        if (pme.time > latestBeginTime) {
//          final long tardiness = pme.time - latestBeginTime;
//          pickupTardiness += tardiness;
//          eventDispatcher.dispatchEvent(new StatisticsEvent(
//            StatisticsEventType.PICKUP_TARDINESS, this, p, v, tardiness,
//            pme.time));
//        }
//      } else if (e.getEventType() == PDPModelEventType.END_PICKUP) {
//        totalPickups++;
//      } else if (e.getEventType() == PDPModelEventType.START_DELIVERY) {
//        final PDPModelEvent pme = (PDPModelEvent) e;
//
//        final Parcel p = pme.parcel;
//        final Vehicle v = pme.vehicle;
//        assert p != null;
//        assert v != null;
//        long pickupTime = parcelToPickupTimeMap.get(p);
//
//        System.out.println("real traveltime "+(pme.time-pickupTime));
//        RoutingTable routingTable = RoutingTableSupplier.getRoutingTable();
//        RoadPath roadPath = routingTable.getPathTo(pme.parcel.getPickupLocation(),pme.parcel.getDeliveryLocation());
//        System.out.println("table traveltime "+roadPath.getTravelTime());
//        System.out.println("diff " +((pme.time-pickupTime)-roadPath.getTravelTime()));
//        System.out.println("vehicle distance "+vehicleToDistanceMap.get(v));
//        System.out.println("vehicle time "+vehicleToTimeMap.get(v));
//        System.out.println(p.getPickupLocation() +" "+p.getDeliveryLocation());
//
//        System.out.println();
//        vehicleToTimeMap.remove(v);
//        vehicleToDistanceMap.remove(v);
//
//
//        final long latestBeginTime = p.getDeliveryTimeWindow().end()
//          - p.getDeliveryDuration();
//        if (pme.time > latestBeginTime) {
//          final long tardiness = pme.time - latestBeginTime;
//          deliveryTardiness += tardiness;
//          eventDispatcher.dispatchEvent(new StatisticsEvent(
//            StatisticsEventType.DELIVERY_TARDINESS, this, p, v, tardiness,
//            pme.time));
//        }
//      } else if (e.getEventType() == PDPModelEventType.END_DELIVERY) {
//        totalDeliveries++;
//      } else if (e.getEventType() == SCENARIO_EVENT) {
//        final ScenarioEvent se = (ScenarioEvent) e;
//        if (se.getTimedEvent() instanceof AddParcelEvent) {
//          totalParcels++;
//        } else if (se.getTimedEvent() instanceof AddVehicleEvent) {
//          totalVehicles++;
//        } else if (se.getTimedEvent() instanceof TimeOutEvent) {
//          simFinish = true;
//          scenarioEndTime = se.getTimedEvent().getTime();
//        }
//      } else if (e.getEventType() == NEW_PARCEL) {
//        // pdp model event
//        acceptedParcels++;
//      } else if (e.getEventType() == NEW_VEHICLE) {
//        verify(e instanceof PDPModelEvent);
//        final PDPModelEvent ev = (PDPModelEvent) e;
//        lastArrivalTimeAtDepot.put(ev.vehicle, clock.getCurrentTime());
//      } else {
//        // currently not handling fall throughs
//      }
//
//    }
//
//    protected void increment(MovingRoadUser mru, double num) {
//      if (!distanceMap.containsKey(mru)) {
//        distanceMap.put(mru, num);
//      } else {
//        distanceMap.put(mru, distanceMap.get(mru) + num);
//      }
//    }
//  }
//
//  static class StatisticsEvent extends Event {
//    final Parcel parcel;
//    final Vehicle vehicle;
//    final long tardiness;
//    final long time;
//
//    StatisticsEvent(Enum<?> type, Object pIssuer, Parcel p, Vehicle v,
//        long tar, long tim) {
//      super(type, pIssuer);
//      parcel = p;
//      vehicle = v;
//      tardiness = tar;
//      time = tim;
//    }
//  }
//
//  /**
//   * Builder for creating {@link StatsTracker} instance.
//   * @author Rinde van Lon
//   */
//  @AutoValue
//  public abstract static class Builder
//      extends AbstractModelBuilder<StatsTracker, Object> {
//    private static final long serialVersionUID = -4339759920383479477L;
//
//    Builder() {
//      setDependencies(ScenarioController.class,
//        Clock.class,
//        RoadModel.class,
//        PDPModel.class);
//      setProvidingTypes(StatisticsProvider.class);
//    }
//
//    @Override
//    public StatsTracker build(DependencyProvider dependencyProvider) {
//      final ScenarioController ctrl =
//        dependencyProvider.get(ScenarioController.class);
//      final Clock clck = dependencyProvider.get(Clock.class);
//      final RoadModel rm = dependencyProvider.get(RoadModel.class);
//      final PDPModel pm = dependencyProvider.get(PDPModel.class);
//      return new StatsTracker(ctrl, clck, rm, pm);
//    }
//  }
//}
