package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Execution: Transition: zu Vorderfahrzeug aufgeschlossen bzw. vWunsch erreicht und Lücke noch vorhanden
 * @param drivingTask the driving task context
 */
public class ExecuteLaneChangeLeft extends StartOvertakingManeuver
{
    private final int originLaneId;

    private final int targetLaneId;

    public ExecuteLaneChangeLeft(final DrivingTask drivingTask)
    {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl()
    {
        Duration minimumTimeHeadway = this.drivingTask.getParameters().getParameter(ParameterTypes.TMIN);
        if (this.drivingTask.getParameters().getParameter(ParameterTypes.T).gt(minimumTimeHeadway))
        {
            this.drivingTask.getParameters().setParameterResettable(ParameterTypes.T, minimumTimeHeadway);
        }
        Parameters parameters = this.drivingTask.getParameters();
        Speed egoSpeed = this.drivingTask.getEgoPerception().getSpeed();
        SpeedLimitInfo speedLimitInfoOriginLane = this.drivingTask.getInfrastructurePerception()
                .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(new Length(1.0));
        SpeedLimitInfo speedLimitInfoTargetLane = this.drivingTask.getInfrastructurePerception()
                .getSpeedLimitProspect(RelativeLane.LEFT).getSpeedLimitInfo(new Length(1.0));

        HeadwayGtu headwayGtuTargetLane = this.drivingTask.getNeighborsPerception().getFirstLeaders(LateralDirectionality.LEFT);
        HeadwayGtu headwayGtuOriginLane = this.drivingTask.getDirectDefaultSimplePerception().getForwardHeadwayGtu();

        Acceleration accelerationOriginLane = this.drivingTask.getAbstractMirovaVehicle().getCarFollowingModel()
                .followingAcceleration(parameters, egoSpeed, speedLimitInfoOriginLane, headwayGtuOriginLane);
        Acceleration accelerationTargetLane = this.drivingTask.getAbstractMirovaVehicle().getCarFollowingModel()
                .followingAcceleration(parameters, egoSpeed, speedLimitInfoTargetLane, headwayGtuTargetLane);
    }

    @Override
    public void update()
    {
        super.update();
    }

    @Override
    public void next()
    {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        if (v.getCurrentLaneId() == this.targetLaneId
                && "NONE".equals(v.getContextDriverDevice().getVissimVehicle().AttValue("LNCHG")))
        {
            this.active = false;
            if (v.getCurrentSpeed() >= v.getContextDriverDevice().getDesiredSpeed())
            {
                v.setLastExecutedActionState(new EndOvertakingManeuver(this.drivingTask));
            }
            else
            {
                v.setLastExecutedActionState(new AccelerateOnTargetLane(this.drivingTask));
            }
        }
    }
}
