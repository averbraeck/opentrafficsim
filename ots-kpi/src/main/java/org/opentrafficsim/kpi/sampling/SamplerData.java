package org.opentrafficsim.kpi.sampling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.IntStream;

import org.djunits.Throw;
import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.Unit;
import org.djunits.value.base.Scalar;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.scalar.FloatLength;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djutils.data.Column;
import org.djutils.data.Row;
import org.djutils.data.Table;
import org.djutils.data.csv.CsvData;
import org.djutils.data.serialization.TextSerializationException;
import org.djutils.io.CompressedFileWriter;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.FilterDataType;

/**
 * SamplerData is a storage for trajectory data. Adding trajectory groups can only be done by subclasses. This is however not a
 * guaranteed read-only class. Any type can obtain the lane directions and with those the coupled trajectory groups.
 * Trajectories can be added to these trajectory groups. Data can also be added to the trajectories themselves.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public class SamplerData<G extends GtuData> extends Table
{

    /** Base columns. */
    private static Collection<Column<?>> baseColumns = new LinkedHashSet<>();

    /** Extended data types, in order of relevant columns. */
    private final List<ExtendedDataType<?, ?, ?, G>> extendedDataTypes;

    /** Filter data types, in order of relevant columns. */
    private final List<FilterDataType<?>> filterDataTypes;

    /** Map with all sampling data. */
    private final Map<LaneData, TrajectoryGroup<G>> trajectories = new LinkedHashMap<>();

    static
    {
        baseColumns.add(new Column<>("traj#", "Trajectory number", Integer.class, null));
        baseColumns.add(new Column<>("linkId", "Link id", String.class, null));
        baseColumns.add(new Column<>("laneId", "Lane id", String.class, null));
        baseColumns.add(new Column<>("gtuId", "GTU id", String.class, null));
        baseColumns.add(new Column<>("t", "Simulation time", FloatDuration.class, DurationUnit.SI.getId()));
        baseColumns.add(new Column<>("x", "Position on the lane", FloatLength.class, LengthUnit.SI.getId()));
        baseColumns.add(new Column<>("v", "Speed", FloatSpeed.class, SpeedUnit.SI.getId()));
        baseColumns.add(new Column<>("a", "Acceleration", FloatAcceleration.class, AccelerationUnit.SI.getId()));
    }

    /**
     * Constructor.
     * @param extendedDataTypes Set&lt;? extends ExtendedDataType&lt;?, ?, ?, G&gt;&gt;; extended data types.
     * @param filterDataTypes Set&lt;FilterDataType&lt;?&gt;&gt;; filter data types.
     */
    public SamplerData(final Set<ExtendedDataType<?, ?, ?, G>> extendedDataTypes, final Set<FilterDataType<?>> filterDataTypes)
    {
        super("sampler", "Trajectory data", generateColumns(extendedDataTypes, filterDataTypes));
        /*
         * The delivered types may not have a consistent iteration order. We need to store them in a data structure that does.
         * The order in which we add them needs to be consistent with the columns generated, where we skip the 8 base columns.
         */
        this.extendedDataTypes = new ArrayList<>(extendedDataTypes.size());
        for (int i = 8; i < 8 + extendedDataTypes.size(); i++)
        {
            String columnId = getColumn(i).getId();
            for (ExtendedDataType<?, ?, ?, G> extendedDataType : extendedDataTypes)
            {
                if (extendedDataType.getId().equals(columnId))
                {
                    this.extendedDataTypes.add(extendedDataType);
                }
            }
        }
        this.filterDataTypes = new ArrayList<>(filterDataTypes.size());
        for (int i = 8 + extendedDataTypes.size(); i < 8 + extendedDataTypes.size() + filterDataTypes.size(); i++)
        {
            String columnId = getColumn(i).getId();
            for (FilterDataType<?> filterType : filterDataTypes)
            {
                if (filterType.getId().equals(columnId))
                {
                    this.filterDataTypes.add(filterType);
                }
            }
        }
    }

    /**
     * Generates the columns based on base information and the extended and filter types.
     * @param extendedDataTypes Set&lt;? extends ExtendedDataType&lt;?, ?, ?, ? extends GtuData&gt;&gt;; extended data types.
     * @param filterDataTypes Set&lt;FilterDataType&lt;?&gt;&gt;; filter data types.
     * @return Collection&lt;Column&lt;?&gt;&gt;; columns.
     */
    private static Collection<Column<?>> generateColumns(
            final Set<? extends ExtendedDataType<?, ?, ?, ? extends GtuData>> extendedDataTypes,
            final Set<FilterDataType<?>> filterDataTypes)
    {
        Collection<Column<?>> out = new ArrayList<>(baseColumns.size() + extendedDataTypes.size() + filterDataTypes.size());
        out.addAll(baseColumns);
        for (ExtendedDataType<?, ?, ?, ?> extendedDataType : extendedDataTypes)
        {
            out.add(new Column<>(extendedDataType.getId(), extendedDataType.getDescription(), extendedDataType.getType(),
                    getUnit(extendedDataType)));
        }
        for (FilterDataType<?> filterDataType : filterDataTypes)
        {
            out.add(new Column<>(filterDataType.getId(), filterDataType.getDescription(), String.class, null));
        }
        return out;
    }

    /**
     * Returns the unit for values in an extended data type.
     * @param extendedDataType ExtendedDataType&lt;?, ?, ?, ?&gt;; extended data type.
     * @return String; representation of the unit
     */
    private static String getUnit(final ExtendedDataType<?, ?, ?, ?> extendedDataType)
    {
        if (Scalar.class.isAssignableFrom(extendedDataType.getType()))
        {
            try
            {
                Class<?> unitClass = Class.forName(
                        "org.djunits.unit." + extendedDataType.getType().getSimpleName().replace("Float", "") + "Unit");
                return ((Unit<?>) unitClass.getDeclaredField("SI").get(unitClass)).getId();
            }
            catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                    | SecurityException exception)
            {
                return null;
            }
        }
        return null;
    }

    /**
     * Stores a trajectory group with the lane direction.
     * @param lane LaneData; lane direction
     * @param trajectoryGroup trajectory group for given lane direction
     */
    protected final void putTrajectoryGroup(final LaneData lane, final TrajectoryGroup<G> trajectoryGroup)
    {
        this.trajectories.put(lane, trajectoryGroup);
    }

    /**
     * Returns the set of lane directions.
     * @return Set&lt;LaneData&gt;; lane directions
     */
    public final Set<LaneData> getLanes()
    {
        return this.trajectories.keySet();
    }

    /**
     * Returns whether there is data for the give lane.
     * @param lane LaneData; lane
     * @return whether there is data for the give lane
     */
    public final boolean contains(final LaneData lane)
    {
        return this.trajectories.containsKey(lane);
    }

    /**
     * Returns the trajectory group of given lane.
     * @param lane LaneData; lane
     * @return trajectory group of given lane, {@code null} if none
     */
    public final TrajectoryGroup<G> getTrajectoryGroup(final LaneData lane)
    {
        return this.trajectories.get(lane);
    }

    /**
     * Write the contents of the sampler in to a file. By default this is zipped.
     * @param file String; file
     */
    public final void writeToFile(final String file)
    {
        writeToFile(file, Compression.ZIP);
    }

    /**
     * Write the contents of the sampler in to a file.
     * @param file String; file
     * @param compression Compression; how to compress the data
     */
    public final void writeToFile(final String file, final Compression compression)
    {
        try
        {
            if (compression.equals(Compression.ZIP))
            {
                String name = new File(file).getName();
                String csvName = name.toLowerCase().endsWith(".zip") ? name.substring(0, name.length() - 4) : name;
                CsvData.writeZippedData(new CompressedFileWriter(file), csvName, csvName + ".header", this);
            }
            else
            {
                CsvData.writeData(file, file + ".header", this);
            }
        }
        catch (IOException | TextSerializationException exception)
        {
            throw new RuntimeException("Unable to write sampler data.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Row> iterator()
    {
        return new SamplerDataIterator();
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

    /**
     * Iterator over the sampler data. It iterates over lanes, trajectories on a lane, and indices within the trajectory.
     * <p>
     * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private final class SamplerDataIterator implements Iterator<Row>
    {
        /** Iterator over the sampled lanes. */
        private Iterator<Entry<LaneData, TrajectoryGroup<G>>> laneIterator =
                SamplerData.this.trajectories.entrySet().iterator();

        /** Current lane. */
        private LaneData currentLane;

        /** Iterator over trajectories on a lane. */
        private Iterator<Trajectory<G>> trajectoryIterator = Collections.emptyIterator();

        /** Current trajectory. */
        private Trajectory<G> currentTrajectory;

        /** Size of current trajectory, to check concurrent modification. */
        private int currentTrajectorySize = 0;

        /** Trajectory counter (first column). */
        private int trajectoryCounter = 0;

        /** Iterator over indices in a trajectory. */
        private Iterator<Integer> indexIterator = Collections.emptyIterator();

        /** {@inheritDoc} */
        @Override
        public boolean hasNext()
        {
            while (!this.indexIterator.hasNext())
            {
                while (!this.trajectoryIterator.hasNext())
                {
                    if (!this.laneIterator.hasNext())
                    {
                        return false;
                    }
                    Entry<LaneData, TrajectoryGroup<G>> entry = this.laneIterator.next();
                    this.currentLane = entry.getKey();
                    this.trajectoryIterator = entry.getValue().iterator();
                }
                this.currentTrajectory = this.trajectoryIterator.next();
                this.currentTrajectorySize = this.currentTrajectory.size();
                this.trajectoryCounter++;
                this.indexIterator = IntStream.range(0, this.currentTrajectory.size()).iterator();
            }
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public Row next()
        {
            Throw.when(!hasNext(), NoSuchElementException.class, "Sampler data has no next row.");
            Throw.when(this.currentTrajectory.size() != this.currentTrajectorySize, ConcurrentModificationException.class,
                    "Trajectory modified while iterating.");

            int trajectoryIndex = this.indexIterator.next();
            try
            {
                // base data
                Object[] data = getBaseData(trajectoryIndex);
                int dataIndex = 8;

                // extended data
                for (int i = 0; i < SamplerData.this.extendedDataTypes.size(); i++)
                {
                    ExtendedDataType<?, ?, ?, G> extendedDataType = SamplerData.this.extendedDataTypes.get(i);
                    data[dataIndex++] = this.currentTrajectory.contains(extendedDataType)
                            ? this.currentTrajectory.getExtendedData(extendedDataType, trajectoryIndex) : null;
                }

                // filter data
                for (int i = 0; i < SamplerData.this.filterDataTypes.size(); i++)
                {
                    FilterDataType<?> filterDataType = SamplerData.this.filterDataTypes.get(i);
                    // filter data is only stored on the first index, as this data is fixed over a trajectory
                    data[dataIndex++] = trajectoryIndex == 0 && this.currentTrajectory.contains(filterDataType)
                            ? this.currentTrajectory.getFilterData(filterDataType) : null;
                }

                return new Row(SamplerData.this, data);
            }
            catch (SamplingException se)
            {
                throw new RuntimeException("Sampling exception during iteration over sampler data.", se);
            }
        }

        /**
         * Returns an array with the base data. The array is of size to also contain the extended and filter data.
         * @param trajectoryIndex int; trajectory index in the current trajectory.
         * @return Object[] base data of size to also contain the extended and filter data.
         * @throws SamplingException if data can not be obtained.
         */
        private Object[] getBaseData(final int trajectoryIndex) throws SamplingException
        {
            Object[] data = new Object[SamplerData.this.getNumberOfColumns()];
            int dataIndex = 0;
            for (Column<?> column : baseColumns)
            {
                switch (column.getId())
                {
                    case "traj#":
                        data[dataIndex] = this.trajectoryCounter;
                        break;
                    case "linkId":
                        data[dataIndex] = this.currentLane.getLinkData().getId();
                        break;
                    case "laneId":
                        data[dataIndex] = this.currentLane.getId();
                        break;
                    case "gtuId":
                        data[dataIndex] = this.currentTrajectory.getGtuId();
                        break;
                    case "t":
                        data[dataIndex] = FloatDuration.instantiateSI(this.currentTrajectory.getT(trajectoryIndex));
                        break;
                    case "x":
                        data[dataIndex] = FloatLength.instantiateSI(this.currentTrajectory.getX(trajectoryIndex));
                        break;
                    case "v":
                        data[dataIndex] = FloatSpeed.instantiateSI(this.currentTrajectory.getV(trajectoryIndex));
                        break;
                    case "a":
                        data[dataIndex] = FloatAcceleration.instantiateSI(this.currentTrajectory.getA(trajectoryIndex));
                        break;
                    default:
                        
                }
                dataIndex++;
            }
            return data;
        }
    }

    /**
     * Compression method.
     * <p>
     * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public enum Compression
    {
        /** No compression. */
        NONE,

        /** Zip compression. */
        ZIP
    }

}
