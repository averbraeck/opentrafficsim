package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.FreeMerge;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;

/**
 * Represents the state where the vehicle accelerates to align itself with the target gap. This state is responsible for
 * controlling the vehicle's acceleration to ensure it reaches the desired position within the merge gap. It calculates the
 * required acceleration based on the current gap parameters and transitions to the next state once the vehicle is properly
 * positioned. Responsibilities: - Controls the vehicle's acceleration to align with the target gap. - Ensures the vehicle
 * maintains safe distances to the leading and following vehicles in the gap. - Transitions to the next state when the vehicle
 * is correctly positioned. Methods: - executeControl(): Sets the vehicle's acceleration based on the calculated minimum
 * required acceleration. - next(): Transitions to the laneChangeToTargetGap state if the vehicle is correctly positioned.
 * Attributes: - drivingTask: The driving task associated with the merge maneuver. Transitions: - To laneChangeToTargetGap if
 * the vehicle is correctly positioned within the gap.
 */
public class AccelerateToTargetGap extends StartFreeMergeToTargetGap
{
    /** Time until end of lane. */
    private Duration timeToEndOfLane;

    /** Ego vehicle deceleration for lane change. */
    private Acceleration egoDecel;

    /** Follower vehicle deceleration for lane change. */
    private Acceleration rearDecel;

    public AccelerateToTargetGap(final MergingHidas drivingTask)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super(drivingTask);
        this.drivingTask.getAbstractMirovaVehicle().setDesire(1.0,
                this.drivingTask.getParameters().getParameter(ParameterTypes.TAU));
        this.update();
    }

    @Override
    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
     // Berechne und speichere die Werte als Attribute
        this.timeToEndOfLane = this.drivingTask.getTimeUntilEndOfLane();
        this.egoDecel = this.drivingTask.getAbstractMirovaVehicle()
                .getLaneChangeEgoDeceleration(LateralDirectionality.LEFT);
        this.rearDecel = this.drivingTask.getAbstractMirovaVehicle()
                .getLaneChangeFollowerDeceleration(LateralDirectionality.LEFT);
        return super.update();
    }

    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException
    {
        this.drivingTask.getAbstractMirovaVehicle().setDesire(1.0,
                this.drivingTask.getParameters().getParameter(ParameterTypes.TAU));
        Acceleration neededAcceleration = this.drivingTask.getAbstractMirovaVehicle().freeAcceleration();

        if (getGapFollowerSpeed().minus(Speed.instantiateSI(8)).lt(getEgoSpeed()))
        {
            neededAcceleration = Acceleration.min(neededAcceleration, this.calculateAccelerationForMergeTime(this.drivingTask
                    .getTimeUntilEndOfLane().minus(this.drivingTask.getParameters().getParameter(ParameterTypes.LCDUR))));
        }

        return new SimpleOperationalPlan(neededAcceleration, this.drivingTask.getParameters().getParameter(ParameterTypes.DT),
                LateralDirectionality.NONE);
    }

    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {

        if (this.timeToEndOfLane.ge(this.drivingTask.getParameters().getParameter(ParameterTypes.LCDUR).times(0.5)))
        {
            if (this.rearDecel.ge(this.drivingTask.getParameters().getParameter(ParameterTypes.B).neg())
                    && this.egoDecel.ge(this.drivingTask.getParameters().getParameter(ParameterTypes.B).neg()))
            {
                this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new LaneChangeToTargetGap(this.drivingTask));
            }
            else if (this.timeToEndOfLane.le(this.drivingTask.getParameters().getParameter(ParameterTypes.LCDUR))
                    && this.rearDecel.ge(this.drivingTask.getParameters().getParameter(ParameterTypes.BCRIT).neg())
                    && this.egoDecel.ge(this.drivingTask.getParameters().getParameter(ParameterTypes.BCRIT).neg()))
            {
                this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new LaneChangeToTargetGap(this.drivingTask));
            }
        }
    }

}
