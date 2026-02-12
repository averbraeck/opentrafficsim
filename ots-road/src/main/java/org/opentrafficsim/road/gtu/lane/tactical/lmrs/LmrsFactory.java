package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.parameters.ParameterFactoryOneShot;
import org.opentrafficsim.core.units.distributions.ContinuousDistSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.PerceivedGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.PerceivedGtuType.AnticipationPerceivedGtuType;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationHeadway;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationLaneChangeDesire;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSpeed;
import org.opentrafficsim.road.gtu.lane.perception.mental.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.FactorEstimation;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.gtu.lane.perception.mental.SumFuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArFuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArTaskCarFollowing;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArTaskCarFollowingExp;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArTaskLaneChanging;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArTaskLaneChangingD;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArTaskRoadSideDistraction;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.AdaptationSpeedChannel;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.AdaptationUpdateTime;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelFuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelMental;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskAcceleration;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskCarFollowing;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskConflict;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskCooperation;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskLaneChange;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskRoadSideDistraction;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskScan;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskSignal;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.ChannelTaskTrafficLight;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.IntersectionPerceptionChannel;
import org.opentrafficsim.road.gtu.lane.perception.mental.channel.NeighborsPerceptionChannel;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.Idm;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusMulti;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

import nl.tudelft.simulation.jstats.distributions.DistLogNormal;
import nl.tudelft.simulation.jstats.distributions.DistNormalTrunc;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;

/**
 * This tactical planner is general purpose for the use of the LMRS with any combination of defined sub-components. The class
 * seconds as a parameter factory. For both model components and parameter values, the factory can be used specific to the GTU
 * type. Model components can be set through command line arguments, or through calling {@link #set(Setting, Object)}.
 * Parameters can be set through standard parameter factory methods.
 * <p>
 * <b>Usage</b><br>
 * The factory can be used as:
 *
 * <pre>
 * LmrsFactory&lt;Lmrs&gt; factory = new LmrsFactory&lt;&gt;(List.of(DefaultsNl.CAR, DefaultsNl.VAN), Lmrs::new);
 *
 * factory.set(Setting.ADAPTATION_SPEED, false);
 * </pre>
 *
 * The GTU list is optional (default is {@code List.of(DefaultsNl.CAR, DefaultsNl.TRUCK)}), and the second
 * {@link TacticalPlannerProvider} argument can also be given as a list to specifiy different providers per GTU type. All
 * settings must be provided either for all GTU types, or for a specific GTU type with which the factory was initialized. GTUs
 * of other types can be generated within the simulation, so long as any of their parent types is within the list of the
 * factory.
 * <p>
 * <b>Command line arguments</b><br>
 * This class can be mixed in with any program using command line arguments:
 *
 * <pre>
 * &#64;Mixin
 * private LmrsFactory&lt;Lmrs&gt; factory = new LmrsFactory&lt;&gt;(Lmrs::new);
 *
 * public static void main(String[] args)
 * {
 *     Program program = new Program();
 *     CliUtil.changeOptionDefault(program, "gtuTypes", "NL.CAR|NL.VAN|NL.TRUCK");
 *     CliUtil.execute(program, args);
 * }
 * </pre>
 *
 * <i>Note: command line arguments and programmatically setting settings (set method) should in principle not both be used
 * within the same program. Default command line arguments should be changed by the option default as in the example above,
 * before the input args are executed.</i>
 * <p>
 * Use command line argument {@code --help} to get a list of all available command line arguments. In the example the factory is
 * initialized using the constructor of {@link Lmrs} as a supplier of the tactical planner class. Within the context of a
 * program it may be necessary to use a supplier of an another implementation of a tactical planner instead. The supplier can be
 * any function that follows the {@link TacticalPlannerProvider} signature, supplying an extension of
 * {@link AbstractIncentivesTacticalPlanner}.
 * <p>
 * Command line arguments can be defined to contain values for specific GTU types. The following arguments allow a simulation
 * with GTU types {@code NL.CAR} and {@code NL.TRUCK}, or any of their sub types. All GTUs will not generally keep to the slow
 * lane ({@code incentiveKeep}), while trucks will limit themselves on the rightmost two lanes ({@code incentiveStayRight}). GTU
 * types and the values that apply to them should be given in the same order. The number of values given should either be 1, or
 * equal to the number of GTU types given.
 *
 * <pre>
 * --gtuTypes=NL.CAR|NL.TRUCK --incentiveKeep=false --incentiveStayRight=false|true
 * </pre>
 *
 * The pipe character ({@code |}) is used to separate values. If the pipe character is part of any value, values can be quoted.
 * If a quote is part of a any value, the value can be quoted and the quote of the value can be escaped with a backslash \. The
 * following code will set the {@code --gtuTypes} to {@code [NL.CAR, NL.VAN, NL.T|RUCK]}.
 *
 * <pre>
 * CliUtil.execute(new CommandLine(myProgram).setTrimQuotes(true), new String[] {"--gtuTypes=NL.CAR|NL.VAN|\"NL.T|RUCK\""});
 * </pre>
 *
 * <b>One-shot mode</b>
 * <p>
 * This class extends {@link ParameterFactoryOneShot} and can thus be used as a parameter factory. It supports one-shot mode.
 * Any parameter set after {@link #setOneShotMode()} is called and before the next GTU is generated is only applied to said GTU.
 * At any other moment parameters that are set are fixed. This class implements {@link #setOneShotMode()} to apply the same mode
 * on model settings, applying only to the next GTU generated, after which settings are reverted to the state when
 * {@link #setOneShotMode()} was called.
 * <p>
 * <b>Settings applicability</b><br>
 * Many settings on perception components may only apply to some implementations of Fuller, see Table 1.<br>
 * <br>
 * <table border="1" style="text-align:center; padding:2px; border-spacing:0px; border-width:1px; border-style:solid">
 * <caption><i>Table 1: Perception components for Fuller implementations</i></caption>
 * <tr>
 * <td style="text-align:left"><b>Setting</b></td>
 * <td colspan="4" style="text-align:center"><b>Fuller implementation</b> (FULLER_IMPLEMENTATION)</td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>NONE</td>
 * <td>SUMMATIVE</td>
 * <td>ANTICIPATION_RELIANCE</td>
 * <td>ATTENTION_MATRIX</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">PRIMARY_TASK</td>
 * <td>-</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>-</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TEMPORAL_ANTICIPATION</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">ESTIMATION</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">FRACTION_OVERESTIMATION</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td colspan="5"><i>Tasks for mental model</i></td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TASK_CAR_FOLLOWING</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TASK_FREE_ACCELERATION</td>
 * <td>-</td>
 * <td>-</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TASK_TRAFFIC_LIGHTS</td>
 * <td>-</td>
 * <td>-</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TASK_SIGNAL</td>
 * <td>-</td>
 * <td>-</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TASK_LANE_CHANGE</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TASK_COOPERATION</td>
 * <td>-</td>
 * <td>-</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TASK_CONFLICTS</td>
 * <td>-</td>
 * <td>-</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">TASK_ROADSIDE_DISTRACTION</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td colspan="5"><i>Behavioral adaptations for mental model</i></td>
 * </tr>
 * <tr>
 * <td style="text-align:left">ADAPTATION_SPEED</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">ADAPTATION_HEADWAY</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">ADAPTATION_LANE_CHANGE</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * <td>&#10003;</td>
 * </tr>
 * <tr>
 * <td style="text-align:left">ADAPTATION_UPDATE_TIME</td>
 * <td>-</td>
 * <td>-</td>
 * <td>-</td>
 * <td>&#10003;</td>
 * </tr>
 * </table>
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> tactical planner type
 */
