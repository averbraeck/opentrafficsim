package org.opentrafficsim.draw.network;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djutils.base.Identifiable;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.PaintLine;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;
import org.opentrafficsim.draw.road.OtsRenderable;

import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws LinkData.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LinkAnimation extends OtsRenderable<LinkData>
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** */
    private float width;

    /** the Text object to destroy when the animation is destroyed. */
    private Text text;

    /** Drawable path. */
    private Path2D.Double path;

    /** Drawable path for start point. */
    private Path2D.Double startPoint;

    /** Drawable path for end point. */
    private Path2D.Double endPoint;

    /** Color. */
    private final Color color;

    /** Design line, also cached in parent, this is to see if it has changed. */
    private PolyLine2d designLine;

    /** Whether animation is dynamic. */
    private boolean dynamic = false;

    /**
     * @param link LinkData; link data.
     * @param contextualized Contextualized; context provider.
     * @param width float; width
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
     */
    public LinkAnimation(final LinkData link, final Contextualized contextualized, final float width)
            throws NamingException, RemoteException
    {
        super(link, contextualized);
        this.width = width;
        this.text = new Text(link, link::getId, 0.0f, 1.5f, TextAlignment.CENTER, Color.BLACK, contextualized,
                TextAnimation.RENDERWHEN10);
        PolyLine2d designLine = getSource().getDesignLine();
        this.path = PaintLine.getPath(getSource().getLocation(), designLine);
        this.startPoint = getEndPoint(designLine.getFirst(), designLine.get(1));
        this.endPoint = getEndPoint(designLine.getLast(), designLine.get(designLine.size() - 2));
        this.color = getSource().isConnector() ? Color.PINK.darker() : Color.BLUE;
    }

    /**
     * Sets the animation as dynamic, obtaining geometry at each draw.
     * @param dynamic boolean; whether it is dynamic {@code false} by default.
     * @return LinkAnimation; for method chaining.
     */
    public LinkAnimation setDynamic(final boolean dynamic)
    {
        this.dynamic = dynamic;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (this.dynamic)
        {
            PolyLine2d designLine = getSource().getDesignLine();
            if (!designLine.equals(this.designLine))
            {
                this.path = PaintLine.getPath(getSource().getLocation(), designLine);
                this.startPoint = getEndPoint(designLine.getFirst(), designLine.get(1));
                this.endPoint = getEndPoint(designLine.getLast(), designLine.get(designLine.size() - 2));
            }
        }
        PaintLine.paintLine(graphics, this.color, this.width, this.path);
        PaintLine.paintLine(graphics, this.color, this.width / 30, this.startPoint);
        PaintLine.paintLine(graphics, this.color, this.width / 30, this.endPoint);
    }

    /**
     * @param endPoint Point2d; the end of the design line where a end point must be highlighted
     * @param nextPoint Point2d; the point nearest <code>endPoint</code> (needed to figure out the direction of the design line)
     * @return Path2D.Double; path to draw an end point.
     */
    private Path2D.Double getEndPoint(final Point2d endPoint, final Point2d nextPoint)
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

    /** {@inheritDoc} */
    @Override
    public void destroy(final Contextualized contextProvider)
    {
        super.destroy(contextProvider);
        this.text.destroy(contextProvider);
    }

    /** {@inheritDoc} */
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
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class Text extends TextAnimation<LinkData>
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source LinkData; the object for which the text is displayed
         * @param text Supplier&lt;String&gr;; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param contextualized Contextualized; context provider.
         * @param scaleDependentRendering ScaleDependentRendering; enables rendering in a scale dependent fashion
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final LinkData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized,
                final ScaleDependentRendering scaleDependentRendering) throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, 2.0f, 12.0f, 50f, contextualized, null, scaleDependentRendering);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public OrientedPoint2d getLocation()
        {
            // draw always on top, and not upside down.
            Ray2d p = getSource().getDesignLine().getLocationFractionExtended(0.5);
            double a = Angle.normalizePi(p.getPhi());
            if (a > Math.PI / 2.0 || a < -0.99 * Math.PI / 2.0)
            {
                a += Math.PI;
            }
            return new OrientedPoint2d(p.x, p.y, a);
        }

        /** {@inheritDoc} */
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
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface LinkData extends OtsLocatable, Identifiable
    {
        /**
         * Returns whether this is a connector.
         * @return boolean; whether this is a connector.
         */
        boolean isConnector();

        /**
         * Returns the design line.
         * @return PolyLine2d; design line.
         */
        PolyLine2d getDesignLine();

        /** {@inheritDoc} */
        @Override
        default double getZ()
        {
            return DrawLevel.LINE.getZ();
        }
    }

}
