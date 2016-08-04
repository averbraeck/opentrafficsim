package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 28, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class InfrastructureLaneChangeInfoToledo extends InfrastructureLaneChangeInfo
{

    /** Split number, 0 if this info does not regard a split. */
    private final int splitNumber;

    /**
     * @param requiredNumberOfLaneChanges number of lane changes
     * @param remainingDistance remaining distance
     */
    public InfrastructureLaneChangeInfoToledo(final int requiredNumberOfLaneChanges, final Length remainingDistance)
    {
        super(requiredNumberOfLaneChanges, remainingDistance);
        this.splitNumber = 0;
    }

    /**
     * @param requiredNumberOfLaneChanges number of lane changes
     * @param remainingDistance remaining distance
     * @param splitNumber number of the split along the road
     * @throws GTUException if the split number is below 1
     */
    public InfrastructureLaneChangeInfoToledo(final int requiredNumberOfLaneChanges, final Length remainingDistance,
        final int splitNumber) throws GTUException
    {
        super(requiredNumberOfLaneChanges, remainingDistance);
        Throw.when(splitNumber <= 0, GTUException.class, "Split number should be at least 1.");
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
