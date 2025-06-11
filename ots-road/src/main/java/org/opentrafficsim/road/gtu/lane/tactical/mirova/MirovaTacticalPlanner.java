package org.opentrafficsim.road.gtu.lane.tactical.mirova;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;

public class MirovaTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    @Override
    public OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint2d locationAtStartTime)
            throws GtuException, NetworkException, ParameterException
    {
        return null;
    }




}
