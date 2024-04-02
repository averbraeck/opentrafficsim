package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
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
 * @param <L> detector data type
 * @param <T> text type
 */
public class LaneDetectorAnimation<L extends LaneDetectorData, T extends TextAnimation<L, T>> extends AbstractLineAnimation<L>
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** The color of the detector. */
    private final Color color;

    /** the Text object to destroy when the animation is destroyed. */
    private T text;

    /**
     * Constructor. This constructor creates no text type. The method {@code ofGenericType()} uses this.
     * @param laneDetector L; the lane detector to draw.
     * @param contextualized Contextualized; context provider.
     * @param color Color; the display color of the detector.
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    private LaneDetectorAnimation(final L laneDetector, final Contextualized contextualized, final Color color)
            throws NamingException, RemoteException
    {
        super(laneDetector, contextualized, .9, new Length(0.5, LengthUnit.SI));
        this.color = color;
    }

    /**
     * This method produces a detector animation that toggles generally for all LaneDetectorData and its respective text id.
     * @param laneDetector L; detector data.
     * @param contextualized Contextualized; context provider.
     * @param color Color; color.
     * @return animation for generic lane detector type.
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public static LaneDetectorAnimation<LaneDetectorData, Text> ofGenericType(final LaneDetectorData laneDetector,
            final Contextualized contextualized, final Color color) throws RemoteException, NamingException
    {
        LaneDetectorAnimation<LaneDetectorData, Text> animation =
                new LaneDetectorAnimation<>(laneDetector, contextualized, color);
        animation.text = new Text(laneDetector, laneDetector::getId, 0.0f, (float) animation.getHalfLength() + 0.2f,
                TextAlignment.CENTER, Color.BLACK, contextualized);
        return animation;
    }

    /**
     * This constructor uses a provider for the text animation. This should provide an animation that extends
     * {@code TextAnimation} and implements the right tagging interface to toggle the correct label belonging to L.
     * Alternatively, the toggle can be specified to the class that extends {@code TextAnimation} directly.
     * @param laneDetector L; the lane detector to draw.
     * @param contextualized Contextualized; context provider.
     * @param color Color; the display color of the detector.
     * @param textSupplier Function&lt;Float, Text&gt;; text supplier.
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public LaneDetectorAnimation(final L laneDetector, final Contextualized contextualized, final Color color,
            final Function<LaneDetectorAnimation<L, T>, T> textSupplier) throws NamingException, RemoteException
    {
        super(laneDetector, contextualized, .9, new Length(0.5, LengthUnit.SI));
        this.color = color;
        this.text = textSupplier.apply(this);
    }

    /**
     * @return text.
     */
    public final T getText()
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
    public static class Text extends TextAnimation<LaneDetectorData, Text> implements DetectorData.Text
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
            super(source, text, dx, dy, textPlacement, color, contextualized, TextAnimation.RENDERWHEN10);
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

    /**
     * SinkData provides the information required to draw a sink.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface LoopDetectorData extends LaneDetectorData
    {
        /**
         * Tagging implementation for loop detector ids.
         * <p>
         * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
         * </p>
         * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
         */
        public class LoopDetectorText extends TextAnimation<LoopDetectorData, LoopDetectorText>
        {
            /** */
            private static final long serialVersionUID = 20240301L;

            /**
             * Constructor.
             * @param laneDetector LoopDetectorData; loop detector data.
             * @param dy float; vertical spacing.
             * @param contextualized Contextualized; context provider.
             * @throws NamingException when animation context cannot be created or retrieved
             * @throws RemoteException when remote context cannot be found
             */
            public LoopDetectorText(final LoopDetectorData laneDetector, final float dy, final Contextualized contextualized)
                    throws RemoteException, NamingException
            {
                super(laneDetector, laneDetector::getId, 0.0f, dy, TextAlignment.CENTER, Color.BLACK, contextualized,
                        TextAnimation.RENDERWHEN10);
            }
        }
    }

    /**
     * SinkData provides the information required to draw a sink.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface SinkData extends LaneDetectorData
    {
        /**
         * Tagging implementation for sink ids.
         * <p>
         * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
         * </p>
         * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
         */
        public class SinkText extends TextAnimation<SinkData, SinkText>
        {
            /** */
            private static final long serialVersionUID = 20240301L;

            /**
             * Constructor.
             * @param sink SinkData; loop detector data.
             * @param dy float; vertical spacing.
             * @param contextualized Contextualized; context provider.
             * @throws NamingException when animation context cannot be created or retrieved
             * @throws RemoteException when remote context cannot be found
             */
            public SinkText(final SinkData sink, final float dy, final Contextualized contextualized)
                    throws RemoteException, NamingException
            {
                super(sink, sink::getId, 0.0f, dy, TextAlignment.CENTER, Color.BLACK, contextualized,
                        TextAnimation.RENDERWHEN10);
            }
        }
    }

}
