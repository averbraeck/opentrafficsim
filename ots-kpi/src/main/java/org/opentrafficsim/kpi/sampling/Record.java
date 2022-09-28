package org.opentrafficsim.kpi.sampling;

/**
 * Consistent set of values corresponding to columns.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Record
{

    /**
     * Returns the column value of this record.
     * @param column Column&lt;T&gt;; column
     * @param <T> value type
     * @return the column value in this record
     */
    <T> T getValue(Column<T> column);

    /**
     * Returns the column value of this record.
     * @param id String; column id
     * @return the column value in this record
     */
    Object getValue(String id);

}
