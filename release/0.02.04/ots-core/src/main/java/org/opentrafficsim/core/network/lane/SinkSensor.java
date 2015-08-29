package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * sensor that deletes the GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-12 16:37:45 +0200 (Wed, 12 Aug 2015) $, @version $Revision: 1240 $, by $Author: averbraeck $,
 * initial version an 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SinkSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /**
     * @param lane the lane that triggers the deletion of the GTU.
     * @param position the position of the sensor
     * @param simulator the simulator to enable animation.
     */
    public SinkSensor(final Lane lane, final DoubleScalar.Rel<LengthUnit> position,
        final OTSSimulatorInterface simulator)
    {
        super(lane, position, RelativePosition.FRONT, "SINK@" + lane.toString(), simulator);
        try
        {
            new SinkAnimation(this, simulator);
        }
        catch (RemoteException | NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void trigger(final LaneBasedGTU gtu) throws RemoteException
    {
        gtu.destroy();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SinkSensor [Lane=" + this.getLane() + "]";
    }
}
