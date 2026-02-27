package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.util.function.Supplier;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.draw.RenderableTextSource;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.draw.road.TrafficLightAnimation.Text;
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw a traffic light on the road at th place where the cars are expected to stop.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrafficLightAnimation extends AbstractLineAnimation<TrafficLightData, Text>
{

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param trafficLight the traffic light
     * @param contextualized context provider
     */
    public TrafficLightAnimation(final TrafficLightData trafficLight, final Contextualized contextualized)
    {
        super(trafficLight, contextualized, new Length(0.5, LengthUnit.SI));
    }

    @Override
    protected Text createText(final TrafficLightData source, final Contextualized contextualized, final String prefix)
    {
        float halfLength = (float) (source.getLine().getLength() / 2.0);
        return new Text(source, source::getId, 0.0f, halfLength + 0.2f, TextAlignment.CENTER, Color.BLACK, contextualized);
    }

    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(getSource().getColor());
        super.paint(graphics, observer);
    }

    @Override
    public final String toString()
    {
        return "TrafficLightAnimation [getSource()=" + this.getSource() + "]";
    }

    /**
     * Text animation for the TrafficLight.
     */
    public static class Text extends RenderableTextSource<TrafficLightData, Text>
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
        public Text(final TrafficLightData source, final Supplier<String> text, final float dx, final float dy,
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
     * TrafficLightData provides the information required to draw a traffic light.
     */
    public interface TrafficLightData extends LaneBasedObjectData
    {
        /**
         * Returns the traffic light color.
         * @return traffic light color
         */
        Color getColor();
    }

}
