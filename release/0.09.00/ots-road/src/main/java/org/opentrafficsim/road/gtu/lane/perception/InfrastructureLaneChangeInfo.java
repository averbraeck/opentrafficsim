package org.opentrafficsim.road.gtu.lane.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.Throw;

/**
 * Contains information by which drivers know when they need to leave a lane in order to be able to stay on the infrastructure
 * and follow their route.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 2, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class InfrastructureLaneChangeInfo implements Comparable<InfrastructureLaneChangeInfo>
{

    /** Required number of lane changes. */
    private final int requiredNumberOfLaneChanges;

    /** Remaining distance to perform required lane changes. */
    private final Length remainingDistance;

    /**
     * Constructor.
     * @param requiredNumberOfLaneChanges required number of lane changes
     * @param remainingDistance remaining distance to perform required lane changes
     * @throws IllegalArgumentException if required number of lane changes or remaining distance is negative
     * @throws NullPointerException if remaining distance is null
     */
    public InfrastructureLaneChangeInfo(final int requiredNumberOfLaneChanges, final Length remainingDistance)
    {
        Throw.when(requiredNumberOfLaneChanges < 0, IllegalArgumentException.class,
            "Required number of lane changes may not be negative.");
        Throw.whenNull(remainingDistance, "Remaining distance may not be null.");
        Throw.when(remainingDistance.si < 0, IllegalArgumentException.class, "Remaining distance may not be negative.");
        this.requiredNumberOfLaneChanges = requiredNumberOfLaneChanges;
        this.remainingDistance = remainingDistance;
    }

    /**
     * @return requiredNumberOfLaneChanges required number of lane changes.
     */
    public final int getRequiredNumberOfLaneChanges()
    {
        return this.requiredNumberOfLaneChanges;
    }

    /**
     * @return remainingDistance remaining distance to perform required lane changes.
     */
    public final Length getRemainingDistance()
    {
        return this.remainingDistance;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "InfrastructureLaneChangeInfo [requiredNumberOfLaneChanges=" + this.requiredNumberOfLaneChanges
                + ", remainingDistance=" + this.remainingDistance + "]";
    }

    /** {@inheritDoc} */
    @Override
    public final int compareTo(final InfrastructureLaneChangeInfo infrastructureLaneChangeInfo)
    {
        return this.remainingDistance.compareTo(infrastructureLaneChangeInfo.getRemainingDistance());
    }

}
