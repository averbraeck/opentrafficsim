package org.opentrafficsim.road.network.lane;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.StaticObject;

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

    /** The length of the object in the longitudinal direction, on the center line of the lane. */
    private final Length length;

    /**
     * Construct a new LanebasedObject with the required fields.
     * @param lane Lane; The lane for which this is a sensor
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param length Length; The length of the object in the longitudinal direction, on the center line of the lane
     * @param geometry the geometry of the object, which provides its location and bounds as well
     * @param height the height of the object, in case it is a 3D object
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public AbstractLaneBasedObject(final Lane lane, final Length longitudinalPosition, final Length length,
            final OTSLine3D geometry, final Length height) throws NetworkException
    {
        super(geometry, height);

        Throw.whenNull(lane, "lane is null");
        Throw.whenNull(longitudinalPosition, "longitudinal position is null");
        Throw.whenNull(geometry, "geometry is null");
        Throw.whenNull(length, "length is null");
        Throw.when(longitudinalPosition.si < 0.0 || longitudinalPosition.si > lane.getCenterLine().getLengthSI(),
                NetworkException.class, "Position of the object on the lane is out of bounds");

        this.lane = lane;
        this.longitudinalPosition = longitudinalPosition;
        this.length = length;
    }

    /**
     * Construct a new LanebasedObject with the required fields.
     * @param lane Lane; The lane for which this is a sensor
     * @param longitudinalPosition Length; The position (between 0.0 and the length of the Lane) of the sensor on the design
     *            line of the lane
     * @param length Length; The length of the object in the longitudinal direction, on the center line of the lane
     * @param geometry the geometry of the object, which provides its location and bounds as well
     * @throws NetworkException when the position on the lane is out of bounds
     */
    public AbstractLaneBasedObject(final Lane lane, final Length longitudinalPosition, final Length length,
            final OTSLine3D geometry) throws NetworkException
    {
        this(lane, longitudinalPosition, length, geometry, Length.ZERO);
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
    public final Length getLength()
    {
        return this.length;
    }

}
