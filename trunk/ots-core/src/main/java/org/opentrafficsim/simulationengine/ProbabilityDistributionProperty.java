package org.opentrafficsim.simulationengine;

/**
 * Property that describes a probability distribution.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 18 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ProbabilityDistributionProperty extends AbstractProperty<Double[]>
{
    /** The current set of probability values (should add up to 1.0). */
    private Double[] value;

    /** The names of the values. */
    private String[] names;

    /** The shortName of the property. */
    private String shortName;

    /** The description of the property. */
    private String description;

    /** The property is read-only. */
    private final Boolean readOnly;

    /**
     * Construct a new ProbabilityDistributionProperty.
     * @param shortName String; the short name of the new ProbabilityDistributionProperty
     * @param description String; the description of the new ProbabilityDistributionProperty (may use HTML markup)
     * @param elementNames String[]; names of the elements that, together, add up to probability 1.0
     * @param initialValue Double[]; array of Double values
     * @param readOnly boolean; if true this ProbabilityDistributionProperty can not be altered
     * @throws IncompatiblePropertyException when the array is empty, any value is outside the range 0.0 .. 1.0, or when
     *             the sum of the values is not equal to 1.0 within a small error margin
     */
    public ProbabilityDistributionProperty(final String shortName, final String description,
            final String[] elementNames, final Double[] initialValue, final boolean readOnly)
            throws IncompatiblePropertyException
    {
        this.shortName = shortName;
        this.description = description;
        this.names = elementNames;
        verifyProposedValues(initialValue);
        this.value = initialValue;
        this.readOnly = readOnly;
    }

    /**
     * Verify that a provided array of probability values is acceptable.
     * @param values double[]; the array of values to verify
     * @throws IncompatiblePropertyException when the number of values is 0, any value is outside [0..1], or the sum of
     *             the values does not add up to 1.0 within a (very small) error margin
     */
    private void verifyProposedValues(final Double[] values) throws IncompatiblePropertyException
    {
        if (values.length < 1)
        {
            throw new IncompatiblePropertyException("Array of probability values may not be empty");
        }
        double sum = 0.0;
        for (double v : values)
        {
            if (v < 0.0 || v > 1.0)
            {
                throw new IncompatiblePropertyException("Probability value " + v
                        + " is invalid (valid range is 0.0..1.0)");
            }
            sum += v;
        }
        double maximumError = Math.ulp(1.0) * values.length;
        if (sum < 1.0 - maximumError || sum > 1.0 + maximumError)
        {
            throw new IncompatiblePropertyException("Probabilities do not add up to 1.0 (actual sum is " + sum + ")");
        }

    }

    /** {@inheritDoc} */
    @Override
    public final Double[] getValue()
    {
        // Double is immutable; but we should return a shallow copy of the array so the caller can't replace the
        // elements of our array
        return this.value.clone();
    }

    /**
     * Retrieve one probability value.
     * @param index int; the index of the requested probability value
     * @return Double; the requested probability value
     */
    final Double getValue(final int index)
    {
        return this.value[index];
    }

    /**
     * Retrieve the name of one of the values of this ProbabilityDistributionProperty.
     * @param index int; the index of the value
     * @return String; the name of the value at the requested index
     */
    final String getElementName(final int index)
    {
        return this.names[index];
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
    public final void setValue(final Double[] newValue) throws IncompatiblePropertyException
    {
        if (this.readOnly)
        {
            throw new IncompatiblePropertyException("This property is read-only");
        }
        verifyProposedValues(newValue);
        this.value = newValue;
    }

    /**
     * Return the names of the elements of this ProbabilityDistributionProperty.
     * @return String[]; the names of the elements of this ProbabilityDistributionProperty
     */
    public final String[] getElementNames()
    {
        return this.names.clone();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isReadOnly()
    {
        return this.readOnly;
    }

}
