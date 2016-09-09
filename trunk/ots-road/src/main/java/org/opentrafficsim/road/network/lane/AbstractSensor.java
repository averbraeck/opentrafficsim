package org.opentrafficsim.road.network.lane;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.RelativePosition;

import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractSensor extends EventProducer implements Sensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The lane for which this is a sensor. */
    private final Lane lane;

    /** The position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    private final Length longitudinalPosition;

    /** The relative position of the vehicle that triggers the sensor. */
    private final RelativePosition.TYPE positionType;

    /** The name of the sensor. */
    private final String name;

    /** The cached location for animation. */
    private DirectedPoint location = null;

    /** The cached bounds for animation. */
    private Bounds bounds = null;

    /** The simulator for being able to generate an animation. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * @param lane Lane; the lane for which this is a sensor.
     * @param longitudinalPosition Length; the position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the sensor.
     * @param name String; the name of the sensor.
     * @param simulator OTSDEVSSimulatorInterface; the simulator (needed to generate the animation).
     */
    public AbstractSensor(final Lane lane, final Length longitudinalPosition,
        final RelativePosition.TYPE positionType, final String name, final OTSDEVSSimulatorInterface simulator)
    {
        this.lane = lane;
        this.longitudinalPosition = longitudinalPosition;
        this.positionType = positionType;
        this.name = name;
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public final Lane getLane()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLongitudinalPosition()
    {
        return this.longitudinalPosition;
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition.TYPE getPositionType()
    {
        return this.positionType;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation()
    {
        if (this.location == null)
        {
            try
            {
                this.location = this.lane.getCenterLine().getLocationSI(this.longitudinalPosition.si);
                this.location.z = this.lane.getLocation().z + 0.01;
            }
            catch (OTSGeometryException exception)
            {
                exception.printStackTrace();
                return null;
            }
        }
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
    {
        if (this.bounds == null)
        {
            this.bounds =
                new BoundingBox(new Point3d(-0.4, -this.lane.getWidth(0.0).getSI() * 0.4, 0.0), new Point3d(0.4,
                    this.lane.getWidth(0.0).getSI() * 0.4, this.lane.getLocation().z + 0.01));
        }
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
        long temp;
        temp = Double.doubleToLongBits(this.longitudinalPosition.si);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((this.positionType == null) ? 0 : this.positionType.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:designforextension"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractSensor other = (AbstractSensor) obj;
        if (this.lane == null)
        {
            if (other.lane != null)
                return false;
        }
        else if (!this.lane.equals(other.lane))
            return false;
        if (Double.doubleToLongBits(this.longitudinalPosition.si) != Double
            .doubleToLongBits(other.longitudinalPosition.si))
            return false;
        if (this.positionType == null)
        {
            if (other.positionType != null)
                return false;
        }
        else if (!this.positionType.equals(other.positionType))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int compareTo(final Sensor o)
    {
        if (this.lane != o.getLane())
        {
            return this.lane.hashCode() < o.getLane().hashCode() ? -1 : 1;
        }
        if (this.longitudinalPosition.si != o.getLongitudinalPosition().si)
        {
            return this.longitudinalPosition.si < o.getLongitudinalPosition().si ? -1 : 1;
        }
        if (!this.positionType.equals(o.getPositionType()))
        {
            return this.positionType.hashCode() < o.getPositionType().hashCode() ? -1 : 1;
        }
        if (!this.equals(o))
        {
            return this.hashCode() < o.hashCode() ? -1 : 1;
        }
        return 0;
    }

}
