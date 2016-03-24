package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 24, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> 
 */
public abstract class AbstractParameterType<T extends DoubleScalar.Rel<?>>
{
    private final String id;
    private final String description;
    protected final Class<T> valueClass;
    private final T defaultValue;
    

    /**
     * @param id
     * @param description
     * @param valueClass
     * @param defaultValue
     */
    public AbstractParameterType(String id, String description, Class<T> valueClass, T defaultValue)
    {
        super();
        this.id = id;
        this.description = description;
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
    }

    /**
     * @param id
     * @param description
     * @param valueClass
     */
    public AbstractParameterType(String id, String description, Class<T> valueClass)
    {
        this(id, description, valueClass, null);
    }

    /**
     * @return id
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * @return description
     */
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * @return defaultValue
     */
    public final T getDefaultValue()
    {
        return this.defaultValue;
    } 
    
}

