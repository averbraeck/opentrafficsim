package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.draw.core.ClonableRenderable2DInterface;
import org.opentrafficsim.draw.core.TextAlignment;
import org.opentrafficsim.draw.core.TextAnimation;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Draw a traffic light on the road at th place where the cars are expected to stop.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrafficLightAnimation extends AbstractLineAnimation<TrafficLight>
        implements ClonableRenderable2DInterface<TrafficLight>, Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param trafficLight TrafficLight; the CSEBlock to draw
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public TrafficLightAnimation(final TrafficLight trafficLight, final SimulatorInterface.TimeDoubleUnit simulator)
            throws NamingException, RemoteException
    {
        super(trafficLight, simulator, 0.9, new Length(0.5, LengthUnit.SI));

        this.text = new Text(trafficLight,
                trafficLight.getLane().getParentLink().getId() + "." + trafficLight.getLane().getId() + trafficLight.getId(),
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
    public final void destroy(final SimulatorInterface<?, ?, ?> simulator)
    {
        super.destroy(simulator);
        this.text.destroy(simulator);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<TrafficLight> clone(final TrafficLight newSource,
            final SimulatorInterface.TimeDoubleUnit newSimulator) throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new TrafficLightAnimation(newSource, newSimulator);
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
         * @param source Locatable; the object for which the text is displayed
         * @param text String; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final SimulatorInterface.TimeDoubleUnit simulator)
                throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, simulator, TextAnimation.RENDERALWAYS);
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
            return "Text []";
        }
    }

}
