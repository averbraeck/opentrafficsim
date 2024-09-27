package org.opentrafficsim.draw.egtf;

/**
 * Converter for use in {@code Quantity} to convert internal filtered data to an output type.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <K> grid output format
 */
public interface Converter<K>
{
    /** Standard converter that returns the internal SI data directly. */
    Converter<double[][]> SI = new Converter<double[][]>()
    {
        /** {@inheritDoc} */
        @Override
        public double[][] convert(final double[][] filteredData)
        {
            return filteredData;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "Converter SI";
        }
    };

    /**
     * Convert the filtered data to an output format.
     * @param filteredData filtered data
     * @return data in output format
     */
    K convert(double[][] filteredData);
}
