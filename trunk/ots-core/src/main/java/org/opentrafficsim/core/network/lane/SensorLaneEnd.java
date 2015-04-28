package org.opentrafficsim.core.network.lane;

import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * This is a sensor that is placed at the end of a Lane to unregister a GTU from the lane, and unregister the lane from
 * the GTU when the back of the vehicle passes over the sensor.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SensorLaneEnd extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /**
     * Place a sensor that is triggered with the back of the GTU one ulp (see <code>Math.ulp(double d)</code>) before
     * the end of the lane to make sure it will always be triggered, independent of the algorithm used to move the GTU.
     * @param lane The lane for which this is a sensor.
     */
    public SensorLaneEnd(final Lane lane)
    {
        super(lane, new DoubleScalar.Rel<LengthUnit>(lane.getLength().getSI() - Math.ulp(lane.getLength().getSI()),
                LengthUnit.METER), RelativePosition.REAR);
    }

    /**
     * {@inheritDoc} <br>
     * For this method, we assume that the right sensor triggered this method. In this case the sensor that indicates
     * the front of the GTU. The code triggering the sensor therefore has to do the checking for sensor type.
     */
    @Override
    public final void trigger(final LaneBasedGTU<?> gtu)
    {
        /*-
        try
        {
            System.out.println(gtu.getSimulator().getSimulatorTime().get() + ": removing " + gtu + " at end of lane " 
                    + getLane());
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
        */
        gtu.removeLane(getLane());
        getLane().removeGTU(gtu);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SensorLaneEnd [getLane()=" + this.getLane() + ", getLongitudinalPosition()="
                + this.getLongitudinalPosition() + ", getPositionType()=" + this.getPositionType() + "]";
    }

}
