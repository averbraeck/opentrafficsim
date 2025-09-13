package org.opentrafficsim.draw.network;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.util.function.Supplier;

import org.djutils.base.Identifiable;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.LineLocatable;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.draw.PaintLine;
import org.opentrafficsim.draw.RenderableTextSource;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws LinkData.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LinkAnimation extends OtsRenderable<LinkData>
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** Width. */
    private float width;

    /** the Text object to destroy when the animation is destroyed. */
    private Text text;

    /** Drawable path. */
    private Path2D.Float path;

    /** Drawable path for start point. */
    private Path2D.Float startPoint;

    /** Drawable path for end point. */
    private Path2D.Float endPoint;

    /** Color. */
    private final Color color;

    /** Design line, also cached in parent, this is to see if it has changed. */
    private PolyLine2d designLine;

    /** Whether animation is dynamic. */
    private boolean dynamic = false;

    /**
     * Constructor.
     * @param link link data.
     * @param contextualized context provider.
     * @param width width
     */
    public LinkAnimation(final LinkData link, final Contextualized contextualized, final float width)
    {
        super(link, contextualized);
        this.width = width;
        this.text = new Text(link, link::getId, 0.0f, 1.5f, TextAlignment.CENTER, Color.BLACK, contextualized,
                RenderableTextSource.RENDERWHEN10);
        setPath();
        this.color = getSource().isConnector() ? Color.PINK.darker() : Color.BLUE;
    }

    /**
     * Sets the animation as dynamic, obtaining geometry at each draw.
     * @param dynamic whether it is dynamic {@code false} by default.
     * @return for method chaining.
     */
    @SuppressWarnings("all")
    public LinkAnimation setDynamic(final boolean dynamic)
    {
        this.dynamic = dynamic;
        return this;
    }

    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (this.dynamic)
        {
            PolyLine2d designLine = getSource().getCenterLine();
            if (!designLine.equals(this.designLine))
            {
                setPath();
            }
        }
        setRendering(graphics);
        double scale = Math.sqrt(graphics.getTransform().getDeterminant());
        double factor = 2.0 / Math.min(scale, 2.0); // do not make smaller when zooming out below scale 3
        PaintLine.paintLine(graphics, this.color, factor * this.width, this.path);
        PaintLine.paintLine(graphics, this.color, this.width / 30, this.startPoint);
        PaintLine.paintLine(graphics, this.color, this.width / 30, this.endPoint);
        resetRendering(graphics);
    }

    /**
     * Sets drawable paths.
     */
    private void setPath()
    {
        this.designLine = getSource().getCenterLine();
        this.path = PaintLine.getPath(getSource().getLocation(), this.designLine);
        this.startPoint = getEndPoint(this.designLine.getFirst(), this.designLine.get(1));
        this.endPoint = getEndPoint(this.designLine.getLast(), this.designLine.get(this.designLine.size() - 2));
    }

    /**
     * @param endPoint the end of the design line where a end point must be highlighted
     * @param nextPoint the point nearest <code>endPoint</code> (needed to figure out the direction of the design line)
     * @return Path2D.Float; path to draw an end point.
     */
    private Path2D.Float getEndPoint(final Point2d endPoint, final Point2d nextPoint)
    {
        double dx = nextPoint.x - endPoint.x;
        double dy = nextPoint.y - endPoint.y;
        double length = endPoint.distance(nextPoint);
        // scale dx, dy so that size is this.width
        dx *= this.width / length;
        dy *= this.width / length;
        PolyLine2d line =
                new PolyLine2d(new Point2d(endPoint.x - dy, endPoint.y + dx), new Point2d(endPoint.x + dy, endPoint.y - dx));
        return PaintLine.getPath(getSource().getLocation(), line);
    }

    @Override
    public void destroy(final Contextualized contextProvider)
    {
        super.destroy(contextProvider);
        this.text.destroy(contextProvider);
    }

    @Override
    public final String toString()
    {
        return "LinkAnimation [width=" + this.width + ", link=" + super.getSource() + "]";
    }

    /**
     * Text animation for the Link. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class Text extends RenderableTextSource<LinkData, Text>
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * Constructor.
         * @param source the object for which the text is displayed
         * @param text the text to display
         * @param dx the horizontal movement of the text, in meters
         * @param dy the vertical movement of the text, in meters
         * @param textPlacement where to place the text
         * @param color the color of the text
         * @param contextualized context provider.
         * @param scaleDependentRendering enables rendering in a scale dependent fashion
         */
        public Text(final LinkData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized,
                final ScaleDependentRendering scaleDependentRendering)
        {
            super(source, text, dx, dy, textPlacement, color, 2.0f, 12.0f, 50f, contextualized, null, scaleDependentRendering);
        }

        @Override
        public final String toString()
        {
            return "LinkAnimation.Text []";
        }
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
    public interface LinkData extends LineLocatable, Identifiable
    {
        @Override
        DirectedPoint2d getLocation();

        /**
         * Returns whether this is a connector.
         * @return whether this is a connector.
         */
        boolean isConnector();

        /**
         * Returns the center line in world coordinates.
         * @return the center line in world coordinates.
         */
        PolyLine2d getCenterLine();

        @Override
        default double getZ()
        {
            return DrawLevel.LINK.getZ();
        }
    }

}
