package org.opentrafficsim.road.network.sampling;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opentrafficsim.core.immutablecollections.ImmutableIterator;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CrossSection implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160929L;
    
    /** Set of lane locations. */
    private final Set<DirectedLanePosition> directedLanePositions;

    /**
     * Constructor.
     * @param directedLanePositions set of lane locations
     */
    public CrossSection(final Set<DirectedLanePosition> directedLanePositions)
    {
        this.directedLanePositions = new HashSet<>(directedLanePositions);
    }
    
    /**
     * @return number of directed lane positions
     */
    public final int size()
    {
        return this.directedLanePositions.size();
    }
    
    /**
     * @return safe copy of directed lane positions
     */
    public final Set<DirectedLanePosition> getDirectedLanePositions()
    {
        return new HashSet<>(this.directedLanePositions);
    }
    
    /**
     * @return iterator over directed lane positions
     */
    public final Iterator<DirectedLanePosition> getIterator() 
    {
        return new ImmutableIterator<>(this.directedLanePositions.iterator());
    }
    
}
