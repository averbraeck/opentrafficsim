package nl.grontmij.smarttraffic.lane;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.lane.AbstractTrafficLight;
import org.opentrafficsim.core.gtu.lane.LaneBlockOnOff;
import org.opentrafficsim.core.network.lane.AbstractSensor;

/**
 * Draw a road block.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CheckSensorAnimation extends Renderable2D
{
    /** the half width left and right of the center line that is used to draw the block. */
    private final double halfWidth;

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param sensor the Car to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public CheckSensorAnimation(final CheckSensor sensor, final OTSSimulatorInterface simulator) throws NamingException,
        RemoteException
    {
        super(sensor, simulator);
        this.halfWidth = 0.4 * sensor.getLane().getWidth(0.0).getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        if ( ((CheckSensor) this.source).getCurrentStatus()==0)
        {
            graphics.setColor(Color.BLUE);
        }
        else
        {
            graphics.setColor(Color.MAGENTA);
        }
//        graphics.setColor(Color.BLUE);
        Rectangle2D rectangle = new Rectangle2D.Double(-0.25, -this.halfWidth, 0.5, 2 * this.halfWidth);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CheckSensorAnimation [getSource()=" + this.getSource() + "]";
    }

}
