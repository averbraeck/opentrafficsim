package org.opentrafficsim.road.network.lane;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Sensor that prints which GTU triggers it.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-12 16:37:45 +0200 (Wed, 12 Aug 2015) $, @version $Revision: 1240 $, by $Author: averbraeck $,
 * initial version an 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimpleReportingSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /**
     * @param lane the lane that triggers the deletion of the GTU.
     * @param position the position of the sensor
     * @param triggerPosition RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the sensor.
     * @param id the id of the sensor.
     * @param simulator the simulator to enable animation.
     * @throws NetworkException when the position on the lane is out of bounds w.r.t. the center line of the lane
     */
    public SimpleReportingSensor(final String id, final Lane lane, final Length position,
            final RelativePosition.TYPE triggerPosition, final OTSDEVSSimulatorInterface simulator) throws NetworkException
    {
        super(id, lane, position, triggerPosition, simulator);
        try
        {
            new SensorAnimation(this, position, simulator, Color.YELLOW);
        }
        catch (RemoteException | NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void triggerResponse(final LaneBasedGTU gtu)
    {
        //System.out.println(this + " triggered by " + getPositionType().getName() + " of " + gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Sensor [Lane=" + this.getLane() + "]";
    }
}
