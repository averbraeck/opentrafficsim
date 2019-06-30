package org.opentrafficsim.ahfe;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.PerceptionException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@Deprecated
public abstract class AbstractDelayedNeighborsPerception extends AbstractDelayedPerceptionCategory
        implements NeighborsPerception
{

    /** */
    private static final long serialVersionUID = 20170217L;

    /** Reaction time parameter type. */
    protected static final ParameterTypeDuration TR = ParameterTypes.TR;

    /** Time step parameter type. */
    protected static final ParameterTypeDuration DT = ParameterTypes.DT;

    /** First update time. */
    private Time initialTime = null;

    /** Wrapped direct perception. */
    private final DirectNeighborsPerception direct;

    /** Reaction time. */
    private Duration reactionTime = null;

    /** Time step of planner. */
    private Duration plannerTimeStep = null;

    /** Remainder between reaction time and planner time step. */
    private Duration remainder = null;

    /** Info type id base for first leaders. */
    public static final String FIRSTLEADERS = "firstLeaders";

    /** Info type id base for first followers. */
    public static final String FIRSTFOLLOWERS = "firstFollower";

    /** Info type id base for gtu alongside. */
    public static final String GTUALONGSIDE = "gtuAlongside";

    /** Info type id base for leaders. */
    public static final String LEADERS = "leaders";

    /** Info type id base for followers. */
    public static final String FOLLOWERS = "followers";

    /** Info type id for cross-section. */
    public static final NeighborsInfoType<SortedSet<RelativeLane>> CROSSSECTION = new NeighborsInfoType<>("cross-section");

    /** Id for odometer info type. */
    public static final NeighborsInfoType<Length> ODOMETER = new NeighborsInfoType<>("odometer");

    /** Override for left lane change. */
    private boolean gtuAlongsideLeftOverride = false;

    /** Override for right lane change. */
    private boolean gtuAlongsideRightOverride = false;

    /**
     * Constructor.
     * @param perception LanePerception; perception
     */
    public AbstractDelayedNeighborsPerception(final LanePerception perception)
    {
        super(perception);
        this.direct = new DirectNeighborsPerception(perception, HeadwayGtuType.COPY);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll() throws GTUException, NetworkException, ParameterException
    {

        if (this.remainder == null)
        {
            try
            {
                // TODO The reaction time may differ between observed objects and vary over time
                Parameters params = getPerception().getGtu().getParameters();
                this.reactionTime = params.getParameter(TR);
                this.plannerTimeStep = params.getParameter(DT);
                double rem;
                if (this.reactionTime.eq0())
                {
                    rem = 0;
                }
                else if (this.reactionTime.gt(this.plannerTimeStep))
                {
                    rem = this.reactionTime.si % this.plannerTimeStep.si;
                }
                else
                {
                    rem = this.plannerTimeStep.si - this.reactionTime.si;
                }
                this.remainder = Duration.createSI(rem);
            }
            catch (ParameterException | GTUException exception)
            {
                throw new RuntimeException("Exception while setting up delayed neighors perception.", exception);
            }
        }

        // direct perception in first few time steps; build up history
        Time now = getPerception().getGtu().getSimulator().getSimulatorTime();
        if (this.initialTime == null)
        {
            this.initialTime = now;
        }
        if (now.minus(this.initialTime).le(this.reactionTime))
        {
            updateAllDelayed();
            return;
        }

        if (this.remainder.eq0())
        {
            // reaction time is multiple of time step, just do it now
            updateAllDelayed();
        }
        else
        {
            // schedule actual update slightly in the future: this will be the snapshot for a future time step
            Time scheduledTime = now.plus(this.remainder);
            try
            {
                getPerception().getGtu().getSimulator().scheduleEventAbs(scheduledTime, this, this, "updateAllDelayed", null);
            }
            catch (SimRuntimeException exception)
            {
                throw new RuntimeException("Scheduling perception update in the past.", exception);
            }
        }

        /*
         * During the reaction time, an instantaneous lane change by a neighbor may be performed, which may cause an
         * unreasonable lane change of the subject vehicle if it is not considered. Therefore, the 'gtuAlongSide' information is
         * amended with a current snapshot of the surroundings. If the current first leaders/followers contains a GTU that the
         * delayed leaders/followers do not contain, and that is within 50m, 'gtuAlongSide' is overruled with 'true', preventing
         * a lane change.
         */
        if (getPerception().getLaneStructure().getExtendedCrossSection().contains(RelativeLane.LEFT))
        {
            this.gtuAlongsideLeftOverride = newFirstLeaderOrFollower(getFollowers(RelativeLane.LEFT),
                    this.direct.getFirstFollowers(LateralDirectionality.LEFT))
                    || newFirstLeaderOrFollower(getLeaders(RelativeLane.LEFT),
                            this.direct.getFirstLeaders(LateralDirectionality.LEFT))
                    || this.direct.isGtuAlongside(LateralDirectionality.LEFT);
        }
        if (getPerception().getLaneStructure().getExtendedCrossSection().contains(RelativeLane.RIGHT))
        {
            this.gtuAlongsideRightOverride = newFirstLeaderOrFollower(getFollowers(RelativeLane.RIGHT),
                    this.direct.getFirstFollowers(LateralDirectionality.RIGHT))
                    || newFirstLeaderOrFollower(getLeaders(RelativeLane.RIGHT),
                            this.direct.getFirstLeaders(LateralDirectionality.RIGHT))
                    || this.direct.isGtuAlongside(LateralDirectionality.RIGHT);
        }

    }

    /**
     * Returns whether there is a gtu in the current set that is not present in the delayed set.
     * @param delayedSet Iterable&lt;? extends HeadwayGTU&gt;; delayed set
     * @param currentSet Set&lt;? extends HeadwayGTU&gt;; current set
     * @return whether there is a gtu in the current set that is not present in the delayed set
     */
    private boolean newFirstLeaderOrFollower(final Iterable<? extends HeadwayGTU> delayedSet,
            final Set<? extends HeadwayGTU> currentSet)
    {
        Set<String> set = new LinkedHashSet<>();
        for (HeadwayGTU gtu : delayedSet)
        {
            set.add(gtu.getId());
        }
        for (HeadwayGTU gtu : currentSet)
        {
            if (!set.contains(gtu.getId()) && gtu.getDistance().si < 50)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether to override the gtu alongside boolean as true.
     * @param lat LateralDirectionality; lateral direction
     * @return whether to override the gtu alongside boolean as true
     */
    public final boolean isGtuAlongsideOverride(final LateralDirectionality lat)
    {
        return lat.isLeft() ? this.gtuAlongsideLeftOverride : this.gtuAlongsideRightOverride;
    }

    /**
     * Performs actual update.
     * @throws ParameterException if parameter is not present or is given a wrong value
     * @throws NetworkException on error in the network
     * @throws GTUException if not initialized
     */
    // TODO private when DSOL allows
    protected void updateAllDelayed() throws GTUException, NetworkException, ParameterException
    {

        try
        {
            getGtu().getReferencePosition();
        }
        catch (GTUException exception)
        {
            // GTU was destroyed
            return;
        }

        this.direct.updateAll();
        // below code is a copy of the updateAll() method in the direct perception TODO structure better
        setInfo(CROSSSECTION,
                new TimeStampedObject<>(getPerception().getLaneStructure().getExtendedCrossSection(), getTimestamp()));
        setInfo(ODOMETER, new TimeStampedObject<>(getGtu().getOdometer(), getTimestamp()));
    }

    /**
     * Returns the cross-section on which the most recent observed neighbors were determined.
     * @return cross-section on which the most recent observed neighbors were determined
     */
    public final SortedSet<RelativeLane> getDelayedCrossSection()
    {
        try
        {
            return getInfo(CROSSSECTION).getObject();
        }
        catch (PerceptionException exception)
        {
            throw new RuntimeException("Crosssection was not perceived.", exception);
        }
    }

    /**
     * Delayed information about the type of the neighbors. <br>
     * @param <T> data type of info
     */
    public static final class NeighborsInfoType<T> extends DelayedInfoType<T>
    {

        /** Map of id's and lane info types. */
        private static final Map<String, NeighborsInfoType<?>> LANEINFOTYPES = new LinkedHashMap<>();

        /**
         * Construct new info.
         * @param id String; id
         */
        public NeighborsInfoType(final String id)
        {
            super(id, TR);
        }

        /**
         * Returns a (cached) info type for a sorted set of GTU's.
         * @param id String; id
         * @return info type
         */
        @SuppressWarnings("unchecked")
        public static NeighborsInfoType<SortedSet<HeadwayGTU>> getSortedSetType(final String id)
        {
            if (!LANEINFOTYPES.containsKey(id))
            {
                LANEINFOTYPES.put(id, new NeighborsInfoType<SortedSet<HeadwayGTU>>(id));
            }
            return (NeighborsInfoType<SortedSet<HeadwayGTU>>) LANEINFOTYPES.get(id);
        }

        /**
         * Returns a (cached) info type for a sorted set of GTU's.
         * @param id String; id
         * @return info type
         */
        @SuppressWarnings("unchecked")
        public static NeighborsInfoType<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>> getIterableType(final String id)
        {
            if (!LANEINFOTYPES.containsKey(id))
            {
                LANEINFOTYPES.put(id, new NeighborsInfoType<SortedSet<HeadwayGTU>>(id));
            }
            return (NeighborsInfoType<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>>) LANEINFOTYPES.get(id);
        }

        /**
         * Returns a (cached) info type for a sorted set of GTU's.
         * @param id String; id
         * @return info type
         */
        @SuppressWarnings("unchecked")
        public static NeighborsInfoType<Boolean> getBooleanType(final String id)
        {
            if (!LANEINFOTYPES.containsKey(id))
            {
                LANEINFOTYPES.put(id, new NeighborsInfoType<SortedSet<HeadwayGTU>>(id));
            }
            return (NeighborsInfoType<Boolean>) LANEINFOTYPES.get(id);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "NeighborsInfoType []";
        }

    }

}
