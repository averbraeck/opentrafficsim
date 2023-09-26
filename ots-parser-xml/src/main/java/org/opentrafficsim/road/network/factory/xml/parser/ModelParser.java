package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectBusStopPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType.PerceivedHeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationHeadway;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSpeed;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;
import org.opentrafficsim.road.gtu.lane.perception.mental.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskManager;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskManager.SummativeTaskManager;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.ModelComponentFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.Idm;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationBusStop;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationConflicts;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationNoRightOvertake;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationSpeedLimitTransition;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationTrafficLights;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLmrsPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveBusStop;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveDummy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveGetInLane;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveStayRight;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.SocioDesiredSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.ParseDistribution;
import org.opentrafficsim.xml.bindings.types.ClassType;
import org.opentrafficsim.xml.generated.CarFollowingModelType;
import org.opentrafficsim.xml.generated.DesiredHeadwayModelType;
import org.opentrafficsim.xml.generated.DesiredSpeedModelType;
import org.opentrafficsim.xml.generated.ModelType;
import org.opentrafficsim.xml.generated.ModelType.ModelParameters.AccelerationDist;
import org.opentrafficsim.xml.generated.ModelType.ModelParameters.DoubleDist;
import org.opentrafficsim.xml.generated.ModelType.ModelParameters.Fraction;
import org.opentrafficsim.xml.generated.ModelType.ModelParameters.FrequencyDist;
import org.opentrafficsim.xml.generated.ModelType.ModelParameters.IntegerDist;
import org.opentrafficsim.xml.generated.ModelType.ModelParameters.LengthDist;
import org.opentrafficsim.xml.generated.ModelType.ModelParameters.LinearDensityDist;
import org.opentrafficsim.xml.generated.ModelType.ModelParameters.SpeedDist;
import org.opentrafficsim.xml.generated.PerceptionType;
import org.opentrafficsim.xml.generated.PerceptionType.HeadwayGtuType.Perceived;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistNormal;

