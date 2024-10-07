package org.opentrafficsim.animation.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * Colors GTUs by the direction that they will take at the next split.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class SplitColorer implements GtuColorer
{

    /** Left color. */
    static final Color LEFT = Color.GREEN;

    /** Other color. */
    static final Color OTHER = Color.BLUE;

    /** Right color. */
    static final Color RIGHT = Color.RED;

    /** Unknown color. */
    static final Color UNKNOWN = Color.WHITE;

    /** The legend. */
    private static final List<LegendEntry> LEGEND;

    static
    {
        LEGEND = new ArrayList<>(4);
        LEGEND.add(new LegendEntry(LEFT, "Left", "Left"));
        LEGEND.add(new LegendEntry(RIGHT, "Right", "Right"));
        LEGEND.add(new LegendEntry(OTHER, "Other", "Other"));
        LEGEND.add(new LegendEntry(UNKNOWN, "Unknown", "Unknown"));
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final Gtu gtu)
    {
        if (!(gtu instanceof LaneBasedGtu))
        {
            return UNKNOWN;
        }
        LaneBasedGtu laneGtu = (LaneBasedGtu) gtu;
        LanePosition refPos;
        try
        {
            refPos = laneGtu.getReferencePosition();
        }
        catch (GtuException exception)
        {
            return UNKNOWN;
        }
        Link link = refPos.lane().getLink();
        Route route = laneGtu.getStrategicalPlanner().getRoute();
        if (route == null)
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
                preLink = link;
                nextLinks = link.getEndNode().nextLinks(gtu.getType(), link);
                if (!nextLinks.isEmpty())
                {
                    link = laneGtu.getStrategicalPlanner().nextLink(preLink, gtu.getType());
                }
            }
            catch (NetworkException exception)
            {
                return UNKNOWN;
            }
        }
        while (nextLinks.size() == 1);

        // dead end
        if (nextLinks.isEmpty())
        {
            return UNKNOWN;
        }

        // split
        try
        {
            double preAngle = preLink.getDesignLine().getLocationFraction(1.0).getDirZ();
            double angleLeft = 0.0;
            double angleRight = 0.0;
            Link linkLeft = null;
            Link linkRight = null;
            for (Link nextLink : nextLinks)
            {
                double angle = nextLink.getStartNode().equals(link.getStartNode())
                        ? nextLink.getDesignLine().getLocationFraction(0.0).getDirZ()
                        : nextLink.getDesignLine().getLocationFraction(1.0).getDirZ() + Math.PI;
                angle -= preAngle; // difference with from
                while (angle < -Math.PI)
                {
                    angle += Math.PI * 2;
                }
                while (angle > Math.PI)
                {
                    angle -= Math.PI * 2;
                }
                if (angle < angleRight)
                {
                    angleRight = angle;
                    linkRight = nextLink;
                }
                else if (angle > angleLeft)
                {
                    angleLeft = angle;
                    linkLeft = nextLink;
                }
            }
            if (link.equals(linkRight))
            {
                return RIGHT;
            }
            else if (link.equals(linkLeft))
            {
                return LEFT;
            }
            return OTHER;
        }
        catch (OtsGeometryException exception)
        {
            // should not happen as the fractions are 0.0 and 1.0
            throw new RuntimeException("Angle could not be calculated.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Split";
    }

}
