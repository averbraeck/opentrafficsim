package org.opentrafficsim.core.gtu;

import java.util.Set;

import org.opentrafficsim.core.network.LaneLocation;
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
public interface LaneBasedGTU<ID> extends GTU<ID>
{
    /**
     * @param delta the reference point relative to the vehicle's position for which we want to know the location on all lanes
     *            where the vehicle is registered.
     * @return the location of the GTU, relative to one or more Lanes
     */
    Set<LaneLocation> getCurrentLocation(GTUReferencePoint delta);
    
    /**
     * @param delta the preference point relative to the vehicle's position.
     * @param location the location on a lane to which we want to calculate the relative distance. 
     * @return the relative distance of a point on this vehicle to another point in a lane.
     */
    DoubleScalar.Rel<LengthUnit> longitudinalDistance(GTUReferencePoint delta, LaneLocation location); 

    /** @return the velocity of the GTU, in the direction of the lane */
    DoubleScalar<SpeedUnit> getCurrentLongitudinalVelocity();

    /** @return the velocity of the GTU, perpendicular to the direction of the lane */
    DoubleScalar<SpeedUnit> getCurrentLateralVelocity();
}
