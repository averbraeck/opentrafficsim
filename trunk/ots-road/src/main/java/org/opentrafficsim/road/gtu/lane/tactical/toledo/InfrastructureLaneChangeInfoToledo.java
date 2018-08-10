package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;

import nl.tudelft.simulation.language.Throw;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 28, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class InfrastructureLaneChangeInfoToledo extends InfrastructureLaneChangeInfo implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Split number, 0 if this info does not regard a split. */
    private final int splitNumber;

    /**
     * @param requiredNumberOfLaneChanges number of lane changes
     * @param record record
     * @param splitNumber number of the split along the road
     * @throws GTUException if the split number is below 1
     */
    public InfrastructureLaneChangeInfoToledo(final int requiredNumberOfLaneChanges, final LaneStructureRecord record,
            final int splitNumber) throws GTUException
    {
        super(requiredNumberOfLaneChanges, record, RelativePosition.REFERENCE_POSITION, splitNumber > 0,
                LateralDirectionality.NONE);
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
