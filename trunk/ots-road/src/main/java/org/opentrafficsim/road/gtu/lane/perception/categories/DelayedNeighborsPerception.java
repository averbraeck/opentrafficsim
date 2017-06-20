package org.opentrafficsim.road.gtu.lane.perception.categories;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;
import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVEZERO;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDuration;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.perception.PerceptionException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.language.Throw;

/**
 * Implementation of delayed neighbors perception which anticipates using constant speed.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DelayedNeighborsPerception extends AbstractDelayedNeighborsPerception
{

    /** Parameter for anticipating beyond current time. */
    public static final ParameterTypeDuration TA =
            new ParameterTypeDuration("ta", "anticipation time in future", Duration.ZERO, POSITIVEZERO);

    /** Parameter for correlation in errors. */
    public static final ParameterTypeDuration TAUE =
            new ParameterTypeDuration("tau_e", "error correlation time", Duration.createSI(20), POSITIVE);

    /** Parameter for distance error factor. */
    public static final ParameterTypeDouble SERROR =
            new ParameterTypeDouble("s_error", "distance error factor", 0.1, POSITIVEZERO);

    /** Parameter for speed error factor. */
    public static final ParameterTypeDouble VERROR =
            new ParameterTypeDouble("v_error", "speed error factor", 0.1, POSITIVEZERO);

    /** Parameter for acceleration error factor. */
    public static final ParameterTypeDouble AERROR =
            new ParameterTypeDouble("a_error", "acceleration error factor", 0.2, POSITIVEZERO);

    /** Margin to check time step in Wiener process. */
    private static final double MARGIN = 1e-6;

    /** */
    private static final long serialVersionUID = 20170217L;

    /** Form of anticipation. */
    private final Anticipation anticipation;

    /** Latest update time of neighbor rearrangement. */
    private Time rearrangeTime;

    /** Set of followers per relative lane. */
    private final Map<RelativeLane, SortedSet<HeadwayGTU>> followers = new HashMap<>();

    /** Set of leaders per relative lane. */
    private final Map<RelativeLane, SortedSet<HeadwayGTU>> leaders = new HashMap<>();

    /** Set of first followers per lane upstream of merge per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, SortedSet<HeadwayGTU>> firstFollowers = new HashMap<>();

    /** Set of first leaders per lane downstream of split per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, SortedSet<HeadwayGTU>> firstLeaders = new HashMap<>();

    /** Whether a GTU is alongside per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, Boolean> gtuAlongside = new HashMap<>();

    /** Map of errors by a Wiener process for each GTU. */
    private HashMap<String, ErrorValue> errors = new HashMap<>();

    /** Random numbers for perception errors. */
    private final DistNormal norm;

    /**
     * @param perception perception
     * @param anticipation anticipation
     */
    public DelayedNeighborsPerception(final LanePerception perception, final Anticipation anticipation)
    {
        super(perception);
        Throw.whenNull(anticipation, "Anticipation may not be null.");
        this.anticipation = anticipation;
        try
        {
            this.norm = new DistNormal(perception.getGtu().getSimulator().getReplication().getStream("perception"));
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("GTU not initialized.", exception);
        }
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
            time = getPerception().getGtu().getSimulator().getSimulatorTime().getTime();
            if (time.equals(this.rearrangeTime))
            {
                return;
            }
            BehavioralCharacteristics bc = getPerception().getGtu().getBehavioralCharacteristics();
            ta = bc.getParameter(TA);
            taue = bc.getParameter(TAUE);
            distanceError = bc.getParameter(SERROR);
            speedError = bc.getParameter(VERROR);
            accelerationError = bc.getParameter(AERROR);
            length = getPerception().getGtu().getLength();
            egoSpeed = getPerception().getPerceptionCategory(EgoPerception.class).getSpeed();
            dt = bc.getParameter(DT);
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
                Acceleration acceleration = getPerception().getPerceptionCategory(EgoPerception.class).getAcceleration();
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
        SortedSet<RelativeLane> crossSection = getDelayedCrossSection();
        for (RelativeLane lane : crossSection)
        {

            try
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
                                speedError, accelerationError, egoSpeed), d, traveledDistance);
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
                                accelerationError, egoSpeed), d, traveledDistance);
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
                SortedSet<HeadwayGTU> followersSet = new TreeSet<>();
                this.followers.put(lane, followersSet);
                SortedSet<HeadwayGTU> leadersSet = new TreeSet<>();
                this.leaders.put(lane, leadersSet);

                // followers
                TimeStampedObject<SortedSet<HeadwayGTU>> delayedFollowers =
                        getInfo(NeighborsInfoType.getSortedSetType(FOLLOWERS), lane);
                Duration d = time.minus(delayedFollowers.getTimestamp()).plus(ta);
                for (HeadwayGTU gtu : delayedFollowers.getObject())
                {
                    NeighborTriplet info = this.anticipation.anticipate(
                            erroneousTriplet(gtu.getDistance().neg(), gtu.getSpeed(), gtu.getAcceleration(),
                                    getError(gtu.getId(), taue, dt), distanceError, speedError, accelerationError, egoSpeed),
                            d, traveledDistance);
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
                TimeStampedObject<SortedSet<HeadwayGTU>> delayedLeaders =
                        getInfo(NeighborsInfoType.getSortedSetType(LEADERS), lane);
                d = time.minus(delayedLeaders.getTimestamp()).plus(ta);
                for (HeadwayGTU gtu : delayedLeaders.getObject())
                {

                    NeighborTriplet info = this.anticipation.anticipate(
                            erroneousTriplet(gtu.getDistance(), gtu.getSpeed(), gtu.getAcceleration(),
                                    getError(gtu.getId(), taue, dt), distanceError, speedError, accelerationError, egoSpeed),
                            d, traveledDistance);
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
            catch (@SuppressWarnings("unused") PerceptionException exception)
            {
                // lane change performed, info on a lane not present
            }

        }

    }

    /**
     * Returns a standard Gaussian distributed random value generated with a Wiener process.
     * @param gtuId gtu id of neighbor
     * @param tau error correlation parameter
     * @param dt model time step
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
     * @param distance actual distance
     * @param speed actual speed
     * @param acceleration actual acceleration
     * @param error random error
     * @param distanceError error factor on distance
     * @param speedError error factor on speed
     * @param accelerationError error factor on acceleration
     * @param egoSpeed own speed
     * @return erroneous triplet
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private NeighborTriplet erroneousTriplet(final Length distance, final Speed speed, final Acceleration acceleration,
            final double error, final double distanceError, final double speedError, final double accelerationError,
            final Speed egoSpeed)
    {
        Length s = Length.createSI(distance.si * (1 + ((distance.ge0() ? error : -error) * distanceError)));
        Speed v = Speed.createSI(speed.si + (error * speedError * distance.si));
        if (v.lt0())
        {
            v = Speed.ZERO;
        }
        Acceleration a = Acceleration.createSI(acceleration.si * (1 + error * accelerationError));
        return new NeighborTriplet(s, v, a);
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayGTU> getFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        rearrangeNeighbors();
        return this.firstLeaders.get(lat);
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayGTU> getFirstFollowers(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        rearrangeNeighbors();
        return this.firstFollowers.get(lat);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isGtuAlongside(final LateralDirectionality lat)
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
    public SortedSet<HeadwayGTU> getLeaders(final RelativeLane lane)
    {
        rearrangeNeighbors();
        return this.leaders.get(lane);
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayGTU> getFollowers(final RelativeLane lane)
    {
        rearrangeNeighbors();
        return this.followers.get(lane);
    }

    /**
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
         * @param t time
         * @param err error
         */
        public void set(final Time t, final double err)
        {
            this.time = t;
            this.error = err;
        }

    }

    /**
     * Form of anticipation.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 feb. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum Anticipation
    {

        /** Assume no anticipation. */
        NONE
        {
            @Override
            public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                    final Length traveledDistance)
            {
                return neighborTriplet;
            }

            @Override
            public Length egoAnticipation(final Speed speed, final Acceleration acceleration, final Duration duration)
            {
                return Length.ZERO;
            }
        },

        /** Assume constant speed. */
        CONSTANT_SPEED
        {
            @Override
            public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                    final Length traveledDistance)
            {
                return new NeighborTriplet(neighborTriplet.getHeadway().plus(neighborTriplet.getSpeed().multiplyBy(duration))
                        .minus(traveledDistance), neighborTriplet.getSpeed(), neighborTriplet.getAcceleration());
            }

            @Override
            public Length egoAnticipation(final Speed speed, final Acceleration acceleration, final Duration duration)
            {
                return speed.multiplyBy(duration);
            }
        },

        /** Assume constant acceleration. */
        CONSTANT_ACCELERATION
        {
            @Override
            public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                    final Length traveledDistance)
            {
                if (neighborTriplet.getSpeed().si < -neighborTriplet.getAcceleration().si * duration.si)
                {
                    // to stand still
                    double t = neighborTriplet.getSpeed().si / -neighborTriplet.getAcceleration().si;
                    double dx = neighborTriplet.getSpeed().si * t + .5 * neighborTriplet.getAcceleration().si * t * t;
                    return new NeighborTriplet(Length.createSI(neighborTriplet.getHeadway().si + dx - traveledDistance.si),
                            Speed.ZERO, Acceleration.ZERO);
                }
                double dx = neighborTriplet.getSpeed().si * duration.si
                        + .5 * neighborTriplet.getAcceleration().si * duration.si * duration.si;
                double dv = neighborTriplet.getAcceleration().si * duration.si;
                return new NeighborTriplet(Length.createSI(neighborTriplet.getHeadway().si + dx - traveledDistance.si),
                        Speed.createSI(neighborTriplet.getSpeed().si + dv), neighborTriplet.getAcceleration());
            }

            @Override
            public Length egoAnticipation(final Speed speed, final Acceleration acceleration, final Duration duration)
            {
                return speed.multiplyBy(duration);
            }
        };

        /**
         * Anticipate movement.
         * @param neighborTriplet headway, speed and acceleration
         * @param duration duration
         * @param traveledDistance distance the subject vehicle traveled during the anticipation time
         * @return anticipated info
         */
        public abstract NeighborTriplet anticipate(NeighborTriplet neighborTriplet, Duration duration, Length traveledDistance);

        /**
         * Anticipate own movement.
         * @param speed current speed
         * @param acceleration current acceleration
         * @param duration anticipation time
         * @return anticipated distance traveled
         */
        public abstract Length egoAnticipation(Speed speed, Acceleration acceleration, Duration duration);

    }

    /**
     * Results from anticipation.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 feb. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class NeighborTriplet
    {

        /** Headway. */
        private final Length headway;

        /** Speed. */
        private final Speed speed;

        /** Acceleration. */
        private final Acceleration acceleration;

        /**
         * @param headway headway
         * @param speed speed
         * @param acceleration acceleration
         */
        NeighborTriplet(final Length headway, final Speed speed, final Acceleration acceleration)
        {
            this.headway = headway;
            this.speed = speed;
            this.acceleration = acceleration;
        }

        /**
         * @return headway.
         */
        public Length getHeadway()
        {
            return this.headway;
        }

        /**
         * @return speed.
         */
        public Speed getSpeed()
        {
            return this.speed;
        }

        /**
         * @return acceleration.
         */
        public Acceleration getAcceleration()
        {
            return this.acceleration;
        }

    }
}
