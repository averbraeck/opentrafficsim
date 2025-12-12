package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.MandatoryLaneChangePattern_old;

//
//import java.util.Iterator;
//
//import org.djunits.value.vdouble.scalar.Acceleration;
//import org.djunits.value.vdouble.scalar.Duration;
//import org.djunits.value.vdouble.scalar.Length;
//import org.djunits.value.vdouble.scalar.Speed;
//import org.opentrafficsim.base.parameters.ParameterException;
//import org.opentrafficsim.base.parameters.ParameterType;
//import org.opentrafficsim.base.parameters.ParameterTypes;
//import org.opentrafficsim.base.parameters.Parameters;
//import org.opentrafficsim.core.gtu.GtuException;
//import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
//import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
//import org.opentrafficsim.core.network.LateralDirectionality;
//import org.opentrafficsim.core.network.NetworkException;
//import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
//import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
//import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
//import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
//import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
//import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
//import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.MandatoryLaneChangePattern;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext;
//import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
//import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
//import org.opentrafficsim.road.network.lane.Lane;
//import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
//
///**
// * MandatoryLaneChangePattern
// * --------------------------
// *
// * Maneuver pattern for necessary (mandatory) lane changes, e.g. merging from an on‑ramp.
// *
// * <p>Das Pattern implementiert die State Machine aus deinem Diagramm:
// * <ul>
// *   <li>{@link MatchTargetLaneSpeedState} – match_target_lane_speed</li>
// *   <li>{@link SearchForGapState} – search_for_gap</li>
// *   <li>{@link AccelerateToTargetGapState} – accelerate_to_target_gap</li>
// *   <li>{@link BreakingEndOfRampState} – breaking_end_of_ramp</li>
// *   <li>{@link ExecuteLaneChangeState} – execute_lane_change</li>
// *   <li>{@link ExecuteLaneChangeFromStandstillState} – execute_lane_change_from_standstill</li>
// * </ul>
// *
// * <p>Die exakten Formeln (Zeitsicherheiten, Merge‑Punkt etc.) sind bewusst gekapselt und mit
// * {@code TODO}-Kommentaren versehen, so dass du sie aus deinem Manuskript/Diagramm direkt
// * nachrüsten kannst.</p>
// */
//public class MandatoryLaneChangePattern_old extends ManeuverPattern {
//
//    /** Richtung des notwendigen Fahrstreifenwechsels (meist LEFT für Einfädeln von rechts). */
//    private final LateralDirectionality targetDirection;
//
//
//    /** Aktuell gewählte Ziel-Lücke auf der Zielspur. */
//    private GapCandidate activeGap;
//
//    /**
//     * Container für eine Ziel-Lücke auf der Zielspur.
//     * Enthält Leader/Follower auf der Zielspur und ggf. einen geplanten Merge-Punkt.
//     */
//    public static class GapCandidate {
//
//        /** Führendes Fahrzeug der Ziel-Lücke. */
//        private final HeadwayGtu leader;
//
//        /** Folgendes Fahrzeug der Ziel-Lücke. */
//        private final HeadwayGtu follower;
//
//        /** Geplanter Merge-Punkt entlang der Zielspur (optional). */
//        private final Length mergePoint;
//
//        public GapCandidate(final HeadwayGtu leader, final HeadwayGtu follower, final Length mergePoint) {
//            this.leader = leader;
//            this.follower = follower;
//            this.mergePoint = mergePoint;
//        }
//
//        public HeadwayGtu getLeader() {
//            return this.leader;
//        }
//
//        public HeadwayGtu getFollower() {
//            return this.follower;
//        }
//
//        public Length getMergePoint() {
//            return this.mergePoint;
//        }
//    }
//
//    /**
//     * Constructs a mandatory lane change pattern for a given merge direction.
//     *
//     * @param knowledgeChunk knowledge chunk providing access to contexts and tactical planner
//     * @param targetDirection direction of the mandatory lane change (e.g. LEFT for a right-hand on-ramp)
//     */
//    public MandatoryLaneChangePattern_old(final KnowledgeChunk knowledgeChunk, final LateralDirectionality targetDirection) {
//        super(PatternType.COOPERATIVE, knowledgeChunk);
//        this.targetDirection = targetDirection;
//        this.initialActionState = new MatchTargetLaneSpeedState(this);
//        this.requiredContextKeys.add("Ego");
//        this.requiredContextKeys.add("Neighbors");
//        this.requiredContextKeys.add("Infrastructure");
//    }
//
//    /** Returns the direction in which the merge must happen. */
//    public LateralDirectionality getTargetDirection() {
//        return this.targetDirection;
//    }
//
//    /** Gets the currently active target gap (may be null). */
//    public GapCandidate getActiveGap() {
//        return this.activeGap;
//    }
//
//    /** Sets the currently active target gap. */
//    public void setActiveGap(final GapCandidate gap) {
//        this.activeGap = gap;
//    }
//
//    @Override
//    public boolean checkContext() {
//        // Mandatory lane change is relevant on ramps / lane drops.
//        // If you have an explicit ramp context flag, you can check it here.
//        return true;
//    }
//
//    @Override
//    public boolean checkAbility() {
//        // Feasibility is handled within individual states (gap checks, safety, etc.).
//        return true;
//    }
//
//    /* =================================================================================================
//     *  STATE: match_target_lane_speed
//     * ================================================================================================= */
//
//    /**
//     * State {@code match_target_lane_speed}.
//     *
//     * <p>Ziel: Die eigene Geschwindigkeit an die mittlere Geschwindigkeit der Zielspur anpassen,
//     * um die spätere Lückenwahl und Beschleunigung zu erleichtern.</p>
//     *
//     * <p>Aktionen (vereinfacht):
//     * <ul>
//     *   <li>Standard Car-Following auf der aktuellen Spur (EgoContext)</li>
//     *   <li>Falls ein Leader auf der Zielspur existiert: zusätzliche Annäherung an dessen Geschwindigkeit</li>
//     * </ul>
//     *
//     * <p>Übergänge (vereinfacht):
//     * <ul>
//     *   <li>→ {@link ExecuteLaneChangeState}, falls bereits sichere Lücke erkannt.</li>
//     *   <li>→ {@link SearchForGapState}, wenn Mandatory-Desire die Such-Schwelle {@link #D_SEARCH} erreicht.</li>
//     * </ul>
//     */
//    public static class MatchTargetLaneSpeedState extends ActionState {
//
//        /** Referenz auf das übergeordnete Pattern. */
//        private final MandatoryLaneChangePattern pattern;
//
//        public MatchTargetLaneSpeedState(final ManeuverPattern pattern) {
//            super(pattern);
//            this.pattern = (MandatoryLaneChangePattern) pattern;
//            this.active = true;
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
//
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
//            Parameters params = this.vehicle.getGtu().getParameters();
//
//            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
//
//            Speed targetLaneSpeed = ego.getEgoSpeed(); // default: ego speed
//            if (this.pattern.getTargetDirection().isLeft()) {
//                targetLaneSpeed = macro.getAverageSpeedLeft();
//            }
//            else if (this.pattern.getTargetDirection().isRight()) {
//                targetLaneSpeed = macro.getAverageSpeedRight();
//            }
//
//            Acceleration aTargetLaneSpeed = CarFollowingUtil.approachTargetSpeed(
//                        this.vehicle.getCarFollowingModel(),
//                        params,
//                        ego.getEgoSpeed(),
//                        infra.getCurrentSpeedLimit(),
//                        Length.instantiateSI(100.0), // TODO: geeignete Distanz wählen
//                        targetLaneSpeed);
//
//            acc = Acceleration.min(acc, aTargetLaneSpeed);
//
//            return new SimpleOperationalPlan(
//                    acc,
//                    params.getParameter(ParameterTypes.DT));
//        }
//
//        @Override
//        public SimpleOperationalPlan next()
//                throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
//
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            Desire mandatoryDesire = this.vehicle.getMandatoryLaneChangeDesire();
//            double dMand = mandatoryDesire.magnitude();
//
//            // Direktwechsel möglich? (große Lücke, Sicherheitsbedingungen erfüllt)
//            if (neighbors.getIfLaneChangePossible(this.pattern.getTargetDirection())) {
//                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
//            }
//
//            // Mandatory-Desire hoch genug → in systematische Lückensuche wechseln
//            if (dMand >= this.vehicle.getParameters().getParameter(MirovaParameters.DSEARCH)) {
//                return transitionTo(new SearchForGapState(this.maneuverPattern));
//            }
//
//            return null; // im aktuellen Zustand bleiben
//        }
//
//        @Override
//        public SimpleOperationalPlan abort() {
//            // Kein expliziter Abbruch in diesem Default-Startzustand
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "MatchTargetLaneSpeedState";
//        }
//    }
//
//    /* =================================================================================================
//     *  STATE: search_for_gap
//     * ================================================================================================= */
//
//    /**
//     * State {@code search_for_gap}.
//     *
//     * <p>Ziel: Eine passende Lücke auf der Zielspur identifizieren (ohne Kooperation),
//     * wobei Kandidaten gesammelt und anhand einfacher Kriterien bewertet werden.</p>
//     *
//     * <p>Vereinfacht:
//     * <ul>
//     *   <li>Bestimme Leader und Follower auf der Zielspur über {@link NeighborsContext}.</li>
//     *   <li>Erzeuge daraus eine {@link GapCandidate} und speichere sie im Pattern.</li>
//     * </ul>
//     *
//     * <p>Übergänge (vereinfacht):
//     * <ul>
//     *   <li>→ {@link ExecuteLaneChangeState}, wenn Lücke sofort sicher ist.</li>
//     *   <li>→ {@link AccelerateToTargetGapState}, wenn Mandatory-Desire &gt;= {@link #D_ACCEL} und ein Gap existiert.</li>
//     *   <li>→ {@link BreakingEndOfRampState}, wenn Mandatory-Desire &gt;= {@link #D_FORCE} aber keine brauchbare Lücke vorhanden.</li>
//     * </ul>
//     */
//    public static class SearchForGapState extends ActionState {
//
//        private final MandatoryLaneChangePattern pattern;
//
//        public SearchForGapState(final ManeuverPattern pattern) {
//            super(pattern);
//            this.pattern = (MandatoryLaneChangePattern) pattern;
//            this.active = true;
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl()
//                throws ParameterException, GtuException, NetworkException {
//
//            // In diesem Zustand wird longitudinal (fast) wie gewohnt gefahren.
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            Parameters params = this.vehicle.getGtu().getParameters();
//
//            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
//
//            Speed targetLaneSpeed = ego.getEgoSpeed(); // default: ego speed
//            if (this.pattern.getTargetDirection().isLeft()) {
//                targetLaneSpeed = macro.getAverageSpeedLeft();
//            }
//            else if (this.pattern.getTargetDirection().isRight()) {
//                targetLaneSpeed = macro.getAverageSpeedRight();
//            }
//
//            Acceleration aTargetLaneSpeed = CarFollowingUtil.approachTargetSpeed(
//                        this.vehicle.getCarFollowingModel(),
//                        params,
//                        ego.getEgoSpeed(),
//                        infra.getCurrentSpeedLimit(),
//                        Length.instantiateSI(100.0), // TODO: geeignete Distanz wählen
//                        targetLaneSpeed);
//
//            acc = Acceleration.min(acc, aTargetLaneSpeed);
//
//            return new SimpleOperationalPlan(
//                    acc,
//                    params.getParameter(ParameterTypes.DT));
//        }
//
//        @Override
//        public SimpleOperationalPlan next()
//                throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
//            Parameters params = this.vehicle.getParameters();
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            // 1. Sofortige sichere Lücke? → direkt Spurwechsel
//            if (neighbors.getIfLaneChangePossible(this.pattern.getTargetDirection())) {
//                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
//            }
//
//            Desire mandatoryDesire = this.vehicle.getMandatoryLaneChangeDesire();
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            double dMand = mandatoryDesire.magnitude();
//
//            Length distanceToEndOfLane = infra.getDistanceToLaneEnd();
//            Acceleration currentAcceleration = ego.getCurrentCarFollowingAcceleration();
//            Speed currentSpeed = ego.getEgoSpeed();
//
//            // calculate time to end of lane based on current speed and acceleration with pq formula from s = 1/2 a t^2 + v t
//            // as acceleratino will get lower with higher speeds, this is a lower bound
//            Duration timeToEndOfLane =
//                    Duration.instantiateSI(
//                    - (currentSpeed.si / currentAcceleration.si)
//                    + Math.sqrt(
//                            Math.pow(currentSpeed.si / currentAcceleration.si, 2)
//                            + 2 * distanceToEndOfLane.si / currentAcceleration.si
//                            )
//                    );
//
//            HeadwayGtu leaderTarget = neighbors.getLeader(this.pattern.getTargetDirection());
//            HeadwayGtu followerTarget = neighbors.getFollower(this.pattern.getTargetDirection());
//
//            // Step 2: get position to merge in front of follower: assume constant speed of follower and acceleration of ego
//            Speed followerSpeed = followerTarget.getSpeed();
//            Length distanceToFollower = followerTarget.getDistance();
//
//            Duration timeToMergePoint = Duration.instantiateSI(
//                    - (currentSpeed.si - followerSpeed.si) / currentAcceleration.si
//                    + Math.sqrt(
//                            Math.pow((currentSpeed.si - followerSpeed.si) / currentAcceleration.si, 2)
//                            - (distanceToFollower.si
//                                    - params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)
//                                    * params.getParameter(ParameterTypes.T).si * followerSpeed.si)
//                            / currentAcceleration.si
//                            )
//                    );
//
//            Length distanceToMergePoint = Length.instantiateSI(
//                    0.5 * currentAcceleration.si * Math.pow(timeToMergePoint.si, 2)
//                    + currentSpeed.si * timeToMergePoint.si
//                    );
//
//
//            // if time to merge point is after time to end of lane, we cannot merge before end of lane
//            if (distanceToMergePoint.si < distanceToEndOfLane.si) {
//                Speed speedAtMergePoint = Speed.instantiateSI(
//                        currentSpeed.si + currentAcceleration.si * timeToMergePoint.si);
//
//                followerTarget.getParameters().setParameterResettable(ParameterTypes.T,
//                        followerTarget.getParameters().getParameter(ParameterTypes.T)
//                        .times(params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)));
//                Acceleration followerDecelAtMergePoint = CarFollowingUtil.followSingleLeader(
//                        followerTarget.getCarFollowingModel(),
//                        followerTarget.getParameters(),
//                        followerTarget.getSpeed(),
//                        followerTarget.getSpeedLimitInfo(),
//                        Length.instantiateSI(params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)
//                        * params.getParameter(ParameterTypes.T).si * followerSpeed.si),
//                        speedAtMergePoint);
//                followerTarget.getParameters().resetParameter(ParameterTypes.T);
//
//                if (followerDecelAtMergePoint.si
//                        >= params.getParameter(MirovaParameters.followerDecelerationThreshold).si
//                        ) {
//                    return new GapCandidate(leaderTarget, followerTarget, distanceToMergePoint)
//                } else {
//                    // no safe gap
//                    followerTarget = null;
//                    leaderTarget = null;
//                }
//            }
//
//
//
//
//
//            // 2. Kandidaten-Lücke bestimmen und im Pattern speichern
//
//            if (leaderTarget != null && followerTarget != null) {
//                // TODO: Merge-Punkt anhand deines Diagramms berechnen
//                // Temporär: Null als Platzhalter
//                this.pattern.setActiveGap(new GapCandidate(leaderTarget, followerTarget, null));
//            } else {
//                this.pattern.setActiveGap(null);
//            }
//
//            // 3. Mandatorische Dringlichkeit hoch → aktiv zum Gap beschleunigen
//            if (this.pattern.getActiveGap() != null && dMand >= this.pattern.D_ACCEL) {
//                return transitionTo(new AccelerateToTargetGapState(this.maneuverPattern));
//            }
//
//            // 4. Sehr hohe Dringlichkeit, aber keine brauchbare Lücke → Rampenende-Strategie
//            if (dMand >= this.pattern.D_FORCE && this.pattern.getActiveGap() == null) {
//                return transitionTo(new BreakingEndOfRampState(this.maneuverPattern));
//            }
//
//            return null;
//        }
//
//        @Override
//        public SimpleOperationalPlan abort() {
//            // Kein expliziter Abbruch – ggf. später wieder in MatchTargetLaneSpeed zurückwechseln
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "SearchForGapState";
//        }
//
//        private GapCandidate searchUpstreamGap() throws ParameterException, GtuException, NetworkException {
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
//            Parameters params = this.vehicle.getGtu().getParameters();
//            NeighborsPerception neighPerception = this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);
//
//            Length distanceToEndOfLane = infra.getDistanceToLaneEnd();
//            Acceleration currentAcceleration = ego.getCurrentCarFollowingAcceleration();
//            Speed currentSpeed = ego.getEgoSpeed();
//            Speed targetLaneSpeed = macro.getAverageSpeedLeft();
//            RelativeLane targetLane = RelativeLane.LEFT;
//
//            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> followers = neighPerception.getFollowers(targetLane);
//            Iterator<HeadwayGtu> Iterator = followers.iterator();
//            HeadwayGtu leaderTarget = neighbors.getLeader(this.pattern.getTargetDirection());
//
//            for (; Iterator.hasNext(); ) {
//                HeadwayGtu followerTarget = Iterator.next();
//
//                Speed followerSpeed = followerTarget.getSpeed();
//                Length distanceToFollower = followerTarget.getDistance();
//
//                Duration timeToMergePoint = Duration.instantiateSI(
//                        - (currentSpeed.si - followerSpeed.si) / currentAcceleration.si
//                        + Math.sqrt(
//                                Math.pow((currentSpeed.si - followerSpeed.si) / currentAcceleration.si, 2)
//                                - (distanceToFollower.si
//                                        - params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)
//                                        * params.getParameter(ParameterTypes.T).si * followerSpeed.si)
//                                / currentAcceleration.si
//                                )
//                        );
//
//                Length distanceToMergePoint = Length.instantiateSI(
//                        0.5 * currentAcceleration.si * Math.pow(timeToMergePoint.si, 2)
//                        + currentSpeed.si * timeToMergePoint.si
//                        );
//
//                // if time to merge point is after time to end of lane, we cannot merge before end of lane
//                if (distanceToMergePoint.si < distanceToEndOfLane.si) {
//                    Speed speedAtMergePoint = Speed.instantiateSI(
//                            currentSpeed.si + currentAcceleration.si * timeToMergePoint.si);
//
//                    // set TargetTimeGap to handle reduced safety distance during lane change
//                    followerTarget.getParameters().setParameterResettable(ParameterTypes.T,
//                            followerTarget.getParameters().getParameter(ParameterTypes.T)
//                            .times(params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)));
//
//                    // calculate follower deceleration at merge point
//                    Acceleration followerDecelAtMergePoint = CarFollowingUtil.followSingleLeader(
//                            followerTarget.getCarFollowingModel(),
//                            followerTarget.getParameters(),
//                            followerTarget.getSpeed(),
//                            followerTarget.getSpeedLimitInfo(),
//                            Length.instantiateSI(params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)
//                            * params.getParameter(ParameterTypes.T).si * followerSpeed.si),
//                            speedAtMergePoint);
//
//                    // reset TargetTimeGap to original value
//                    followerTarget.getParameters().resetParameter(ParameterTypes.T);
//
//                    if (followerDecelAtMergePoint.si
//                            >= params.getParameter(MirovaParameters.followerDecelerationThreshold).si
//                            ) {
//                        return new GapCandidate(leaderTarget, followerTarget, distanceToMergePoint);
//                    }
//                }
//                else {
//                    // no possible gap available after that gap
//                    return null;
//                }
//
//                leaderTarget = followerTarget;
//
//    }
//            return null;
//        }
//
//
//        private GapCandidate searchDownstreamGap() throws ParameterException, GtuException, NetworkException {
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            MacroTrafficContext macro = this.vehicle.getContext(MacroTrafficContext.class);
//            Parameters params = this.vehicle.getGtu().getParameters();
//            NeighborsPerception neighPerception = this.vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);
//
//            Length distanceToEndOfLane = infra.getDistanceToLaneEnd();
//            Acceleration currentAcceleration = ego.getCurrentCarFollowingAcceleration();
//            Speed currentSpeed = ego.getEgoSpeed();
//
//
//            Speed targetLaneSpeed = macro.getAverageSpeedRight();
//            RelativeLane targetLane = RelativeLane.RIGHT;
//
//            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders = neighPerception.getLeaders(targetLane);
//            Iterator<HeadwayGtu> Iterator = leaders.iterator();
//            HeadwayGtu leaderTarget = null;
//            HeadwayGtu followerTarget = neighbors.getFollower(this.pattern.getTargetDirection());
//
//            for (; Iterator.hasNext(); ) {
//                leaderTarget = Iterator.next();
//
//                // Step 2: get position to merge in front of follower: assume constant speed of follower and acceleration of ego
//                Speed followerSpeed = followerTarget.getSpeed();
//                Length distanceToFollower = followerTarget.getDistance();
//
//                Duration timeToMergePoint = Duration.instantiateSI(
//                        - (currentSpeed.si - followerSpeed.si) / currentAcceleration.si
//                        + Math.sqrt(
//                                Math.pow((currentSpeed.si - followerSpeed.si) / currentAcceleration.si, 2)
//                                - (distanceToFollower.si
//                                        - params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)
//                                        * params.getParameter(ParameterTypes.T).si * followerSpeed.si)
//                                / currentAcceleration.si
//                                )
//                        );
//
//                Length distanceToMergePoint = Length.instantiateSI(
//                        0.5 * currentAcceleration.si * Math.pow(timeToMergePoint.si, 2)
//                        + currentSpeed.si * timeToMergePoint.si
//                        );
//
//
//                // if time to merge point is after time to end of lane, we cannot merge before end of lane
//                if (distanceToMergePoint.si < distanceToEndOfLane.si) {
//                    Speed speedAtMergePoint = Speed.instantiateSI(
//                            currentSpeed.si + currentAcceleration.si * timeToMergePoint.si);
//
//                    // set TargetTimeGap to handle reduced safety distance during lane change
//                    followerTarget.getParameters().setParameterResettable(ParameterTypes.T,
//                            followerTarget.getParameters().getParameter(ParameterTypes.T)
//                            .times(params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)));
//
//                    // calculate follower deceleration at merge point
//                    Acceleration followerDecelAtMergePoint = CarFollowingUtil.followSingleLeader(
//                            followerTarget.getCarFollowingModel(),
//                            followerTarget.getParameters(),
//                            followerTarget.getSpeed(),
//                            followerTarget.getSpeedLimitInfo(),
//                            Length.instantiateSI(params.getParameter(MirovaParameters.safetyDistanceReductionFactorLaneChange)
//                            * params.getParameter(ParameterTypes.T).si * followerSpeed.si),
//                            speedAtMergePoint);
//
//                    // reset TargetTimeGap to original value
//                    followerTarget.getParameters().resetParameter(ParameterTypes.T);
//
//                    if (followerDecelAtMergePoint.si
//                            >= params.getParameter(MirovaParameters.followerDecelerationThreshold).si
//                            ) {
//                        return new GapCandidate(leaderTarget, followerTarget, distanceToMergePoint);
//                    }
//                }
//                else {
//                    // no possible gap available after that gap
//                    return null;
//                }
//
//                if (currentSpeed.si <= targetLaneSpeed.si) {
//                    leaderTarget = followerTarget;
//                }
//                else {
//                    followerTarget = leaderTarget;
//                }
//
//    }
//            return null;
//        }
//
//    }
//
//    /* =================================================================================================
//     *  STATE: accelerate_to_target_gap
//     * ================================================================================================= */
//
//    /**
//     * State {@code accelerate_to_target_gap}.
//     *
//     * <p>Ziel: So beschleunigen (oder verzögern), dass der Merge-Punkt hinter dem Gap-Leader
//     * rechtzeitig erreicht und dort eine zulässige Zeitlücke realisiert wird.</p>
//     *
//     * <p>Hier wird vereinfacht: wir nutzen Car-Following relativ zum gewählten Gap-Leader und
//     * beschränken die Beschleunigung zusätzlich, um grob den Merge-Punkt zu treffen.</p>
//     */
//    public static class AccelerateToTargetGapState extends ActionState {
//
//        private final MandatoryLaneChangePattern pattern;
//
//        public AccelerateToTargetGapState(final ManeuverPattern pattern) {
//            super(pattern);
//            this.pattern = (MandatoryLaneChangePattern) pattern;
//            this.active = true;
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl()
//                throws ParameterException, GtuException, NetworkException {
//
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            Parameters params = this.vehicle.getGtu().getParameters();
//
//            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
//
//            GapCandidate gap = this.pattern.getActiveGap();
//            if (gap != null && gap.getLeader() != null) {
//                // Vereinfachung: Annäherung an den Gap-Leader mit Car-Following auf Zielspur
//                Acceleration aToGap = CarFollowingUtil.followSingleLeader(
//                        this.vehicle.getCarFollowingModel(),
//                        params,
//                        ego.getEgoSpeed(),
//                        infra.getCurrentSpeedLimit(),
//                        gap.getLeader().getDistance(),
//                        gap.getLeader().getSpeed());
//
//                // Mindestbeschleunigung wählen (defensive Grenze)
//                acc = Acceleration.min(acc, aToGap);
//            }
//
//            return new SimpleOperationalPlan(
//                    acc,
//                    params.getParameter(ParameterTypes.DT));
//        }
//
//        @Override
//        public SimpleOperationalPlan next()
//                throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
//
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            Desire mandatoryDesire = this.vehicle.getMandatoryLaneChangeDesire();
//            double dMand = mandatoryDesire.magnitude();
//
//            // 1. Gap inzwischen sicher → Spurwechsel ausführen
//            if (neighbors.getIfLaneChangePossible(this.pattern.getTargetDirection())) {
//                return transitionTo(new ExecuteLaneChangeState(this.maneuverPattern, this.pattern.getTargetDirection()));
//            }
//
//            // 2. Gap verloren (z. B. durch neue Fahrzeuge) → erneut suchen
//            if (this.pattern.getActiveGap() == null) {
//                return transitionTo(new SearchForGapState(this.maneuverPattern));
//            }
//
//            // 3. Sehr hohe Dringlichkeit, aber weiterhin keine sichere Lücke → Rampenende-Strategie
//            if (dMand >= this.pattern.D_FORCE && !neighbors.getIfLaneChangePossible(this.pattern.getTargetDirection())) {
//                return transitionTo(new BreakingEndOfRampState(this.maneuverPattern));
//            }
//
//            return null;
//        }
//
//        @Override
//        public SimpleOperationalPlan abort() {
//            // Optional: Bei sehr niedriger Mandatory-Desire könnte man zurück in MatchTargetLaneSpeed
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "AccelerateToTargetGapState";
//        }
//    }
//
//    /* =================================================================================================
//     *  STATE: breaking_end_of_ramp
//     * ================================================================================================= */
//
//    /**
//     * State {@code breaking_end_of_ramp}.
//     *
//     * <p>Ziel: Sicheres Abbrechen der Lückensuche am Rampenende, ggf. bis zum Stillstand.</p>
//     *
//     * <p>Hier ist viel domänenspezifische Logik notwendig (Distanz bis Rampenende,
//     * Rückstauerkennung etc.). In dieser Grundversion wird einfach mit einer komfortablen
//     * Verzögerung gebremst, bis der GTU quasi steht. Ab sehr kleinen Geschwindigkeiten kann
//     * dann in den Zustand {@link ExecuteLaneChangeFromStandstillState} gewechselt werden.</p>
//     */
//    public static class BreakingEndOfRampState extends ActionState {
//
//        private final MandatoryLaneChangePattern pattern;
//
//        public BreakingEndOfRampState(final ManeuverPattern pattern) {
//            super(pattern);
//            this.pattern = (MandatoryLaneChangePattern) pattern;
//            this.active = true;
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl()
//                throws ParameterException, GtuException, NetworkException {
//
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            Parameters params = this.vehicle.getGtu().getParameters();
//
//            // Komfortable Verzögerung (kann mit B oder B0 parametriert werden)
//            Acceleration bDes = params.getParameter(ParameterTypes.B).neg();
//            Acceleration acc = bDes;
//
//            // Stoppen, wenn wir faktisch stehen
//            if (ego.getEgoSpeed().lt(Speed.instantiateSI(0.5))) {
//                acc = Acceleration.instantiateSI(0.0);
//            }
//
//            return new SimpleOperationalPlan(
//                    acc,
//                    params.getParameter(ParameterTypes.DT));
//        }
//
//        @Override
//        public SimpleOperationalPlan next()
//                throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
//
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//
//            // Am Rampenende im Stau → Reißverschluss: Spurwechsel aus dem Stand
//            if (ego.getEgoSpeed().lt(Speed.instantiateSI(0.5))
//                    && neighbors.getIfLaneChangePossible(this.pattern.getTargetDirection())) {
//                return transitionTo(new ExecuteLaneChangeFromStandstillState(this.maneuverPattern, this.pattern.getTargetDirection()));
//            }
//
//            // TODO: ggf. zusätzliche Abbruch-/Fallback-Logik, wenn gar kein Spurwechsel mehr möglich ist
//
//            return null;
//        }
//
//        @Override
//        public SimpleOperationalPlan abort() {
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "BreakingEndOfRampState";
//        }
//    }
//
//    /* =================================================================================================
//     *  STATE: execute_lane_change
//     * ================================================================================================= */
//
//    /**
//     * State {@code execute_lane_change}.
//     *
//     * <p>Ziel: Den Spurwechsel tatsächlich durchführen, basierend auf einem
//     * Two-Leader-Car-Following-Ansatz (aktueller und Zielspur-Leader) wie in deinem
//     * bestehenden {@code ExecuteLaneChange}-State.</p>
//     */
//    public static class ExecuteLaneChangeState extends ActionState {
//
//        /** Zielrichtung des Spurwechsels. */
//        private final LateralDirectionality direction;
//
//        /** Ursprüngliche Spur, um Abschluss des Spurwechsels zu erkennen. */
//        private final Lane originLane;
//
//        public ExecuteLaneChangeState(final ManeuverPattern pattern, final LateralDirectionality direction) {
//            super(pattern);
//            this.direction = direction;
//            this.originLane = this.vehicle.getGtu().getLane();
//            this.active = true;
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl()
//                throws ParameterException, GtuException, NetworkException {
//
//            InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
//            NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            Parameters params = this.vehicle.getGtu().getParameters();
//
//            Speed egoSpeed = ego.getEgoSpeed();
//            Acceleration acc = ego.getCurrentCarFollowingAcceleration();
//
//            // Zielspur-Leader berücksichtigen, solange wir noch auf der Ursprungsspur sind
//            if (this.vehicle.getGtu().getLane() == this.originLane) {
//                HeadwayGtu targetLeader = neighbors.getLeader(this.direction);
//                if (targetLeader != null) {
//                    Acceleration aTarget = CarFollowingUtil.followSingleLeader(
//                            this.vehicle.getCarFollowingModel(),
//                            params,
//                            egoSpeed,
//                            infra.getCurrentSpeedLimit(),
//                            targetLeader.getDistance(),
//                            targetLeader.getSpeed());
//                    acc = Acceleration.min(acc, aTarget);
//                }
//            }
//
//            SimpleOperationalPlan plan = new SimpleOperationalPlan(
//                    acc,
//                    params.getParameter(ParameterTypes.DT),
//                    this.direction);
//
//            if (this.direction == LateralDirectionality.LEFT) {
//                plan.setIndicatorIntentLeft();
//            } else if (this.direction == LateralDirectionality.RIGHT) {
//                plan.setIndicatorIntentRight();
//            }
//
//            return plan;
//        }
//
//        @Override
//        public SimpleOperationalPlan next()
//                throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
//
//            boolean finished = !this.vehicle.getLaneChange().isChangingLane()
//                    && !this.originLane.equals(this.vehicle.getGtu().getLane());
//
//            if (finished) {
//                // Nach dem Spurwechsel kehren wir in das Default-Fahrverhalten zurück
//                return transitionTo(new AutobahnFreeDrivingPattern.FreeDrivingState(this.maneuverPattern));
//            }
//            return null;
//        }
//
//        @Override
//        public SimpleOperationalPlan abort() {
//            // Explizites Abbrechen des Spurwechsels ist derzeit nicht implementiert
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "ExecuteLaneChangeState[" + this.direction + "]";
//        }
//    }
//
//    /* =================================================================================================
//     *  STATE: execute_lane_change_from_standstill
//     * ================================================================================================= */
//
//    /**
//     * State {@code execute_lane_change_from_standstill}.
//     *
//     * <p>Ziel: Spurwechsel im Stau aus dem Stillstand heraus (Reißverschlussprinzip).
//     * Die Logik ist der normalen {@link ExecuteLaneChangeState}-Logik ähnlich, verwendet
//     * aber typischerweise kleinere Beschleunigungen.</p>
//     */
//    public static class ExecuteLaneChangeFromStandstillState extends ActionState {
//
//        private final LateralDirectionality direction;
//        private final Lane originLane;
//
//        public ExecuteLaneChangeFromStandstillState(final ManeuverPattern pattern, final LateralDirectionality direction) {
//            super(pattern);
//            this.direction = direction;
//            this.originLane = this.vehicle.getGtu().getLane();
//            this.active = true;
//        }
//
//        @Override
//        public SimpleOperationalPlan executeControl()
//                throws ParameterException, GtuException, NetworkException {
//
//            EgoContext ego = this.vehicle.getContext(EgoContext.class);
//            Parameters params = this.vehicle.getGtu().getParameters();
//
//            // Sanfte Anfahrbeschleunigung (z. B. halbes BCRIT)
//            Acceleration aStart = params.getParameter(ParameterTypes.BCRIT).neg().times(0.5);
//
//            SimpleOperationalPlan plan = new SimpleOperationalPlan(
//                    aStart,
//                    params.getParameter(ParameterTypes.DT),
//                    this.direction);
//
//            if (this.direction == LateralDirectionality.LEFT) {
//                plan.setIndicatorIntentLeft();
//            } else if (this.direction == LateralDirectionality.RIGHT) {
//                plan.setIndicatorIntentRight();
//            }
//
//            return plan;
//        }
//
//        @Override
//        public SimpleOperationalPlan next()
//                throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
//
//            boolean finished = !this.vehicle.getLaneChange().isChangingLane()
//                    && !this.originLane.equals(this.vehicle.getGtu().getLane());
//
//            if (finished) {
//                return transitionTo(new AutobahnFreeDrivingPattern.FreeDrivingState(this.maneuverPattern));
//            }
//            return null;
//        }
//
//        @Override
//        public SimpleOperationalPlan abort() {
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "ExecuteLaneChangeFromStandstillState[" + this.direction + "]";
//        }
//    }
//}
