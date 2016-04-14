package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.formatter.EngineeringFormatter;
import org.djunits.value.vdouble.scalar.Dimensionless;

/**
 * Wrapper class for double parameters.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeDouble extends AbstractParameterType<DimensionlessUnit, Dimensionless> implements Serializable
{

    /** */
    private static final long serialVersionUID = 120160400;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeDouble(final String id, final String description)
    {
        this(id, description, Double.NaN, null, false);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name..
     * @param defaultValue Default value.
     */
    public ParameterTypeDouble(final String id, final String description, final double defaultValue)
    {
        this(id, description, defaultValue, null, true);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param check Check for parameter values.
     */
    public ParameterTypeDouble(final String id, final String description, final Check check)
    {
        this(id, description, Double.NaN, check, false);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param check Check for parameter values.
     */
    public ParameterTypeDouble(final String id, final String description, final double defaultValue, final Check check)
    {
        super(id, description, Dimensionless.class, new Dimensionless(defaultValue, DimensionlessUnit.SI), check, true);
    }

    /**
     * Private constructor with default value and check, which may check the default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param check Check for parameter values.
     * @param hasDefaultValue Whether to check the default value for null.
     */
    private ParameterTypeDouble(final String id, final String description, final double defaultValue, final Check check,
        final boolean hasDefaultValue)
    {
        super(id, description, Dimensionless.class, hasDefaultValue ? new Dimensionless(defaultValue, DimensionlessUnit.SI)
            : null, check, hasDefaultValue);
        try
        {
            // Forward empty set of parameters. At creation time of parameter types, values cannot be checked with values of
            // other parameter types.
            check(defaultValue, new BehavioralCharacteristics());
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Default value does not comply with constraints.", exception);
        }
    }

    /**
     * Default double value.
     * @return Default double value.
     * @throws ParameterException If no default value was given.
     */
    public final Double getDefaultValue() throws ParameterException
    {
        ParameterException.throwIf(null == this.defaultValue, "No default value was set for '%s'.", getId());
        return super.defaultValue.si;
    }

    /** {@inheritDoc} */
    public final String printValue(final BehavioralCharacteristics behavioralCharacteristics) throws ParameterException
    {
        return EngineeringFormatter.format(behavioralCharacteristics.getParameter(this));
    }

    /**
     * Method to overwrite for checks with constraints.
     * @param value Value to check with constraints.
     * @param bc Set of behavioral characteristics.
     * @throws ParameterException If the value does not comply with constraints.
     */
    public void check(final double value, final BehavioralCharacteristics bc) throws ParameterException
    {
        //
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "ParameterTypeDouble [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
