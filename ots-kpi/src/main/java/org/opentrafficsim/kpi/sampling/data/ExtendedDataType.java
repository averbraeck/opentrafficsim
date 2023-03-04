package org.opentrafficsim.kpi.sampling.data;

import org.djunits.unit.AbsoluteLinearUnit;
import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.djunits.value.vdouble.scalar.base.DoubleScalarInterface;
import org.djunits.value.vfloat.scalar.base.FloatScalar;
import org.djunits.value.vfloat.scalar.base.FloatScalarInterface;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.SamplingException;

/**
 * Type class to define different types of data by which trajectories can be extended beyond the basic t, x, v, a. Extend this
 * class to define a new data type. Extended data types are defined with 3 generic types. {@code <T>} is the type of a single
 * scalar value, {@code <O>} is the output type of the whole trajectory, e.g. {@code List<Double>} or {@code FloatLengthVector},
 * and {@code <S>} is the storage type by which the data is gathered, e.g. {@code List<Double>} or {@code float[]}.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of value
 * @param <O> output type
 * @param <S> storage type
 * @param <G> gtu data type
 */
public abstract class ExtendedDataType<T, O, S, G extends GtuData> implements Identifiable
{

    /** Id. */
    private final String id;
    
    /** Description. */
    private final String description;

    /** Type of value. */
    private final Class<T> type;

    /**
     * Constructor setting the id.
     * @param id String; id
     * @param description String; description
     * @param type Class&lt;T&gt;; type class
     */
    public ExtendedDataType(final String id, final String description, final Class<T> type)
    {
        Throw.whenNull(id, "Id may nog be null.");
        Throw.whenNull(description, "Description may nog be null.");
        Throw.whenNull(type, "Type may not bee null.");
        this.id = id;
        this.description = description;
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }
    
    /**
     * Returns the description.
     * @return String; description.
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the type.
     * @return the type
     */
    public Class<T> getType()
    {
        return this.type;
    }

    /**
     * Returns the current value of the gtu.
     * @param gtu G; gtu
     * @return current value of the gtu
     */
    public abstract T getValue(G gtu);

    /**
     * Returns an updated list/array/vector of the storage type, including a new value at given index.
     * @param storage S; storage
     * @param index int; index to store next value
     * @param value T; value to add
     * @return updated list/array/vector of the storage type, including a new value at given index
     */
    public abstract S setValue(S storage, int index, T value);

    /**
     * Returns a specific output value. This is used to store extended data types as generic file, i.e. text file.
     * @param output O; output
     * @param index int; index of value to return
     * @return the i'th output value
     * @throws SamplingException when {@code i} is out of bounds.
     */
    public abstract T getOutputValue(O output, int index) throws SamplingException;

    /**
     * Returns a specific storage value. This is used to bypass conversion to the output type when trajectories are cut.
     * @param storage S; storage
     * @param index int; index of value to return
     * @return the i'th output value
     * @throws SamplingException when {@code i} is out of bounds.
     */
    public abstract T getStorageValue(S storage, int index) throws SamplingException;

    /**
     * Returns an initial storage object.
     * @return initial storage object.
     */
    public abstract S initializeStorage();

    /**
     * Convert storage type to output type.
     * @param storage S; stored data
     * @param size int; size of trajectory
     * @return converted output
     */
    public abstract O convert(S storage, int size);

    /**
     * Parses a stored string representation to original type.
     * @param string String; stored string representation
     * @return T; value in original type
     */
    public abstract T parseValue(String string);

    /**
     * Interpolate value between two measured values. The default implementation takes a linear interpolation over time for
     * {@link DoubleScalar}, {@link FloatScalar}, {@link Double} and {@link Float}, or the closest value in time otherwise.
     * @param value0 T; first value
     * @param value1 T; second value
     * @param f double; interpolation fraction
     * @param <AU> unit of value, if values are {@code DoubleScalar}
     * @param <RU> the corresponding relative unit
     * @param <R> the relative double type
     * @param <RA> the relative double type belonging to the absolute type
     * @param <A> the absolute double type
     * @param <FR> the relative float type
     * @param <FRA> the relative float type belonging to the absolute type
     * @param <FA> the absolute float type
     * @return interpolated value
     */
    @SuppressWarnings({"unchecked", "checkstyle:designforextension"})
    public <AU extends AbsoluteLinearUnit<AU, RU>, RU extends Unit<RU>, R extends DoubleScalarInterface.Rel<RU, R>,
            RA extends DoubleScalarInterface.RelWithAbs<AU, A, RU, RA>, A extends DoubleScalarInterface.Abs<AU, A, RU, RA>,
            FR extends FloatScalarInterface.Rel<RU, FR>, FRA extends FloatScalarInterface.RelWithAbs<AU, FA, RU, FRA>,
            FA extends FloatScalarInterface.Abs<AU, FA, RU, FRA>> T interpolate(final T value0, final T value1, final double f)
    {
        Throw.whenNull(value0, "Values to interpolate may not be null.");
        Throw.whenNull(value1, "Values to interpolate may not be null.");
        if (value0 instanceof DoubleScalarInterface.Rel<?, ?>)
        {
            return (T) DoubleScalar.interpolate((R) value0, (R) value1, f);
        }
        if (value0 instanceof DoubleScalarInterface.Abs<?, ?, ?, ?>)
        {
            return (T) DoubleScalar.interpolate((A) value0, (A) value1, f);
        }
        if (value0 instanceof FloatScalarInterface.Rel<?, ?>)
        {
            return (T) FloatScalar.interpolate((FR) value0, (FR) value1, (float) f);
        }
        if (value0 instanceof FloatScalarInterface.Abs<?, ?, ?, ?>)
        {
            return (T) FloatScalar.interpolate((FA) value0, (FA) value1, (float) f);
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
        ExtendedDataType<?, ?, ?, ?> other = (ExtendedDataType<?, ?, ?, ?>) obj;
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
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ExtendedDataType [id=" + this.id + ", description=" + this.description + "]";
    }

}
