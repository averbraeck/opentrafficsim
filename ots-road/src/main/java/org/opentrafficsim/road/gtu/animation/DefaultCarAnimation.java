package org.opentrafficsim.road.gtu.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import org.opentrafficsim.core.animation.TextAnimation;
import org.opentrafficsim.core.animation.TextAlignment;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Draw a car.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DefaultCarAnimation extends Renderable2D implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;
    
    /** The GTUColorer that determines the fill color for the car. */
    private GTUColorer gtuColorer;
    
    /**
     * Construct the DefaultCarAnimation for a LaneBasedIndividualCar.
     * @param gtu the Car to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public DefaultCarAnimation(final LaneBasedIndividualGTU gtu, final OTSSimulatorInterface simulator)
        throws NamingException, RemoteException
    {
        this(gtu, simulator, null);
    }

    /**
     * Construct the DefaultCarAnimation for a LaneBasedIndividualCar.
     * @param gtu the Car to draw
     * @param simulator the simulator to schedule on
     * @param gtuColorer GTUColorer; the GTUColorer that determines what fill color to use
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public DefaultCarAnimation(final LaneBasedIndividualGTU gtu, final OTSSimulatorInterface simulator,
        final GTUColorer gtuColorer) throws NamingException, RemoteException
    {
        super(gtu, simulator);
        if (null == gtuColorer)
        {
            this.gtuColorer = new IDGTUColorer();
        }
        else
        {
            this.gtuColorer = gtuColorer;
        }
        
        new Text(gtu, gtu.getId(), 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, simulator);
    }

    /**
     * Replace the GTUColorer.
     * @param newGTUColorer GTUColorer; the GTUColorer to use from now on
     */
    public final void setGTUColorer(final GTUColorer newGTUColorer)
    {
        this.gtuColorer = newGTUColorer;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        final LaneBasedIndividualGTU car = (LaneBasedIndividualGTU) getSource();

        if (car.isDestroyed())
        {
            try
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            destroy();
                        }
                        catch (NamingException exception)
                        {
                            System.err.println("Failed to destroy GTU animation of GTU " + getSource().toString());
                        }
                    }
                });
            }
            catch (Exception e)
            {
                // ignore
            }
            return;
        }

        final double length = car.getLength().getSI();
        final double l2 = length / 2;
        final double width = car.getWidth().getSI();
        final double w2 = width / 2;
        final double w4 = width / 4;
        graphics.setColor(this.gtuColorer.getColor(car));
        BasicStroke saveStroke = (BasicStroke) graphics.getStroke();
        graphics.setStroke(new BasicStroke(0));
        Rectangle2D rectangle = new Rectangle2D.Double(-l2, -w2, length, width);
        graphics.draw(rectangle);
        graphics.fill(rectangle);
        // Draw a white disk at the front to indicate which side faces forward
        graphics.setColor(Color.WHITE);
        Ellipse2D.Double frontIndicator = new Ellipse2D.Double(l2 - w2 - w4, -w4, w2, w2);
        graphics.draw(frontIndicator);
        graphics.fill(frontIndicator);

        // turn indicator lights
        graphics.setColor(Color.YELLOW);
        if (car.getTurnIndicatorStatus() != null && car.getTurnIndicatorStatus().isLeftOrBoth())
        {
            Rectangle2D.Double leftIndicator = new Rectangle2D.Double(l2 - w4, -w2, w4, w4);
            graphics.fill(leftIndicator);
        }
        if (car.getTurnIndicatorStatus() != null && car.getTurnIndicatorStatus().isRightOrBoth())
        {
            Rectangle2D.Double rightIndicator = new Rectangle2D.Double(l2 - w4, w2 - w4, w4, w4);
            graphics.fill(rightIndicator);            
        }
        
        // braking lights
        graphics.setColor(Color.RED);
        if (car.getAcceleration().si < -0.5) // TODO this could be a property of a GTU 
        {
            Rectangle2D.Double leftBrake = new Rectangle2D.Double(-l2, w2 - w4, w4, w4);
            Rectangle2D.Double rightBrake = new Rectangle2D.Double(-l2, -w2, w4, w4);
            graphics.setColor(Color.RED);
            graphics.fill(leftBrake);
            graphics.fill(rightBrake);
        }
        graphics.setStroke(saveStroke);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return this.getSource().toString();
    }

    /**
     * Text animation for the Car. Separate class to be able to turn it on and off...
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
            super(source, text, dx, dy, textPlacement, color, 1.0f, simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
        {
            if (((LaneBasedIndividualGTU) getSource()).isDestroyed())
            {
                try
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            destroy();
                        }
                    });
                }
                catch (Exception e)
                {
                    // ignore
                }
                return;
            }
            
            super.paint(graphics, observer);
        }
        
        /**
         * Try to destroy the animation.
         */
        protected final void destroy()
        {
            try
            {
                this.animationImpl.destroy();
            }
            catch (NamingException exception)
            {
                System.err.println("Tried to destroy Text for GTU animation of GTU " + getSource().toString());
            }
        }
        
        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public DirectedPoint getLocation() throws RemoteException
        {
            // draw always on top, and not upside down.
            DirectedPoint p = ((LaneBasedIndividualGTU) this.source).getLocation();
            double a = Angle.normalizePi(p.getRotZ());
            if (a > Math.PI / 2.0 || a < -0.99 * Math.PI / 2.0)
            {
                a += Math.PI;
            }
            return new DirectedPoint(p.x, p.y, Double.MAX_VALUE, 0.0, 0.0, a);
        }
    }

}
