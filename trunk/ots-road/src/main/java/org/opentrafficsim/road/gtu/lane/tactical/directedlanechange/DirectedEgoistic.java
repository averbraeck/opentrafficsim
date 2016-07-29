package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.road.gtu.lane.perceptionold.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;

/**
 * The egoistic drive changes lane when this yields is personal advantage (totally ignoring any disadvantage to others).
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1375 $, $LastChangedDate: 2015-09-03 03:32:20 +0200 (Thu, 03 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Sep 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DirectedEgoistic extends AbstractDirectedLaneChangeModel
{
    /**
     * @param perception the perception to use
     */
    public DirectedEgoistic(final LanePerceptionFull perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration applyDriverPersonality(final DualAccelerationStep accelerations)
    {
        // The egoistic driver only looks at the effects on him-/herself.
        return accelerations.getLeaderAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Egoistic";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Egoistic lane change model (as described by Treiber).";
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectedEgoistic [name=" + this.getName() + "]";
    }

}
