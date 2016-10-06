package org.opentrafficsim.road.network.lane.object.trafficlight;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractCSEObject;
import org.opentrafficsim.road.network.lane.object.animation.TrafficLightAnimation;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimpleTrafficLight extends AbstractCSEObject implements TrafficLight
{
    /** */
    private static final long serialVersionUID = 201601001L;

    /** The color of the traffic light. */
    private TrafficLightColor trafficLightColor;

    /**
     * @param id traffic light id
     * @param lane lane where the traffic light is located
     * @param position position of the traffic light on the lane, in the design direction
     * @param simulator simulator on which to schedule color changes
     * @throws OTSGeometryException on failure to place the object
     */
    public SimpleTrafficLight(final String id, final Lane lane, final Length position,
            final OTSDEVSSimulatorInterface simulator) throws OTSGeometryException
    {
        super(AbstractCSEObject.createRectangleOnCSE(lane, position, new Length(0.5, LengthUnit.METER),
                lane.getWidth(position).multiplyBy(0.8), new Length(0.5, LengthUnit.METER)), new Length(0.5, LengthUnit.METER));
        this.trafficLightColor = TrafficLightColor.RED;

        try
        {
            new TrafficLightAnimation(this, simulator);
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
        catch (NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final TrafficLightColor getTrafficLightColor()
    {
        return this.trafficLightColor;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTrafficLightColor(final TrafficLightColor trafficLightColor)
    {
        this.trafficLightColor = trafficLightColor;
    }

}
