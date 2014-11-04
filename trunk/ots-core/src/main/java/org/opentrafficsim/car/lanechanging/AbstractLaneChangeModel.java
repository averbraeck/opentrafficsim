package org.opentrafficsim.car.lanechanging;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.following.FollowAcceleration;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVector;

/**
 * Common code of many lane change models.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 4 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneChangeModel implements LaneChangeModel
{
    /**
     * {@inheritDoc}
     */
    @Override
    public final LaneChangeModelResult computeLaneChangeAndAcceleration(final AbstractLaneBasedGTU<?> gtu,
            final Collection<AbstractLaneBasedGTU<?>> sameLaneGTUs,
            final Collection<AbstractLaneBasedGTU<?>> preferredLaneGTUs,
            final Collection<AbstractLaneBasedGTU<?>> nonPreferredLaneGTUs,
            final DoubleScalar.Abs<SpeedUnit> speedLimit, final Rel<AccelerationUnit> preferredLaneRouteIncentive,
            final Rel<AccelerationUnit> nonPreferredLaneRouteIncentive) throws RemoteException
    {
        // Fairly straightforward application of egoistic lane change model in Treiber c.s. pp 239-244
        try
        {
            System.out.println(String.format(
                    "Route desire to merge to preferredLane: %.3f, route desire to merge to overtakingLane: %.3f",
                    preferredLaneRouteIncentive, nonPreferredLaneRouteIncentive));
            Lane lane = gtu.getLongitudinalPositions().keySet().iterator().next();
            // TODO make this driving side dependent.
            final LateralDirectionality preferred = LateralDirectionality.RIGHT;
            final LateralDirectionality nonPreferred = LateralDirectionality.LEFT;
            Lane nonPreferredLane = lane.accessibleAdjacentLane(nonPreferred, gtu.getGTUType());
            Lane preferredLane = lane.accessibleAdjacentLane(preferred, gtu.getGTUType());
            DoubleScalar.Abs<AccelerationUnit> straightA =
                    applyDriverPersonality(FollowAcceleration.acceleration(gtu, sameLaneGTUs, speedLimit));
            DoubleScalar.Abs<AccelerationUnit> nonPreferredA =
                    null == nonPreferredLane ? null : applyDriverPersonality(FollowAcceleration.acceleration(gtu,
                            nonPreferredLaneGTUs, speedLimit));
            DoubleScalar.Abs<AccelerationUnit> preferredA =
                    null == preferredLane ? null : applyDriverPersonality(FollowAcceleration.acceleration(gtu,
                            preferredLaneGTUs, speedLimit));
            // The egoistic lane change only looks at his/her own gain; therefore ignores field 1 of the accelerations.
            if (null == preferredA)
            {
                if (null == nonPreferredA)
                {
                    // No lane change possible; this is definitely the easy case
                    return new LaneChangeModelResult(gtu.getGTUFollowingModel().computeAcceleration(gtu, sameLaneGTUs,
                            speedLimit), null);
                }
                else
                {
                    // Merge to nonPreferredLane is possible; merge to preferredLane is NOT possible
                    if (DoubleScalar.plus(nonPreferredA, nonPreferredLaneRouteIncentive).getSI() > straightA.getSI())
                    {
                        // Merge to the adjacent nonPreferred lane
                        return new LaneChangeModelResult(gtu.getGTUFollowingModel().computeAcceleration(gtu,
                                nonPreferredLaneGTUs, speedLimit), nonPreferred);
                    }
                    else
                    {
                        // Stay in the current lane
                        return new LaneChangeModelResult(gtu.getGTUFollowingModel().computeAcceleration(gtu,
                                sameLaneGTUs, speedLimit), null);
                    }
                }
            }
            // A merge to the preferredLane is possible
            if (null == nonPreferredA)
            {
                // Merge to preferredLane is possible; merge to nonPreferred lane is NOT possible
                if (DoubleScalar.plus(preferredA, preferredLaneRouteIncentive).getSI() > straightA.getSI())
                {
                    // Merge to the preferred lane
                    return new LaneChangeModelResult(gtu.getGTUFollowingModel().computeAcceleration(gtu,
                            preferredLaneGTUs, speedLimit), preferred);
                }
                else
                {
                    // Stay in current lane
                    return new LaneChangeModelResult(gtu.getGTUFollowingModel().computeAcceleration(gtu, sameLaneGTUs,
                            speedLimit), null);
                }
            }
            // All merges are possible
            DoubleScalar.Rel<AccelerationUnit> preferredAttractiveness =
                    DoubleScalar.minus(DoubleScalar.plus(preferredA, preferredLaneRouteIncentive).immutable(),
                            straightA).immutable();
            DoubleScalar.Rel<AccelerationUnit> nonPreferredAttractiveness =
                    DoubleScalar.minus(DoubleScalar.plus(nonPreferredA, nonPreferredLaneRouteIncentive).immutable(),
                            straightA).immutable();
            if (preferredAttractiveness.getSI() <= 0 && nonPreferredAttractiveness.getSI() < 0)
            {
                // Stay in current lane
                return new LaneChangeModelResult(gtu.getGTUFollowingModel().computeAcceleration(gtu, sameLaneGTUs,
                        speedLimit), null);

            }
            if (preferredAttractiveness.getSI() > 0
                    && preferredAttractiveness.getSI() > nonPreferredAttractiveness.getSI())
            {
                // Merge to the preferred lane
                return new LaneChangeModelResult(gtu.getGTUFollowingModel().computeAcceleration(gtu, preferredLaneGTUs,
                        speedLimit), preferred);
            }
            // Merge to the adjacent nonPreferred lane
            return new LaneChangeModelResult(gtu.getGTUFollowingModel().computeAcceleration(gtu, nonPreferredLaneGTUs,
                    speedLimit), nonPreferred);
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        throw new Error("Cannot happen");
    }

    /**
     * Return the weighted acceleration as described by the personality.
     * @param accelerations DoubleVector.Abs.Dense&lt;AccelerationUnit&gt;; the acceleration that the reference GTU will
     *            make (in position 0 of the vector) and the acceleration that the (new) follower GTU will make (in
     *            position 1 of the vector)
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration that the personality of the driver uses
     */
    public abstract DoubleScalar.Abs<AccelerationUnit> applyDriverPersonality(
            DoubleVector.Abs.Dense<AccelerationUnit> accelerations);
}
