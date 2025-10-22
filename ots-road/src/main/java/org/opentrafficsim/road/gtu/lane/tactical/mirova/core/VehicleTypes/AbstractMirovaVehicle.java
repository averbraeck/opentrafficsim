package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.*;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.*;
import org.opentrafficsim.core.gtu.*;
import org.opentrafficsim.core.gtu.perception.*;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.*;
import org.opentrafficsim.road.gtu.lane.perception.*;
import org.opentrafficsim.road.gtu.lane.perception.categories.*;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.*;
import org.opentrafficsim.road.gtu.lane.plan.operational.*;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.VotingArbiter.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern.PatternType;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.*;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
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
public abstract class AbstractMirovaVehicle
{
    // ----------------------------------------------------------------------
    // Tactical and Planning Components
    // ----------------------------------------------------------------------

    protected final VotingArbiter votingArbiter;
    protected final MirovaTacticalPlanner tacticalPlanner;
    protected boolean runningManeuver = false;
    protected ActionState currentActionState = null;
    protected final CarFollowingModel carFollowingModel;
    protected final LaneBasedGtu gtu;
    protected final LanePerception lanePerception;
    protected SimpleOperationalPlan operationalPlan;
    protected final LaneChange laneChange;

    // ----------------------------------------------------------------------
    // LMRS Desire Dynamics
    // ----------------------------------------------------------------------

    protected Double absoluteDesire = 0.0;
    protected Duration desireRelaxationTime = new Duration(0.0, DurationUnit.SI);
    private final double DFREE = 0.365;
    private final double DMAND = 0.577;
    private static final Speed vGain = Speed.instantiateSI(20.0);
    private static final Speed vCrit = Speed.instantiateSI(16.7);
    private static final Double socioSpeedSensitivity = 0.25;

    // ----------------------------------------------------------------------
    /** Declarative knowledge base for this vehicle. */
    protected final List<KnowledgeChunk> knowledgeChunks = new ArrayList<>();

    // ----------------------------------------------------------------------
    // Context Manager Integration
    // ----------------------------------------------------------------------

    /** Central contextual model for this vehicle. */
    private final VehicleContextManager contextManager;


    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    public AbstractMirovaVehicle(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
                                 final LanePerception lanePerception, final MirovaTacticalPlanner tacticalPlanner)
            throws OperationalPlanException
    {
        this.votingArbiter = new VotingArbiter();
        this.tacticalPlanner = tacticalPlanner;
        this.carFollowingModel = carFollowingModel;
        this.gtu = gtu;
        this.lanePerception = lanePerception;
        this.laneChange = Try.assign(() -> new LaneChange(gtu), "Parameter LCDUR is required.", GtuException.class);

        // Initialize context manager
        this.contextManager = new VehicleContextManager(this);
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
     * <h3>Process overview</h3>
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
     * @throws OperationalPlanException if no valid operational plan can be generated
     * @throws ParameterException if a parameter lookup fails during desire or ability checks
     * @throws NullPointerException if required perception or context data are unavailable
     * @throws IllegalArgumentException if a consistency condition is violated
     */

    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        // 1️. Update perception and contextual information
        this.contextManager.advanceTick();

        // 2. Compute current LMRS-style net desire (aggregated from all knowledge chunks)
        updateLaneChangeDesire();

        // 3️. Derive a single scalar desire magnitude for car-following adjustments
        this.absoluteDesire = this.laneChangeDesire.magnitude();

        // 4️. Apply temporal relaxation (gradual decay of short-term motivation)
        updateDesire();

        // 5️. Reset operational plan for this time step
        this.operationalPlan = null;

        // 6️. If a maneuver is already running → continue executing it
        if (this.runningManeuver && this.currentActionState != null)
        {
            this.operationalPlan = this.currentActionState.update();
        }
        else
        {
            // 7️. Hierarchical pattern selection based on Desire → Context → Ability
            ManeuverPattern selectedPattern = null;

            double dFree = this.getDFree();       // threshold for free lane changes
            double dTactical = this.getDMand();   // threshold for tactical lane changes

            // --- 7.1️. Tactical lane change (highest priority) ---
            if (this.laneChangeDesire.magnitude() > dTactical)
            {
                selectedPattern = selectPatternByType(PatternType.TACTICAL_LC);
            }

            // --- 7.2️. Free lane change ---
            else if (this.laneChangeDesire.magnitude() > dFree)
            {
                // Try a standard free lane change first
                selectedPattern = selectPatternByType(PatternType.FREE_LC);

                // If not feasible → fall back to cooperative patterns
                if (selectedPattern == null)
                {
                    selectedPattern = selectPatternByType(PatternType.COOPERATIVE);
                }
            }

            // --- 7.3️. Cooperative behavior (no strong desire or fallback) ---
            else
            {
                selectedPattern = selectPatternByType(PatternType.COOPERATIVE);
            }

            // 8️. Execute selected pattern or continue with default car-following
            if (selectedPattern != null)
            {
                this.currentActionState = selectedPattern.getInitialActionState();
                this.operationalPlan = this.currentActionState.update();
            }
            else
            {
                // Default: continue standard following (no tactical action)
                this.operationalPlan = new SimpleOperationalPlan(
                    getMinAcceleration(),
                    this.getGtu().getParameters().getParameter(ParameterTypes.DT)
                );
            }
        }
        return this.operationalPlan;
    }

