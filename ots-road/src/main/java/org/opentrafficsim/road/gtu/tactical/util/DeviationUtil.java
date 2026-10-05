package org.opentrafficsim.road.gtu.tactical.util;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeBoolean;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.perception.categories.WienerProcess;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.network.LanePosition;

import nl.tudelft.simulation.jstats.math.ProbMath;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Utility to integrate in to tactical planners to allow lateral deviation within the lane.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public final class DeviationUtil
{

    /** Enables lateral deviation due to leaders. */
    public static final ParameterTypeBoolean DEV_LEADERS =
            new ParameterTypeBoolean("dev_leaders", "Enables lateral deviation due to leaders", false);

    /** Enables lateral deviation due to randomness. */
    public static final ParameterTypeBoolean DEV_RANDOM =
            new ParameterTypeBoolean("dev_random", "Enables lateral deviation due to randomness", false);

    /** Auto-correlation time of random deviation. */
    public static final ParameterTypeDuration DEV_RANDOM_TAU = new ParameterTypeDuration("dev_random_tau",
            "Auto-correlation time of random deviation", Duration.ofSI(120.0), NumericConstraint.POSITIVE);

    /**
     * Constructor.
     */
    private DeviationUtil()
    {
        //
    }

    /**
     * Adds lateral deviation intent. This method contains no {@link DeviationData} and thus ignores all deviation reasons that
     * rely on this. The current implementation performs deviation to avoid leaders on adjacent lanes that are partially on the
     * ego lane.
     * @param context tactical context
     * @param tManeuver duration of normal maneuver
     * @throws ParameterException if parameter is not defined
     * @throws OperationalPlanException if neighbors perception is not available
     */
    public static void deviate(final TacticalContextEgo context, final Duration tManeuver)
            throws ParameterException, OperationalPlanException
    {
        deviate(context, tManeuver, null);
    }

    /**
     * Adds lateral deviation intent. The current implementation performs deviation to avoid leaders on adjacent lanes that are
     * partially on the ego lane, and adds some randomness to the deviation.
     * @param context tactical context
     * @param data GTU specific deviation data
     * @param tManeuver duration of normal maneuver
     * @throws ParameterException if parameter is not defined
     * @throws OperationalPlanException if neighbors perception is not available
     */
    public static void deviate(final TacticalContextEgo context, final Duration tManeuver, final DeviationData data)
            throws ParameterException, OperationalPlanException
    {
        if (context.getParameters().getOptionalParameter(DEV_LEADERS).orElse(false))
        {
            deviateLeaders(context, tManeuver);
        }
        if (data != null && context.getParameters().getOptionalParameter(DEV_RANDOM).orElse(false))
        {
            deviateRandom(context, data);
        }
    }

    /**
     * Adds lateral deviation from leaders.
     * @param context tactical context
     * @param tManeuver duration of normal maneuver
     * @throws ParameterException if parameter is not defined
     * @throws OperationalPlanException if neighbors or infrastructure perception is not available
     */
    private static void deviateLeaders(final TacticalContextEgo context, final Duration tManeuver)
            throws ParameterException, OperationalPlanException
    {
        NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        considerLeaders(context, tManeuver, neighbors.getLeaders(RelativeLane.LEFT), -1.0,
                infra.isShoulder(RelativeLane.RIGHT));
        considerLeaders(context, tManeuver, neighbors.getLeaders(RelativeLane.RIGHT), 1.0, infra.isShoulder(RelativeLane.LEFT));
    }

    /**
     * Adds lateral deviation as a response to a leader being partially on the lane. Deviation goes from 0 to maximum (not
     * exceeding the edge) as the leader exceeds the lane marking from 0 distance up to the maximum still allowing the width of
     * the ego vehicle against the edge. This allows a reducing lateral buffer space.
     * @param context tactical context
     * @param tManeuver duration of normal maneuver
     * @param leaders leaders
     * @param sign direction sign
     * @param shoulder whether the lane that ego can deviate towards is a shoulder, in which case we allow going over the edge
     */
    private static void considerLeaders(final TacticalContextEgo context, final Duration tManeuver,
            final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders, final double sign, final boolean shoulder)
    {
        Length most = null;
        Length mostDistance = null;
        if (context.getSpeed().si == 0.0)
        {
            return;
        }
        double horizon = tManeuver.si * context.getSpeed().si;
        for (PerceivedGtu leader : leaders)
        {
            if (leader.getDistance().si > horizon)
            {
                break;
            }
            if (leader.getSpeed().si > context.getSpeed().si)
            {
                continue; // ttc negative
            }
            double devLeader = leader.getManeuver().getDeviation().si;
            double wLeader = leader.getWidth().si;
            double wLaneLeader = leader.getLaneWidth().si;
            double exceeds = sign * devLeader + 0.5 * (wLeader - wLaneLeader);
            if (exceeds > 0.0)
            {
                double dist = leader.getDistance().si < 0.0 ? 0.0 : leader.getDistance().si;
                double ttc = dist / (context.getSpeed().si - leader.getSpeed().si);
                double wEgo = context.getWidth().si;
                double wLaneEgo = context.getLaneWidth().si;
                // stay within edge, or in case of shoulder allow going 45% of width on it (let's keep center on lane)
                double laneSpace = shoulder ? wLaneEgo - .45 * wEgo : wLaneEgo - wEgo;
                // amount of deviation depends on extend the leader exceeds the lane marking
                double f = Math.max(0.0, Math.min(exceeds / laneSpace, 1.0));
                double dev = sign * f * 0.5 * laneSpace;
                // select most critical leader, discounting over space
                if (most == null || dev * (1.0 - dist / horizon) > most.si * (1.0 - mostDistance.si / horizon))
                {
                    most = Length.ofSI(dev);
                    // at the location of the "collision" we want the deviation
                    mostDistance = Length.ofSI(context.getSpeed().si * ttc);
                }
            }
        }
        if (most != null)
        {
            context.addIntent(most, mostDistance);
        }
    }

    /**
     * Adds lateral deviation from randomness.
     * @param context tactical context
     * @param data deviation data
     */
    private static void deviateRandom(final TacticalContextEgo context, final DeviationData data)
    {
        Optional<DistancedObject<Length>> intentedDeviation = context.getIntent(Length.class);
        LanePosition pos = context.getPosition();
        double laneWidth = pos.lane().getWidth(pos.getFraction()).si;
        double vehWidth = context.getWidth().si;
        Length targetDist = Length.ZERO;
        double r = data.getRandom();
        // stay with 30cm of edge (on the edge looks unnatural)
        double rScaling = Math.max(0.0, 0.5 * (laneWidth - vehWidth) - 0.3);
        double targetDev;
        if (intentedDeviation.isPresent())
        {
            targetDist = intentedDeviation.get().distance();
            double dev = intentedDeviation.get().object().si;
            if (Math.signum(r) == Math.signum(dev))
            {
                // if random and actual intent are in the same direction, reduce random amplitude to not exceed the lane width
                rScaling = Math.max(0.0, rScaling - Math.abs(dev));
            }
            targetDev = dev + r * rScaling;
        }
        else
        {
            targetDev = r * rScaling;
        }
        context.addIntent(Length.ofSI(targetDev), targetDist);
    }

    /**
     * Deviation data.
     */
    public static class DeviationData
    {

        /** Random auto-correlation Wiener process. */
        private final WienerProcess wienerProcess;

        /**
         * Constructor.
         * @param simulator simulator
         * @param tau random auto-correlation time
         */
        public DeviationData(final OtsSimulatorInterface simulator, final Duration tau)
        {
            StreamInterface stream = simulator.getModel().getDefaultStream();
            this.wienerProcess = new WienerProcess(stream, 0.0, 1.0, tau, simulator);
        }

        /**
         * Returns a random value in the range [-1 1] with bias towards the middle. This is computed by taking the value from a
         * Wiener process that is standard normally distributed, but with auto-correlation, and taking half of it through the
         * {@code erf()} function. This gives values equivalent to {@code 2*NormalCDF(Normal(0, sqrt(0.5))) - 1}. The behavioral
         * interpretation of this is:
         * <ul>
         * <li>Around a deviation of 0 the distribution is essentially normally distributed (with a linear scaling of about
         * 0.564).</li>
         * <li>Towards the edges there is a <i>saturation</i> effect prohibiting further deviation. This can be seen as a pushing
         * force from the edge.</li>
         * </ul>
         * @return random value in the range [-1 1]
         */
        private double getRandom()
        {
            return ProbMath.erf(0.5 * this.wienerProcess.draw());
        }

    }

}
