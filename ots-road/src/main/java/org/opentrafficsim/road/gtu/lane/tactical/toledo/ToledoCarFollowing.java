package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.NEGATIVE;
import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDuration;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractCarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class ToledoCarFollowing extends AbstractCarFollowingModel
{

    /** */
    public static final ParameterTypeSpeed CDS = new ParameterTypeSpeed("C_DS", "Constant in desired speed.", new Speed(
        17.636, SpeedUnit.SI), POSITIVE);

    /** */
    public static final ParameterTypeSpeed BETADS = new ParameterTypeSpeed("BETA_DS",
        "Reduction of desired speed for trucks.", new Speed(-1.458, SpeedUnit.SI), NEGATIVE);

    /** */
    public static final ParameterTypeSpeed ALPHADS = new ParameterTypeSpeed("ALPHA_DS",
        "Factor on error term of desired speed.", new Speed(-0.105, SpeedUnit.SI));

    /** */
    public static final ParameterTypeDuration HSTAR = new ParameterTypeDuration("h*", "Desired time headway.", new Duration(
        2.579, TimeUnit.SI));

    /** */
    public static final ParameterTypeDouble LAMBDAFF = new ParameterTypeDouble("Lambda_ff",
        "Free flow acceleration sensitivity.", 0.0881, POSITIVE);

    /** */
    public static final ParameterTypeDouble SIGMAFF = new ParameterTypeDouble("Sigma_ff",
        "Free flow acceleration standard deviation.", Math.exp(0.169));

    /** */
    public static final ParameterTypeDouble CCFACC = new ParameterTypeDouble("C_CF_ACC",
        "Constant for car following acceleration.", 0.0355, POSITIVE);

    /** */
    public static final ParameterTypeDouble BETAACC = new ParameterTypeDouble("BETA_ACC",
        "Power on speed for acceleration.", 0.291, POSITIVE);

    /** */
    public static final ParameterTypeDouble GAMMAACC = new ParameterTypeDouble("GAMMA_ACC",
        "Power on distance headway for acceleration.", -0.166, NEGATIVE);

    /** */
    public static final ParameterTypeDouble RHOACC = new ParameterTypeDouble("RHO_ACC",
        "Power on density for acceleration.", 0.550, POSITIVE);

    /** */
    public static final ParameterTypeDouble LAMBDAACC = new ParameterTypeDouble("LAMBDA_ACC",
        "Power on speed difference for acceleration.", 0.520, POSITIVE);

    /** */
    public static final ParameterTypeDouble SIGMAACC = new ParameterTypeDouble("Sigma_acc",
        "Car-following acceleration standard deviation.", Math.exp(0.126));

    /** */
    public static final ParameterTypeDouble CCFDEC = new ParameterTypeDouble("C_CF_DEC",
        "Constant for car following deceleration.", -0.860, NEGATIVE);

    /** */
    public static final ParameterTypeDouble GAMMADEC = new ParameterTypeDouble("GAMMA_DEC",
        "Power on distance headway for deceleration.", -0.565, NEGATIVE);

    /** */
    public static final ParameterTypeDouble RHODEC = new ParameterTypeDouble("RHO_DEC",
        "Power on density for deceleration.", 0.143, POSITIVE);

    /** */
    public static final ParameterTypeDouble LAMBDADEC = new ParameterTypeDouble("LAMBDA_DEC",
        "Power on speed difference for deceleration.", 0.834, POSITIVE);

    /** */
    public static final ParameterTypeDouble SIGMADEC = new ParameterTypeDouble("Sigma_DEC",
        "Car-following deceleration standard deviation.", Math.exp(0.156));

    /** {@inheritDoc} */
    @Override
    public final Speed
        desiredSpeed(final BehavioralCharacteristics behavioralCharacteristics, final SpeedLimitInfo speedInfo)
            throws ParameterException
    {
        return behavioralCharacteristics.getParameter(CDS).plus(behavioralCharacteristics.getParameter(BETADS)).plus(
            behavioralCharacteristics.getParameter(ALPHADS).multiplyBy(
                behavioralCharacteristics.getParameter(ToledoLaneChangeParameters.ERROR_TERM)));
    }

    /** {@inheritDoc} */
    @Override
    public final Length desiredHeadway(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed)
        throws ParameterException
    {
        return behavioralCharacteristics.getParameter(HSTAR).multiplyBy(speed);
    }

    /** {@inheritDoc} */
    @Override
    protected final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final Speed desiredSpeed, final Length desiredHeadway, final SortedMap<Length, Speed> leaders)
        throws ParameterException
    {
        if (leaders.isEmpty() || leaders.firstKey().gt(desiredHeadway))
        {
            // free
            double eff =
                Toledo.RANDOM.nextGaussian() * behavioralCharacteristics.getParameter(SIGMAFF)
                    * behavioralCharacteristics.getParameter(SIGMAFF);
            return new Acceleration(behavioralCharacteristics.getParameter(LAMBDAFF) * (desiredSpeed.si - speed.si) + eff,
                AccelerationUnit.SI);
        }
        // TODO speed difference with reaction time
        if (leaders.get(leaders.firstKey()).ge(speed))
        {
            // accelerate
            double eCfAcc =
                Toledo.RANDOM.nextGaussian() * behavioralCharacteristics.getParameter(SIGMAACC)
                    * behavioralCharacteristics.getParameter(SIGMAACC);
            // {@formatter:off}
            return new Acceleration(
                behavioralCharacteristics.getParameter(CCFACC) 
                    * Math.pow(speed.si, behavioralCharacteristics.getParameter(BETAACC))
                    * Math.pow(leaders.firstKey().si, behavioralCharacteristics.getParameter(GAMMAACC))
                    * Math.pow(getDensity(leaders), behavioralCharacteristics.getParameter(RHOACC))
                    * Math.pow(leaders.get(leaders.firstKey()).si - speed.si, behavioralCharacteristics.getParameter(LAMBDAACC))
                    + eCfAcc,
                AccelerationUnit.SI);
            // {@formatter:on}
        }
        // decelerate
        double eCfDec =
            Toledo.RANDOM.nextGaussian() * behavioralCharacteristics.getParameter(SIGMADEC)
                * behavioralCharacteristics.getParameter(SIGMADEC);
        // {@formatter:off}
        return new Acceleration(
            behavioralCharacteristics.getParameter(CCFDEC) 
                * Math.pow(leaders.firstKey().si, behavioralCharacteristics.getParameter(GAMMADEC))
                * Math.pow(getDensity(leaders), behavioralCharacteristics.getParameter(RHODEC))
                * Math.pow(speed.si - leaders.get(leaders.firstKey()).si, behavioralCharacteristics.getParameter(LAMBDADEC))
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
    private double getDensity(final SortedMap<Length, Speed> leaders)
    {
        if (leaders.isEmpty())
        {
            return 0;
        }
        return leaders.lastKey().getInUnit(LengthUnit.KILOMETER) / leaders.size();
    }

}
