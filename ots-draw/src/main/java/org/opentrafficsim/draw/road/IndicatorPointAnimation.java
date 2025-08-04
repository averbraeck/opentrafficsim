package org.opentrafficsim.draw.road;

import nl.tudelft.simulation.naming.context.Contextualized;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsRenderable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.road.IndicatorPointAnimation.IndicatorPointData;

import javax.naming.NamingException;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class IndicatorPointAnimation extends OtsRenderable<IndicatorPointData>
{

    /** */
    private static final long serialVersionUID = 202508042L;

    /** Radius in meters. */
    private static final double EDGE = 2;

    /**
     * @param source IndicatorPointData; speed sign
     * @param contextualized Contextualized; context provider
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public IndicatorPointAnimation(final IndicatorPointData source, final Contextualized contextualized)
            throws NamingException, RemoteException
    {
        super(source, contextualized);
        setRotate(false);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D g, final ImageObserver arg1)
    {
        setRendering(g);
        Ellipse2D ellipse = new Ellipse2D.Double(-EDGE, -EDGE, 2 * EDGE, 2 * EDGE);
        g.setColor(Color.GREEN);
        g.fill(ellipse);
        g.setColor(Color.BLACK);
        resetRendering(g);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IndicatorPointAnimation";
    }

    /**
     * LinkData provides the information required to draw a link.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface IndicatorPointData extends OtsLocatable
    {
    }

}
