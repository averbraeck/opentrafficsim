package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;

/**
 * Wrapper class for boolean parameters.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, 
 *          initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterTypeBoolean extends AbstractParameterType<DimensionlessUnit, Dimensionless> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /**
     * Constructor.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param defaultValue Default value.
     */
    public ParameterTypeBoolean(final String id, final String description, final boolean defaultValue)
    {
        super(id, description, Dimensionless.class, new Dimensionless(defaultValue ? 1.0 : 0.0, DimensionlessUnit.SI));
    }
    
    /**
     * Default boolean value.
     * @return Default boolean value.
     * @throws ParameterException If no default value was given.
     */
    public final Boolean getDefaultValue() throws ParameterException
    {
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
