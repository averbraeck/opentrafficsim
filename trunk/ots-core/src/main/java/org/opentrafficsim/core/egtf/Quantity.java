package org.opentrafficsim.core.egtf;

/**
 * Defines a quantity that data sources can provide, such as speed, flow, etc.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 okt. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> data type
 * @param <K> grid output format
 */
public class Quantity<T extends Number, K>
{
    /** Standard quantity for speed. */
    public static final Quantity<Double, double[][]> SPEED_SI = new Quantity<>("Speed", true, Converter.SI);

    /** Standard quantity for flow. */
    public static final Quantity<Double, double[][]> FLOW_SI = new Quantity<>("Flow", Converter.SI);

    /** Standard quantity for density. */
    public static final Quantity<Double, double[][]> DENSITY_SI = new Quantity<>("Density", Converter.SI);

    /** Name. */
    private final String name;

    /** Whether this quantity is speed. */
    private final boolean speed;

    /** Converter for output format. */
    private final Converter<K> converter;

    /**
     * Constructor.
     * @param name String; name
     * @param converter Converter&lt;K&gt;; converter for output format
     */
    public Quantity(final String name, final Converter<K> converter)
    {
        this(name, false, converter);
    }

    /**
     * Constructor. Protected so only the default SPEED_SI quantity is speed.
     * @param name String; name
     * @param speed boolean; whether this quantity is speed
     * @param converter Converter&lt;K&gt;; converter for output format
     */
    protected Quantity(final String name, final boolean speed, final Converter<K> converter)
    {
        this.name = name;
        this.speed = speed;
        this.converter = converter;
    }

    /**
     * Returns a quantity with {@code double[][]} containing SI values as output format.
     * @param name String; name
     * @return quantity with {@code double[][]} containing SI values as output format
     */
    public static Quantity<?, double[][]> si(final String name)
    {
        return new SI<>(name);
    }

    /**
     * Returns the name.
     * @return String; name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Returns whether this quantity is speed.
     * @return boolean; whether this quantity is speed
     */
    final boolean isSpeed()
    {
        return this.speed;
    }

    /**
     * Converts the filtered data to an output format.
     * @param data double[][]; filtered data
     * @return K; output data
     */
    final K convert(final double[][] data)
    {
        return this.converter.convert(data);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Quantity<?, ?> other = (Quantity<?, ?>) obj;
        if (this.name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Quantity [name=" + this.name + "]";
    }

    /**
     * Class to return in {@code double[][]} output format.
     * @param <T> data type
     */
    private static class SI<T extends Number> extends Quantity<T, double[][]>
    {

        /**
         * Constructor.
         * @param name String; String name
         */
        SI(final String name)
        {
            super(name, Converter.SI);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "SI []";
        }

    }

}
