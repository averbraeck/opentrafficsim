package org.opentrafficsim.draw.network;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.util.function.Supplier;

import org.djutils.base.Identifiable;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.draw.RenderableTextSource;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws NodeData.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class NodeAnimation extends OtsRenderable<NodeData>
{
    /** the Text object to destroy when the animation is destroyed. */
    private Text text;

    /**
     * Constructor.
     * @param node node data.
     * @param contextualized context provider
     */
    public NodeAnimation(final NodeData node, final Contextualized contextualized)
    {
        super(node, contextualized);
        this.text = new Text(node, node::getId, 0.0f, 3.0f, TextAlignment.CENTER, Color.BLACK, contextualized,
                RenderableTextSource.RENDERWHEN10);
        setScaleY(false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isScaleY()
    {
        return super.isScaleY();
    }

    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        setRendering(graphics);
        double scale = Math.sqrt(graphics.getTransform().getDeterminant());
        double factor = 4.0 / Math.min(scale, 4.0); // do not make smaller when zooming out below scale 4
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke((float) (factor * 0.5), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        graphics.draw(new Ellipse2D.Double(-0.5 * factor, -0.5 * factor, 1.0 * factor, 1.0 * factor));
        if (!getSource().isCentroid())
        {
            double direction = getSource().getLocation().getDirZ();
            if (!Double.isNaN(direction))
            {
                GeneralPath arrow = new GeneralPath(Path2D.WIND_EVEN_ODD, 3);
                arrow.moveTo(0.7 * factor, -0.7 * factor);
                arrow.lineTo(1.4 * factor, 0.0);
                arrow.lineTo(0.7 * factor, 0.7 * factor);
                graphics.setStroke(new BasicStroke((float) (factor * 0.25), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                graphics.draw(arrow);
            }
        }
        resetRendering(graphics);
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
        return "NodeAnimation [node=" + super.getSource() + "]";
    }

    /**
     * Text animation for the Node. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class Text extends RenderableTextSource<NodeData, Text>
    {
        /**
         * Constructor.
         * @param source the object for which the text is displayed
         * @param text the text to display
         * @param dx the horizontal movement of the text, in meters
         * @param dy the vertical movement of the text, in meters
         * @param textPlacement where to place the text
         * @param color the color of the text
         * @param contextualized context provider
         * @param scaleDependentRendering size limiter for text animation
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public Text(final NodeData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized,
                final ScaleDependentRendering scaleDependentRendering)
        {
            super(source, text, dx, dy, textPlacement, color, 2.0f, 12.0f, 50f, contextualized, scaleDependentRendering);
            setFlip(false);
            setRotate(false);
        }

        @Override
        public final String toString()
        {
            return "NodeAnimation.Text []";
        }
    }

    /**
     * NodeData provides the information required to draw a node.
     * <p>
     * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface NodeData extends OtsShape, Identifiable
    {
        @Override
        DirectedPoint2d getLocation();

        /**
         * Returns whether this node is a centroid.
         * @return whether this node is a centroid
         */
        boolean isCentroid();

        @Override
        default double signedDistance(final Point2d point)
        {
            return Math.hypot(point.x, point.y);
        }

        @Override
        default boolean contains(final Point2d point)
        {
            return signedDistance(point) < WORLD_MARGIN_LINE;
        }

        @Override
        default double getZ()
        {
            return DrawLevel.NODE.getZ();
        }
    }

}