/**
 * Parser of the {@code Model} tags. Returns a map of strategical planner factories by model ID for use in demand parsing.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ModelParser
{
    /** */
    private ModelParser()
    {
        // static class
    }

    /**
     * Parse parameter factories.
     * @param definitions Definitions; parsed definitions.
     * @param models List&lt;MODEL&gt;; models
     * @param eval Eval; expression evaluator
     * @param parameterTypes Map&lt;String, ParameterType&lt;?&gt;&gt;; parameter types
     * @param streamMap Map&lt;String, StreamInformation&gt;; stream information
     * @param <U> a unit
     * @param <T> a scalar type
     * @param <K> a parameter type value
     * @return Map&lt;String, ParameterFactory&lt;?&gt;&gt;; parameter factories by model ID
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    @SuppressWarnings("unchecked")
    public static <U extends Unit<U>, T extends DoubleScalarRel<U, T>, K> ParameterFactory parseParameters(
            final Definitions definitions, final List<ModelType> models, final Eval eval,
            final Map<String, ParameterType<?>> parameterTypes, final StreamInformation streamMap) throws XmlParserException
    {
        ParameterFactoryByType parameterFactory = new ParameterFactoryByType();
        for (ModelType model : models)
        {
            GtuType gtuType = definitions.get(GtuType.class, model.getGtuType().get(eval));
            // Parameter factory

            // set model parameters
            if (model.getModelParameters() != null)
            {
                for (Serializable parameter : model.getModelParameters().getDurationOrDurationDistOrLength())
                {
                    if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.String)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.String p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.String) parameter;
                        parameterFactory.addParameter(gtuType, (ParameterType<String>) parameterTypes.get(p.getId().get(eval)),
                                p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Acceleration)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Acceleration p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Acceleration) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Acceleration>) parameterTypes.get(p.getId().get(eval)),
                                p.getValue().get(eval));
                    }
                    else if (parameter instanceof AccelerationDist)
                    {
                        AccelerationDist p = (AccelerationDist) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Acceleration>) parameterTypes.get(p.getId().get(eval)),
                                ParseDistribution.parseContinuousDist(streamMap, p, p.getAccelerationUnit().get(eval), eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Boolean)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Boolean p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Boolean) parameter;
                        parameterFactory.addParameter(gtuType, (ParameterType<Boolean>) parameterTypes.get(p.getId().get(eval)),
                                p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterType<Class<?>>) parameterTypes.get(p.getId().get(eval)), p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Double)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Double p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Double) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Double>) parameterTypes.get(p.getId().get(eval)), p.getValue().get(eval));
                    }
                    else if (parameter instanceof DoubleDist)
                    {
                        DoubleDist p = (DoubleDist) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Double>) parameterTypes.get(p.getId().get(eval)),
                                ParseDistribution.makeDistContinuous(streamMap, p, eval));
                    }
                    else if (parameter instanceof Fraction)
                    {
                        Fraction p = (Fraction) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Double>) parameterTypes.get(p.getId().get(eval)), p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Frequency)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Frequency p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Frequency) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Frequency>) parameterTypes.get(p.getId().get(eval)),
                                p.getValue().get(eval));
                    }
                    else if (parameter instanceof FrequencyDist)
                    {
                        FrequencyDist p = (FrequencyDist) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Frequency>) parameterTypes.get(p.getId().get(eval)),
                                ParseDistribution.parseContinuousDist(streamMap, p, p.getFrequencyUnit().get(eval), eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Integer)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Integer p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Integer) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Integer>) parameterTypes.get(p.getId().get(eval)),
                                p.getValue().get(eval).intValue());
                    }
                    else if (parameter instanceof IntegerDist)
                    {
                        IntegerDist p = (IntegerDist) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Integer>) parameterTypes.get(p.getId().get(eval)),
                                ParseDistribution.makeDistDiscrete(streamMap, p, eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Length)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Length p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Length) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Length>) parameterTypes.get(p.getId().get(eval)), p.getValue().get(eval));
                    }
                    else if (parameter instanceof LengthDist)
                    {
                        LengthDist p = (LengthDist) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Length>) parameterTypes.get(p.getId().get(eval)),
                                ParseDistribution.parseContinuousDist(streamMap, p, p.getLengthUnit().get(eval), eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.LinearDensity)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.LinearDensity p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.LinearDensity) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<LinearDensity>) parameterTypes.get(p.getId().get(eval)),
                                p.getValue().get(eval));
                    }
                    else if (parameter instanceof LinearDensityDist)
                    {
                        LinearDensityDist p = (LinearDensityDist) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<LinearDensity>) parameterTypes.get(p.getId().get(eval)),
                                ParseDistribution.parseContinuousDist(streamMap, p, p.getLinearDensityUnit().get(eval), eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Speed)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Speed p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Speed) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Speed>) parameterTypes.get(p.getId().get(eval)), p.getValue().get(eval));
                    }
                    else if (parameter instanceof SpeedDist)
                    {
                        SpeedDist p = (SpeedDist) parameter;
                        parameterFactory.addParameter(gtuType,
                                (ParameterTypeNumeric<Speed>) parameterTypes.get(p.getId().get(eval)),
                                ParseDistribution.parseContinuousDist(streamMap, p, p.getSpeedUnit().get(eval), eval));
                    }
                }
            }
        }
        return parameterFactory;
    }

    /**
     * Creates strategical planner factories for models.
     * @param otsNetwork RoadNetwork; network
     * @param models List&lt;MODEL&gt;; models
     * @param eval Eval; expression evaluator
     * @param parameterTypes Map&lt;String, ParameterType&lt;?&gt;&gt;; parameter types
     * @param streamInformation Map&lt;String, StreamInformation&gt;; stream information
     * @param parameterFactory ParameterFactory; parameter factories
     * @param <U> a unit
     * @param <T> a scalar type
     * @param <K> a parameter type value
     * @return Map&lt;String, LaneBasedStrategicalPlannerFactory&lt;?&gt;&gt;; strategical planner factories by model ID
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    public static <U extends Unit<U>, T extends DoubleScalarRel<U, T>,
            K> Map<String, LaneBasedStrategicalPlannerFactory<?>> parseModel(final RoadNetwork otsNetwork,
                    final List<ModelType> models, final Eval eval, final Map<String, ParameterType<?>> parameterTypes,
                    final StreamInformation streamInformation, final ParameterFactory parameterFactory)
                    throws XmlParserException
    {
        Map<String, LaneBasedStrategicalPlannerFactory<?>> factories = new LinkedHashMap<>();
        for (ModelType model : models)
        {
            // Tactical planner
            LaneBasedTacticalPlannerFactory<?> tacticalPlannerFactory;
            if (model.getTacticalPlanner() != null)
            {
                if (model.getTacticalPlanner().getLmrs() != null)
                {
                    tacticalPlannerFactory = parseLmrs(model.getTacticalPlanner().getLmrs(), streamInformation, eval);
                }
                else
                {
                    throw new XmlParserException("Tactical planner has unsupported value.");
                }
            }
            else
            {
                // default
                tacticalPlannerFactory = new LmrsFactory(new IdmPlusFactory(streamInformation.getStream("generation")),
                        new DefaultLmrsPerceptionFactory());
            }

            LaneBasedStrategicalPlannerFactory<?> strategicalPlannerFactory;
            if (model.getStrategicalPlanner() == null || model.getStrategicalPlanner().getRoute() != null)
            {
                // TODO: RouteGeneratorOD as third argument, which may however be based on demand
                // TODO: model.getStrategicalPlanner().getRoute() defines route finding procedure: NONE|SHORTEST
                strategicalPlannerFactory =
                        new LaneBasedStrategicalRoutePlannerFactory(tacticalPlannerFactory, parameterFactory);
            }
            else
            {
                throw new XmlParserException("Strategical planner has unsupported value.");
            }

            factories.put(model.getId(), strategicalPlannerFactory);
        }
        return factories;
    }

    /**
     * Parse Lmrs model.
     * @param lmrs org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs; Lmrs information
     * @param streamInformation Map&lt;String, StreamInformation&gt;; stream information
     * @param eval Eval; expression evaluator.
     * @return LaneBasedTacticalPlannerFactory&lt;Lmrs&gt;; Lmrs factory
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    @SuppressWarnings("unchecked")
    private static LaneBasedTacticalPlannerFactory<Lmrs> parseLmrs(
            final org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs lmrs,
            final StreamInformation streamInformation, final Eval eval) throws XmlParserException
    {
        // Synchronization
        Synchronization synchronization =
                lmrs.getSynchronization() != null ? lmrs.getSynchronization().get(eval) : Synchronization.PASSIVE;

        // Cooperation
        Cooperation cooperation = lmrs.getCooperation() != null ? lmrs.getCooperation().get(eval) : Cooperation.PASSIVE;

        // Gap-acceptance
        GapAcceptance gapAcceptance =
                lmrs.getGapAcceptance() != null ? lmrs.getGapAcceptance().get(eval) : GapAcceptance.INFORMED;

        // Tailgating
        Tailgating tailgating = lmrs.getTailgating() != null ? lmrs.getTailgating().get(eval) : Tailgating.NONE;

        // Mandatory incentives
        Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
        if (!lmrs.getMandatoryIncentives().getRoute().isEmpty())
        {
            mandatoryIncentives.add(new IncentiveRoute());
        }
        if (!lmrs.getMandatoryIncentives().getGetInLane().isEmpty())
        {
            mandatoryIncentives.add(new IncentiveGetInLane());
        }
        if (!lmrs.getMandatoryIncentives().getBusStop().isEmpty())
        {
            mandatoryIncentives.add(new IncentiveBusStop());
        }
        for (ClassType classType : lmrs.getMandatoryIncentives().getClazz())
        {
            try
            {
                mandatoryIncentives.add(
                        (MandatoryIncentive) ClassUtil.resolveConstructor(classType.get(eval), new Class<?>[0]).newInstance());
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | ClassCastException exception)
            {
                throw new XmlParserException("Class " + classType.get(eval)
                        + " does not have a valid empty contructor, or is not a MandatoryIncentive.");
            }
        }
        if (mandatoryIncentives.isEmpty())
        {
            mandatoryIncentives.add(new IncentiveDummy());
        }

        // Voluntary incentives
        Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
        if (!lmrs.getVoluntaryIncentives().getKeep().isEmpty())
        {
            voluntaryIncentives.add(new IncentiveKeep());
        }
        if (!lmrs.getVoluntaryIncentives().getSpeedWithCourtesy().isEmpty())
        {
            voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
        }
        if (!lmrs.getVoluntaryIncentives().getCourtesy().isEmpty())
        {
            voluntaryIncentives.add(new IncentiveCourtesy());
        }
        if (!lmrs.getVoluntaryIncentives().getSocioSpeed().isEmpty())
        {
            voluntaryIncentives.add(new IncentiveSocioSpeed());
        }
        if (!lmrs.getVoluntaryIncentives().getStayRight().isEmpty())
        {
            voluntaryIncentives.add(new IncentiveStayRight());
        }
        for (ClassType classType : lmrs.getVoluntaryIncentives().getClazz())
        {
            try
            {
                voluntaryIncentives.add(
                        (VoluntaryIncentive) ClassUtil.resolveConstructor(classType.get(eval), new Class<?>[0]).newInstance());
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | ClassCastException exception)
            {
                throw new XmlParserException("Class " + classType.get(eval)
                        + " does not have a valid empty contructor, or is not a VoluntaryIncentive.");
            }
        }

        // Acceleration incentives
        Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
        if (!lmrs.getAccelerationIncentives().getBusStop().isEmpty())
        {
            accelerationIncentives.add(new AccelerationBusStop());
        }
        if (!lmrs.getAccelerationIncentives().getConflicts().isEmpty())
        {
            accelerationIncentives.add(new AccelerationConflicts());
        }
        if (!lmrs.getAccelerationIncentives().getSpeedLimitTransitions().isEmpty())
        {
            accelerationIncentives.add(new AccelerationSpeedLimitTransition());
        }
        if (!lmrs.getAccelerationIncentives().getTrafficLights().isEmpty())
        {
            accelerationIncentives.add(new AccelerationTrafficLights());
        }
        if (!lmrs.getAccelerationIncentives().getNoRightOvertake().isEmpty())
        {
            accelerationIncentives.add(new AccelerationNoRightOvertake());
        }
        for (ClassType classType : lmrs.getAccelerationIncentives().getClazz())
        {
            try
            {
                accelerationIncentives.add((AccelerationIncentive) ClassUtil
                        .resolveConstructor(classType.get(eval), new Class<?>[0]).newInstance());
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | ClassCastException exception)
            {
                throw new XmlParserException("Class " + classType.get(eval)
                        + " does not have a valid empty contructor, or is not a AccelerationIncentive.");
            }
        }

        // Perception
        PerceptionFactory perceptionFactory = parsePerception(lmrs.getPerception(), eval);

        // Car-following model
        CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory =
                parseCarFollowingModel(lmrs.getCarFollowingModel(), streamInformation, eval);

        // Lmrs factory
        return new LmrsFactory(carFollowingModelFactory, perceptionFactory, synchronization, cooperation, gapAcceptance,
                tailgating, mandatoryIncentives, voluntaryIncentives, accelerationIncentives);
    }

    /**
     * Parse car-following model.
     * @param carFollowingModel CarFollowingModelType; car-following model information
     * @param streamInformation Map&lt;String, StreamInformation&gt;; stream information
     * @param eval Eval; expression evaluator.
     * @return CarFollowingModelFactory&lt;? extends CarFollowingModel&gt;; car-following model factory
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    private static CarFollowingModelFactory<? extends CarFollowingModel> parseCarFollowingModel(
            final CarFollowingModelType carFollowingModel, final StreamInformation streamInformation, final Eval eval)
            throws XmlParserException
    {
        // This method works for either IDM or IDM+; for other car-following models the structure needs to be elaborated
        BiFunction<DesiredHeadwayModel, DesiredSpeedModel, CarFollowingModel> carFollowingModelFunction;
        Factory<DesiredHeadwayModel> desiredHeadwayModelFactory;
        Factory<DesiredSpeedModel> desiredSpeedModelFactory;
        if (carFollowingModel.getIdm() != null)
        {
            carFollowingModelFunction = (headway, speed) -> new Idm(headway, speed);
            desiredHeadwayModelFactory = parseDesiredHeadwayModel(carFollowingModel.getIdm().getDesiredHeadwayModel(), eval);
            desiredSpeedModelFactory = parseDesiredSpeedModel(carFollowingModel.getIdm().getDesiredSpeedModel(), eval);
        }
        else if (carFollowingModel.getIdmPlus() != null)
        {
            carFollowingModelFunction = (headway, speed) -> new IdmPlus(headway, speed);
            desiredHeadwayModelFactory =
                    parseDesiredHeadwayModel(carFollowingModel.getIdmPlus().getDesiredHeadwayModel(), eval);
            desiredSpeedModelFactory = parseDesiredSpeedModel(carFollowingModel.getIdmPlus().getDesiredSpeedModel(), eval);
        }
        else
        {
            throw new XmlParserException("Car-following model has unsupported value.");
        }

        DistContinuous fSpeed = new DistNormal(streamInformation.getStream("generation"), 123.7 / 120.0, 0.1);
        return new CarFollowingModelFactory<CarFollowingModel>()
        {
            /** {@inheritDoc} */
            @Override
            public Parameters getParameters() throws ParameterException
            {
                // Note when extending for more car-following models, these parameters are IDM specific
                ParameterSet parameters = new ParameterSet();
                desiredHeadwayModelFactory.getParameters().setAllIn(parameters);
                desiredSpeedModelFactory.getParameters().setAllIn(parameters);
                parameters.setDefaultParameters(AbstractIdm.class);
                parameters.setParameter(ParameterTypes.FSPEED, fSpeed.draw());
                return parameters;
            }

            /** {@inheritDoc} */
            @Override
            public CarFollowingModel generateCarFollowingModel()
            {
                return carFollowingModelFunction.apply(desiredHeadwayModelFactory.get(), desiredSpeedModelFactory.get());
            }
        };
    }

    /**
     * Parse desired headway model.
     * @param desiredHeadwayModel DesiredHeadwayModelType; desired headway model tag.
     * @param eval Eval; expression evaluator.
     * @return Factory for desired headway model.
     * @throws XmlParserException when no supported tag is provided.
     */
    @SuppressWarnings("unchecked")
    private static Factory<DesiredHeadwayModel> parseDesiredHeadwayModel(final DesiredHeadwayModelType desiredHeadwayModel,
            final Eval eval) throws XmlParserException
    {
        if (desiredHeadwayModel.getIdm() != null)
        {
            return new Factory<>()
            {
                /** {@inheritDoc} */
                @Override
                public Parameters getParameters() throws ParameterException
                {
                    return new ParameterSet(); // IDM factory takes care of parameters for default model
                }

                /** {@inheritDoc} */
                @Override
                public DesiredHeadwayModel get()
                {
                    return Idm.HEADWAY;
                }
            };
        }
        else if (desiredHeadwayModel.getClazz() != null)
        {
            return getFactoryForClass((Class<DesiredHeadwayModel>) desiredHeadwayModel.getClazz().get(eval));
        }
        throw new XmlParserException("Desired headway model has unsupported value.");
    }

    /**
     * Parse desired speed model.
     * @param desiredSpeedModel DesiredSpeedModelType; desired speed model tag.
     * @param eval Eval; expression evaluator.
     * @return Factory for desired headway model.
     * @throws XmlParserException when no supported tag is provided.
     */
    @SuppressWarnings("unchecked")
    private static Factory<DesiredSpeedModel> parseDesiredSpeedModel(final DesiredSpeedModelType desiredSpeedModel,
            final Eval eval) throws XmlParserException
    {
        if (desiredSpeedModel.getIdm() != null)
        {
            return new Factory<>()
            {
                /** {@inheritDoc} */
                @Override
                public Parameters getParameters() throws ParameterException
                {
                    return new ParameterSet(); // IDM factory takes care of parameters for default model
                }

                /** {@inheritDoc} */
                @Override
                public DesiredSpeedModel get()
                {
                    return Idm.DESIRED_SPEED;
                }
            };
        }
        else if (desiredSpeedModel.getSocio() != null)
        {
            Throw.when(desiredSpeedModel.getSocio().getSocio() != null, XmlParserException.class,
                    "Socio desired speed model wraps another socio desired speed model. This is not allowed.");
            Factory<DesiredSpeedModel> wrapped = parseDesiredSpeedModel(desiredSpeedModel.getSocio(), eval);
            return new Factory<>()
            {
                /** {@inheritDoc} */
                @Override
                public Parameters getParameters() throws ParameterException
                {
                    ParameterSet parameters = new ParameterSet();
                    wrapped.getParameters().setAllIn(parameters);
                    parameters.setDefaultParameters(SocioDesiredSpeed.class);
                    return parameters;
                }

                /** {@inheritDoc} */
                @Override
                public DesiredSpeedModel get()
                {
                    return new SocioDesiredSpeed(wrapped.get());
                }
            };
        }
        else if (desiredSpeedModel.getClazz() != null)
        {
            return getFactoryForClass((Class<DesiredSpeedModel>) desiredSpeedModel.getClazz().get(eval));
        }
        throw new XmlParserException("Desired speed model has unsupported value.");
    }

    /**
     * Returns a factory for a class with empty constructor. The parameters statically defined in the class will be provided by
     * the factory with default values, e.g. {@code static MyClass.MY_PARAMETER = new ParameterTypeAcceleration(...)}.
     * @param clazz Class&lt;T&gt;; class.
     * @param <T> model component type .
     * @return Factory for component.
     * @throws XmlParserException if the class or empty constructor cannot be found.
     */
    private static <T> Factory<T> getFactoryForClass(final Class<T> clazz) throws XmlParserException
    {
        Constructor<? extends T> constructor =
                Try.assign(() -> (Constructor<? extends T>) ClassUtil.resolveConstructor(clazz, new Object[0]),
                        XmlParserException.class, "Class %s does not have a valid empty constructor.", clazz);
        return new Factory<>()
        {
            /** {@inheritDoc} */
            @Override
            public Parameters getParameters() throws ParameterException
            {
                ParameterSet parameters = new ParameterSet();
                parameters.setDefaultParameters(clazz);
                return parameters;
            }

            /** {@inheritDoc} */
            @Override
            public T get()
            {
                return Try.assign(() -> constructor.newInstance(), "Exception while instantiating a instance of class %s.",
                        clazz);
            }
        };
    }

    /**
     * Defines a simple factory for model components, combining {@code ModelComponentFactory.getParameters()} with a
     * {@code get()} method.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <T> model component type.
     */
    private static interface Factory<T> extends ModelComponentFactory
    {
        /**
         * Returns component.
         * @return T; component.
         */
        T get();
    }

    /**
     * Parse perception for any tactical planner that has PerceptionType to support perception.
     * @param perception PerceptionType; perception xml information
     * @param eval Eval; expression evaluator.
     * @return PerceptionFactory; parsed perception factory
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    @SuppressWarnings("unchecked")
    private static <G extends Gtu> PerceptionFactory parsePerception(final PerceptionType perception, final Eval eval)
            throws XmlParserException
    {
        // Headway GTU type
        HeadwayGtuType headwayGtuType;
        if (perception.getHeadwayGtuType().getWrap() != null)
        {
            headwayGtuType = HeadwayGtuType.WRAP;
        }
        else if (perception.getHeadwayGtuType().getPerceived() != null)
        {
            Perceived perceived = perception.getHeadwayGtuType().getPerceived();
            Estimation estimation = perceived.getEstimation() == null ? Estimation.NONE : perceived.getEstimation().get(eval);
            Anticipation anticipation =
                    perceived.getAnticipation() == null ? Anticipation.NONE : perceived.getAnticipation().get(eval);
            headwayGtuType = new PerceivedHeadwayGtuType(estimation, anticipation);
        }
        else
        {
            throw new XmlParserException("HeadwayGtuType is unknown.");
        }

        // Categories
        @SuppressWarnings("rawtypes")
        List<Constructor<? extends PerceptionCategory>> categoryConstructorsPerception = new ArrayList<>();
        @SuppressWarnings("rawtypes")
        List<Constructor<? extends PerceptionCategory>> categoryConstructorsPerceptionHeadway = new ArrayList<>();
        Class<?>[] perceptionConstructor = new Class[] {LanePerception.class};
        Class<?>[] perceptionHeadwayConstructor = new Class[] {LanePerception.class, HeadwayGtuType.class};
        try
        {
            if (!perception.getCategories().getEgo().isEmpty())
            {
                categoryConstructorsPerception
                        .add(ClassUtil.resolveConstructor(DirectEgoPerception.class, perceptionConstructor));
            }
            if (!perception.getCategories().getBusStop().isEmpty())
            {
                categoryConstructorsPerception
                        .add(ClassUtil.resolveConstructor(DirectBusStopPerception.class, perceptionConstructor));
            }
            if (!perception.getCategories().getInfrastructure().isEmpty())
            {
                categoryConstructorsPerception
                        .add(ClassUtil.resolveConstructor(DirectInfrastructurePerception.class, perceptionConstructor));
            }
            if (!perception.getCategories().getIntersection().isEmpty())
            {
                categoryConstructorsPerception
                        .add(ClassUtil.resolveConstructor(DirectIntersectionPerception.class, perceptionHeadwayConstructor));
            }
            if (!perception.getCategories().getNeighbors().isEmpty())
            {
                categoryConstructorsPerception
                        .add(ClassUtil.resolveConstructor(DirectNeighborsPerception.class, perceptionHeadwayConstructor));
            }
            if (!perception.getCategories().getTraffic().isEmpty())
            {
                categoryConstructorsPerception
                        .add(ClassUtil.resolveConstructor(AnticipationTrafficPerception.class, perceptionHeadwayConstructor));
            }
            for (ClassType classType : perception.getCategories().getClazz())
            {
                Class<? extends PerceptionCategory<?, ?>> clazz = classType.get(eval);
                Constructor<? extends PerceptionCategory<?, ?>> constructor;
                try
                {
                    constructor = ClassUtil.resolveConstructor(clazz, perceptionHeadwayConstructor);
                    categoryConstructorsPerceptionHeadway.add(constructor);
                }
                catch (NoSuchMethodException exception)
                {
                    try
                    {
                        constructor = ClassUtil.resolveConstructor(clazz, perceptionConstructor);
                        categoryConstructorsPerception.add(constructor);
                    }
                    catch (NoSuchMethodException exceptionInner)
                    {
                        throw new XmlParserException("Class " + clazz
                                + " does not have a valid perception category contructor, or is not a PerceptionCategory.");
                    }
                }
                catch (NullPointerException exception)
                {
                    throw new XmlParserException("Perception category defined with value Class but no class is specified.",
                            exception);
                }
            }
        }
        catch (NoSuchMethodException exception)
        {
            throw new XmlParserException("Perception category does not have a valid perception category contructor, "
                    + "or is not a PerceptionCategory.");
        }

        // Mental
        Mental mental;
        List<Class<?>> componentClasses = new ArrayList<>(); // to set default parameter values
        if (perception.getMental().getFuller() != null)
        {
            componentClasses.add(Fuller.class);
            org.opentrafficsim.xml.generated.PerceptionType.Mental.Fuller fuller = perception.getMental().getFuller();

            // Tasks
            Set<Task> tasks = new LinkedHashSet<>();
            for (ClassType taskClass : fuller.getTask())
            {
                Class<?> clazz = taskClass.get(eval);
                componentClasses.add(clazz);
                try
                {
                    tasks.add((Task) ClassUtil.resolveConstructor(clazz, new Class<?>[0]).newInstance());
                }
                catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException exception)
                {
                    throw new XmlParserException(
                            "Could not instantiate task of class " + clazz + " through an empty constructor.", exception);
                }
            }

            // Behavioural adaptations
            Set<BehavioralAdaptation> behavioralAdaptations = new LinkedHashSet<>();
            if (!fuller.getBehavioralAdaptations().getSituationalAwareness().isEmpty())
            {
                behavioralAdaptations.add(new AdaptationSituationalAwareness());
                componentClasses.add(AdaptationSituationalAwareness.class);
            }
            if (!fuller.getBehavioralAdaptations().getHeadway().isEmpty())
            {
                behavioralAdaptations.add(new AdaptationHeadway());
                componentClasses.add(AdaptationHeadway.class);
            }
            if (!fuller.getBehavioralAdaptations().getSpeed().isEmpty())
            {
                behavioralAdaptations.add(new AdaptationSpeed());
                componentClasses.add(AdaptationSpeed.class);
            }
            for (ClassType classType : fuller.getBehavioralAdaptations().getClazz())
            {
                Class<?> clazz = classType.get(eval);
                componentClasses.add(clazz);
                try
                {
                    behavioralAdaptations
                            .add((BehavioralAdaptation) ClassUtil.resolveConstructor(clazz, new Object[0]).newInstance());
                }
                catch (NullPointerException exception)
                {
                    throw new XmlParserException("Behavioral adaptation defined with value Class but no class is specified.",
                            exception);
                }
                catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException exception)
                {
                    throw new XmlParserException("Unable to instantiate class " + clazz + " through an empty constructor.",
                            exception);
                }
            }

            // Task manager
            TaskManager taskManager;
            if (fuller.getTaskManager() == null)
            {
                taskManager = null;
            }
            else
            {
                switch (fuller.getTaskManager().get(eval))
                {
                    case "SUMMATIVE":
                        taskManager = new SummativeTaskManager();
                        break;
                    case "ANTICIPATION_RELIANCE":
                        // TODO: support once more consolidated
                        throw new XmlParserException("Task manager AnticipationReliance is not yet supported.");
                    default:
                        throw new XmlParserException("Task manager " + fuller.getTaskManager() + " is unknown.");
                }
            }

            // Fuller
            mental = new Fuller(tasks, behavioralAdaptations, taskManager);
        }
        else
        {
            mental = null;
        }

        // Perception factory
        return new PerceptionFactory()
        {
            /** {@inheritDoc} */
            @Override
            public Parameters getParameters() throws ParameterException
            {
                ParameterSet parameters = new ParameterSet();
                for (Class<?> clazz : componentClasses)
                {
                    parameters.setDefaultParameters(clazz);
                }
                for (@SuppressWarnings("rawtypes")
                Constructor<? extends PerceptionCategory> constructor : categoryConstructorsPerception)
                {
                    parameters.setDefaultParameters(constructor.getDeclaringClass());
                }
                for (@SuppressWarnings("rawtypes")
                Constructor<? extends PerceptionCategory> constructor : categoryConstructorsPerceptionHeadway)
                {
                    parameters.setDefaultParameters(constructor.getDeclaringClass());
                }
                return parameters;
            }

            /** {@inheritDoc} */
            @Override
            public LanePerception generatePerception(final LaneBasedGtu gtu)
            {
                CategoricalLanePerception lanePerception = new CategoricalLanePerception(gtu, mental);
                try
                {
                    for (@SuppressWarnings("rawtypes")
                    Constructor<? extends PerceptionCategory> constructor : categoryConstructorsPerception)
                    {
                        lanePerception.addPerceptionCategory(constructor.newInstance(lanePerception));
                    }
                    for (@SuppressWarnings("rawtypes")
                    Constructor<? extends PerceptionCategory> constructor : categoryConstructorsPerceptionHeadway)
                    {
                        lanePerception.addPerceptionCategory(constructor.newInstance(lanePerception, headwayGtuType));
                    }
                }
                catch (InvocationTargetException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException exception)
                {
                    throw new RuntimeException("Exception while creating new instance of perception category.", exception);
                }
                return lanePerception;
            }
        };
    }
}
