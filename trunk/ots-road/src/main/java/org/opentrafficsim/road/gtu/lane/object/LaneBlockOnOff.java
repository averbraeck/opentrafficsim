package org.opentrafficsim.road.gtu.lane.object;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.object.animation.DefaultBlockOnOffAnimation;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBlockOnOff extends AbstractTrafficLight
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param name the name or id of the traffic light
     * @param lane The lane where the block has to be put
     * @param position the position on the lane as a length
     * @param simulator the simulator to avoid NullPointerExceptions
     * @param network the network that the GTU is initially registered in
     * @throws GTUException when GTU cannot be created.
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws OTSGeometryException x
     * @throws SimRuntimeException x
     */
    public LaneBlockOnOff(final String name, final Lane lane, final Length.Rel position,
        final OTSDEVSSimulatorInterface simulator, final OTSNetwork network) throws GTUException, NetworkException,
        NamingException, SimRuntimeException, OTSGeometryException
    {
        super(name, lane, position, simulator, network);

        try
        {
            new DefaultBlockOnOffAnimation(this, getSimulator());
            // animation
            if (simulator instanceof OTSAnimatorInterface)
            {
                // TODO
            }
            getSimulator().scheduleEventRel(new Time.Rel(30.0, TimeUnit.SECOND), this, this, "changeColorTime", null);
        }
        catch (RemoteException exception)
        {
            throw new NetworkException(exception);
        }
    }

    protected void changeColorTime()
    {
        setBlocked(!isBlocked());

        try
        {
            getSimulator().scheduleEventRel(new Time.Rel(30.0, TimeUnit.SECOND), this, this, "changeColorTime", null);
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LaneBlockOnOff [lane=" + this.laneTL + ", position=" + this.positionTL + "]";
    }

}
