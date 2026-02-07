package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborTriplet;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Estimation using EST_FACTOR as set by the Fuller implementation.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class FactorEstimation implements Estimation, Stateless<FactorEstimation>
{

    /** Erroneous estimation factor on distance and speed difference. */
    public static final ParameterTypeDouble EST_FACTOR = new ParameterTypeDouble("f_est",
            "Erroneous estimation factor on distance and speed difference.", 1.0, NumericConstraint.POSITIVE);

    /** Estimation based on a factor determined by Fuller. */
    public static final FactorEstimation SINGLETON = new FactorEstimation();

    /**
     * Constructor.
     */
    private FactorEstimation()
    {

    }

    @Override
    public NeighborTriplet estimate(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
            final LaneBasedGtu perceivedGtu, final Length distance, final boolean downstream, final Duration when)
            throws ParameterException
    {
        double factor = perceivingGtu.getParameters().getParameter(EST_FACTOR);
        Length headway = getDelayedDistance(perceivingGtu, reference, perceivedGtu, distance, downstream, when).times(factor);
        Speed speed = getDelayedReferenceSpeed(perceivingGtu, reference, when)
                .plus(getDelayedSpeedDifference(perceivingGtu, reference, perceivedGtu, when).times(factor));
        Acceleration acceleration = perceivedGtu.getAcceleration(when);
        return new NeighborTriplet(headway, Speed.max(speed, Speed.ZERO), acceleration);
    }

    @Override
    public FactorEstimation get()
    {
        return SINGLETON;
    }

    @Override
    public String toString()
    {
        return "FactorEstimation";
    }

}
