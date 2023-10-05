package org.opentrafficsim.draw.egtf;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data source for the EGTF. These are obtained using {@code EGTF.getDataSource()}.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class DataSource
{
    /** Unique name. */
    private final String name;

    /** Data stream of this data source. */
    private final Map<String, DataStream<?>> streams = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param name String; unique name
     */
    DataSource(final String name)
    {
        this.name = name;
    }

    /**
     * Returns the name.
     * @return String; name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Add a non-speed stream for the quantity to this data source.
     * @param quantity Quantity&lt;T, ?&gt;; quantity
     * @param thetaCong T; standard deviation of this quantity of measurements in congestion by this data source
     * @param thetaFree T; standard deviation of this quantity of measurements in free flow by this data source
     * @param <T> implicit data type
     * @return DataStream; the created data stream
     * @throws IllegalArgumentException if the quantity is speed
     */
    public <T extends Number> DataStream<T> addStream(final Quantity<T, ?> quantity, final T thetaCong, final T thetaFree)
    {
        return addStreamSI(quantity, thetaCong.doubleValue(), thetaFree.doubleValue());
    }

    /**
     * Add a stream for the quantity to this data source.
     * @param quantity Quantity&lt;T, ?&gt;; quantity
     * @param thetaCong double; standard deviation of this quantity of measurements in congestion by this data source in SI
     * @param thetaFree double; standard deviation of this quantity of measurements in free flow by this data source in SI
     * @param <T> implicit data type
     * @return DataStream; the created data stream
     */
    public <T extends Number> DataStream<T> addStreamSI(final Quantity<T, ?> quantity, final double thetaCong,
            final double thetaFree)
    {
        if (this.streams.containsKey(quantity.getName()))
        {
            throw new IllegalStateException(
                    String.format("Data source %s already has a stream for quantity %s.", this.name, quantity.getName()));
        }
        if (thetaCong <= 0.0 || thetaFree <= 0.0)
        {
            throw new IllegalArgumentException("Standard deviation must be positive and above 0.");
        }
        DataStream<T> dataStream = new DataStream<>(this, quantity, thetaCong, thetaFree);
        this.streams.put(quantity.getName(), dataStream);
        return dataStream;
    }

    /**
     * Get a stream for the quantity of this data source. If no stream has been created, one will be created with 1.0 standard
     * deviation.
     * @param quantity Quantity&lt;T, ?&gt;; quantity
     * @return DataStream&lt;T&gt;; stream for the quantity of this data source
     * @param <T> implicit data type
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Number> DataStream<T> getStream(final Quantity<T, ?> quantity)
    {
        if (!this.streams.containsKey(quantity.getName()))
        {
            addStreamSI(quantity, 1.0, 1.0);
        }
        return (DataStream<T>) this.streams.get(quantity.getName());
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
        DataSource other = (DataSource) obj;
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
        return "DataSource [" + this.name + "]";
    }

}
