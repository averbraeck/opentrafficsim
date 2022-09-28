package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * sink sensor animation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrafficLightSensorAnimation extends Renderable2D<TrafficLightSensor> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** The traffic light sensor. */
    private final TrafficLightSensor sensor;

    /** Path of the detector. */
    private final OTSLine3D path;

    /**
     * Construct a SensorAnimation.
     * @param sensor TrafficLightSensor; the traffic light sensor that will be animated
     * @param simulator OTSSimulatorInterface; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     * @throws OTSGeometryException when the geometry is bad
     */
    public TrafficLightSensorAnimation(final TrafficLightSensor sensor, final OTSSimulatorInterface simulator)
            throws NamingException, RemoteException, OTSGeometryException
    {
        super(sensor, simulator);
        this.sensor = sensor;
        OTSLine3D coordinates = this.sensor.getPath();
        double dx = this.sensor.getLocation().x;
        double dy = this.sensor.getLocation().y;
        double dz = this.sensor.getLocation().z;
        List<OTSPoint3D> points = new ArrayList<>(coordinates.size());
        for (OTSPoint3D p : coordinates.getPoints())
        {
            points.add(new OTSPoint3D(p.x - dx, p.y - dy, p.z - dz));
        }
        this.path = new OTSLine3D(points);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(this.sensor.getOccupancy() ? Color.BLUE : Color.BLACK);
        OTSPoint3D prevPoint = null;
        for (OTSPoint3D p : this.path.getPoints())
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
