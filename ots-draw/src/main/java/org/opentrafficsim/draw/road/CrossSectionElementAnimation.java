package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws cross section elements (those that are not defined more specifically).
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CrossSectionElementAnimation extends Renderable2d<CrossSectionElementData> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** The animation color. */
    private final Color color;

    /** Drawable paths. */
    private final Set<Path2D.Double> paths;

    /**
     * @param source CrossSectionElementData; cross section element
     * @param contextualized Contextualized; context provider
     * @param color Color; the color to draw the shoulder with
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public CrossSectionElementAnimation(final CrossSectionElementData source, final Contextualized contextualized,
            final Color color) throws NamingException, RemoteException
    {
        super(source, contextualized);
        this.color = color;
        this.paths = PaintPolygons.getPaths(getSource().getLocation(), getSource().getContour());
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        PaintPolygons.paintPaths(graphics, this.color, this.paths, true);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CrossSectionElementAnimation [source = " + getSource().toString() + ", color=" + this.color + "]";
    }

    /**
     * CrossSectionElementData provides the information required to draw a cross section element.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface CrossSectionElementData extends Locatable
    {
        /**
         * Returns the contour.
         * @return List&lt;Point2d&gt;; points.
         */
        List<Point2d> getContour();

        /** {@inheritDoc} */
        @Override
        Point2d getLocation();
    }

    /**
     * ShoulderData provides the information required to draw a shoulder.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface ShoulderData extends CrossSectionElementData
    {
        /** {@inheritDoc} */
        @Override
        default public double getZ()
        {
            return DrawLevel.SHOULDER.getZ();
        }
    }
}
