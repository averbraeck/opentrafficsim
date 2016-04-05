package org.opentrafficsim.core.gtu.drivercharacteristics;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;

/**
 * In this class a list of behavioral characteristics in the form of parameters can be stored for use in behavioral models.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Wouter Schakel
 */
public class BehavioralCharacteristics
{

    // TODO: !=EMPTY may not work across multiple computers
    /** 
     * Object to recognize that none was set previously. 
     */
    private static final Dimensionless EMPTY = new Dimensionless(0, DimensionlessUnit.SI);

    /** List of parameters. */
    private final Map<AbstractParameterType<?>, DoubleScalar.Rel<?>> parameters = new HashMap<>();

    /** List of parameters with values before last set. */
    private final Map<AbstractParameterType<?>, DoubleScalar.Rel<?>> previous = new HashMap<>();

    /**
     * Set parameter value of given parameter type.
     * @param parameterType Parameter type.
     * @param value Value.
     * @param <U> Class of unit.
     * @param <T> Class of value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    @SuppressWarnings("unchecked")
    public final <U extends Unit<U>, T extends DoubleScalar.Rel<U>> void setParameter(
        final ParameterType<T> parameterType, final DoubleScalar.Rel<U> value) throws ParameterException
    {
        parameterType.check((T) value, this);
        saveSetParameter(parameterType, value);
    }

    /**
     * Set parameter value of given parameter type.
     * @param parameterType Parameter type.
     * @param value Value.
     */
    public final void setParameter(final ParameterTypeBoolean parameterType, final boolean value)
    {
        saveSetParameter(parameterType, new Dimensionless(value ? 1.0 : 0.0, DimensionlessUnit.SI));
    }

    /**
     * Set parameter value of given parameter type.
     * @param parameterType Parameter type.
     * @param value Value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    public final void setParameter(final ParameterTypeDouble parameterType, final double value) throws ParameterException
    {
        parameterType.check(value, this);
        saveSetParameter(parameterType, new Dimensionless(value, DimensionlessUnit.SI));
    }

    /**
     * Set parameter value of given parameter type.
     * @param parameterType Parameter type.
     * @param value Value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    public final void setParameter(final ParameterTypeInteger parameterType, final int value) throws ParameterException
    {
        parameterType.check(value, this);
        saveSetParameter(parameterType, new Dimensionless(value, DimensionlessUnit.SI));
    }

    /**
     * Remembers the current value, or if it is not given, for possible reset.
     * @param parameterType Parameter type.
     * @param value Value.
     */
    private void saveSetParameter(final AbstractParameterType<?> parameterType, final DoubleScalar.Rel<?> value)
    {
        if (this.parameters.containsKey(parameterType))
        {
            this.previous.put(parameterType, this.parameters.get(parameterType));
        }
        else
        {
            // remember that there was no value before this set
            this.previous.put(parameterType, EMPTY);
        }
        this.parameters.put(parameterType, value);
    }

    /**
     * Resets the parameter value to the value from before the last set. This goes only a single value back.
     * @param parameterType Parameter type.
     * @throws ParameterException If the parameter was never set.
     */
    public final void resetParameter(final AbstractParameterType<?> parameterType) throws ParameterException
    {
        ParameterException.failIf(!this.previous.containsKey(parameterType), "Reset on parameter of type '"
            + parameterType.getId() + "' could not be performed, it was not set.");
        if (this.previous.get(parameterType) != EMPTY)
        {
            this.parameters.put(parameterType, this.previous.get(parameterType));
        }
        else
        {
            // no value was set before last set, so make parameter type not set
            this.parameters.remove(parameterType);
        }
        this.previous.remove(parameterType); // prevent consecutive resets
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @param <U> Class of unit.
     * @param <T> Class of value.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    @SuppressWarnings("unchecked")
    public final <U extends Unit<U>, T extends DoubleScalar.Rel<U>> DoubleScalar.Rel<U>
        getParameter(final ParameterType<T> parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return (DoubleScalar.Rel<U>) this.parameters.get(parameterType);
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final boolean getParameter(final ParameterTypeBoolean parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return this.parameters.get(parameterType).si != 0.0;
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final int getParameter(final ParameterTypeInteger parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return (int) this.parameters.get(parameterType).si;
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final double getParameter(final ParameterTypeDouble parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return this.parameters.get(parameterType).si;
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final Speed getSpeedParameter(final ParameterType<Speed> parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return (Speed) this.parameters.get(parameterType);
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final Acceleration getAccelerationParameter(final ParameterType<Acceleration> parameterType) 
            throws ParameterException
    {
        checkContains(parameterType);
        return (Acceleration) this.parameters.get(parameterType);
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final Time.Rel getTimeParameter(final ParameterType<Time.Rel> parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return (Time.Rel) this.parameters.get(parameterType);
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final Length.Rel getLengthParameter(final ParameterType<Length.Rel> parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return (Length.Rel) this.parameters.get(parameterType);
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final Frequency getFrequencyParameter(final ParameterType<Frequency> parameterType) throws ParameterException
    {
        checkContains(parameterType);
        return (Frequency) this.parameters.get(parameterType);
    }

    /**
     * Get parameter of given type.
     * @param parameterType Parameter type.
     * @return Parameter of given type.
     * @throws ParameterException If parameter was never set.
     */
    public final LinearDensity getLinearDensityParameter(final ParameterType<LinearDensity> parameterType)
            throws ParameterException
    {
        checkContains(parameterType);
        return (LinearDensity) this.parameters.get(parameterType);
    }

    /**
     * Check whether parameter has been set.
     * @param parameterType Parameter type.
     * @throws ParameterException If parameter is not present.
     */
    private void checkContains(final AbstractParameterType<?> parameterType) throws ParameterException
    {
        ParameterException.failIf(!contains(parameterType), "Could not get parameter of type '" + parameterType.getId()
            + "' as it was not set.");
    }

    /**
     * Whether the given parameter type has been set.
     * @param parameterType Parameter type.
     * @return Whether the given parameter type has been set.
     */
    public final boolean contains(final AbstractParameterType<?> parameterType)
    {
        return this.parameters.containsKey(parameterType);
    }

    /**
     * Returns a safe copy of the parameters.
     * @return Safe copy of the parameters, e.g., for printing.
     */
    public final Map<AbstractParameterType<?>, DoubleScalar.Rel<?>> getParameters()
    {
        return new HashMap<AbstractParameterType<?>, DoubleScalar.Rel<?>>(this.parameters);
    }

}
