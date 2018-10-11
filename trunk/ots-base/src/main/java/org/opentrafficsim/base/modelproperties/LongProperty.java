package org.opentrafficsim.base.modelproperties;

import nl.tudelft.simulation.language.Throw;

/**
 * Long property.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LongProperty extends AbstractProperty<Long>
{

    /** */
    private static final long serialVersionUID = 20180409L;

    /** Value. */
    private long value;

    /** Minimum value. */
    private long minValue;

    /** Maximum value. */
    private long maxValue;

    /** Format string. */
    private final String formatString;

    /**
     * Constructor.
     * @param key String; key
     * @param displayPriority int; display property
     * @param shortName String; shortName
     * @param description String; description
     * @param initialValue long; initial value
     * @param minValue long; minimum value
     * @param maxValue long; maximum value
     * @param formatString String; format string
     * @param readOnly boolean; read-only
     */
    public LongProperty(final String key, final int displayPriority, final String shortName, final String description,
            final long initialValue, final long minValue, final long maxValue, final String formatString,
            final boolean readOnly)
    {
        super(key, displayPriority, shortName, description);
        this.value = initialValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.formatString = formatString;
        setReadOnly(readOnly);
    }

    /** {@inheritDoc} */
    @Override
    public Long getValue()
    {
        return this.value;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(final Long newValue) throws PropertyException
    {
        Throw.when(isReadOnly(), PropertyException.class, "Cannot set read-only property.");
        Throw.when(newValue < this.minValue || newValue > this.maxValue, PropertyException.class,
                "Value %d is not in the range %d through %d.", newValue, this.minValue, this.maxValue);
        this.value = newValue;
    }

    /** {@inheritDoc} */
    @Override
    public AbstractProperty<Long> deepCopy()
    {
        return new LongProperty(getKey(), getDisplayPriority(), getShortName(), getDescription(), this.value, this.minValue,
                this.maxValue, this.formatString, isReadOnly());
    }

    /** {@inheritDoc} */
    @Override
    public final String htmlStateDescription()
    {
        return getShortName() + ": " + String.format(this.formatString, getValue());
    }

}
