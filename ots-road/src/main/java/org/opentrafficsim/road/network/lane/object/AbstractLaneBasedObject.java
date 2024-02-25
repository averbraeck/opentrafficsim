package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.StaticObject;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;

/**
 * An abstract implementation of the LaneBasedObject interface with the required fields being initialized and getters for those
 * fields. All StaticObjects are EventProducers, allowing them to provide state changes to subscribers.<br>
 * <br>
 * Note that extending classes must use a create(...) factory method that calls init() after fully constructing the object to
 * avoid "half constructed" objects to be registered in the network.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractLaneBasedObject extends StaticObject implements LaneBasedObject
{
    /** */
    private static final long serialVersionUID = 20160909L;

    /** The lane for which this is a sensor. */
    private final Lane lane;

    /** The position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    private final Length longitudinalPosition;

    /**
     * Construct a new AbstractLanebasedObject with the required fields.
     * @param id String; the id of the new object
     * @param lane Lane; The lane on which the new object resides. If the new object is a Sensor; it is automatically registered
     *            on the lane
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param geometry PolyLine2d; the geometry of the object, which provides its location and bounds as well
     * @param height Length; the height of the object, in case it is a 3D object
     * @throws NetworkException when the position on the lane is out of bounds
     */
    protected AbstractLaneBasedObject(final String id, final Lane lane, final Length longitudinalPosition,
            final PolyLine2d geometry, final Length height) throws NetworkException
    {
        super(id, getPoint(lane, longitudinalPosition), geometry, height);

        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(longitudinalPosition, "longitudinal position is null");
        Throw.when(longitudinalPosition.si < 0.0 || longitudinalPosition.si > lane.getCenterLine().getLength().si,
                NetworkException.class, "Position of the object on the lane is out of bounds");

        this.lane = lane;
        this.longitudinalPosition = longitudinalPosition;
    }

    /**
     * Returns the oriented point of the position on a lane.
     * @param lane Lane; lane.
     * @param longitudinalPosition Length; longitudinal position.
     * @return OrientedPoint2d; oriented point of the position on a lane.
     */
    private static OrientedPoint2d getPoint(final Lane lane, final Length longitudinalPosition)
    {
        return lane.getCenterLine().getLocationExtended(longitudinalPosition);
    }

    /**
     * Construct a new LaneBasedObject with the required fields.
     * @param id String; the id of the new AbstractLaneBasedObject
     * @param lane Lane; The lane for which this is a sensor
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param geometry PolyLine2d; the geometry of the object, which provides its location and bounds as well
     * @throws NetworkException when the position on the lane is out of bounds
     */
    protected AbstractLaneBasedObject(final String id, final Lane lane, final Length longitudinalPosition,
            final PolyLine2d geometry) throws NetworkException
    {
        this(id, lane, longitudinalPosition, geometry, Length.ZERO);
    }

    /** {@inheritDoc} */
    @Override
    protected void init() throws NetworkException
    {
        super.init();

        // OTS-218: detectors register themselves.
        if (!(this instanceof LaneDetector))
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
    public final Length getLongitudinalPosition()
    {
        return this.longitudinalPosition;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LaneBasedObject[" + getId() + "]";
    }

}
