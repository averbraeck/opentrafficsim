package org.opentrafficsim.road.gtu.perception.categories.neighbors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborTriplet;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.network.lane.conflict.Conflict;

/**
 * EstimationTest.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class EstimationTest
{

    /**
     * Constructor.
     */
    public EstimationTest()
    {
        //
    }

    /**
     * Test factor estimation and none estimation.
     * @throws ParameterException exception
     * @throws OperationalPlanException exception
     */
    @Test
    public void testEstimation() throws ParameterException, OperationalPlanException
    {
        singleEstimation(Estimation.FACTOR_ESTIMATION, 0.9, Time.instantiateSI(59.0));
        singleEstimation(Estimation.FACTOR_ESTIMATION, 0.9, Time.instantiateSI(58.0));
        singleEstimation(Estimation.FACTOR_ESTIMATION, 0.5, Time.instantiateSI(59.0));
        singleEstimation(Estimation.FACTOR_ESTIMATION, 0.8, Time.instantiateSI(58.0));
        // Mimic NONE estimation by using 1.0 for the situational awareness (all equations multiplied by 1)
        singleEstimation(Estimation.NONE, 1.0, Time.instantiateSI(59.0));
        singleEstimation(Estimation.NONE, 1.0, Time.instantiateSI(58.0));
    }

    /**
     * Test specific case.
     * @param estimation estimation
     * @param sa situational awareness
     * @param estTime estimation time
     * @throws ParameterException exception
     * @throws OperationalPlanException exception
     */
    private void singleEstimation(final Estimation estimation, final double sa, final Time estTime)
            throws ParameterException, OperationalPlanException
    {
        // Scenario
        Time now = Time.instantiateSI(60.0);
        Speed egoSpeed = Speed.instantiateSI(26.0);
        Speed otherSpeed = Speed.instantiateSI(25.0);
        Length egoPosition = Length.instantiateSI(200.0);
        Length otherPosition = Length.instantiateSI(230.0);
        Length currentHeadway = otherPosition.minus(egoPosition);
        double factor = 1.0 + (1.0 - sa);

        // Perceiving GTU
        LaneBasedGtu perceivingGtu = Mockito.mock(LaneBasedGtu.class, Mockito.RETURNS_DEEP_STUBS);
        ParameterSet perceivingParameters = new ParameterSet();
        perceivingParameters.setParameter(Estimation.OVER_EST, 1.0);
        perceivingParameters.setParameter(AdaptationSituationalAwareness.SA_MAX, 1.0);
        perceivingParameters.setParameter(AdaptationSituationalAwareness.SA, sa);
        Mockito.when(perceivingGtu.getParameters()).thenReturn(perceivingParameters);
        Mockito.when(perceivingGtu.getOdometer()).thenReturn(egoPosition);
        Mockito.when(perceivingGtu.getSpeed(any(Time.class))).thenReturn(egoSpeed);
        Mockito.when(perceivingGtu.getOdometer(any(Time.class)))
                .thenAnswer((t) -> egoPosition.minus(now.minus(estTime).times(egoSpeed)));
        EgoPerception<?, ?> ego = Mockito.mock(EgoPerception.class);
        Mockito.when(ego.getSpeed()).thenReturn(egoSpeed);
        Mockito.when(perceivingGtu.getTacticalPlanner().getPerception().getPerceptionCategory(any())).thenReturn(ego);

        // Some arbitrary static reference object
        Conflict reference = Mockito.mock(Conflict.class);

        // Perceived GTU
        LaneBasedGtu perceivedGtu = Mockito.mock(LaneBasedGtu.class);
        Mockito.when(perceivedGtu.getOdometer()).thenReturn(otherPosition);
        Mockito.when(perceivedGtu.getOdometer(any(Time.class)))
                .thenAnswer((t) -> otherPosition.minus(now.minus(estTime).times(otherSpeed)));
        Mockito.when(perceivedGtu.getSpeed(any(Time.class))).thenReturn(otherSpeed);
        Mockito.when(perceivedGtu.getAcceleration(any(Time.class))).thenReturn(Acceleration.ZERO);

        // == static reference ==
        // downstream: vehicle is 30m downstream, was closer
        NeighborTriplet neighbor = estimation.estimate(perceivingGtu, reference, perceivedGtu, currentHeadway, true, estTime);
        assertEquals(Acceleration.ZERO, neighbor.acceleration());
        assertEquals(otherSpeed.times(factor), neighbor.speed());
        assertEquals(currentHeadway.minus(otherSpeed.times(now.minus(estTime))).times(factor), neighbor.headway());

        // upstream: vehicle is 30m upstream, was even further upstream
        neighbor = estimation.estimate(perceivingGtu, reference, perceivedGtu, currentHeadway, false, estTime);
        assertEquals(Acceleration.ZERO, neighbor.acceleration());
        assertEquals(otherSpeed.times(factor), neighbor.speed());
        assertEquals(currentHeadway.plus(otherSpeed.times(now.minus(estTime))).times(factor), neighbor.headway());

        // == dynamic reference (i.e. perceiving GTU) ==
        // downstream: vehicle is 30m downstream, was closer, ego was further
        neighbor = estimation.estimate(perceivingGtu, perceivingGtu, perceivedGtu, currentHeadway, true, estTime);
        assertEquals(Acceleration.ZERO, neighbor.acceleration());
        assertEquals(egoSpeed.plus(otherSpeed.minus(egoSpeed).times(factor)), neighbor.speed());
        assertEquals(currentHeadway.minus(otherSpeed.times(now.minus(estTime))).plus(egoSpeed.times(now.minus(estTime)))
                .times(factor), neighbor.headway());

        // upstream: vehicle is 30m upstream, was even further upstream, ego was closer
        neighbor = estimation.estimate(perceivingGtu, perceivingGtu, perceivedGtu, currentHeadway, false, estTime);
        assertEquals(Acceleration.ZERO, neighbor.acceleration());
        assertEquals(egoSpeed.plus(otherSpeed.minus(egoSpeed).times(factor)), neighbor.speed());
        assertEquals(currentHeadway.plus(otherSpeed.times(now.minus(estTime))).minus(egoSpeed.times(now.minus(estTime)))
                .times(factor), neighbor.headway());
    }

}
