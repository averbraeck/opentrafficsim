package org.opentrafficsim.road.gtu.lane.object;

import java.rmi.RemoteException;
import java.util.UUID;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.object.animation.DefaultBlockAnimation;
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
public class LaneBlock extends AbstractTrafficLight
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param lane The lane where the block has to be put
     * @param position the position on the lane as a length
     * @param simulator the simulator to avoid NullPointerExceptions
     * @param animationClass Class&lt;? extends Renderable2D&gt;; the class for animation or null if no animation
     * @param network the network that the GTU is initially registered in
     * @throws GTUException when GTU cannot be created.
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws OTSGeometryException x
     * @throws SimRuntimeException x
     */
    public LaneBlock(final Lane lane, final Length position, final OTSDEVSSimulatorInterface simulator,
        final Class<? extends Renderable2D> animationClass, final OTSNetwork network)
        throws GTUException, NetworkException, NamingException, SimRuntimeException, OTSGeometryException
    {
        super(UUID.randomUUID().toString(), lane, position, simulator, network);
        try
        {
            new DefaultBlockAnimation(this, getSimulator());
            if (simulator instanceof OTSAnimatorInterface && animationClass != null)
            {
                // TODO use animationClass
            }
        }
        catch (RemoteException exception)
        {
            throw new NetworkException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBlock [lane=" + this.laneTL + ", position=" + this.positionTL + "]";
    }

}
