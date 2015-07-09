package org.opentrafficsim.core.gtu.lane;

import java.rmi.RemoteException;
import java.util.Map;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Specific type of LaneBasedGTU. This class adds length, width, maximum velocity and a reference to the simulator to
 * the AbstractLaneBasedGTU.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractLaneBasedIndividualGTU<ID> extends AbstractLaneBasedGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** the maximum length of the GTU (parallel with driving direction). */
    private final DoubleScalar.Rel<LengthUnit> length;

    /** the maximum width of the GTU (perpendicular to driving direction). */
    private final DoubleScalar.Rel<LengthUnit> width;

    /** the maximum speed of the GTU (in the driving direction). */
    private final DoubleScalar.Abs<SpeedUnit> maximumVelocity;

    /** the simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * Construct a new AbstractLaneBasedIndividualGTU.
     * @param id the id of the GTU, could be String or Integer
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param gtuFollowingModel the following model, including a reference to the simulator
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes
     * @param initialSpeed the initial speed of the car on the lane
     * @param length the maximum length of the GTU (parallel with driving direction)
     * @param width the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction)
     * @param route Route; the route that the GTU will take
     * @param simulator the simulator
     * @throws RemoteException when the simulator cannot be reached
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedIndividualGTU(final ID id, final GTUType<?> gtuType,
            final GTUFollowingModel gtuFollowingModel, final LaneChangeModel laneChangeModel,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Rel<LengthUnit> width, final DoubleScalar.Abs<SpeedUnit> maximumVelocity,
            final Route route, final OTSDEVSSimulatorInterface simulator) throws RemoteException, NetworkException,
            SimRuntimeException, GTUException
    {
        super(id, gtuType, gtuFollowingModel, laneChangeModel, initialLongitudinalPositions, initialSpeed, route,
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
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getMaximumVelocity()
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