public class LmrsFactory<T extends AbstractIncentivesTacticalPlanner> extends ParameterFactoryOneShot
        implements LaneBasedTacticalPlannerFactory<T>
{

    /** Remembered state to reset in one-shot mode. */
    private Map<Setting<?>, List<?>> state;

    /** Random number stream. */
    private StreamInterface stream;

    /** Distribution of vGain. */
    private ContinuousDistSpeed vGainDist;

    /** Distribution of sigma. */
    private DistTriangular sigmaDist;

    /** Distribution of fSpeed. */
    private DistNormalTrunc fSpeedDist;

    /** LMRS provider. */
    private final List<TacticalPlannerProvider<T>> lmrsProvider;

    /** GTU type IDs in order of multi-valued arguments. */
    @Option(names = {"--gtuTypes"}, description = "GTU type IDs in order of multi-valued arguments.",
            defaultValue = "NL.CAR|NL.TRUCK", split = "\\|", splitSynopsisLabel = "|")
    private final List<String> gtuTypes;

    /** Peeked car-following model per GTU type. */
    private Map<GtuType, CarFollowingModel> peekedCarFollowingModel = new LinkedHashMap<>();

    // LMRS

    /** Car-following model: IDM, IDM_PLUS (default) or IDM_PLUS_MULTI. */
    @Option(names = {"--carFollowingModel"}, description = "Car-following model: IDM, IDM_PLUS or IDM_PLUS_MULTI.",
            defaultValue = "IDM_PLUS", split = "\\|", splitSynopsisLabel = "|", converter = CarFollowingModelConverter.class)
    private List<BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>> carFollowingModel =
            listOf(IdmPlus::new);

    /** Lane change synchronization: PASSIVE (default), PASSIVE_MOVING, ALIGN_GAP or ACTIVE. */
    @Option(names = {"--synchronization"},
            description = "Lane change synchronization: PASSIVE, PASSIVE_MOVING, ALIGN_GAP or ACTIVE.",
            defaultValue = "PASSIVE", split = "\\|", splitSynopsisLabel = "|", converter = SynchronizationConverter.class)
    private List<Synchronization> synchronization = listOf(Synchronization.PASSIVE);

    /** Lane change cooperation: PASSIVE (default), PASSIVE_MOVING or ACTIVE. */
    @Option(names = {"--cooperation"}, description = "Lane change cooperation: PASSIVE, PASSIVE_MOVING or ACTIVE.",
            defaultValue = "PASSIVE", split = "\\|", splitSynopsisLabel = "|", converter = CooperationConverter.class)
    private List<Cooperation> cooperation = listOf(Cooperation.PASSIVE);

    /** Lane change gap-acceptance: INFORMED (default) or EGO_HEADWAY. */
    @Option(names = {"--gapAcceptance"}, description = "Lane change gap-acceptance: INFORMED or EGO_HEADWAY.",
            defaultValue = "INFORMED", split = "\\|", splitSynopsisLabel = "|", converter = GapAcceptanceConverter.class)
    private List<GapAcceptance> gapAcceptance = listOf(GapAcceptance.INFORMED);

    // LMRS -> Mandatory incentives

    /** Mandatory lane change incentive for route (default: true). */
    @Option(names = {"--incentiveRoute"}, description = "Mandatory lane change incentive for route.", defaultValue = "true",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> incentiveRoute = listOf(true);

    /** Mandatory lane change incentive to join slow traffic at split and not block other traffic. */
    @Option(names = {"--incentiveGetInLane"},
            description = "Mandatory lane change incentive to join slow traffic at split and not block other traffic.",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> incentiveGetInLane = listOf(false);

    /** Custom mandatory incentives. */
    private List<Set<Supplier<MandatoryIncentive>>> customMandatoryIncentives = listOf(new LinkedHashSet<>());

    // LMRS -> Voluntary incentives

    /** Voluntary lane change incentive for speed with courtesy (default: true). */
    @Option(names = {"--incentiveSpeedWithCourtesy"}, description = "Voluntary lane change incentive for speed with courtesy.",
            defaultValue = "true", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> incentiveSpeedWithCourtesy = listOf(true);

    /** Voluntary lane change incentive for cooperative lane changes (default: false). */
    @Option(names = {"--incentiveCourtesy"}, description = "Voluntary lane change incentive for cooperative lane changes.",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> incentiveCourtesy = listOf(false);

    /** Voluntary lane change incentive to join the shortest queue (default: false). */
    @Option(names = {"--incentiveQueue"}, description = "Voluntary lane change incentive to join the shortest queue.",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> incentiveQueue = listOf(false);

    /** Voluntary lane change incentive for trucks to stay in slowest rightmost two lanes (default: false). */
    @Option(names = {"--incentiveStayRight"},
            description = "Voluntary lane change incentive for trucks to stay in rightmost two lanes.", defaultValue = "false",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> incentiveStayRight = listOf(false);

    /** Voluntary lane change incentive to keep to the slow lane (default: true). */
    @Option(names = {"--incentiveKeep"}, description = "Voluntary lane change incentive to keep to the slow lane.",
            defaultValue = "true", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> incentiveKeep = listOf(true);

    /** Custom voluntary incentives. */
    private List<Set<Supplier<VoluntaryIncentive>>> customVoluntaryIncentives = listOf(new LinkedHashSet<>());

    // LMRS -> Acceleration incentives

    /** Acceleration incentive to slow down prior to a lower speed limit (default: false). */
    @Option(names = {"--accelerationSpeedLimitTransition"},
            description = "Acceleration incentive to slow down prior to a lower speed limit.", defaultValue = "false",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> accelerationSpeedLimitTransition = listOf(false);

    /** Acceleration incentive to approach traffic lights (default: false). */
    @Option(names = {"--accelerationTrafficLights"}, description = "Acceleration incentive to approach traffic lights.",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> accelerationTrafficLights = listOf(false);

    /** Acceleration incentive to approach intersection conflicts (default: false). */
    @Option(names = {"--accelerationConflicts"}, description = "Acceleration incentive to approach intersection conflicts.",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> accelerationConflicts = listOf(false);

    /** Acceleration incentive to not overtake traffic in the left lane (default: false). */
    @Option(names = {"--accelerationNoRightOvertake"},
            description = "Acceleration incentive to not overtake traffic in the left lane.", defaultValue = "false",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> accelerationNoRightOvertake = listOf(false);

    /** Custom acceleration incentives. */
    private List<Set<Supplier<AccelerationIncentive>>> customAccelerationIncentives = listOf(new LinkedHashSet<>());

    // Fuller

    /** Implementation of Fuller: NONE (default), SUMMATIVE, ANTICIPATION_RELIANCE or ATTENTION_MATRIX. */
    @Option(names = {"--fullerImplementation"},
            description = "Implementation of Fuller: NONE, SUMMATIVE, ANTICIPATION_RELIANCE or ATTENTION_MATRIX.",
            defaultValue = "ATTENTION_MATRIX", split = "\\|", splitSynopsisLabel = "|")
    private List<FullerImplementation> fullerImplementation = listOf(FullerImplementation.NONE);

    /** Id of primary task under ANTICIPATION_RELIANCE (default: lane-changing). */
    @Option(names = {"--primaryTask"}, description = "Id of primary task under ANTICIPATION_RELIANCE.",
            defaultValue = "lane-changing", split = "\\|", splitSynopsisLabel = "|")
    private List<String> primaryTask = listOf("lane-changing");

    /** Enables temporal constant-speed anticipation (default: true). */
    @Option(names = {"--anticipation"}, description = "Enables temporal constant-speed anticipation.", defaultValue = "true",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> temporalAnticipation = listOf(true);

    /** Enables estimation of neighboring vehicles (default: true). */
    @Option(names = {"--estimation"}, description = "Enables estimation of neighboring vehicles.", defaultValue = "true",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> neighborEstimation = listOf(true);

    /** Fraction of drivers over-estimating speed and distance [0..1] (default: 1). */
    @Option(names = {"--fractionOverEstimation"},
            description = "Fraction of drivers over-estimating speed and distance [0..1].", defaultValue = "0.0", split = "\\|",
            splitSynopsisLabel = "|")
    private List<Double> fractionOverEstimation = listOf(1.0);

    // Fuller -> Tasks

    /** Enables car-following task (default: true). */
    @Option(names = {"--carFollowingTask"}, description = "Enables car-following task.", defaultValue = "true", split = "\\|",
            splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> carFollowingTask = listOf(true);

    /** Enables alternate car-following task under SUMMATIVE and ANTICIPATION_RELIANCE (default: false). */
    @Option(names = {"--alternateCarFollowingTask"}, description = "Enables alternate car-following task (exponential).",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> alternateCarFollowingTask = listOf(false);

    /** Enables free acceleration task under ATTENTION_MATRIX (default: false). */
    @Option(names = {"--freeAccelerationTask"},
            description = "Enables free acceleration task, useful when updateTimeAdaptation is true.", defaultValue = "false",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> freeAccelerationTask = listOf(false);

    /** Enables traffic lights task under ATTENTION_MATRIX (default: false). */
    @Option(names = {"--trafficLightsTask"}, description = "Enables traffic light task.", defaultValue = "false", split = "\\|",
            splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> trafficLightsTask = listOf(false);

    /** Enables signal task under ATTENTION_MATRIX (default: true). */
    @Option(names = {"--signalTask"}, description = "Enables signal task.", defaultValue = "true", split = "\\|",
            splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> signalTask = listOf(true);

    /** Enables lane-changing task (default: true). */
    @Option(names = {"--laneChangingTask"}, description = "Enables lane-change task.", defaultValue = "true", split = "\\|",
            splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> laneChangingTask = listOf(true);

    /** Enables alternate lane-changing task under SUMMATIVE and ANTICIPATION_RELIANCE (default: false). */
    @Option(names = {"--alternateLaneChangingTask"}, description = "Enables alternate lane-changing task (lane change desire).",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> alternateLaneChangingTask = listOf(false);

    /** Enables cooperation task under ATTENTION_MATRIX (default: true). */
    @Option(names = {"--cooperationTask"}, description = "Enables cooperation task.", defaultValue = "true", split = "\\|",
            splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> cooperationTask = listOf(true);

    /** Enables conflicts task under ATTENTION_MATRIX (default: false). */
    @Option(names = {"--conflictsTask"}, description = "Enables conflict task.", defaultValue = "false", split = "\\|",
            splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> conflictsTask = listOf(false);

    /** Enables road-side distraction task (default: false). */
    @Option(names = {"--roadSideDistractionTask"}, description = "Enables road-side distraction task.", defaultValue = "false",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> roadSideDistractionTask = listOf(false);

    // Fuller -> Adaptations

    /** Enables behavioral speed adaptation. */
    @Option(names = {"--speedAdaptation"}, description = "Enables behavioral speed adaptation.", defaultValue = "true",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> speedAdaptation = listOf(true);

    /** Enables behavioral headway adaptation. */
    @Option(names = {"--headwayAdaptation"}, description = "Enables behavioral headway adaptation.", defaultValue = "true",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> headwayAdaptation = listOf(true);

    /** Enables behavioral voluntary lane change adaptation. */
    @Option(names = {"--laneChangeAdaptation"}, description = "Enables behavioral voluntary lane change adaptation.",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> laneChangeAdaptation = listOf(false);

    /** Enables behavioral update time adaptation under ATTENTION_MATRIX. */
    @Option(names = {"--updateTimeAdaptation"}, description = "Enables behavioral update time adaptation.",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> updateTimeAdaptation = listOf(false);

    // Social interactions

    /** Enables tailgating. Without tailgating, any social interaction still results in social pressure. */
    @Option(names = {"--tailgating"},
            description = "Enables tailgating. Without tailgating, any social interaction still results in social pressure.",
            defaultValue = "false", split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> tailgating = listOf(false);

    /** Enables lane changes due to social pressure. */
    @Option(names = {"--socioLaneChange"}, description = "Enables lane changes due to social pressure.", defaultValue = "false",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> socioLaneChange = listOf(false);

    /** Enables speed increase due to social pressure. */
    @Option(names = {"--socioSpeed"}, description = "Enables speed increase due to social pressure.", defaultValue = "false",
            split = "\\|", splitSynopsisLabel = "|", negatable = true)
    private List<Boolean> socioSpeed = listOf(false);

    /**
     * Shorthand to create a list of default value(s). This is used instead of {@code List.of()} to return a mutable list.
     * @param <V> value type
     * @param values values
     * @return list of default value(s)
     */
    @SafeVarargs
    private static <V> List<V> listOf(final V... values)
    {
        return Stream.of(values).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Constructor that will use default NL.CAR and NL.TRUCK GTU types.
     * @param lmrsProvider provider for LMRS implementation, e.g. {@code Lmrs::new}
     * @throws NullPointerException when lmrsProvider is null
     */
    public LmrsFactory(final TacticalPlannerProvider<T> lmrsProvider)
    {
        this(List.of(DefaultsNl.CAR, DefaultsNl.TRUCK), lmrsProvider);
    }

    /**
     * Constructor with given GTU types and default LMRS implementation class.
     * @param gtuTypes GTU types
     * @param lmrsProvider provider for LMRS implementation, e.g. {@code Lmrs::new}
     * @throws NullPointerException when gtuTypes or lmrsProvider is null
     */
    public LmrsFactory(final List<GtuType> gtuTypes, final TacticalPlannerProvider<T> lmrsProvider)
    {
        Throw.whenNull(gtuTypes, "gtuTypes");
        Throw.whenNull(lmrsProvider, "lmrsProvider");
        this.gtuTypes = gtuTypes.stream().map((g) -> g.getId()).collect(Collectors.toCollection(ArrayList::new));
        this.lmrsProvider = listOf(lmrsProvider);
    }

    /**
     * Constructor with given GTU types and LMRS implementation class per GTU type.
     * @param gtuTypes GTU types
     * @param lmrsProviders providers for LMRS implementation for each GTU type, e.g. {@code Lmrs::new}, may contain
     *            {@code null} values (except first element)
     * @throws NullPointerException when gtuTypes or lmrsProviders is null
     * @throws IllegalArgumentException when gtuTypes and lmrsProviders are not of equal size
     */
    public LmrsFactory(final List<GtuType> gtuTypes, final List<TacticalPlannerProvider<T>> lmrsProviders)
    {
        Throw.whenNull(gtuTypes, "gtuTypes");
        Throw.whenNull(lmrsProviders, "lmrsProviders");
        Throw.when(gtuTypes.size() != lmrsProviders.size(), IllegalArgumentException.class,
                "gtuTypes and lmrsProviders are not of equal size");
        this.gtuTypes = gtuTypes.stream().map((g) -> g.getId()).collect(Collectors.toList());
        this.lmrsProvider = new ArrayList<>(lmrsProviders);
    }

    /**
     * Sets the factory to one-shot mode. All {@linkplain Setting}s, parameters and correlations set or added between a call to
     * this method and to {@link #create(LaneBasedGtu) create()} are removed from the factory after the call to
     * {@link #create(LaneBasedGtu) create()}.
     */
    @Override
    public void setOneShotMode()
    {
        super.setOneShotMode();
        this.state = new LinkedHashMap<>();
    }

    /**
     * Resets the factory to the state when {@link #setOneShotMode()} was called. Note that the parameter factory resets its own
     * state when it has set parameter values.
     * @param <V> setting value type
     */
    @SuppressWarnings("unchecked")
    protected <V> void resetState()
    {
        if (this.state == null)
        {
            return;
        }
        for (Entry<Setting<?>, List<?>> entry : this.state.entrySet())
        {
            List<V> values = (List<V>) entry.getKey().getListFunction().apply(this);
            values.clear();
            values.addAll((List<V>) entry.getValue());
        }
        this.state = null;
    }

    /**
     * Sets the random number stream.
     * @param stream random number stream
     * @return this factory for method chaining
     */
    @SuppressWarnings("hiddenfield")
    public LmrsFactory<T> setStream(final StreamInterface stream)
    {
        this.stream = stream;
        this.vGainDist = new ContinuousDistSpeed(new DistLogNormal(stream, 3.379, 0.4), SpeedUnit.KM_PER_HOUR);
        this.sigmaDist = new DistTriangular(stream, 0.0, 0.25, 1.0);
        this.fSpeedDist = new DistNormalTrunc(stream, 123.7 / 120.0, 0.1, 0.8, 50.0);
        return this;
    }

    /**
     * Sets the setting value for all GTU types.
     * @param <V> value type
     * @param setting setting
     * @param value value used for all GTU types
     * @return this factory for method chaining
     */
    public <V> LmrsFactory<T> set(final Setting<V> setting, final V value)
    {
        Throw.whenNull(value, "value");
        final List<V> values = setting.getListFunction().apply(this);
        saveState(setting, values);
        values.clear();
        values.add(value);
        return this;
    }

    /**
     * Sets the setting value for given GTU type.
     * @param <V> value type
     * @param setting setting
     * @param value value
     * @param gtuType GTU type
     * @return this factory for method chaining
     * @throws IllegalArgumentException when the GTU type is not known
     */
    public <V> LmrsFactory<T> set(final Setting<V> setting, final V value, final GtuType gtuType)
    {
        Throw.whenNull(value, "value");
        Throw.whenNull(gtuType, "gtuType");

        int gtuTypeIndex = this.gtuTypes.indexOf(gtuType.getId());
        Throw.when(gtuTypeIndex < 0, IllegalArgumentException.class, "GTU type %s not defined.", gtuType.getId());

        final List<V> values = setting.getListFunction().apply(this);
        saveState(setting, values);

        /*
         * Append with null values if not going from 1 to N but from M to N. This is a weird state with an incomplete command
         * line argument, and code setting a setting. In this state a default or global value is unknown. We try to deal with
         * it, but when the setting is required for any GTU type M+1 through N an exception might arise later. This depends on
         * whether the setting will also be set for those GTU types, or whether the setting is known for a parent type.
         */
        while (values.size() < this.gtuTypes.size())
        {
            values.add(null);
        }
        values.set(gtuTypeIndex, value);
        return this;
    }

    /**
     * Saves the state of given setting when in one-shot mode and the setting was not already saved.
     * @param <V> value type
     * @param setting setting
     * @param values value list
     */
    protected <V> void saveState(final Setting<V> setting, final List<V> values)
    {
        if (this.state != null && !this.state.containsKey(setting))
        {
            this.state.put(setting, new ArrayList<>(values));
        }
    }

    /**
     * Returns value of setting for the GTU type.
     * @param <V> value type
     * @param setting setting
     * @param gtuType GTU type
     * @return value from value array for the GTU type
     */
    // This method is for sub classes, which cannot access the value lists directly
    protected <V> V get(final Setting<V> setting, final GtuType gtuType)
    {
        return get(setting.getListFunction().apply(this), gtuType, gtuType);
    }

    /**
     * Returns value from value array for the GTU type.
     * @param <V> value type
     * @param values values
     * @param gtuType GTU type
     * @return value from value array for the GTU type
     */
    protected <V> V get(final List<V> values, final GtuType gtuType)
    {
        return get(values, gtuType, gtuType);
    }

    /**
     * Returns value from value array for the GTU type.
     * @param <V> value type
     * @param values values
     * @param gtuType GTU type
     * @param originalGtuType original GTU type (only for possible exception message)
     * @return value from value array for the GTU type
     */
    private <V> V get(final List<V> values, final GtuType gtuType, final GtuType originalGtuType)
    {
        Throw.when(values.size() > 1 && this.gtuTypes.size() > values.size(), IllegalArgumentException.class,
                "Argument has %s values %s but %s GTU types are defined.", values.size(), values, this.gtuTypes.size());
        if (values.size() == 1)
        {
            return values.get(0);
        }
        int index = this.gtuTypes.indexOf(gtuType.getId());
        if (index < 0 || values.get(index) == null)
        {
            return get(values,
                    gtuType.getParent().orElseThrow(() -> new IllegalArgumentException(
                            "Unable to obtain setting value for GTU type " + originalGtuType.getId() + " or any parent.")),
                    originalGtuType);
        }
        return values.get(index);
    }

    /**
     * Peek car-following model to support peeked desired speed and desired headway.
     * @param gtuType GTU type
     * @return next car-following model
     */
    private CarFollowingModel peekCarFollowingModel(final GtuType gtuType)
    {
        return this.peekedCarFollowingModel.computeIfAbsent(gtuType, (gt) ->
        {
            DesiredSpeedModel desiredSpeedModel = get(this.socioSpeed, gtuType)
                    ? new SocioDesiredSpeed(AbstractIdm.DESIRED_SPEED) : AbstractIdm.DESIRED_SPEED;
            return get(this.carFollowingModel, gtuType).apply(AbstractIdm.HEADWAY, desiredSpeedModel);
        });
    }

    @Override
    public Optional<Speed> peekDesiredSpeed(final GtuType gtuType, final Speed speedLimit, final Speed maxGtuSpeed,
            final Parameters parameters) throws GtuException
    {
        SpeedLimitInfo sli = new SpeedLimitInfo();
        sli.addSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED, maxGtuSpeed);
        sli.addSpeedInfo(SpeedLimitTypes.FIXED_SIGN, speedLimit);
        return Try.assign(() -> Optional.of(peekCarFollowingModel(gtuType).desiredSpeed(parameters, sli)),
                IllegalStateException.class, "Parameter for desired speed missing.");
    }

    @Override
    public Optional<Length> peekDesiredHeadway(final GtuType gtuType, final Speed speed, final Parameters parameters)
            throws GtuException
    {
        return Try.assign(() -> Optional.of(peekCarFollowingModel(gtuType).desiredHeadway(parameters, speed)),
                IllegalStateException.class, "Parameter for desired headway missing.");
    }

    @Override
    public Parameters getParameters(final GtuType gtuType) throws ParameterException
    {
        ParameterSet parameters = new ParameterSet();
        parameters.setDefaultParameters(LmrsUtil.class);
        parameters.setDefaultParameters(LmrsParameters.class);
        parameters.setDefaultParameters(AbstractIdm.class);
        parameters.setDefaultParameters(ConflictUtil.class);
        parameters.setDefaultParameter(ParameterTypes.T0);
        parameters.setDefaultParameter(ParameterTypes.LANE_STRUCTURE);
        parameters.setDefaultParameter(ParameterTypes.LOOKBACK);
        parameters.setDefaultParameter(ParameterTypes.LOOKAHEAD);
        parameters.setDefaultParameter(ParameterTypes.VCONG);
        parameters.setDefaultParameter(ParameterTypes.LCDUR);
        if (IdmPlusMultiFunction.SINGLETON.equals(get(this.carFollowingModel, gtuType)))
        {
            parameters.setDefaultParameter(IdmPlusMulti.NLEADERS);
        }

        // Fuller
        if (!FullerImplementation.NONE.equals(get(this.fullerImplementation, gtuType)))
        {
            parameters.setParameter(ParameterTypes.TR, Duration.ZERO);
            parameters.setDefaultParameter(AdaptationSituationalAwareness.TR_MAX);
            parameters.setParameter(ChannelFuller.EST_FACTOR, 1.0);
            parameters.setParameter(Fuller.OVER_EST,
                    this.stream.nextDouble() <= get(this.fractionOverEstimation, gtuType) ? 1.0 : -1.0);
            parameters.setDefaultParameter(ChannelTaskScan.TDSCAN);
            if (get(this.headwayAdaptation, gtuType))
            {
                parameters.setDefaultParameter(AdaptationHeadway.BETA_T);
            }
            if (get(this.speedAdaptation, gtuType))
            {
                parameters.setDefaultParameter(AdaptationSpeed.BETA_V0);
            }

            if (FullerImplementation.ATTENTION_MATRIX.equals(get(this.fullerImplementation, gtuType)))
            {
                // Attention matrix
                parameters.setDefaultParameters(ChannelFuller.class);
                parameters.setDefaultParameters(ChannelMental.class);
                if (get(this.updateTimeAdaptation, gtuType))
                {
                    parameters.setDefaultParameter(AdaptationUpdateTime.DT_MIN);
                    parameters.setDefaultParameter(AdaptationUpdateTime.DT_MAX);
                }
                if (get(this.carFollowingTask, gtuType))
                {
                    parameters.setDefaultParameter(ArTaskCarFollowingExp.HEXP);
                }
                if (get(this.trafficLightsTask, gtuType) || get(this.conflictsTask, gtuType))
                {
                    // TODO parameters of enhanced intersection model
                    parameters.setDefaultParameter(ChannelTaskConflict.HEGO);
                    parameters.setDefaultParameter(ChannelTaskConflict.HCONF);
                }
                if (get(this.signalTask, gtuType))
                {
                    parameters.setDefaultParameter(ChannelTaskSignal.TDSIGNAL);
                }
            }
            else
            {
                // Summative or anticipation reliance
                parameters.setDefaultParameters(Fuller.class);
                parameters.setDefaultParameters(SumFuller.class);
                parameters.setDefaultParameter(AdaptationSituationalAwareness.SA);
                parameters.setDefaultParameter(AdaptationSituationalAwareness.SA_MIN);
                parameters.setDefaultParameter(AdaptationSituationalAwareness.SA_MAX);
                if (FullerImplementation.ANTICIPATION_RELIANCE.equals(get(this.fullerImplementation, gtuType)))
                {
                    parameters.setDefaultParameter(ArFuller.ALPHA);
                    parameters.setDefaultParameter(ArFuller.BETA);
                }
                if (get(this.alternateCarFollowingTask, gtuType))
                {
                    parameters.setDefaultParameter(ArTaskCarFollowingExp.HEXP);
                }
            }

        }

        // Social interactions
        if (anySocialInteractions())
        {
            parameters.setDefaultParameter(Tailgating.RHO);
        }
        // Alternate default values in case of social interactions, including distributions for the speed-leading strategy
        if (get(this.tailgating, gtuType))
        {
            parameters.setParameter(ParameterTypes.TMAX, Duration.ofSI(1.6));
        }
        if (get(this.socioLaneChange, gtuType) || get(this.socioSpeed, gtuType))
        {
            parameters.setParameter(LmrsParameters.VGAIN, this.vGainDist.get());
            parameters.setParameter(LmrsParameters.SOCIO, this.sigmaDist.draw());
        }
        parameters.setParameter(ParameterTypes.FSPEED, this.fSpeedDist.draw());
        return parameters;
    }

    @Override
    public T create(final LaneBasedGtu gtu) throws GtuException
    {
        GtuType gtuType = gtu.getType();

        // Car-following model
        CarFollowingModel cfModel = peekCarFollowingModel(gtuType);
        this.peekedCarFollowingModel.remove(gtuType);

        // Perception
        LanePerception perception = getPerception(gtu);

        // Tactical planner
        Synchronization sync = get(this.synchronization, gtuType);
        Cooperation coop = get(this.cooperation, gtuType);
        GapAcceptance gapAccept = get(this.gapAcceptance, gtuType);
        Tailgating tail = get(this.tailgating, gtuType) ? Tailgating.PRESSURE
                : (anySocialInteractions() ? Tailgating.RHO_ONLY : Tailgating.NONE);
        T tacticalPlanner = get(this.lmrsProvider, gtuType).from(cfModel, gtu, perception, sync, coop, gapAccept, tail);

        // Mandatory incentives
        if (get(this.incentiveRoute, gtuType))
        {
            tacticalPlanner.addMandatoryIncentive(IncentiveRoute.SINGLETON);
        }
        if (get(this.incentiveGetInLane, gtuType))
        {
            tacticalPlanner.addMandatoryIncentive(IncentiveGetInLane.SINGLETON);
        }
        get(this.customMandatoryIncentives, gtuType).forEach((s) -> tacticalPlanner.addMandatoryIncentive(s.get()));

        // Voluntary incentives
        if (get(this.incentiveSpeedWithCourtesy, gtuType))
        {
            tacticalPlanner.addVoluntaryIncentive(IncentiveSpeedWithCourtesy.SINGLETON);
        }
        if (get(this.incentiveCourtesy, gtuType))
        {
            tacticalPlanner.addVoluntaryIncentive(IncentiveCourtesy.SINGLETON);
        }
        if (get(this.incentiveQueue, gtuType))
        {
            tacticalPlanner.addVoluntaryIncentive(IncentiveQueue.SINGLETON);
        }
        if (get(this.incentiveStayRight, gtuType))
        {
            tacticalPlanner.addVoluntaryIncentive(IncentiveStayRight.SINGLETON);
        }
        if (get(this.incentiveKeep, gtuType))
        {
            tacticalPlanner.addVoluntaryIncentive(IncentiveKeep.SINGLETON);
        }
        if (get(this.socioLaneChange, gtuType))
        {
            tacticalPlanner.addVoluntaryIncentive(IncentiveSocioSpeed.SINGLETON);
        }
        get(this.customVoluntaryIncentives, gtuType).forEach((s) -> tacticalPlanner.addVoluntaryIncentive(s.get()));

        // Acceleration incentives
        if (get(this.accelerationSpeedLimitTransition, gtuType))
        {
            tacticalPlanner.addAccelerationIncentive(AccelerationSpeedLimitTransition.SINGLETON);
        }
        if (get(this.accelerationTrafficLights, gtuType))
        {
            tacticalPlanner.addAccelerationIncentive(AccelerationTrafficLights.SINGLETON);
        }
        if (get(this.accelerationConflicts, gtuType))
        {
            tacticalPlanner.addAccelerationIncentive(new AccelerationConflicts());
        }
        if (get(this.accelerationNoRightOvertake, gtuType))
        {
            tacticalPlanner.addAccelerationIncentive(AccelerationNoRightOvertake.SINGLETON);
        }
        get(this.customAccelerationIncentives, gtuType).forEach((s) -> tacticalPlanner.addAccelerationIncentive(s.get()));

        // Add AccelerationLaneChangers if the bookkeeping requires it
        if (gtu.getBookkeeping().isEdge())
        {
            tacticalPlanner.addAccelerationIncentive(AccelerationLaneChangers.SINGLETON);
        }

        resetState();

        return tacticalPlanner;
    }

    /**
     * Returns whether social interactions are at play for any of the GTU types.
     * @return whether social interactions are at play for any of the GTU types
     */
    private boolean anySocialInteractions()
    {
        return this.tailgating.contains(true) || this.socioLaneChange.contains(true) || this.socioSpeed.contains(true);
    }

    /**
     * Returns perception for the GTU.
     * @param gtu GTU
     * @return perception for the GTU
     */
    protected LanePerception getPerception(final LaneBasedGtu gtu)
    {
        GtuType gtuType = gtu.getType();

        Mental mental;
        Estimation estimation;
        Anticipation anticipation;

        if (FullerImplementation.ATTENTION_MATRIX.equals(get(this.fullerImplementation, gtuType)))
        {
            // tasks
            LinkedHashSet<Function<LanePerception, Set<ChannelTask>>> taskSuppliers = new LinkedHashSet<>();
            addChannelTask(taskSuppliers, get(this.carFollowingTask, gtuType), ChannelTaskCarFollowing.SUPPLIER);
            addChannelTask(taskSuppliers, get(this.freeAccelerationTask, gtuType), ChannelTaskAcceleration.SUPPLIER);
            addChannelTask(taskSuppliers, get(this.trafficLightsTask, gtuType), ChannelTaskTrafficLight.SUPPLIER);
            addChannelTask(taskSuppliers, get(this.signalTask, gtuType), ChannelTaskSignal.SUPPLIER);
            addChannelTask(taskSuppliers, get(this.laneChangingTask, gtuType), ChannelTaskLaneChange.SUPPLIER);
            addChannelTask(taskSuppliers, get(this.cooperationTask, gtuType), ChannelTaskCooperation.SUPPLIER);
            // TODO update conflict channel task to better intersection/infrastructure based model
            addChannelTask(taskSuppliers, get(this.conflictsTask, gtuType), ChannelTaskConflict.SUPPLIER);
            addChannelTask(taskSuppliers, get(this.roadSideDistractionTask, gtuType),
                    new ChannelTaskRoadSideDistraction.Supplier(gtu));
            addChannelTask(taskSuppliers, true, ChannelTaskScan.SUPPLIER);

            // behavioral adaptation
            Set<BehavioralAdaptation> behavioralAdapatations = new LinkedHashSet<>();
            if (get(this.speedAdaptation, gtuType))
            {
                behavioralAdapatations.add(new AdaptationSpeedChannel());
            }
            if (get(this.headwayAdaptation, gtuType))
            {
                behavioralAdapatations.add(new AdaptationHeadway());
            }
            if (get(this.laneChangeAdaptation, gtuType))
            {
                behavioralAdapatations.add(AdaptationLaneChangeDesire.SINGLETON);
            }
            if (get(this.updateTimeAdaptation, gtuType))
            {
                behavioralAdapatations.add(AdaptationUpdateTime.SINGLETON);
            }

            mental = new ChannelFuller(taskSuppliers, behavioralAdapatations);

            estimation = get(this.neighborEstimation, gtuType) ? FactorEstimation.SINGLETON : Estimation.NONE;
            anticipation = get(this.temporalAnticipation, gtuType) ? Anticipation.CONSTANT_SPEED : Anticipation.NONE;
        }
        else if (FullerImplementation.NONE.equals(get(this.fullerImplementation, gtuType)))
        {
            mental = null;
            estimation = Estimation.NONE;
            anticipation = Anticipation.NONE;
        }
        else
        {
            // SUMMATIVE or ANTICIPATION_RELIANCE
            Set<ArTask> tasks = new LinkedHashSet<>();
            FullerImplementation fullerImpl = get(this.fullerImplementation, gtuType);

            // tasks
            if (get(this.carFollowingTask, gtuType))
            {
                Throw.when(get(this.alternateCarFollowingTask, gtuType), IllegalStateException.class,
                        "Both carFollowingTask and alternateCarFollowingTask are true");
                tasks.add(ArTaskCarFollowing.SINGLETON);
            }
            else if (get(this.alternateCarFollowingTask, gtuType))
            {
                tasks.add(ArTaskCarFollowingExp.SINGLETON);
            }
            if (get(this.laneChangingTask, gtuType))
            {
                Throw.when(get(this.alternateLaneChangingTask, gtuType), IllegalStateException.class,
                        "Both laneChangingTask and alternateLaneChangingTask are true");
                tasks.add(ArTaskLaneChanging.SINGLETON);
            }
            else if (get(this.alternateLaneChangingTask, gtuType))
            {
                tasks.add(ArTaskLaneChangingD.SINGLETON);
            }
            if (get(this.roadSideDistractionTask, gtuType))
            {
                tasks.add(new ArTaskRoadSideDistraction(gtu));
            }

            // behavioral adaptation
            Set<BehavioralAdaptation> behavioralAdapatations = new LinkedHashSet<>();
            if (get(this.speedAdaptation, gtuType))
            {
                behavioralAdapatations.add(new AdaptationSpeed());
            }
            if (get(this.headwayAdaptation, gtuType))
            {
                behavioralAdapatations.add(new AdaptationHeadway());
            }
            if (get(this.laneChangeAdaptation, gtuType))
            {
                behavioralAdapatations.add(AdaptationLaneChangeDesire.SINGLETON);
            }
            behavioralAdapatations.add(new AdaptationSituationalAwareness());

            // summative or anticipation reliance
            if (FullerImplementation.SUMMATIVE.equals(fullerImpl))
            {
                mental = new SumFuller<>(tasks, behavioralAdapatations);
            }
            else if (FullerImplementation.ANTICIPATION_RELIANCE.equals(fullerImpl))
            {
                mental = new ArFuller(tasks, behavioralAdapatations, get(this.primaryTask, gtuType));
            }
            else
            {
                throw new IllegalArgumentException("Unable to load Fuller model from setting " + fullerImpl);
            }

            estimation = get(this.neighborEstimation, gtuType) ? FactorEstimation.SINGLETON : Estimation.NONE;
            anticipation = get(this.temporalAnticipation, gtuType) ? Anticipation.CONSTANT_SPEED : Anticipation.NONE;
        }

        // categories
        LanePerception perception = new CategoricalLanePerception(gtu, mental);
        perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
        perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
        perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
        if (FullerImplementation.NONE.equals(get(this.fullerImplementation, gtuType)))
        {
            perception.addPerceptionCategory(new DirectNeighborsPerception(perception, PerceivedGtuType.WRAP));
            perception.addPerceptionCategory(new DirectIntersectionPerception(perception, PerceivedGtuType.WRAP));
        }
        else if (FullerImplementation.ATTENTION_MATRIX.equals(get(this.fullerImplementation, gtuType)))
        {
            perception.addPerceptionCategory(new NeighborsPerceptionChannel(perception, estimation, anticipation));
            perception.addPerceptionCategory(new IntersectionPerceptionChannel(perception, estimation, anticipation));
        }
        else
        {
            PerceivedGtuType headwayGtuType = new AnticipationPerceivedGtuType(estimation, anticipation, () ->
            {
                return Try.assign(() -> gtu.getParameters().getParameter(ParameterTypes.TR),
                        "Unable to obtain reaction time parameter");
            });
            perception.addPerceptionCategory(new DirectNeighborsPerception(perception, headwayGtuType));
            perception.addPerceptionCategory(new DirectIntersectionPerception(perception, headwayGtuType));
        }
        return perception;
    }

    /**
     * Add channel task if required.
     * @param taskSuppliers suppliers to add task suppliers to
     * @param task whether to add task
     * @param supplier supplier for the task
     */
    private void addChannelTask(final LinkedHashSet<Function<LanePerception, Set<ChannelTask>>> taskSuppliers,
            final boolean task, final Function<LanePerception, Set<ChannelTask>> supplier)
    {
        if (task)
        {
            taskSuppliers.add(supplier);
        }
    }

    // *************************************************************************************************************************
    // *********************************************** Nested Classes and Enums ************************************************
    // *************************************************************************************************************************

    /**
     * Provider for the correct tactical planner implementation.
     * @param <P> tactical planner class
     */
    public interface TacticalPlannerProvider<P extends AbstractIncentivesTacticalPlanner>
    {
        /**
         * Constructs an instance of the correct tactical planner class.
         * @param carFollowingModel car-following model
         * @param gtu GTU
         * @param lanePerception perception
         * @param synchronization type of synchronization
         * @param cooperation type of cooperation
         * @param gapAcceptance gap-acceptance
         * @param tailgating tailgating
         * @return instance of the correct tactical planner class
         */
        P from(CarFollowingModel carFollowingModel, LaneBasedGtu gtu, LanePerception lanePerception,
                Synchronization synchronization, Cooperation cooperation, GapAcceptance gapAcceptance, Tailgating tailgating);
    }

    /**
     * Settings class with static instances.
     * @param <V> setting value type
     */
    public static final class Setting<V>
    {
        // LMRS

        /** Car-following model: IDM, IDM_PLUS (default) or IDM_PLUS_MULTI. */
        public static final Setting<BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>> CAR_FOLLOWING_MODEL =
                new Setting<>((factory) -> factory.carFollowingModel);

        /** Lane change synchronization: PASSIVE (default), PASSIVE_MOVING, ALIGN_GAP or ACTIVE. */
        public static final Setting<Synchronization> SYNCHRONIZATION = new Setting<>((factory) -> factory.synchronization);

        /** Lane change cooperation: PASSIVE (default), PASSIVE_MOVING or ACTIVE. */
        public static final Setting<Cooperation> COOPERATION = new Setting<>((factory) -> factory.cooperation);

        /** Lane change gap-acceptance: INFORMED (default) or EGO_HEADWAY. */
        public static final Setting<GapAcceptance> GAP_ACCEPTANCE = new Setting<>((factory) -> factory.gapAcceptance);

        // LMRS -> Mandatory incentives

        /** Mandatory lane change incentive for route (default: true). */
        public static final Setting<Boolean> INCENTIVE_ROUTE = new Setting<>((factory) -> factory.incentiveRoute);

        /** Mandatory lane change incentive to join slow traffic at split and not block other traffic (default: false). */
        public static final Setting<Boolean> INCENTIVE_GET_IN_LANE = new Setting<>((factory) -> factory.incentiveGetInLane);

        /** Custom mandatory lane change incentives. */
        public static final Setting<Set<Supplier<MandatoryIncentive>>> CUSTOM_MANDATORY_INCENTIVES =
                new Setting<>((factory) -> factory.customMandatoryIncentives);

        // LMRS -> Voluntary incentives

        /** Voluntary lane change incentive for speed with courtesy (default: true). */
        public static final Setting<Boolean> INCENTIVE_SPEED_WITH_COURTESY =
                new Setting<>((factory) -> factory.incentiveSpeedWithCourtesy);

        /** Voluntary lane change incentive for cooperative lane changes (default: false). */
        public static final Setting<Boolean> INCENTIVE_COURTESY = new Setting<>((factory) -> factory.incentiveCourtesy);

        /** Voluntary lane change incentive to join the shortest queue (default: false). */
        public static final Setting<Boolean> INCENTIVE_QUEUE = new Setting<>((factory) -> factory.incentiveQueue);

        /** Voluntary lane change incentive for trucks to stay in slowest rightmost two lanes (default: false). */
        public static final Setting<Boolean> INCENTIVE_STAY_RIGHT = new Setting<>((factory) -> factory.incentiveStayRight);

        /** Voluntary lane change incentive to keep to the slow lane (default: true). */
        public static final Setting<Boolean> INCENTIVE_KEEP = new Setting<>((factory) -> factory.incentiveKeep);

        /** Custom voluntary lane change incentives. */
        public static final Setting<Set<Supplier<VoluntaryIncentive>>> CUSTOM_VOLUNTARY_INCENTIVES =
                new Setting<>((factory) -> factory.customVoluntaryIncentives);

        // LMRS -> Acceleration incentives

        /** Acceleration incentive to slow down prior to a lower speed limit (default: false). */
        public static final Setting<Boolean> ACCELERATION_SPEED_LIMIT_TRANSITION =
                new Setting<>((factory) -> factory.accelerationSpeedLimitTransition);

        /** Acceleration incentive to approach traffic lights (default: false). */
        public static final Setting<Boolean> ACCELERATION_TRAFFIC_LIGHTS =
                new Setting<>((factory) -> factory.accelerationTrafficLights);

        /** Acceleration incentive to approach intersection conflicts (default: false). */
        public static final Setting<Boolean> ACCELERATION_CONFLICTS = new Setting<>((factory) -> factory.accelerationConflicts);

        /** Acceleration incentive to not overtake traffic in the left lane (default: false). */
        public static final Setting<Boolean> ACCELERATION_NO_RIGHT_OVERTAKE =
                new Setting<>((factory) -> factory.accelerationNoRightOvertake);

        /** Custom acceleration incentives. */
        public static final Setting<Set<Supplier<AccelerationIncentive>>> CUSTOM_ACCELERATION_INCENTIVES =
                new Setting<>((factory) -> factory.customAccelerationIncentives);

        // Fuller

        /** Implementation of Fuller: NONE (default), SUMMATIVE, ANTICIPATION_RELIANCE or ATTENTION_MATRIX. */
        public static final Setting<FullerImplementation> FULLER_IMPLEMENTATION =
                new Setting<>((factory) -> factory.fullerImplementation);

        /** Id of primary task under ANTICIPATION_RELIANCE (default: lane-changing). */
        public static final Setting<String> PRIMARY_TASK = new Setting<>((factory) -> factory.primaryTask);

        /** Enables temporal constant-speed anticipation (default: true). */
        public static final Setting<Boolean> TEMPORAL_ANTICIPATION = new Setting<>((factory) -> factory.temporalAnticipation);

        /** Enables estimation of neighboring vehicles. */
        public static final Setting<Boolean> ESTIMATION = new Setting<>((factory) -> factory.neighborEstimation);

        /** Fraction of drivers over-estimating speed and distance [0..1] (default: 0). */
        public static final Setting<Double> FRACTION_OVERESTIMATION =
                new Setting<>((factory) -> factory.fractionOverEstimation);

        // Fuller -> Tasks

        /** Enables car-following task (default: true). */
        public static final Setting<Boolean> TASK_CAR_FOLLOWING = new Setting<>((factory) -> factory.carFollowingTask);

        /** Enables alternate car-following task (default: false). */
        public static final Setting<Boolean> TASK_CAR_FOLLOWING_ALTERNATE =
                new Setting<>((factory) -> factory.alternateCarFollowingTask);

        /** Enables free acceleration task under ATTENTION_MATRIX (default: false). */
        public static final Setting<Boolean> TASK_FREE_ACCELERATION = new Setting<>((factory) -> factory.freeAccelerationTask);

        /** Enables traffic lights task under ATTENTION_MATRIX (default: false). */
        public static final Setting<Boolean> TASK_TRAFFIC_LIGHTS = new Setting<>((factory) -> factory.trafficLightsTask);

        /** Enables signal task under ATTENTION_MATRIX (default: true). */
        public static final Setting<Boolean> TASK_SIGNAL = new Setting<>((factory) -> factory.signalTask);

        /** Enables lane-changing task (default: true). */
        public static final Setting<Boolean> TASK_LANE_CHANGE = new Setting<>((factory) -> factory.laneChangingTask);

        /** Enables alternate lane-changing task (default: false). */
        public static final Setting<Boolean> TASK_LANE_CHANGE_ALTERNATE =
                new Setting<>((factory) -> factory.alternateLaneChangingTask);

        /** Enables cooperation task under ATTENTION_MATRIX (default: true). */
        public static final Setting<Boolean> TASK_COOPERATION = new Setting<>((factory) -> factory.cooperationTask);

        /** Enables conflicts task under ATTENTION_MATRIX (default: false). */
        public static final Setting<Boolean> TASK_CONFLICTS = new Setting<>((factory) -> factory.conflictsTask);

        /** Enables road-side distraction task (default: false). */
        public static final Setting<Boolean> TASK_ROADSIDE_DISTRACTION =
                new Setting<>((factory) -> factory.roadSideDistractionTask);

        // Fuller -> Adaptations

        /** Enables behavioral speed adaptation (default: true). */
        public static final Setting<Boolean> ADAPTATION_SPEED = new Setting<>((factory) -> factory.speedAdaptation);

        /** Enables behavioral headway adaptation (default: true). */
        public static final Setting<Boolean> ADAPTATION_HEADWAY = new Setting<>((factory) -> factory.headwayAdaptation);

        /** Enables behavioral voluntary lane change adaptation (default: false). */
        public static final Setting<Boolean> ADAPTATION_LANE_CHANGE = new Setting<>((factory) -> factory.laneChangeAdaptation);

        /** Enables behavioral update time adaptation under ATTENTION_MATRIX (default: false). */
        public static final Setting<Boolean> ADAPTATION_UPDATE_TIME = new Setting<>((factory) -> factory.updateTimeAdaptation);

        // Social interactions

        /** Enables tailgating. Without tailgating, any social interaction still results in social pressure (default: false). */
        public static final Setting<Boolean> SOCIO_TAILGATING = new Setting<>((factory) -> factory.tailgating);

        /** Enables lane changes due to social pressure (default: false). */
        public static final Setting<Boolean> SOCIO_LANE_CHANGE = new Setting<>((factory) -> factory.socioLaneChange);

        /** Enables speed increase due to social pressure (default: false). */
        public static final Setting<Boolean> SOCIO_SPEED = new Setting<>((factory) -> factory.socioSpeed);

        /** Function to return the right list. */
        private final Function<LmrsFactory<?>, List<V>> listFunction;

        /**
         * Constructor.
         * @param listFunction function to return the right list from the factory
         */
        private Setting(final Function<LmrsFactory<?>, List<V>> listFunction)
        {
            this.listFunction = listFunction;
        }

        /**
         * Returns the list function to return the right list from the factory.
         * @return list function to return the right list from the factory
         */
        public Function<LmrsFactory<?>, List<V>> getListFunction()
        {
            return this.listFunction;
        }
    }

    /**
     * Type of management of different tasks.
     */
    public enum FullerImplementation
    {
        /** No perception model. */
        NONE,

        /** Task demands are summed. */
        SUMMATIVE,

        /**
         * Task demand based on one primary and multiple auxiliary tasks. Requires parameters ALPHA and BETA and the id of the
         * primary task.
         */
        ANTICIPATION_RELIANCE,

        /** Task demand as steady-state in the attention matrix. */
        ATTENTION_MATRIX;
    }

    /**
     * Argument converter for car-following supplier.
     */
    private static class CarFollowingModelConverter
            implements ITypeConverter<BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>>
    {
        /**
         * Constructor.
         */
        @SuppressWarnings({"unused", "redundantModifier"}) // referred to in @Option annotation
        public CarFollowingModelConverter()
        {
            //
        }

        @Override
        public BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel> convert(final String value)
                throws Exception
        {
            return switch (value.toLowerCase())
            {
                case "idm" -> Idm::new;
                case "idm_plus" -> IdmPlus::new;
                case "idm_plus_multi" -> IdmPlusMultiFunction.SINGLETON;
                default -> throw new IllegalArgumentException(
                        "Unable to parse car following model " + value + ". Use any of IDM, IDM_PLUS or IDM_PLUS_MULTI.");
            };
        }
    }

    /**
     * Recognition interface to add nLeaders parameter.
     */
    public static final class IdmPlusMultiFunction
            implements BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel>, Stateless<IdmPlusMultiFunction>
    {
        /** Singleton instance. */
        public static final IdmPlusMultiFunction SINGLETON = new IdmPlusMultiFunction();

        @Override
        public CarFollowingModel apply(final DesiredHeadwayModel t, final DesiredSpeedModel u)
        {
            return new IdmPlusMulti(t, u);
        }

        @Override
        public IdmPlusMultiFunction get()
        {
            return SINGLETON;
        }
    }

    /**
     * Argument converter for {@link Synchronization}.
     */
    private static class SynchronizationConverter implements ITypeConverter<Synchronization>
    {
        /**
         * Constructor.
         */
        @SuppressWarnings({"unused", "redundantModifier"}) // referred to in @Option annotation
        public SynchronizationConverter()
        {
            //
        }

        @Override
        public Synchronization convert(final String value) throws Exception
        {
            switch (value.toLowerCase())
            {
                case "passive":
                    return Synchronization.PASSIVE;
                case "passive_moving":
                    return Synchronization.PASSIVE_MOVING;
                case "align_gap":
                    return Synchronization.ALIGN_GAP;
                case "active":
                    return Synchronization.ACTIVE;
                default:
                    throw new IllegalArgumentException("Unable to parse synchronization " + value
                            + ". Use any of PASSIVE, PASSIVE_MOVING, ALIGN_GAP or ACTIVE.");
            }
        }
    }

    /**
     * Argument converter for {@link Cooperation}.
     */
    private static class CooperationConverter implements ITypeConverter<Cooperation>
    {
        /**
         * Constructor.
         */
        @SuppressWarnings({"unused", "redundantModifier"}) // referred to in @Option annotation
        public CooperationConverter()
        {
            //
        }

        @Override
        public Cooperation convert(final String value) throws Exception
        {
            switch (value.toLowerCase())
            {
                case "passive":
                    return Cooperation.PASSIVE;
                case "passive_moving":
                    return Cooperation.PASSIVE_MOVING;
                case "active":
                    return Cooperation.ACTIVE;
                default:
                    throw new IllegalArgumentException(
                            "Unable to parse cooperation " + value + ". Use any of PASSIVE, PASSIVE_MOVING or ACTIVE.");
            }
        }
    }

    /**
     * Argument converter for {@link GapAcceptance}.
     */
    private static class GapAcceptanceConverter implements ITypeConverter<GapAcceptance>
    {
        /**
         * Constructor.
         */
        @SuppressWarnings({"unused", "redundantModifier"}) // referred to in @Option annotation
        public GapAcceptanceConverter()
        {
            //
        }

        @Override
        public GapAcceptance convert(final String value) throws Exception
        {
            switch (value.toLowerCase())
            {
                case "informed":
                    return GapAcceptance.INFORMED;
                case "ego_headway":
                    return GapAcceptance.EGO_HEADWAY;
                default:
                    throw new IllegalArgumentException(
                            "Unable to parse gap-acceptance " + value + ". Use any of INFORMED or EGO_HEADWAY.");
            }
        }
    }

}
