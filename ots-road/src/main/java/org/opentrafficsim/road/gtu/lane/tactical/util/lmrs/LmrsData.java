package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.DesireBased;
import org.opentrafficsim.road.gtu.lane.tactical.Synchronizable;

/**
 * Keeps data for LMRS for a specific GTU.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class LmrsData implements DesireBased, Synchronizable
{

    /** Form of synchronization. */
    private final Synchronization synchronization;

    /** Form of cooperation. */
    private final Cooperation cooperation;

    /** Form of gap-acceptance. */
    private final GapAcceptance gapAcceptance;

    /** Form of tail gating. */
    private final Tailgating tailgating;

    /** Most recent leaders. */
    private final Set<String> leaders = new LinkedHashSet<>();

    /** Current leaders. */
    private final Set<String> tempLeaders = new LinkedHashSet<>();

    /** Latest desire value for visualization. */
    private final Map<Class<? extends Incentive>, Desire> desireMap = new LinkedHashMap<>();

    /** Synchronization state. */
    private Synchronizable.State synchronizationState = Synchronizable.State.NONE;

    /** Vehicle that is being synchronized to. */
    private String syncVehicle;

    /** Whether the longitudinal control is human. */
    private boolean humanLongitudinalControl = true;

    /**
     * @param synchronization Synchronization; synchronization
     * @param cooperation Cooperation; cooperation
     * @param gapAcceptance GapAcceptance; gap-acceptance
     * @param tailgating Tailgating; tail gating
     */
    public LmrsData(final Synchronization synchronization, final Cooperation cooperation, final GapAcceptance gapAcceptance,
            final Tailgating tailgating)
    {
        this.synchronization = synchronization;
        this.cooperation = cooperation;
        this.gapAcceptance = gapAcceptance;
        this.tailgating = tailgating;
    }

    /**
     * Checks if the given leader is a new leader.
     * @param gtu HeadwayGTU; gtu to check
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
     * @param gtu HeadwayGTU; gtu that is synchronized to
     */
    void setSyncVehicle(final HeadwayGTU gtu)
    {
        this.syncVehicle = gtu == null ? null : gtu.getId();
    }

    /**
     * Returns whether the provided gtu is the gtu that is synchronized to.
     * @param gtu HeadwayGTU; gtu to inquiry
     * @return whether the provided gtu is the gtu that is synchronized to
     */
    boolean isSyncVehicle(final HeadwayGTU gtu)
    {
        return this.syncVehicle == null ? false : gtu.getId().equals(this.syncVehicle);
    }

    /**
     * Returns the gtu from the set that is the current sync vehicle, or {@code null} of there is no sync vehicle or it is not
     * in the set.
     * @param adjLeaders PerceptionCollectable&lt;HeadwayGTU,LaneBasedGTU&gt;; leaders in adjacent lane
     * @return gtu from the set that is the current sync vehicle
     */
    HeadwayGTU getSyncVehicle(final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> adjLeaders)
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

    /**
     * Returns the cooperation.
     * @return cooperation
     */
    Cooperation getCooperation()
    {
        return this.cooperation;
    }

    /**
     * Return the gap-acceptance.
     * @return gap-acceptance
     */
    GapAcceptance getGapAcceptance()
    {
        return this.gapAcceptance;
    }

    /**
     * Return the tail gating.
     * @return gap-acceptance
     */
    Tailgating getTailgating()
    {
        return this.tailgating;
    }

    /** {@inheritDoc} */
    @Override
    public Desire getLatestDesire(final Class<? extends Incentive> incentiveClass)
    {
        return this.desireMap.get(incentiveClass);
    }

    /**
     * Returns the desire map.
     * @return Map&lt;Class&lt;? extends Incentive&gt;, Desire&gt;; desire map
     */
    Map<Class<? extends Incentive>, Desire> getDesireMap()
    {
        return this.desireMap;
    }

    /**
     * Sets the synchronization state.
     * @param synchronizationState Synchronizable.State; synchronization step
     */
    void setSynchronizationState(final Synchronizable.State synchronizationState)
    {
        this.synchronizationState = synchronizationState;
    }

    /** {@inheritDoc} */
    @Override
    public Synchronizable.State getSynchronizationState()
    {
        return this.synchronizationState;
    }

    /**
     * @return humanLongitudinalControl.
     */
    boolean isHumanLongitudinalControl()
    {
        return this.humanLongitudinalControl;
    }

    /**
     * @param humanLongitudinalControl boolean; set humanLongitudinalControl.
     */
    public void setHumanLongitudinalControl(final boolean humanLongitudinalControl)
    {
        this.humanLongitudinalControl = humanLongitudinalControl;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LmrsData [synchronization=" + this.synchronization + ", leaders=" + this.leaders + ", tempLeaders="
                + this.tempLeaders + ", syncVehicle=" + this.syncVehicle + "]";
    }

}
