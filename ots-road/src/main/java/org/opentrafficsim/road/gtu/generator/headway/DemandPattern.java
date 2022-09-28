package org.opentrafficsim.road.gtu.generator.headway;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;

/**
 * Demand pattern defined by a frequency vector, time vector and interpolation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DemandPattern implements Arrivals
{

    /** Demand vector. */
    private final FrequencyVector demandVector;

    /** Time vector, may be null. */
    private final TimeVector timeVector;

    /** Interpolation, may be null. */
    private final Interpolation interpolation;

    /**
     * Constructor.
     * @param demandVector FrequencyVector; demand vector
     * @param timeVector TimeVector; time vector
     * @param interpolation Interpolation; interpolation
     */
    public DemandPattern(final FrequencyVector demandVector, final TimeVector timeVector, final Interpolation interpolation)
    {
        this.demandVector = demandVector;
        this.timeVector = timeVector;
        this.interpolation = interpolation;
    }

    /**
     * Returns the demand vector.
     * @return FrequencyVector; returns the demand vector
     */
    public final FrequencyVector getDemandVector()
    {
        return this.demandVector;
    }

    /**
     * Returns the time vector.
     * @return TimeVector; returns the time vector
     */
    public final TimeVector getTimeVector()
    {
        return this.timeVector;
    }

    /**
     * Returns the interpolation.
     * @return Interpolation; returns the interpolation
     */
    public final Interpolation getInterpolation()
    {
        return this.interpolation;
    }

    /** {@inheritDoc} */
    @Override
    public Frequency getFrequency(final Time time, final boolean sliceStart)
    {
        return this.interpolation.interpolateVector(time, this.demandVector, this.timeVector, sliceStart);
    }

    /** {@inheritDoc} */
    @Override
    public Time nextTimeSlice(final Time time)
    {
        for (Time d : this.timeVector)
        {
            if (d.gt(time))
            {
                return d;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.demandVector == null) ? 0 : this.demandVector.hashCode());
        result = prime * result + ((this.interpolation == null) ? 0 : this.interpolation.hashCode());
        result = prime * result + ((this.timeVector == null) ? 0 : this.timeVector.hashCode());
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
        DemandPattern other = (DemandPattern) obj;
        if (this.demandVector == null)
        {
            if (other.demandVector != null)
            {
                return false;
            }
        }
        else if (!this.demandVector.equals(other.demandVector))
        {
            return false;
        }
        if (this.interpolation != other.interpolation)
        {
            return false;
        }
        if (this.timeVector == null)
        {
            if (other.timeVector != null)
            {
                return false;
            }
        }
        else if (!this.timeVector.equals(other.timeVector))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DemandPattern [demandVector=" + this.demandVector + ", timeVector=" + this.timeVector + ", interpolation="
                + this.interpolation + "]";
    }

}
