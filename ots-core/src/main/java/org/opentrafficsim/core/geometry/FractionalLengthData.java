package org.opentrafficsim.core.geometry;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableNavigableSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.djutils.immutablecollections.ImmutableTreeSet;

/**
 * Container for fractional length data.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FractionalLengthData
{

    /** Underlying data. */
    private final NavigableMap<Double, Double> data = new TreeMap<>();

    /**
     * Create FractionalLengthData.
     * @param data fractional length - value pairs. Fractional lengths do not need to be in order.
     * @throws IllegalArgumentException when the number of input values is not even or 0.
     * @throws IllegalArgumentException when a fractional value is not in the range [0 ... 1].
     */
    public FractionalLengthData(final double... data) throws IllegalArgumentException
    {
        Throw.when(data.length < 2 || data.length % 2 > 0, IllegalArgumentException.class,
                "Number of input values must be even and at least 2.");
        for (int i = 0; i < data.length; i = i + 2)
        {
            Throw.when(data[i] < 0.0 || data[i] > 1.0, IllegalArgumentException.class,
                    "Fractional length %s is outside of range [0 ... 1].", data[i]);
            this.data.put(data[i], data[i + 1]);
        }
    }

    /**
     * Create FractionalLengthData.
     * @param data fractional length - value pairs. Fractional lengths do not need to be in order.
     * @throws IllegalArgumentException when the input data is null or empty.
     * @throws IllegalArgumentException when a fractional value is not in the range [0 ... 1].
     */
    public FractionalLengthData(final Map<Double, Double> data) throws IllegalArgumentException
    {
        Throw.when(data == null || data.isEmpty(), IllegalArgumentException.class, "Input data is empty or null.");
        for (Entry<Double, Double> entry : data.entrySet())
        {
            Throw.when(entry.getKey() < 0.0 || entry.getKey() > 1.0, IllegalArgumentException.class,
                    "Fractional length %s is outside of range [0 ... 1].", entry.getKey());
            this.data.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns the data at given fractional length. If only data beyond the fractional length is available, the first available
     * value is returned. If only data before the fractional length is available, the last available value is returned.
     * Otherwise data is linearly interpolated.
     * @param fractionalLength fractional length, may be outside range [0 ... 1].
     * @return interpolated or extended value.
     */
    public double get(final double fractionalLength)
    {
        Double exact = this.data.get(fractionalLength);
        if (exact != null)
        {
            return exact;
        }
        Entry<Double, Double> ceiling = this.data.ceilingEntry(fractionalLength);
        if (ceiling == null)
        {
            return this.data.lastEntry().getValue();
        }
        Entry<Double, Double> floor = this.data.floorEntry(fractionalLength);
        if (floor == null)
        {
            return this.data.firstEntry().getValue();
        }
        double w = (fractionalLength - floor.getKey()) / (ceiling.getKey() - floor.getKey());
        return (1.0 - w) * floor.getValue() + w * ceiling.getValue();
    }

    /**
     * Returns the derivative of the data with respect to fractional length.
     * @param fractionalLength fractional length, may be outside range [0 ... 1].
     * @return derivative of the data with respect to fractional length.
     */
    public double getDerivative(final double fractionalLength)
    {
        Entry<Double, Double> ceiling, floor;
        if (fractionalLength == 0.0)
        {
            ceiling = this.data.higherEntry(fractionalLength);
            floor = this.data.floorEntry(fractionalLength);
        }
        else
        {
            ceiling = this.data.ceilingEntry(fractionalLength);
            floor = this.data.lowerEntry(fractionalLength);
        }
        if (ceiling == null || floor == null)
        {
            return 0.0;
        }
        return (ceiling.getValue() - floor.getValue()) / (ceiling.getKey() - floor.getKey());
    }

    /**
     * Returns the fractional lengths in the underlying data.
     * @return fractional lengths in the underlying data.
     */
    public ImmutableNavigableSet<Double> getFractionalLengths()
    {
        return new ImmutableTreeSet<>(this.data.keySet());
    }

    /**
     * Returns the values in the underlying data.
     * @return values in the underlying data.
     */
    public ImmutableSet<Double> getValues()
    {
        return new ImmutableLinkedHashSet<>(this.data.values());
    }

    /**
     * Returns fractional lengths in array form, including 0.0 and 1.0.
     * @return fractional lengths.
     */
    public double[] getFractionalLengthsAsArray()
    {
        NavigableMap<Double, Double> full = fullRange();
        double[] fractionalLengths = new double[full.size()];
        int i = 0;
        for (double f : full.navigableKeySet())
        {
            fractionalLengths[i++] = f;
        }
        return fractionalLengths;
    }

    /**
     * Returns fractional lengths in array form, including values at 0.0 and 1.0.
     * @return fractional lengths.
     */
    public double[] getValuesAsArray()
    {
        NavigableMap<Double, Double> full = fullRange();
        double[] values = new double[full.size()];
        int i = 0;
        for (double f : full.navigableKeySet())
        {
            values[i++] = full.get(f);
        }
        return values;
    }

    /**
     * Returns the data including entries at 0.0 and 1.0.
     * @return data with fill range.
     */
    private NavigableMap<Double, Double> fullRange()
    {
        NavigableMap<Double, Double> full = new TreeMap<>(this.data);
        full.put(0.0, full.firstEntry().getValue());
        full.put(1.0, full.lastEntry().getValue());
        return full;
    }

    /**
     * Returns the number of data points.
     * @return number of data points.
     */
    public int size()
    {
        return this.data.size();
    }

    /**
     * Create FractionalLengthData.
     * @param data fractional length - value pairs. Fractional lengths do not need to be in order.
     * @return fractional length data.
     * @throws IllegalArgumentException when the number of input values is not even or 0.
     * @throws IllegalArgumentException when a fractional value is not in the range [0 ... 1].
     */
    public static FractionalLengthData of(final double... data) throws IllegalArgumentException
    {
        return new FractionalLengthData(data);
    }

}
