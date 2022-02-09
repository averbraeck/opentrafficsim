package org.opentrafficsim.kpi.sampling;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.CompressedFileWriter;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;
import org.opentrafficsim.kpi.interfaces.NodeDataInterface;
import org.opentrafficsim.kpi.interfaces.RouteDataInterface;
import org.opentrafficsim.kpi.sampling.ListTable.ListRecord;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.FilterDataType;

/**
 * SamplerData is a storage for trajectory data. Adding trajectory groups can only be done by subclasses. This is however not a
 * guaranteed read-only class. Any type can obtain the lane directions and with those the coupled trajectory groups.
 * Trajectories can be added to these trajectory groups. Data can also be added to the trajectories themselves.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
// TODO: extending list table requires us to know the columns beforehand, create a view asTable()?
public class SamplerData<G extends GtuDataInterface> extends AbstractTable
{

    /**
     * Constructor.
     * @param columns Collection&lt;Column&lt;?&gt;&gt;; columns
     */
    public SamplerData(final Collection<Column<?>> columns)
    {
        super("sampler", "Trajectory data", columns);
    }

    /** Map with all sampling data. */
    private final Map<KpiLaneDirection, TrajectoryGroup<G>> trajectories = new LinkedHashMap<>();

    /**
     * Stores a trajectory group with the lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     * @param trajectoryGroup trajectory group for given lane direction
     */
    protected final void putTrajectoryGroup(final KpiLaneDirection kpiLaneDirection, final TrajectoryGroup<G> trajectoryGroup)
    {
        this.trajectories.put(kpiLaneDirection, trajectoryGroup);
    }

    /**
     * Returns the set of lane directions.
     * @return Set&lt;KpiLaneDirection&gt;; lane directions
     */
    public final Set<KpiLaneDirection> getLaneDirections()
    {
        return this.trajectories.keySet();
    }

    /**
     * Returns whether there is data for the give lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     * @return whether there is data for the give lane direction
     */
    public final boolean contains(final KpiLaneDirection kpiLaneDirection)
    {
        return this.trajectories.containsKey(kpiLaneDirection);
    }

    /**
     * Returns the trajectory group of given lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     * @return trajectory group of given lane direction, {@code null} if none
     */
    public final TrajectoryGroup<G> getTrajectoryGroup(final KpiLaneDirection kpiLaneDirection)
    {
        return this.trajectories.get(kpiLaneDirection);
    }

    /**
     * Write the contents of the sampler in to a file. By default this is zipped and numeric data is formated %.3f.
     * @param file String; file
     */
    public final void writeToFile(final String file)
    {
        writeToFile(file, "%.3f", CompressionMethod.ZIP);
    }

    /**
     * Write the contents of the sampler in to a file.
     * @param file String; file
     * @param format String; number format, as used in {@code String.format()}
     * @param compression CompressionMethod; how to compress the data
     */
    public final void writeToFile(final String file, final String format, final CompressionMethod compression)
    {
        int counter = 0;
        BufferedWriter bw = CompressedFileWriter.create(file, compression.equals(CompressionMethod.ZIP));

        // TODO: Sampler used this to cut-off space if SpaceTimeRegion's did not cover complete lanes. Trajectories are however
        // recorded over the complete length.

        /*
         * // create Query, as this class is designed to filter for space-time regions Query<G> query = new Query<>(this, "",
         * new MetaDataSet()); for (SpaceTimeRegion str : this.spaceTimeRegions) {
         * query.addSpaceTimeRegion(str.getLaneDirection(), str.getStartPosition(), str.getEndPosition(), str.getStartTime(),
         * str.getEndTime()); } List<TrajectoryGroup<G>> groups =
         * query.getTrajectoryGroups(Time.instantiateSI(Double.POSITIVE_INFINITY));
         */

        Collection<TrajectoryGroup<G>> groups = this.trajectories.values();
        try
        {
            // gather all filter data types for the header line
            List<FilterDataType<?>> allFilterDataTypes = new ArrayList<>();
            for (TrajectoryGroup<G> group : groups)
            {
                for (Trajectory<G> trajectory : group.getTrajectories())
                {
                    for (FilterDataType<?> filterDataType : trajectory.getFilterDataTypes())
                    {
                        if (!allFilterDataTypes.contains(filterDataType))
                        {
                            allFilterDataTypes.add(filterDataType);
                        }
                    }
                }
            }
            // gather all extended data types for the header line
            List<ExtendedDataType<?, ?, ?, ?>> allExtendedDataTypes = new ArrayList<>();
            for (TrajectoryGroup<G> group : groups)
            {
                for (Trajectory<?> trajectory : group.getTrajectories())
                {
                    for (ExtendedDataType<?, ?, ?, ?> extendedDataType : trajectory.getExtendedDataTypes())
                    {
                        if (!allExtendedDataTypes.contains(extendedDataType))
                        {
                            allExtendedDataTypes.add(extendedDataType);
                        }
                    }
                }
            }
            // create header line
            StringBuilder str = new StringBuilder();
            str.append("traj#,linkId,laneId&dir,gtuId,t,x,v,a");
            for (FilterDataType<?> metaDataType : allFilterDataTypes)
            {
                str.append(",");
                str.append(metaDataType.getId());
            }
            for (ExtendedDataType<?, ?, ?, ?> extendedDataType : allExtendedDataTypes)
            {
                str.append(",");
                str.append(extendedDataType.getId());
            }
            bw.write(str.toString());
            bw.newLine();
            for (TrajectoryGroup<G> group : groups)
            {
                for (Trajectory<G> trajectory : group.getTrajectories())
                {
                    counter++;
                    float[] t = trajectory.getT();
                    float[] x = trajectory.getX();
                    float[] v = trajectory.getV();
                    float[] a = trajectory.getA();
                    Map<ExtendedDataType<?, ?, ?, ?>, Object> extendedData = new LinkedHashMap<>();
                    for (ExtendedDataType<?, ?, ?, ?> extendedDataType : allExtendedDataTypes)
                    {
                        if (trajectory.contains(extendedDataType))
                        {
                            try
                            {
                                extendedData.put(extendedDataType, trajectory.getExtendedData(extendedDataType));
                            }
                            catch (SamplingException exception)
                            {
                                // should not occur, we obtain the extended data types from the trajectory
                                throw new RuntimeException("Error while loading extended data type.", exception);
                            }
                        }
                    }
                    for (int i = 0; i < t.length; i++)
                    {
                        // TODO: values can contain ","; use csv writer
                        str = new StringBuilder();
                        str.append(counter);
                        str.append(",");
                        if (!compression.equals(CompressionMethod.OMIT_DUPLICATE_INFO) || i == 0)
                        {
                            str.append(group.getLaneDirection().getLaneData().getLinkData().getId());
                            str.append(",");
                            str.append(group.getLaneDirection().getLaneData().getId());
                            str.append(group.getLaneDirection().getKpiDirection().isPlus() ? "+" : "-");
                            str.append(",");
                            str.append(trajectory.getGtuId());
                            str.append(",");
                        }
                        else
                        {
                            // one trajectory is on the same lane and pertains to the same GTU, no need to repeat data
                            str.append(",,,");
                        }
                        str.append(String.format(format, t[i]));
                        str.append(",");
                        str.append(String.format(format, x[i]));
                        str.append(",");
                        str.append(String.format(format, v[i]));
                        str.append(",");
                        str.append(String.format(format, a[i]));
                        for (FilterDataType<?> metaDataType : allFilterDataTypes)
                        {
                            str.append(",");
                            if (i == 0 && trajectory.contains(metaDataType))
                            {
                                // no need to repeat meta data
                                str.append(metaDataType.formatValue(format, castValue(trajectory.getMetaData(metaDataType))));
                            }
                        }
                        for (ExtendedDataType<?, ?, ?, ?> extendedDataType : allExtendedDataTypes)
                        {
                            str.append(",");
                            if (trajectory.contains(extendedDataType))
                            {
                                try
                                {
                                    str.append(extendedDataType.formatValue(format, castValue(extendedData, extendedDataType,
                                        i)));
                                }
                                catch (SamplingException exception)
                                {
                                    // should not occur, we obtain the extended data types from the trajectory
                                    throw new RuntimeException("Error while loading extended data type.", exception);
                                }
                            }
                        }
                        bw.write(str.toString());
                        bw.newLine();
                    }
                }
            }
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Could not write to file.", exception);
        }
        // close file on fail
        finally
        {
            try
            {
                if (bw != null)
                {
                    bw.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Cast value to type for meta data.
     * @param value Object; value object to cast
     * @return cast value
     * @param <T> type of value
     */
    @SuppressWarnings("unchecked")
    private <T> T castValue(final Object value)
    {
        return (T) value;
    }

    /**
     * Cast value to type for extended data.
     * @param extendedData Map&lt;ExtendedDataType&lt;?,?,?,?&gt;,Object&gt;; extended data of trajectory in output form
     * @param extendedDataType ExtendedDataType&lt;?,?,?,?&gt;; extended data type
     * @param i int; index of value to return
     * @return cast value
     * @throws SamplingException when the found index is out of bounds
     * @param <T> type of value
     * @param <O> output type
     * @param <S> storage type
     */
    @SuppressWarnings("unchecked")
    private <T, O, S> T castValue(final Map<ExtendedDataType<?, ?, ?, ?>, Object> extendedData, final ExtendedDataType<?, ?, ?,
            ?> extendedDataType, final int i) throws SamplingException
    {
        // is only called on value directly taken from an ExtendedDataType within range of trajectory
        ExtendedDataType<T, O, S, ?> edt = (ExtendedDataType<T, O, S, ?>) extendedDataType;
        return edt.getOutputValue((O) extendedData.get(edt), i);
    }

    /**
     * Defines the compression method for stored data.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mei 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum CompressionMethod
    {
        /** No compression. */
        NONE,

        /** Duplicate info per trajectory is only stored at the first sample, and empty for other samples. */
        OMIT_DUPLICATE_INFO,

        /** Zip compression. */
        ZIP,
    }

    /**
     * Loads sampler data from a file. There are a few limitations with respect to live sampled data:
     * <ol>
     * <li>The number of decimals in numeric data is equal to the stored format.</li>
     * <li>All extended data types are stored as {@code String}.</li>
     * <li>Meta data types are not recognized, and hence stored as extended data types. Values are always stored as
     * {@code String}.</li>
     * </ol>
     * @param file String; file
     * @return Sampler data from file
     */
    public static SamplerData<?> loadFromFile(final String file)
    {
        return loadFromFile(file, new LinkedHashSet<ExtendedDataType<?, ?, ?, ?>>(), new LinkedHashSet<FilterDataType<?>>());
    }

    /**
     * Loads sampler data from a file. There are a few limitations with respect to live sampled data:
     * <ol>
     * <li>The number of decimals in numeric data is equal to the stored format.</li>
     * <li>All extended data types are stored as {@code String}, unless recognized by id as provided.</li>
     * <li>Meta data types are not recognized, and hence stored as extended data types, unless recognized by id as provided.
     * Values are always stored as {@code String}.</li>
     * </ol>
     * @param file String; file
     * @param extendedDataTypes Set&lt;ExtendedDataType&lt;?, ?, ?, ?&gt;&gt;; extended data types
     * @param metaDataTypes Set&lt;MetaDataType&lt;?&gt;&gt;; meta data types
     * @return Sampler data from file
     */
    @SuppressWarnings("unchecked")
    public static SamplerData<?> loadFromFile(final String file, final Set<ExtendedDataType<?, ?, ?, ?>> extendedDataTypes,
            final Set<FilterDataType<?>> metaDataTypes)
    {
        /*
        @SuppressWarnings("rawtypes")
        SamplerData samplerData = new SamplerData();

        // "traj#,linkId,laneId&dir,gtuId,t,x,v,a" meta data types, extended data types

        // we can use the default meta data types: cross section, destination, origin, route and GTU type

        Getter<NodeData> nodes = new Getter<NodeData>((id) -> new NodeData(id));
        Getter<GtuTypeData> gtuTypes = new Getter<GtuTypeData>((id) -> new GtuTypeData(id));
        Getter<RouteData> routes = new Getter<RouteData>((id) -> new RouteData(id));
        Getter<LinkData> links = new Getter<LinkData>((id) -> new LinkData(id));
        BiGetter<LinkData, LaneData> lanes = new BiGetter<LinkData, LaneData>((id, link) -> new LaneData(id, link));
        BiGetter<LaneData, KpiLaneDirection> laneDirections = new BiGetter<LaneData, KpiLaneDirection>((dir,
                lane) -> new KpiLaneDirection(lane, dir.equals("+") ? KpiGtuDirectionality.DIR_PLUS
                        : KpiGtuDirectionality.DIR_MINUS));
        @SuppressWarnings("rawtypes")
        Function<KpiLaneDirection, TrajectoryGroup> groupFunction = (laneDir) -> new TrajectoryGroup(Time.ZERO, laneDir);

        String id = null;
        if (!gtus.containsKey(id))
        {
            // NOTE: USE SEPARATE IDS HERE
            gtus.put(id, new GtuData(id, nodeSupplier.apply(id), nodeSupplier.apply(id), gtuTypeSupplier.apply(id),
                routeSupplier.apply(id)));
        }
        GtuData gtuData = gtus.get(id);

        Trajectory<?> trajectory = new Trajectory(gtuData, metaData, extendedDataTypes, kpiLaneDirection);

        // TODO: set data from outside
        trajectory.add(position, speed, acceleration, time, gtu);

        KpiLaneDirection laneDir = null;
        ((TrajectoryGroup) samplerData.trajectories.computeIfAbsent(laneDir, groupFunction)).addTrajectory(trajectory);

        return samplerData;
        */
        return null;
    }

    /**
     * Returns a value from the map. Creates a value if needed.
     * @param id String; id of object (key in map)
     * @param map Map&lt;String, T&gt;; stored values
     * @param producer Supplier&lt;TT&gt;; producer used if no value exists in the map
     * @param <T> type
     * @return value for the id
     */
    private final <T> T getOrCreate(final String id, final Map<String, T> map, final Function<String, T> producer)
    {
        if (!map.containsKey(id))
        {
            map.put(id, producer.apply(id));
        }
        return map.get(id);
    }

    // TABLE METHODS

    /** {@inheritDoc} */
    @Override
    public Iterator<Record> iterator()
    {
        // TODO: local iterator over this.trajectories, trajectories per group, and length of each trajectory

        // TODO: gathering the extended and filter data types should be done here, these are within the trajectories, and upon
        // file loading, this should be mimicked

        Iterator<KpiLaneDirection> laneIterator = this.trajectories.keySet().iterator();

        return new Iterator<Record>()
        {
            private Iterator<Trajectory<G>> trajectoryIterator = laneIterator.hasNext() ? SamplerData.this.trajectories.get(
                laneIterator.next()).iterator() : null;

            private Trajectory<G> trajectory = this.trajectoryIterator != null && this.trajectoryIterator.hasNext()
                    ? this.trajectoryIterator.next() : null;

            private Trajectory<G> currentTrajectory;

            private int index;

            @Override
            public boolean hasNext()
            {
                if (this.index == this.currentTrajectory.size())
                {
                    // get next trajectory

                }
                return true;
            }

            @Override
            public Record next()
            {
                Record record = new Record()
                {
                    @Override
                    public <T> T getValue(final Column<T> column)
                    {
                        return null;
                    }

                    @Override
                    public Object getValue(final String id)
                    {
                        return null;
                    }
                };
                this.index++;
                return record;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty()
    {
        for (TrajectoryGroup<G> group : this.trajectories.values())
        {
            for (Trajectory<G> trajectory : group.getTrajectories())
            {
                if (trajectory.size() > 0)
                {
                    return false;
                }
            }
        }
        return true;
    }

    // LOCAL HELPER CLASSES TO IMPLEMENT INTERFACES //

    /**
     * Getter for single {@code String} input.
     * @param <T> output value type
     */
    private static class Getter<T>
    {

        /** Map with cached values. */
        private final Map<String, T> map = new LinkedHashMap<>();

        /** Provider function. */
        private Function<String, T> function;

        /**
         * Constructor.
         * @param function Function&lt;String, T&gt;; provider function
         */
        Getter(final Function<String, T> function)
        {
            this.function = function;
        }

        /**
         * Get value, from cache or provider function.
         * @param id String; id
         * @return T; value, from cache or provider function
         */
        public T get(final String id)
        {
            T t;
            if (!this.map.containsKey(id))
            {
                t = this.function.apply(id);
                this.map.put(id, t);
            }
            else
            {
                t = this.map.get(id);
            }
            return t;
        }
    }

    /**
     * Getter for dual {@code String} and {@code O} input.
     * @param <O> type of second input (besides the first being {@code String})
     * @param <T> output value type
     */
    private static class BiGetter<O, T>
    {

        /** Map with cached values. */
        private final Map<String, T> map = new LinkedHashMap<>();

        /** Provider function. */
        private BiFunction<String, O, T> function;

        /**
         * Constructor.
         * @param function BiFunction&lt;String, T&gt;; provider function
         */
        BiGetter(final BiFunction<String, O, T> function)
        {
            this.function = function;
        }

        /**
         * Get value, from cache or provider function.
         * @param id String; id
         * @param o O; other object
         * @return T; value, from cache or provider function
         */
        public T get(final String id, final O o)
        {
            T t;
            if (!this.map.containsKey(id))
            {
                t = this.function.apply(id, o);
                this.map.put(id, t);
            }
            else
            {
                t = this.map.get(id);
            }
            return t;
        }
    }

    /** Helper class LinkData. */
    private static class LinkData implements LinkDataInterface
    {

        /** Length ({@code null} always). */
        private final Length length = null; // unknown in this context

        /** Id. */
        private final String id;

        /** Lanes. */
        private final List<LaneData> lanes = new ArrayList<>();

        /**
         * @param id String; id
         */
        LinkData(final String id)
        {
            this.id = id;
        }

        /** {@inheritDoc} */
        @Override
        public Length getLength()
        {
            return this.length;
        }

        /** {@inheritDoc} */
        @Override
        public List<? extends LaneDataInterface> getLaneDatas()
        {
            return this.lanes;
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return this.id;
        }

    }

    /** Helper class LaneData. */
    private static class LaneData implements LaneDataInterface
    {

        /** Length ({@code null} always). */
        private final Length length = null; // unknown in this context

        /** Id. */
        private final String id;

        /** Link. */
        private final LinkData link;

        /**
         * Constructor.
         * @param id String; id
         * @param link LinkData; link
         */
        @SuppressWarnings("synthetic-access")
        LaneData(final String id, final LinkData link)
        {
            this.id = id;
            this.link = link;
            link.lanes.add(this);
        }

        /** {@inheritDoc} */
        @Override
        public Length getLength()
        {
            return this.length;
        }

        /** {@inheritDoc} */
        @Override
        public LinkData getLinkData()
        {
            return this.link;
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return this.id;
        }

    }

    /** Helper class NodeData. */
    private static class NodeData implements NodeDataInterface
    {

        /** Node id. */
        private String id;

        /**
         * Constructor.
         * @param id String; id
         */
        NodeData(final String id)
        {
            this.id = id;
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return null;
        }

    }

    /** Helper class GtuTypeData. */
    private static class GtuTypeData implements GtuTypeDataInterface
    {

        /** Node id. */
        private String id;

        /**
         * Constructor.
         * @param id String; id
         */
        GtuTypeData(final String id)
        {
            this.id = id;
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return null;
        }

    }

    /** Helper class RouteData. */
    private static class RouteData implements RouteDataInterface
    {

        /** Node id. */
        private String id;

        /**
         * Constructor.
         * @param id String; id
         */
        RouteData(final String id)
        {
            this.id = id;
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return null;
        }

    }

    /** Helper class GtuData. */
    private static class GtuData implements GtuDataInterface
    {

        /** Id. */
        private final String id;

        /** Origin. */
        private final NodeData origin;

        /** Destination. */
        private final NodeData destination;

        /** GTU type. */
        private final GtuTypeData gtuType;

        /** Route. */
        private final RouteData route;

        /**
         * @param id String; id
         * @param origin NodeData; origin
         * @param destination NodeData; destination
         * @param gtuType GtuTypeData; GTU type
         * @param route RouteData; route
         */
        GtuData(final String id, final NodeData origin, final NodeData destination, final GtuTypeData gtuType,
                final RouteData route)
        {
            this.id = id;
            this.origin = origin;
            this.destination = destination;
            this.gtuType = gtuType;
            this.route = route;
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return this.id;
        }

        /** {@inheritDoc} */
        @Override
        public NodeDataInterface getOriginNodeData()
        {
            return this.origin;
        }

        /** {@inheritDoc} */
        @Override
        public NodeDataInterface getDestinationNodeData()
        {
            return this.destination;
        }

        /** {@inheritDoc} */
        @Override
        public GtuTypeDataInterface getGtuTypeData()
        {
            return this.gtuType;
        }

        /** {@inheritDoc} */
        @Override
        public RouteDataInterface getRouteData()
        {
            return this.route;
        }

    }

}
