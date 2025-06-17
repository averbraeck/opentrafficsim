package org.opentrafficsim.road.gtu.lane.tactical.mirova;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.HumanDrivenVehicle;

public class MirovaTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{

    private static final ParameterTypeDuration DT = ParameterTypes.DT;
    private static AbstractMirovaVehicle mirovaVehicle;
    {

    };

    public MirovaTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception)
    {
        super(carFollowingModel, gtu, lanePerception);
        mirovaVehicle = new HumanDrivenVehicle( carFollowingModel, gtu, lanePerception, this);

    }

    @Override
    public OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint2d locationAtStartTime)
            throws GtuException, NetworkException, ParameterException
    {
        Acceleration updatedAcceleration = Acceleration.ZERO;

        SimpleOperationalPlan plan = new SimpleOperationalPlan(updatedAcceleration, DT.getDefaultValue());

        return LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), startTime, plan, null);
    }


    public Duration getDT()
    {
        return DT.getDefaultValue();
    }
}
