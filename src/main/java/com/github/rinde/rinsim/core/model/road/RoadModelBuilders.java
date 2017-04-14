/*
 * Copyright (C) 2011-2017 Rinde van Lon, imec-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rinde.rinsim.core.model.road;

import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.Point;
import com.google.auto.value.AutoValue;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.primitives.Doubles;

import javax.annotation.CheckReturnValue;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class is the main entry point to obtain builders for creating
 * {@link RoadModel}s. There are three kinds of road model implementations:
 * <ul>
 * <li>plane based, see {@link #plane()}</li>
 * <li>static graph based, see {@link #staticGraph(Graph)}</li>
 * <li>dynamic graph based, see {@link #dynamicGraph(ListenableGraph)}</li>
 * </ul>
 *
 * @author Rinde van Lon
 */
public final class RoadModelBuilders {
    private RoadModelBuilders() {
    }

    /**
     * @return A new {@link PlaneRMB} for creating a
     * {@link PlaneRoadModel}.
     */
    public static PlaneRMB plane() {
        return PlaneRMB.create();
    }

    /**
     * Construct a new {@link StaticGraphRMB} for creating a
     * {@link GraphRoadModel}.
     * @param graph The graph which will be used as road structure.
     * @return A new {@link StaticGraphRMB}.
     */
    public static StaticGraphRMB staticGraph(Graph<?> graph) {
        return StaticGraphRMB.create(Suppliers.ofInstance(graph));
    }

    /**
     * Construct a new {@link StaticGraphRMB} for creating a
     * {@link GraphRoadModel}.
     * @param graphSupplier The supplier that creates a graph that will be used as
     *          road structure.
     * @return A new {@link StaticGraphRMB}.
     */
    public static StaticGraphRMB staticGraph(
            Supplier<? extends Graph<?>> graphSupplier) {
        return StaticGraphRMB.create(graphSupplier);
    }

    /**
     * Create a {@link DynamicGraphRMB} for constructing
     * {@link DynamicGraphRoadModel} instances.
     *
     * @param g A {@link ListenableGraph}.
     * @return A new {@link DynamicGraphRMB} instance.
     */
    public static DynamicGraphRMB dynamicGraph(
            ListenableGraph<?> g) {
        return dynamicGraph(Suppliers.<ListenableGraph<?>>ofInstance(g));
    }

    /**
     * Create a {@link DynamicGraphRMB} for constructing
     * {@link DynamicGraphRoadModel} instances.
     *
     * @param g A supplier of {@link ListenableGraph}.
     * @return A new {@link DynamicGraphRMB} instance.
     */
    public static DynamicGraphRMB dynamicGraph(
            Supplier<? extends ListenableGraph<?>> g) {
        return DynamicGraphRMB.create(g);
    }

    /**
     * Abstract builder for constructing subclasses of {@link RoadModel}.
     *
     * @param <T> The type of the model that the builder is constructing.
     * @param <S> The builder type itself, necessary to make an inheritance-based
     *            builder.
     * @author Rinde van Lon
     */
    public abstract static class AbstractRMB<T extends RoadModel, S>
            extends AbstractModelBuilder<T, RoadUser> {

        /**
         * The default distance unit: {@link SI#KILOMETER}.
         */
        protected static final Unit<Length> DEFAULT_DISTANCE_UNIT = SI.KILOMETER;

        protected static final boolean DEFAULT_ROUTING_TABLE = false;

        /**
         * The default speed unit: {@link NonSI#KILOMETERS_PER_HOUR}.
         */
        protected static final Unit<Velocity> DEFAULT_SPEED_UNIT =
                NonSI.KILOMETERS_PER_HOUR;

        private static final long serialVersionUID = 5047700025488786509L;

        /**
         * @return the distanceUnit
         */
        public abstract Unit<Length> getDistanceUnit();

        /**
         * @return the speedUnit
         */
        public abstract Unit<Velocity> getSpeedUnit();

        /**
         * Returns a new copy of this builder with the specified distance unit used
         * for all distances. The default is {@link SI#KILOMETER}.
         *
         * @param unit The distance unit to set.
         * @return A new builder instance.
         */
        @CheckReturnValue
        public abstract S withDistanceUnit(Unit<Length> unit);

        /**
         * Returns a new copy of this builder with the specified speed unit to use
         * for all speeds. The default is {@link NonSI#KILOMETERS_PER_HOUR}.
         *
         * @param unit The speed unit to set
         * @return A new builder instance.
         */
        @CheckReturnValue
        public abstract S withSpeedUnit(Unit<Velocity> unit);
    }

