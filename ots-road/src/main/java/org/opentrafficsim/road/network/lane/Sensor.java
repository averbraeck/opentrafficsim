package org.opentrafficsim.road.network.lane;

import java.io.Serializable;

import javax.media.j3d.Bounds;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Sensor extends Serializable, Comparable<Sensor>, LaneBasedObject
{
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

    /** @return The simulator. */
    OTSDEVSSimulatorInterface getSimulator();
    
    /** {@inheritDoc} */
    @Override
    DirectedPoint getLocation();

    /** {@inheritDoc} */
    @Override
    Bounds getBounds();


}
