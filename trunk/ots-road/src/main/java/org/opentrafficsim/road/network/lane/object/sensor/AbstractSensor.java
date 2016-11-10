package org.opentrafficsim.road.network.lane.object.sensor;

import java.util.HashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.immutablecollections.ImmutableHashSet;
import org.opentrafficsim.base.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;

import nl.tudelft.simulation.language.Throw;
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
public abstract class AbstractSensor extends AbstractLaneBasedObject implements Sensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The relative position of the vehicle that triggers the sensor. */
    private final RelativePosition.TYPE positionType;

    /** The simulator for being able to generate an animation. */
    private final OTSDEVSSimulatorInterface simulator;

    /** The GTU types will trigger this particular sensor. */
    private final ImmutableSet<GTUType> triggeringGTUTypes;

    /** The immutable set for all gtu type. Implements OTS-219. */
    protected static final ImmutableSet<GTUType> GTUTYPE_SET_ALL;

    static
    {
        Set<GTUType> gtuTypeSet = new HashSet<>();
        gtuTypeSet.add(GTUType.ALL);
        GTUTYPE_SET_ALL = new ImmutableHashSet<GTUType>(gtuTypeSet);

    }

    /**
     * Create a sensor on a lane at a position on that lane.
     * @param id String; the id of the sensor.
     * @param lane Lane; the lane for which this is a sensor.
     * @param longitudinalPosition Length; the position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the sensor.
     * @param simulator OTSDEVSSimulatorInterface; the simulator (needed to generate the animation).
     * @param geometry the geometry of the object, which provides its location and bounds as well
     * @param triggeringGTUTypes Tthe GTU types will trigger this particular sensor
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public AbstractSensor(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.TYPE positionType, final OTSDEVSSimulatorInterface simulator, final OTSLine3D geometry,
            final ImmutableSet<GTUType> triggeringGTUTypes) throws NetworkException
    {
        super(id, lane, longitudinalPosition, geometry);
        Throw.when(simulator == null, NullPointerException.class, "simulator is null");
        Throw.when(positionType == null, NullPointerException.class, "positionType is null");
        Throw.when(id == null, NullPointerException.class, "id is null");
        this.positionType = positionType;
        this.simulator = simulator;
        this.triggeringGTUTypes = triggeringGTUTypes;
        getLane().addSensor(this); // Implements OTS-218
    }

    /**
     * Create a sensor on a lane at a position on that lane.
     * @param id String; the id of the sensor.
     * @param lane Lane; the lane for which this is a sensor.
     * @param longitudinalPosition Length; the position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the sensor.
     * @param simulator OTSDEVSSimulatorInterface; the simulator (needed to generate the animation).
     * @param geometry the geometry of the object, which provides its location and bounds as well
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public AbstractSensor(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.TYPE positionType, final OTSDEVSSimulatorInterface simulator, final OTSLine3D geometry)
            throws NetworkException
    {
        this(id, lane, longitudinalPosition, positionType, simulator, geometry, GTUTYPE_SET_ALL);
    }

    /**
     * @param id String; the id of the sensor.
     * @param lane Lane; the lane for which this is a sensor.
     * @param longitudinalPosition Length; the position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane.
     * @param positionType RelativePosition.TYPE; the relative position type (e.g., FRONT, BACK) of the vehicle that triggers
     *            the sensor.
     * @param simulator OTSDEVSSimulatorInterface; the simulator (needed to generate the animation).
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public AbstractSensor(final String id, final Lane lane, final Length longitudinalPosition,
            final RelativePosition.TYPE positionType, final OTSDEVSSimulatorInterface simulator) throws NetworkException
    {
        this(id, lane, longitudinalPosition, positionType, simulator, makeGeometry(lane, longitudinalPosition));
    }

    /**
     * Make a geometry perpendicular to the center line of the lane with a length of 90% of the width of the lane.
     * @param lane the lane for which to make a perpendicular geometry
     * @param longitudinalPosition the position on the lane
     * @return an OTSLine3D that describes the line
     * @throws NetworkException in case the sensor point on the center line of the lane cannot be found
     */
    private static OTSLine3D makeGeometry(final Lane lane, final Length longitudinalPosition) throws NetworkException
    {
        try
        {
            double w45 = lane.getWidth(longitudinalPosition).si * 0.45;
            DirectedPoint c = lane.getCenterLine().getLocation(longitudinalPosition);
            double a = c.getRotZ();
            OTSPoint3D p1 = new OTSPoint3D(c.x + w45 * Math.cos(a + Math.PI / 2), c.y - w45 * Math.sin(a + Math.PI / 2), c.z);
            OTSPoint3D p2 = new OTSPoint3D(c.x - w45 * Math.cos(a + Math.PI / 2), c.y + w45 * Math.sin(a + Math.PI / 2), c.z);
            return new OTSLine3D(p1, p2);
        }
        catch (OTSGeometryException exception)
        {
            throw new NetworkException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void trigger(final LaneBasedGTU gtu)
    {
        fireTimedEvent(Sensor.SENSOR_TRIGGER_EVENT, new Object[] { getId(), this, gtu, this.positionType },
                getSimulator().getSimulatorTime());
        triggerResponse(gtu);
    }

    /**
     * Implementation of the response to a trigger of this sensor by a GTU.
     * @param gtu LaneBasedGTU; the lane based GTU that triggered this sensor.
     */
    protected abstract void triggerResponse(final LaneBasedGTU gtu);

    /** {@inheritDoc} */
    @Override
    public final RelativePosition.TYPE getPositionType()
    {
        return this.positionType;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSet<GTUType> getTriggeringGTUTypes()
    {
        return this.triggeringGTUTypes;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLane() == null) ? 0 : getLane().hashCode());
        long temp;
        temp = Double.doubleToLongBits(getLongitudinalPosition().si);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((this.positionType == null) ? 0 : this.positionType.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "checkstyle:needbraces", "checkstyle:designforextension" })
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
        if (this.getLane() == null)
        {
            if (other.getLane() != null)
                return false;
        }
        else if (!this.getLane().equals(other.getLane()))
            return false;
        if (Double.doubleToLongBits(this.getLongitudinalPosition().si) != Double
                .doubleToLongBits(other.getLongitudinalPosition().si))
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
        if (this.getLane() != o.getLane())
        {
            return this.getLane().hashCode() < o.getLane().hashCode() ? -1 : 1;
        }
        if (this.getLongitudinalPosition().si != o.getLongitudinalPosition().si)
        {
            return this.getLongitudinalPosition().si < o.getLongitudinalPosition().si ? -1 : 1;
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

    /** {@inheritDoc} */
    @Override
    public abstract AbstractSensor clone(CrossSectionElement newCSE, OTSSimulatorInterface newSimulator, boolean animation)
            throws NetworkException;

}
