package org.opentrafficsim.road.gtu.lane.object;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.road.network.lane.CrossSectionElement;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrafficLight extends AbstractTrafficLightNew
{
    /** The GTU colors for a normal traffic light. */
    public enum TrafficLightColor
    {
        /** GTU needs to stop. */
        RED,

        /** GTU is allowed to continue if it cannot stop anymore. */
        YELLOW,

        /** GTU is allowed to drive. */
        GREEN,

        /** Traffic light is not working. */
        BLACK
    };

    /** The color of the traffic light. */
    private TrafficLightColor trafficLightColor;

    /**
     * @param geometry the geometry of the object
     * @param height the height of the object
     * @param initialColor the initial color of the traffic light
     */
    public TrafficLight(final OTSLine3D geometry, final Length height, final TrafficLightColor initialColor)
    {
        super(geometry, height);
        this.trafficLightColor = initialColor;
    }

    /**
     * @return the trafficLightColor
     */
    public final TrafficLightColor getTrafficLightColor()
    {
        return this.trafficLightColor;
    }

    /**
     * @param trafficLightColor set the trafficLightColor
     */
    public final void setTrafficLightColor(final TrafficLightColor trafficLightColor)
    {
        this.trafficLightColor = trafficLightColor;
    }

    /**
     * @param cse the cross section element, e.g. lane, where the traffic light is located
     * @param position the relative position on the design line of the link for this traffic light
     * @param initialColor the initial color of the traffic light
     * @return a new CrossSectionElementBlock on the right position on the cse
     * @throws OTSGeometryException in case the position is outside the CSE
     */
    public static TrafficLight createTrafficLight(final CrossSectionElement cse, final Length position,
        final TrafficLightColor initialColor) throws OTSGeometryException
    {
        // return new TrafficLight(AbstractCSEObject.createRectangleOnCSE(cse, position, new Length(0.5,
        // LengthUnit.METER), cse.getWidth(position).multiplyBy(0.8), new Length(0.5, LengthUnit.METER)), new
        // Length(0.5, LengthUnit.METER),
        // initialColor);
        return new TrafficLight(AbstractCSEObject.createRectangleOnCSE(cse, position, new Length(0.5,
            LengthUnit.METER), cse.getWidth(position).multiplyBy(0.8), new Length(0.5, LengthUnit.METER)),
            new Length(0.5, LengthUnit.METER), initialColor);
    }
}
