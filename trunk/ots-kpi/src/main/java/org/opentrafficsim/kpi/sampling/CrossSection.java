package org.opentrafficsim.kpi.sampling;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;

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
    private final Set<KpiDirectedLanePosition> directedLanePositions;

    /**
     * Constructor with set of directed lane positions.
     * @param directedLanePositions set of lane locations
     */
    public CrossSection(final Set<KpiDirectedLanePosition> directedLanePositions)
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
    public CrossSection(final LinkDataInterface link, final KpiGtuDirectionality direction, final double fraction) throws SamplingException
    {
        this.directedLanePositions = new HashSet<>();
        for (LaneDataInterface lane : link.getLaneDatas())
        {
            KpiDirectedLanePosition directedLanePosition =
                    new KpiDirectedLanePosition(lane, lane.getLength().multiplyBy(fraction), direction);
            this.directedLanePositions.add(directedLanePosition);
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
    public final Set<KpiDirectedLanePosition> getDirectedLanePositions()
    {
        return new HashSet<>(this.directedLanePositions);
    }

    /**
     * @return iterator over directed lane positions
     */
    public final Iterator<KpiDirectedLanePosition> getIterator()
    {
        // TODO use immutable
        // return new ImmutableIterator<>(this.directedLanePositions.iterator());
        return this.directedLanePositions.iterator();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "CrossSection [directedLanePositions=" + this.directedLanePositions + "]";
    }

}
