package org.opentrafficsim.core.network;

import java.io.Serializable;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.geometry.OTSLine3D;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public interface Link extends LocatableInterface, Serializable, OTS_SCALAR
{
    /** @return id. */
    String getId();

    /** @return start node. */
    Node getStartNode();

    /** @return end node. */
    Node getEndNode();

    /** @return link capacity. */
    Frequency.Abs getCapacity();

    /**
     * Set the link capacity.
     * @param capacity the new capacity of the link as a frequency in GTUs per time unit.
     */
    void setCapacity(final Frequency.Abs capacity);

    /** @return the design line. */
    OTSLine3D getDesignLine();

    /** @return length of the link. */
    Length.Rel getLength();
    
    /** {@inheritDoc} */
    @Override
    DirectedPoint getLocation();
    
    /** {@inheritDoc} */
    @Override
    Bounds getBounds();
}