    /**
     * Abstract builder for constructing subclasses of {@link GraphRoadModel}.
     *
     * @param <T> The type of the model that the builder is constructing.
     * @param <S> The builder type itself, necessary to make a inheritance-based
     *            builder.
     * @param <G> The type of the graph.
     * @author Rinde van Lon
     */
    public abstract static class AbstractGraphRMB<T extends GraphRoadModel, S, G extends Graph<?>>
            extends AbstractRMB<T, S> {

        /**
         *
         */
        private static final long serialVersionUID = -2141173156740097368L;

        /**
         * Create a new instance.
         */
        protected AbstractGraphRMB() {
            setProvidingTypes(RoadModel.class, GraphRoadModel.class);
        }

        /**
         * @return The graph supplier.
         */
        protected abstract Supplier<G> getGraphSupplier();

        protected abstract boolean getRoutingTable();


        /**
         * @return the graph
         */
        public G getGraph() {
            return getGraphSupplier().get();
        }
    }

    /**
     * Abstract builder for constructing subclasses of
     * {@link DynamicGraphRoadModel}.
     *
     * @param <T> The type of the model that the builder is constructing.
     * @param <S> The builder type itself, necessary to make a inheritance-based
     *            builder.
     * @author Rinde van Lon
     */
    public abstract static class AbstractDynamicGraphRMB<T extends DynamicGraphRoadModel, S>
            extends AbstractGraphRMB<T, S, ListenableGraph<?>> {
        static final boolean DEFAULT_MOD_CHECK = true;
        private static final long serialVersionUID = 3364846541093473765L;

        /**
         * Allows to enable or disable graph modification checking. When graph
         * modification checking is enabled, modifications of the
         * {@link ListenableGraph} are checked to prevent inconsistent state in the
         * model. An example of an invalid modification is the removal (or change of
         * connection data) of a connection with a road user on it. It is the
         * responsibility of the user to ensure that graph modifications are
         * consistent, the modification check merely throws an
         * {@link IllegalStateException} when such an inconsistency is detected.
         * Adding a custom modification checker can be done by adding a listener to
         * the specified {@link ListenableGraph}.
         * <p>
         * <b>Warning:</b> disabling this check may result in an inconsistent model,
         * <i>without any exception thrown to indicate so</i>.
         *
         * @param enabled <code>true</code> means enabled, <code>false</code> means
         *                disabled. Default: <code>true</code>.
         * @return A new builder instance.
         */
        public abstract S withModificationCheck(boolean enabled);

        @Override
        protected abstract Supplier<ListenableGraph<?>> getGraphSupplier();

        protected abstract boolean getRoutingTable();


        /**
         * @return Whether the graph modification checker is enabled.
         */
        protected abstract boolean isModCheckEnabled();
    }

