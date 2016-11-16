package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.StaticObject;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.Sensor;

import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * An abstract implementation of the LaneBasedObject interface with the required fields being initialized and getters for those
 * fields. All StaticObjects are EventProducers, allowing them to provide state changes to subscribers.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** The position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    private final Length longitudinalPosition;
    
    /** The DirectedPoint that indicates the location on the lane. */
    private final DirectedPoint location; 

    /**
     * Construct a new LanebasedObject with the required fields.
     * @param id the id
     * @param lane Lane; The lane for which this is a sensor
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param geometry the geometry of the object, which provides its location and bounds as well
     * @param height the height of the object, in case it is a 3D object
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public AbstractLaneBasedObject(final String id, final Lane lane, final Length longitudinalPosition,
            final OTSLine3D geometry, final Length height) throws NetworkException
    {
        super(id, geometry, height);

        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(longitudinalPosition, "longitudinal position is null");
        Throw.when(longitudinalPosition.si < 0.0 || longitudinalPosition.si > lane.getCenterLine().getLengthSI(),
                NetworkException.class, "Position of the object on the lane is out of bounds");

        this.lane = lane;
        this.longitudinalPosition = longitudinalPosition;
        this.location = lane.getCenterLine().getLocationExtended(this.longitudinalPosition);

        if (!(this instanceof Sensor))
        {
            this.lane.addLaneBasedObject(this); // implements OTS-218
        }
    }

    /**
     * Construct a new LanebasedObject with the required fields.
     * @param id the id
     * @param lane Lane; The lane for which this is a sensor
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param geometry the geometry of the object, which provides its location and bounds as well
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public AbstractLaneBasedObject(final String id, final Lane lane, final Length longitudinalPosition,
            final OTSLine3D geometry) throws NetworkException
    {
        this(id, lane, longitudinalPosition, geometry, Length.ZERO);
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
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public final StaticObject clone(final Network newNetwork, final OTSSimulatorInterface newSimulator, final boolean animation)
            throws NetworkException
    {
        throw new NetworkException("LaneBasedObjects should be cloned with the clone(lane, simulator, animation) method");
    }

    /**
     * Clone the LAneBasedObject for e.g., copying a network.
     * @param newCSE the new cross section element to which the clone belongs
     * @param newSimulator the new simulator for this network
     * @param animation whether to (re)create animation or not
     * @return a clone of this object
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public abstract AbstractLaneBasedObject clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
            final boolean animation) throws NetworkException;

}
