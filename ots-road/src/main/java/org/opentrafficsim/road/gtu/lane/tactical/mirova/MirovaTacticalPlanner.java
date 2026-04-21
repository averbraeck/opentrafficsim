package org.opentrafficsim.road.gtu.lane.tactical.mirova;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.*;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.*;
import org.opentrafficsim.core.gtu.*;
import org.opentrafficsim.core.gtu.perception.*;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.*;
import org.opentrafficsim.road.gtu.lane.perception.*;
import org.opentrafficsim.road.gtu.lane.perception.categories.*;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.*;
import org.opentrafficsim.road.gtu.lane.plan.operational.*;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern.PatternType;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util.MaxUtilityArbitrator;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util.PatternSelector;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util.PlanArbitrator;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util.ScoredOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.following.MirovaCarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.network.*;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.speed.*;

import java.util.*;

/**
 * Abstract base vehicle for the MIROVA tactical framework.
 * <p>
 * Provides:
 * <ul>
 * <li>Integration of LMRS-based tactical reasoning</li>
 * <li>Voting arbiter for maneuver arbitration</li>
 * <li>Central {@link VehicleContextManager} for contextual data handling</li>
 * </ul>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MirovaTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    // ----------------------------------------------------------------------
    // Tactical and Planning Components
    // ----------------------------------------------------------------------

    /** Serial version UID for serialization compatibility. */
    private static final long serialVersionUID = 1L;

    /** The active action state of the currently executing maneuver. */
    protected ActionState currentActionState = null;

    /** The operational plan generated for the current simulation step. */
    protected SimpleOperationalPlan operationalPlan;

    /** The lane change object handling the physical lane change constraints. */
    protected final LaneChange laneChange;

    /** The maneuver pattern that won the arbitration in the previous simulation step. */
    protected ManeuverPattern lastActivePattern = null;

    /** Hysteresis multiplier to prevent rapid switching between maneuver patterns (default 1.10 = +10%). */
    protected double hysteresisMultiplier = 1.10;

    /** The arbitration strategy used to select the winning operational plan. */
    protected PlanArbitrator planArbitrator = new MaxUtilityArbitrator();

    // ----------------------------------------------------------------------
    // LMRS Desire Dynamics
    // ----------------------------------------------------------------------

    /** Current total lateral desire vector (left/right). */
    protected Desire laneChangeDesire = Desire.zero();

    /** Current mandatory lateral desire vector (left/right). */
    protected Desire mandatoryLaneChangeDesire = Desire.zero();

    /** Current discretionary lateral desire vector (left/right). */
    protected Desire discretionaryLaneChangeDesire = Desire.zero();

    /** Absolute magnitude of the current lane change desire. */
    protected Double absoluteDesire = 0.0;

    /** Relaxation time for the desire vector. */
    protected Duration desireRelaxationTime = new Duration(0.0, DurationUnit.SI);

    /** Socio-speed pressure experienced by the GTU. */
    private Double socioSpeedPressure = 0.0;

    /** Time since last lane change maneuver started. */
    private Duration timeSinceLastLaneChange = new Duration(0.0, DurationUnit.SI);

    /** GTU specific parameters. */
    private Parameters params;

    // ----------------------------------------------------------------------
    // Knowledge Base and Patterns
    // ----------------------------------------------------------------------

    /** Declarative knowledge base for this vehicle. */
    protected final List<KnowledgeChunk> knowledgeChunks = new ArrayList<>();

    /** Procedural knowledge: available exclusive maneuver patterns. */
    protected final List<ManeuverPattern> exclusiveManeuverPatterns = new ArrayList<>();

    /** Procedural knowledge: available parallel maneuver patterns. */
    protected final List<ManeuverPattern> parallelManeuverPatterns = new ArrayList<>();

    /**
     * * The ActionState that currently locks the tactical planner. This is ONLY set during physical points of no return (e.g.,
     * executing a lane change).
     */
    protected ActionState lockedActionState = null;

    // ----------------------------------------------------------------------
    // Context Manager Integration
    // ----------------------------------------------------------------------

    /** Central contextual model for this vehicle. */
    private final VehicleContextManager contextManager;

    // ----------------------------------------------------------------------
    // Headway Relaxation: Episode-based implementation (Schakel et al. 2012, 2023)
    // ----------------------------------------------------------------------

    /**
     * Relaxation time constant τ (seconds) controlling the adaptation speed when the desired headway increases after a decrease
     * in desire.
     * <p>
     * Typical LMRS range: 20–30 s.
     * </p>
     */
    private Duration tauHeadway = new Duration(25.0, DurationUnit.SI);

    /** Current relaxed headway T(t) used by the car-following model. */
    private Duration currentRelaxedHeadway = null;

    /** Instantaneous target headway derived from the current lane-change desire. */
    private Duration targetDesiredHeadway = null;

    /** Headway value at the start of the current relaxation episode. */
    private Duration headwayAtRelaxStart = null;

    /** Target headway fixed at the start of the current relaxation episode. */
    private Duration targetAtRelaxStart = null;

    /** Normalized relaxation progress from 0 (start) to 1 (completed). */
    private double relaxProgress = 0.0;

    /** Simulation time at which the vehicle was created. */
    private Duration createTime;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Instantiates the MIROVA Tactical Planner. * @param carFollowingModel the car following model
     * @param gtu the lane based GTU
     * @param lanePerception the lane perception system
     * @throws ParameterException if a required parameter is missing
     */
    public MirovaTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception) throws ParameterException
    {
        super(carFollowingModel, gtu, lanePerception);

        this.laneChange = Try.assign(() -> new LaneChange(gtu), "Parameter LCDUR is required.", GtuException.class);
        this.contextManager = new VehicleContextManager(this);
        this.params = getGtu().getParameters();
        this.laneChange.setDesiredLaneChangeDuration(getGtu().getParameters().getParameter(ParameterTypes.LCDUR));
        this.createTime = gtu.getSimulator().getSimulatorTime();
    }

    // ----------------------------------------------------------------------
    // Main Tactical Update
    // ----------------------------------------------------------------------

    /**
     * Generates the operational plan for the current simulation step.
     * <p>
     * This method checks if the vehicle is fully positioned; if not, it returns a default plan to skip the current step.
     * Otherwise, it invokes the main tactical update routine to compute the vehicle's behavior.
     * </p>
     * @param startTime the start time of the operational plan
     * @param locationAtStartTime the location of the vehicle at the start time
     * @return the generated {@link OperationalPlan}
     * @throws GtuException if GTU-related errors occur
     * @throws NetworkException if network-related errors occur
     * @throws ParameterException if parameter access fails
     */
    @Override
    public OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint2d locationAtStartTime)
            throws GtuException, NetworkException, ParameterException
    {
        Duration dt = getGtu().getParameters().getParameter(ParameterTypes.DT);
        SimpleOperationalPlan plan;
        Boolean justCreated = (startTime.si < this.createTime.si + 1.0);

        if (getGtu().getFront() == null || getGtu().getReferencePosition() == null || getGtu().getOperationalPlan() == null
                || justCreated)
        {
            // GTU is not fully positioned yet -> skip this tick
            Acceleration acc = getGtu().getCarFollowingAcceleration();
            plan = new SimpleOperationalPlan(acc, dt);
        }
        else
        {
            plan = this.update();
        }

        return LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), startTime, plan, this.getLaneChange());
    }

    /**
     * Executes one full tactical decision cycle for the MIROVA vehicle.
     * <p>
     * This method represents the central update routine that governs the vehicle’s tactical behavior on a microscopic level.
     * The process follows a strict 4-layer architecture:
     * </p>
     * <ol>
     * <li><b>Perception & Context:</b> Updates world knowledge via {@link VehicleContextManager}.</li>
     * <li><b>Cognition:</b> Computes aggregated motivation (desire) from all {@link KnowledgeChunk}s.</li>
     * <li><b>Relaxation:</b> Applies temporal smoothing to desired headways to prevent abrupt maneuvers.</li>
     * <li><b>Decision & Action:</b> Evaluates running maneuvers, selects exclusive or parallel {@link ManeuverPattern}s, and
     * outputs a physical {@link SimpleOperationalPlan}. Defaults to standard car-following.</li>
     * </ol>
     * @return the {@link SimpleOperationalPlan} representing the vehicle’s tactical decision for the current time step
     * @throws ParameterException if a parameter lookup fails during desire or ability checks
     * @throws NullPointerException if required perception or context data are unavailable
     * @throws IllegalArgumentException if a consistency condition is violated
     * @throws NetworkException if the network structure cannot be queried
     * @throws GtuException if GTU state errors occur
     */
    public SimpleOperationalPlan update()
            throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException
    {
        if (this.currentRelaxedHeadway == null)
        {
            this.currentRelaxedHeadway = this.getGtu().getParameters().getParameter(ParameterTypes.T);
        }

        // 1. Update perception and contextual information
        this.contextManager.advanceTick();
        updateTimeSinceLastLaneChange();
        this.updateContext();
        NeighborsContext neighborsContext = getContextManager().getCategory("Neighbors", NeighborsContext.class);
        neighborsContext.getFrontGapDeltaSpeed(LateralDirectionality.NONE); // ensure headway GTU are updated

        // 2. Compute current LMRS-style net desire (aggregated from all knowledge chunks)
        updateLaneChangeDesire();

        // 3. Derive a single scalar desire magnitude for car-following adjustments
        this.absoluteDesire = this.laneChangeDesire.magnitude();

        // 4. Apply temporal relaxation (gradual decay of short-term motivation and headway adaptation)
        updateTargetDesiredHeadway();
        updateCurrentRelaxedHeadway();

        // 5. Reset operational plan for this time step
        this.operationalPlan = null;

        // 6. Determine operational plan
        // 6.1 Check if an action is strictly locked (Physical point of no return)
        // 6.1 Check if an action is strictly locked (Physical point of no return)
        if (this.lockedActionState != null)
        {
            this.operationalPlan = this.lockedActionState.update();
            if (this.operationalPlan == null)
            {
                // Action finished its procedure this tick. Release lock and fall through to arbitration!
                this.releaseActionLock();
            }
        }
        else
        {
            // 6.2 Arbitration: Evaluate all applicable patterns using the PlanArbitrator strategy
            // NEU: Korrigierter Code (Refactoring)
            List<ScoredOperationalPlan> proposedPlans = new ArrayList<>();
            ArrayList<ManeuverPattern> allPatterns = new ArrayList<>(); // ArrayList explizit nutzen für Parameterübergabe
            allPatterns.addAll(this.exclusiveManeuverPatterns);
            allPatterns.addAll(this.parallelManeuverPatterns);

            // 6.2a Vorfilterung: Nur anwendbare oder bereits laufende Manöver zulassen
            ArrayList<ManeuverPattern> relevantPatterns = PatternSelector.getAllRelevantPatterns(allPatterns);

            // 6.2b Evaluation: Iteriere nur über die gefilterten, validen Patterns
            for (ManeuverPattern pattern : relevantPatterns)
            {
                // Das Pattern ist kontextuell und physikalisch valide (oder läuft bereits).
                // Jetzt generieren wir den vorgeschlagenen physischen Plan.
                SimpleOperationalPlan proposedPlan = pattern.update();

                if (proposedPlan != null)
                {
                    double utility = pattern.getCurrentActionState().getUtility();

                    // Apply Hysteresis to prevent flickering during the initial selection phase
                    if (pattern.equals(this.lastActivePattern))
                    {
                        utility *= this.hysteresisMultiplier;
                    }

                    // Wrap the proposal into the strategy-compatible object
                    proposedPlans
                            .add(new ScoredOperationalPlan(proposedPlan, utility, pattern, pattern.getCurrentActionState()));
                }
            }

            // 6.3 Execute arbitration strategy if at least one plan was proposed
            if (!proposedPlans.isEmpty())
            {
                ScoredOperationalPlan winningPlan = this.planArbitrator.arbitrate(proposedPlans);

                this.operationalPlan = winningPlan.getOperationalPlan();
                this.lastActivePattern = winningPlan.getSourcePattern();
                this.currentActionState = winningPlan.getSourceState();
            }
            else
            {
                // 6.4 Fallback: Pure Car-Following (only invoked if no pattern is active or returns a valid plan)
                this.lastActivePattern = null;
                this.currentActionState = null; // Ensure no orphaned state remains active

                EgoContext egoContext = getContextManager().getCategory("Ego", EgoContext.class);
                Acceleration cfAcceleration = egoContext.getCurrentCarFollowingAcceleration();
                Duration dt = this.getGtu().getParameters().getParameter(ParameterTypes.DT);

                this.operationalPlan = new SimpleOperationalPlan(cfAcceleration, dt, LateralDirectionality.NONE);
            }
        }

        // 7. Update turn indicator intent based on plan and desires
        if (this.operationalPlan.getIndicatorIntent().isLeft())
        {
            getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.LEFT);
        }
        else if (this.operationalPlan.getIndicatorIntent().isRight())
        {
            getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.RIGHT);
        }
        else if (getLaneChangeDesire().magnitude() > getDFree())
        {
            // if strong desire but no explicit indicator intent, use desire direction for indicators
            if (getLaneChangeDesire().dominantDirection() == LateralDirectionality.LEFT)
            {
                getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.LEFT);
            }
            else if (getLaneChangeDesire().dominantDirection() == LateralDirectionality.RIGHT)
            {
                getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.RIGHT);
            }
        }
        else
        {
            getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.NONE);
        }

        // Debug output for critical accelerations
        Acceleration planAcc = this.operationalPlan.getAcceleration();
        if (planAcc.si < -8.0 || planAcc.eq(Acceleration.NEGATIVE_INFINITY) || planAcc.le(Acceleration.NEG_MAXVALUE))
        {
            System.out.printf("GTU: %s @simsec: %s -> Plan acceleration: %s, ActionState: %s%n", getGtu().getId(),
                    getGtu().getSimulator().getSimulatorTime().toDisplayString(), planAcc.toDisplayString(),
                    (this.currentActionState != null) ? this.currentActionState.toString() : "none");
        }

        // if (getGtu().getLane().getId().equals("FORWARD4"))
        // {
        // System.out.printf("GTU: %s @simsec: %s -> State: %s, Desire: %s, Plan Acc: %s%n", getGtu().getId(),
        // getGtu().getSimulator().getSimulatorTime().toDisplayString(),
        // (this.currentActionState != null) ? this.currentActionState.toString() : "none",
        // getLaneChangeDesire().toString(), planAcc.toDisplayString());
        // }
        if (getGtu().getLane().getLink().getId().equals("BC")
                && (getGtu().getId().equals("319") || getGtu().getId().equals("281") || getGtu().getId().equals("82")))
        {
            EgoContext egoContext = getContextManager().getCategory("Ego", EgoContext.class);
            InfrastructureContext infra = getContextManager().getCategory("Infrastructure", InfrastructureContext.class);
            System.out.printf("GTU: %s @simsec: %s -> State: %s, Desire: %s, Plan Acc: %s%n", getGtu().getId(),
                    getGtu().getSimulator().getSimulatorTime().toDisplayString(),
                    (this.currentActionState != null) ? this.currentActionState.toString() : "none",
                    getLaneChangeDesire().toString(), planAcc.toDisplayString());
            System.out.printf("  -> Active Relaxations: %s, Acc Cache: %s%n", egoContext.getActiveRelaxations().toString(),
                    egoContext.getCurrentTickAccelerationCache().toString());
            System.out.printf("  -> Distance to End of Lane right: %s%n", infra.getDistanceToLaneEnd(RelativeLane.RIGHT));
        }

        return this.operationalPlan;
    }

    /**
     * Selects a maneuver pattern of the specified type using the {@link PatternSelector}.
     * @param patterns the list of candidate maneuver patterns
     * @return the selected maneuver pattern, or null if none is applicable
     * @throws ParameterException if pattern selection fails due to parameter issues
     */
    protected ManeuverPattern selectPatternByType(final ArrayList<ManeuverPattern> patterns) throws ParameterException
    {
        return PatternSelector.select(patterns);
    }

    /**
     * Returns all {@link KnowledgeChunk}s currently assigned to this vehicle. These represent the declarative knowledge
     * influencing tactical reasoning.
     * @return list of all knowledge chunks
     */
    public List<KnowledgeChunk> getKnowledgeChunks()
    {
        return this.knowledgeChunks;
    }

    /**
     * Registers a new {@link KnowledgeChunk} to this vehicle. This method is typically called in the constructor of the
     * concrete vehicle class.
     * @param chunk the knowledge chunk to add
     */
    public void addKnowledgeChunk(final KnowledgeChunk chunk)
    {
        if (chunk != null && !this.knowledgeChunks.contains(chunk))
        {
            this.knowledgeChunks.add(chunk);
        }
    }

    /**
     * Registers a new exclusive {@link ManeuverPattern} to this vehicle. Exclusive patterns represent maneuvers that cannot be
     * combined with others. This method is typically called in the constructor of the concrete vehicle class.
     * @param pattern the exclusive maneuver pattern to add
     */
    public void addExclusiveManeuverPattern(final ManeuverPattern pattern)
    {
        if (pattern != null && !this.exclusiveManeuverPatterns.contains(pattern))
        {
            this.exclusiveManeuverPatterns.add(pattern);
        }
    }

    /**
     * Registers a new parallel {@link ManeuverPattern} to this vehicle. Parallel patterns represent maneuvers that can be
     * combined with others. This method is typically called in the constructor of the concrete vehicle class.
     * @param pattern the parallel maneuver pattern to add
     */
    public void addParallelManeuverPattern(final ManeuverPattern pattern)
    {
        if (pattern != null && !this.parallelManeuverPatterns.contains(pattern))
        {
            this.parallelManeuverPatterns.add(pattern);
        }
    }

    /**
     * Returns all registered parallel {@link ManeuverPattern}s for this vehicle.
     * @return list of parallel maneuver patterns
     */
    public ArrayList<ManeuverPattern> getParallelManeuverPatterns()
    {
        return new ArrayList<>(this.parallelManeuverPatterns);
    }

    /**
     * Returns all registered exclusive {@link ManeuverPattern}s for this vehicle. * @return list of exclusive maneuver patterns
     */
    public ArrayList<ManeuverPattern> getExclusiveManeuverPatterns()
    {
        return new ArrayList<>(this.exclusiveManeuverPatterns);
    }

    // ----------------------------------------------------------------------
    // LMRS Desire Integration
    // ----------------------------------------------------------------------

    /**
     * Computes and updates the total (mandatory + discretionary) desire vector for this vehicle based on all active
     * {@link KnowledgeChunk}s.
     * <p>
     * The result represents the LMRS-style aggregated motivation for lane changing, which can later be used for tactical
     * decisions (e.g., thresholding, maneuver selection).
     * </p>
     * @throws ParameterException if any chunk's desire computation fails
     * @throws NetworkException if the network structure cannot be queried
     * @throws GtuException if GTU state errors occur
     */
    protected void updateLaneChangeDesire() throws ParameterException, GtuException, NetworkException
    {
        this.mandatoryLaneChangeDesire = Desire.zero();
        this.discretionaryLaneChangeDesire = Desire.zero();

        // collect all desires from active chunks
        for (KnowledgeChunk chunk : this.getKnowledgeChunks())
        {
            if (chunk.isApplicable())
            {
                Desire d = chunk.computeDesire();
                if (d.isMandatory())
                {
                    this.mandatoryLaneChangeDesire = this.mandatoryLaneChangeDesire.add(d);
                }
                else
                {
                    this.discretionaryLaneChangeDesire = this.discretionaryLaneChangeDesire.add(d);
                }
            }
        }

        // combine mandatory + discretionary using LMRS weighting per direction
        double dSync = this.getDMand();
        double dCoop = this.getDFree();

        this.laneChangeDesire =
                Desire.combine(this.mandatoryLaneChangeDesire, this.discretionaryLaneChangeDesire, dSync, dCoop);
    }

    /**
     * Returns the current combined LMRS desire. * @return the combined lane change desire
     */
    public Desire getLaneChangeDesire()
    {
        return this.laneChangeDesire;
    }

    // ----------------------------------------------------------------------
    // Context Handling
    // ----------------------------------------------------------------------

    /** Updates all registered context categories once per simulation tick. */
    public void updateContext()
    {
        this.contextManager.updateFromPerception();
    }

    /**
     * Returns the central vehicle context manager. * @return the context manager
     */
    public VehicleContextManager getContextManager()
    {
        return this.contextManager;
    }

    /**
     * Generic accessor for a full context category. * @param <T> the type of the context category
     * @param clazz the class type of the context category
     * @return the requested context category, or null if not found
     */
    public <T extends ContextCategory> T getContext(final Class<T> clazz)
    {
        for (ContextCategory cat : this.contextManager.getAllCategories().values())
        {
            if (clazz.isInstance(cat))
            {
                return clazz.cast(cat);
            }
        }
        return null;
    }

    /**
     * Generic accessor for a specific value in a context category. * @param <T> the value type
     * @param categoryName the name of the category
     * @param key the key mapping to the value
     * @param clazz the class type of the value
     * @return the context value, or null if not found
     */
    public <T> T getContextValue(final String categoryName, final String key, final Class<T> clazz)
    {
        ContextCategory cat = this.contextManager.getCategory(categoryName, ContextCategory.class);
        return cat != null ? cat.getValue(key, clazz) : null;
    }

    /**
     * Returns the free driving time in the specified lane change direction. This method iterates through all leaders in the
     * specified direction and calculates the minimum free driving time based on their speed and distance. * @param
     * laneChangeDirection The direction of the lane change (LEFT or RIGHT).
     * @return The free driving time available for a lane change.
     * @throws ParameterException if a required parameter is missing
     * @throws OperationalPlanException if operational plan errors occur
     */
    public Duration getFreeDrivingTime(final LateralDirectionality laneChangeDirection)
            throws ParameterException, OperationalPlanException
    {
        Duration freeDrivingTime = new Duration(Double.POSITIVE_INFINITY, DurationUnit.SI);
        for (HeadwayGtu leader : getPerception().getPerceptionCategory(NeighborsPerception.class)
                .getFirstLeaders(LateralDirectionality.RIGHT))
        {
            Speed speedDeltaLeader = getGtu().getDesiredSpeed().minus(leader.getSpeed());
            Length distanceLeader = leader.getDistance();

            if (speedDeltaLeader.gt0())
            {
                Duration freeDrivingTimeIterary = new Duration(distanceLeader.si / speedDeltaLeader.si, DurationUnit.SI);
                freeDrivingTime = Duration.min(freeDrivingTime, freeDrivingTimeIterary);
            }
        }
        return freeDrivingTime;
    }

    /**
     * Commits the vehicle to a specific action state, bypassing utility arbitration.
     * @param state the ActionState to lock
     */
    public void commitToAction(final ActionState state)
    {
        this.lockedActionState = state;
    }

    /**
     * Releases the physical action lock, allowing utility arbitration to resume.
     */
    public void releaseActionLock()
    {
        this.lockedActionState = null;
    }

    /**
     * Gets the currently active action state. * @return the current action state
     */
    public ActionState getCurrentActionState()
    {
        return this.currentActionState;
    }

    /**
     * Sets the currently active action state. * @param currentActionState the action state to set
     */
    public void setCurrentActionState(final ActionState currentActionState)
    {
        this.currentActionState = currentActionState;
    }

    /**
     * Retrieves the lane change model. * @return the lane change instance
     */
    public LaneChange getLaneChange()
    {
        return this.laneChange;
    }

    // ----------------------------------------------------------------------
    // Headway Relaxation Methods
    // ----------------------------------------------------------------------

    /**
     * Initializes or restarts a headway relaxation episode toward a new target.
     * <p>
     * This method fixes the starting and target headways and resets the progress accumulator. It should be called whenever a
     * significant change in the target headway occurs (e.g., due to a large change in lane-change desire).
     * </p>
     * @param newTarget the new target headway that should be reached after τ seconds
     */
    private void startHeadwayRelaxation(final Duration newTarget)
    {
        if (this.currentRelaxedHeadway == null)
        {
            this.currentRelaxedHeadway = newTarget;
        }
        this.headwayAtRelaxStart = this.currentRelaxedHeadway;
        this.targetAtRelaxStart = newTarget;
        this.relaxProgress = 0.0;
    }

    /**
     * Updates the instantaneous target headway from the current lane-change desire.
     * <p>
     * If the target headway differs significantly from the previous value, a new relaxation episode is started to ensure a
     * smooth transition.
     * </p>
     * @throws ParameterException if parameter access fails
     */
    protected void updateTargetDesiredHeadway() throws ParameterException
    {
        final Parameters parameters = this.getGtu().getParameters();
        final double limitedDesire = Math.max(0.0, Math.min(1.0, getSocioSpeedPressure()));

        final double tMin = parameters.getParameter(ParameterTypes.TMIN).si;
        final double tMax = parameters.getParameter(ParameterTypes.TMAX).si;
        final Duration newTarget = Duration.instantiateSI(limitedDesire * tMin + (1.0 - limitedDesire) * tMax);

        // Start a new relaxation episode only if the target changes significantly
        final double EPS = 0.05; // 50 ms tolerance to avoid jitter
        if (this.targetDesiredHeadway == null || Math.abs(newTarget.si - this.targetDesiredHeadway.si) > EPS)
        {
            this.targetDesiredHeadway = newTarget;
            startHeadwayRelaxation(newTarget);
        }
        else
        {
            this.targetDesiredHeadway = newTarget;
        }
    }

    /**
     * Sets a new target desired headway if it is smaller than the current target. * @param newTargetDesiredHeadway the new
     * target desired headway
     * @throws ParameterException if parameter resolution fails
     */
    public void setTargetDesiredHeadway(final Duration newTargetDesiredHeadway) throws ParameterException
    {
        if (newTargetDesiredHeadway.le(this.targetDesiredHeadway))
        {
            this.targetDesiredHeadway = newTargetDesiredHeadway;
            updateCurrentRelaxedHeadway();
        }
    }

    /**
     * Progresses the relaxed headway T(t) toward the episode’s target using a normalized progress accumulator. The adaptation
     * follows a linear schedule that reaches the target exactly after τ seconds.
     * <p>
     * When the target headway is smaller than the current one (i.e., higher desire), adaptation occurs immediately without
     * relaxation.
     * </p>
     * @throws ParameterException if parameter access fails
     */
    protected void updateCurrentRelaxedHeadway() throws ParameterException
    {
        // Initialize if not yet set
        if (this.currentRelaxedHeadway == null)
        {
            if (this.targetDesiredHeadway == null)
            {
                updateTargetDesiredHeadway();
            }
            this.currentRelaxedHeadway = this.targetDesiredHeadway;
            this.headwayAtRelaxStart = this.currentRelaxedHeadway;
            this.targetAtRelaxStart = this.targetDesiredHeadway;
            this.relaxProgress = 1.0;
            return;
        }

        final Duration dt = this.getGtu().getParameters().getParameter(ParameterTypes.DT);

        // Immediate change for decreasing headway or τ = 0
        if (this.tauHeadway.si == 0.0 || this.targetDesiredHeadway.si <= this.currentRelaxedHeadway.si)
        {
            this.currentRelaxedHeadway = this.targetDesiredHeadway;
            this.headwayAtRelaxStart = this.currentRelaxedHeadway;
            this.targetAtRelaxStart = this.targetDesiredHeadway;
            this.relaxProgress = 1.0;
        }
        else
        {
            // Linear progression: reach target exactly after τ seconds
            this.relaxProgress = Math.min(1.0, this.relaxProgress + dt.si / this.tauHeadway.si);
            final double startT = this.headwayAtRelaxStart.si;
            final double deltaT = this.targetAtRelaxStart.si - startT;
            final double newT = startT + this.relaxProgress * deltaT;
            this.currentRelaxedHeadway = Duration.instantiateSI(newT);
        }

        // Update GTU parameters for use by the car-following model
        final Parameters parameters = this.getGtu().getParameters();
        parameters.setParameterResettable(ParameterTypes.T, this.currentRelaxedHeadway);
    }

    private void updateDecelerationThresholds() throws ParameterException
    {
        final Parameters parameters = this.getGtu().getParameters();
        Acceleration maxEgo = parameters.getParameter(MirovaParameters.maxEgoDecelerationThreshold);
        Acceleration minEgo = parameters.getParameter(MirovaParameters.minEgoDecelerationThreshold);
        Acceleration maxFollower = parameters.getParameter(MirovaParameters.maxFollowerDecelerationThreshold);
        Acceleration minFollower = parameters.getParameter(MirovaParameters.minFollowerDecelerationThreshold);

        Double currentLaneChangeDesire =
                this.laneChangeDesire.magnitude() > getDFree() ? this.laneChangeDesire.magnitude() : getDFree(); // binary
                                                                                                                 // desire for
                                                                                                                 // simplicity,
                                                                                                                 // can be
                                                                                                                 // refined to
                                                                                                                 // use actual
                                                                                                                 // magnitude
        Double desireFactor = Math.min(currentLaneChangeDesire - getDFree(), 1.0);

        // interpolate linear based on desire (between DFREE and 1.0)
        Acceleration newEgo = Acceleration.instantiateSI(minEgo.si + (maxEgo.si - minEgo.si) * desireFactor);
        Acceleration newFollower =
                Acceleration.instantiateSI(minFollower.si + (maxFollower.si - minFollower.si) * desireFactor);

        parameters.setParameterResettable(MirovaParameters.egoDecelerationThreshold, newEgo);
        parameters.setParameterResettable(MirovaParameters.followerDecelerationThreshold, newFollower);
    }

    /**
     * Sets a new headway relaxation time constant τ (seconds). A value of zero disables relaxation and applies all headway
     * changes immediately.
     * @param tauSeconds the new relaxation time constant in seconds
     */
    public void setHeadwayRelaxationTime(final double tauSeconds)
    {
        this.tauHeadway = Duration.instantiateSI(Math.max(0.0, tauSeconds));
    }

    /**
     * Returns the current headway relaxation time constant τ.
     * @return the relaxation time constant
     */
    public Duration getHeadwayRelaxationTime()
    {
        return this.tauHeadway;
    }

    /**
     * Returns the currently active relaxed headway used by the car-following model.
     * @return the relaxed headway T(t)
     */
    public Duration getCurrentRelaxedHeadway()
    {
        return this.currentRelaxedHeadway;
    }

    /**
     * Returns the most recent target headway value derived from the current desire.
     * @return the target headway T<sub>target</sub>
     */
    public Duration getTargetDesiredHeadway()
    {
        return this.targetDesiredHeadway;
    }

    /**
     * Computes the longitudinal acceleration for the current time step, considering all tactical and environmental influences
     * provided via the vehicle’s {@link VehicleContextManager}.
     * <p>
     * The following control components are considered:
     * <ul>
     * <li><b>Leader-following behavior:</b> based on perceived headway, automatically applying Keane and Gao (2021)
     * relaxation.</li>
     * <li><b>Speed-limit adaptation:</b> handling of upcoming lower speed limits.</li>
     * <li><b>Transition effects:</b> optional curvature or bump-based deceleration.</li>
     * </ul>
     * The resulting acceleration is the minimum (most restrictive) value among all components.
     * </p>
     * <p>
     * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @return final longitudinal acceleration [m/s²]
     * @throws ParameterException if parameter retrieval fails
     * @throws NetworkException if the network structure cannot be queried
     * @throws GtuException if GTU state errors occur
     */
    public Acceleration computeLongitudinalAcceleration() throws ParameterException, GtuException, NetworkException
    {
        // 1. Retrieve tightly coupled contexts and parameters
        EgoContext ego = this.getContext(EgoContext.class);
        InfrastructureContext infra = this.getContext(InfrastructureContext.class);
        NeighborsContext neighbors = this.getContext(NeighborsContext.class);
        Parameters parameters = this.getParameters();

        // List of candidate accelerations
        List<Acceleration> candidates = new ArrayList<>();

        // =========================================================================================
        // SCHRITT 3: ZEITLÜCKEN-HACK WURDE HIER GELÖSCHT!
        // parameters.setParameterResettable(ParameterTypes.T, this.getCurrentRelaxedHeadway());
        // Das Basis-Modell bleibt ab sofort unangetastet.
        // =========================================================================================

        // 2. Leader-following (incorporating automatic 2-parameter relaxation via our Utility)
        Iterable<HeadwayGtu> currentLeaders = neighbors.getLeaders(LateralDirectionality.NONE);
        double maxLeadersToConsider = getParameters().getParameter(MirovaParameters.CF_MAX_LEADERS);
        List<HeadwayGtu> limitedLeaders = new ArrayList<>();
        int leaderCount = 0;
        for (HeadwayGtu leader : currentLeaders)
        {
            if (leaderCount >= maxLeadersToConsider)
            {
                break;
            }
            limitedLeaders.add(leader);
            leaderCount++;
        }
        currentLeaders = limitedLeaders;
        Acceleration aCf = MirovaCarFollowingUtil.followMultipleLeaders(this, currentLeaders);
        candidates.add(aCf);

        // 3. Transition deceleration (e.g., curvature or bumps)
        Acceleration aTrans = SpeedLimitUtil.considerSpeedLimitTransitions(parameters, ego.getEgoSpeed(), getPerception()
                .getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(RelativeLane.CURRENT),
                this.getCarFollowingModel());

        if (aTrans != null && aTrans.lt(Acceleration.POSITIVE_INFINITY))
        {
            candidates.add(aTrans);
        }

        // 4. Upcoming lower speed limit ahead
        SpeedLimitInfo nextLimit = infra.getNextSpeedLimit();
        Speed currentLegalLimit = infra.getLegalSpeedLimit();

        // Null-Safety: Prüfe, ob sowohl das nächste als auch das aktuelle SpeedLimit bekannt sind
        if (nextLimit != null && currentLegalLimit != null)
        {
            Speed nextLegal = SpeedLimitUtil.getLegalSpeedLimit(nextLimit);

            if (nextLegal.lt(currentLegalLimit))
            {
                // Nutze den OTS-Parameter anstelle der harten 200 Meter
                Length distanceToLimit = parameters.getParameter(ParameterTypes.LOOKAHEAD);
                Acceleration aLimit = MirovaCarFollowingUtil.approachTargetSpeed(this, distanceToLimit, nextLegal);

                if (aLimit != null)
                {
                    candidates.add(aLimit);
                }
            }
        }

        // 5. Compute most restrictive acceleration safely
        return candidates.stream().filter(java.util.Objects::nonNull).min(Acceleration::compareTo).orElse(aCf);
    }

    /**
     * Gets the current absolute lateral desire. * @return the magnitude of the lateral desire
     */
    public Double getDesire()
    {
        return this.absoluteDesire;
    }

    /**
     * Sets the absolute desire and its relaxation time. * @param desire the desire magnitude
     * @param desireRelaxationTime the relaxation duration
     */
    public void setDesire(final Double desire, final Duration desireRelaxationTime)
    {
        this.absoluteDesire = desire;
        this.desireRelaxationTime = desireRelaxationTime;
    }

    /**
     * Gets the mandatory lane change desire vector. * @return the mandatory lane change desire
     */
    public Desire getMandatoryLaneChangeDesire()
    {
        return this.mandatoryLaneChangeDesire;
    }

    /**
     * Gets the discretionary lane change desire vector. * @return the discretionary lane change desire
     */
    public Desire getDiscretionaryLaneChangeDesire()
    {
        return this.discretionaryLaneChangeDesire;
    }

    /**
     * Returns the free driving distance constant. * @return the value of DFREE
     * @throws ParameterException if parameter resolution fails
     */
    public double getDFree() throws ParameterException
    {
        return getParameters().getParameter(MirovaParameters.DFREE);
    }

    /**
     * Returns the mandatory driving distance constant. * @return the value of DMAND
     * @throws ParameterException if parameter resolution fails
     */
    public double getDMand() throws ParameterException
    {
        return getParameters().getParameter(MirovaParameters.DMAND);
    }

    /**
     * Returns the speed difference threshold (vGain) used in LMRS. * @return the value of vGain
     * @throws ParameterException if parameter resolution fails
     */
    public Speed getVGain() throws ParameterException
    {
        return getParameters().getParameter(MirovaParameters.vGain);
    }

    /**
     * Returns the critical speed threshold (vCrit) used in LMRS. * @return the value of vCrit
     * @throws ParameterException if parameter resolution fails
     */
    public Speed getVCrit() throws ParameterException
    {
        return getParameters().getParameter(MirovaParameters.vCrit);
    }

    /**
     * Returns the sensitivity parameter for social speed dynamics. * @return the value of socioSpeedSensitivity
     * @throws ParameterException if parameter resolution fails
     */
    public Double getSocioSpeedSensitivity() throws ParameterException
    {
        return getParameters().getParameter(MirovaParameters.socioSpeedSensitivity);
    }

    /**
     * Returns a map containing all relevant properties and states of the tactical planner. * @return key-value map of system
     * properties
     */
    public Map<String, Object> getProperties()
    {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("Speed [km/h]", getGtu().getSpeed());
        props.put("Acceleration [m/s²]", getGtu().getAcceleration());
        props.put("Current Desire", String.format("%.3f", this.absoluteDesire));
        props.put("Headway (relaxed)", getCurrentRelaxedHeadway());
        props.put("Lane Change Active", this.getLaneChange().isChangingLane());
        props.put("Active ActionState", this.currentActionState != null ? this.currentActionState.toString() : "none");
        return props;
    }

    /**
     * Gets the relaxation progress of the headway adaptation. * @return normalized relaxation progress [0,1]
     */
    public Double getRelaxProgress()
    {
        return this.relaxProgress;
    }

    /**
     * Gets the target desired headway duration. * @return the target desire duration
     */
    public Duration getTargetDesire()
    {
        return this.targetDesiredHeadway;
    }

    /**
     * Retrieves the simple operational plan calculated for the current tick. * @return the operational plan
     */
    public SimpleOperationalPlan getOperationalPlan()
    {
        return this.operationalPlan;
    }

    /**
     * Retrieves the parameters attached to this GTU. * @return the parameters object
     */
    public Parameters getParameters()
    {
        return this.params;
    }

    /**
     * Sets the socio speed pressure experienced by the GTU. * @param newValue the new socio speed pressure
     */
    public void setSocioSpeedPressure(final Double newValue)
    {
        this.socioSpeedPressure = newValue;
    }

    /**
     * Gets the socio speed pressure currently experienced by the GTU. * @return the current socio speed pressure
     */
    public Double getSocioSpeedPressure()
    {
        return this.socioSpeedPressure;
    }

    /**
     * Retrieves the time duration since the last lane change started. * @return the duration since the last lane change
     */
    public Duration getTimeSinceLastLaneChange()
    {
        return this.timeSinceLastLaneChange;
    }

    /**
     * Updates the tracker for the time since the last lane change. * @throws ParameterException if accessing the time step (DT)
     * parameter fails
     */
    public void updateTimeSinceLastLaneChange() throws ParameterException
    {
        if (this.laneChange.isChangingLane())
        {
            this.timeSinceLastLaneChange = Duration.ZERO;
        }
        else
        {
            this.timeSinceLastLaneChange = this.timeSinceLastLaneChange.plus(getParameters().getParameter(ParameterTypes.DT));
        }
    }
}
