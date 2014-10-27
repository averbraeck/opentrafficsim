package org.opentrafficsim.car;

import java.rmi.RemoteException;
import java.util.Map;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public class Car<ID> extends AbstractLaneBasedGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param length the maximum length of the GTU (parallel with driving direction).
     * @param width the maximum width of the GTU (perpendicular to driving direction).
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction).
     * @param gtuFollowingModel the following model, including a reference to the simulator.
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes.
     * @param initialSpeed the initial speed of the car on the lane.
     * @param simulator the simulator.
     * @throws RemoteException in case the simulation time cannot be read.
     */
    public Car(final ID id, final GTUType<?> gtuType, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Rel<LengthUnit> width, final DoubleScalar.Abs<SpeedUnit> maximumVelocity,
            final GTUFollowingModel gtuFollowingModel,
            final Map<Lane, DoubleScalar.Abs<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed, final OTSDEVSSimulatorInterface simulator)
            throws RemoteException
    {
        super(id, gtuType, length, width, maximumVelocity, gtuFollowingModel, initialLongitudinalPositions, initialSpeed,
                simulator);
    }
    
    /** {@inheritDoc} */
    public final String toString()
    {
        try
        {
            return String.format("Car %s rear:%s front:%s", getId().toString(), positionOfRear(), positionOfFront());
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
        return("Caught exception in toString");
    }

}
