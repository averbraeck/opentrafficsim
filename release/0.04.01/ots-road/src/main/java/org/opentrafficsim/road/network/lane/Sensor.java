package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Sensor extends Serializable, Comparable<Sensor>, LocatableInterface
{
    /** @return The lane for which this is a sensor. */
    Lane getLane();

    /** @return the position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane. */
    Length.Rel getLongitudinalPosition();

    /** @return the position as a double in SI units for quick sorting and sensor triggering. */
    double getLongitudinalPositionSI();

    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the sensor. */
    RelativePosition.TYPE getPositionType();

    /**
     * Trigger an action on the GTU. Normally this is the GTU that triggered the sensor. The typical call therefore is
     * <code>sensor.trigger(this);</code>.
     * @param gtu the GTU for which to carry out the trigger action.
    */
    void trigger(LaneBasedGTU gtu);

    /** @return The name of the sensor. */
    String getName();
    
    /** {@inheritDoc} */
    @Override
    DirectedPoint getLocation();
    
    /** {@inheritDoc} */
    @Override
    Bounds getBounds();

}
