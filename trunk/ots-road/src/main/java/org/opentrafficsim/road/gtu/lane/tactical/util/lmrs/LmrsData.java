package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Keeps data for LMRS for a specific GTU.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class LmrsData
{

    /** Form of synchronization. */
    private final Synchronization synchronization;

    /** Most recent leaders. */
    private final Set<String> leaders = new HashSet<>();

    /** Current leaders. */
    private final Set<String> tempLeaders = new HashSet<>();

    /** Vehicle that is being synchronized to. */
    private String syncVehicle;

    /**
     * @param synchronization synchronization
     */
    public LmrsData(final Synchronization synchronization)
    {
        this.synchronization = synchronization;
    }

    /**
     * Checks if the given leader is a new leader.
     * @param gtu gtu to check
     * @return whether the gtu is a new leader
     */
    boolean isNewLeader(final HeadwayGTU gtu)
    {
        this.tempLeaders.add(gtu.getId());
        return !this.leaders.contains(gtu.getId());
    }

    /**
     * Remembers the leaders of the current time step (those forwarded to isNewLeader()) for the next time step.
     */
    void finalizeStep()
    {
        this.leaders.clear();
        this.leaders.addAll(this.tempLeaders);
        this.tempLeaders.clear();
    }

    /**
     * Remembers the gtu that is synchronized to.
     * @param gtu gtu that is synchronized to
     */
    void setSyncVehicle(final HeadwayGTU gtu)
    {
        this.syncVehicle = gtu == null ? null : gtu.getId();
    }

    /**
     * Returns whether the provided gtu is the gtu that is synchronized to.
     * @param gtu gtu to inquiry
     * @return whether the provided gtu is the gtu that is synchronized to
     */
    boolean isSyncVehicle(final HeadwayGTU gtu)
    {
        return this.syncVehicle == null ? false : gtu.getId().equals(this.syncVehicle);
    }

    /**
     * Returns the gtu from the set that is the current sync vehicle, or {@code null} of there is no sync vehicle or it is not
     * in the set.
     * @param adjLeaders leaders in adjacent lane
     * @return gtu from the set that is the current sync vehicle
     */
    HeadwayGTU getSyncVehicle(final Set<HeadwayGTU> adjLeaders)
    {
        if (this.syncVehicle == null)
        {
            return null;
        }
        for (HeadwayGTU leader : adjLeaders)
        {
            if (leader.getId().equals(this.syncVehicle))
            {
                return leader;
            }
        }
        return null;
    }

    /**
     * Returns the synchronization.
     * @return synchronization
     */
    Synchronization getSynchronization()
    {
        return this.synchronization;
    }

}
