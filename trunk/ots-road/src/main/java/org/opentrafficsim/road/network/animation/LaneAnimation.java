package org.opentrafficsim.road.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.animation.ClonableRenderable2DInterface;
import org.opentrafficsim.core.animation.TextAlignment;
import org.opentrafficsim.core.animation.TextAnimation;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.animation.PaintLine;
import org.opentrafficsim.core.network.animation.PaintPolygons;
import org.opentrafficsim.road.network.animation.SensorAnimation.Text;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneAnimation extends Renderable2D implements ClonableRenderable2DInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** Color of the lane. */
    private final Color color;

    /** Whether to draw the center line or not. */
    private final boolean drawCenterLine;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Animate a Lane.
     * @param lane Lane; the lane
     * @param simulator OTSSimulatorInterface; the simulator
     * @param color Color of the lane.
     * @param drawCenterLine boolean; whether to draw the center line or not
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public LaneAnimation(final Lane lane, final OTSSimulatorInterface simulator, final Color color,
            final boolean drawCenterLine) throws NamingException, RemoteException
    {
        super(lane, simulator);
        this.color = color;
        this.drawCenterLine = drawCenterLine;
        this.text = new Text(lane, lane.getParentLink().getId() + "." + lane.getId(), 0.0f, 0.0f, TextAlignment.CENTER,
                Color.BLACK, simulator);
    }

    /**
     * @return text.
     */
    public final Text getText()
    {
        return this.text;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        Lane lane = (Lane) getSource();
        if (this.color != null)
        {
            PaintPolygons.paintMultiPolygon(graphics, this.color, lane.getLocation(), lane.getContour(), true);
        }

        if (this.drawCenterLine)
        {
            PaintLine.paintLine(graphics, Color.RED, 0.25, lane.getLocation(), lane.getCenterLine());
            Shape startCircle = new Ellipse2D.Double(lane.getCenterLine().getFirst().x - lane.getLocation().x - 0.25,
                    -lane.getCenterLine().getFirst().y + lane.getLocation().y - 0.25, 0.5, 0.5);
            Shape endCircle = new Ellipse2D.Double(lane.getCenterLine().getLast().x - lane.getLocation().x - 0.25,
                    -lane.getCenterLine().getLast().y + lane.getLocation().y - 0.25, 0.5, 0.5);
            graphics.setColor(Color.BLUE);
            graphics.fill(startCircle);
            graphics.setColor(Color.RED);
            graphics.fill(endCircle);
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
    public ClonableRenderable2DInterface clone(final Locatable newSource, final OTSSimulatorInterface newSimulator)
            throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new LaneAnimation((Lane) newSource, newSimulator, this.color, this.drawCenterLine);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneAnimation [lane = " + getSource().toString() + ", color=" + this.color + ", drawCenterLine="
                + this.drawCenterLine + "]";
    }

    /**
     * Text animation for the Node. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
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
                final TextAlignment textPlacement, final Color color, final OTSSimulatorInterface simulator)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, simulator);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public DirectedPoint getLocation() throws RemoteException
        {
            // draw always on top.
            DirectedPoint p = ((Lane) getSource()).getCenterLine().getLocationFractionExtended(0.5);
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
        public TextAnimation clone(final Locatable newSource, final OTSSimulatorInterface newSimulator)
                throws RemoteException, NamingException
        {
            return new Text(newSource, getText(), getDx(), getDy(), getTextAlignment(), getColor(), newSimulator);
        }

    }

}
