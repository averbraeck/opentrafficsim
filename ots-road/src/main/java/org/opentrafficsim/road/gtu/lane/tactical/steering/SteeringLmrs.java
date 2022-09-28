package org.opentrafficsim.road.gtu.lane.tactical.steering;

import java.util.LinkedHashSet;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeClassList;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.Steering;
import org.opentrafficsim.road.gtu.lane.tactical.util.Steering.FeedbackTable;
import org.opentrafficsim.road.gtu.lane.tactical.util.Steering.SteeringState;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsData;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SteeringLmrs extends AbstractLaneBasedTacticalPlanner
{

    /** Parameter type for mandatory lane change incentives. */
    public static final ParameterTypeClassList<MandatoryIncentive> MANDATORY = new ParameterTypeClassList<>("man.incent.",
            "Mandatory lane-change incentives.", ParameterTypeClassList.getValueClass(MandatoryIncentive.class));

    /** Parameter type for voluntary lane change incentives. */
    public static final ParameterTypeClassList<VoluntaryIncentive> VOLUNTARY = new ParameterTypeClassList<>("vol.incent.",
            "Voluntary lane-change incentives.", ParameterTypeClassList.getValueClass(VoluntaryIncentive.class));

    /** Serialization id. */
    private static final long serialVersionUID = 20160300L;

    /** Lane change status. */
    private final LaneChange laneChange;

    /** LMRS data. */
    private final LmrsData lmrsData;

    /** Set of mandatory lane change incentives. */
    private final LinkedHashSet<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();

    /** Set of voluntary lane change incentives. */
    private final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();

    /** Steering state. */
    private final SteeringState steeringState = new SteeringState();

    /** Feedback table. */
    private final FeedbackTable feedbackTable;

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel CarFollowingModel; Car-following model.
     * @param gtu LaneBasedGTU; GTU
     * @param lanePerception LanePerception; perception
     * @param synchronization Synchronization; type of synchronization
     * @param cooperation Cooperation; type of cooperation
     * @param gapAcceptance GapAcceptance; gap-acceptance
     * @param feedbackTable FeedbackTable; feedback table
     */
    public SteeringLmrs(final CarFollowingModel carFollowingModel, final LaneBasedGTU gtu, final LanePerception lanePerception,
            final Synchronization synchronization, final Cooperation cooperation, final GapAcceptance gapAcceptance,
            final FeedbackTable feedbackTable)
    {
        super(carFollowingModel, gtu, lanePerception);
        this.laneChange = Try.assign(() -> new LaneChange(gtu), "Parameter LCDUR is required.", GTUException.class);
        this.lmrsData = new LmrsData(synchronization, cooperation, gapAcceptance, Tailgating.NONE);
        this.feedbackTable = feedbackTable;
    }

    /**
     * Adds a mandatory incentive. Ignores {@code null}.
     * @param incentive MandatoryIncentive; Incentive to add.
     */
    public final void addMandatoryIncentive(final MandatoryIncentive incentive)
    {
        if (incentive != null)
        {
            this.mandatoryIncentives.add(incentive);
        }
    }

    /**
     * Adds a voluntary incentive. Ignores {@code null}.
     * @param incentive VoluntaryIncentive; Incentive to add.
     */
    public final void addVoluntaryIncentive(final VoluntaryIncentive incentive)
    {
        if (incentive != null)
        {
            this.voluntaryIncentives.add(incentive);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, GTUException, NetworkException, ParameterException
    {
        // LMRS
        SimpleOperationalPlan simplePlan = LmrsUtil.determinePlan(getGtu(), startTime, getCarFollowingModel(), this.laneChange,
                this.lmrsData, getPerception(), this.mandatoryIncentives, this.voluntaryIncentives);

        if (simplePlan.isLaneChange())
        {
            this.laneChange.setDesiredLaneChangeDuration(getGtu().getParameters().getParameter(ParameterTypes.LCDUR));
        }

        // set turn indicator
        simplePlan.setTurnIndicator(getGtu());

        // create plan
        OperationalPlan referencePlan =
                LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), startTime, simplePlan, this.laneChange);

        return Steering.fromReferencePlan(getGtu(), getGtu().getParameters(), this.steeringState, referencePlan,
                this.feedbackTable);
    }

}
