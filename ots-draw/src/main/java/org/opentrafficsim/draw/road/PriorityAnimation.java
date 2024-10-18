package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Map;

import org.opentrafficsim.draw.ClickableLocatable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.draw.road.PriorityAnimation.PriorityData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Animation of conflict priority (which is a link property).
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PriorityAnimation extends OtsRenderable<PriorityData>
{

    /** */
    private static final long serialVersionUID = 20240228L;

    /** Shadow. */
    private static final Color SHADOW = new Color(0, 0, 0, 128);

    /** Shadow x translation. */
    private static final double SHADOW_DX = 0.1;

    /** Shadow y translation. */
    private static final double SHADOW_DY = 0.05;

    /**
     * Constructor.
     * @param source source.
     * @param contextProvider contextualized.
     */
    public PriorityAnimation(final PriorityData source, final Contextualized contextProvider)
    {
        super(source, contextProvider);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRotate()
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (getSource().isNone())
        {
            return;
        }
        setRendering(graphics);
        if (getSource().isAllStop() || getSource().isStop())
        {
            paintOctagon(graphics, 1.0, SHADOW, true, true);
            paintOctagon(graphics, 1.0, Color.WHITE, true, false);
            paintOctagon(graphics, 1.0, Color.BLACK, false, false);
            paintOctagon(graphics, 0.868, new Color(230, 0, 0), true, false);
            paintString(graphics, "STOP", Color.WHITE, 0.9f, getSource().isAllStop() ? -0.1f : 0.0f);
            if (getSource().isAllStop())
            {
                paintString(graphics, "ALL WAY", Color.WHITE, 0.4f, 0.45f);
            }
        }
        else if (getSource().isBusStop())
        {
            graphics.setColor(SHADOW);
            graphics.fill(new Ellipse2D.Double(-1.0 + SHADOW_DX, -1.0 + SHADOW_DY, 2.0, 2.0));
            Color blue = new Color(20, 94, 169);
            graphics.setColor(blue);
            graphics.fill(new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0));
            graphics.setColor(Color.WHITE);
            graphics.setStroke(new BasicStroke(0.04f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(new Ellipse2D.Double(-0.94, -0.94, 1.88, 1.88));
            paintBus(graphics, blue);
        }
        else if (getSource().isPriority())
        {
            paintDiamond(graphics, 1.0, SHADOW, true, true);
            paintDiamond(graphics, 1.0, Color.WHITE, true, false);
            paintDiamond(graphics, 11.0 / 12.0, Color.BLACK, false, false);
            paintDiamond(graphics, 11.0 / 18.0, new Color(255, 204, 0), true, false);
        }
        else if (getSource().isYield())
        {
            paintTriangle(graphics, 1.0, SHADOW, true, true);
            paintTriangle(graphics, 1.0, new Color(230, 0, 0), true, false);
            paintTriangle(graphics, 0.9, Color.WHITE, false, false);
            paintTriangle(graphics, 0.55, Color.WHITE, true, false);
        }
        resetRendering(graphics);
    }

    /**
     * Paint octagon.
     * @param graphics graphics.
     * @param radius radius (half width).
     * @param color color.
     * @param fill fill (or draw line).
     * @param shadow whether this is shadow.
     */
    private void paintOctagon(final Graphics2D graphics, final double radius, final Color color, final boolean fill,
            final boolean shadow)
    {
        double k = Math.tan(Math.PI / 8.0) * radius;
        double dx = shadow ? SHADOW_DX : 0.0;
        double dy = shadow ? SHADOW_DY : 0.0;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(dx + radius, dy);
        path.lineTo(dx + radius, dy + k);
        path.lineTo(dx + k, dy + radius);
        path.lineTo(dx - k, dy + radius);
        path.lineTo(dx - radius, dy + k);
        path.lineTo(dx - radius, dy - k);
        path.lineTo(dx - k, dy - radius);
        path.lineTo(dx + k, dy - radius);
        path.lineTo(dx + radius, dy - k);
        path.lineTo(dx + radius, dy);
        graphics.setColor(color);
        if (fill)
        {
            graphics.fill(path);
        }
        else
        {
            graphics.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            graphics.draw(path);
        }
    }

    /**
     * Paints a bus.
     * @param graphics graphics.
     * @param blue color used for blue background.
     */
    private void paintBus(final Graphics2D graphics, final Color blue)
    {
        // bus
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0.77, -0.07);
        path.lineTo(0.74, -0.36);
        path.lineTo(-0.69, -0.36);
        path.lineTo(-0.77, -0.07);
        path.lineTo(-0.77, 0.22);
        path.lineTo(0.43, 0.22);
        path.lineTo(0.77, 0.17);
        path.lineTo(0.77, -0.07);
        graphics.fill(path);
        // wheels
        graphics.fill(new Ellipse2D.Double(-0.43 - 0.125, 0.22 - 0.125, 0.25, 0.25));
        graphics.fill(new Ellipse2D.Double(0.43 - 0.125, 0.22 - 0.125, 0.25, 0.25));
        graphics.setColor(blue);
        graphics.setStroke(new BasicStroke(0.015f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.draw(new Ellipse2D.Double(-0.43 - 0.125, 0.22 - 0.125, 0.25, 0.25));
        graphics.draw(new Ellipse2D.Double(0.43 - 0.125, 0.22 - 0.125, 0.25, 0.25));
        // windows
        graphics.setColor(blue);
        path = new Path2D.Double();
        path.moveTo(-0.52, -0.32);
        path.lineTo(-0.66, -0.32);
        path.lineTo(-0.73, -0.07);
        path.lineTo(-0.52, -0.07);
        path.lineTo(-0.52, -0.32);
        graphics.fill(path);
        for (double x : new double[] {-0.48, -0.23, 0.02, 0.27})
        {
            graphics.fill(new Rectangle.Double(x, -0.32, 0.21, 0.21));
        }
        path = new Path2D.Double();
        path.moveTo(0.71, -0.32);
        path.lineTo(0.52, -0.32);
        path.lineTo(0.52, -0.11);
        path.lineTo(0.73, -0.11);
        path.lineTo(0.71, -0.32);
        graphics.fill(path);
    }

    /**
     * Paint diamond.
     * @param graphics graphics.
     * @param radius radius (half width).
     * @param color color.
     * @param fill fill (or draw line).
     * @param shadow whether this is shadow.
     */
    private void paintDiamond(final Graphics2D graphics, final double radius, final Color color, final boolean fill,
            final boolean shadow)
    {
        double dx = shadow ? SHADOW_DX : 0.0;
        double dy = shadow ? SHADOW_DY : 0.0;
        graphics.setColor(color);
        if (fill)
        {
            Path2D.Float path = new Path2D.Float();
            path.moveTo(dx + radius, dy);
            path.lineTo(dx, dy + radius);
            path.lineTo(dx - radius, dy);
            path.lineTo(dx, dy - radius);
            path.lineTo(dx + radius, dy);
            graphics.fill(path);
        }
        else
        {
            // to assist rounded corners, we rotate by 1/8th circle and use RoundRectangle2D
            graphics.rotate(Math.PI / 4);
            graphics.setStroke(new BasicStroke(0.04f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            double r = radius / Math.sqrt(2.0); // diagonal vs. base
            RoundRectangle2D.Double shape = new RoundRectangle2D.Double(-r, -r, 2.0 * r, 2.0 * r, 0.15 * r, 0.15 * r);
            graphics.draw(shape);
            graphics.rotate(-Math.PI / 4);
        }
    }

    /**
     * Paint triangle.
     * @param graphics graphics.
     * @param radius radius (half width).
     * @param color color.
     * @param fill fill (or draw line).
     * @param shadow whether this is shadow.
     */
    private void paintTriangle(final Graphics2D graphics, final double radius, final Color color, final boolean fill,
            final boolean shadow)
    {
        double k = radius * Math.sqrt(3.0) / 3.0;
        double g = (radius * Math.sqrt(3.0)) - k;
        double dx = shadow ? SHADOW_DX : 0.0;
        double dy = shadow ? SHADOW_DY : 0.0;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(dx + 0.0, dy - k);
        path.lineTo(dx + -radius, dy - k);
        path.lineTo(dx + 0.0, dy + g);
        path.lineTo(dx + radius, dy - k);
        path.lineTo(dx + 0.0, dy - k);
        graphics.setColor(color);
        if (fill)
        {
            graphics.fill(path);
        }
        else
        {
            graphics.setStroke(new BasicStroke(0.04f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            graphics.draw(path);
        }
    }

    /**
     * Paint string.
     * @param graphics graphics.
     * @param text text.
     * @param color color.
     * @param fontSize font size.
     * @param dy distance down from object location.
     */
    private void paintString(final Graphics2D graphics, final String text, final Color color, final float fontSize,
            final float dy)
    {
        if (graphics.getTransform().getDeterminant() > 400000)
        {
            // TODO
            /*
             * If we are very zoomed in, the font gets huge on screen. FontMetrics somehow uses this actual image size in Java
             * 11, and this gives a bug for fonts above a certain size. Dimensions become 0, and this does not recover after we
             * zoom out again. The text never shows anymore. A later java version may not require skipping painting the font.
             * See more at: https://bugs.openjdk.org/browse/JDK-8233097
             */
            return;
        }
        graphics.setColor(color);
        int fontSizeMetrics = 100;
        float factor = fontSize / fontSizeMetrics;
        Font font = new Font("Arial", Font.BOLD, fontSizeMetrics).deriveFont(Map.of(TextAttribute.WIDTH, 0.67f));
        graphics.setFont(font.deriveFont(fontSize));
        FontMetrics metrics = graphics.getFontMetrics(font);
        float w = metrics.stringWidth(text) * factor;
        float d = metrics.getDescent() * factor;
        float h = metrics.getHeight() * factor;
        graphics.drawString(text, -w / 2.0f, dy + h / 2.0f - d);
    }

    /**
     * Data for priority animation.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface PriorityData extends ClickableLocatable
    {

        /** {@inheritDoc} */
        @Override
        default double getZ() throws RemoteException
        {
            return DrawLevel.NODE.getZ();
        }

        /**
         * Returns whether the priority is all stop.
         * @return whether the priority is all stop.
         */
        boolean isAllStop();

        /**
         * Returns whether the priority is bus stop.
         * @return whether the priority is bus stop.
         */
        boolean isBusStop();

        /**
         * Returns whether the priority is none.
         * @return whether the priority is none.
         */
        boolean isNone();

        /**
         * Returns whether the priority is priority.
         * @return whether the priority is priority.
         */
        boolean isPriority();

        /**
         * Returns whether the priority is stop.
         * @return whether the priority is stop.
         */
        boolean isStop();

        /**
         * Returns whether the priority is yield.
         * @return whether the priority is yield.
         */
        boolean isYield();
    }

}
