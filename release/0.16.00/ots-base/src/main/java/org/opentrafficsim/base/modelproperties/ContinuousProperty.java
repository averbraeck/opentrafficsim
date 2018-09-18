package org.opentrafficsim.base.modelproperties;

import java.io.Serializable;

/**
 * Continuous property.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-05-28 11:33:31 +0200 (Sat, 28 May 2016) $, @version $Revision: 2051 $, by $Author: averbraeck $,
 * initial version 30 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContinuousProperty extends AbstractProperty<Double> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The current value. */
    private Double value;

    /** Format string to display the value of the property. */
    private String format;

    /** The minimum value of the property. */
    private Double minimumValue;

    /** The maximum value of the property. */
    private Double maximumValue;

    /** The minimum value is valid. */
    private boolean includesLowerBound;

    /** The maximum value is valid. */
    private boolean includesUpperBound;

    /**
     * Construct a ContinousProperty that is inclusive of lowerBound and upperBound.
     * @param key String; the unique key of the new property
     * @param shortName String; the short name of the new ContinuousProperty
     * @param description String; description of the new ContinuousProperty (may use HTML mark up)
     * @param initialValue Double; the initial value of the new ContinuousProperty (may be out of range)
     * @param minimumValue Double; the minimum value of the new ContinuousProperty
     * @param maximumValue Double; the maximumValue of the new ContinuousProperty
     * @param formatString String; format string to display the value
     * @param readOnly boolean; if true this ContinuousProperty can not be altered
     * @param displayPriority int; the displayPriority of the new ContinuousProperty
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public ContinuousProperty(final String key, final String shortName, final String description, final Double initialValue,
            final Double minimumValue, final Double maximumValue, final String formatString, final boolean readOnly,
            final int displayPriority)
    {
        this(key, shortName, description, initialValue, minimumValue, maximumValue, true, true, formatString, readOnly,
                displayPriority);
    }

    /**
     * Construct a ContinousProperty.
     * @param key String; the unique key of the new property
     * @param shortName String; the short name of the new ContinuousProperty
     * @param description String; description of the new ContinuousProperty (may use HTML mark up)
     * @param initialValue Double; the initial value of the new ContinuousProperty (may be out of range)
     * @param minimumValue Double; the minimum value of the new ContinuousProperty
     * @param maximumValue Double; the maximumValue of the new ContinuousProperty
     * @param includesLowerBound boolean; if true, the minimumValue is valid; if false, the minimumValue is not valid
     * @param includesUpperBound boolean; if true, the maximumValue is valid; if false, the maximumValue is not valid
     * @param formatString String; format string to display the value
     * @param readOnly boolean; if true this ContinuousProperty can not be altered
     * @param displayPriority int; the displayPriority of the new ContinuousProperty
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public ContinuousProperty(final String key, final String shortName, final String description, final Double initialValue,
            final Double minimumValue, final Double maximumValue, final boolean includesLowerBound,
            final boolean includesUpperBound, final String formatString, final boolean readOnly, final int displayPriority)
    {
        super(key, displayPriority, shortName, description);
        this.value = initialValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.includesLowerBound = includesLowerBound;
        this.includesUpperBound = includesUpperBound;
        this.format = formatString;
        setReadOnly(readOnly);
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
    public final void setValue(final Double newValue) throws PropertyException
    {
        if (isReadOnly())
        {
            throw new PropertyException("This property is read-only");
        }
        // @formatter:off
        if (this.minimumValue > newValue || this.maximumValue < newValue
                || this.minimumValue == newValue && (!this.includesLowerBound) && this.minimumValue != this.maximumValue 
                || this.maximumValue == newValue && (!this.includesUpperBound) && this.minimumValue != this.maximumValue 
                || this.minimumValue == newValue && this.maximumValue == newValue && (!this.includesLowerBound) 
                && (!this.includesUpperBound))
        // @formatter:on
        {
            throw new PropertyException("new value " + newValue + " is out of valid range (" + this.minimumValue + ".."
                    + this.maximumValue + ")");
        }
        this.value = newValue;
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
    public final AbstractProperty<Double> deepCopy()
    {
        return new ContinuousProperty(getKey(), getShortName(), getDescription(), this.value, this.minimumValue,
                this.maximumValue, this.format, isReadOnly(), getDisplayPriority());
    }

    /**
     * Is the range inclusive of the minimumValue?
     * @return boolean; true if the range is inclusive of the minimumValue
     */
    public final boolean includesLowerBound()
    {
        return this.includesLowerBound;
    }

    /**
     * Is the range inclusive of the maximumValue?
     * @return boolean; true if the range is inclusive of the maximumValue
     */
    public final boolean includesUpperBound()
    {
        return this.includesUpperBound;
    }

}
