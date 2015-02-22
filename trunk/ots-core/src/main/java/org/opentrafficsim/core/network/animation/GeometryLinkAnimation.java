package org.opentrafficsim.core.network.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.geotools.LinkGeotools;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class GeometryLinkAnimation extends Renderable2D
{
    /** */
    private float width;

    /**
     * @param source Link
     * @param simulator simulator
     * @param width width
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communications failure
     */
    public GeometryLinkAnimation(final LinkGeotools source, final OTSSimulatorInterface simulator, final float width)
        throws NamingException, RemoteException
    {
        super(source, simulator);
        this.width = width;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.RED);
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(this.width));
        DirectedPoint p = ((LinkGeotools) getSource()).getLocation();
        boolean start = false;
        Path2D.Double path = new Path2D.Double();
        for (Coordinate c : ((LinkGeotools) getSource()).getGeometry().getLineString().getCoordinates())
        {
            if (!start)
            {
                start = true;
                path.moveTo(c.x - p.x, -c.y + p.y);
            }
            else
            {
                path.lineTo(c.x - p.x, -c.y + p.y);
            }
        }
        graphics.draw(path);
        graphics.setStroke(oldStroke);
    }

}
