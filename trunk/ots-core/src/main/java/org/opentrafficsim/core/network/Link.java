package org.opentrafficsim.core.network;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionAug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> the ID type of the Link, e.g., String or Integer.
 * @param <N> the type of node that this link uses.
 */
public interface Link<ID, N extends Node<?, ?>>
{
    /** @return id. */
    ID getId();

    /** @return start node. */
    N getStartNode();

    /** @return end node. */
    N getEndNode();

    /** @return link capacity. */
    DoubleScalar.Abs<FrequencyUnit> getCapacity();

    /**
     * Set the link capacity.
     * @param capacity the new capacity of the link as a frequency in GTUs per time unit.
     */
    void setCapacity(final DoubleScalar.Abs<FrequencyUnit> capacity);

    /** @return length of the link. */
    DoubleScalar.Rel<LengthUnit> getLength();
}
