package org.opentrafficsim.road.network.sampling;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.immutablecollections.ImmutableIterator;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A cross sections contains locations on lanes that together make up a cross section. It is not required that this is on a 
 * single road, i.e. the cross section may any section in space.
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
     * Constructor with set of directed lane positions.
     * @param directedLanePositions set of lane locations
     */
    public CrossSection(final Set<DirectedLanePosition> directedLanePositions)
    {
        this.directedLanePositions = new HashSet<>(directedLanePositions);
    }

    /**
     * Constructor with link and direction.
     * @param link link
     * @param direction direction
     * @param fraction fraction on link
     * @throws SamplingException if an input is null
     */
    public CrossSection(final CrossSectionLink link, final GTUDirectionality direction, final double fraction)
            throws SamplingException
    {
        this.directedLanePositions = new HashSet<>();
        for (Lane lane : link.getLanes())
        {
            try
            {
                this.directedLanePositions.add(new DirectedLanePosition(lane, lane.getLength().multiplyBy(fraction), direction));
            }
            catch (GTUException exception)
            {
                throw new SamplingException("Improper cross section input.", exception);
            }
        }
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

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "CrossSection [directedLanePositions=" + this.directedLanePositions + "]";
    }
    
}
