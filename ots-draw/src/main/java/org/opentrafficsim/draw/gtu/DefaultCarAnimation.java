package org.opentrafficsim.draw.gtu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsRenderable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw a car.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DefaultCarAnimation extends OtsRenderable<GtuData>
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** the Text object to destroy when the GTU animation is destroyed. */
    private Text text;

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

    /** Marker if zoomed out. */
    private RectangularShape marker;

    /**
     * Construct the DefaultCarAnimation for a LaneBasedIndividualCar.
     * @param gtu GtuData; the Car to draw
     * @param contextualized Contextualized; context provider
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public DefaultCarAnimation(final GtuData gtu, final Contextualized contextualized) throws NamingException, RemoteException
    {
        super(gtu, contextualized);
        this.hashCode = gtu.hashCode();
        this.text = new Text(gtu, gtu::getId, 0.0f, 0.0f, TextAlignment.CENTER, Color.BLACK, contextualized,
                new TextAnimation.ContrastToBackground()
                {
                    /** {@inheritDoc} */
                    @Override
                    public Color getBackgroundColor()
                    {
                        return gtu.getColor();
                    }
                }).setDynamic(true);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        setRendering(graphics);
        final GtuData gtu = getSource();
        if (this.rectangle == null)
        {
            // set shapes, this is done in paint() and not the constructor, as the super constructor binds to context causing
            // paint commands before the shapes are calculated in the constructor
            final double length = gtu.getLength().si;
            final double lFront = gtu.getFront().si;
            final double lRear = gtu.getRear().si;
            final double width = gtu.getWidth().si;
            final double w2 = width / 2;
            final double w4 = width / 4;
            this.rectangle = new Rectangle2D.Double(lRear, -w2, length, width);
            this.frontIndicator = new Ellipse2D.Double(lFront - w2 - w4, -w4, w2, w2);
            this.leftIndicator = new Rectangle2D.Double(lFront - w4, -w2, w4, w4);
            this.rightIndicator = new Rectangle2D.Double(lFront - w4, w2 - w4, w4, w4);
            this.leftBrake = new Rectangle2D.Double(lRear, w2 - w4, w4, w4);
            this.rightBrake = new Rectangle2D.Double(lRear, -w2, w4, w4);
            this.marker = gtu.getMarker();
        }

        double scale = graphics.getTransform().getDeterminant();
        // Math.sqrt(Math.pow(graphics.getTransform()..getScaleX(), 2)
        // Math.pow(graphics.getTransform().getScaleY(), 2));
        if (scale > 1)
        {
            Color color = gtu.getColor();
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
            if (gtu.leftIndicatorOn())
            {
                graphics.fill(this.leftIndicator);
                if (color.equals(Color.YELLOW))
                {
                    graphics.setColor(Color.BLACK);
                    graphics.draw(this.leftIndicator);
                }
            }
            if (gtu.rightIndicatorOn())
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
            graphics.setColor(gtu.getColor());
            double w = 7.0 / Math.sqrt(scale);
            double x = -w / 2.0;
            this.marker.setFrame(x, x, w, w);
            graphics.fill(this.marker);
        }
        resetRendering(graphics);
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
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public class Text extends TextAnimation<GtuData, Text>
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** is the animation destroyed? */
        private boolean isTextDestroyed = false;

        /**
         * @param source GtuData; the object for which the text is displayed
         * @param text Supplier&lt;String&gt;; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textAlignment TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param contextualized Contextualized; context provider
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final GtuData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textAlignment, final Color color, final Contextualized contextualized)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textAlignment, color, 1.0f, 12.0f, 50f, contextualized, TextAnimation.RENDERWHEN1);
        }

        /**
         * @param source GtuData; the object for which the text is displayed
         * @param text Supplier&lt;String&gt;; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textAlignment TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param contextualized Contextualized; context provider
         * @param background TextAnimation.ContrastToBackground; connection to retrieve the current background color
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        @SuppressWarnings("parameternumber")
        public Text(final GtuData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textAlignment, final Color color, final Contextualized contextualized,
                final TextAnimation.ContrastToBackground background) throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textAlignment, color, 1.0f, 12.0f, 50f, contextualized, background, RENDERWHEN1);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Text [isTextDestroyed=" + this.isTextDestroyed + "]";
        }

    }

    /**
     * GtuData provides the information required to draw a link.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface GtuData extends OtsLocatable, Identifiable
    {
        /**
         * Returns the GTU color.
         * @return Color; GTU color.
         */
        Color getColor();

        /**
         * Returns the length.
         * @return Length; length.
         */
        Length getLength();

        /**
         * Returns the width.
         * @return Length; width.
         */
        Length getWidth();

        /**
         * Returns the distance towards the front.
         * @return Length; distance towards the front.
         */
        Length getFront();

        /**
         * Returns the distance towards the rear.
         * @return Length; distance towards the rear.
         */
        Length getRear();

        /**
         * Returns whether the left indicator is on.
         * @return boolean; whether the left indicator is on.
         */
        boolean leftIndicatorOn();

        /**
         * Returns whether the right indicator is on.
         * @return boolean; whether the right indicator is on.
         */
        boolean rightIndicatorOn();

        /**
         * Returns the shape of a marker to show when zoomed out.
         * @return RectangularShape; shape of a marker to show when zoomed out.
         */
        default RectangularShape getMarker()
        {
            return new Ellipse2D.Double(0, 0, 0, 0);
        }

        /**
         * Returns whether the braking lights are on.
         * @return boolean; whether the braking lights are on.
         */
        boolean isBrakingLightsOn();

        /** {@inheritDoc} */
        @Override
        OrientedPoint2d getLocation();

        /** {@inheritDoc} */
        @Override
        default double getZ()
        {
            return DrawLevel.GTU.getZ();
        }
    }

}