    /**
     * A builder for {@link PlaneRoadModel}. Instances can be obtained via
     * {@link #plane()}.
     *
     * @author Rinde van Lon
     */
    @AutoValue
    public abstract static class PlaneRMB
            extends AbstractRMB<PlaneRoadModel, PlaneRMB> {
        static final double DEFAULT_MAX_SPEED = 50d;
        static final Point DEFAULT_MIN_POINT = new Point(0, 0);
        static final Point DEFAULT_MAX_POINT = new Point(10, 10);
        private static final long serialVersionUID = 8160700332762443917L;

        PlaneRMB() {
            setProvidingTypes(RoadModel.class, PlaneRoadModel.class);
        }

        static PlaneRMB create() {
            return create(DEFAULT_DISTANCE_UNIT, DEFAULT_SPEED_UNIT,
                    DEFAULT_MIN_POINT, DEFAULT_MAX_POINT, DEFAULT_MAX_SPEED);
        }

        static PlaneRMB create(Unit<Length> distanceUnit, Unit<Velocity> speedUnit,
                               Point min, Point max, double maxSpeed) {
            return new AutoValue_RoadModelBuilders_PlaneRMB(distanceUnit, speedUnit,
                    min, max, maxSpeed);
        }

        abstract Point getMin();

        abstract Point getMax();

        abstract double getMaxSpeed();

        /**
         * Returns a copy of this builder with the specified min point. The min
         * point defines the left top corner of the plane. The default is
         * <code>(0,0)</code>.
         *
         * @param minPoint The min point to set.
         * @return A new builder instance.
         */
        @CheckReturnValue
        public PlaneRMB withMinPoint(Point minPoint) {
            return create(getDistanceUnit(), getSpeedUnit(), minPoint, getMax(),
                    getMaxSpeed());
        }

        /**
         * Returns a copy of this builder with the specified max point. The max
         * point defines the right bottom corner of the plane. The default is
         * <code>(10,10)</code>.
         *
         * @param maxPoint The max point to set.
         * @return A new builder instance.
         */
        @CheckReturnValue
        public PlaneRMB withMaxPoint(Point maxPoint) {
            return create(getDistanceUnit(), getSpeedUnit(), getMin(), maxPoint,
                    getMaxSpeed());
        }

        /**
         * Returns a copy of this builder with the specified maximum speed. The
         * maximum speed will be used for all vehicles in the model. The default is
         * <code>50</code>.
         *
         * @param maxSpeed The max speed to set.
         * @return A new builder instance.
         */
        @CheckReturnValue
        public PlaneRMB withMaxSpeed(double maxSpeed) {
            checkArgument(maxSpeed > 0d,
                    "Max speed must be strictly positive but is %s.",
                    maxSpeed);
            return create(getDistanceUnit(), getSpeedUnit(), getMin(), getMax(),
                    maxSpeed);
        }

        @Override
        public PlaneRMB withDistanceUnit(Unit<Length> unit) {
            return create(unit, getSpeedUnit(), getMin(), getMax(), getMaxSpeed());
        }

        @Override
        public PlaneRMB withSpeedUnit(Unit<Velocity> unit) {
            return create(getDistanceUnit(), unit, getMin(), getMax(), getMaxSpeed());
        }

        @Override
        public PlaneRoadModel build(DependencyProvider dependencyProvider) {
            checkArgument(
                    getMin().x < getMax().x && getMin().y < getMax().y,
                    "Min should have coordinates smaller than max, found min %s and max"
                            + " %s.",
                    getMin(), getMax());
            return new PlaneRoadModel(this);
        }

        @Override
        public String toString() {
            return RoadModelBuilders.class.getSimpleName() + ".plane()";
        }
    }


