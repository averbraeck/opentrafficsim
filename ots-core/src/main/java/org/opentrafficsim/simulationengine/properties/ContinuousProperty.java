package org.opentrafficsim.simulationengine.properties;

/**
 * Continuous property.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version30 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContinuousProperty extends AbstractProperty<Double>
{
    /** The current value. */
    private Double value;

    /** The shortName of the property. */
    private String shortName;

    /** The description of the property. */
    private String description;

    /** Format string to display the value of the property. */
    private String format;

    /** The minimum value of the property. */
    private Double minimumValue;

    /** The maximum value of the property. */
    private Double maximumValue;

    /** The property is read-only. */
    private final Boolean readOnly;

    /**
     * Construct a ContinousProperty.
     * @param shortName String; the short name of the new ContinuousProperty
     * @param description String; description of the new ContinuousProperty (may use HTML mark up)
     * @param initialValue Double; the initial value of the new ContinuousProperty
     * @param minimumValue Double; the minimum value of the new ContinuousProperty
     * @param maximumValue Double; the maximumValue of the new ContinuousProperty
     * @param formatString String; format string to display the value
     * @param readOnly boolean; if true this ContinuousProperty can not be altered
     * @param displayPriority int; the displayPriority of the new ContinuousProperty
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public ContinuousProperty(final String shortName, final String description, final Double initialValue,
            final Double minimumValue, final Double maximumValue, final String formatString, final boolean readOnly,
            final int displayPriority)
    {
        super(displayPriority);
        this.shortName = shortName;
        this.description = description;
        this.value = initialValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.format = formatString;
        this.readOnly = readOnly;
    }

    /** {@inheritDoc} */
    @Override
    public final Double getValue()
    {
        return this.value;
    }

    /**
     * @return the minimum value
     */
    public final Double getMinimumValue()
    {
        return this.minimumValue;
    }

    /**
     * @return the minimum value
     */
    public final Double getMaximumValue()
    {
        return this.maximumValue;
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
    public final void setValue(final Double newValue) throws PropertyException
    {
        if (this.readOnly)
        {
            throw new PropertyException("This property is read-only");
        }
        if (this.minimumValue > newValue || this.maximumValue < newValue)
        {
            throw new PropertyException("new value " + newValue + " is out of valid range (" + this.minimumValue + ".."
                    + this.maximumValue + ")");
        }
        this.value = newValue;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isReadOnly()
    {
        return false;
    }

    /**
     * @return String; the format string to display the value
     */
    public final String getFormatString()
    {
        return this.format;
    }

    /** {@inheritDoc} */
    @Override
    public final String htmlStateDescription()
    {
        return getShortName() + ": " + String.format(getFormatString(), getValue());
    }

    /** {@inheritDoc} */
    @Override
    public AbstractProperty<Double> deepCopy()
    {
        return new ContinuousProperty(this.shortName, this.description, this.value, this.minimumValue,
                this.maximumValue, this.format, this.readOnly, getDisplayPriority());
    }

}
