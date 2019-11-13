package org.opentrafficsim.road.gtu.lane;

import java.util.Map;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.route.LaneBasedRouteNavigator;

/**
 * Specific type of LaneBasedGTU. This class adds length, width, maximum velocity and a reference to the simulator to the
 * AbstractLaneBasedGTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** the maximum length of the GTU (parallel with driving direction). */
    private final Length.Rel length;

    /** the maximum width of the GTU (perpendicular to driving direction). */
    private final Length.Rel width;

    /** the maximum speed of the GTU (in the driving direction). */
    private final Speed maximumVelocity;

    /** the simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * Construct a new AbstractLaneBasedIndividualGTU.
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param gtuFollowingModel the following model, including a reference to the simulator
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes
     * @param initialSpeed the initial speed of the car on the lane
     * @param length the maximum length of the GTU (parallel with driving direction)
     * @param width the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction)
     * @param routeNavigator RouteNavigator; the individual route that the GTU will take
     * @param simulator the simulator
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedIndividualGTU(final String id, final GTUType gtuType, final GTUFollowingModel gtuFollowingModel,
        final LaneChangeModel laneChangeModel, final Map<Lane, Length.Rel> initialLongitudinalPositions,
        final Speed initialSpeed, final Length.Rel length, final Length.Rel width, final Speed maximumVelocity,
        final LaneBasedRouteNavigator routeNavigator, final OTSDEVSSimulatorInterface simulator) throws
        NetworkException, SimRuntimeException, GTUException
    {
        super(id, gtuType, gtuFollowingModel, laneChangeModel, initialLongitudinalPositions, initialSpeed, routeNavigator,
            simulator);
        this.length = length;
        this.width = width;
        if (null == maximumVelocity)
        {
            throw new GTUException("maximumVelocity may not be null");
        }
        this.maximumVelocity = maximumVelocity;
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getMaximumVelocity()
    {
        return this.maximumVelocity;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

}