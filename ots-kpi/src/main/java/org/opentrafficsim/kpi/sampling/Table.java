package org.opentrafficsim.kpi.sampling;

import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.base.Identifiable;

/**
 * Table with data stored in structured records.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Table extends Iterable<Record>, Identifiable
{

    /**
     * Returns the description.
     * @return description
     */
    String getDescription();

    /**
     * Returns the list of columns.
     * @return list of columns
     */
    ImmutableList<Column<?>> getColumns();

    /**
     * Returns the number of columns.
     * @return number of columns
     */
    default int getNumberOfColumns()
    {
        return getColumns().size();
    }

    /**
     * Returns whether the table is empty.
     * @return whether the table is empty
     */
    boolean isEmpty();

}
