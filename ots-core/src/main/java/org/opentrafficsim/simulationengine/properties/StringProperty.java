package org.opentrafficsim.simulationengine.properties;

import java.io.Serializable;

/**
 * String property.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class StringProperty extends AbstractProperty<String> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The current value of the property. */
    private String value;

    /**
     * Construct an StringProperty.
     * @param key String; the unique key of the new property
     * @param shortName String; the short name of the new StringProperty
     * @param description String; description of the new StringProperty (may use HTML mark up)
     * @param initialValue Integer; the initial value of the new StringProperty
     * @param readOnly boolean; if true this StringProperty can not be altered
     * @param displayPriority int; the displayPriority of the new StringProperty
     */
    public StringProperty(final String key, final String shortName, final String description, final String initialValue,
            final boolean readOnly, final int displayPriority)
    {
        super(key, displayPriority, shortName, description);
        this.value = initialValue;
        setReadOnly(readOnly);
    }

    /** {@inheritDoc} */
    @Override
    public final String getValue()
    {
        return this.value;
    }

    /** {@inheritDoc} */
    @Override
    public final void setValue(final String newValue) throws PropertyException
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
        return getShortName() + ": " + (null == this.value ? "null" : this.value);
    }

    /** {@inheritDoc} */
    @Override
    public final AbstractProperty<String> deepCopy()
    {
        return new StringProperty(getKey(), getShortName(), getDescription(), this.value, isReadOnly(),
                this.getDisplayPriority());
    }

}
