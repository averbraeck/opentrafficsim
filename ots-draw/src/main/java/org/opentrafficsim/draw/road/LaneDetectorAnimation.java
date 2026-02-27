package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.draw.LineLocatable;
import org.opentrafficsim.draw.RenderableTextSource;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.draw.road.LaneDetectorAnimation.LaneDetectorData;
import org.opentrafficsim.draw.road.LaneDetectorAnimation.Text;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw lane detector.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <L> detector data type
 */
public class LaneDetectorAnimation<L extends LaneDetectorData> extends AbstractLineAnimation<L, Text<L>>
{

    /** The color of the detector. */
    private final Color color;

    /**
     * This constructor uses a provider for the text animation. This should provide an animation that extends
     * {@code TextAnimation} and implements the right tagging interface to toggle the correct label belonging to L.
     * Alternatively, the toggle can be specified to the class that extends {@code TextAnimation} directly.
     * @param laneDetector the lane detector to draw
     * @param contextualized context provider
     * @param color the display color of the detector
     */
    public LaneDetectorAnimation(final L laneDetector, final Contextualized contextualized, final Color color)
    {
        this(laneDetector, contextualized, color, "");
    }

    /**
     * This constructor uses a provider for the text animation. This should provide an animation that extends
     * {@code TextAnimation} and implements the right tagging interface to toggle the correct label belonging to L.
     * Alternatively, the toggle can be specified to the class that extends {@code TextAnimation} directly.
     * @param laneDetector the lane detector to draw
     * @param contextualized context provider
     * @param color the display color of the detector
     * @param prefix label prefix
     */
    public LaneDetectorAnimation(final L laneDetector, final Contextualized contextualized, final Color color,
            final String prefix)
    {
        super(laneDetector, contextualized, new Length(0.5, LengthUnit.SI), prefix);
        this.color = color;
    }

    @Override
    protected Text<L> createText(final L source, final Contextualized contextualized, final String prefix)
    {
        float halfLength = (float) (source.getLine().getLength() / 2.0);
        return new Text<L>(source, () -> prefix + source.getId(), 0.0f, halfLength + 0.2f, TextAlignment.CENTER, Color.BLACK,
                contextualized);
    }

    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(this.color);
        super.paint(graphics, observer);
    }

    @Override
    public final String toString()
    {
        return "DetectorAnimation [getSource()=" + this.getSource() + "]";
    }

    /**
     * Text animation for the detector.
     * @param <L> source type
     */
    public static class Text<L extends LaneDetectorData> extends RenderableTextSource<L, Text<L>> implements DetectorData.Text
    {
        /**
         * Constructor.
         * @param source the object for which the text is displayed
         * @param text the text to display
         * @param dx the horizontal movement of the text, in meters
         * @param dy the vertical movement of the text, in meters
         * @param textPlacement where to place the text
         * @param color the color of the text
         * @param contextualized context provider
         */
        public Text(final L source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized)
        {
            super(source, text, dx, dy, textPlacement, color, contextualized, RenderableTextSource.RENDERWHEN10);
        }

        @Override
        public final String toString()
        {
            return "Text []";
        }
    }

    /**
     * Provides the information required to draw a lane detector.
     */
    public interface LaneDetectorData extends LaneBasedObjectData, DetectorData, LineLocatable
    {
    }

    /**
     * Provides the information required to draw a loop detector.
     */
    public interface LoopDetectorData extends LaneDetectorData
    {
        /**
         * Tagging implementation for loop detector IDs.
         */
        class LoopDetectorText extends RenderableTextSource<LoopDetectorData, LoopDetectorText>
        {
            /**
             * Constructor.
             * @param laneDetector loop detector data
             * @param dy vertical spacing
             * @param contextualized context provider
             * @throws NamingException when animation context cannot be created or retrieved
             * @throws RemoteException when remote context cannot be found
             */
            public LoopDetectorText(final LoopDetectorData laneDetector, final float dy, final Contextualized contextualized)
                    throws RemoteException, NamingException
            {
                super(laneDetector, laneDetector::getId, 0.0f, dy, TextAlignment.CENTER, Color.BLACK, contextualized,
                        RenderableTextSource.RENDERWHEN10);
            }
        }
    }

    /**
     * Provides the information required to draw a sink.
     */
    public interface SinkData extends LaneDetectorData
    {
        /**
         * Tagging implementation for sink IDs.
         */
        class SinkText extends RenderableTextSource<SinkData, SinkText>
        {
            /**
             * Constructor.
             * @param sink loop detector data
             * @param dy vertical spacing
             * @param contextualized context provider
             */
            public SinkText(final SinkData sink, final float dy, final Contextualized contextualized)
            {
                super(sink, sink::getId, 0.0f, dy, TextAlignment.CENTER, Color.BLACK, contextualized,
                        RenderableTextSource.RENDERWHEN10);
            }
        }
    }

}
