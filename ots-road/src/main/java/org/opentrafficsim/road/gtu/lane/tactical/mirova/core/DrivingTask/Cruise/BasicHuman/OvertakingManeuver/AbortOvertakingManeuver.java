package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;

public class AbortOvertakingManeuver extends StartOvertakingManeuver
{
    public AbortOvertakingManeuver(final BasicHuman drivingTask)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super(drivingTask);
        this.update();
    }

    @Override
    public SimpleOperationalPlan executeControl()
    {
        return null; // No operational plan is executed in this state

    }

    @Override
    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        return super.update();
    }

    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new EndOvertakingManeuver(this.drivingTask));
    }

    @Override
    public void abort() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super.abort();
    }
}
