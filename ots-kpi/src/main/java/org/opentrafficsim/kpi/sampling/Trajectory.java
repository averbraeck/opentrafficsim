package org.opentrafficsim.kpi.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vfloat.vector.FloatAccelerationVector;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.djunits.value.vfloat.vector.FloatTimeVector;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.MetaData;
import org.opentrafficsim.kpi.sampling.meta.MetaDataType;

import nl.tudelft.simulation.language.Throw;

/**
 * Contains position, speed, acceleration and time data of a GTU, over some section. Position is relative to the start of the
 * lane in the direction of travel, also when trajectories have been truncated at a position x &gt; 0. Note that this regards
 * internal data and output. Input position always refers to the design line of the lane. This class internally flips input
 * positions and boundaries.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Trajectory
{

    /** Default array capacity. */
    private static final int DEFAULT_CAPACITY = 10;

    /** Effective length of the underlying data (arrays may be longer). */
    private int size = 0;

    /**
     * Position array. Position is relative to the start of the lane in the direction of travel, also when trajectories have
     * been truncated at a position x &gt; 0.
     */
    private float[] x = new float[DEFAULT_CAPACITY];

    /** Speed array. */
    private float[] v = new float[DEFAULT_CAPACITY];

    /** Acceleration array. */
    private float[] a = new float[DEFAULT_CAPACITY];

    /** Time array. */
    private float[] t = new float[DEFAULT_CAPACITY];

    /** GTU id. */
    private final String gtuId;

    /** Meta data. */
    private final MetaData metaData;

    /** Map of array data types and their values. */
    private final Map<ExtendedDataType<?>, List<Object>> extendedData = new HashMap<>();

    /** Direction of travel. */
    private final KpiLaneDirection kpiLaneDirection;

    /**
     * @param gtu GTU of this trajectory, only the id is stored.
     * @param metaData meta data
     * @param extendedData types of extended data
     * @param kpiLaneDirection direction of travel
     */
    public Trajectory(final GtuDataInterface gtu, final MetaData metaData, final Set<ExtendedDataType<?>> extendedData,
            final KpiLaneDirection kpiLaneDirection)
    {
        this(gtu == null ? null : gtu.getId(), metaData, extendedData, kpiLaneDirection);
    }

    /**
     * Private constructor for creating subsets.
     * @param gtuId GTU id
     * @param metaData meta data
     * @param extendedData types of extended data
     * @param kpiLaneDirection direction of travel
     */
    private Trajectory(final String gtuId, final MetaData metaData, final Set<ExtendedDataType<?>> extendedData,
            final KpiLaneDirection kpiLaneDirection)
    {
        Throw.whenNull(gtuId, "GTU may not be null.");
        Throw.whenNull(metaData, "Meta data may not be null.");
        Throw.whenNull(extendedData, "Extended data may not be null.");
        Throw.whenNull(kpiLaneDirection, "Lane direction may not be null.");
        this.gtuId = gtuId;
        this.metaData = new MetaData(metaData);
        for (ExtendedDataType<?> dataType : extendedData)
        {
            this.extendedData.put(dataType, new ArrayList<>());
        }
        this.kpiLaneDirection = kpiLaneDirection;
    }

    /**
     * Adds values of position, speed, acceleration and time.
     * @param position position is relative to the start of the lane in the direction of the design line, i.e. irrespective of
     *            the travel direction, also when trajectories have been truncated at a position x &gt; 0
     * @param speed speed
     * @param acceleration acceleration
     * @param time time
     */
    public void add(final Length position, final Speed speed, final Acceleration acceleration, final Time time)
    {
        add(position, speed, acceleration, time, null);
    }

    /**
     * Adds values of position, speed, acceleration and time.
     * @param position position is relative to the start of the lane in the direction of the design line, i.e. irrespective of
     *            the travel direction, also when trajectories have been truncated at a position x &gt; 0
     * @param speed speed
     * @param acceleration acceleration
     * @param time time
     * @param gtu gtu to add extended data for
     */
    public void add(final Length position, final Speed speed, final Acceleration acceleration, final Time time,
            final GtuDataInterface gtu)
    {
        Throw.whenNull(position, "Position may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        Throw.whenNull(gtu, "GTU may not be null.");
        if (this.size == this.x.length)
        {
            int cap = this.size + (this.size >> 1);
            this.x = Arrays.copyOf(this.x, cap);
            this.v = Arrays.copyOf(this.v, cap);
            this.a = Arrays.copyOf(this.a, cap);
            this.t = Arrays.copyOf(this.t, cap);
        }
        this.x[this.size] = (float) this.kpiLaneDirection.getPositionInDirection(position).si;
        this.v[this.size] = (float) speed.si;
        this.a[this.size] = (float) acceleration.si;
        this.t[this.size] = (float) time.si;
        this.size++;
        if (gtu != null)
        {
            for (ExtendedDataType<?> extendedDataType : this.extendedData.keySet())
            {
                this.extendedData.get(extendedDataType).add(this.size - 1, extendedDataType.getValue(gtu));
            }
        }
        else
        {
            for (ExtendedDataType<?> extendedDataType : this.extendedData.keySet())
            {
                this.extendedData.get(extendedDataType).add(this.size - 1, null);
            }
        }
    }

    /**
     * @return size of the underlying trajectory data
     */
    public int size()
    {
        return this.size;
    }

    /**
     * @return GTU id
     */
    public String getGtuId()
    {
        return this.gtuId;
    }

    /**
     * @return si position values, position is relative to the start of the lane, also when trajectories have been truncated at
     *         a position x &gt; 0
     */
    public float[] getX()
    {
        return Arrays.copyOf(this.x, this.size);
    }

    /**
     * @return si speed values
     */
    public float[] getV()
    {
        return Arrays.copyOf(this.v, this.size);
    }

    /**
     * @return si acceleration values
     */
    public float[] getA()
    {
        return Arrays.copyOf(this.a, this.size);
    }

    /**
     * @return si time values
     */
    public float[] getT()
    {
        return Arrays.copyOf(this.t, this.size);
    }

    /**
     * @return strongly typed copy of position, position is relative to the start of the lane, also when trajectories have been
     *         truncated at a position x &gt; 0
     */
    public FloatLengthVector getPosition()
    {
        try
        {
            return new FloatLengthVector(this.x, LengthUnit.SI, StorageType.DENSE);
        }
        catch (ValueException exception)
        {
            // should not happen, inputs are not null
            throw new RuntimeException("Could not return trajectory data.", exception);
        }
    }

    /**
     * @return strongly typed copy of speed
     */
    public FloatSpeedVector getSpeed()
    {
        try
        {
            return new FloatSpeedVector(this.v, SpeedUnit.SI, StorageType.DENSE);
        }
        catch (ValueException exception)
        {
            // should not happen, inputs are not null
            throw new RuntimeException("Could not return trajectory data.", exception);
        }
    }

    /**
     * @return strongly typed copy of acceleration
     */
    public FloatAccelerationVector getAcceleration()
    {
        try
        {
            return new FloatAccelerationVector(this.a, AccelerationUnit.SI, StorageType.DENSE);
        }
        catch (ValueException exception)
        {
            // should not happen, inputs are not null
            throw new RuntimeException("Could not return trajectory data.", exception);
        }
    }

    /**
     * @return strongly typed copy of time
     */
    public FloatTimeVector getTime()
    {
        try
        {
            return new FloatTimeVector(this.t, TimeUnit.SI, StorageType.DENSE);
        }
        catch (ValueException exception)
        {
            // should not happen, inputs are not null
            throw new RuntimeException("Could not return trajectory data.", exception);
        }
    }

    /**
     * @return total length of this trajectory
     * @throws IllegalStateException if trajectory is empty
     */
    public Length getTotalLength()
    {
        // TODO do not allow empty trajectory
        // Throw.when(this.size == 0, IllegalStateException.class, "Empty trajectory does not have a length.");
        if (this.size == 0)
        {
            return Length.ZERO;
        }
        return new Length(this.x[this.size - 1] - this.x[0], LengthUnit.SI);
    }

    /**
     * @return total duration of this trajectory
     * @throws IllegalStateException if trajectory is empty
     */
    public Duration getTotalDuration()
    {
        // TODO do not allow empty trajectory
        // Throw.when(this.size == 0, IllegalStateException.class, "Empty trajectory does not have a duration.");
        if (this.size == 0)
        {
            return Duration.ZERO;
        }
        return new Duration(this.t[this.size - 1] - this.t[0], TimeUnit.SI);
    }

    /**
     * @param metaDataType meta data type
     * @return whether the trajectory contains the meta data of give type
     */
    public boolean contains(final MetaDataType<?> metaDataType)
    {
        return this.metaData.contains(metaDataType);
    }

    /**
     * @param metaDataType meta data type
     * @param <T> class of meta data
     * @return value of meta data
     */
    public <T> T getMetaData(final MetaDataType<T> metaDataType)
    {
        return this.metaData.get(metaDataType);
    }

    /**
     * @param extendedDataType extended data type
     * @return whether the trajectory contains the extended data of give type
     */
    public boolean contains(final ExtendedDataType<?> extendedDataType)
    {
        return this.extendedData.containsKey(extendedDataType);
    }

    /**
     * @param extendedDataType extended data type to return
     * @param <T> value type of the extended data type
     * @return values of extended data type
     * @throws SamplingException if the extended data type is not in the trajectory
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getExtendedData(final ExtendedDataType<T> extendedDataType) throws SamplingException
    {
        Throw.when(!this.extendedData.containsKey(extendedDataType), SamplingException.class,
                "Extended data type %s is not in the trajectory.", extendedDataType);
        return (List<T>) this.extendedData.get(extendedDataType);
    }

    /**
     * Copies the trajectory but with a subset of the data. Longitudinal entry is only true if the original trajectory has true,
     * and the subset is from the start.
     * @param startPosition start position
     * @param endPosition end position
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minLength is smaller than maxLength
     */
    public Trajectory subSet(final Length startPosition, final Length endPosition)
    {
        Throw.whenNull(startPosition, "Start position may not be null");
        Throw.whenNull(endPosition, "End position may not be null");
        Length length0 = this.kpiLaneDirection.getPositionInDirection(startPosition);
        Length length1 = this.kpiLaneDirection.getPositionInDirection(endPosition);
        Throw.when(length0.gt(length1), IllegalArgumentException.class,
                "Start position should be smaller than end position in the direction of travel");
        return subSet(spaceBoundaries(length0, length1));
    }

    /**
     * Copies the trajectory but with a subset of the data. Longitudinal entry is only true if the original trajectory has true,
     * and the subset is from the start.
     * @param startTime start time
     * @param endTime end time
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minTime is smaller than maxTime
     */
    public Trajectory subSet(final Time startTime, final Time endTime)
    {
        Throw.whenNull(startTime, "Start time may not be null");
        Throw.whenNull(endTime, "End time may not be null");
        Throw.when(startTime.gt(endTime), IllegalArgumentException.class, "Start time should be smaller than end time.");
        return subSet(timeBoundaries(startTime, endTime));
    }

    /**
     * Copies the trajectory but with a subset of the data. Longitudinal entry is only true if the original trajectory has true,
     * and the subset is from the start.
     * @param startPosition start position
     * @param endPosition end position
     * @param startTime start time
     * @param endTime end time
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minLength/Time is smaller than maxLength/Time
     */
    public Trajectory subSet(final Length startPosition, final Length endPosition, final Time startTime, final Time endTime)
    {
        // could use this.subSet(minLength, maxLength).subSet(minTime, maxTime), but that copies twice
        Throw.whenNull(startPosition, "Start position may not be null");
        Throw.whenNull(endPosition, "End position may not be null");
        Length length0 = this.kpiLaneDirection.getPositionInDirection(startPosition);
        Length length1 = this.kpiLaneDirection.getPositionInDirection(endPosition);
        Throw.when(length0.gt(length1), IllegalArgumentException.class,
                "Start position should be smaller than end position in the direction of travel");
        Throw.whenNull(startTime, "Start time may not be null");
        Throw.whenNull(endTime, "End time may not be null");
        Throw.when(startTime.gt(endTime), IllegalArgumentException.class, "Start time should be smaller than end time.");
        return subSet(spaceBoundaries(length0, length1).intersect(timeBoundaries(startTime, endTime)));
    }

    /**
     * Determine spatial boundaries.
     * @param startPosition start position
     * @param endPosition end position
     * @return spatial boundaries
     */
    private Boundaries spaceBoundaries(final Length startPosition, final Length endPosition)
    {
        int from = 0;
        double fFrom = 0;
        while (startPosition.si > this.x[from + 1] && from < this.size - 1)
        {
            from++;
        }
        if (this.x[from] < startPosition.si)
        {
            fFrom = (startPosition.si - this.x[from]) / (this.x[from + 1] - this.x[from]);
        }
        int to = this.size - 1;
        double fTo = 0;
        while (endPosition.si < this.x[to] && to > 0)
        {
            to--;
        }
        if (to < this.size - 1)
        {
            fTo = (endPosition.si - this.x[to]) / (this.x[to + 1] - this.x[to]);
        }
        return new Boundaries(from, fFrom, to, fTo);
    }

    /**
     * Determine temporal boundaries.
     * @param startTime start time
     * @param endTime end time
     * @return spatial boundaries
     */
    private Boundaries timeBoundaries(final Time startTime, final Time endTime)
    {
        int from = 0;
        double fFrom = 0;
        while (startTime.si > this.t[from + 1] && from < this.size - 1)
        {
            from++;
        }
        if (this.t[from] < startTime.si)
        {
            fFrom = (startTime.si - this.t[from]) / (this.t[from + 1] - this.t[from]);
        }
        int to = this.size - 1;
        double fTo = 0;
        while (endTime.si < this.t[to] && to > 0)
        {
            to--;
        }
        if (to < this.size - 1)
        {
            fTo = (endTime.si - this.t[to]) / (this.t[to + 1] - this.t[to]);
        }
        return new Boundaries(from, fFrom, to, fTo);
    }

    /**
     * Copies the trajectory but with a subset of the data. Data is taken from position (from + fFrom) to (to + fTo).
     * @param bounds boundaries
     * @param <T> type of underlying extended data value
     * @return subset of the trajectory
     */
    @SuppressWarnings("unchecked")
    private <T> Trajectory subSet(final Boundaries bounds)
    {
        Trajectory out = new Trajectory(this.gtuId, this.metaData, this.extendedData.keySet(), this.kpiLaneDirection);
        if (bounds.from < bounds.to) // otherwise empty, no data in the subset
        {
            int nBefore = bounds.fFrom < 1.0 ? 1 : 0;
            int nAfter = bounds.fTo > 0.0 ? 1 : 0;
            int n = bounds.to - bounds.from + nBefore + nAfter;
            out.x = new float[n];
            out.v = new float[n];
            out.a = new float[n];
            out.t = new float[n];
            System.arraycopy(this.x, bounds.from + 1, out.x, nBefore, bounds.to - bounds.from);
            System.arraycopy(this.v, bounds.from + 1, out.v, nBefore, bounds.to - bounds.from);
            System.arraycopy(this.a, bounds.from + 1, out.a, nBefore, bounds.to - bounds.from);
            System.arraycopy(this.t, bounds.from + 1, out.t, nBefore, bounds.to - bounds.from);
            if (nBefore == 1)
            {
                out.x[0] = (float) (this.x[bounds.from] * (1 - bounds.fFrom) + this.x[bounds.from + 1] * bounds.fFrom);
                out.v[0] = (float) (this.v[bounds.from] * (1 - bounds.fFrom) + this.v[bounds.from + 1] * bounds.fFrom);
                out.a[0] = (float) (this.a[bounds.from] * (1 - bounds.fFrom) + this.a[bounds.from + 1] * bounds.fFrom);
                out.t[0] = (float) (this.t[bounds.from] * (1 - bounds.fFrom) + this.t[bounds.from + 1] * bounds.fFrom);
            }
            if (nAfter == 1)
            {
                out.x[n - 1] = (float) (this.x[bounds.to] * (1 - bounds.fTo) + this.x[bounds.to + 1] * bounds.fTo);
                out.v[n - 1] = (float) (this.v[bounds.to] * (1 - bounds.fTo) + this.v[bounds.to + 1] * bounds.fTo);
                out.a[n - 1] = (float) (this.a[bounds.to] * (1 - bounds.fTo) + this.a[bounds.to + 1] * bounds.fTo);
                out.t[n - 1] = (float) (this.t[bounds.to] * (1 - bounds.fTo) + this.t[bounds.to + 1] * bounds.fTo);
            }
            out.size = n;
            for (ExtendedDataType<?> extendedDataType : this.extendedData.keySet())
            {
                List<Object> fromList = this.extendedData.get(extendedDataType);
                List<Object> toList = new ArrayList<>();
                if (nBefore == 1)
                {
                    toList.add(((ExtendedDataType<T>) extendedDataType).interpolate((T) fromList.get(bounds.from),
                            (T) fromList.get(bounds.from + 1), bounds.fFrom));
                }
                for (int i = bounds.from + 1; i < bounds.to; i++)
                {
                    toList.add(fromList.get(i));
                }
                if (nAfter == 1)
                {
                    toList.add(((ExtendedDataType<T>) extendedDataType).interpolate((T) fromList.get(bounds.to),
                            (T) fromList.get(bounds.to + 1), bounds.fTo));
                }
                out.extendedData.put(extendedDataType, toList);
            }
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.a);
        result = prime * result + ((this.extendedData == null) ? 0 : this.extendedData.hashCode());
        result = prime * result + ((this.gtuId == null) ? 0 : this.gtuId.hashCode());
        result = prime * result + ((this.metaData == null) ? 0 : this.metaData.hashCode());
        result = prime * result + this.size;
        result = prime * result + Arrays.hashCode(this.t);
        result = prime * result + Arrays.hashCode(this.v);
        result = prime * result + Arrays.hashCode(this.x);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Trajectory other = (Trajectory) obj;
        if (!Arrays.equals(this.a, other.a))
        {
            return false;
        }
        if (this.extendedData == null)
        {
            if (other.extendedData != null)
            {
                return false;
            }
        }
        else if (!this.extendedData.equals(other.extendedData))
        {
            return false;
        }
        if (this.gtuId == null)
        {
            if (other.gtuId != null)
            {
                return false;
            }
        }
        else if (!this.gtuId.equals(other.gtuId))
        {
            return false;
        }
        if (this.metaData == null)
        {
            if (other.metaData != null)
            {
                return false;
            }
        }
        else if (!this.metaData.equals(other.metaData))
        {
            return false;
        }
        if (this.size != other.size)
        {
            return false;
        }
        if (!Arrays.equals(this.t, other.t))
        {
            return false;
        }
        if (!Arrays.equals(this.v, other.v))
        {
            return false;
        }
        if (!Arrays.equals(this.x, other.x))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        if (this.size > 0)
        {
            return "Trajectory [size=" + this.size + ", x={" + this.x[0] + "..." + this.x[this.size - 1] + "}, t={" + this.t[0]
                    + "..." + this.t[this.size - 1] + "}, metaData=" + this.metaData + ", gtuId=" + this.gtuId + "]";
        }
        return "Trajectory [size=" + this.size + ", x={}, t={}, metaData=" + this.metaData + ", gtuId=" + this.gtuId + "]";
    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 okt. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class Boundaries
    {
        /** Rounded-down from-index. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public final int from;

        /** Fraction of to-index. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public final double fFrom;

        /** Rounded-down to-index. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public final int to;

        /** Fraction of to-index. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public final double fTo;

        /**
         * @param from from index, rounded down
         * @param fFrom from index, fraction
         * @param to to index, rounded down
         * @param fTo to index, fraction
         */
        Boundaries(final int from, final double fFrom, final int to, final double fTo)
        {
            Throw.when(from < 0 || from > Trajectory.this.size() - 1, IllegalArgumentException.class,
                    "Argument from (%d) is out of bounds.", from);
            Throw.when(fFrom < 0 || fFrom > 1, IllegalArgumentException.class, "Argument fFrom (%f) is out of bounds.", fFrom);
            Throw.when(from == Trajectory.this.size() && fFrom > 0, IllegalArgumentException.class,
                    "Arguments from (%d) and fFrom (%f) are out of bounds.", from, fFrom);
            Throw.when(to < 0 || to >= Trajectory.this.size(), IllegalArgumentException.class,
                    "Argument to (%d) is out of bounds.", to);
            Throw.when(fTo < 0 || fTo > 1, IllegalArgumentException.class, "Argument fTo (%f) is out of bounds.", fTo);
            Throw.when(to == Trajectory.this.size() && fTo > 0, IllegalArgumentException.class,
                    "Arguments to (%d) and fTo (%f) are out of bounds.", to, fTo);
            this.from = from;
            this.fFrom = fFrom;
            this.to = to;
            this.fTo = fTo;
        }

        /**
         * @param boundaries boundaries
         * @return intersection of both boundaries
         */
        public Boundaries intersect(final Boundaries boundaries)
        {
            int newFrom;
            double newFFrom;
            if (this.from > boundaries.from || this.from == boundaries.from && this.fFrom > boundaries.fFrom)
            {
                newFrom = this.from;
                newFFrom = this.fFrom;
            }
            else
            {
                newFrom = boundaries.from;
                newFFrom = boundaries.fFrom;
            }
            int newTo;
            double newFTo;
            if (this.to < boundaries.to || this.to == boundaries.to && this.fTo < boundaries.fTo)
            {
                newTo = this.to;
                newFTo = this.fTo;
            }
            else
            {
                newTo = boundaries.to;
                newFTo = boundaries.fTo;
            }
            return new Boundaries(newFrom, newFFrom, newTo, newFTo);
        }

    }

}
