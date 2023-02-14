package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * Traffic light detector animation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TrafficLightDetectorAnimation extends Renderable2D<TrafficLightDetector> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** The traffic light detector. */
    private final TrafficLightDetector detector;

    /** Path of the detector. */
    private final Path2D.Float polygon;

    /**
     * Construct a TrafficLightDetectorAnimation.
     * @param detector TrafficLightSensor; the traffic light detector that will be animated
     * @param simulator OtsSimulatorInterface; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     * @throws OtsGeometryException when the geometry is bad
     */
    public TrafficLightDetectorAnimation(final TrafficLightDetector detector, final OtsSimulatorInterface simulator)
            throws NamingException, RemoteException, OtsGeometryException
    {
        super(detector, simulator);
        this.detector = detector;
        OtsLine3d coordinates = this.detector.getGeometry();
        this.polygon = new Path2D.Float();
        this.polygon.moveTo(coordinates.get(0).x, coordinates.get(0).y);
        for (int i = 1; i < coordinates.size(); i++)
        {
            this.polygon.lineTo(coordinates.get(i).x, coordinates.get(i).y);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(this.detector.getOccupancy() ? Color.BLUE : Color.BLACK);
        graphics.setStroke(new BasicStroke(0.2f));
        graphics.draw(this.polygon);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrafficLightDetectorAnimation [getSource()=" + this.getSource() + "]";
    }

}
