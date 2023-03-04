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
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vfloat.vector.FloatAccelerationVector;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.djunits.value.vfloat.vector.FloatTimeVector;
import org.djunits.value.vfloat.vector.base.FloatVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.FilterDataType;

/**
 * Contains position, speed, acceleration and time data of a GTU, over some section. Position is relative to the start of the
 * lane in the direction of travel, also when trajectories have been truncated at a position x &gt; 0. Note that this regards
 * internal data and output. Input position always refers to the design line of the lane. This class internally flips input
 * positions and boundaries.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
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

    /** Meta data. */
    private final Map<FilterDataType<?>, Object> filterData = new LinkedHashMap<>();

    /** Map of array data types and their values. */
    private final Map<ExtendedDataType<?, ?, ?, G>, Object> extendedData = new LinkedHashMap<>();

    /** Lane of travel. */
    private final LaneData lane;

    /**
     * @param gtu GtuData; GTU of this trajectory, only the id is stored.
     * @param filterData Map&lt;FilterDataType&lt;?&gt;, Object&gt;; filter data
     * @param extendedData Set&lt;ExtendedDataType&lt;?,?,?,G&gt;&gt;; types of extended data
     * @param lane LaneData; lane of travel
     */
    public Trajectory(final GtuData gtu, final Map<FilterDataType<?>, Object> filterData,
            final Set<ExtendedDataType<?, ?, ?, G>> extendedData, final LaneData lane)
    {
        this(gtu == null ? null : gtu.getId(), filterData, extendedData, lane);
    }

    /**
     * Private constructor for creating subsets.
     * @param gtuId String; GTU id
     * @param filterData Map&lt;FilterDataType&lt;?&gt;, Object&gt;; filter data
     * @param extendedData Set&lt;ExtendedDataType&lt;?,?,?,G&gt;&gt;; types of extended data
     * @param lane LaneData; lane of travel
     */
    private Trajectory(final String gtuId, final Map<FilterDataType<?>, Object> filterData,
            final Set<ExtendedDataType<?, ?, ?, G>> extendedData, final LaneData lane)
    {
        Throw.whenNull(gtuId, "GTU may not be null.");
        Throw.whenNull(filterData, "Filter data may not be null.");
        Throw.whenNull(extendedData, "Extended data may not be null.");
        Throw.whenNull(lane, "Lane direction may not be null.");
        this.gtuId = gtuId;
        this.filterData.putAll(filterData);
        for (ExtendedDataType<?, ?, ?, G> dataType : extendedData)
        {
            this.extendedData.put(dataType, dataType.initializeStorage());
        }
        this.lane = lane;
    }

    /**
     * Adds values of position, speed, acceleration and time.
     * @param position Length; position is relative to the start of the lane in the direction of the design line, i.e.
     *            irrespective of the travel direction, also when trajectories have been truncated at a position x &gt; 0
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @param time Time; time
     */
    public void add(final Length position, final Speed speed, final Acceleration acceleration, final Time time)
    {
        add(position, speed, acceleration, time, null);
    }

    /**
     * Adds values of position, speed, acceleration and time.
     * @param position Length; position is relative to the start of the lane in the direction of the design line, i.e.
     *            irrespective of the travel direction, also when trajectories have been truncated at a position x &gt; 0
     * @param speed Speed; speed
     * @param acceleration Acceleration; acceleration
     * @param time Time; time
     * @param gtu G; gtu to add extended data for
     */
    public void add(final Length position, final Speed speed, final Acceleration acceleration, final Time time, final G gtu)
    {
        Throw.whenNull(position, "Position may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        if (!this.extendedData.isEmpty())
        {
            Throw.whenNull(gtu, "GTU may not be null.");
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
        for (ExtendedDataType<?, ?, ?, G> extendedDataType : this.extendedData.keySet())
        {
            appendValue(extendedDataType, gtu);
        }
        this.size++;
    }

    /**
     * Append value of the extended data type.
     * @param extendedDataType ExtendedDataType&lt;T,?,S,G&gt;; extended data type
     * @param gtu G; gtu
     * @param <T> extended data value type
     * @param <S> extended data storage data type
     */
    @SuppressWarnings("unchecked")
    private <T, S> void appendValue(final ExtendedDataType<T, ?, S, G> extendedDataType, final G gtu)
    {
        S in = (S) this.extendedData.get(extendedDataType);
        S out = extendedDataType.setValue(in, this.size, extendedDataType.getValue(gtu));
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
     * Returns the id.
     * @return GTU id
     */
    public String getGtuId()
    {
        return this.gtuId;
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
     * @param position float; position
     * @return int; last index with a position smaller than or equal to the given position
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
     * @param time float; time
     * @return int; last index with a time smaller than or equal to the given time
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
     * @param index int; index
     * @return {@code x} value of a single sample
     * @throws SamplingException if the index is out of bounds
     */
    public float getX(final int index) throws SamplingException
    {
        checkSample(index);
        return this.x[index];
    }

    /**
     * Returns {@code v} value of a single sample.
     * @param index int; index
     * @return {@code v} value of a single sample
     * @throws SamplingException if the index is out of bounds
     */
    public float getV(final int index) throws SamplingException
    {
        checkSample(index);
        return this.v[index];
    }

    /**
     * Returns {@code a} value of a single sample.
     * @param index int; index
     * @return {@code a} value of a single sample
     * @throws SamplingException if the index is out of bounds
     */
    public float getA(final int index) throws SamplingException
    {
        checkSample(index);
        return this.a[index];
    }

    /**
     * Returns {@code t} value of a single sample.
     * @param index int; index
     * @return {@code t} value of a single sample
     * @throws SamplingException if the index is out of bounds
     */
    public float getT(final int index) throws SamplingException
    {
        checkSample(index);
        return this.t[index];
    }

    /**
     * Returns extended data type value of a single sample.
     * @param extendedDataType ExtendedDataType&lt;T,?,S,?&gt;; data type from which to retrieve the data
     * @param index int; index for which to retrieve the data
     * @param <T> scalar type of extended data type
     * @param <S> storage type of extended data type
     * @return extended data type value of a single sample
     * @throws SamplingException if the index is out of bounds
     */
    @SuppressWarnings("unchecked")
    public <T, S> T getExtendedData(final ExtendedDataType<T, ?, S, ?> extendedDataType, final int index)
            throws SamplingException
    {
        checkSample(index);
        return extendedDataType.getStorageValue((S) this.extendedData.get(extendedDataType), index);
    }

    /**
     * Throws an exception if the sample index is out of bounds.
     * @param index int; sample index
     * @throws SamplingException if the sample index is out of bounds
     */
    private void checkSample(final int index) throws SamplingException
    {
        Throw.when(index < 0 || index >= this.size, SamplingException.class, "Index is out of bounds.");
    }

    /**
     * Returns strongly type position array.
     * @return strongly typed position array.
     */
    public FloatLengthVector getPosition()
    {
        try
        {
            return FloatVector.instantiate(getX(), LengthUnit.SI, StorageType.DENSE);
        }
        catch (ValueRuntimeException exception)
        {
            // should not happen, inputs are not null
            throw new RuntimeException("Could not return trajectory data.", exception);
        }
    }

    /**
     * Returns strongly typed speed array.
     * @return strongly typed speed array.
     */
    public FloatSpeedVector getSpeed()
    {
        try
        {
            return FloatVector.instantiate(getV(), SpeedUnit.SI, StorageType.DENSE);
        }
        catch (ValueRuntimeException exception)
        {
            // should not happen, inputs are not null
            throw new RuntimeException("Could not return trajectory data.", exception);
        }
    }

    /**
     * Returns strongly typed acceleration array.
     * @return strongly typed acceleration array.
     */
    public FloatAccelerationVector getAcceleration()
    {
        try
        {
            return FloatVector.instantiate(getA(), AccelerationUnit.SI, StorageType.DENSE);
        }
        catch (ValueRuntimeException exception)
        {
            // should not happen, inputs are not null
            throw new RuntimeException("Could not return trajectory data.", exception);
        }
    }

    /**
     * Returns strongly typed time array.
     * @return strongly typed time array.
     */
    public FloatTimeVector getTime()
    {
        try
        {
            return FloatVector.instantiate(getT(), TimeUnit.BASE_SECOND, StorageType.DENSE);
        }
        catch (ValueRuntimeException exception)
        {
            // should not happen, inputs are not null
            throw new RuntimeException("Could not return trajectory data.", exception);
        }
    }

    /**
     * Returns the length of the data.
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
     * Returns the total duration span.
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
        return new Duration(this.t[this.size - 1] - this.t[0], DurationUnit.SI);
    }

    /**
     * Returns whether the filter data is contained.
     * @param filterDataType MetaDataType&lt;?&gt;; filter data type
     * @return whether the trajectory contains the filter data of give type
     */
    public boolean contains(final FilterDataType<?> filterDataType)
    {
        return this.filterData.containsKey(filterDataType);
    }

    /**
     * Returns the value of the filter data.
     * @param filterDataType MetaDataType&lt;T&gt;; filter data type
     * @param <T> class of filter data
     * @return value of filter data
     */
    @SuppressWarnings("unchecked")
    public <T> T getFilterData(final FilterDataType<T> filterDataType)
    {
        return (T) this.filterData.get(filterDataType);
    }

    /**
     * Returns the included filter data types.
     * @return included filter data types
     */
    public Set<FilterDataType<?>> getFilterDataTypes()
    {
        return this.filterData.keySet();
    }

    /**
     * Returns whether ths extended data type is contained.
     * @param extendedDataType ExtendedDataType&lt;?,?,?,?&gt;; extended data type
     * @return whether the trajectory contains the extended data of give type
     */
    public boolean contains(final ExtendedDataType<?, ?, ?, ?> extendedDataType)
    {
        return this.extendedData.containsKey(extendedDataType);
    }

    /**
     * Returns the output data of the extended data type.
     * @param extendedDataType ExtendedDataType&lt;?,O,S,?&gt;; extended data type to return
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
    public Set<ExtendedDataType<?, ?, ?, G>> getExtendedDataTypes()
    {
        return this.extendedData.keySet();
    }

    /**
     * Returns a space-time view of this trajectory. This is much more efficient than {@code subSet()} as no trajectory is
     * copied. The limitation is that only distance and time (and mean speed) in the space-time view can be obtained.
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
     * @return space-time view of this trajectory
     */
    public SpaceTimeView getSpaceTimeView(final Length startPosition, final Length endPosition, final Time startTime,
            final Time endTime)
    {
        if (size() == 0)
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
        return new SpaceTimeView(Length.instantiateSI(xTo - xFrom), Duration.instantiateSI(tTo - tFrom));
    }

    /**
     * Copies the trajectory but with a subset of the data. Longitudinal entry is only true if the original trajectory has true,
     * and the subset is from the start.
     * @param startPosition Length; start position
     * @param endPosition Length; end position
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
            return new Trajectory<>(this.gtuId, this.filterData, this.extendedData.keySet(), this.lane);
        }
        return subSet(spaceBoundaries(startPosition, endPosition));
    }

    /**
     * Copies the trajectory but with a subset of the data.
     * @param startTime Time; start time
     * @param endTime Time; end time
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minTime is smaller than maxTime
     */
    public Trajectory<G> subSet(final Time startTime, final Time endTime)
    {
        Throw.whenNull(startTime, "Start time may not be null");
        Throw.whenNull(endTime, "End time may not be null");
        Throw.when(startTime.gt(endTime), IllegalArgumentException.class, "Start time should be smaller than end time.");
        if (this.size == 0)
        {
            return new Trajectory<>(this.gtuId, this.filterData, this.extendedData.keySet(), this.lane);
        }
        return subSet(timeBoundaries(startTime, endTime));
    }

    /**
     * Copies the trajectory but with a subset of the data.
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minLength/Time is smaller than maxLength/Time
     */
    public Trajectory<G> subSet(final Length startPosition, final Length endPosition, final Time startTime, final Time endTime)
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
            return new Trajectory<>(this.gtuId, this.filterData, this.extendedData.keySet(), this.lane);
        }
        return subSet(spaceBoundaries(startPosition, endPosition).intersect(timeBoundaries(startTime, endTime)));
    }

    /**
     * Determine spatial boundaries.
     * @param startPosition Length; start position
     * @param endPosition Length; end position
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
     * @param startTime Time; start time
     * @param endTime Time; end time
     * @return spatial boundaries
     */
    private Boundaries timeBoundaries(final Time startTime, final Time endTime)
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
     * @param position float; position
     * @param end boolean; whether the end of a range is searched
     * @return Boundary; boundary at the given position
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
     * @param time float; time
     * @param end boolean; whether the end of a range is searched
     * @return Boundary; boundary at the given time
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
     * @param position Length; position
     * @return Time; interpolated time at the given position
     */
    public Time getTimeAtPosition(final Length position)
    {
        return Time.instantiateSI(getBoundaryAtPosition((float) position.si, false).getValue(this.t));
    }

    /**
     * Returns an interpolated speed at the given position.
     * @param position Length; position
     * @return Speed; interpolated speed at the given position
     */
    public Speed getSpeedAtPosition(final Length position)
    {
        return Speed.instantiateSI(getBoundaryAtPosition((float) position.si, false).getValue(this.v));
    }

    /**
     * Returns an interpolated acceleration at the given position.
     * @param position Length; position
     * @return Acceleration; interpolated acceleration at the given position
     */
    public Acceleration getAccelerationAtPosition(final Length position)
    {
        return Acceleration.instantiateSI(getBoundaryAtPosition((float) position.si, false).getValue(this.a));
    }

    /**
     * Returns an interpolated position at the given time.
     * @param time Time; time
     * @return Length; interpolated position at the given time
     */
    public Length getPositionAtTime(final Time time)
    {
        return Length.instantiateSI(getBoundaryAtTime((float) time.si, false).getValue(this.x));
    }

    /**
     * Returns an interpolated speed at the given time.
     * @param time Time; time
     * @return Speed; interpolated speed at the given time
     */
    public Speed getSpeedAtTime(final Time time)
    {
        return Speed.instantiateSI(getBoundaryAtTime((float) time.si, false).getValue(this.v));
    }

    /**
     * Returns an interpolated acceleration at the given time.
     * @param time Time; time
     * @return Acceleration; interpolated acceleration at the given time
     */
    public Acceleration getAccelerationAtTime(final Time time)
    {
        return Acceleration.instantiateSI(getBoundaryAtTime((float) time.si, false).getValue(this.a));
    }

    /**
     * Copies the trajectory but with a subset of the data. Data is taken from position (from + fFrom) to (to + fTo).
     * @param bounds Boundaries; boundaries
     * @param <T> type of underlying extended data value
     * @param <S> storage type
     * @return subset of the trajectory
     */
    @SuppressWarnings("unchecked")
    private <T, S> Trajectory<G> subSet(final Boundaries bounds)
    {
        Trajectory<G> out = new Trajectory<>(this.gtuId, this.filterData, this.extendedData.keySet(), this.lane);
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
            for (ExtendedDataType<?, ?, ?, G> extendedDataType : this.extendedData.keySet())
            {
                int j = 0;
                ExtendedDataType<T, ?, S, G> edt = (ExtendedDataType<T, ?, S, G>) extendedDataType;
                S fromList = (S) this.extendedData.get(extendedDataType);
                S toList = edt.initializeStorage();
                try
                {
                    if (nBefore == 1)
                    {
                        toList = edt.setValue(toList, j,
                                ((ExtendedDataType<T, ?, ?, G>) extendedDataType).interpolate(
                                        edt.getStorageValue(fromList, bounds.from),
                                        edt.getStorageValue(fromList, bounds.from + 1), bounds.fFrom));
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
                }
                catch (SamplingException se)
                {
                    // should not happen as bounds are determined internally
                    throw new RuntimeException("Error while obtaining subset of trajectory.", se);
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
        result = prime * result + ((this.gtuId == null) ? 0 : this.gtuId.hashCode());
        result = prime * result + this.size;
        if (this.size > 0)
        {
            result = prime * result + Float.floatToIntBits(this.t[0]);
        }
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
        Trajectory<?> other = (Trajectory<?>) obj;
        if (this.size != other.size)
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
        if (this.size > 0)
        {
            if (this.t[0] != other.t[0])
            {
                return false;
            }
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
                    + "..." + this.t[this.size - 1] + "}, filterData=" + this.filterData + ", gtuId=" + this.gtuId + "]";
        }
        return "Trajectory [size=" + this.size + ", x={}, t={}, filterData=" + this.filterData + ", gtuId=" + this.gtuId + "]";
    }

    /**
     * Spatial or temporal boundary as a fractional position in the array.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class Boundary
    {
        /** Rounded-down index. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public final int index;

        /** Fraction. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public final double fraction;

        /**
         * @param index int; rounded down index
         * @param fraction double; fraction
         */
        Boundary(final int index, final double fraction)
        {
            this.index = index;
            this.fraction = fraction;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Boundary [index=" + this.index + ", fraction=" + this.fraction + "]";
        }

        /**
         * Returns the value at the boundary in the array.
         * @param array float[]; float[] array
         * @return double; value at the boundary in the array
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
    }

    /**
     * Spatial or temporal range as a fractional positions in the array.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
         * @param from int; from index, rounded down
         * @param fFrom double; from index, fraction
         * @param to int; to index, rounded down
         * @param fTo double; to index, fraction
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
         * Returns the intersect of both boundaries.
         * @param boundaries Boundaries; boundaries
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

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Boundaries [from=" + this.from + ", fFrom=" + this.fFrom + ", to=" + this.to + ", fTo=" + this.fTo + "]";
        }

    }

    /**
     * Space-time view of a trajectory. This supplies distance and time (and mean speed) in a space-time box.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static final class SpaceTimeView
    {

        /** Distance. */
        private final Length distance;

        /** Time. */
        private final Duration time;

        /**
         * Constructor.
         * @param distance Length; distance
         * @param time Duration; time
         */
        private SpaceTimeView(final Length distance, final Duration time)
        {
            this.distance = distance;
            this.time = time;
        }

        /**
         * Returns the distance.
         * @return Length; distance
         */
        public Length getDistance()
        {
            return this.distance;
        }

        /**
         * Returns the time.
         * @return Duration; time
         */
        public Duration getTime()
        {
            return this.time;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "SpaceTimeView [distance=" + this.distance + ", time=" + this.time + "]";
        }
    }

}
