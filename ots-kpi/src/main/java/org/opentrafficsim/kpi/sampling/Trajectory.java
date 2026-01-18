package org.opentrafficsim.kpi.sampling;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vfloat.vector.FloatAccelerationVector;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.djunits.value.vfloat.vector.FloatTimeVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.filter.FilterDataType;

/**
 * Contains position, speed, acceleration and time data of a GTU, over some section. Position is relative to the start of the
 * lane in the direction of travel.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> GTU data type
 */
public final class Trajectory<G extends GtuData>
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

    /** GTU type id. */
    private final String gtuTypeId;

    /** Filter data. */
    private final Map<FilterDataType<?, ? super G>, Object> filterData = new LinkedHashMap<>();

    /** Map of extended data types and their values (usually arrays). */
    private final Map<ExtendedDataType<?, ?, ?, ? super G>, Object> extendedData = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param gtu GTU of this trajectory, only the id is stored.
     * @param filterData filter data
     * @param extendedData types of extended data
     */
    public Trajectory(final GtuData gtu, final Map<FilterDataType<?, ? super G>, Object> filterData,
            final Set<ExtendedDataType<?, ?, ?, ? super G>> extendedData)
    {
        this(gtu == null ? null : gtu.getId(), gtu == null ? null : gtu.getGtuTypeId(), filterData, extendedData);
    }

    /**
     * Private constructor for creating subsets.
     * @param gtuId GTU id
     * @param gtuTypeId GTU type id
     * @param filterData filter data
     * @param extendedData types of extended data
     */
    private Trajectory(final String gtuId, final String gtuTypeId, final Map<FilterDataType<?, ? super G>, Object> filterData,
            final Set<ExtendedDataType<?, ?, ?, ? super G>> extendedData)
    {
        Throw.whenNull(gtuId, "GTU id may not be null.");
        Throw.whenNull(gtuTypeId, "GTU type id may not be null.");
        Throw.whenNull(filterData, "Filter data may not be null.");
        Throw.whenNull(extendedData, "Extended data may not be null.");
        this.gtuId = gtuId;
        this.gtuTypeId = gtuTypeId;
        this.filterData.putAll(filterData);
        for (ExtendedDataType<?, ?, ?, ? super G> dataType : extendedData)
        {
            this.extendedData.put(dataType, dataType.initializeStorage());
        }
    }

    /**
     * Adds values of position, speed, acceleration and time.
     * @param position position is relative to the start of the lane in the direction of the design line, i.e. irrespective of
     *            the travel direction, also when trajectories have been truncated at a position x &gt; 0
     * @param speed speed
     * @param acceleration acceleration
     * @param time time
     */
    public void add(final Length position, final Speed speed, final Acceleration acceleration, final Duration time)
    {
        add(position, speed, acceleration, time, null);
    }

    /**
     * Adds values of position, speed, acceleration, time and extended data.
     * @param position position is relative to the start of the lane in the direction of the design line
     * @param speed speed
     * @param acceleration acceleration
     * @param time time
     * @param gtu gtu to add extended data for
     */
    public void add(final Length position, final Speed speed, final Acceleration acceleration, final Duration time, final G gtu)
    {
        Throw.whenNull(position, "Position may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        if (!this.extendedData.isEmpty())
        {
            Throw.whenNull(gtu, "GTU may not be null when extended data is part of the trajectory.");
        }
        if (this.size == this.x.length)
        {
            int cap = this.size + (this.size >> 1);
            this.x = Arrays.copyOf(this.x, cap);
            this.v = Arrays.copyOf(this.v, cap);
            this.a = Arrays.copyOf(this.a, cap);
            this.t = Arrays.copyOf(this.t, cap);
        }
        this.x[this.size] = (float) position.si;
        this.v[this.size] = (float) speed.si;
        this.a[this.size] = (float) acceleration.si;
        this.t[this.size] = (float) time.si;
        for (ExtendedDataType<?, ?, ?, ? super G> extendedDataType : this.extendedData.keySet())
        {
            appendValue(extendedDataType, gtu);
        }
        this.size++;
    }

    /**
     * Append value of the extended data type.
     * @param extendedDataType extended data type
     * @param gtu gtu
     * @param <T> extended data value type
     * @param <S> extended data storage data type
     */
    @SuppressWarnings("unchecked")
    private <T, S> void appendValue(final ExtendedDataType<T, ?, S, ? super G> extendedDataType, final G gtu)
    {
        S in = (S) this.extendedData.get(extendedDataType);
        S out = extendedDataType.setValue(in, this.size, extendedDataType.getValue(gtu).get());
        if (in != out)
        {
            this.extendedData.put(extendedDataType, out);
        }
    }

    /**
     * The size of the underlying data.
     * @return size of the underlying trajectory data
     */
    public int size()
    {
        return this.size;
    }

    /**
     * Returns the GTU id.
     * @return GTU id
     */
    public String getGtuId()
    {
        return this.gtuId;
    }

    /**
     * Returns the GTU type id.
     * @return GTU type id
     */
    public String getGtuTypeId()
    {
        return this.gtuTypeId;
    }

    /**
     * Returns the position array.
     * @return si position values.
     */
    public float[] getX()
    {
        return Arrays.copyOf(this.x, this.size);
    }

    /**
     * Returns the speed array.
     * @return si speed values
     */
    public float[] getV()
    {
        return Arrays.copyOf(this.v, this.size);
    }

    /**
     * Returns the acceleration array.
     * @return si acceleration values
     */
    public float[] getA()
    {
        return Arrays.copyOf(this.a, this.size);
    }

    /**
     * Returns the time array.
     * @return si time values
     */
    public float[] getT()
    {
        return Arrays.copyOf(this.t, this.size);
    }

    /**
     * Returns the last index with a position smaller than or equal to the given position.
     * @param position position
     * @return last index with a position smaller than or equal to the given position
     */
    public int binarySearchX(final float position)
    {
        if (this.x[0] >= position)
        {
            return 0;
        }
        int index = Arrays.binarySearch(this.x, 0, this.size, position);
        return index < 0 ? -index - 2 : index;
    }

    /**
     * Returns the last index with a time smaller than or equal to the given time.
     * @param time time
     * @return last index with a time smaller than or equal to the given time
     */
    public int binarySearchT(final float time)
    {
        if (this.t[0] >= time)
        {
            return 0;
        }
        int index = Arrays.binarySearch(this.t, 0, this.size, time);
        return index < 0 ? -index - 2 : index;
    }

    /**
     * Returns {@code x} value of a single sample.
     * @param index index
     * @return {@code x} value of a single sample
     */
    public float getX(final int index)
    {
        checkSample(index);
        return this.x[index];
    }

    /**
     * Returns {@code v} value of a single sample.
     * @param index index
     * @return {@code v} value of a single sample
     */
    public float getV(final int index)
    {
        checkSample(index);
        return this.v[index];
    }

    /**
     * Returns {@code a} value of a single sample.
     * @param index index
     * @return {@code a} value of a single sample
     */
    public float getA(final int index)
    {
        checkSample(index);
        return this.a[index];
    }

    /**
     * Returns {@code t} value of a single sample.
     * @param index index
     * @return {@code t} value of a single sample
     */
    public float getT(final int index)
    {
        checkSample(index);
        return this.t[index];
    }

    /**
     * Returns extended data type value of a single sample.
     * @param extendedDataType data type from which to retrieve the data
     * @param index index for which to retrieve the data
     * @param <T> scalar type of extended data type
     * @param <S> storage type of extended data type
     * @return extended data type value of a single sample
     */
    @SuppressWarnings("unchecked")
    public <T, S> T getExtendedData(final ExtendedDataType<T, ?, S, ?> extendedDataType, final int index)
    {
        checkSample(index);
        return extendedDataType.getStorageValue((S) this.extendedData.get(extendedDataType), index);
    }

    /**
     * Throws an exception if the sample index is out of bounds.
     * @param index sample index
     */
    private void checkSample(final int index)
    {
        Throw.when(index < 0 || index >= this.size, IndexOutOfBoundsException.class, "Index is out of bounds.");
    }

    /**
     * Returns strongly type position array.
     * @return strongly typed position array.
     */
    public FloatLengthVector getPosition()
    {
        return new FloatLengthVector(getX(), LengthUnit.SI);
    }

    /**
     * Returns strongly typed speed array.
     * @return strongly typed speed array.
     */
    public FloatSpeedVector getSpeed()
    {
        return new FloatSpeedVector(getV(), SpeedUnit.SI);
    }

    /**
     * Returns strongly typed acceleration array.
     * @return strongly typed acceleration array.
     */
    public FloatAccelerationVector getAcceleration()
    {
        return new FloatAccelerationVector(getA(), AccelerationUnit.SI);
    }

    /**
     * Returns strongly typed time array.
     * @return strongly typed time array.
     */
    public FloatTimeVector getTime()
    {
        return new FloatTimeVector(getT(), TimeUnit.BASE_SECOND);
    }

    /**
     * Returns the length of the data.
     * @return total length of this trajectory
     */
    public Length getTotalLength()
    {
        if (this.size < 2)
        {
            return Length.ZERO;
        }
        return new Length(this.x[this.size - 1] - this.x[0], LengthUnit.SI);
    }

    /**
     * Returns the total duration span.
     * @return total duration of this trajectory
     */
    public Duration getTotalDuration()
    {
        if (this.size < 2)
        {
            return Duration.ZERO;
        }
        return new Duration(this.t[this.size - 1] - this.t[0], DurationUnit.SI);
    }

    /**
     * Returns whether the filter data is contained.
     * @param filterDataType filter data type
     * @return whether the trajectory contains the filter data of give type
     */
    public boolean contains(final FilterDataType<?, ?> filterDataType)
    {
        return this.filterData.containsKey(filterDataType);
    }

    /**
     * Returns the value of the filter data.
     * @param filterDataType filter data type
     * @param <T> class of filter data
     * @return value of filter data
     */
    @SuppressWarnings("unchecked")
    public <T> T getFilterData(final FilterDataType<T, ?> filterDataType)
    {
        return (T) this.filterData.get(filterDataType);
    }

    /**
     * Returns the included filter data types.
     * @return included filter data types
     */
    public Set<FilterDataType<?, ? super G>> getFilterDataTypes()
    {
        return this.filterData.keySet();
    }

    /**
     * Returns whether ths extended data type is contained.
     * @param extendedDataType extended data type
     * @return whether the trajectory contains the extended data of give type
     */
    public boolean contains(final ExtendedDataType<?, ?, ?, ?> extendedDataType)
    {
        return this.extendedData.containsKey(extendedDataType);
    }

    /**
     * Returns the output data of the extended data type.
     * @param extendedDataType extended data type to return
     * @param <O> output type
     * @param <S> storage type
     * @return values of extended data type
     * @throws SamplingException if the extended data type is not in the trajectory
     */
    @SuppressWarnings("unchecked")
    public <O, S> O getExtendedData(final ExtendedDataType<?, O, S, ?> extendedDataType) throws SamplingException
    {
        Throw.when(!this.extendedData.containsKey(extendedDataType), SamplingException.class,
                "Extended data type %s is not in the trajectory.", extendedDataType);
        return extendedDataType.convert((S) this.extendedData.get(extendedDataType), this.size);
    }

    /**
     * Returns the included extended data types.
     * @return included extended data types
     */
    public Set<ExtendedDataType<?, ?, ?, ? super G>> getExtendedDataTypes()
    {
        return this.extendedData.keySet();
    }

    /**
     * Returns a space-time view of this trajectory. This is much more efficient than {@code getX()} as no array is copied. The
     * limitation is that only distance and time (and mean speed) in the space-time view can be obtained.
     * @return space-time view of this trajectory
     */
    public SpaceTimeView getSpaceTimeView()
    {
        if (size() < 2)
        {
            return new SpaceTimeView(Length.ZERO, Duration.ZERO);
        }
        return new SpaceTimeView(Length.ofSI(this.x[this.size - 1] - this.x[0]),
                Duration.ofSI(this.t[this.size - 1] - this.t[0]));
    }

    /**
     * Returns a space-time view of this trajectory as contained within the defined space-time region. This is much more
     * efficient than {@code subSet()} as no trajectory is copied. The limitation is that only distance and time (and mean
     * speed) in the space-time view can be obtained.
     * @param startPosition start position
     * @param endPosition end position
     * @param startTime start time
     * @param endTime end time
     * @return space-time view of this trajectory
     */
    public SpaceTimeView getSpaceTimeView(final Length startPosition, final Length endPosition, final Duration startTime,
            final Duration endTime)
    {
        if (size() < 2)
        {
            return new SpaceTimeView(Length.ZERO, Duration.ZERO);
        }
        Boundaries bounds = spaceBoundaries(startPosition, endPosition).intersect(timeBoundaries(startTime, endTime));
        double xFrom;
        double tFrom;
        if (bounds.fFrom > 0.0)
        {
            xFrom = this.x[bounds.from] * (1 - bounds.fFrom) + this.x[bounds.from + 1] * bounds.fFrom;
            tFrom = this.t[bounds.from] * (1 - bounds.fFrom) + this.t[bounds.from + 1] * bounds.fFrom;
        }
        else
        {
            xFrom = this.x[bounds.from];
            tFrom = this.t[bounds.from];
        }
        double xTo;
        double tTo;
        if (bounds.fTo > 0.0)
        {
            xTo = this.x[bounds.to] * (1 - bounds.fTo) + this.x[bounds.to + 1] * bounds.fTo;
            tTo = this.t[bounds.to] * (1 - bounds.fTo) + this.t[bounds.to + 1] * bounds.fTo;
        }
        else
        {
            xTo = this.x[bounds.to];
            tTo = this.t[bounds.to];
        }
        return new SpaceTimeView(Length.ofSI(xTo - xFrom), Duration.ofSI(tTo - tFrom));
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
    public Trajectory<G> subSet(final Length startPosition, final Length endPosition)
    {
        Throw.whenNull(startPosition, "Start position may not be null");
        Throw.whenNull(endPosition, "End position may not be null");
        Throw.when(startPosition.gt(endPosition), IllegalArgumentException.class,
                "Start position should be smaller than end position in the direction of travel");
        if (this.size == 0)
        {
            return new Trajectory<>(this.gtuId, this.gtuTypeId, this.filterData, this.extendedData.keySet());
        }
        return subSet(spaceBoundaries(startPosition, endPosition));
    }

    /**
     * Copies the trajectory but with a subset of the data.
     * @param startTime start time
     * @param endTime end time
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minTime is smaller than maxTime
     */
    public Trajectory<G> subSet(final Duration startTime, final Duration endTime)
    {
        Throw.whenNull(startTime, "Start time may not be null");
        Throw.whenNull(endTime, "End time may not be null");
        Throw.when(startTime.gt(endTime), IllegalArgumentException.class, "Start time should be smaller than end time.");
        if (this.size == 0)
        {
            return new Trajectory<>(this.gtuId, this.gtuTypeId, this.filterData, this.extendedData.keySet());
        }
        return subSet(timeBoundaries(startTime, endTime));
    }

    /**
     * Copies the trajectory but with a subset of the data that is contained in the given space-time region.
     * @param startPosition start position
     * @param endPosition end position
     * @param startTime start time
     * @param endTime end time
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minLength/Time is smaller than maxLength/Time
     */
    public Trajectory<G> subSet(final Length startPosition, final Length endPosition, final Duration startTime,
            final Duration endTime)
    {
        // could use this.subSet(minLength, maxLength).subSet(minTime, maxTime), but that copies twice
        Throw.whenNull(startPosition, "Start position may not be null");
        Throw.whenNull(endPosition, "End position may not be null");
        Throw.when(startPosition.gt(endPosition), IllegalArgumentException.class,
                "Start position should be smaller than end position in the direction of travel");
        Throw.whenNull(startTime, "Start time may not be null");
        Throw.whenNull(endTime, "End time may not be null");
        Throw.when(startTime.gt(endTime), IllegalArgumentException.class, "Start time should be smaller than end time.");
        if (this.size == 0)
        {
            return new Trajectory<>(this.gtuId, this.gtuTypeId, this.filterData, this.extendedData.keySet());
        }
        return subSet(spaceBoundaries(startPosition, endPosition).intersect(timeBoundaries(startTime, endTime)));
    }

    /**
     * Determine spatial boundaries.
     * @param startPosition start position
     * @param endPosition end position
     * @return spatial boundaries
     */
    private Boundaries spaceBoundaries(final Length startPosition, final Length endPosition)
    {
        if (startPosition.si > this.x[this.size - 1] || endPosition.si < this.x[0])
        {
            return new Boundaries(0, 0.0, 0, 0.0);
        }
        // to float needed as x is in floats and due to precision fTo > 1 may become true
        float startPos = (float) startPosition.si;
        float endPos = (float) endPosition.si;
        Boundary from = getBoundaryAtPosition(startPos, false);
        Boundary to = getBoundaryAtPosition(endPos, true);
        return new Boundaries(from.index, from.fraction, to.index, to.fraction);
    }

    /**
     * Determine temporal boundaries.
     * @param startTime start time
     * @param endTime end time
     * @return spatial boundaries
     */
    private Boundaries timeBoundaries(final Duration startTime, final Duration endTime)
    {
        if (startTime.si > this.t[this.size - 1] || endTime.si < this.t[0])
        {
            return new Boundaries(0, 0.0, 0, 0.0);
        }
        // to float needed as x is in floats and due to precision fTo > 1 may become true
        float startTim = (float) startTime.si;
        float endTim = (float) endTime.si;
        Boundary from = getBoundaryAtTime(startTim, false);
        Boundary to = getBoundaryAtTime(endTim, true);
        return new Boundaries(from.index, from.fraction, to.index, to.fraction);
    }

    /**
     * Returns the boundary at the given position.
     * @param position position
     * @param end whether the end of a range is searched
     * @return boundary at the given position
     */
    private Boundary getBoundaryAtPosition(final float position, final boolean end)
    {
        int index = binarySearchX(position);
        double fraction = 0;
        if (end ? index < this.size - 1 : this.x[index] < position)
        {
            fraction = (position - this.x[index]) / (this.x[index + 1] - this.x[index]);
        }
        return new Boundary(index, fraction);
    }

    /**
     * Returns the boundary at the given time.
     * @param time time
     * @param end whether the end of a range is searched
     * @return boundary at the given time
     */
    private Boundary getBoundaryAtTime(final float time, final boolean end)
    {
        int index = binarySearchT(time);
        double fraction = 0;
        if (end ? index < this.size - 1 : this.t[index] < time)
        {
            fraction = (time - this.t[index]) / (this.t[index + 1] - this.t[index]);
        }
        return new Boundary(index, fraction);
    }

    /**
     * Returns an interpolated time at the given position.
     * @param position position
     * @return interpolated time at the given position
     */
    public Duration getTimeAtPosition(final Length position)
    {
        return Duration.ofSI(getBoundaryAtPosition((float) position.si, false).getValue(this.t));
    }

    /**
     * Returns an interpolated speed at the given position.
     * @param position position
     * @return interpolated speed at the given position
     */
    public Speed getSpeedAtPosition(final Length position)
    {
        return Speed.ofSI(getBoundaryAtPosition((float) position.si, false).getValue(this.v));
    }

    /**
     * Returns an interpolated acceleration at the given position.
     * @param position position
     * @return interpolated acceleration at the given position
     */
    public Acceleration getAccelerationAtPosition(final Length position)
    {
        return Acceleration.ofSI(getBoundaryAtPosition((float) position.si, false).getValue(this.a));
    }

    /**
     * Returns an interpolated position at the given time.
     * @param time time
     * @return interpolated position at the given time
     */
    public Length getPositionAtTime(final Duration time)
    {
        return Length.ofSI(getBoundaryAtTime((float) time.si, false).getValue(this.x));
    }

    /**
     * Returns an interpolated speed at the given time.
     * @param time time
     * @return interpolated speed at the given time
     */
    public Speed getSpeedAtTime(final Duration time)
    {
        return Speed.ofSI(getBoundaryAtTime((float) time.si, false).getValue(this.v));
    }

    /**
     * Returns an interpolated acceleration at the given time.
     * @param time time
     * @return interpolated acceleration at the given time
     */
    public Acceleration getAccelerationAtTime(final Duration time)
    {
        return Acceleration.ofSI(getBoundaryAtTime((float) time.si, false).getValue(this.a));
    }

    /**
     * Copies the trajectory but with a subset of the data. Data is taken from position (from + fFrom) to (to + fTo).
     * @param bounds boundaries
     * @param <T> type of underlying extended data value
     * @param <S> storage type
     * @return subset of the trajectory
     */
    @SuppressWarnings("unchecked")
    private <T, S> Trajectory<G> subSet(final Boundaries bounds)
    {
        Trajectory<G> out = new Trajectory<>(this.gtuId, this.gtuTypeId, this.filterData, this.extendedData.keySet());
        if (bounds.from + bounds.fFrom < bounds.to + bounds.fTo) // otherwise empty, no data in the subset
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
            for (ExtendedDataType<?, ?, ?, ? super G> extendedDataType : this.extendedData.keySet())
            {
                int j = 0;
                ExtendedDataType<T, ?, S, G> edt = (ExtendedDataType<T, ?, S, G>) extendedDataType;
                S fromList = (S) this.extendedData.get(extendedDataType);
                S toList = edt.initializeStorage();
                if (nBefore == 1)
                {
                    toList = edt.setValue(toList, j,
                            ((ExtendedDataType<T, ?, ?, G>) extendedDataType).interpolate(
                                    edt.getStorageValue(fromList, bounds.from), edt.getStorageValue(fromList, bounds.from + 1),
                                    bounds.fFrom));
                    j++;
                }
                for (int i = bounds.from + 1; i <= bounds.to; i++)
                {
                    toList = edt.setValue(toList, j, edt.getStorageValue(fromList, i));
                    j++;
                }
                if (nAfter == 1)
                {
                    toList = edt.setValue(toList, j,
                            ((ExtendedDataType<T, ?, ?, G>) extendedDataType).interpolate(
                                    edt.getStorageValue(fromList, bounds.to), edt.getStorageValue(fromList, bounds.to + 1),
                                    bounds.fTo));
                }
                out.extendedData.put(extendedDataType, toList);
            }
        }
        return out;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.gtuId.hashCode();
        result = prime * result + this.size;
        if (this.size > 0)
        {
            result = prime * result + Float.floatToIntBits(this.t[0]);
        }
        return result;
    }

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
        Trajectory<?> other = (Trajectory<?>) obj;
        if (this.size != other.size)
        {
            return false;
        }
        if (!this.gtuId.equals(other.gtuId))
        {
            return false;
        }
        if (this.size > 0 && other.size > 0)
        {
            if (this.t[0] != other.t[0])
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {
        if (this.size > 0)
        {
            return "Trajectory [size=" + this.size + ", x={" + this.x[0] + "..." + this.x[this.size - 1] + "}, t={" + this.t[0]
                    + "..." + this.t[this.size - 1] + "}, filterData=" + this.filterData + ", gtuId=" + this.gtuId + "]";
        }
        return "Trajectory [size=" + this.size + ", x={}, t={}, filterData=" + this.filterData + ", gtuId=" + this.gtuId + "]";
    }

    /**
     * Spatial or temporal boundary as a fractional position in the array.
     * @param index index
     * @param fraction fraction
     */
    private record Boundary(int index, double fraction)
    {
        /**
         * Returns the value at the boundary in the array.
         * @param array float[] array
         * @return value at the boundary in the array
         */
        public double getValue(final float[] array)
        {
            if (this.fraction == 0.0)
            {
                return array[this.index];
            }
            if (this.fraction == 1.0)
            {
                return array[this.index + 1];
            }
            return (1 - this.fraction) * array[this.index] + this.fraction * array[this.index + 1];
        }

        @Override
        public String toString()
        {
            return "Boundary [index=" + this.index + ", fraction=" + this.fraction + "]";
        }
    }

    /**
     * Spatial or temporal range as a fractional positions in the array.
     * @param from from index
     * @param fFrom from fraction
     * @param to to index
     * @param fTo to index
     */
    private record Boundaries(int from, double fFrom, int to, double fTo)
    {
        /**
         * Returns the intersect of both boundaries.
         * @param boundaries boundaries
         * @return intersect of both boundaries
         */
        public Boundaries intersect(final Boundaries boundaries)
        {
            if (this.to < boundaries.from || boundaries.to < this.from
                    || this.to == boundaries.from && this.fTo < boundaries.fFrom
                    || boundaries.to == this.from && boundaries.fTo < this.fFrom)
            {
                return new Boundaries(0, 0.0, 0, 0.0); // no overlap
            }
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

        @Override
        public String toString()
        {
            return "Boundaries [from=" + this.from + ", fFrom=" + this.fFrom + ", to=" + this.to + ", fTo=" + this.fTo + "]";
        }
    }

    /**
     * Space-time view of a trajectory. This supplies distance and time (and mean speed) in a space-time box.
     * @param distance distance
     * @param time time
     */
    public record SpaceTimeView(Length distance, Duration time)
    {
        /**
         * Returns the speed.
         * @return speed
         */
        public Speed speed()
        {
            return this.distance.divide(this.time);
        }
    }

}
