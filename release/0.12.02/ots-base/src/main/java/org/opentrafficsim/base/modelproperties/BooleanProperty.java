package org.opentrafficsim.base.modelproperties;

import java.io.Serializable;

/**
 * Boolean property.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-05-28 11:33:31 +0200 (Sat, 28 May 2016) $, @version $Revision: 2051 $, by $Author: averbraeck $,
 * initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class BooleanProperty extends AbstractProperty<Boolean> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The current value of the property. */
    private Boolean value;

    /**
     * Construct an BooleanProperty.
     * @param key String; the unique key of the new property
     * @param shortName String; the short name of the new BooleanProperty
     * @param description String; description of the new BooleanProperty (may use HTML mark up)
     * @param initialValue Integer; the initial value of the new BooleanProperty
     * @param readOnly boolean; if true this BooleanProperty can not be altered
     * @param displayPriority int; the displayPriority of the new BooleanProperty
     * @throws PropertyException if <cite>key</cite> is already in use
     */
    public BooleanProperty(final String key, final String shortName, final String description, final Boolean initialValue,
            final boolean readOnly, final int displayPriority) throws PropertyException
    {
        super(key, displayPriority, shortName, description);
        this.value = initialValue;
        setReadOnly(readOnly);
    }

    /** {@inheritDoc} */
    @Override
    public final Boolean getValue()
    {
        return this.value;
    }

    /** {@inheritDoc} */
    @Override
    public final void setValue(final Boolean newValue) throws PropertyException
    {
        if (isReadOnly())
        {
            throw new PropertyException("This property is read-only");
        }
        this.value = newValue;
    }

    /** {@inheritDoc} */
    @Override
    public final String htmlStateDescription()
    {
        return getShortName() + ": " + (null == this.value ? "null" : this.value ? "true" : "false");
    }

    /** {@inheritDoc} */
    @Override
    public final AbstractProperty<Boolean> deepCopy()
    {
        try
        {
            return new BooleanProperty(getKey(), getShortName(), getDescription(), this.value, isReadOnly(),
                    this.getDisplayPriority());
        }
        catch (PropertyException exception)
        {
            System.err.println("Cannot happen");
            exception.printStackTrace();
        }
        return null; // NOTREACHED
    }

}
