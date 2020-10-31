package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.StaticObject;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * An abstract implementation of the LaneBasedObject interface with the required fields being initialized and getters for those
 * fields. All StaticObjects are EventProducers, allowing them to provide state changes to subscribers.<br>
 * <br>
 * Note that extending classes must use a create(...) factory method that calls init() after fully constructing the object to
 * avoid "half constructed" objects to be registered in the network.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 10, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractLaneBasedObject extends StaticObject implements LaneBasedObject
{
    /** */
    private static final long serialVersionUID = 20160909L;

    /** The lane for which this is a sensor. */
    private final Lane lane;

    /** The direction in which this is valid. */
    private final LongitudinalDirectionality direction;

    /** The position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    private final Length longitudinalPosition;

    /** The DirectedPoint that indicates the location on the lane. */
    private final DirectedPoint location;

    /**
     * Construct a new AbstractLanebasedObject with the required fields.
     * @param id String; the id of the new object
     * @param lane Lane; The lane on which the new object resides. If the new object is a Sensor; it is automatically registered
     *            on the lane
     * @param direction LongitudinalDirectionality; the directionality in which this is valid.
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param geometry OTSLine3D; the geometry of the object, which provides its location and bounds as well
     * @param height Length; the height of the object, in case it is a 3D object
     * @throws NetworkException when the position on the lane is out of bounds
     */
    protected AbstractLaneBasedObject(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final OTSLine3D geometry, final Length height) throws NetworkException
    {
        super(id, geometry, height);

        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(direction, "Longitudinal direction is null");
        Throw.whenNull(longitudinalPosition, "longitudinal position is null");
        Throw.when(longitudinalPosition.si < 0.0 || longitudinalPosition.si > lane.getCenterLine().getLengthSI(),
                NetworkException.class, "Position of the object on the lane is out of bounds");

        this.lane = lane;
        this.direction = direction;
        this.longitudinalPosition = longitudinalPosition;
        DirectedPoint p = lane.getCenterLine().getLocationExtended(this.longitudinalPosition);
        this.location = new DirectedPoint(p.x, p.y, p.z + 0.01, p.getRotX(), p.getRotY(), p.getRotZ());
    }

    /**
     * Construct a new AbstractLanebasedObject with the required fields.
     * @param id String; the id of the new object
     * @param lane Lane; The lane on which the new object resides. If the new object is a Sensor; it is automatically registered
     *            on the lane
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param geometry OTSLine3D; the geometry of the object, which provides its location and bounds as well
     * @param height Length; the height of the object, in case it is a 3D object
     * @throws NetworkException when the position on the lane is out of bounds
     */
    protected AbstractLaneBasedObject(final String id, final Lane lane, final Length longitudinalPosition,
            final OTSLine3D geometry, final Length height) throws NetworkException
    {
        this(id, lane, LongitudinalDirectionality.DIR_BOTH, longitudinalPosition, geometry, height);
    }

    /**
     * Construct a new LaneBasedObject with the required fields.
     * @param id String; the id of the new AbstractLaneBasedObject
     * @param lane Lane; The lane for which this is a sensor
     * @param direction LongitudinalDirectionality; the directionality in which this is valid.
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param geometry OTSLine3D; the geometry of the object, which provides its location and bounds as well
     * @throws NetworkException when the position on the lane is out of bounds
     */
    protected AbstractLaneBasedObject(final String id, final Lane lane, final LongitudinalDirectionality direction,
            final Length longitudinalPosition, final OTSLine3D geometry) throws NetworkException
    {
        this(id, lane, direction, longitudinalPosition, geometry, Length.ZERO);
    }

    /**
     * Construct a new LaneBasedObject with the required fields.
     * @param id String; the id of the new AbstractLaneBasedObject
     * @param lane Lane; The lane for which this is a sensor
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param geometry OTSLine3D; the geometry of the object, which provides its location and bounds as well
     * @throws NetworkException when the position on the lane is out of bounds
     */
    protected AbstractLaneBasedObject(final String id, final Lane lane, final Length longitudinalPosition,
            final OTSLine3D geometry) throws NetworkException
    {
        this(id, lane, longitudinalPosition, geometry, Length.ZERO);
    }

    /** {@inheritDoc} */
    @Override
    protected void init() throws NetworkException
    {
        super.init();

        // OTS-218: sensors register themselves.
        if (!(this instanceof SingleSensor))
        {
            this.lane.addLaneBasedObject(this); // implements OTS-218
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String getFullId()
    {
        return getLane().getFullId() + "." + super.getId();
    }

    /** {@inheritDoc} */
    @Override
    public final Lane getLane()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public final LongitudinalDirectionality getDirection()
    {
        return this.direction;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLongitudinalPosition()
    {
        return this.longitudinalPosition;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public final StaticObject clone(final Network newNetwork, final boolean animation) throws NetworkException
    {
        throw new NetworkException("LaneBasedObjects should be cloned with the clone(lane, simulator, animation) method");
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LaneBasedObject[" + getId() + "]";
    }

    /**
     * Clone the LaneBasedObject for e.g., copying a network.
     * @param newCSE CrossSectionElement; the new cross section element to which the clone belongs
     * @param newSimulator SimulatorInterface.TimeDoubleUnit; the new simulator for this network
     * @return AbstractLaneBasedObject; a clone of this object
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public abstract AbstractLaneBasedObject clone(CrossSectionElement newCSE, SimulatorInterface.TimeDoubleUnit newSimulator)
            throws NetworkException;

}
