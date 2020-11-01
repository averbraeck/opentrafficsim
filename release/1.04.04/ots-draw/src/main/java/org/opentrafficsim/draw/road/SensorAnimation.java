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
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Sensor animation.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-12 16:37:45 +0200 (Wed, 12 Aug 2015) $, @version $Revision: 1240 $, by $Author: averbraeck $,
 * initial version Jan 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SensorAnimation extends AbstractLineAnimation<SingleSensor>
        implements ClonableRenderable2DInterface<SingleSensor>, Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** the position of the sensor on the lane to determine the width of the lane at that point. */
    private final Length sensorPosition;

    /** The color of the sensor. */
    private final Color color;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Construct a SensorAnimation.
     * @param sensor SingleSensor; the Sensor to draw
     * @param sensorPosition Length; the position of the sensor on the lane to determine the width of the lane at that point
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator to schedule on
     * @param color Color; the display color of the sensor
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public SensorAnimation(final SingleSensor sensor, final Length sensorPosition,
            final SimulatorInterface.TimeDoubleUnit simulator, final Color color) throws NamingException, RemoteException
    {
        super(sensor, simulator, .9, new Length(0.5, LengthUnit.SI));
        this.sensorPosition = sensorPosition;
        this.color = color;

        this.text = new Text(sensor, sensor.getLane().getParentLink().getId() + "." + sensor.getLane().getId() + sensor.getId(),
                0.0f, (float) getHalfLength() + 0.2f, TextAlignment.CENTER, Color.BLACK, simulator);
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
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(this.color);
        super.paint(graphics, observer);
    }

    /** {@inheritDoc} */
    @Override
    public final void destroy() throws NamingException, RemoteException
    {
        super.destroy();
        this.text.destroy();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<SingleSensor> clone(final SingleSensor newSource,
            final SimulatorInterface.TimeDoubleUnit newSimulator) throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new SensorAnimation(newSource, this.sensorPosition, newSimulator, this.color);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SensorAnimation [getSource()=" + this.getSource() + "]";
    }

    /**
     * Text animation for the Sensor. Separate class to be able to turn it on and off...
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
