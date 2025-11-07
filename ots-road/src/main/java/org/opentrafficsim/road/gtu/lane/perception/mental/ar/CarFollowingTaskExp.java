package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.TaskHeadwayCollector;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;

/**
 * Car-following task demand based on headway with exponential relationship.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CarFollowingTaskExp extends AbstractArTask
{

    /** Car-following task parameter. */
    public static final ParameterTypeDuration HEXP = new ParameterTypeDuration("h_exp",
            "Exponential decay of car-following task by headway.", Duration.ofSI(3.83), NumericConstraint.POSITIVE);

    /**
     * Constructor.
     */
    public CarFollowingTaskExp()
    {
        super("car-following");
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        LaneBasedGtu gtu = perception.getGtu();
        Parameters parameters = gtu.getParameters();
        NeighborsPerception neighbors = Try.assign(() -> perception.getPerceptionCategory(NeighborsPerception.class),
                IllegalStateException.class, "No NeighborsPerception in perception");
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
        Duration headway = leaders.collect(new TaskHeadwayCollector(gtu.getSpeed()));
        return headway == null ? 0.0 : Math.exp(-headway.si / parameters.getParameter(HEXP).si);
    }

}
