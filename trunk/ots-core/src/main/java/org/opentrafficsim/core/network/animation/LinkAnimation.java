package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.animation.ClonableRenderable2DInterface;
import org.opentrafficsim.core.animation.TextAlignment;
import org.opentrafficsim.core.animation.TextAnimation;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.Link;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Draws a Link.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LinkAnimation extends Renderable2D<Link> implements ClonableRenderable2DInterface<Link>, Serializable
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** */
    private float width;

    /** the Text object to destroy when the animation is destroyed. */
    private Text text;

    /**
     * @param link Link
     * @param simulator simulator
     * @param width width
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
     */
    public LinkAnimation(final Link link, final SimulatorInterface.TimeDoubleUnit simulator, final float width)
            throws NamingException, RemoteException
    {
        super(link, simulator);
        this.width = width;
        this.text = new Text(link, link.getId(), 0.0f, 1.5f, TextAlignment.CENTER, Color.BLACK, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        Color color = getSource().getLinkType().isConnector() ? Color.BLUE : Color.RED;
        OTSLine3D designLine = getSource().getDesignLine();
        PaintLine.paintLine(graphics, color, this.width, getSource().getLocation(), designLine);
        // Accentuate the end points
        try
        {
            drawEndPoint(designLine.getFirst(), designLine.get(1), graphics);
            drawEndPoint(designLine.getLast(), designLine.get(designLine.size() - 2), graphics);
        }
        catch (OTSGeometryException exception)
        {
            // Cannot happen
            SimLogger.always().error(exception);
        }
    }

    /**
     * Draw end point on design line.
     * @param endPoint OTSPoint3D; the end of the design line where a end point must be highlighted
     * @param nextPoint OTSPoint3D; the point nearest <code>endPoint</code> (needed to figure out the direction of the design
     *            line)
     * @param graphics Graphics2D; graphics content
     */
    private void drawEndPoint(final OTSPoint3D endPoint, final OTSPoint3D nextPoint, final Graphics2D graphics)
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
            OTSLine3D line = new OTSLine3D(new OTSPoint3D(endPoint.x - dy, endPoint.y + dx, endPoint.z),
                    new OTSPoint3D(endPoint.x + dy, endPoint.y - dx, endPoint.z));
            PaintLine.paintLine(graphics, getSource().getLinkType().isConnector() ? Color.BLUE : Color.RED, this.width / 30,
                    getSource().getLocation(), line);
        }
        catch (OTSGeometryException exception)
        {
            SimLogger.always().error(exception);
        }
        catch (RemoteException exception)
        {
            SimLogger.always().error(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void destroy() throws NamingException
    {
        super.destroy();
        this.text.destroy();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<Link> clone(final Link newSource, final SimulatorInterface.TimeDoubleUnit newSimulator)
            throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new LinkAnimation(newSource, newSimulator, this.width);
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
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version Dec 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public class Text extends TextAnimation
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source the object for which the text is displayed
         * @param text the text to display
         * @param dx the horizontal movement of the text, in meters
         * @param dy the vertical movement of the text, in meters
         * @param textPlacement where to place the text
         * @param color the color of the text
         * @param simulator the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final SimulatorInterface.TimeDoubleUnit simulator)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, simulator);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public DirectedPoint getLocation() throws RemoteException
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
        @SuppressWarnings("checkstyle:designforextension")
        public TextAnimation clone(final Locatable newSource, final SimulatorInterface.TimeDoubleUnit newSimulator)
                throws RemoteException, NamingException
        {
            return new Text(newSource, getText(), getDx(), getDy(), getTextAlignment(), getColor(), newSimulator);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LinkAnimation.Text []";
        }
    }

}
