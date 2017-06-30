package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

/**
 * Wrapper class for int parameters.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeInteger extends ParameterTypeNumeric<Integer> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeInteger(final String id, final String description)
    {
        this(id, description, 0, null, false);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name..
     * @param defaultValue Default value.
     */
    public ParameterTypeInteger(final String id, final String description, final int defaultValue)
    {
        this(id, description, defaultValue, null, true);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeInteger(final String id, final String description, final NumericConstraint constraint)
    {
        this(id, description, 0, constraint, false);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeInteger(final String id, final String description, final int defaultValue, final NumericConstraint constraint)
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
    private ParameterTypeInteger(final String id, final String description, final int defaultValue, final NumericConstraint constraint,
            final boolean hasDefaultValue)
    {
        super(id, description, Integer.class, hasDefaultValue ? defaultValue : null, constraint, hasDefaultValue);
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

    /** {@inheritDoc} */
    public final String printValue(final BehavioralCharacteristics behavioralCharacteristics) throws ParameterException
    {
        return Integer.toString(behavioralCharacteristics.getParameter(this));
    }

    /**
     * Method to overwrite for checks with constraints.
     * @param value Value to check with constraints.
     * @param bc Set of behavioral characteristics.
     * @throws ParameterException If the value does not comply with constraints.
     */
    public void check(final int value, final BehavioralCharacteristics bc) throws ParameterException
    {
        //
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "ParameterTypeInteger [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
