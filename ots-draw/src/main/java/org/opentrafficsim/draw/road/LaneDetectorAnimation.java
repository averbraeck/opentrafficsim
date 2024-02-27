package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.draw.road.LaneDetectorAnimation.LaneDetectorData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw LaneDetectorData.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneDetectorAnimation extends AbstractLineAnimation<LaneDetectorData>
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** The color of the detector. */
    private final Color color;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Construct a DetectorAnimation.
     * @param laneDetector LaneDetectorData; the lane detector to draw
     * @param contextualized Contextualized; context provider
     * @param color Color; the display color of the detector
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public LaneDetectorAnimation(final LaneDetectorData laneDetector, final Contextualized contextualized, final Color color)
            throws NamingException, RemoteException
    {
        super(laneDetector, contextualized, .9, new Length(0.5, LengthUnit.SI));
        this.color = color;
        this.text = new Text(laneDetector, laneDetector::getId, 0.0f, (float) getHalfLength() + 0.2f, TextAlignment.CENTER,
                Color.BLACK, contextualized);
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
        graphics.setColor(this.color);
        super.paint(graphics, observer);
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
        return "DetectorAnimation [getSource()=" + this.getSource() + "]";
    }

    /**
     * Text animation for the Detector. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public class Text extends TextAnimation<LaneDetectorData, Text> implements DetectorData.Text
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source LaneDetectorData; the object for which the text is displayed
         * @param text Supplier&lt;String&gt;; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param contextualized Contextualized; context provider
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final LaneDetectorData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, contextualized, TextAnimation.RENDERALWAYS);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Text []";
        }
    }

    /**
     * LaneDetectorData provides the information required to draw a lane detector.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface LaneDetectorData extends LaneBasedObjectData, DetectorData
    {
    }

}
