package org.opentrafficsim.road.gtu.animation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SplitColorer implements GTUColorer
{

    /** Left color. */
    static final Color LEFT = Color.RED;

    /** Other color. */
    static final Color OTHER = Color.BLUE;

    /** Right color. */
    static final Color RIGHT = Color.GREEN;

    /** Unknown color. */
    static final Color UNKNOWN = Color.WHITE;

    /** The legend. */
    private static final List<LegendEntry> LEGEND;

    static
    {
        LEGEND = new ArrayList<>(3);
        LEGEND.add(new LegendEntry(LEFT, "Left", "Left"));
        LEGEND.add(new LegendEntry(RIGHT, "Right", "Right"));
        LEGEND.add(new LegendEntry(OTHER, "Other", "Other"));
        LEGEND.add(new LegendEntry(UNKNOWN, "Unknown", "Unknown"));
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final GTU gtu)
    {
        if (!(gtu instanceof LaneBasedGTU))
        {
            return UNKNOWN;
        }
        LaneBasedGTU laneGtu = (LaneBasedGTU) gtu;
        LinkDirection linkDir;
        try
        {
            DirectedLanePosition pos = laneGtu.getReferencePosition();
            linkDir = new LinkDirection(pos.getLane().getParentLink(), pos.getGtuDirection());
        }
        catch (GTUException exception)
        {
            return UNKNOWN;
        }

        // get all links we can go in to
        Set<Link> nextLinks;
        Link preLink;
        do
        {
            try
            {
                preLink = linkDir.getLink();
                nextLinks = linkDir.getNodeTo().nextLinks(gtu.getGTUType(), linkDir.getLink());
                if (!nextLinks.isEmpty())
                {
                    linkDir = laneGtu.getStrategicalPlanner().nextLinkDirection(preLink, linkDir.getDirection(),
                            gtu.getGTUType());
                }
            }
            catch (NetworkException exception)
            {
                System.err.println("Network exception while defining split color for GTU.");
                return UNKNOWN;
            }
        }
        while (nextLinks.size() == 1);

        // dead end
        if (nextLinks.isEmpty())
        {
            return UNKNOWN;
        }

        // split, sort next links
        try
        {
            double preAngle =
                    linkDir.getDirection().isPlus() ? linkDir.getLink().getDesignLine().getLocationFraction(1.0).getRotZ()
                            : linkDir.getLink().getDesignLine().getLocationFraction(0.0).getRotZ() - Math.PI;
            OTSPoint3D pre = linkDir.getDirection().isPlus() ? linkDir.getLink().getDesignLine().getLast()
                    : linkDir.getLink().getDesignLine().getFirst();
            List<Double> angles = new ArrayList<>();
            List<Link> links = new ArrayList<>();
            for (Link nextLink : nextLinks)
            {
                double angle = getAngle(pre, nextLink.getStartNode().equals(linkDir.getNodeFrom())
                        ? nextLink.getDesignLine().get(1) : nextLink.getDesignLine().get(nextLink.getDesignLine().size() - 2));
                angle -= preAngle; // difference with from
                while (angle < -Math.PI)
                {
                    angle += Math.PI * 2;
                }
                while (angle > Math.PI)
                {
                    angle -= Math.PI * 2;
                }
                if (angles.isEmpty() || angle < angles.get(0))
                {
                    angles.add(0, angle);
                    links.add(0, nextLink);
                }
                else if (angle > angles.get(angles.size() - 1))
                {
                    angles.add(angle);
                    links.add(nextLink);
                }
                else
                {
                    for (double a : angles)
                    {
                        if (a > angle)
                        {
                            int index = angles.indexOf(angle);
                            angles.add(index, angle);
                            links.add(index, nextLink);
                        }
                    }
                }
            }
            int index = links.indexOf(linkDir.getLink());
            if (index == 0)
            {
                return LEFT;
            }
            else if (index == links.size() - 1)
            {
                return RIGHT;
            }
            return OTHER;
        }
        catch (OTSGeometryException exception)
        {
            // should not happen as the fractions are 0.0 and 1.0
            throw new RuntimeException("Angle could not be calculated.", exception);
        }
    }

    /**
     * Returns the angle between two points.
     * @param from
     * @param to
     * @return angle between two points
     */
    private final double getAngle(final OTSPoint3D from, final OTSPoint3D to)
    {
        return Math.atan2(to.x - from.x, to.y - from.y);
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Split";
    }

}
