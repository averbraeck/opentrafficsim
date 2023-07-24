package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2DInterface;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw a traffic light on the road at th place where the cars are expected to stop.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TrafficLightAnimation extends AbstractLineAnimation<TrafficLight>
        implements Renderable2DInterface<TrafficLight>, Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param trafficLight TrafficLight; the CSEBlock to draw
     * @param simulator OtsSimulatorInterface; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public TrafficLightAnimation(final TrafficLight trafficLight, final OtsSimulatorInterface simulator)
            throws NamingException, RemoteException
    {
        super(trafficLight, simulator, 0.9, new Length(0.5, LengthUnit.SI));

        this.text = new Text(trafficLight,
                trafficLight.getLane().getLink().getId() + "." + trafficLight.getLane().getId() + trafficLight.getId(),
                0.0f, (float) getHalfLength() + 0.2f, TextAlignment.CENTER, Color.BLACK, simulator);
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
        TrafficLight trafficLight = getSource();
        Color fillColor;
        switch (trafficLight.getTrafficLightColor())
        {
            case RED:
                fillColor = Color.red;
                break;

            case YELLOW:
                fillColor = Color.yellow;
                break;

            case GREEN:
                fillColor = Color.green;
                break;

            default:
                fillColor = Color.black;
                break;
        }

        // PaintPolygons.paintMultiPolygon(graphics, fillColor, trafficLight.getLocation(), trafficLight.getGeometry(), false);
        graphics.setColor(fillColor);
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
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class Text extends TextAnimation
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source Locatable; the object for which the text is displayed
         * @param text String; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param simulator OtsSimulatorInterface; the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final OtsSimulatorInterface simulator)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, simulator, TextAnimation.RENDERALWAYS);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Text []";
        }
    }

}
