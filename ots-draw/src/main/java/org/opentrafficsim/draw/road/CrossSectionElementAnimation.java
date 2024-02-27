package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Set;

import javax.naming.NamingException;

import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsRenderable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws cross section elements (those that are not defined more specifically).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <L> cross section element data type
 */
public class CrossSectionElementAnimation<L extends CrossSectionElementData> extends OtsRenderable<L>
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** The animation color. */
    private final Color color;

    /** Drawable paths. */
    private final Set<Path2D.Float> paths;

    /**
     * @param source L; cross section element
     * @param contextualized Contextualized; context provider
     * @param color Color; the color to draw the shoulder with
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public CrossSectionElementAnimation(final L source, final Contextualized contextualized, final Color color)
            throws NamingException, RemoteException
    {
        super(source, contextualized);
        this.color = color;
        this.paths = PaintPolygons.getPaths(getSource().getBounds().asPolygon().getPointList());
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        setRendering(graphics);
        PaintPolygons.paintPaths(graphics, this.color, this.paths, true);
        // drawing some extra width by painting the edge (i.e. fill = false) prevents anti-alias lines between adjacent elements
        double scale = Math.min(Math.max(3.0 / graphics.getTransform().getDeterminant(), 0.1), 0.5);
        graphics.setStroke(new BasicStroke((float) scale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
        PaintPolygons.paintPaths(graphics, this.color, this.paths, false);
        resetRendering(graphics);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "CrossSectionElementAnimation [source = " + getSource().toString() + ", color=" + this.color + "]";
    }

    /**
     * CrossSectionElementData provides the information required to draw a cross section element.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface CrossSectionElementData extends OtsLocatable
    {
        /**
         * Returns the center line.
         * @return PolyLine2d; center line.
         */
        PolyLine2d getCenterLine();

        /**
         * Return the id of the link.
         * @return String; link id.
         */
        String getLinkId();
    }

    /**
     * ShoulderData provides the information required to draw a shoulder.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
