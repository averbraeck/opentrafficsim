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
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class BehavioralCharacteristics
{
    private final Map<AbstractParameterType<?>, DoubleScalar.Rel<?>> parameters = new HashMap<>();

    /**
     * @param parameterType
     * @param value
     * @throws ParameterException
     */
    public <U extends Unit<U>, T extends DoubleScalar.Rel<U>> void setParameter(ParameterType<T> parameterType,
        DoubleScalar.Rel<U> value) throws ParameterException
    {
        parameterType.check((T) value);
        this.parameters.put(parameterType, value);
    }

    /**
     * @param parameterType
     * @param value
     * @throws ParameterException
     */
    public void setParameter(ParameterTypeDouble parameterType, double value) throws ParameterException
    {
        parameterType.check(value);
        this.parameters.put(parameterType, new Dimensionless(value, DimensionlessUnit.SI));
    }

    public <U extends Unit<U>, T extends DoubleScalar.Rel<U>> DoubleScalar.Rel<U> getParameter(
        ParameterType<T> parameterType)
    {
        return (DoubleScalar.Rel<U>) this.parameters.get(parameterType);
    }

    public boolean getParameter(ParameterTypeBoolean parameterType)
    {
        return this.parameters.get(parameterType).si != 0.0;
    }

    public int getParameter(ParameterTypeInteger parameterType)
    {
        return (int) this.parameters.get(parameterType).si;
    }

    public double getParameter(ParameterTypeDouble parameterType)
    {
        return this.parameters.get(parameterType).si;
    }

    public Speed getSpeedParameter(ParameterType<Speed> parameterType)
    {
        return (Speed) this.parameters.get(parameterType);
    }

    public Acceleration getAcclerationParameter(ParameterType<Acceleration> parameterType)
    {
        return (Acceleration) this.parameters.get(parameterType);
    }

    public Time.Rel getTimeParameter(ParameterType<Time.Rel> parameterType)
    {
        return (Time.Rel) this.parameters.get(parameterType);
    }

    public Length.Rel getLengthParameter(ParameterType<Length.Rel> parameterType)
    {
        return (Length.Rel) this.parameters.get(parameterType);
    }

    public Frequency getFrequencyParameter(ParameterType<Frequency> parameterType)
    {
        return (Frequency) this.parameters.get(parameterType);
    }

    public LinearDensity getLinearDensityParameter(ParameterType<LinearDensity> parameterType)
    {
        return (LinearDensity) this.parameters.get(parameterType);
    }

   /**
     * @return safe copy of parameters, e.g., for printing.
     */
    public final Map<AbstractParameterType<?>, DoubleScalar.Rel<?>> getParameters()
    {
        return new HashMap(this.parameters);
    }

}