    /**
     * A builder for creating {@link GraphRoadModel} instances. Instances can be
     * obtained via {@link RoadModelBuilders#staticGraph(Graph)}.
     *
     * @author Rinde van Lon
     */
    @AutoValue
    public abstract static class StaticGraphRMB
            extends AbstractGraphRMB<GraphRoadModel, StaticGraphRMB, Graph<?>> {
        private static final long serialVersionUID = 1206566008918936928L;

        StaticGraphRMB() {
            setProvidingTypes(RoadModel.class, GraphRoadModel.class);
        }

        @SuppressWarnings("unchecked")
        static StaticGraphRMB create(Unit<Length> distanceUnit,
                                     Unit<Velocity> speedUnit, Supplier<? extends Graph<?>> graph, boolean routingTable) {
            return new AutoValue_RoadModelBuilders_StaticGraphRMB(distanceUnit,
                    speedUnit, (Supplier<Graph<?>>) graph, routingTable);
        }

        static StaticGraphRMB create(Supplier<? extends Graph<?>> graphSupplier) {
            return new AutoValue_RoadModelBuilders_StaticGraphRMB(DEFAULT_DISTANCE_UNIT,
                    DEFAULT_SPEED_UNIT, (Supplier<Graph<?>>) graphSupplier, DEFAULT_ROUTING_TABLE);
        }

        @Override
        protected abstract Supplier<Graph<?>> getGraphSupplier();

        public abstract boolean getRoutingTable();

        @Override
        public StaticGraphRMB withDistanceUnit(Unit<Length> unit) {
            return create(unit, getSpeedUnit(), getGraphSupplier(), getRoutingTable());
        }

        @Override
        public StaticGraphRMB withSpeedUnit(Unit<Velocity> unit) {
            return create(getDistanceUnit(), unit, getGraphSupplier(), getRoutingTable());
        }

        /**
         * When this is called it will return a builder that creates
         * {@link CachedGraphRoadModel} instead.
         *
         * @return A new {@link CachedGraphRMB} instance.
         */
        @CheckReturnValue
        public CachedGraphRMB withCache() {
            return CachedGraphRMB.create(getDistanceUnit(), getSpeedUnit(),
                    getGraphSupplier());
        }

        public StaticGraphRMB withRoutingTable(boolean routingTable) {
            return create(getDistanceUnit(), getSpeedUnit(), getGraphSupplier(), routingTable);
        }

        @Override
        public GraphRoadModelImpl build(DependencyProvider dependencyProvider) {
            return new GraphRoadModelImpl(getGraph(), this);
        }

        @Override
        public String toString() {
            return RoadModelBuilders.class.getSimpleName() + ".staticGraph()";
        }
    }

    /**
     * A builder for constructing {@link DynamicGraphRoadModel} instances. Use
     * {@link RoadModelBuilders#dynamicGraph(ListenableGraph)} for obtaining
     * builder instances.
     *
     * @author Rinde van Lon
     */
    @AutoValue
    public abstract static class DynamicGraphRMB
            extends
            AbstractDynamicGraphRMB<DynamicGraphRoadModelImpl, DynamicGraphRMB> {

        private static final long serialVersionUID = 7269626100558413212L;

        static DynamicGraphRMB create(
                Supplier<? extends ListenableGraph<?>> graphSupplier) {
            return create(DEFAULT_DISTANCE_UNIT, DEFAULT_SPEED_UNIT, graphSupplier,
                    DEFAULT_MOD_CHECK);
        }

        @SuppressWarnings("unchecked")
        static DynamicGraphRMB create(Unit<Length> distanceUnit,
                                      Unit<Velocity> speedUnit,
                                      Supplier<? extends ListenableGraph<?>> graphSupplier,
                                      boolean isGmcEnabled) {
            return new AutoValue_RoadModelBuilders_DynamicGraphRMB(distanceUnit,
                    speedUnit, (Supplier<ListenableGraph<?>>) graphSupplier, DEFAULT_ROUTING_TABLE, isGmcEnabled);
        }

        /**
         * Will return a new builder that constructs {@link CollisionGraphRoadModel}
         * instances instead of {@link DynamicGraphRoadModel} instances. Note that
         * all connections in the specified graph must have length
         * <code>2 * vehicleLength</code>, where vehicle length can be specified in
         * {@link CollisionGraphRMB#withVehicleLength(double)}.
         *
         * @return A new {@link CollisionGraphRMB} instance.
         */
        @CheckReturnValue
        public CollisionGraphRMB withCollisionAvoidance() {
            return CollisionGraphRMB.create(this);
        }

        @Override
        public DynamicGraphRMB withModificationCheck(boolean enabled) {
            return create(getDistanceUnit(), getSpeedUnit(), getGraphSupplier(),
                    enabled);
        }

        @Override
        public DynamicGraphRMB withDistanceUnit(Unit<Length> unit) {
            return create(unit, getSpeedUnit(), getGraphSupplier(),
                    isModCheckEnabled());
        }

        @Override
        public DynamicGraphRMB withSpeedUnit(Unit<Velocity> unit) {
            return create(getDistanceUnit(), unit, getGraphSupplier(),
                    isModCheckEnabled());
        }

        @Override
        public DynamicGraphRoadModelImpl build(
                DependencyProvider dependencyProvider) {
            return new DynamicGraphRoadModelImpl(getGraph(), this);
        }

        @Override
        public String toString() {
            return RoadModelBuilders.class.getSimpleName() + ".dynamicGraph()";
        }
    }

