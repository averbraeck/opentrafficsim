package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djutils.base.Identifiable;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsRenderable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.TextAlignment;
import org.opentrafficsim.draw.TextAnimation;
import org.opentrafficsim.draw.road.TrafficLightDetectorAnimation.TrafficLightDetectorData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Traffic light detector animation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TrafficLightDetectorAnimation extends OtsRenderable<TrafficLightDetectorData>
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** The traffic light detector. */
    private final TrafficLightDetectorData detector;

    /** Path of the detector. */
    private final Set<Path2D.Float> paths;

    /** the Text object to destroy when the animation is destroyed. */
    private final Text text;

    /**
     * Construct a TrafficLightDetectorAnimation.
     * @param detector TrafficLightSensor; the traffic light detector that will be animated
     * @param contextualized Contextualized; context provider
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public TrafficLightDetectorAnimation(final TrafficLightDetectorData detector, final Contextualized contextualized)
            throws NamingException, RemoteException
    {
        super(detector, contextualized);
        this.detector = detector;
        this.paths = PaintPolygons.getPaths(this.detector.getGeometry().getPointList());
        this.text = new Text(detector, detector::getId, 0.0f, 0.5f + 0.2f, TextAlignment.CENTER, Color.BLACK, contextualized);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setStroke(new BasicStroke(0.2f));
        setRendering(graphics);
        PaintPolygons.paintPaths(graphics, this.detector.getOccupancy() ? Color.BLUE : Color.BLACK, this.paths, false);
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
    public final String toString()
    {
        return "TrafficLightDetectorAnimation [getSource()=" + this.getSource() + "]";
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
    public class Text extends TextAnimation<TrafficLightDetectorData, Text> implements DetectorData.Text
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source TrafficLightDetectorData; the object for which the text is displayed
         * @param text Supplier&lt;String&gt;; the text to display
         * @param dx float; the horizontal movement of the text, in meters
         * @param dy float; the vertical movement of the text, in meters
         * @param textPlacement TextAlignment; where to place the text
         * @param color Color; the color of the text
         * @param contextualized Contextualized; context provider
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public Text(final TrafficLightDetectorData source, final Supplier<String> text, final float dx, final float dy,
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
     * TrafficLightDetectorData provides the information required to draw a traffic light detector.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface TrafficLightDetectorData extends DetectorData, Identifiable
    {
        /**
         * Returns the geometry.
         * @return PolyLine2d; geometry.
         */
        PolyLine2d getGeometry();

        /**
         * Returns whether the detector is occupied.
         * @return boolean; whether the detector is occupied.
         */
        boolean getOccupancy();

        /** {@inheritDoc} */
        @Override
        OrientedPoint2d getLocation();

        /** {@inheritDoc} */
        @Override
        default double getZ()
        {
            return DrawLevel.OBJECT.getZ();
        }
    }

}
