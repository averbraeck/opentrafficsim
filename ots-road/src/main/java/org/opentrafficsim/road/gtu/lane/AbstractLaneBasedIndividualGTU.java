package org.opentrafficsim.road.gtu.lane;

import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * Specific type of LaneBasedGTU. This class adds length, width, maximum velocity and a reference to the simulator to the
 * AbstractLaneBasedGTU.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedIndividualGTU extends AbstractLaneBasedGTU
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** The maximum length of the GTU (parallel with driving direction). */
    private final Length length;

    /** The maximum width of the GTU (perpendicular to driving direction). */
    private final Length width;

    /** The maximum speed of the GTU (in the driving direction). */
    private final Speed maximumVelocity;

    /**
     * Construct a new AbstractLaneBasedIndividualGTU.
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes
     * @param initialSpeed the initial speed of the car on the lane
     * @param length the maximum length of the GTU (parallel with driving direction)
     * @param width the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction)
     * @param simulator the simulator
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param perception the lane-based perception model of the GTU
     * @param network the network that the GTU is initially registered in
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     * @throws OTSGeometryException when the initial path is wrong
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedIndividualGTU(final String id, final GTUType gtuType,
        final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
        final Length length, final Length width, final Speed maximumVelocity,
        final OTSDEVSSimulatorInterface simulator, final LaneBasedStrategicalPlanner strategicalPlanner,
        final LanePerception perception, final OTSNetwork network) throws NetworkException, SimRuntimeException,
        GTUException, OTSGeometryException
    {
        super(id, gtuType, initialLongitudinalPositions, initialSpeed, simulator, strategicalPlanner, perception,
            network);
        this.length = length;
        this.width = width;
        if (null == maximumVelocity)
        {
            throw new GTUException("maximumVelocity may not be null");
        }
        this.maximumVelocity = maximumVelocity;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getMaximumVelocity()
    {
        return this.maximumVelocity;
    }

}
