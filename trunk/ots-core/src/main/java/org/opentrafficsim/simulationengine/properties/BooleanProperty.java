package org.opentrafficsim.simulationengine.properties;

/**
 * Boolean property.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class BooleanProperty extends AbstractProperty<Boolean>
{
    /** The current value of the property. */
    private Boolean value;

    /** The shortName of the property. */
    private String shortName;

    /** The description of the property. */
    private String description;

    /** The property is read-only. */
    private final Boolean readOnly;

    /**
     * Construct an BooleanProperty.
     * @param shortName String; the short name of the new BooleanProperty
     * @param description String; description of the new BooleanProperty (may use HTML mark up)
     * @param initialValue Integer; the initial value of the new BooleanProperty
     * @param readOnly boolean; if true this BooleanProperty can not be altered
     * @param displayPriority int; the displayPriority of the new BooleanProperty
     */
    public BooleanProperty(final String shortName, final String description, final Boolean initialValue,
        final boolean readOnly, final int displayPriority)
    {
        super(displayPriority);
        this.shortName = shortName;
        this.description = description;
        this.value = initialValue;
        this.readOnly = readOnly;
    }

    /** {@inheritDoc} */
    @Override
    public final Boolean getValue()
    {
        return this.value;
    }

    /** {@inheritDoc} */
    @Override
    public final String getShortName()
    {
        return this.shortName;
    }

    /** {@inheritDoc} */
    @Override
    public final String getDescription()
    {
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    public final void setValue(final Boolean newValue) throws PropertyException
    {
        if (this.readOnly)
        {
            throw new PropertyException("This property is read-only");
        }
        this.value = newValue;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isReadOnly()
    {
        return this.readOnly;
    }

    /** {@inheritDoc} */
    @Override
    public final String htmlStateDescription()
    {
        return getShortName() + ": " + (null == this.value ? "null" : this.value ? "true" : "false");
    }

    /** {@inheritDoc} */
    @Override
    public AbstractProperty<Boolean> deepCopy()
    {
        return new BooleanProperty(this.shortName, this.description, this.value, this.readOnly, this.getDisplayPriority());
    }

}