    /**
     * Builder for {@link CachedGraphRoadModel} instances.
     *
     * @author Rinde van Lon
     */
    @AutoValue
    public abstract static class CachedGraphRMB
            extends AbstractGraphRMB<CachedGraphRoadModel, CachedGraphRMB, Graph<?>> {

        private static final long serialVersionUID = -7837221650923727573L;

        @SuppressWarnings("unchecked")
        static CachedGraphRMB create(Unit<Length> distanceUnit,
                                     Unit<Velocity> speedUnit, Supplier<? extends Graph<?>> graph) {
            return new AutoValue_RoadModelBuilders_CachedGraphRMB(distanceUnit,
                    speedUnit, (Supplier<Graph<?>>) graph, DEFAULT_ROUTING_TABLE);
        }

        @Override
        protected abstract Supplier<Graph<?>> getGraphSupplier();

        protected abstract boolean getRoutingTable();


        @Override
        public CachedGraphRoadModel build(DependencyProvider dependencyProvider) {
            return new CachedGraphRoadModel(getGraph(), this);
        }

        @Override
        public CachedGraphRMB withDistanceUnit(Unit<Length> unit) {
            return create(unit, getSpeedUnit(), getGraphSupplier());
        }

        @Override
        public CachedGraphRMB withSpeedUnit(Unit<Velocity> unit) {
            return create(getDistanceUnit(), unit, getGraphSupplier());
        }

        @Override
        public String toString() {
            return RoadModelBuilders.class.getSimpleName()
                    + ".staticGraph().withCache()";
        }
    }

