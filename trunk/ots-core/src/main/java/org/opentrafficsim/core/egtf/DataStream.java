package org.opentrafficsim.core.egtf;

import java.util.Objects;

/**
 * Data stream for the EGTF. These are obtained by {@code DataSource.addStream()} and {@code DataSource.getStream()}.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 okt. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @param dataSource DataSource; data source
     * @param quantity Quantity&lt;T, ?&gt;; quantity
     * @param thetaCong double; standard deviation in congestion
     * @param thetaFree double; standard deviation in free flow
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
     * @return DataSource; the data source
     */
    DataSource getDataSource()
    {
        return this.dataSource;
    }

    /**
     * Returns the quantity.
     * @return Quantity; the quantity
     */
    Quantity<T, ?> getQuantity()
    {
        return this.quantity;
    }

    /**
     * Returns the standard deviation in congestion.
     * @return double; the standard deviation in congestion
     */
    double getThetaCong()
    {
        return this.thetaCong;
    }

    /**
     * Returns the standard deviation in free flow.
     * @return double; the standard deviation in free flow
     */
    double getThetaFree()
    {
        return this.thetaFree;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.dataSource.getName() == null) ? 0 : this.dataSource.getName().hashCode());
        result = prime * result + ((this.quantity == null) ? 0 : this.quantity.hashCode());
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
        DataStream<?> other = (DataStream<?>) obj;
        return Objects.equals(this.dataSource.getName(), other.dataSource.getName())
                && Objects.equals(this.quantity, other.quantity);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DataStream [" + this.dataSource.getName() + ", " + this.quantity.getName() + "]";
    }

}
