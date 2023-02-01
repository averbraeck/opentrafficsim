package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
<<<<<<< HEAD
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

    /** The traffic light sensor. */
    private final TrafficLightDetector sensor;

    /** Path of the detector. */
    private final OtsLine3D path;

    /**
     * Construct a TrafficLightDetectorAnimation.
     * @param detector TrafficLightSensor; the traffic light detector that will be animated
     * @param simulator OTSSimulatorInterface; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     * @throws OtsGeometryException when the geometry is bad
     */
    public TrafficLightDetectorAnimation(final TrafficLightDetector detector, final OtsSimulatorInterface simulator)
            throws NamingException, RemoteException, OtsGeometryException
    {
        super(detector, simulator);
        this.sensor = detector;
        OtsLine3D coordinates = this.sensor.getPath();
        double dx = this.sensor.getLocation().x;
        double dy = this.sensor.getLocation().y;
        double dz = this.sensor.getLocation().z;
        List<OtsPoint3D> points = new ArrayList<>(coordinates.size());
        for (OtsPoint3D p : coordinates.getPoints())
        {
            points.add(new OtsPoint3D(p.x - dx, p.y - dy, p.z - dz));
        }
        this.path = new OtsLine3D(points);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(this.sensor.getOccupancy() ? Color.BLUE : Color.BLACK);
        OtsPoint3D prevPoint = null;
        for (OtsPoint3D p : this.path.getPoints())
        {
            if (null != prevPoint)
            {
                // System.out.println("Drawing sensor line from " + prevPoint + " to " + p);
                graphics.drawLine((int) prevPoint.x, (int) prevPoint.y, (int) p.x, (int) p.y);
            }
            prevPoint = p;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SensorAnimation [getSource()=" + this.getSource() + "]";
    }

}
