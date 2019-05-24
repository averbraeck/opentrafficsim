package org.opentrafficsim.draw.gtu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.IDGTUColorer;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.draw.core.ClonableRenderable2DInterface;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d2.Angle;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Draw a car.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DefaultCarAnimation extends Renderable2D<LaneBasedGTU>
        implements ClonableRenderable2DInterface<LaneBasedGTU>, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The GTUColorer that determines the fill color for the car. */
    private GTUColorer gtuColorer = new DefaultSwitchableGTUColorer();

    /** the Text object to destroy when the GTU animation is destroyed. */
    private Text text;

    /** is the animation destroyed? */
    private boolean isDestroyed = false;

    /** Hashcode. */
    private final int hashCode;

    /** GTU outline. */
    private Rectangle2D.Double rectangle;

    /** Front indicator (white circle). */
    private Ellipse2D.Double frontIndicator;

    /** Left indicator. */
    private Rectangle2D.Double leftIndicator;

    /** Right indicator. */
    private Rectangle2D.Double rightIndicator;

    /** Left brake light. */
    private Rectangle2D.Double leftBrake;

    /** Right brake light. */
    private Rectangle2D.Double rightBrake;

    /** Dot if zoomed out. */
    private RectangularShape dot;

    /**
     * Construct the DefaultCarAnimation for a LaneBasedIndividualCar.
     * @param gtu LaneBasedGTU; the Car to draw
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public DefaultCarAnimation(final LaneBasedGTU gtu, final SimulatorInterface.TimeDoubleUnit simulator)
            throws NamingException, RemoteException
    {
        this(gtu, simulator, null);
    }

    /**
     * Construct the DefaultCarAnimation for a LaneBasedIndividualCar.
     * @param gtu LaneBasedGTU; the Car to draw
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @param gtuColorer GTUColorer; the GTUColorer that determines what fill color to use
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public DefaultCarAnimation(final LaneBasedGTU gtu, final SimulatorInterface.TimeDoubleUnit simulator,
            final GTUColorer gtuColorer) throws NamingException, RemoteException
    {
        super(gtu, simulator);
        this.hashCode = gtu.hashCode();
        if (null == gtuColorer)
        {
            this.gtuColorer = new IDGTUColorer();
        }
        else
        {
            this.gtuColorer = gtuColorer;
        }
        this.text = new Text(gtu, gtu.getId(), 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, simulator,
                new TextAnimation.ContrastToBackground()
                {

                    @Override
                    public Color getBackgroundColor()
                    {
                        return gtuColorer.getColor(gtu);
                    }
                });
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
        final LaneBasedGTU gtu = getSource();
        if (gtu.isDestroyed())
        {
            if (!this.isDestroyed)
            {
                try
                {
                    destroy();
                }
                catch (Exception e)
                {
                    System.err.println("Error while destroying GTU " + gtu.getId());
                    e.printStackTrace();
                }
            }
            return;
        }

        if (this.rectangle == null)
        {
            // set shapes, this is done in paint() and not the constructor, as the super constructor binds to context causing
            // paint commands before the shapes are calculated in the constructor
            final double length = gtu.getLength().si;
            final double lFront = gtu.getFront().getDx().si;
            final double lRear = gtu.getRear().getDx().si;
            final double width = gtu.getWidth().si;
            final double w2 = width / 2;
            final double w4 = width / 4;
            this.rectangle = new Rectangle2D.Double(lRear, -w2, length, width);
            this.frontIndicator = new Ellipse2D.Double(lFront - w2 - w4, -w4, w2, w2);
            this.leftIndicator = new Rectangle2D.Double(lFront - w4, -w2, w4, w4);
            this.rightIndicator = new Rectangle2D.Double(lFront - w4, w2 - w4, w4, w4);
            this.leftBrake = new Rectangle2D.Double(lRear, w2 - w4, w4, w4);
            this.rightBrake = new Rectangle2D.Double(lRear, -w2, w4, w4);
            this.dot = gtu.getGTUType().isOfType(gtu.getPerceivableContext().getGtuType(GTUType.DEFAULTS.TRUCK))
                    ? new Rectangle2D.Double(0, 0, 0, 0) : new Ellipse2D.Double(0, 0, 0, 0);
        }

        double scale = graphics.getTransform().getDeterminant();
        // Math.sqrt(Math.pow(graphics.getTransform()..getScaleX(), 2)
        // Math.pow(graphics.getTransform().getScaleY(), 2));
        if (scale > 1)
        {
            Color color = this.gtuColorer.getColor(gtu);
            graphics.setColor(color);
            BasicStroke saveStroke = (BasicStroke) graphics.getStroke();
            graphics.setStroke(new BasicStroke(0.05f)); // 5 cm
            graphics.fill(this.rectangle);

            graphics.setColor(Color.WHITE);
            graphics.fill(this.frontIndicator);
            // Draw a white disk at the front to indicate which side faces forward
            if (color.equals(Color.WHITE))
            {
                // Put a black ring around it
                graphics.setColor(Color.BLACK);
                graphics.draw(this.frontIndicator);
            }

            // turn indicator lights
            graphics.setColor(Color.YELLOW);
            if (gtu.getTurnIndicatorStatus() != null && gtu.getTurnIndicatorStatus().isLeftOrBoth())
            {
                graphics.fill(this.leftIndicator);
                if (color.equals(Color.YELLOW))
                {
                    graphics.setColor(Color.BLACK);
                    graphics.draw(this.leftIndicator);
                }
            }
            if (gtu.getTurnIndicatorStatus() != null && gtu.getTurnIndicatorStatus().isRightOrBoth())
            {
                graphics.fill(this.rightIndicator);
                if (color.equals(Color.YELLOW))
                {
                    graphics.setColor(Color.BLACK);
                    graphics.draw(this.rightIndicator);
                }
            }

            // braking lights
            if (gtu.isBrakingLightsOn())
            {
                graphics.setColor(Color.RED);
                graphics.fill(this.leftBrake);
                graphics.fill(this.rightBrake);
                if (color.equals(Color.RED))
                {
                    graphics.setColor(Color.BLACK);
                    graphics.draw(this.leftBrake);
                    graphics.draw(this.rightBrake);
                }
            }
            graphics.setStroke(saveStroke);
        }
        else
        {
            // zoomed out, draw as marker with 7px diameter
            graphics.setColor(this.gtuColorer.getColor(gtu));
            double w = 7.0 / Math.sqrt(scale);
            double x = -w / 2.0;
            this.dot.setFrame(x, x, w, w);
            graphics.fill(this.dot);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void destroy() throws NamingException
    {
        this.isDestroyed = true;
        super.destroy();
        this.text.destroy();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<LaneBasedGTU> clone(final LaneBasedGTU newSource,
            final SimulatorInterface.TimeDoubleUnit newSimulator) throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new DefaultCarAnimation(newSource, newSimulator, this.gtuColorer);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return super.toString(); // this.getSource().toString();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return this.hashCode;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object object)
    {
        // only here to prevent a 'hashCode without equals' warning
        return super.equals(object);
    }

    /**
     * Text animation for the Car. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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

        /** is the animation destroyed? */
        private boolean isTextDestroyed = false;

        /**
         * @param source Locatable; the object for which the text is displayed
         * @param text String; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textAlignment TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textAlignment, final Color color, final SimulatorInterface.TimeDoubleUnit simulator)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textAlignment, color, 1.0f, 12.0f, 50f, simulator, TextAnimation.RENDERWHEN1);
        }

        /**
         * @param source Locatable; the object for which the text is displayed
         * @param text String; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textAlignment TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator
         * @param background TextAnimation.ContrastToBackground; connection to retrieve the current background color
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        @SuppressWarnings("parameternumber")
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textAlignment, final Color color, final SimulatorInterface.TimeDoubleUnit simulator,
                final TextAnimation.ContrastToBackground background) throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textAlignment, color, 1.0f, 12.0f, 50f, simulator, background, RENDERWHEN1);
        }

        /** {@inheritDoc} */
        @Override
        public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
        {
            final LaneBasedIndividualGTU car = (LaneBasedIndividualGTU) getSource();

            if (car.isDestroyed())
            {
                if (!this.isTextDestroyed)
                {
                    try
                    {
                        destroy();
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error while destroying text animation of GTU " + car.getId());
                    }
                    this.isTextDestroyed = true;
                }
                return;
            }

            super.paint(graphics, observer);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public DirectedPoint getLocation() throws RemoteException
        {
            // draw always on top, and not upside down.
            DirectedPoint p = ((LaneBasedIndividualGTU) getSource()).getLocation();
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
            return "Text [isTextDestroyed=" + this.isTextDestroyed + "]";
        }

    }

}
