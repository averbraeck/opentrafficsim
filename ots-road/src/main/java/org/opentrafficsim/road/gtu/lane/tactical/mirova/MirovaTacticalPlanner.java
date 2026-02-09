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
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.util.PatternSelector;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.network.*;
import org.opentrafficsim.road.network.speed.*;

import java.util.*;

/**
 * Abstract base vehicle for the MIROVA tactical framework.
 * <p>
 * Provides:
 * <ul>
 *   <li>Integration of LMRS-based tactical reasoning</li>
 *   <li>Voting arbiter for maneuver arbitration</li>
 *   <li>Central {@link VehicleContextManager} for contextual data handling</li>
 * </ul>
 * </p>
 */
public class MirovaTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    // ----------------------------------------------------------------------
    // Tactical and Planning Components
    // ----------------------------------------------------------------------

    protected boolean runningManeuver = false;
    protected ActionState currentActionState = null;

    protected SimpleOperationalPlan operationalPlan;
    protected final LaneChange laneChange;

    // ----------------------------------------------------------------------
    // LMRS Desire Dynamics
    // ----------------------------------------------------------------------
    /** Current total lateral desire vector (left/right). */
    protected Desire laneChangeDesire = Desire.zero();
    protected Desire mandatoryLaneChangeDesire = Desire.zero();
    protected Desire discretionaryLaneChangeDesire = Desire.zero();
    protected Double absoluteDesire = 0.0;
    protected Duration desireRelaxationTime = new Duration(0.0, DurationUnit.SI);

    private Double socioSpeedPressure = 0.0;

    /** Time since last lane change maneuver started. */
    private Duration timeSinceLastLaneChange = new Duration(0.0, DurationUnit.SI);

    /** */
    private Parameters params;

    // ----------------------------------------------------------------------
    /** Declarative knowledge base for this vehicle. */
    protected final List<KnowledgeChunk> knowledgeChunks = new ArrayList<>();
    /** Procedural knowledge: available exclusive maneuver patterns. */
    protected final List<ManeuverPattern> exclusiveManeuverPatterns = new ArrayList<>();
    /** Procedural knowledge: available parallel maneuver patterns. */
    protected final List<ManeuverPattern> parallelManeuverPatterns = new ArrayList<>();

    // ----------------------------------------------------------------------
    // Context Manager Integration
    // ----------------------------------------------------------------------

    /** Central contextual model for this vehicle. */
    private final VehicleContextManager contextManager;

     /// ----------------------------------------------------------------------
     // Headway Relaxation: Episode-based implementation (Schakel et al. 2012, 2023)
     // ----------------------------------------------------------------------

     /**
      * Relaxation time constant τ (seconds) controlling the adaptation speed
      * when the desired headway increases after a decrease in desire.
      * <p>Typical LMRS range: 20–30 s.</p>
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
     Duration createTime;



    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    public MirovaTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception) throws ParameterException
    {
        super(carFollowingModel, gtu, lanePerception);

        this.laneChange = Try.assign(() -> new LaneChange(gtu), "Parameter LCDUR is required.", GtuException.class);

//        this.laneChange.setDesiredLaneChangeDuration(getGtu().getParameters().getParameter(ParameterTypes.LCDUR));
//        this.currentRelaxedHeadway = gtu.getParameters().getParameter(ParameterTypes.T);

        // Initialize context manager
        this.contextManager = new VehicleContextManager(this);
        this.params = getGtu().getParameters();
        this.laneChange.setDesiredLaneChangeDuration(getGtu().getParameters().getParameter(ParameterTypes.LCDUR));

        this.createTime = gtu.getSimulator().getSimulatorTime();

    }

    @Override
    /**
     * Generates the operational plan for the current simulation step.
     * <p>
     * This method checks if the vehicle is fully positioned; if not, it returns
     * a default plan to skip the current step. Otherwise, it invokes the main
     * tactical update routine to compute the vehicle's behavior.
     * </p>
     *
     * @param startTime the start time of the operational plan
     * @param locationAtStartTime the location of the vehicle at the start time
     * @return the generated {@link OperationalPlan}
     * @throws GtuException if GTU-related errors occur
     * @throws NetworkException if network-related errors occur
     * @throws ParameterException if parameter access fails
     */
    public OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint2d locationAtStartTime)
            throws GtuException, NetworkException, ParameterException
    {
        Duration dt = getGtu().getParameters().getParameter(ParameterTypes.DT);
        SimpleOperationalPlan plan;
        Boolean justCreated = (startTime.si < this.createTime.si + 1.0);
        if (getGtu().getFront() == null || getGtu().getReferencePosition() == null || getGtu().getOperationalPlan() == null
                || justCreated) {
            // GTU noch nicht vollständig positioniert → überspringe diesen Takt
            Acceleration acc = getGtu().getCarFollowingAcceleration();
            plan =  new SimpleOperationalPlan(acc, dt);
        }

        else {
            plan = this.update();

        }

        return LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), startTime, plan, this.getLaneChange());
    }

    // ----------------------------------------------------------------------
    // Main Tactical Update
    // ----------------------------------------------------------------------
    /**
     * Executes one full tactical decision cycle for the MIROVA vehicle.
     * <p>
     * This method represents the central update routine that governs the vehicle’s
     * tactical behavior on a microscopic level. Each simulation step includes the
     * complete cognitive evaluation process consisting of perception, reasoning,
     * and tactical decision making.
     * </p>
     *
     * <h3>Process overview THIS IS PROBABLY OUTDATED</h3>
     * <ol>
     *   <li><b>Perception update:</b> The {@link VehicleContextManager} updates all contextual
     *       information (e.g., traffic state, neighboring vehicles, infrastructure).</li>
     *   <li><b>Desire computation:</b> Each {@link KnowledgeChunk} contributes a partial
     *       {@link Desire} component, which is combined into a total LMRS-style desire vector
     *       ({@code netDesire}) representing the current motivation for lateral maneuvers.</li>
     *   <li><b>Desire relaxation:</b> Temporal smoothing of lane-change motivation to avoid
     *       abrupt transitions between tactical decisions.</li>
     *   <li><b>Pattern selection:</b> Depending on the magnitude of {@code netDesire},
     *       the system evaluates available {@link ManeuverPattern}s in hierarchical order:
     *       <ul>
     *         <li><b>Tactical lane changes</b> — if {@code d > d_tactical}</li>
     *         <li><b>Free lane changes</b> — if {@code d > d_free}</li>
     *         <li><b>Cooperative maneuvers</b> — if no lane change is possible but interaction
     *             with other vehicles is required (e.g., gap creation, yielding)</li>
     *       </ul>
     *       If a free lane change is intended but infeasible (failed {@code checkAbility()}),
     *       cooperative maneuvers are automatically considered as fallback.</li>
     *   <li><b>Action execution:</b> The selected {@link ManeuverPattern} is activated by
     *       starting its initial {@link ActionState}, which returns a corresponding
     *       {@link SimpleOperationalPlan}.</li>
     *   <li><b>Default behavior:</b> If no pattern is applicable, standard car-following is
     *       continued on the current lane.</li>
     * </ol>
     *
     * <p>
     * The resulting {@link SimpleOperationalPlan} specifies the longitudinal and lateral
     * acceleration targets for the current simulation step. Individual maneuver patterns
     * are responsible for enforcing their own physical constraints and consistency.
     * </p>
     *
     * @return the {@link SimpleOperationalPlan} representing the vehicle’s tactical decision
     *         for the current time step
     * @throws ParameterException if a parameter lookup fails during desire or ability checks
     * @throws NullPointerException if required perception or context data are unavailable
     * @throws IllegalArgumentException if a consistency condition is violated
     * @throws NetworkException
     * @throws GtuException
     */

    public SimpleOperationalPlan update()
            throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException
    {
        if (this.currentRelaxedHeadway == null) {
            this.currentRelaxedHeadway = this.getGtu().getParameters().getParameter(ParameterTypes.T);
        }

          // 1️. Update perception and contextual information
        //this.getPerception().getPerceptionCategory(DirectDefaultSimplePerception.class).updateForwardHeadwayGtu();
        this.contextManager.advanceTick();
        updateTimeSinceLastLaneChange();
        NeighborsContext neighborsContext = getContextManager().getCategory("Neighbors", NeighborsContext.class);
        neighborsContext.getFrontGapDeltaSpeed(LateralDirectionality.NONE);; // ensure headway GTU are updated

        // 2. Compute current LMRS-style net desire (aggregated from all knowledge chunks)
        updateLaneChangeDesire();


        // 3️. Derive a single scalar desire magnitude for car-following adjustments
        this.absoluteDesire = this.laneChangeDesire.magnitude();

        // 4️. Apply temporal relaxation (gradual decay of short-term motivation and headway adaptation)
        updateTargetDesiredHeadway();
        updateCurrentRelaxedHeadway();

        // 5️. Reset operational plan for this time step
        this.operationalPlan = null;

        // 6. determine operational plan
        // 6.1 check if maneuver is running and continue if so
        if (this.runningManeuver)
        {
            this.operationalPlan = this.currentActionState.update();
        }
        else {
            // 6.2 check if situation requires new exclusive maneuver
            ManeuverPattern selectedPattern = null;
            selectedPattern = selectPatternByType(getExclusiveManeuverPatterns());
            // Execute exclusive maneuver if selected
            if (selectedPattern != null) {
                this.operationalPlan = selectedPattern.update();
                this.currentActionState = selectedPattern.getCurrentActionState();
            }

            // 6.3 if exclusive maneuver not necessary, proceed with parallel maneuver patterns
            else {
                ArrayList<ManeuverPattern> parallelPatterns = PatternSelector.getAllRelevantPatterns(getParallelManeuverPatterns());

                // Evaluate all applicable parallel patterns and select the one with the most restrictive acceleration
                for (ManeuverPattern pattern : parallelPatterns) {
                    SimpleOperationalPlan plan = pattern.update();
                    if (this.operationalPlan == null) {
                        this.operationalPlan = plan;
                        this.currentActionState = pattern.getCurrentActionState();

                    } else {
                        if (this.operationalPlan.isLaneChange() && !plan.isLaneChange()) {
                            // keep lane change over non-lane change
                            continue;
                        }
                        else if (!this.operationalPlan.isLaneChange() && plan.isLaneChange()) {
                            // prefer lane change over non-lane change
                            this.operationalPlan = plan;
                            this.currentActionState = pattern.getCurrentActionState();
                        }
                        else if (plan.getAcceleration().lt(this.operationalPlan.getAcceleration())) {
                            // prefer more restrictive acceleration
                            this.operationalPlan = plan;
                            this.currentActionState = pattern.getCurrentActionState();
                        }
                    }
                }

                // If no parallel pattern produced a plan, fall back to car-following
                EgoContext egoContext = getContextManager().getCategory("Ego", EgoContext.class);
                Acceleration cfAcceleration = egoContext.getCurrentCarFollowingAcceleration();
                if (this.operationalPlan == null || this.operationalPlan.getAcceleration().gt(cfAcceleration)) {
                    // Default: continue standard following (no tactical action)
                    this.operationalPlan = new SimpleOperationalPlan(
                            cfAcceleration,
                            this.getGtu().getParameters().getParameter(ParameterTypes.DT),
                            LateralDirectionality.NONE
                    );
                }
            }
        }

//
//        // 6️. If a maneuver is already running → continue executing it
//        if (this.runningManeuver && this.currentActionState != null)
//        {
//            this.operationalPlan = this.currentActionState.update();
//        }
//        else
//        {
//            // 7️. Hierarchical pattern selection based on Desire → Context → Ability
//            // Pattern selection is delegated to PatternSelector for modular context evaluation.
//            ManeuverPattern selectedPattern = null;
//
//            double dFree = this.getDFree();       // threshold for free lane changes
//            double dTactical = this.getDMand();   // threshold for tactical lane changes
//
//            // --- 7.1️. Tactical lane change (highest priority) ---
//            if (this.laneChangeDesire.magnitude() >= dTactical)
//            {
//                selectedPattern = selectPatternByType(PatternType.TACTICAL_LC);
//            }
//
//            // --- 7.2️. Free lane change ---
//            else if (this.laneChangeDesire.magnitude() >= dFree)
//            {
//                // Try a standard free lane change first
//                selectedPattern = selectPatternByType(PatternType.FREE_LC);
//
//                // If not feasible → fall back to cooperative patterns
//                if (selectedPattern == null)
//                {
//                    selectedPattern = selectPatternByType(PatternType.COOPERATIVE);
//                }
//            }
//
//            // --- 7.3️. Cooperative behavior (no strong desire or fallback) ---
//            else
//            {
//                selectedPattern = selectPatternByType(PatternType.COOPERATIVE);
//            }
//
//            // 8️. Execute selected pattern or continue with default car-following
//            if (selectedPattern != null)
//            {
//                this.currentActionState = selectedPattern.getInitialActionState();
//                this.operationalPlan = this.currentActionState.update();
//            }
//
//        }
//
//        if (this.operationalPlan == null)
//        {
//             // Default: continue standard following (no tactical action)
//                this.operationalPlan = new SimpleOperationalPlan(
//                        //computeLongitudinalAcceleration()
//                        getContextManager().getCategory("Ego", EgoContext.class).getCurrentCarFollowingAcceleration(),
//                    this.getGtu().getParameters().getParameter(ParameterTypes.DT),
//                    LateralDirectionality.NONE
//                );
//        }
//
//        else
//        {
//            getContextManager().getCategory("Ego", EgoContext.class).cacheValue(EgoContext.CURRENT_CF_ACCELERATION, this.operationalPlan.getAcceleration(), true);
//        }
//
//
//
//        if (getContextManager().getCategory("Ego", EgoContext.class).getCurrentCarFollowingAcceleration().si < -8.0  || getContextManager().getCategory("Ego", EgoContext.class).getCurrentCarFollowingAcceleration().eq(Acceleration.NEGATIVE_INFINITY)
//                || getContextManager().getCategory("Ego", EgoContext.class).getCurrentCarFollowingAcceleration().le(Acceleration.NEG_MAXVALUE)
//                )
//        {
//            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leader = getPerception().getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
//            if (!leader.isEmpty())
//            {
//                System.out.printf("GTU: %s @simsec: %s -> Leader: %s distance=%s speed=%s%n", getGtu().getId(), getGtu().getSimulator().getSimulatorTime().toDisplayString(),  leader.first().getId(), leader.first().getDistance().toDisplayString(), leader.first().getSpeed().toDisplayString());
//            }
//
//            else
//            {
//                System.out.println("GTU: " + getGtu().getId() + ": No leader detected.");
//            }
        /*
        if (getGtu().getId().equals("6"))
        {

            String activeActionState = (this.currentActionState != null) ? this.currentActionState.toString() : "none";
            NeighborsContext neighborsContext = getContextManager().getCategory("Neighbors", NeighborsContext.class);
            SpeedLimitInfo currentSpeedLimitInfo = getPerception().getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(new Length(0.0, LengthUnit.SI));
            Speed desired = getCarFollowingModel().desiredSpeed(getGtu().getParameters(), currentSpeedLimitInfo);
            System.out.printf("%s type=%s vMaxVeh=%s desired=%s%n activeActionState=%s laneChangeDesire=[left=%.3f,right=%.3f]%n headway=%.3f%n lane=%s%n isChangingLane=%b%n PlanisLaneChange=%b PlanDir=%s%n RightOvertakingAhead=%b%n LCPossibleRight=%b%n LCPossibleLeft=%b%n   frontGapDist=%s frontGapDistLeft=%s frontGapDeltaV=%s frontGapDeltaVLeft=%s%n%n MandatoryLCDesireLeft=%.3f%n",
                getGtu(), getGtu().getType().getId(),
                SpeedLimitUtil.getMaximumVehicleSpeed(currentSpeedLimitInfo),
                desired,
                activeActionState,
                this.laneChangeDesire.getLeft(),
                this.laneChangeDesire.getRight(),
                getCurrentRelaxedHeadway().si,
                this.getGtu().getLane().toString(),
                this.laneChange.isChangingLane(),
                this.operationalPlan.isLaneChange(),
                this.operationalPlan.getLaneChangeDirection(),
                neighborsContext.getRightSideOvertakingAhead(),
                neighborsContext.getIfLaneChangePossible(LateralDirectionality.RIGHT),
                neighborsContext.getIfLaneChangePossible(LateralDirectionality.LEFT),
                neighborsContext.getFrontGapDistance(LateralDirectionality.NONE),
                neighborsContext.getFrontGapDistance(LateralDirectionality.LEFT),
                neighborsContext.getFrontGapDeltaSpeed(LateralDirectionality.NONE),
                neighborsContext.getFrontGapDeltaSpeed(LateralDirectionality.LEFT),
                getMandatoryLaneChangeDesire().getLeft()
                )
                ;

           }
        */

        if (this.operationalPlan.getIndicatorIntent().isLeft())
        {
            getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.LEFT);
            }
        else if (this.operationalPlan.getIndicatorIntent().isRight())
        {
            getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.RIGHT);
            }
        else if (getLaneChangeDesire().magnitude() > getDFree()) {
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


        Acceleration planAcc = this.operationalPlan.getAcceleration();

            if (planAcc.si < -8.0  || planAcc.eq(Acceleration.NEGATIVE_INFINITY)
                    || planAcc.le(Acceleration.NEG_MAXVALUE))
            {
                System.out.printf("GTU: %s @simsec: %s -> Plan acceleration: %s, ActionState: %s%n",
                        getGtu().getId(),
                        getGtu().getSimulator().getSimulatorTime().toDisplayString(),
                        planAcc.toDisplayString(), (this.currentActionState != null) ? this.currentActionState.toString() : "none");
            }
        return this.operationalPlan;
    }

    /**
     * Selects a maneuver pattern of the specified type using the {@link PatternSelector}.
     *
     * @param patterns the list of candidate maneuver patterns
     * @return the selected maneuver pattern, or null if none is applicable
     * @throws ParameterException if pattern selection fails due to parameter issues
     */
    protected ManeuverPattern selectPatternByType(final ArrayList<ManeuverPattern> patterns) throws ParameterException {
        return PatternSelector.select(patterns);
    }

    /**
     * Returns all {@link KnowledgeChunk}s currently assigned to this vehicle.
     * These represent the declarative knowledge influencing tactical reasoning.
     *
     * @return list of all knowledge chunks
     */
    public List<KnowledgeChunk> getKnowledgeChunks() {
        return this.knowledgeChunks;
    }

    /**
     * Registers a new {@link KnowledgeChunk} to this vehicle.
     * This method is typically called in the constructor of the concrete vehicle class.
     *
     * @param chunk the knowledge chunk to add
     */
    public void addKnowledgeChunk(final KnowledgeChunk chunk) {
        if (chunk != null && !this.knowledgeChunks.contains(chunk)) {
            this.knowledgeChunks.add(chunk);
        }
    }
    /**
     * Registers a new exclusive {@link ManeuverPattern} to this vehicle.
     * Exclusive patterns represent maneuvers that cannot be combined with others.
     * This method is typically called in the constructor of the concrete vehicle class.
     *
     * @param pattern the exclusive maneuver pattern to add
     */
    public void addExclusiveManeuverPattern(final ManeuverPattern pattern) {
        if (pattern != null && !this.exclusiveManeuverPatterns.contains(pattern)) {
            this.exclusiveManeuverPatterns.add(pattern);
        }
    }

    /**
     * Registers a new parallel {@link ManeuverPattern} to this vehicle.
     * Parallel patterns represent maneuvers that can be combined with others.
     * This method is typically called in the constructor of the concrete vehicle class.
     *
     * @param pattern the parallel maneuver pattern to add
     */
    public void addParallelManeuverPattern(final ManeuverPattern pattern) {
        if (pattern != null && !this.parallelManeuverPatterns.contains(pattern)) {
            this.parallelManeuverPatterns.add(pattern);
        }
        }

    /**
     * Returns all registered exclusive {@link ManeuverPattern}s for this vehicle.
     *
     * @return list of exclusive maneuver patterns
     */
    public ArrayList<ManeuverPattern> getParallelManeuverPatterns() {
        return new ArrayList<>(this.parallelManeuverPatterns);
        }


    /**
     * Returns all registered exclusive {@link ManeuverPattern}s for this vehicle.
     * @return list of exclusive maneuver patterns
     */
    public ArrayList<ManeuverPattern> getExclusiveManeuverPatterns() {
        return new ArrayList<>(this.exclusiveManeuverPatterns);
        }


     // ----------------------------------------------------------------------
     // LMRS Desire Integration
     // ----------------------------------------------------------------------



     /**
      * Computes and updates the total (mandatory + discretionary) desire vector
      * for this vehicle based on all active {@link KnowledgeChunk}s.
      * <p>
      * The result represents the LMRS-style aggregated motivation for lane changing,
      * which can later be used for tactical decisions (e.g., thresholding, maneuver selection).
      * </p>
      *
      * @throws ParameterException if any chunk’s desire computation fails
     * @throws NetworkException
     * @throws GtuException
      */
     protected void updateLaneChangeDesire() throws ParameterException, GtuException, NetworkException
     {
         this.mandatoryLaneChangeDesire = Desire.zero();
         this.discretionaryLaneChangeDesire = Desire.zero();

         // collect all desires from active chunks
         for (KnowledgeChunk chunk : this.getKnowledgeChunks())
         {
             if (chunk.isApplicable() == true)
             {  Desire d = chunk.computeDesire();
                 if (d.isMandatory())
                     this.mandatoryLaneChangeDesire = this.mandatoryLaneChangeDesire.add(d);
                 else
                     this.discretionaryLaneChangeDesire = this.discretionaryLaneChangeDesire.add(d);}
         }

         // combine mandatory + discretionary using LMRS weighting per direction
         double dSync = this.getDMand(); // or specific param from Parameters
         double dCoop = this.getDFree(); // typical LMRS thresholds

         this.laneChangeDesire = Desire.combine(this.mandatoryLaneChangeDesire, this.discretionaryLaneChangeDesire, dSync, dCoop);
     }

     /** Returns the current combined LMRS desire. */
     public Desire getLaneChangeDesire() {
         return this.laneChangeDesire;
     }


    // ----------------------------------------------------------------------
    // Context Handling
    // ----------------------------------------------------------------------

    /** Updates all registered context categories once per simulation tick. */
    public void updateContext() {
        this.contextManager.updateFromPerception();
    }

    /** Returns the central vehicle context manager. */
    public VehicleContextManager getContextManager() {
        return this.contextManager;
    }

    /** Generic accessor for a full context category. */
    public <T extends ContextCategory> T getContext(final Class<T> clazz) {
        for (ContextCategory cat : this.contextManager.getAllCategories().values()) {
            if (clazz.isInstance(cat)) {
                return clazz.cast(cat);
            }
        }
        return null;
    }

    /** Generic accessor for a specific value in a context category. */
    public <T> T getContextValue(final String categoryName, final String key, final Class<T> clazz) {
        ContextCategory cat = this.contextManager.getCategory(categoryName, ContextCategory.class);
        return cat != null ? cat.getValue(key, clazz) : null;
    }


    /**
     * Returns the free driving time in the specified lane change direction. This method iterates through all leaders in the
     * specified direction and calculates the minimum free driving time based on their speed and distance.
     * @param laneChangeDirection The direction of the lane change (LEFT or RIGHT).
     * @return The free driving time available for a lane change.
     * @throws ParameterException
     * @throws OperationalPlanException
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

    // Getter und Setter für runningManeuver
    public boolean isRunningManeuver()
    {
        return this.runningManeuver;
    }

    public void setRunningManeuver(final boolean runningManeuver)
    {
        this.runningManeuver = runningManeuver;
    }

    // Getter und Setter für currentActionState
    public ActionState getCurrentActionState()
    {
        return this.currentActionState;
    }

    public void setCurrentActionState(final ActionState currentActionState)
    {
        this.currentActionState = currentActionState;
    }


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
      * This method fixes the starting and target headways and resets the progress
      * accumulator. It should be called whenever a significant change in the target
      * headway occurs (e.g., due to a large change in lane-change desire).
      * </p>
      *
      * @param newTarget the new target headway that should be reached after τ seconds
      */
     private void startHeadwayRelaxation(final Duration newTarget) {
         if (this.currentRelaxedHeadway == null) {
             this.currentRelaxedHeadway = newTarget;
         }
         this.headwayAtRelaxStart = this.currentRelaxedHeadway;
         this.targetAtRelaxStart = newTarget;
         this.relaxProgress = 0.0;
     }


     /**
      * Updates the instantaneous target headway from the current lane-change desire.
      * <p>
      * If the target headway differs significantly from the previous value, a new
      * relaxation episode is started to ensure a smooth transition.
      * </p>
      *
      * @throws ParameterException if parameter access fails
      */
     protected void updateTargetDesiredHeadway() throws ParameterException {
         final Parameters params = this.getGtu().getParameters();
         final double limitedDesire = Math.max(0.0, Math.min(1.0, getSocioSpeedPressure()));

         final double tMin = params.getParameter(ParameterTypes.TMIN).si;
         final double tMax = params.getParameter(ParameterTypes.TMAX).si;
         final Duration newTarget = Duration.instantiateSI(limitedDesire * tMin + (1.0 - limitedDesire) * tMax);

         // Start a new relaxation episode only if the target changes significantly
         final double EPS = 0.05; // 50 ms tolerance to avoid jitter
         if (this.targetDesiredHeadway == null || Math.abs(newTarget.si - this.targetDesiredHeadway.si) > EPS) {
             this.targetDesiredHeadway = newTarget;
             startHeadwayRelaxation(newTarget);
         } else {
             this.targetDesiredHeadway = newTarget;
         }
     }

     /**
      * Sets a new target desired headway if it is smaller than the current target.
      * @param newTargetDesiredHeadway the new target desired headway
      */
     public void setTargetDesiredHeadway(final Duration newTargetDesiredHeadway) {
         if (newTargetDesiredHeadway.le(this.targetDesiredHeadway)) {
             this.targetDesiredHeadway = newTargetDesiredHeadway;
             startHeadwayRelaxation(newTargetDesiredHeadway);
         }
     }


     /**
      * Progresses the relaxed headway T(t) toward the episode’s target using a
      * normalized progress accumulator. The adaptation follows a linear schedule
      * that reaches the target exactly after τ seconds.
      * <p>
      * When the target headway is smaller than the current one (i.e., higher desire),
      * adaptation occurs immediately without relaxation.
      * </p>
      *
      * @throws ParameterException if parameter access fails
      */
     protected void updateCurrentRelaxedHeadway() throws ParameterException {
         // Initialize if not yet set
         if (this.currentRelaxedHeadway == null) {
             if (this.targetDesiredHeadway == null) {
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
         if (this.tauHeadway.si == 0.0 || this.targetDesiredHeadway.si <= this.currentRelaxedHeadway.si) {
             this.currentRelaxedHeadway = this.targetDesiredHeadway;
             this.headwayAtRelaxStart = this.currentRelaxedHeadway;
             this.targetAtRelaxStart = this.targetDesiredHeadway;
             this.relaxProgress = 1.0;
         } else {
             // Linear progression: reach target exactly after τ seconds
             this.relaxProgress = Math.min(1.0, this.relaxProgress + dt.si / this.tauHeadway.si);
             final double startT = this.headwayAtRelaxStart.si;
             final double deltaT = this.targetAtRelaxStart.si - startT;
             final double newT = startT + this.relaxProgress * deltaT;
             this.currentRelaxedHeadway = Duration.instantiateSI(newT);
         }

         // Update GTU parameters for use by the car-following model
         final Parameters params = this.getGtu().getParameters();
         params.setParameterResettable(ParameterTypes.T, this.currentRelaxedHeadway);
     }


     /**
      * Sets a new headway relaxation time constant τ (seconds).
      * A value of zero disables relaxation and applies all headway
      * changes immediately.
      *
      * @param tauSeconds the new relaxation time constant in seconds
      */
     public void setHeadwayRelaxationTime(final double tauSeconds) {
         this.tauHeadway = Duration.instantiateSI(Math.max(0.0, tauSeconds));
     }


     /**
      * Returns the current headway relaxation time constant τ.
      *
      * @return the relaxation time constant
      */
     public Duration getHeadwayRelaxationTime() {
         return this.tauHeadway;
     }


     /**
      * Returns the currently active relaxed headway used by the car-following model.
      *
      * @return the relaxed headway T(t)
      */
     public Duration getCurrentRelaxedHeadway() {
         return this.currentRelaxedHeadway;
     }


     /**
      * Returns the most recent target headway value derived from the current desire.
      *
      * @return the target headway T<sub>target</sub>
      */
     public Duration getTargetDesiredHeadway() {
         return this.targetDesiredHeadway;
     }

     /**
      * Computes the longitudinal acceleration for the current time step,
      * considering all tactical and environmental influences provided
      * via the vehicle’s {@link VehicleContextManager}.
      * <p>
      * The following control components are considered:
      * <ul>
      *   <li><b>Leader-following behavior:</b> based on perceived headway</li>
      *   <li><b>Lane-end braking:</b> deceleration to enforce timely merging</li>
      *   <li><b>Speed-limit adaptation:</b> handling of upcoming lower speed limits</li>
      *   <li><b>Free-flow acceleration:</b> baseline term for unconstrained motion</li>
      *   <li><b>Transition effects:</b> optional curvature or bump-based deceleration</li>
      * </ul>
      * The resulting acceleration is the minimum (most restrictive) value among all components.
      * </p>
      *
      * @return final longitudinal acceleration [m/s²]
      * @throws ParameterException         if parameter retrieval fails
     * @throws NetworkException
     * @throws GtuException
      */
     public Acceleration computeLongitudinalAcceleration()
             throws ParameterException, GtuException, NetworkException
     {
         // ----------------------------------------------------------------------
         // 1️⃣ Retrieve context and parameters
         // ----------------------------------------------------------------------
         VehicleContextManager ctx = this.getContextManager();
         InfrastructureContext infra = ctx.getCategory("Infrastructure", InfrastructureContext.class);
         Parameters params = this.getGtu().getParameters();
         CarFollowingModel cfModel = this.getCarFollowingModel();

         // Ego and environment data from context
         Speed egoSpeed = getContextManager().getCategory("Ego", EgoContext.class).getEgoSpeed();

         SpeedLimitInfo currentSpeedLimitInfo =  getPerception()
                 .getPerceptionCategory(InfrastructurePerception.class)
                 .getSpeedLimitProspect(RelativeLane.CURRENT)
                 .getSpeedLimitInfo(Length.ZERO);
         SpeedLimitInfo nextLimit = infra.getNextSpeedLimit();
         Speed legalSpeed = infra.getLegalSpeedLimit();
         Length laneEndDist = infra.getDistanceToLaneEnd();

         // Apply relaxed headway (from Desire relaxation)
         params.setParameterResettable(ParameterTypes.T, this.getCurrentRelaxedHeadway());

         // Candidate accelerations (we’ll take the minimum)
         List<Acceleration> candidates = new ArrayList<>();

         // ----------------------------------------------------------------------
         // 2️⃣ Free acceleration (baseline)
         // ----------------------------------------------------------------------
         //Acceleration aFree = CarFollowingUtil.freeAcceleration(cfModel, params, egoSpeed, currentSpeedLimitInfo);
         //candidates.add(aFree);

         // ----------------------------------------------------------------------
         // 3️⃣ Leader-following (if a leader is detected)
         // ----------------------------------------------------------------------

         Headway leader = this.getPerception()
                 .getPerceptionCategory(DirectDefaultSimplePerception.class)
                 .getForwardHeadwayGtu();
         NeighborsPerception neighbors =
                 getPerception().getPerceptionCategory(NeighborsPerception.class);
         PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders =
                 neighbors.getLeaders(RelativeLane.CURRENT);

         Acceleration aCf = getCarFollowingModel().followingAcceleration(
                 params,
                 egoSpeed,
                 currentSpeedLimitInfo,
                 leaders
             );

         candidates.add(aCf);


//         if (leader != null)
//         {
//             Acceleration aLeader = CarFollowingUtil.followSingleLeader(
//                     cfModel,
//                     params,
//                     egoSpeed,
//                     currentSpeedLimitInfo,
//                     leader.getDistance(),
//                     leader.getSpeed() != null ? leader.getSpeed() : Speed.ZERO);
//             candidates.add(aLeader);
//         }

         // ----------------------------------------------------------------------
         // 4️⃣ Lane-end braking (encourage merging)
         // ----------------------------------------------------------------------
         if (infra.isLaneEndUrgent())
         {
             Acceleration aLaneEnd = CarFollowingUtil.stop(
                     cfModel, params, egoSpeed, currentSpeedLimitInfo, laneEndDist);

             // Only consider strong braking responses (to avoid minor fluctuations)
             if (aLaneEnd.ge(params.getParameter(ParameterTypes.BCRIT).times(0.95)) && aLaneEnd!= null)
             {
                 candidates.add(aLaneEnd);
             }
         }

         // ----------------------------------------------------------------------
         // 5️⃣ Transition deceleration (e.g., curvature or bumps)
         // ----------------------------------------------------------------------
         // Still computed here (not inside context), for flexibility.
         Acceleration aTrans = SpeedLimitUtil.considerSpeedLimitTransitions(
                 params, egoSpeed,
                 getPerception().getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(RelativeLane.CURRENT),
                 cfModel);
         if (aTrans != null && aTrans.lt(Acceleration.POSITIVE_INFINITY))
         {
             candidates.add(aTrans);
         }
         // ----------------------------------------------------------------------
         // 6️⃣ Upcoming lower speed limit ahead
         // ----------------------------------------------------------------------
         if (nextLimit != null)
         {
             Speed nextLegal = SpeedLimitUtil.getLegalSpeedLimit(nextLimit);
             if (nextLegal.lt(legalSpeed))
             {
                 Length distanceToLimit = new Length(200.0, LengthUnit.SI);
                 Acceleration aLimit = CarFollowingUtil.approachTargetSpeed(
                         cfModel, params, egoSpeed, nextLimit, distanceToLimit, nextLegal);
                 if (aLimit != null)
                 {
                     candidates.add(aLimit);

                 }
             }
         }
         // ----------------------------------------------------------------------
         // 7️⃣ Compute most restrictive acceleration
         // ----------------------------------------------------------------------
         Acceleration finalAcc = candidates.stream()
                 .min(Acceleration::compareTo)
                 .orElse(aCf);

//         if (finalAcc == null || finalAcc.equals(Acceleration.NEGATIVE_INFINITY) || finalAcc.equals(Acceleration.NEG_MAXVALUE) || finalAcc.si < -8.0)
//         {
//             System.out.println("finalAcc is " + finalAcc.toString() + " for gtu " + getGtu().getId() + " with properties: "
//                     + egoSpeed.toString() + ", " + currentSpeedLimitInfo.toString()+ ", " + getContextManager().getCategory("Neighbors", NeighborsContext.class).toString());
//             System.out.println("candidates: " + candidates.toString());
//         }

         return finalAcc;
     }


    public Double getDesire()
    {
        return this.absoluteDesire;
    }

    public void setDesire(final Double desire, final Duration desireRelaxationTime)
    {
        this.absoluteDesire = desire;
        this.desireRelaxationTime = desireRelaxationTime;
    }

    public Desire getMandatoryLaneChangeDesire()
    {
        return this.mandatoryLaneChangeDesire;
    }

    public Desire getDiscretionaryLaneChangeDesire()
    {
        return this.discretionaryLaneChangeDesire;
    }

    /**
     * Returns the free driving distance constant.
     * @return the value of DFREE
     * @throws ParameterException
     */
    public double getDFree() throws ParameterException {
        return getParameters().getParameter(MirovaParameters.DFREE);
    }

    /**
     * Returns the mandatory driving distance constant.
     * @return the value of DMAND
     * @throws ParameterException
     */
    public double getDMand() throws ParameterException {
        return getParameters().getParameter(MirovaParameters.DMAND);
    }

    /**
     * Returns the speed difference threshold (vGain) used in LMRS.
     * @return the value of vGain
     * @throws ParameterException
     */
    public Speed getVGain() throws ParameterException {
        return getParameters().getParameter(MirovaParameters.vGain);
    }

    /**
     * Returns the critical speed threshold (vCrit) used in LMRS.
     * @return the value of vCrit
     * @throws ParameterException
     */
    public Speed getVCrit() throws ParameterException {
        return getParameters().getParameter(MirovaParameters.vCrit);
    }

    /**
     * Returns the sensitivity parameter for social speed dynamics.
     * @return the value of socioSpeedSensitivity
     * @throws ParameterException
     */
    public Double getSocioSpeedSensitivity() throws ParameterException {
        return getParameters().getParameter(MirovaParameters.socioSpeedSensitivity);
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("Speed [km/h]", getGtu().getSpeed());
        props.put("Acceleration [m/s²]", getGtu().getAcceleration());
        props.put("Current Desire", String.format("%.3f", this.absoluteDesire));
        props.put("Headway (relaxed)", getCurrentRelaxedHeadway());
        props.put("Lane Change Active", this.getLaneChange().isChangingLane());
        props.put("Active ActionState",
                  this.currentActionState != null ? this.currentActionState.toString() : "none");
        return props;
    }

    public Double getRelaxProgress() {
        return this.relaxProgress;
    }

    public Duration getTargetDesire() {
        return this.targetDesiredHeadway;
    }


    /**
     * @return
     */
    public SimpleOperationalPlan getOperationalPlan() {
        return this.operationalPlan;
    }

    /**
     * @return
     */
    public Parameters getParameters() {
        return this.params;
    }

    public void setSocioSpeedPressure(final Double newValue) {
        this.socioSpeedPressure = newValue;
    }

    public Double getSocioSpeedPressure() {
        return this.socioSpeedPressure;
    }

    public Duration getTimeSinceLastLaneChange() {
        return this.timeSinceLastLaneChange;
    }

    public void updateTimeSinceLastLaneChange() throws ParameterException {
        if (this.laneChange.isChangingLane())
        {
            this.timeSinceLastLaneChange = Duration.ZERO;
        }
        else
        {
            this.timeSinceLastLaneChange = this.timeSinceLastLaneChange.plus(getParameters().getParameter(ParameterTypes.DT));
            }
    }


//    /**
//     * @param params
//     */
//    public void setParameters(final Parameters params) {
//        this.params = params;
//    }

}
