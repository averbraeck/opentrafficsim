package org.opentrafficsim.road.gtu.lane.object;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.road.gtu.lane.object.animation.TrafficLightAnimation;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OldTrafficLight extends TrafficLight
{

    /**
     * @param id
     * @param lane
     * @param position
     * @param simulator
     * @throws OTSGeometryException
     */
    public OldTrafficLight(final String id, final Lane lane, final Length.Rel position,
        final OTSDEVSSimulatorInterface simulator) throws OTSGeometryException
    {
        super(AbstractCSEObject.createRectangleOnCSE(lane, position, new Length.Rel(0.5, LengthUnit.METER), lane
            .getWidth(position).multiplyBy(0.8), new Length.Rel(0.5, LengthUnit.METER)), new Length.Rel(0.5,
            LengthUnit.METER), TrafficLightColor.RED);

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
}
