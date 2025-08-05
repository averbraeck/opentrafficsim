package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;

/**
 * Represents the final state of the forced merge maneuver.
 */
public class EndForcedMerge extends StartForcedMerge {

    public EndForcedMerge(final MergingHidas drivingTask) throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException {
        super(drivingTask);
        this.update();
    }

    @Override
    public SimpleOperationalPlan executeControl() {
        this.drivingTask.getAbstractMirovaVehicle().setRunningManeuver(false);
        return null; // No operational plan needed in this state
    }

    @Override
    public void next() {
        // No further transitions
    }
}
