package org.opentrafficsim.road.network.sampling;

import java.util.Arrays;

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
import org.djunits.value.vfloat.vector.FloatAccelerationVector;
import org.djunits.value.vfloat.vector.FloatDurationVector;
import org.djunits.value.vfloat.vector.FloatLengthVector;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.network.sampling.meta.MetaData;
import org.opentrafficsim.road.network.sampling.meta.MetaDataType;

/**
 * Contains position, speed, acceleration and time data of a GTU, over some section.
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
// TODO desired speed per time step, flexibility to add other data per time step
public final class Trajectory
{

    /** Default array capacity. */
    private static final int DEFAULT_CAPACITY = 10;

    /** Effective length of the underlying data (arrays may be longer). */
    private int size = 0;

    /** Distance array. */
    private float[] x = new float[DEFAULT_CAPACITY];

    /** Speed array. */
    private float[] v = new float[DEFAULT_CAPACITY];

    /** Acceleration array. */
    private float[] a = new float[DEFAULT_CAPACITY];

    /** Time array. */
    private float[] t = new float[DEFAULT_CAPACITY];

    /** Meta data. */
    private final MetaData metaData;

    /** GTU id. */
    private final String gtuId;

    /** Whether GTU entered this trajectory longitudinally. */
    private final boolean longitudinalEntry;

    /**
     * @param gtu GTU of this trajectory, only the id is stored.
     * @param longitudinalEntry whether GTU entered this trajectory longitudinally
     * @param metaData meta data
     */
    public Trajectory(final GTU gtu, final boolean longitudinalEntry, final MetaData metaData)
    {
        this.gtuId = gtu.getId();
        this.longitudinalEntry = longitudinalEntry;
        this.metaData = metaData;
    }

    /**
     * Private constructor for creating subsets.
     * @param gtuId GTU id
     * @param longitudinalEntry longitudinal entry
     * @param metaData meta data
     */
    private Trajectory(final String gtuId, final boolean longitudinalEntry, final MetaData metaData)
    {
        this.gtuId = gtuId;
        this.longitudinalEntry = longitudinalEntry;
        this.metaData = new MetaData(metaData);
    }

    /**
     * Returns whether GTU entered this trajectory longitudinally. The first point may then be connected to the last point in a
     * previous trajectory.
     * @return whether GTU entered this trajectory longitudinally
     */
    public boolean isLongitudinalEntry()
    {
        return this.longitudinalEntry;
    }

    /**
     * Adds values of distance, speed, acceleration and time.
     * @param distance distance
     * @param speed speed
     * @param acceleration acceleration
     * @param time time
     */
    public void add(final Length distance, final Speed speed, final Acceleration acceleration, final Duration time)
    {
        add((float) distance.si, (float) speed.si, (float) acceleration.si, (float) time.si);
    }

    /**
     * Adds si values of distance, speed, acceleration and time.
     * @param xSI si distance
     * @param vSI si speed
     * @param aSI si acceleration
     * @param tSI si time
     */
    public void add(final float xSI, final float vSI, final float aSI, final float tSI)
    {
        if (this.size == this.x.length)
        {
            int cap = this.size + (this.size >> 1);
            this.x = Arrays.copyOf(this.x, cap);
            this.v = Arrays.copyOf(this.v, cap);
            this.a = Arrays.copyOf(this.a, cap);
            this.t = Arrays.copyOf(this.t, cap);
        }
        this.x[this.size] = xSI;
        this.v[this.size] = vSI;
        this.a[this.size] = aSI;
        this.t[this.size] = tSI;
        this.size++;
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
     * @return si distance values
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
     * @return strongly typed copy of length
     */
    public FloatLengthVector getLength()
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
    public FloatDurationVector getDuration()
    {
        try
        {
            return new FloatDurationVector(this.t, TimeUnit.SI, StorageType.DENSE);
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
        // TODO don't allow empty trajectories
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
        // TODO don't allow empty trajectories
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
        // Counter c;
        // Tally t;
        // nl.tudelft.simulation.dsol.statistics.Tally<SimTime<?,?,T>> t2;
        // Persistent p;
        // nl.tudelft.simulation.dsol.statistics.Persistent<SimTime<?,?,T>> p2;
    }

    /**
     * Copies the trajectory but with a subset of the data. Longitudinal entry is only true if the original trajectory has true,
     * and the subset is from the start.
     * @param minLength minimum length
     * @param maxLength maximum length
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minLength is smaller than maxLength
     */
    public Trajectory subSet(final Length minLength, final Length maxLength)
    {
        Throw.whenNull(minLength, "minLength may not be null");
        Throw.whenNull(maxLength, "maxLength may not be null");
        Throw.when(minLength.gt(maxLength), IllegalArgumentException.class, "minLength should be smaller than maxLength");
        int from = 0;
        while (minLength.si > this.x[from] && from < this.size)
        {
            from++;
        }
        int to = this.size;
        while (maxLength.si < this.x[to] && to > 0)
        {
            to--;
        }
        return subSet(from, to);
    }

    /**
     * Copies the trajectory but with a subset of the data. Longitudinal entry is only true if the original trajectory has true,
     * and the subset is from the start.
     * @param minTime minimum time
     * @param maxTime maximum time
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minTime is smaller than maxTime
     */
    public Trajectory subSet(final Duration minTime, final Duration maxTime)
    {
        Throw.whenNull(minTime, "minTime may not be null");
        Throw.whenNull(maxTime, "maxTime may not be null");
        Throw.when(minTime.gt(maxTime), IllegalArgumentException.class, "minTime should be smaller than maxTime");
        int from = 0;
        while (minTime.si > this.t[from] && from < this.size)
        {
            from++;
        }
        int to = this.size;
        while (maxTime.si < this.t[to] && to > 0)
        {
            to--;
        }
        return subSet(from, to);
    }

    /**
     * Copies the trajectory but with a subset of the data. Longitudinal entry is only true if the original trajectory has true,
     * and the subset is from the start.
     * @param minLength minimum length
     * @param maxLength maximum length
     * @param minTime minimum time
     * @param maxTime maximum time
     * @return subset of the trajectory
     * @throws NullPointerException if an input is null
     * @throws IllegalArgumentException of minLength/Time is smaller than maxLength/Time
     */
    public Trajectory subSet(final Length minLength, final Length maxLength, final Duration minTime, final Duration maxTime)
    {
        // could use this.subSet(minLength, maxLength).subSet(minTime, maxTime), but that copies twice
        Throw.whenNull(minLength, "minLength may not be null");
        Throw.whenNull(maxLength, "maxLength may not be null");
        Throw.when(minLength.gt(maxLength), IllegalArgumentException.class, "minLength should be smaller than maxLength");
        Throw.whenNull(minTime, "minTime may not be null");
        Throw.whenNull(maxTime, "maxTime may not be null");
        Throw.when(minTime.gt(maxTime), IllegalArgumentException.class, "minTime should be smaller than maxTime");
        int from = 0;
        while ((minLength.si > this.x[from] || minTime.si > this.t[from]) && from < this.size)
        {
            from++;
        }
        int to = this.size - 1;
        if (to >= 0)
        {
            while ((maxLength.si < this.x[to] || maxTime.si < this.t[to]) && to > 0)
            {
                to--;
            }
        }
        else
        {
            to = 0;
        }
        return subSet(from, to);
    }

    /**
     * Copies the trajectory but with a subset of the data. Longitudinal entry is only true if the original trajectory has true,
     * and the subset is from the start.
     * @param from first index
     * @param to last index
     * @return subset of the trajectory
     */
    private Trajectory subSet(final int from, final int to)
    {
        Trajectory out = new Trajectory(this.gtuId, from == 0 ? this.longitudinalEntry : false, this.metaData);
        if (from < to) // otherwise empty, no data in the subset
        {
            out.x = Arrays.copyOfRange(this.x, from, to);
            out.v = Arrays.copyOfRange(this.v, from, to);
            out.a = Arrays.copyOfRange(this.a, from, to);
            out.t = Arrays.copyOfRange(this.t, from, to);
            out.size = out.x.length;
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
        result = prime * result + ((this.gtuId == null) ? 0 : this.gtuId.hashCode());
        result = prime * result + (this.longitudinalEntry ? 1231 : 1237);
        result = prime * result + ((this.metaData == null) ? 0 : this.metaData.hashCode());
        result = prime * result + this.size;
        result = prime * result + Arrays.hashCode(this.t);
        result = prime * result + Arrays.hashCode(this.v);
        result = prime * result + Arrays.hashCode(this.x);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
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
        if (this.longitudinalEntry != other.longitudinalEntry)
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
                    + "..." + this.t[this.size - 1] + "}, metaData=" + this.metaData + ", gtuId=" + this.gtuId
                    + ", longitudinalEntry=" + this.longitudinalEntry + "]";
        }
        return "Trajectory [size=" + this.size + ", x={}, t={}, metaData=" + this.metaData + ", gtuId=" + this.gtuId
                + ", longitudinalEntry=" + this.longitudinalEntry + "]";
    }

}
