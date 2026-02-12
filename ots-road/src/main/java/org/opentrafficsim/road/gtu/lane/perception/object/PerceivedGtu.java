package org.opentrafficsim.road.gtu.lane.perception.object;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.GtuTypeAssumptions;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContext;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Interface for perceived surrounding GTU's, adding signals, maneuver and behavioral information.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface PerceivedGtu extends PerceivedObject, TacticalContext
{

    /**
     * Returns the GTU type.
     * @return gtuType
     */
    GtuType getGtuType();

    /**
     * Returns information on the signals. This includes indicators and braking lights.
     * @return information on the signals
     */
    Signals getSignals();

    /**
     * Returns information on the maneuver. This includes lane changing and lateral deviation.
     * @return information on the maneuver
     */
    Maneuver getManeuver();

    /**
     * Returns information on the behavior. This includes the car-following model, parameters, desired speed, route, lane change
     * desire and social pressure.
     * @return information on the behavior
     */
    Behavior getBehavior();

    // getSpeed() and getAcceleration() implemented to solve duplicate methods in PerceivedObject and TacticalContext

    @Override
    default Speed getSpeed()
    {
        return getKinematics().getSpeed();
    }

    @Override
    default Acceleration getAcceleration()
    {
        return getKinematics().getAcceleration();
    }

    // getParameters(), getCarFollowingModel() and getSpeedLimitInfo() implemented as TacticalContext requires it at this level

    @Override
    default Parameters getParameters()
    {
        return getBehavior().getParameters();
    }

    @Override
    default CarFollowingModel getCarFollowingModel()
    {
        return getBehavior().getCarFollowingModel();
    }

    @Override
    default SpeedLimitInfo getSpeedLimitInfo()
    {
        return getBehavior().getSpeedLimitInfo();
    }

    /**
     * Signal information.
     */
    interface Signals
    {
        /** Instance with no signals. */
        Signals NONE = new Record(TurnIndicatorStatus.NONE, false);

        /**
         * Returns indicator status.
         * @param lat direction of indicator.
         * @return indicator status
         * @throws IllegalArgumentException when the direction is not LEFT or RIGHT
         */
        default boolean isIndicatorOn(final LateralDirectionality lat)
        {
            Throw.when(lat == null || lat.equals(LateralDirectionality.NONE), IllegalArgumentException.class,
                    "Lateral direction should be LEFT or RIGHT.");
            return lat.isLeft() ? getTurnIndicatorStatus().isLeft() : getTurnIndicatorStatus().isRight();
        }

        /**
         * Returns the indicator status.
         * @return the indicator status
         */
        TurnIndicatorStatus getTurnIndicatorStatus();

        /**
         * Returns whether the braking lights are on.
         * @return whether the braking lights are on
         */
        boolean isBrakingLightsOn();

        /**
         * Wraps a GTU and returns its signals.
         * @param gtu GTU
         * @return signals view of the GTU
         */
        static Signals of(final LaneBasedGtu gtu)
        {
            return of(gtu, gtu.getSimulator().getSimulatorTime());
        }

        /**
         * Wraps a GTU and returns its signals.
         * @param gtu GTU
         * @param time simulation time of the signals
         * @return signals view of the GTU
         */
        static Signals of(final LaneBasedGtu gtu, final Duration time)
        {
            return new Signals()
            {
                @Override
                public TurnIndicatorStatus getTurnIndicatorStatus()
                {
                    return gtu.getTurnIndicatorStatus(time);
                }

                @Override
                public boolean isBrakingLightsOn()
                {
                    return gtu.isBrakingLightsOn(time);
                }
            };
        }

        /**
         * Record storing signals information.
         * @param getTurnIndicatorStatus the indicator status
         * @param isBrakingLightsOn whether the braking lights are on
         */
        record Record(TurnIndicatorStatus getTurnIndicatorStatus, boolean isBrakingLightsOn) implements Signals
        {

            /**
             * Constructor.
             * @param getTurnIndicatorStatus the indicator status
             * @param isBrakingLightsOn whether the braking lights are on
             * @throws NullPointerException when getTurnIndicatorStatus is {@code null}
             */
            public Record
            {
                Throw.whenNull(getTurnIndicatorStatus, "getTurnIndicatorStatus");
            }

        };
    }

    /**
     * Information on the maneuver.
     */
    interface Maneuver
    {
        /** Instance with no signals. */
        Maneuver NONE = new Record(false, false, Length.ZERO);

        /**
         * Returns whether the GTU is changing either left or right.
         * @param lat lateral lane change direction
         * @return whether the GTU is changing either left or right
         * @throws IllegalArgumentException when the direction is not LEFT or RIGHT
         */
        default boolean isChangingLane(final LateralDirectionality lat)
        {
            Throw.when(lat == null || lat.equals(LateralDirectionality.NONE), IllegalArgumentException.class,
                    "Lateral direction should be LEFT or RIGHT.");
            return lat.isLeft() ? isChangingLeft() : isChangingRight();
        }

        /**
         * Returns whether the GTU is changing lanes to the left.
         * @return whether the GTU is changing lanes to the left
         */
        boolean isChangingLeft();

        /**
         * Returns whether the GTU is changing lanes to the right.
         * @return whether the GTU is changing lanes to the right
         */
        boolean isChangingRight();

        /**
         * Returns the lateral deviation from the lane center line. Positive values are left, negative values are right.
         * @return lateral deviation from the lane center line
         */
        Length getDeviation();

        /**
         * Wraps a GTU and returns its signals.
         * @param gtu GTU
         * @return signals view of the GTU
         */
        static Maneuver of(final LaneBasedGtu gtu)
        {
            return of(gtu, gtu.getSimulator().getSimulatorTime());
        }

        /**
         * Wraps a GTU and returns its maneuver at given time.
         * @param gtu GTU
         * @param time time of the maneuver
         * @return maneuver view of the GTU
         */
        static Maneuver of(final LaneBasedGtu gtu, final Duration time)
        {
            Throw.whenNull(gtu, "gtu");
            Throw.whenNull(time, "time");
            return new Maneuver()
            {
                @Override
                public boolean isChangingLeft()
                {
                    return gtu.getLaneChangeDirection(time).isLeft();
                }

                @Override
                public boolean isChangingRight()
                {
                    return gtu.getLaneChangeDirection(time).isRight();
                }

                @Override
                public Length getDeviation()
                {
                    return gtu.getDeviation(time);
                }
            };
        }

        /**
         * Record storing signals information.
         * @param isChangingLeft whether the GTU is changing lanes to the left
         * @param isChangingRight whether the GTU is changing lanes to the right
         * @param getDeviation lateral deviation from the lane center line
         */
        record Record(boolean isChangingLeft, boolean isChangingRight, Length getDeviation) implements Maneuver
        {

            /**
             * Constructor.
             * @param isChangingLeft whether the GTU is changing lanes to the left
             * @param isChangingRight whether the GTU is changing lanes to the right
             * @param getDeviation lateral deviation from the lane center line
             * @throws IllegalArgumentException when both {@code isChangingLeft} and {@code isChangingRight} are true
             * @throws NullPointerException when {@code getDeviation} is {@code null}
             */
            public Record
            {
                Throw.when(isChangingLeft && isChangingRight, IllegalArgumentException.class,
                        "Both isChangingLeft and isChangingRight are true.");
                Throw.whenNull(getDeviation, "getDeviation");
            }
        };
    }

    /**
     * Information on the behavior.
     */
    interface Behavior
    {
        /**
         * Many models that observe a GTU need to predict the imminent behavior of that GTU. Having a car following model of the
         * observed GTU can help with that. The car following model that is returned can be on a continuum between the actual
         * car following model of the observed GTU and the own car following model of the observing GTU, not making any
         * assumptions about the observed GTU. When successive observations of the GTU take place, parameters about its behavior
         * can be estimated more accurately. Another interesting easy-to-implement solution is to return a car following model
         * per GTU type, where the following model of a truck can differ from that of a car.
         * @return a car following model that represents the expected behavior of the observed GTU
         */
        CarFollowingModel getCarFollowingModel();

        /**
         * Many models that observe a GTU need to predict the imminent behavior of that GTU. Having an estimate of the
         * behavioral characteristics of the observed GTU can help with that. The parameters that are returned can be on a
         * continuum between the actual parameters of the observed GTU and the own parameters of the observing GTU, not making
         * any assumptions about the observed GTU. When successive observations of the GTU take place, parameters about its
         * behavior can be estimated more accurately. Another interesting easy-to-implement solution is to return a set of
         * parameters per GTU type, where the parameters of a truck can differ from that of a car.
         * @return the parameters that represent the expected behavior of the observed GTU, in case of exact values a safe copy
         *         is returned
         */
        Parameters getParameters();

        /**
         * Many models that observe a GTU need to predict the imminent behavior of that GTU. Having a model of the speed info
         * profile for the observed GTU can help with predicting its future behavior. The speed limit info that is returned can
         * be on a continuum between the actual speed limit model of the observed GTU and the own speed limit model of the
         * observing GTU, not making any assumptions about the observed GTU. When successive observations of the GTU take place,
         * parameters about its behavior, such as the maximum speed it accepts, can be estimated more accurately. Another
         * interesting easy-to-implement solution is to return a speed limit info object per GTU type, where the returned
         * information of a truck -- with a maximum allowed speed on 80 km/h -- can differ from that of a car -- which can have
         * a maximum allowed speed of 100 km/h on the same road.
         * @return a speed limit model that helps in determining the expected behavior of the observed GTU
         */
        SpeedLimitInfo getSpeedLimitInfo();

        /**
         * Returns the perceived desired speed of the neighbor.
         * @return perceived desired speed of the neighbor
         */
        Speed getDesiredSpeed();

        /**
         * Models responding to other GTU may assume a route of the vehicle, for instance at intersections. The route may be
         * short, i.e. only over the next intersection. Implementations may return anything from the actual route, a route based
         * on indicators and other assumptions, or empty if simply not known/estimated.
         * @return route of GTU, empty if there is no route
         */
        Optional<Route> getRoute();

        /**
         * Returns the perceived left lane change desire, a value between -1 and 1.
         * @return the perceived left lane change desire, a value between -1 and 1
         */
        double leftLaneChangeDesire();

        /**
         * Returns the perceived right lane change desire, a value between -1 and 1.
         * @return the perceived right lane change desire, a value between -1 and 1
         */
        double rightLaneChangeDesire();

        /**
         * Returns the perceived social pressure, a value between 0 and 1.
         * @return the perceived social pressure, a value between 0 and 1
         */
        double socialPressure();

        /**
         * Wraps a GTU and returns its behavior. The given time only applies to the parameters, lane change desire and social
         * pressure.
         * @param gtu GTU
         * @return behavior view of the GTU
         * @throws NullPointerException when GTU is {@code null}
         */
        static Behavior of(final LaneBasedGtu gtu)
        {
            Throw.whenNull(gtu, "gtu");
            return of0(gtu, gtu.getSimulator().getSimulatorTime(), null);
        }

        /**
         * Wraps a GTU and returns its behavior. The given time only applies to lane change desire and social pressure.
         * @param gtu GTU
         * @param gtuTypeAssumptions assumptions on the GTU type
         * @return behavior view of the GTU
         * @throws NullPointerException when any input argument is {@code null}
         */
        static Behavior of(final LaneBasedGtu gtu, final GtuTypeAssumptions gtuTypeAssumptions)
        {
            Throw.whenNull(gtu, "gtu");
            Throw.whenNull(gtuTypeAssumptions, "gtuTypeAssumptions");
            return of0(gtu, gtu.getSimulator().getSimulatorTime(), gtuTypeAssumptions);
        }

        /**
         * Wraps a GTU and returns its behavior. The given time only applies to the parameters, lane change desire and social
         * pressure.
         * @param gtu GTU
         * @param time simulation time of the behavior
         * @return behavior view of the GTU
         * @throws NullPointerException when any input argument is {@code null}
         */
        static Behavior of(final LaneBasedGtu gtu, final Duration time)
        {
            return of0(gtu, time, null);
        }

        /**
         * Wraps a GTU and returns its behavior. The given time only applies to the lane change desire and social pressure.
         * @param gtu GTU
         * @param time simulation time of the behavior
         * @param gtuTypeAssumptions assumptions on the GTU type
         * @return behavior view of the GTU
         * @throws NullPointerException when any input argument is {@code null}
         */
        static Behavior of(final LaneBasedGtu gtu, final Duration time, final GtuTypeAssumptions gtuTypeAssumptions)
        {
            Throw.whenNull(gtuTypeAssumptions, "gtuTypeAssumptions");
            return of0(gtu, time, gtuTypeAssumptions);
        }

        /**
         * Wraps a GTU and returns its behavior. The given time only applies to the parameters, lane change desire and social
         * pressure.
         * @param gtu GTU
         * @param time simulation time of the behavior
         * @param gtuTypeAssumptions assumptions on the GTU type, can be {@code null}
         * @return behavior view of the GTU
         */
        private static Behavior of0(final LaneBasedGtu gtu, final Duration time, final GtuTypeAssumptions gtuTypeAssumptions)
        {
            Throw.whenNull(gtu, "gtu");
            Throw.whenNull(time, "time");
            // parameters are not historical, they could be, but that's really slow
            Parameters parameters = new ParameterSet(gtu.getParameters());
            CarFollowingModel carFollowingModel = gtu.getTacticalPlanner().getCarFollowingModel();
            return new Behavior()
            {
                /** Speed limit info. */
                private SpeedLimitInfo speedLimitInfo;

                /** Desired speed. */
                private Speed desiredSpeed;

                @Override
                public CarFollowingModel getCarFollowingModel()
                {
                    return gtuTypeAssumptions == null ? carFollowingModel
                            : gtuTypeAssumptions.getCarFollowingModel(gtu.getType());
                }

                @Override
                public Parameters getParameters()
                {
                    return gtuTypeAssumptions == null ? parameters : gtuTypeAssumptions.getParameters(gtu.getType());
                }

                @Override
                public SpeedLimitInfo getSpeedLimitInfo()
                {
                    if (this.speedLimitInfo == null)
                    {
                        this.speedLimitInfo = new SpeedLimitInfo();
                        this.speedLimitInfo.addSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED, gtu.getMaximumSpeed());
                        this.speedLimitInfo.addSpeedInfo(SpeedLimitTypes.FIXED_SIGN,
                                gtuTypeAssumptions == null
                                        ? Try.assign(() -> gtu.getLane().getSpeedLimit(gtu.getType()),
                                                "Unable to obtain speed limit for GTU on lane where it is at.")
                                        : gtuTypeAssumptions.getLaneTypeMaxSpeed(gtu.getType(), gtu.getLane().getType()));
                    }
                    return this.speedLimitInfo;
                }

                @Override
                public Speed getDesiredSpeed()
                {
                    if (this.desiredSpeed == null)
                    {
                        try
                        {
                            this.desiredSpeed = getCarFollowingModel().desiredSpeed(getParameters(), getSpeedLimitInfo());
                        }
                        catch (ParameterException ex)
                        {
                            this.desiredSpeed = Try.assign(() -> gtu.getLane().getSpeedLimit(gtu.getType()),
                                    "Unable to obtain speed limit for GTU on lane where it is at.");
                        }
                    }
                    return this.desiredSpeed;
                }

                @Override
                public Optional<Route> getRoute()
                {
                    return gtu.getStrategicalPlanner().getRoute();
                }

                @Override
                public double leftLaneChangeDesire()
                {
                    return parameters.getOptionalParameter(LmrsParameters.DLEFT).orElse(0.0);
                }

                @Override
                public double rightLaneChangeDesire()
                {
                    return parameters.getOptionalParameter(LmrsParameters.DRIGHT).orElse(0.0);
                }

                @Override
                public double socialPressure()
                {
                    return parameters.getOptionalParameter(LmrsParameters.SOCIO).orElse(0.0);
                }
            };
        }
    }

    /**
     * Returns a view of this perceived headway GTU that returns different values for the headway, speed and acceleration.
     * @param headway headway
     * @param speed speed
     * @param acceleration acceleration
     * @return copy with different headway, speed and acceleration
     * @throws NullPointerException when any input argument is {@code null}
     * @throws IllegalStateException when this perceived GTU is parallel
     */
    default PerceivedGtu moved(final Length headway, final Speed speed, final Acceleration acceleration)
    {
        Throw.whenNull(headway, "headyway");
        Throw.whenNull(speed, "speed");
        Throw.whenNull(acceleration, "acceleration");
        Throw.when(getKinematics().getOverlap().isParallel(), IllegalStateException.class,
                "GTU {} is moved in perception, but it is parallel.", getId());
        return new PerceivedGtu()
        {
            @Override
            public ObjectType getObjectType()
            {
                return PerceivedGtu.this.getObjectType();
            }

            @Override
            public Length getLength()
            {
                return PerceivedGtu.this.getLength();
            }

            @Override
            public Kinematics getKinematics()
            {
                return new Kinematics()
                {
                    @Override
                    public Speed getSpeed()
                    {
                        return speed;
                    }

                    @Override
                    public Length getDistance()
                    {
                        return headway;
                    }

                    @Override
                    public Acceleration getAcceleration()
                    {
                        return acceleration;
                    }

                    @Override
                    public boolean isFacingSameDirection()
                    {
                        return PerceivedGtu.this.getKinematics().isFacingSameDirection();
                    }

                    @Override
                    public Overlap getOverlap()
                    {
                        return PerceivedGtu.this.getKinematics().getOverlap();
                    }
                };
            }

            @Override
            public String getId()
            {
                return PerceivedGtu.this.getId();
            }

            @Override
            public GtuType getGtuType()
            {
                return PerceivedGtu.this.getGtuType();
            }

            @Override
            public Length getWidth()
            {
                return PerceivedGtu.this.getWidth();
            }

            @Override
            public Signals getSignals()
            {
                return PerceivedGtu.this.getSignals();
            }

            @Override
            public Maneuver getManeuver()
            {
                return PerceivedGtu.this.getManeuver();
            }

            @Override
            public Behavior getBehavior()
            {
                return PerceivedGtu.this.getBehavior();
            }
        };
    }

    /**
     * Returns perceived GTU with given kinematics.
     * @param gtu GTU that is perceived
     * @param kinematics kinematics for the vehicle
     * @return perceived view of the GTU
     * @throws NullPointerException when {@code gtu} is null
     */
    static PerceivedGtu of(final LaneBasedGtu gtu, final Kinematics kinematics)
    {
        Throw.whenNull(gtu, "gtu");
        return new PerceivedGtuBase(gtu.getId(), gtu.getType(), gtu.getLength(), gtu.getWidth(), kinematics, Signals.of(gtu),
                Maneuver.of(gtu), Behavior.of(gtu));
    }

    /**
     * Returns perceived GTU at the given time with given kinematics.
     * @param gtu GTU that is perceived
     * @param kinematics kinematics for the vehicle
     * @param time simulation time at which the GTU is perceived
     * @return perceived view of the GTU
     * @throws NullPointerException when {@code gtu} is null
     */
    static PerceivedGtu of(final LaneBasedGtu gtu, final Kinematics kinematics, final Duration time)
    {
        Throw.whenNull(gtu, "gtu");
        return new PerceivedGtuBase(gtu.getId(), gtu.getType(), gtu.getLength(), gtu.getWidth(), kinematics,
                Signals.of(gtu, time), Maneuver.of(gtu, time), Behavior.of(gtu, time));
    }

}
