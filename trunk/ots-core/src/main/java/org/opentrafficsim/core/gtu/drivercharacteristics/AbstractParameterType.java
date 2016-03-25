package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * Defines meta-information of a parameter, defining the parameter uniquely.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Wouter Schakel
 * @param <T> Class of the value.
 */
public abstract class AbstractParameterType<T extends DoubleScalar.Rel<?>>
{
	
	/** Short name of parameter. */
    private final String id;
    
    /** Parameter description or full name. */
    private final String description;
    
    /** Class of the value. */
    protected final Class<T> valueClass;
    
    /** Default value. */
    private final T defaultValue;

    /**
     * Constructor with default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public AbstractParameterType(String id, String description, Class<T> valueClass, T defaultValue)
    {
        this.id = id;
        this.description = description;
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
    }

    /**
     * Constructor without default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public AbstractParameterType(String id, String description, Class<T> valueClass)
    {
        this(id, description, valueClass, null);
    }

    /**
     * Returns the parameter id.
     * @return id Parameter id.
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * Returns the parameter description.
     * @return description
     */
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the default value.
     * @return defaultValue Default value.
     * @throw ParameterException If no default value was set.
     */
    public final T getDefaultValue() throws ParameterException
    {
    	ParameterException.failIf(null==this.defaultValue, "No default value was set for "+this.id);
        return this.defaultValue;
    } 
    
}