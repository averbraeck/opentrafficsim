package org.opentrafficsim.simulationengine;

/**
 * Continuous property.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 30 dec. 2014 <br>
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
    public ContinuousProperty(final String shortName, final String description, final Double initialValue,
            final Double minimumValue, final Double maximumValue, String formatString, final boolean readOnly,
            int displayPriority)
    {
        this.shortName = shortName;
        this.description = description;
        this.value = initialValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.format = formatString;
        this.readOnly = readOnly;
        this.displayPriority = displayPriority;
    }

    /** {@inheritDoc} */
    @Override
    public Double getValue()
    {
        return this.value;
    }

    /**
     * @return the minimum value
     */
    public Double getMinimumValue()
    {
        return this.minimumValue;
    }

    /**
     * @return the minimum value
     */
    public Double getMaximumValue()
    {
        return this.maximumValue;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortName()
    {
        return this.shortName;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription()
    {
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(Double newValue) throws IncompatiblePropertyException
    {
        if (this.readOnly)
        {
            throw new IncompatiblePropertyException("This property is read-only");
        }
        if (this.minimumValue > newValue || this.maximumValue < newValue)
        {
            throw new IncompatiblePropertyException("new value " + newValue + " is out of valid range ("
                    + this.minimumValue + ".." + this.maximumValue + ")");
        }
        this.value = newValue;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly()
    {
        return false;
    }

    /**
     * @return String; the format string to display the value
     */
    public String getFormatString()
    {
        return this.format;
    }

}
