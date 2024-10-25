package org.opentrafficsim.draw.egtf;

import java.util.Objects;

/**
 * Data stream for the EGTF. These are obtained by {@code DataSource.addStream()} and {@code DataSource.getStream()}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> data type of the stream
 */
public final class DataStream<T extends Number>
{
    /** Data source. */
    private final DataSource dataSource;

    /** Quantity. */
    private final Quantity<T, ?> quantity;

    /** Standard deviation in congestion. */
    private final double thetaCong;

    /** Standard deviation in free flow. */
    private final double thetaFree;

    /**
     * Constructor.
     * @param dataSource data source
     * @param quantity quantity
     * @param thetaCong standard deviation in congestion
     * @param thetaFree standard deviation in free flow
     */
    DataStream(final DataSource dataSource, final Quantity<T, ?> quantity, final double thetaCong, final double thetaFree)
    {
        Objects.requireNonNull(dataSource, "Data source may not be null.");
        Objects.requireNonNull(quantity, "Quantity may not be null.");
        Objects.requireNonNull(thetaCong, "Theta cong may not be null.");
        Objects.requireNonNull(thetaFree, "Theta free may not be null.");
        this.dataSource = dataSource;
        this.quantity = quantity;
        this.thetaCong = thetaCong;
        this.thetaFree = thetaFree;
    }

    /**
     * Returns the data source.
     * @return the data source
     */
    DataSource getDataSource()
    {
        return this.dataSource;
    }

    /**
     * Returns the quantity.
     * @return the quantity
     */
    Quantity<T, ?> getQuantity()
    {
        return this.quantity;
    }

    /**
     * Returns the standard deviation in congestion.
     * @return the standard deviation in congestion
     */
    double getThetaCong()
    {
        return this.thetaCong;
    }

    /**
     * Returns the standard deviation in free flow.
     * @return the standard deviation in free flow
     */
    double getThetaFree()
    {
        return this.thetaFree;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.dataSource.getName() == null) ? 0 : this.dataSource.getName().hashCode());
        result = prime * result + ((this.quantity == null) ? 0 : this.quantity.hashCode());
        return result;
    }

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
        DataStream<?> other = (DataStream<?>) obj;
        return Objects.equals(this.dataSource.getName(), other.dataSource.getName())
                && Objects.equals(this.quantity, other.quantity);
    }

    @Override
    public String toString()
    {
        return "DataStream [" + this.dataSource.getName() + ", " + this.quantity.getName() + "]";
    }

}
