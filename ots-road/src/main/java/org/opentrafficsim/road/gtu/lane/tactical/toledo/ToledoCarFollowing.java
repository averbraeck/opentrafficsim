package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractCarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ToledoCarFollowing extends AbstractCarFollowingModel
{

    /** */
    public static final ParameterTypeSpeed CDS = new ParameterTypeSpeed("C_DS", "Constant in desired speed.",
            new Speed(17.636, SpeedUnit.SI), ConstraintInterface.POSITIVE);

    /** */
    public static final ParameterTypeSpeed BETADS = new ParameterTypeSpeed("BETA_DS", "Reduction of desired speed for trucks.",
            new Speed(-1.458, SpeedUnit.SI), ConstraintInterface.NEGATIVE);

    /** */
    public static final ParameterTypeSpeed ALPHADS =
            new ParameterTypeSpeed("ALPHA_DS", "Factor on error term of desired speed.", new Speed(-0.105, SpeedUnit.SI));

    /** */
    public static final ParameterTypeDuration HSTAR =
            new ParameterTypeDuration("h*", "Desired time headway.", new Duration(2.579, DurationUnit.SI));

    /** */
    public static final ParameterTypeDouble LAMBDAFF =
            new ParameterTypeDouble("Lambda_ff", "Free flow acceleration sensitivity.", 0.0881, ConstraintInterface.POSITIVE);

    /** */
    public static final ParameterTypeDouble SIGMAFF =
            new ParameterTypeDouble("Sigma_ff", "Free flow acceleration standard deviation.", Math.exp(0.169));

    /** */
    public static final ParameterTypeDouble CCFACC = new ParameterTypeDouble("C_CF_ACC",
            "Constant for car following acceleration.", 0.0355, ConstraintInterface.POSITIVE);

    /** */
    public static final ParameterTypeDouble BETAACC =
            new ParameterTypeDouble("BETA_ACC", "Power on speed for acceleration.", 0.291, ConstraintInterface.POSITIVE);

    /** */
    public static final ParameterTypeDouble GAMMAACC = new ParameterTypeDouble("GAMMA_ACC",
            "Power on distance headway for acceleration.", -0.166, ConstraintInterface.NEGATIVE);

    /** */
    public static final ParameterTypeDouble RHOACC =
            new ParameterTypeDouble("RHO_ACC", "Power on density for acceleration.", 0.550, ConstraintInterface.POSITIVE);

    /** */
    public static final ParameterTypeDouble LAMBDAACC = new ParameterTypeDouble("LAMBDA_ACC",
            "Power on speed difference for acceleration.", 0.520, ConstraintInterface.POSITIVE);

    /** */
    public static final ParameterTypeDouble SIGMAACC =
            new ParameterTypeDouble("Sigma_acc", "Car-following acceleration standard deviation.", Math.exp(0.126));

    /** */
    public static final ParameterTypeDouble CCFDEC = new ParameterTypeDouble("C_CF_DEC",
            "Constant for car following deceleration.", -0.860, ConstraintInterface.NEGATIVE);

    /** */
    public static final ParameterTypeDouble GAMMADEC = new ParameterTypeDouble("GAMMA_DEC",
            "Power on distance headway for deceleration.", -0.565, ConstraintInterface.NEGATIVE);

    /** */
    public static final ParameterTypeDouble RHODEC =
            new ParameterTypeDouble("RHO_DEC", "Power on density for deceleration.", 0.143, ConstraintInterface.POSITIVE);

    /** */
    public static final ParameterTypeDouble LAMBDADEC = new ParameterTypeDouble("LAMBDA_DEC",
            "Power on speed difference for deceleration.", 0.834, ConstraintInterface.POSITIVE);

    /** */
    public static final ParameterTypeDouble SIGMADEC =
            new ParameterTypeDouble("Sigma_DEC", "Car-following deceleration standard deviation.", Math.exp(0.156));

    /** Toledo desired headway model. */
    private static final DesiredHeadwayModel HEADWAY = new DesiredHeadwayModel()
    {
        @Override
        public Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
        {
            return parameters.getParameter(HSTAR).times(speed);
        }
    };

    /** Toledo desired speed model. */
    private static final DesiredSpeedModel DESIRED_SPEED = new DesiredSpeedModel()
    {
        @Override
        public Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
        {
            return parameters.getParameter(CDS).plus(parameters.getParameter(BETADS)).plus(
                    parameters.getParameter(ALPHADS).times(parameters.getParameter(ToledoLaneChangeParameters.ERROR_TERM)));
        }
    };

    /**
     * Constructor using Toledo models for desired headway ans speed.
     */
    public ToledoCarFollowing()
    {
        super(HEADWAY, DESIRED_SPEED);
    }

    /** {@inheritDoc} */
    @Override
    protected final Acceleration followingAcceleration(final Parameters parameters, final Speed speed, final Speed desiredSpeed,
            final Length desiredHeadway, final PerceptionIterable<? extends Headway> leaders) throws ParameterException
    {
        if (leaders.isEmpty() || leaders.first().getDistance().gt(desiredHeadway))
        {
            // free
            double eff = Toledo.RANDOM.nextGaussian() * parameters.getParameter(SIGMAFF) * parameters.getParameter(SIGMAFF);
            return new Acceleration(parameters.getParameter(LAMBDAFF) * (desiredSpeed.si - speed.si) + eff,
                    AccelerationUnit.SI);
        }
        // TODO speed difference with reaction time
        if (leaders.first().getSpeed().ge(speed))
        {
            // accelerate
            double eCfAcc =
                    Toledo.RANDOM.nextGaussian() * parameters.getParameter(SIGMAACC) * parameters.getParameter(SIGMAACC);
            // {@formatter:off}
            return new Acceleration(
                parameters.getParameter(CCFACC) 
                    * Math.pow(speed.si, parameters.getParameter(BETAACC))
                    * Math.pow(leaders.first().getDistance().si, parameters.getParameter(GAMMAACC))
                    * Math.pow(getDensity(leaders), parameters.getParameter(RHOACC))
                    * Math.pow(leaders.first().getSpeed().si - speed.si, parameters.getParameter(LAMBDAACC))
                    + eCfAcc,
                AccelerationUnit.SI);
            // {@formatter:on}
        }
        // decelerate
        double eCfDec = Toledo.RANDOM.nextGaussian() * parameters.getParameter(SIGMADEC) * parameters.getParameter(SIGMADEC);
        // {@formatter:off}
        return new Acceleration(
            parameters.getParameter(CCFDEC) 
                * Math.pow(leaders.first().getDistance().si, parameters.getParameter(GAMMADEC))
                * Math.pow(getDensity(leaders), parameters.getParameter(RHODEC))
                * Math.pow(speed.si - leaders.first().getSpeed().si, parameters.getParameter(LAMBDADEC))
                + eCfDec,
            AccelerationUnit.SI);
        // {@formatter:on}
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "ToledoCFM";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Toledo car-following model";
    }

    /**
     * Returns the density based on the leaders in veh/km.
     * @param leaders leading vehicles
     * @return density based on the leaders in veh/km
     */
    private double getDensity(final PerceptionIterable<? extends Headway> leaders)
    {
        if (leaders.isEmpty())
        {
            return 0;
        }
        Headway last = null;
        int n = 0;
        for (Headway next : leaders)
        {
            n++;
            last = next;
        }
        return last.getDistance().getInUnit(LengthUnit.KILOMETER) / n;
    }

}
