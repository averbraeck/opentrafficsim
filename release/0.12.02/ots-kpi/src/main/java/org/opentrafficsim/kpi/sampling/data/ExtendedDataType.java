package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;

import nl.tudelft.simulation.language.Throw;

/**
 * Type class to define different types of data by which trajectories can be extended beyond the basic t, x, v, a. Extend this 
 * class to define a new data type.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of value
 */
public abstract class ExtendedDataType<T>
{

    /** Id. */
    private final String id;
    
    /**
     * Constructor setting the id.
     * @param id id
     */
    public ExtendedDataType(final String id)
    {
        Throw.whenNull(id, "Id may nog be null.");
        this.id = id;
    }
    
    /**
     * @return id
     */
    public final String getId()
    {
        return this.id;
    }
    
    /**
     * @param gtu gtu to get the data value of
     * @return value of this data type of the given GTU
     */
    public abstract T getValue(final GtuDataInterface gtu);
    
    /**
     * Interpolate value between two measured values. The default implementation takes a linear interpolation over time for
     * {@link DoubleScalar}, {@link Double} and {@link Float}, or the closest value in time otherwise.
     * @param value0 first value
     * @param value1 second value
     * @param f interpolation fraction
     * @param <U> unit of value, if values are {@code DoubleScalar}
     * @return interpolated value
     */
    @SuppressWarnings({ "unchecked", "checkstyle:parameternumber", "checkstyle:designforextension" })
    public <U extends Unit<U>> T interpolate(final T value0, final T value1, final double f)
    {
        Throw.whenNull(value0, "Values to interpolate may not be null.");
        Throw.whenNull(value1, "Values to interpolate may not be null.");
        if (value0 instanceof DoubleScalar.Rel<?>)
        {
            return (T) DoubleScalar.interpolate((DoubleScalar.Rel<U>) value0, (DoubleScalar.Rel<U>) value1, f);
        }
        if (value0 instanceof DoubleScalar.Abs<?>)
        {
            return (T) DoubleScalar.interpolate((DoubleScalar.Abs<U>) value0, (DoubleScalar.Abs<U>) value1, f);
        }
        if (value0 instanceof Double)
        {
            return (T) (Double) ((Double) value0 * (1.0 - f) + (Double) value1 * f);
        }
        if (value0 instanceof Float)
        {
            return (T) (Float) (float) ((Float) value0 * (1.0 - f) + (Float) value1 * f);
        }
        if (f < 0.5)
        {
            return value0;
        }
        return value1;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
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
        ExtendedDataType<?> other = (ExtendedDataType<?>) obj;
        if (this.id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        }
        else if (!this.id.equals(other.id))
        {
            return false;
        }
        return true;
    }
    
}
