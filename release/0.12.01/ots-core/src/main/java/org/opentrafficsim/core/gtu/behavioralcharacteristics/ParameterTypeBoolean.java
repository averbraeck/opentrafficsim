package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;

import nl.tudelft.simulation.language.Throw;

/**
 * Wrapper class for boolean parameters.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeBoolean extends AbstractParameterType<Dimensionless> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Constructor without default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     */
    public ParameterTypeBoolean(final String id, final String description)
    {
        this(id, description, false, false);
    }

    /**
     * Constructor with default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     */
    public ParameterTypeBoolean(final String id, final String description, final boolean defaultValue)
    {
        this(id, description, defaultValue, true);
    }

    /**
     * Private constructor with default value, which may check the default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     * @param hasDefaultValue Whether to check the default value for null.
     */
    public ParameterTypeBoolean(final String id, final String description, final boolean defaultValue,
            final boolean hasDefaultValue)
    {
        super(id, description, Dimensionless.class, hasDefaultValue ? new Dimensionless(defaultValue ? 1.0 : 0.0,
                DimensionlessUnit.SI) : null, null, hasDefaultValue);
    }

    /** {@inheritDoc} */
    public final Boolean getDefaultValue() throws ParameterException
    {
        Throw.when(null == this.defaultValue, ParameterException.class, "No default value was set for '%s'.", getId());
        return super.defaultValue.si != 0.0;
    }

    /** {@inheritDoc} */
    public final String printValue(final BehavioralCharacteristics behavioralCharacteristics) throws ParameterException
    {
        return Boolean.toString(behavioralCharacteristics.getParameter(this));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "ParameterTypeBoolean [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
