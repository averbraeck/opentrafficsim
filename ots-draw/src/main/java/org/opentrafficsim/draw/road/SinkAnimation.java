package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;

/**
 * sink sensor animation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SinkAnimation extends AbstractLineAnimation<SinkSensor> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /**
     * Construct the Sink animation.
     * @param source SinkSensor; the Car to draw
     * @param simulator OTSSimulatorInterface; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public SinkAnimation(final SinkSensor source, final OTSSimulatorInterface simulator) throws NamingException, RemoteException
    {
        super(source, simulator, 0.8, new Length(0.5, LengthUnit.SI));
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.YELLOW);
        super.paint(graphics, observer);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SinkAnimation [getSource()=" + this.getSource() + "]";
    }
}
