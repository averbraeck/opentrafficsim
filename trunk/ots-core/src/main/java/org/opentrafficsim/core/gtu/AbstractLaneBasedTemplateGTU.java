package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;
import java.util.Map;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractLaneBasedTemplateGTU<ID> extends AbstractLaneBasedGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param gtuFollowingModel the following model, including a reference to the simulator.
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes.
     * @param initialSpeed the initial speed of the car on the lane.
     * @throws RemoteException in case the simulation time cannot be read.
     */
    public AbstractLaneBasedTemplateGTU(final ID id, final TemplateGTUType<?> gtuType,
        final GTUFollowingModel gtuFollowingModel,
        final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
        final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException
    {
        super(id, gtuType, gtuFollowingModel, initialLongitudinalPositions, initialSpeed, gtuType.getSimulator()
            .getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public TemplateGTUType<?> getGTUType()
    {
        return getGTUType();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return getGTUType().getLength();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getWidth()
    {
        return getGTUType().getWidth();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getMaximumVelocity()
    {
        return getGTUType().getMaximumVelocity();
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return getGTUType().getSimulator();
    }

}
