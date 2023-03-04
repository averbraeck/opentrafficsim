package org.opentrafficsim.draw.network;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djutils.draw.line.PolyLine3d;
import org.djutils.draw.point.Point3d;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.draw.core.PaintLine;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draws a Link.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class LinkAnimation extends Renderable2D<Link> implements Renderable2DInterface<Link>, Serializable
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** */
    private float width;

    /** the Text object to destroy when the animation is destroyed. */
    private Text text;

    /**
     * @param link Link; Link
     * @param simulator OtsSimulatorInterface; simulator
     * @param width float; width
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
     */
    public LinkAnimation(final Link link, final OtsSimulatorInterface simulator, final float width)
            throws NamingException, RemoteException
    {
        super(link, simulator);
        this.width = width;
        this.text = new Text(link, link.getId(), 0.0f, 1.5f, TextAlignment.CENTER, Color.BLACK, simulator,
                TextAnimation.RENDERWHEN10);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        try
        {
            Color color = getSource().isConnector() ? Color.PINK.darker() : Color.BLUE;
            OtsLine3d designLine = getSource().getDesignLine();
            PaintLine.paintLine(graphics, color, this.width, getSource().getLocation(), designLine);
            // Accentuate the end points
            try
            {
                drawEndPoint(designLine.getFirst(), designLine.get(1), graphics);
                drawEndPoint(designLine.getLast(), designLine.get(designLine.size() - 2), graphics);
            }
            catch (OtsGeometryException exception)
            {
                // Cannot happen
                CategoryLogger.always().error(exception);
            }
        }
        catch (RemoteException e)
        {
            CategoryLogger.always().warn(e);
        }
    }

    /**
     * Draw end point on design line.
     * @param endPoint OtsPoint3d; the end of the design line where a end point must be highlighted
     * @param nextPoint OtsPoint3d; the point nearest <code>endPoint</code> (needed to figure out the direction of the design
     *            line)
     * @param graphics Graphics2D; graphics content
     */
    private void drawEndPoint(final OtsPoint3d endPoint, final OtsPoint3d nextPoint, final Graphics2D graphics)
    {
        // End point marker is 2 times the width of the design line
        double dx = nextPoint.x - endPoint.x;
        double dy = nextPoint.y - endPoint.y;
        double length = endPoint.distanceSI(nextPoint);
        // scale dx, dy so that size is this.width
        dx *= this.width / length;
        dy *= this.width / length;
        try
        {
            PolyLine3d line = new PolyLine3d(new Point3d(endPoint.x - dy, endPoint.y + dx, endPoint.z),
                    new Point3d(endPoint.x + dy, endPoint.y - dx, endPoint.z));
            PaintLine.paintLine(graphics, getSource().isConnector() ? Color.PINK.darker() : Color.BLUE, this.width / 30,
                    getSource().getLocation(), line);
        }
        catch (RemoteException exception)
        {
            CategoryLogger.always().error(exception);
        }
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
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class Text extends TextAnimation
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source Locatable; the object for which the text is displayed
         * @param text String; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param simulator OtsSimulatorInterface; the simulator
         * @param scaleDependentRendering ScaleDependentRendering; enables rendering in a scale dependent fashion
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final OtsSimulatorInterface simulator,
                final ScaleDependentRendering scaleDependentRendering) throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, 2.0f, 12.0f, 50f, simulator, null, scaleDependentRendering);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public DirectedPoint getLocation()
        {
            // draw always on top, and not upside down.
            DirectedPoint p = ((Link) getSource()).getDesignLine().getLocationFractionExtended(0.5);
            double a = Angle.normalizePi(p.getRotZ());
            if (a > Math.PI / 2.0 || a < -0.99 * Math.PI / 2.0)
            {
                a += Math.PI;
            }
            return new DirectedPoint(p.x, p.y, Double.MAX_VALUE, 0.0, 0.0, a);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LinkAnimation.Text []";
        }
    }

}
