package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.SocialInteractionsChunk;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;

import java.util.function.Supplier;

/**
 * KnowledgeChunk modeling social interactions based on Schakel et al. (2023):
 * "Get out of the way" and "Stay out of the way" behaviors resulting from social pressure.
 */
public class SocialInteractionsChunk extends KnowledgeChunk
{


    public SocialInteractionsChunk(final AbstractMirovaVehicle vehicle) throws OperationalPlanException
    {
        super(vehicle);
        // procedural patterns possibly triggered by social interactions
        this.addManeuverPattern(() -> createManeuverPattern("GET_OUT_OF_THE_WAY"));
        this.addManeuverPattern(() -> createManeuverPattern("STAY_OUT_OF_THE_WAY"));
    }

    @Override
    public boolean isApplicable() throws ParameterException
    {
        // Applicable if speed is above threshold (e.g., 20 m/s)
        if (getEgoPerception().getSpeed().gt(Speed.instantiateSI(20.0))) // at least 20 m/s
        {
            return true; //
        }
        else
        {
            return false; // no social interaction at low speeds
        }

    }

    @Override
    public Desire computeDesire() throws ParameterException
    {

        double socioSpeedSensitivity = getAbstractMirovaVehicle().getSocioSpeedSensitivity();
        // Stay out of the way (left lane)
        Double rhoPotentialFollower = followerSocialPressure(RelativeLane.LEFT);
        Double rhoEgoPotential = egoSocialPressure(RelativeLane.LEFT);
        Double dLeft = 0.0;

        if (getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).gt(getParameters().getParameter(ParameterTypes.LOOKAHEAD))
            && rhoPotentialFollower != null && rhoEgoPotential != null && rhoPotentialFollower * socioSpeedSensitivity > rhoEgoPotential)
        {
            dLeft = -rhoPotentialFollower * socioSpeedSensitivity ; // suppress lane change to left with negative incentive
        }



        // Get out of the way (right lane)
        Double rhoActualFollower = followerSocialPressure(RelativeLane.CURRENT);
        Double rhoEgo = egoSocialPressure(RelativeLane.CURRENT);
        Double dRight = 0.0;

        if (getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).gt(getParameters().getParameter(ParameterTypes.LOOKAHEAD))
            && rhoActualFollower != null && rhoEgo != null && rhoActualFollower * socioSpeedSensitivity > rhoEgo)
        {
            dRight = rhoActualFollower * socioSpeedSensitivity; // encourage lane change to right with positive incentive
        }

        return new Desire(0.0, dLeft, dRight, false); // discretionary desire

    }

    private Double followerSocialPressure(final RelativeLane lane) throws ParameterException
    {
        if (getNeighborsPerception().getFollowers(lane).isEmpty())
        {
            return null; // no follower
        }

        Speed vGain = getAbstractMirovaVehicle().getVGain();
        Speed vLeader = getEgoPerception().getSpeed();
        HeadwayGtu follower = getNeighborsPerception().getFollowers(lane).first();
        Speed followerDesiredSpeed = follower.getDesiredSpeed();
        Length headway = follower.getDistance();
        Length followerLookahead = follower.getParameters().getParameter(ParameterTypes.LOOKAHEAD);
        double rho = socialPressure(followerDesiredSpeed, vLeader, vGain, headway, followerLookahead);

        return rho;
    }

    private Double egoSocialPressure(final RelativeLane lane) throws ParameterException
    {
        if (getNeighborsPerception().getLeaders(lane).isEmpty())
        {
            return null; // no leader
        }
        Speed vGain = getAbstractMirovaVehicle().getVGain();
        Speed vFollower = getEgoPerception().getSpeed();
        Speed followerDesiredSpeed = getAbstractMirovaVehicle().getGtu().getDesiredSpeed();
        Length followerLookahead = getParameters().getParameter(ParameterTypes.LOOKAHEAD);
        HeadwayGtu leader = getNeighborsPerception().getLeaders(lane).first();
        Speed vLeader = leader.getSpeed();     // same-lane leader
        Length headway = leader.getDistance();               // same-lane spacing

        double rho = socialPressure(followerDesiredSpeed, vLeader, vGain, headway, followerLookahead);

        return rho;
    }

    /**
     * Returns a normalized social pressure, equal to (vDesired - vLead) / vGain.
     * @param followerDesiredSpeed desired speed
     * @param leaderSpeed leader speed
     * @param vGain vGain parameter
     * @param headway headway to the leader
     * @param followerLookahead anticipation distance
     * @return normalized social pressure
     */
    static double socialPressure(final Speed followerDesiredSpeed, final Speed leaderSpeed,
            final Speed vGain, final Length headway, final Length followerLookahead)
    {
        double dv = followerDesiredSpeed.si - leaderSpeed.si;
        if (dv < 0 || headway.gt(followerLookahead)) // larger headway may happen due to perception errors
        {
            return 0.0;
        }
        return 1.0 - Math.exp(-(dv / vGain.si) * (1.0 - (headway.si / followerLookahead.si)));
    }



    // ----------------------------------------------------------------------
    // helper: procedural knowledge patterns
    // ----------------------------------------------------------------------

    private ManeuverPattern createManeuverPattern(final String type)
    {
        return new ManeuverPattern()
        {
            @Override
            public void calculateActivation() throws ParameterException
            {
                if (type.equals("GET_OUT_OF_THE_WAY"))
                    setActivation(1.0); // strong activation if follower is tailgating
                else if (type.equals("STAY_OUT_OF_THE_WAY"))
                    setActivation(0.6); // moderate activation, suppress left change
            }

            @Override
            public String toString()
            {
                return "SocialInteractionPattern[" + type + "]";
            }
        };
    }
}
