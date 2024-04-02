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
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw a traffic light on the road at th place where the cars are expected to stop.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrafficLightAnimation extends AbstractLineAnimation<TrafficLightData>
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param trafficLight TrafficLightData; the traffic light
     * @param contextualized Contextualized; context provider
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public TrafficLightAnimation(final TrafficLightData trafficLight, final Contextualized contextualized)
            throws NamingException, RemoteException
    {
        super(trafficLight, contextualized, 0.9, new Length(0.5, LengthUnit.SI));

        this.text = new Text(trafficLight, trafficLight::getId, 0.0f, (float) getHalfLength() + 0.2f, TextAlignment.CENTER,
                Color.BLACK, contextualized);
    }

    /**
     * @return text.
     */
    public final Text getText()
    {
        return this.text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(getSource().getColor());
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
        return "TrafficLightAnimation [getSource()=" + this.getSource() + "]";
    }

    /**
     * Text animation for the TrafficLight. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public class Text extends TextAnimation<TrafficLightData, Text>
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source TrafficLightData; the object for which the text is displayed
         * @param text Supplier&lt;String&gt;; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param contextualized Contextualized; context provider
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final TrafficLightData source, final Supplier<String> text, final float dx, final float dy,
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
     * TrafficLightData provides the information required to draw a traffic light.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface TrafficLightData extends LaneBasedObjectData
    {
        /**
         * Returns the traffic light color.
         * @return Color; traffic light color.
         */
        Color getColor();
    }

}
