package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes;

import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.SupportMergeHidas;

public class HumanDrivenVehicle extends AbstractMirovaVehicle
{

    public HumanDrivenVehicle(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception, final MirovaTacticalPlanner tacticalPlanner) throws OperationalPlanException
    {
        super(carFollowingModel, gtu, lanePerception, tacticalPlanner);
    }

    @Override
    protected void initializeDrivingTasks() throws OperationalPlanException
    {
        this.listDrivingTasks.add(new MergingHidas(this));
        this.listDrivingTasks.add(new SupportMergeHidas(this));
        this.listDrivingTasks.add(new BasicHuman(this));
        // Initialize driving tasks specific to human-driven vehicles here
        // Example:
        // this.dictDrivingTasks.put("CarFollowing", new CarFollowingTask());
        // this.dictDrivingTasks.put("LaneKeeping", new LaneKeepingTask());
    }
}
