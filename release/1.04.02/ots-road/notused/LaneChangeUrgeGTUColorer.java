package org.opentrafficsim.road.gtu.animation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.animation.ColorInterpolator;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Color GTUs based on their urgency to perform a lane change and the direction of that lane change. <br>
 * Currently, lane change urge depends solely on the intended route; not on keep right conventions, etc.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 3 jun. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneChangeUrgeGTUColorer implements GTUColorer
{
    /** The legend. */
    private final ArrayList<LegendEntry> legend;

    /** Minimum distance . */
    private final Length minimumLaneChangeDistance;

    /** Maximum distance . */
    private final Length horizon;

    /** Color for GTUs that are not lane based (and, consequently cannot have a lane change urge). */
    private static final Color NOTLANEBASEDGTUCOLOR = Color.BLACK;

    /**
     * Construct a new LaneChangeUrgeGTUColorer.
     * @param minimumLaneChangeDistance Length; the minimum distance that a GTU requires to perform
     *            a lane change
     * @param horizon Length; the distance horizon; if a GTU can stay in its current lane for at
     *            least this distance, this GTU will be painted in the neutral color.
     */
    public LaneChangeUrgeGTUColorer(final Length minimumLaneChangeDistance, final Length horizon)
    {
        this.minimumLaneChangeDistance = minimumLaneChangeDistance;
        this.horizon = horizon;
        Color[] colorTable = {Color.RED, Color.GRAY, Color.GREEN};
        String[] labelTable = {"left", "neutral", "right"};
        if (colorTable.length != labelTable.length)
        {
            throw new RuntimeException("Length of colorTable must be equal to length of labelTable");
        }
        this.legend = new ArrayList<LegendEntry>(colorTable.length + 1);
        String minimum = "Lane change should be completed within" + this.minimumLaneChangeDistance.toString();
        for (int index = 0; index < colorTable.length; index++)
        {
            this.legend.add(new LegendEntry(colorTable[index], labelTable[index], 1 == index
                ? "No lane change needed within " + this.horizon.toString() : minimum));
        }
        this.legend.add(new LegendEntry(Color.BLACK, "unknown", "Non lane based GTUs cannot have a valid lane change urge"));
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(GTU gtu) 
    {
        if (gtu instanceof LaneBasedGTU)
        {
            LaneChangeDistanceAndDirection distanceAndDirection = ((LaneBasedGTU) gtu).getLaneChangeDistanceAndDirection();
            Boolean left = distanceAndDirection.getLeft();
            Length distance = distanceAndDirection.getDistance();
            if (null == left || distance.ge(this.horizon))
            {
                return this.legend.get(1).getColor();
            }
            double ratio =
                distance.minus(this.minimumLaneChangeDistance).getSI()
                    / this.horizon.minus(this.minimumLaneChangeDistance).getSI();
            if (ratio < 0) // happens when the vehicle is within the minimumLaneChangeDistance
            {
                ratio = 0;
            }
            return ColorInterpolator.interpolateColor(this.legend.get(distanceAndDirection.getLeft() ? 0 : 2).getColor(),
                this.legend.get(1).getColor(), ratio);
        }
        return NOTLANEBASEDGTUCOLOR;
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return this.legend;
    }

    public final String toString()
    {
        return "Lane change urge";
    }

    /** Pack a distance available for performing a lane change and a direction in one object. */
    public static class LaneChangeDistanceAndDirection
    {
        /** The distance available to complete the next required lane change. */
        public final Length distance;

        /**
         * The lateral direction of the next required lane change is left (if this field is true); right (if this field is
         * false); none (if this field is null)
         */
        public final Boolean left;

        /**
         * Construct a new LaneChangeDistanceAndDirection object.
         * @param distance Length; the distance available for performing a lane change
         * @param left Boolean; if true the lane change to perform is to the left; if false, the lane change to perform is to
         *            the right; if null, no lane change is needed, or possible
         */
        public LaneChangeDistanceAndDirection(final Length distance, final Boolean left)
        {
            this.distance = distance;
            this.left = left;
        }

        /**
         * Retrieve the distance available to complete the next required lane change.
         * @return Length; the distance available to complete the next required lane change
         */
        public final Length getDistance()
        {
            return this.distance;
        }

        /**
         * Retrieve the lateral direction of the next required lane change.
         * @return Boolean; true if the next required lane change is to the left, false if the next required lane change is to
         *         the right, null if no lane change is required (or possible)
         */
        public final Boolean getLeft()
        {
            return this.left;
        }

        public final String toString()
        {
            if (null == this.left || this.distance.getSI() == Double.MAX_VALUE)
            {
                return "No lane change required";
            }
            else
            {
                return String.format("Must change %s within %s", this.left ? "left" : "right", this.distance.toString());
            }
        }
    }

}
