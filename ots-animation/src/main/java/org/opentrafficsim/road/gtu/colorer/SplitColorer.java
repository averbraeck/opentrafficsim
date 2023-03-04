package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
        Link link = refPos.getLane().getParentLink();
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
                // System.err.println("Network exception while defining split color for GTU.");
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
            double preAngle = link.getDesignLine().getLocationFraction(1.0).getRotZ();
            OtsPoint3d pre = link.getDesignLine().getLast();
            List<Double> angles = new ArrayList<>();
            List<Link> links = new ArrayList<>();
            for (Link nextLink : nextLinks)
            {
                double angle = getAngle(pre, nextLink.getStartNode().equals(link.getStartNode())
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
            int index = links.indexOf(link);
            if (index == 0)
            {
                return RIGHT;
            }
            else if (index == links.size() - 1)
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

    /**
     * Returns the angle between two points.
     * @param from OtsPoint3d; from point
     * @param to OtsPoint3d; to point
     * @return angle between two points
     */
    private double getAngle(final OtsPoint3d from, final OtsPoint3d to)
    {
        return Math.atan2(to.x - from.x, to.y - from.y);
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