    /**
     * A builder for constructing {@link CollisionGraphRoadModel} instances.
     *
     * @author Rinde van Lon
     */
    @AutoValue
    public abstract static class CollisionGraphRMB
            extends
            AbstractDynamicGraphRMB<CollisionGraphRoadModelImpl, CollisionGraphRMB> {

        /**
         * The default vehicle length: <code>2</code>.
         */
        public static final double DEFAULT_VEHICLE_LENGTH = 2;

        /**
         * The default minimum distance: <code>.25</code>.
         */
        public static final double DEFAULT_MIN_DISTANCE = .25;

        private static final long serialVersionUID = -5076770082090735004L;

        CollisionGraphRMB() {
            setProvidingTypes(RoadModel.class, GraphRoadModel.class,
                    DynamicGraphRoadModel.class, CollisionGraphRoadModel.class);
        }

        static CollisionGraphRMB create(DynamicGraphRMB builder) {
            return create(builder.getDistanceUnit(), builder.getSpeedUnit(),
                    builder.getGraphSupplier(), builder.isModCheckEnabled(),
                    DEFAULT_VEHICLE_LENGTH, DEFAULT_MIN_DISTANCE);
        }

        static CollisionGraphRMB create(Unit<Length> distanceUnit,
                                        Unit<Velocity> speedUnit,
                                        Supplier<ListenableGraph<?>> graphSupplier,
                                        boolean isGmcEnabled,
                                        double vehicleLength,
                                        double minDistance) {
            return new AutoValue_RoadModelBuilders_CollisionGraphRMB(distanceUnit,
                    speedUnit, graphSupplier, DEFAULT_ROUTING_TABLE, isGmcEnabled, vehicleLength, minDistance);
        }

        abstract double getVehicleLength();

        abstract double getMinDistance();

        /**
         * Returns a copy of this builder with the specified vehicle length. The
         * vehicle length defines the length of each vehicle added to the
         * {@link CollisionGraphRoadModel} that will be constructed by this builder.
         * The vehicle length must be a strictly positive number. The default value
         * is {@link #DEFAULT_VEHICLE_LENGTH}.
         *
         * @param length A length expressed in the unit set by
         *               {@link #withDistanceUnit(Unit)}.
         * @return A new builder instance.
         */
        @CheckReturnValue
        public CollisionGraphRMB withVehicleLength(double length) {
            checkArgument(length > 0d,
                    "Only positive vehicle lengths are allowed, found %s.", length);
            checkArgument(Doubles.isFinite(length),
                    "%s is not a valid vehicle length.", length);
            return create(getDistanceUnit(), getSpeedUnit(), getGraphSupplier(),
                    isModCheckEnabled(), length, getMinDistance());
        }

        @Override
        public CollisionGraphRMB withModificationCheck(boolean enabled) {
            return create(getDistanceUnit(), getSpeedUnit(), getGraphSupplier(),
                    enabled, getVehicleLength(), getMinDistance());
        }

        /**
         * Returns a copy of this builder with the specified min distance. The min
         * distance defines the minimum required distance between two vehicles. The
         * minimum distance must be a positive number &le; to 2 * vehicle length.
         * The default value is {@link #DEFAULT_MIN_DISTANCE}.
         *
         * @param dist A distance expressed in the unit set by
         *             {@link #withDistanceUnit(Unit)}.
         * @return A new builder instance.
         */
        @CheckReturnValue
        public CollisionGraphRMB withMinDistance(double dist) {
            checkArgument(dist >= 0d);
            return create(getDistanceUnit(), getSpeedUnit(), getGraphSupplier(),
                    isModCheckEnabled(), getVehicleLength(), dist);
        }

        @Override
        public CollisionGraphRMB withDistanceUnit(Unit<Length> unit) {
            return create(unit, getSpeedUnit(), getGraphSupplier(),
                    isModCheckEnabled(), getVehicleLength(), getMinDistance());
        }

        @Override
        public CollisionGraphRMB withSpeedUnit(Unit<Velocity> unit) {
            return create(getDistanceUnit(), unit, getGraphSupplier(),
                    isModCheckEnabled(), getVehicleLength(), getMinDistance());
        }

        @Override
        public CollisionGraphRoadModelImpl build(
                DependencyProvider dependencyProvider) {
            checkArgument(getDistanceUnit() == SI.METER,
                    "Currently only %s is supported, found %s.", SI.METER,
                    getDistanceUnit());

            final double minConnectionLength = getVehicleLength();
            checkArgument(
                    getMinDistance() <= minConnectionLength,
                    "Min distance must be smaller than 2 * vehicle length (%s), but is %s.",
                    getVehicleLength(), getMinDistance());
            final ListenableGraph<?> graph = getGraph();

            for (final Connection<?> conn : graph.getConnections()) {
                CollisionGraphRoadModelImpl
                        .checkConnectionLength(minConnectionLength, conn);
            }
            return new CollisionGraphRoadModelImpl(graph, minConnectionLength, this);
        }

        @Override
        public String toString() {
            return RoadModelBuilders.class.getSimpleName()
                    + ".dynamicGraph().withCollisionAvoidance()";
        }
    }
}
