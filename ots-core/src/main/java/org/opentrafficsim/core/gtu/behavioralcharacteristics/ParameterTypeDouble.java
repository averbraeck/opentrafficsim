package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

import org.djunits.value.formatter.EngineeringFormatter;

/**
 * Wrapper class for double parameters.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeDouble extends ParameterTypeNumeric<Double> implements Serializable
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
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeDouble(final String id, final String description, final NumericConstraint constraint)
    {
        this(id, description, Double.NaN, constraint, false);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeDouble(final String id, final String description, final double defaultValue,
            final NumericConstraint constraint)
    {
        this(id, description, defaultValue, constraint, true);
    }

    /**
     * Private constructor with default value and check, which may check the default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     * @param hasDefaultValue Whether to check the default value for null.
     */
    private ParameterTypeDouble(final String id, final String description, final double defaultValue,
            final NumericConstraint constraint, final boolean hasDefaultValue)
    {
        super(id, description, Double.class, hasDefaultValue ? defaultValue : null, constraint, hasDefaultValue);
    }

    /** {@inheritDoc} */
    public final String printValue(final BehavioralCharacteristics behavioralCharacteristics) throws ParameterException
    {
        return EngineeringFormatter.format(behavioralCharacteristics.getParameter(this));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "ParameterTypeDouble [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
