package org.opentrafficsim.road.network.lane.object.sensor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * sink sensor animation.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-12 16:37:45 +0200 (Wed, 12 Aug 2015) $, @version $Revision: 1240 $, by $Author: averbraeck $,
 * initial version Jan 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SensorAnimation extends Renderable2D implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** The half width left and right of the center line that is used to draw the block. */
    private final double halfWidth;

    /** The color of the sensor. */
    private final Color color;

    /**
     * Construct a SensorAnimation.
     * @param source Sensor; the Sensor to draw
     * @param sensorPosition Length; the position of the sensor on the lane to determine the width of the lane at that point
     * @param simulator OTSSimulatorInterface; the simulator to schedule on
     * @param color Color; the display color of the sensor
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public SensorAnimation(final Sensor source, final Length sensorPosition, final OTSSimulatorInterface simulator,
            final Color color) throws NamingException, RemoteException
    {
        super(source, simulator);
        this.halfWidth = 0.45 * source.getLane().getWidth(sensorPosition).getSI();
        this.color = color;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(this.color);
        // TODO: this is strange... Why not Rectangle2D.Double(-this.halfWidth, -0.25, 2 * this.halfWidth, 0.5) ???
        Rectangle2D rectangle = new Rectangle2D.Double(-0.25, -this.halfWidth, 0.5, 2 * this.halfWidth);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SensorAnimation [getSource()=" + this.getSource() + "]";
    }
}
