package org.opentrafficsim.simulationengine;

/**
 * Integer property.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 18 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IntegerProperty extends AbstractProperty<Integer>
{
    /** The current value of the property. */
    private Integer value;

    /** The shortName of the property. */
    private String shortName;

    /** The description of the property. */
    private String description;

    /** Format string to display the value of the property. */
    private String format;

    /** The minimum value of the property. */
    private Integer minimumValue;

    /** The maximum value of the property. */
    private Integer maximumValue;

    /** The property is read-only. */
    private final Boolean readOnly;

    /**
     * Construct an IntegerProperty.
     * @param shortName String; the short name of the new IntegerProperty
     * @param description String; description of the new IntegerProperty (may use HTML mark up)
     * @param initialValue Integer; the initial value of the new IntegerProperty
     * @param minimumValue Integer; the minimum value of the new IntegerProperty
     * @param maximumValue Integer; the maximumValue of the new IntegerProperty
     * @param formatString String; format string to display the value
     * @param readOnly boolean; if true this IntegerProperty can not be altered
     * @param displayPriority int; the display priority of the new IntegerProperty
     */
    public IntegerProperty(final String shortName, final String description, final Integer initialValue,
            final Integer minimumValue, final Integer maximumValue, String formatString, final boolean readOnly,
            int displayPriority)
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
    public final Integer getValue()
    {
        return this.value;
    }

    /**
     * Retrieve the minimum value of this IntegerProperty.
     * @return Integer; the minimum value of this IntegerProperty
     */
    public final Integer getMinimumValue()
    {
        return this.minimumValue;
    }

    /**
     * Retrieve the maximum value of this IntegerProperty.
     * @return Integer; the maximum value of this IntegerProperty
     */
    public final Integer getMaximumValue()
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
    public final void setValue(final Integer newValue) throws IncompatiblePropertyException
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
    public final boolean isReadOnly()
    {
        return this.readOnly;
    }

    /**
     * @return String; the format string to display the value
     */
    public String getFormatString()
    {
        return this.format;
    }

}
