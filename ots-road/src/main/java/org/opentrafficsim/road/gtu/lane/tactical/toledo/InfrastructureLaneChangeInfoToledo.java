package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.io.Serializable;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */

public class InfrastructureLaneChangeInfoToledo extends InfrastructureLaneChangeInfo implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Split number, 0 if this info does not regard a split. */
    private final int splitNumber;

    /**
     * @param requiredNumberOfLaneChanges int; number of lane changes
     * @param record LaneStructureRecord; record
     * @param splitNumber int; number of the split along the road
     * @throws GtuException if the split number is below 1
     */
    public InfrastructureLaneChangeInfoToledo(final int requiredNumberOfLaneChanges, final LaneStructureRecord record,
            final int splitNumber) throws GtuException
    {
        super(requiredNumberOfLaneChanges, record, RelativePosition.REFERENCE_POSITION, splitNumber > 0,
                LateralDirectionality.NONE);
        Throw.when(splitNumber <= 0, GtuException.class, "Split number should be at least 1.");
        this.splitNumber = splitNumber;
    }

    /**
     * Returns whether this information regards a split in the road.
     * @return whether this information regards a split in the road
     */
    public final boolean forSplit()
    {
        return this.splitNumber > 0;
    }

    /**
     * Returns the split number.
     * @return split number
     */
    public final int getSplitNumber()
    {
        return this.splitNumber;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "InfrastructureLaneChangeInfoToledo [requiredNumberOfLaneChanges=" + getRequiredNumberOfLaneChanges()
                + ", remainingDistance=" + getRemainingDistance() + ", split=" + this.splitNumber + "]";
    }
}