    /**
     * Selects the first applicable maneuver pattern of the given type
     * by evaluating both contextual and physical feasibility conditions.
     *
     * @param type the pattern type to evaluate
     * @return the first feasible pattern, or {@code null} if none match
     * @throws ParameterException if parameter access fails
     */
    protected ManeuverPattern selectPatternByType(final PatternType type) throws ParameterException
    {
        for (KnowledgeChunk chunk : this.knowledgeChunks)
        {
            for (var supplier : chunk.getManeuverPatterns())
            {
                ManeuverPattern pattern = supplier.get();
                if (pattern.getType() == type &&
                    pattern.checkContext() &&
                    pattern.checkAbility())
                {
                    return pattern;
                }
            }
        }
        return null;
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
    protected void addKnowledgeChunk(final KnowledgeChunk chunk) {
        if (chunk != null && !this.knowledgeChunks.contains(chunk)) {
            this.knowledgeChunks.add(chunk);
        }
    }

     // ----------------------------------------------------------------------
     // LMRS Desire Integration
     // ----------------------------------------------------------------------

     /** Current total lateral desire vector (left/right). */
     protected Desire laneChangeDesire = Desire.zero();

     /**
      * Computes and updates the total (mandatory + discretionary) desire vector
      * for this vehicle based on all active {@link KnowledgeChunk}s.
      * <p>
      * The result represents the LMRS-style aggregated motivation for lane changing,
      * which can later be used for tactical decisions (e.g., thresholding, maneuver selection).
      * </p>
      *
      * @throws ParameterException if any chunk’s desire computation fails
      */
     protected void updateLaneChangeDesire() throws ParameterException
     {
         Desire mandatorySum = Desire.zero();
         Desire discretionarySum = Desire.zero();

         // collect all desires from active chunks
         for (KnowledgeChunk chunk : this.tacticalPlanner.getKnowledgeChunks())
         {
             if (chunk.isApplicable() == true)
             {  Desire d = chunk.computeDesire();
                 if (d.isMandatory())
                     mandatorySum = mandatorySum.add(d);
                 else
                     discretionarySum = discretionarySum.add(d);}
         }

         // combine mandatory + discretionary using LMRS weighting per direction
         double dSync = this.getDMand(); // or specific param from Parameters
         double dCoop = this.getDFree(); // typical LMRS thresholds

         this.laneChangeDesire = Desire.combine(mandatorySum, discretionarySum, dSync, dCoop);
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
        for (HeadwayGtu leader : getLanePerception().getPerceptionCategory(NeighborsPerception.class)
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

    public CarFollowingModel getCarFollowingModel()
    {
        return this.carFollowingModel;
    }

    public LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    public LanePerception getLanePerception()
    {
        return this.lanePerception;
    }

    public LaneChange getLaneChange()
    {
        return this.laneChange;
    }

    /**
     * Sets the time headway parameter T based on the current lane change desire.
     * <p>
     * The value of T is interpolated between TMAX (no desire, {@code desire} = 0) and TMIN (full desire, {@code desire} = 1).
     * The desire value is clamped to the range [0, 1].
     * </p>
     * @param absoluteDesire lane change desire, where 0 means no desire (T = TMAX) and 1 means full desire (T = TMIN)
     * @throws ParameterException if T, TMIN, or TMAX is not present in the parameters
     */
    public void setDesiredHeadway(final Parameters params) throws ParameterException
    {
        double limitedDesire = this.absoluteDesire < 0 ? 0 : this.absoluteDesire > 1 ? 1 : this.absoluteDesire;
        double tDes = limitedDesire * params.getParameter(ParameterTypes.TMIN).si
                + (1 - limitedDesire) * params.getParameter(ParameterTypes.TMAX).si;
        double t = params.getParameter(ParameterTypes.T).si;
        params.setParameterResettable(ParameterTypes.T, Duration.instantiateSI(tDes < t ? tDes : t));
    }

    public void setDesiredHeadway() throws ParameterException
    {
        Double desire = this.getDesire() == null ? 0.0 : this.getDesire();
        setDesiredHeadway(this.getGtu().getParameters());
    }

    /**
     * Resets the time headway parameter T to its default value.
     * @throws ParameterException if T is not present in the parameters
     */
    public void resetDesiredHeadway(final Parameters params) throws ParameterException
    {
        params.resetParameter(ParameterTypes.T);
    }

    public void resetDesiredHeadway() throws ParameterException
    {
        Double desire = this.getDesire() == null ? 0.0 : this.getDesire();
        resetDesiredHeadway(this.getGtu().getParameters());
    }

    /**
     * Calculates the acceleration using the car-following model, adjusting the time headway T according to the specified lane
     * change desire. The value of T is interpolated between TMAX (no desire, {@code desire} = 0) and TMIN (full desire,
     * {@code desire} = 1).
     * <p>
     * Temporarily sets T based on the desire, computes the acceleration, and then resets T to its original value.
     * </p>
     * @param leader the headway to the leader vehicle (returns free acceleration if leaders are empty)
     * @param absoluteDesire lane change desire, where 0 means no desire and 1 means full desire
     * @return the calculated acceleration based on the adjusted headway
     * @throws ParameterException if a required parameter is not defined
     * @throws OperationalPlanException if an error occurs during acceleration calculation
     */
    public Acceleration desireBasedFollowingAcceleration(final Headway leader)
            throws ParameterException, OperationalPlanException
    {
        Double desire = this.getDesire() == null ? 0.0 : this.getDesire();
        Parameters params = this.getGtu().getParameters();
        Speed egoSpeed = getLanePerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        SpeedLimitInfo speedLimitInfo = this.getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(new Length(1.0, LengthUnit.SI));
        // set T
        setDesiredHeadway();
        // calculate acceleration
        Acceleration a = CarFollowingUtil.followSingleLeader(getCarFollowingModel(), params, egoSpeed, speedLimitInfo,
                leader.getDistance(), leader.getSpeed());
        // reset T
        resetDesiredHeadway();
        return a;
    }

    public Acceleration desireBasedFollowingAcceleration() throws ParameterException, OperationalPlanException
    {
        Headway leader = getLanePerception().getPerceptionCategory(DirectDefaultSimplePerception.class).getForwardHeadwayGtu();
        return desireBasedFollowingAcceleration(leader);
    }

    public Acceleration freeAcceleration() throws OperationalPlanException, ParameterException
    {
        Parameters params = this.getGtu().getParameters();
        Speed egoSpeed = getLanePerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        SpeedLimitInfo speedLimitInfo = this.getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(new Length(1.0, LengthUnit.SI));
        Acceleration freeAcceleration =
                CarFollowingUtil.freeAcceleration(getCarFollowingModel(), params, egoSpeed, speedLimitInfo);
        return freeAcceleration;
    }

    public Acceleration deadEndAcceleration() throws ParameterException, OperationalPlanException
    {
        SortedSet<LaneChangeInfo> currentLaneLCInfo = getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                .getLegalLaneChangeInfo(RelativeLane.CURRENT);
        Length currentLaneLCRemainingDistance =
                currentLaneLCInfo.isEmpty() || currentLaneLCInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
                        : currentLaneLCInfo.first().remainingDistance();
        Parameters params = this.getGtu().getParameters();
        Speed egoSpeed = getLanePerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        SpeedLimitInfo speedLimitInfo = this.getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(new Length(1.0, LengthUnit.SI));

        Acceleration a =
                CarFollowingUtil.stop(this.carFollowingModel, params, egoSpeed, speedLimitInfo, currentLaneLCRemainingDistance);

        return a;

    }

    public Acceleration getMinAcceleration() throws OperationalPlanException, ParameterException {
        Acceleration minAcceleration = Acceleration.min(freeAcceleration(), desireBasedFollowingAcceleration(), deadEndAcceleration());
        return minAcceleration;
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

    public void updateDesire() throws ParameterException
    {
        if (this.desireRelaxationTime.si > 0.0)
        {
            Duration dt = this.getGtu().getParameters().getParameter(ParameterTypes.DT);

            this.absoluteDesire -= this.absoluteDesire * dt.si / this.desireRelaxationTime.si;
            if (this.absoluteDesire < 0.0)
            {
                this.absoluteDesire = 0.0;
            }

            this.desireRelaxationTime = Duration
                    .instantiateSI(this.desireRelaxationTime.si - dt.si < 0 ? 0.0 : this.desireRelaxationTime.si - dt.si);
        }
    }

    /**
     * Returns the free driving distance constant.
     * @return the value of DFREE
     */
    public double getDFree() {
        return this.DFREE;
    }

    /**
     * Returns the mandatory driving distance constant.
     * @return the value of DMAND
     */
    public double getDMand() {
        return this.DMAND;
    }

    /**
     * Returns the speed difference threshold (vGain) used in LMRS.
     * @return the value of vGain
     */
    public static Speed getVGain() {
        return vGain;
    }

    /**
     * Returns the critical speed threshold (vCrit) used in LMRS.
     * @return the value of vCrit
     */
    public static Speed getVCrit() {
        return vCrit;
    }

    /**
     * Returns the sensitivity parameter for social speed dynamics.
     * @return the value of socioSpeedSensitivity
     */
    public static Double getSocioSpeedSensitivity() {
        return socioSpeedSensitivity;
    }




}
