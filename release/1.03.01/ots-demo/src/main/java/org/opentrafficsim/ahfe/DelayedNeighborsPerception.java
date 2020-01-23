package org.opentrafficsim.ahfe;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.perception.PerceptionException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.SortedSetPerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborTriplet;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.network.OTSRoadNetwork;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.jstats.distributions.DistNormal;

/**
 * Implementation of delayed neighbors perception which anticipates using constant speed.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@Deprecated
public class DelayedNeighborsPerception extends AbstractDelayedNeighborsPerception implements EventListenerInterface
{

    /** Parameter for anticipating beyond current time. */
    public static final ParameterTypeDuration TA =
            new ParameterTypeDuration("ta", "anticipation time in future", Duration.ZERO, ConstraintInterface.POSITIVEZERO);

    /** Parameter for correlation in errors. */
    public static final ParameterTypeDuration TAUE = new ParameterTypeDuration("tau_e", "error correlation time",
            Duration.instantiateSI(20), ConstraintInterface.POSITIVE);

    /** Parameter for distance error factor. */
    public static final ParameterTypeDouble SERROR =
            new ParameterTypeDouble("s_error", "distance error factor", 0.1, ConstraintInterface.POSITIVEZERO);

    /** Parameter for speed error factor. */
    public static final ParameterTypeDouble VERROR =
            new ParameterTypeDouble("v_error", "speed error factor", 0.1, ConstraintInterface.POSITIVEZERO);

    /** Parameter for acceleration error factor. */
    public static final ParameterTypeDouble AERROR =
            new ParameterTypeDouble("a_error", "acceleration error factor", 0.2, ConstraintInterface.POSITIVEZERO);

    /** Margin to check time step in Wiener process. */
    private static final double MARGIN = 1e-6;

    /** */
    private static final long serialVersionUID = 20170217L;

    /** Form of anticipation. */
    private final Anticipation anticipation;

    /** Latest update time of neighbor rearrangement. */
    private Time rearrangeTime;

    /** Set of followers per relative lane. */
    private final Map<RelativeLane, PerceptionCollectable<HeadwayGTU, LaneBasedGTU>> followers = new LinkedHashMap<>();

    /** Set of leaders per relative lane. */
    private final Map<RelativeLane, PerceptionCollectable<HeadwayGTU, LaneBasedGTU>> leaders = new LinkedHashMap<>();

    /** Set of first followers per lane upstream of merge per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, SortedSet<HeadwayGTU>> firstFollowers = new LinkedHashMap<>();

    /** Set of first leaders per lane downstream of split per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, SortedSet<HeadwayGTU>> firstLeaders = new LinkedHashMap<>();

    /** Whether a GTU is alongside per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, Boolean> gtuAlongside = new LinkedHashMap<>();

    /** Map of errors by a Wiener process for each GTU. */
    private LinkedHashMap<String, ErrorValue> errors = new LinkedHashMap<>();

    /** Random numbers for perception errors. */
    private final DistNormal norm;

    /**
     * @param perception LanePerception; perception
     * @param anticipation Anticipation; anticipation
     */
    public DelayedNeighborsPerception(final LanePerception perception, final Anticipation anticipation)
    {
        super(perception);
        Throw.whenNull(anticipation, "Anticipation may not be null.");
        this.anticipation = anticipation;
        try
        {
            this.norm = new DistNormal(perception.getGtu().getSimulator().getReplication().getStream("perception"));
            perception.getGtu().addListener(this, LaneBasedGTU.LANE_CHANGE_EVENT);
        }
        catch (GTUException | RemoteException exception)
        {
            throw new RuntimeException("GTU not initialized.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        changeLane((LateralDirectionality) ((Object[]) event.getContent())[1]);
    }

    /**
     * Rearrange neighbors, i.e. a follower may be anticipated to be a leader, etc.
     */
    private void rearrangeNeighbors()
    {
        Time time;
        Duration ta;
        Duration taue;
        Length length;
        Length traveledDistance;
        double distanceError;
        double speedError;
        double accelerationError;
        Speed egoSpeed;
        Duration dt;
        try
        {
            time = getPerception().getGtu().getSimulator().getSimulatorTime();
            if (time.equals(this.rearrangeTime))
            {
                return;
            }
            Parameters params = getPerception().getGtu().getParameters();
            ta = params.getParameter(TA);
            taue = params.getParameter(TAUE);
            distanceError = params.getParameter(SERROR);
            speedError = params.getParameter(VERROR);
            accelerationError = params.getParameter(AERROR);
            length = getPerception().getGtu().getLength();
            EgoPerception ego = getPerception().getPerceptionCategory(EgoPerception.class);
            egoSpeed = ego.getSpeed();
            dt = params.getParameter(DT);
            try
            {
                traveledDistance = getPerception().getGtu().getOdometer().minus(getInfo(ODOMETER).getObject());
            }
            catch (PerceptionException exception)
            {
                throw new RuntimeException("Odometer not percieved.", exception);
            }
            if (!ta.eq0())
            {
                Acceleration acceleration = ego.getAcceleration();
                traveledDistance = traveledDistance.plus(this.anticipation.egoAnticipation(egoSpeed, acceleration, ta));
            }
            this.rearrangeTime = time;
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("GTU not initialized while rearranging neighbors.", exception);
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Could not obtain parameter.", exception);
        }
        catch (OperationalPlanException exception)
        {
            throw new RuntimeException("No ego perception.", exception);
        }
        this.firstFollowers.clear();
        this.firstLeaders.clear();
        this.gtuAlongside.clear();
        this.followers.clear();
        this.leaders.clear();
        try
        {
            for (RelativeLane lane : getDelayedCrossSection())
            {

                // adjacent lanes
                if (lane.getNumLanes() == 1)
                {
                    // alongside, initial (can be overwritten as true by anticipation of first leaders/followers)
                    boolean gtuAlongSide = getInfo(NeighborsInfoType.getBooleanType(GTUALONGSIDE), lane).getObject();

                    // followers
                    SortedSet<HeadwayGTU> firstFollowersSet = new TreeSet<>();
                    this.firstFollowers.put(lane.getLateralDirectionality(), firstFollowersSet);
                    TimeStampedObject<SortedSet<HeadwayGTU>> delayedFirstFollowers =
                            getInfo(NeighborsInfoType.getSortedSetType(FIRSTFOLLOWERS), lane);
                    Duration d = time.minus(delayedFirstFollowers.getTimestamp()).plus(ta);
                    for (HeadwayGTU gtu : delayedFirstFollowers.getObject())
                    {
                        NeighborTriplet info = this.anticipation.anticipate(erroneousTriplet(gtu.getDistance().neg(),
                                gtu.getSpeed(), gtu.getAcceleration(), getError(gtu.getId(), taue, dt), distanceError,
                                speedError, accelerationError, egoSpeed), d, traveledDistance, false);
                        if (info.getHeadway().le0())
                        {
                            firstFollowersSet.add(gtu.moved(info.getHeadway().neg(), info.getSpeed(), info.getAcceleration()));
                        }
                        else
                        {
                            gtuAlongSide = true;
                        }
                    }

                    // leaders
                    SortedSet<HeadwayGTU> firstLeaderssSet = new TreeSet<>();
                    this.firstLeaders.put(lane.getLateralDirectionality(), firstLeaderssSet);
                    TimeStampedObject<SortedSet<HeadwayGTU>> delayedFirstLeaders =
                            getInfo(NeighborsInfoType.getSortedSetType(FIRSTLEADERS), lane);
                    d = time.minus(delayedFirstLeaders.getTimestamp()).plus(ta);
                    for (HeadwayGTU gtu : delayedFirstLeaders.getObject())
                    {
                        NeighborTriplet info = this.anticipation.anticipate(erroneousTriplet(gtu.getDistance(), gtu.getSpeed(),
                                gtu.getAcceleration(), getError(gtu.getId(), taue, dt), distanceError, speedError,
                                accelerationError, egoSpeed), d, traveledDistance, true);
                        if (info.getHeadway().ge0())
                        {
                            firstLeaderssSet.add(gtu.moved(info.getHeadway(), info.getSpeed(), info.getAcceleration()));
                        }
                        else
                        {
                            gtuAlongSide = true;
                        }
                    }

                    // store alongside
                    this.gtuAlongside.put(lane.getLateralDirectionality(), gtuAlongSide);
                }

                // initiate sets
                SortedSetPerceptionIterable<HeadwayGTU> followersSet =
                        new SortedSetPerceptionIterable<>((OTSRoadNetwork) getPerception().getGtu().getReferencePosition()
                                .getLane().getParentLink().getNetwork());
                this.followers.put(lane, followersSet);
                SortedSetPerceptionIterable<HeadwayGTU> leadersSet =
                        new SortedSetPerceptionIterable<>((OTSRoadNetwork) getPerception().getGtu().getReferencePosition()
                                .getLane().getParentLink().getNetwork());
                this.leaders.put(lane, leadersSet);

                // followers
                TimeStampedObject<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>> delayedFollowers =
                        getInfo(NeighborsInfoType.getIterableType(FOLLOWERS), lane);
                Duration d = time.minus(delayedFollowers.getTimestamp()).plus(ta);

                PerceptionCollectable<HeadwayGTU, LaneBasedGTU> perc = delayedFollowers.getObject();
                for (HeadwayGTU gtu : delayedFollowers.getObject())
                {
                    NeighborTriplet info = this.anticipation.anticipate(
                            erroneousTriplet(gtu.getDistance().neg(), gtu.getSpeed(), gtu.getAcceleration(),
                                    getError(gtu.getId(), taue, dt), distanceError, speedError, accelerationError, egoSpeed),
                            d, traveledDistance, false);
                    if (info.getHeadway().le(length) || lane.isCurrent())
                    {
                        followersSet.add(gtu.moved(info.getHeadway().neg(), info.getSpeed(), info.getAcceleration()));
                    }
                    else
                    {
                        leadersSet.add(gtu.moved(info.getHeadway().minus(length).minus(gtu.getLength()), info.getSpeed(),
                                info.getAcceleration()));
                    }
                }

                // leaders
                TimeStampedObject<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>> delayedLeaders =
                        getInfo(NeighborsInfoType.getIterableType(LEADERS), lane);
                d = time.minus(delayedLeaders.getTimestamp()).plus(ta);
                for (HeadwayGTU gtu : delayedLeaders.getObject())
                {

                    NeighborTriplet info = this.anticipation.anticipate(
                            erroneousTriplet(gtu.getDistance(), gtu.getSpeed(), gtu.getAcceleration(),
                                    getError(gtu.getId(), taue, dt), distanceError, speedError, accelerationError, egoSpeed),
                            d, traveledDistance, true);
                    if (info.getHeadway().ge(gtu.getLength().neg()) || lane.isCurrent())
                    {
                        leadersSet.add(gtu.moved(info.getHeadway(), info.getSpeed(), info.getAcceleration()));
                    }
                    else
                    {
                        followersSet.add(gtu.moved(info.getHeadway().plus(length).plus(gtu.getLength()).neg(), info.getSpeed(),
                                info.getAcceleration()));
                    }
                }

            }

        }
        catch (PerceptionException | GTUException exception)
        {
            // lane change performed, info on a lane not present
        }

        try
        {
            // add empty sets on all lanes in the current cross section that are not considered yet
            for (RelativeLane lane : getPerception().getLaneStructure().getExtendedCrossSection())
            {
                if (!this.followers.containsKey(lane))
                {
                    this.followers.put(lane, new SortedSetPerceptionIterable<>((OTSRoadNetwork) getPerception().getGtu()
                            .getReferencePosition().getLane().getParentLink().getNetwork()));
                }
                if (!this.leaders.containsKey(lane))
                {
                    this.leaders.put(lane, new SortedSetPerceptionIterable<>((OTSRoadNetwork) getPerception().getGtu()
                            .getReferencePosition().getLane().getParentLink().getNetwork()));
                }
                if (lane.isLeft() || lane.isRight())
                {
                    if (!this.firstFollowers.containsKey(lane.getLateralDirectionality()))
                    {
                        this.firstFollowers.put(lane.getLateralDirectionality(), new TreeSet<>());
                    }
                    if (!this.firstLeaders.containsKey(lane.getLateralDirectionality()))
                    {
                        this.firstLeaders.put(lane.getLateralDirectionality(), new TreeSet<>());
                    }
                    if (!this.gtuAlongside.containsKey(lane.getLateralDirectionality()))
                    {
                        this.gtuAlongside.put(lane.getLateralDirectionality(), false);
                    }
                }
            }
        }
        catch (ParameterException | GTUException pe)
        {
            //
        }

    }

    /**
     * Returns a standard Gaussian distributed random value generated with a Wiener process.
     * @param gtuId String; gtu id of neighbor
     * @param tau Duration; error correlation parameter
     * @param dt Duration; model time step
     * @return standard Gaussian distributed random value generated with a Wiener process
     */
    private double getError(final String gtuId, final Duration tau, final Duration dt)
    {
        Time now;
        try
        {
            now = getTimestamp();
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("Could not get time stamp.", exception);
        }

        double err;
        ErrorValue errorValue;
        if (!this.errors.containsKey(gtuId))
        {
            err = this.norm.draw();
            errorValue = new ErrorValue();
            this.errors.put(gtuId, errorValue);
        }
        else
        {
            errorValue = this.errors.get(gtuId);
            if (errorValue.getTime().eq(now))
            {
                return errorValue.getError();
            }
            double dtErr = now.si - errorValue.getTime().si;
            if (dtErr <= dt.si + MARGIN)
            {
                err = Math.exp(-dtErr / tau.si) * errorValue.getError() + Math.sqrt((2 * dtErr) / tau.si) * this.norm.draw();
            }
            else
            {
                // too long ago, exp may result in extreme values, draw new independent value
                err = this.norm.draw();
            }
        }
        errorValue.set(now, err);
        return err;

    }

    /**
     * Creates the initial erroneous values for distance, speed and acceleration.
     * @param distance Length; actual distance
     * @param speed Speed; actual speed
     * @param acceleration Acceleration; actual acceleration
     * @param error double; random error
     * @param distanceError double; error factor on distance
     * @param speedError double; error factor on speed
     * @param accelerationError double; error factor on acceleration
     * @param egoSpeed Speed; own speed
     * @return erroneous triplet
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private NeighborTriplet erroneousTriplet(final Length distance, final Speed speed, final Acceleration acceleration,
            final double error, final double distanceError, final double speedError, final double accelerationError,
            final Speed egoSpeed)
    {
        Length s = Length.instantiateSI(distance.si * (1 + ((distance.ge0() ? error : -error) * distanceError)));
        Speed v = Speed.instantiateSI(speed.si + (error * speedError * distance.si));
        if (v.lt0())
        {
            v = Speed.ZERO;
        }
        Acceleration a = Acceleration.instantiateSI(acceleration.si * (1 + error * accelerationError));
        return new NeighborTriplet(s, v, a);
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGTU> getFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        rearrangeNeighbors();
        return this.firstLeaders.get(lat);
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGTU> getFirstFollowers(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        rearrangeNeighbors();
        return this.firstFollowers.get(lat);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isGtuAlongside(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        if (isGtuAlongsideOverride(lat))
        {
            return true;
        }
        rearrangeNeighbors();
        if (this.gtuAlongside.containsKey(lat))
        {
            return this.gtuAlongside.get(lat);
        }
        // If the lane was not perceived at the reaction time in the past, but there is a lane now, be on the safe side.
        // Note that infrastructure perception is separate, i.e. might be with a different or no reaction time.
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getLeaders(final RelativeLane lane)
    {
        rearrangeNeighbors();
        return this.leaders.get(lane);
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getFollowers(final RelativeLane lane)
    {
        rearrangeNeighbors();
        return this.followers.get(lane);
    }

    /**
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 mrt. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class ErrorValue
    {

        /** Time. */
        private Time time;

        /** Error. */
        private double error;

        /**
         * 
         */
        ErrorValue()
        {
        }

        /**
         * @return time.
         */
        public Time getTime()
        {
            return this.time;
        }

        /**
         * @return error.
         */
        public double getError()
        {
            return this.error;
        }

        /**
         * @param t Time; time
         * @param err double; error
         */
        public void set(final Time t, final double err)
        {
            this.time = t;
            this.error = err;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "ErrorValue [time=" + this.time + ", error=" + this.error + "]";
        }

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DelayedNeighborsPerception [anticipation=" + this.anticipation + ", rearrangeTime=" + this.rearrangeTime
                + ", followers=" + this.followers + ", leaders=" + this.leaders + ", firstFollowers=" + this.firstFollowers
                + ", firstLeaders=" + this.firstLeaders + ", gtuAlongside=" + this.gtuAlongside + ", errors=" + this.errors
                + ", norm=" + this.norm + "]";
    }

}
