package org.opentrafficsim.core.network.lane;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Sensor extends Serializable, Comparable<Sensor>
{
    /** @return The lane for which this is a sensor. */
    Lane getLane();

    /** @return the position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    DoubleScalar.Abs<LengthUnit> getLongitudinalPosition();

    /** @return the position as a double in SI units for quick sorting and sensor triggering. */
    double getLongitudinalPositionSI();

    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the sensor. */
    RelativePosition.TYPE getPositionType();

    /**
     * Trigger an action on the GTU. Normally this is the GTU that triggered the sensor. The typical call therefore is
     * <code>sensor.trigger(this);</code>.
     * @param gtu the GTU for which to carry out the trigger action.
     * @throws RemoteException on communications failure
     */
    void trigger(LaneBasedGTU<?> gtu) throws RemoteException;
}
