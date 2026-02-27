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
import org.opentrafficsim.draw.road.BusStopAnimation.BusStopData;
import org.opentrafficsim.draw.road.BusStopAnimation.Text;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw bus stop.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BusStopAnimation extends AbstractLineAnimation<BusStopData, Text>
{

    /**
     * Constructor.
     * @param laneDetector the lane detector to draw
     * @param contextualized context provider
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public BusStopAnimation(final BusStopData laneDetector, final Contextualized contextualized)
            throws NamingException, RemoteException
    {
        super(laneDetector, contextualized, new Length(0.5, LengthUnit.SI));
    }

    @Override
    protected Text createText(final BusStopData source, final Contextualized contextualized, final String prefix)
    {
        float halfLength = (float) (source.getLine().getLength() / 2.0);
        return new Text(source, source::getId, 0.0f, halfLength + 0.2f, TextAlignment.CENTER, Color.BLACK, contextualized);
    }

    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.WHITE);
        super.paint(graphics, observer);
    }

    @Override
    public String toString()
    {
        return "BusStopAnimation [source=" + this.getSource() + "]";
    }

    /**
     * Text animation for the bus stop.
     */
    public static class Text extends RenderableTextSource<BusStopData, Text>
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
        public Text(final BusStopData source, final Supplier<String> text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final Contextualized contextualized)
        {
            super(source, text, dx, dy, textPlacement, color, contextualized, RenderableTextSource.RENDERWHEN10);
        }

        @Override
        public String toString()
        {
            return "Text []";
        }

    }

    /**
     * Provides the information required to draw a bus stop.
     */
    public interface BusStopData extends LaneBasedObjectData, LineLocatable
    {
    }

}
