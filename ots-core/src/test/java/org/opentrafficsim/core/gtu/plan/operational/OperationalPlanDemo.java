package org.opentrafficsim.core.gtu.plan.operational;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * Build various operational plans and print them.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class OperationalPlanDemo
{
    /**
     * Do not instantiate.
     */
    private OperationalPlanDemo()
    {
        // Do not instantiate.
    }

    /**
     * Test.
     * @param args String[]; args for main
     * @throws OperationalPlanException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws OperationalPlanException, OTSGeometryException
    {
        OTSLine3D path1 = new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(0.0, 0.0), new OTSPoint3D(100.0, 0.0)});

        // go from 0 to 10 m/s over entire distance. This should take 20 sec with a=0.5 m/s2.
        OperationalPlan plan1 = OperationalPlanBuilder.buildGradualAccelerationPlan(null, path1, Time.ZERO, Speed.ZERO,
                new Speed(10.0, SpeedUnit.METER_PER_SECOND));
        System.out.println(plan1);

        // go from 0 to 10 m/s over entire distance, but limit a to 0.1 m/s2.
        // This should take 44.72 sec with a=0.1 m/s2, and an end speed of 4.472 m/s.
        OperationalPlan plan2 = OperationalPlanBuilder.buildGradualAccelerationPlan(null, path1, Time.ZERO, Speed.ZERO,
                new Speed(10.0, SpeedUnit.METER_PER_SECOND), new Acceleration(0.1, AccelerationUnit.METER_PER_SECOND_2),
                new Acceleration(-0.1, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan2);

        // go from 0 to 10 m/s with a = 1 m/s2, followed by a constant speed of 10 m/s.
        // This should take 10 sec with a = 1 m/s2, reaching 50 m. After that, 50 m with 10 m/s in 5 sec.
        OperationalPlan plan3 = OperationalPlanBuilder.buildMaximumAccelerationPlan(null, path1, Time.ZERO, Speed.ZERO,
                new Speed(10.0, SpeedUnit.METER_PER_SECOND), new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2),
                new Acceleration(-1.0, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan3);

        // go from 10 to 0 m/s with a = -1 m/s2, which should truncate the path at 50 m.
        // This should take 10 sec with a = -1 m/s2, reaching 50 m. After that, the plan should stop.
        OperationalPlan plan4 = OperationalPlanBuilder.buildMaximumAccelerationPlan(null, path1, Time.ZERO,
                new Speed(10.0, SpeedUnit.METER_PER_SECOND), new Speed(0.0, SpeedUnit.METER_PER_SECOND),
                new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2),
                new Acceleration(-1.0, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan4);

        // try to stop with a = -2 m/s2, which should truncate the path at 25 m.
        // This should take 5 sec with a = -2 m/s2, reaching 25 m. After that, the plan should stop.
        OperationalPlan plan5 = OperationalPlanBuilder.buildStopPlan(null, path1, Time.ZERO,
                new Speed(10.0, SpeedUnit.METER_PER_SECOND), new Acceleration(-2.0, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan5);

    }

}
