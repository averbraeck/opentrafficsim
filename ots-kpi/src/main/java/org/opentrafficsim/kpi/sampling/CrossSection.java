package org.opentrafficsim.kpi.sampling;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableIterator;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.interfaces.LinkData;

/**
 * A cross sections contains locations on lanes that together make up a cross section. It is not required that this is on a
 * single road, i.e. the cross section may any section in space.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CrossSection implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160929L;

    /** Set of lane locations. */
    private final Set<LanePosition> lanePositions;

    /**
     * Constructor with set of lane positions.
     * @param lanePositions set of lane locations
     */
    public CrossSection(final Set<LanePosition> lanePositions)
    {
        Throw.whenNull(lanePositions, "Lane positions may not be null.");
        this.lanePositions = new LinkedHashSet<>(lanePositions);
    }

    /**
     * Constructor with link and fraction.
     * @param link link
     * @param fraction fraction on link
     * @throws SamplingException if an input is null
     */
    public CrossSection(final LinkData<?> link, final double fraction) throws SamplingException
    {
        Throw.whenNull(link, "Link lane positions may not be null.");
        this.lanePositions = new LinkedHashSet<>();
        for (LaneData<?> lane : link.getLaneDatas())
        {
            LanePosition lanePosition = new LanePosition(lane, lane.getLength().times(fraction));
            this.lanePositions.add(lanePosition);
        }
    }

    /**
     * Returns the number of lane positions.
     * @return number of directed lane positions
     */
    public final int size()
    {
        return this.lanePositions.size();
    }

    /**
     * Returns a safe copy of the lane positions.
     * @return safe copy of lane positions
     */
    public final Set<LanePosition> getLanePositions()
    {
        return new LinkedHashSet<>(this.lanePositions);
    }

    /**
     * Returns an iterator over the lane positions.
     * @return iterator over lane positions
     */
    public final Iterator<LanePosition> getIterator()
    {
        return new ImmutableIterator<>(this.lanePositions.iterator());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "CrossSection [lanePositions=" + this.lanePositions + "]";
    }

}
