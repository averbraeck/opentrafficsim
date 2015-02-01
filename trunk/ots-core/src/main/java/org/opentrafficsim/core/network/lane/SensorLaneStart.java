package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;

import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * This is a sensor that is placed at the start of a Lane to register a GTU on the lane, and register the lane with the GTU when
 * the front of the vehicle passes over the sensor.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SensorLaneStart extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /**
     * Place a sensor that is triggered with the front of the GTU at the start of the lane.
     * @param lane The lane for which this is a sensor.
     */
    public SensorLaneStart(final Lane lane)
    {
        super(lane, new DoubleScalar.Abs<LengthUnit>(Math.ulp(0.0), LengthUnit.METER), RelativePosition.FRONT);
    }

    /**
     * {@inheritDoc} <br>
     * For this method, we assume that the right sensor triggered this method. In this case the sensor that indicates the front
     * of the GTU. The code triggering the sensor therefore has to do the checking for sensor type.
     * @throws RemoteException on communications failure
     */
    @Override
    public final void trigger(final LaneBasedGTU<?> gtu) throws RemoteException
    {
        // The GTU is already in the lane to trigger the sensor. So no: gtu.addLane(getLane());
        try
        {
            // if the GTU has the front as its reference point: it enters with its front.
            // otherwise, negatively displaced by the difference between the front and the reference position.
            getLane().addGTU(gtu, new DoubleScalar.Rel<LengthUnit>(-gtu.getFront().getDx().getSI(), LengthUnit.SI));
        }
        catch (NetworkException exception)
        {
            // Cannot happen
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SensorLaneStart [getLane()=" + this.getLane() + ", getLongitudinalPosition()="
            + this.getLongitudinalPosition() + ", getPositionType()=" + this.getPositionType() + "]";
    }

}
