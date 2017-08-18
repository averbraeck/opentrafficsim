package org.opentrafficsim.road.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.SpeedUnit;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.lane.object.SpeedSign;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SpeedSignAnimation extends Renderable2D<SpeedSign> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20170420L;

    /** Radius in meters. */
    private static final double RADIUS = 1.6;

    /** Radius in meters. */
    private static final double EDGE = 0.8;

    /** Scaling for text. */
    private static final double TEXT_SCALE = 0.14;

    /**
     * @param source speed sign
     * @param simulator simulator
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public SpeedSignAnimation(final SpeedSign source, final OTSSimulatorInterface simulator)
            throws NamingException, RemoteException
    {
        super(source, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D arg0, final ImageObserver arg1) throws RemoteException
    {
        SpeedSign sign = getSource();
        double r = RADIUS;
        Ellipse2D ellipse = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
        arg0.setColor(Color.RED);
        arg0.fill(ellipse);
        r *= EDGE;
        ellipse = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
        arg0.setColor(Color.WHITE);
        arg0.fill(ellipse);
        arg0.setColor(Color.BLACK);
        arg0.scale(TEXT_SCALE, TEXT_SCALE);
        String str = Integer.toString((int) sign.getSpeed().getInUnit(SpeedUnit.KM_PER_HOUR));
        int width = arg0.getFontMetrics().stringWidth(str);
        int height = arg0.getFontMetrics().getAscent();
        // not sure why 0.7 is required, getAscent() alone is weird and refers to more than the height of numbers
        arg0.drawString(str, (float) (-.5 * width), (float) (0.5 * 0.7 * height));
        arg0.scale(1.0 / TEXT_SCALE, 1.0 / TEXT_SCALE);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SpeedSignAnimation";
    }

}
