package org.opentrafficsim.road.gtu.lane;

import org.djunits.value.vdouble.scalar.Acceleration;

/**
 * Interface for vehicle models.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 mei 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface VehicleModel
{

    /** No bounds. */
    VehicleModel NONE = new VehicleModel()
    {
        @Override
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGTU gtu)
        {
            return acceleration;
        }
    };

    /** Acceleration bounded by GTU min and max acceleration. */
    VehicleModel MINMAX = new VehicleModel()
    {
        @Override
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGTU gtu)
        {
            return acceleration.si > gtu.getMaximumDeceleration().si
                    ? (acceleration.si < gtu.getMaximumAcceleration().si ? acceleration : gtu.getMaximumAcceleration())
                    : gtu.getMaximumDeceleration();
        }
    };

    /**
     * Returns a bounded acceleration.
     * @param acceleration Acceleration; intended acceleration
     * @param gtu LaneBasedGTU; gtu
     * @return Acceleration; possible acceleration
     */
    Acceleration boundAcceleration(Acceleration acceleration, LaneBasedGTU gtu);

}
