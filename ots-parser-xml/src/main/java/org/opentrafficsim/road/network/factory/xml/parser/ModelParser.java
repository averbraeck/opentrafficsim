package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.base.AbstractDoubleScalarRel;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.core.parameters.InputParameters;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
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
import org.opentrafficsim.xml.generated.CarFollowingModelHeadwaySpeedType;
import org.opentrafficsim.xml.generated.CarFollowingModelType;
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
import org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs.AccelerationIncentives;
import org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs.MandatoryIncentives.Incentive;
import org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs.VoluntaryIncentives;
import org.opentrafficsim.xml.generated.PerceptionType;
import org.opentrafficsim.xml.generated.PerceptionType.Category;
import org.opentrafficsim.xml.generated.PerceptionType.HeadwayGtuType.Perceived;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Parser of the {@code MODEL} tags. Returns a map of strategical planner factories by model ID for use in demand parsing.
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
     * @param inputParameters InputParameters; input parameters
     * @param parameterTypes Map&lt;String, ParameterType&lt;?&gt;&gt;; parameter types
     * @param streamMap Map&lt;String, StreamInformation&gt;; stream information
     * @param <U> a unit
     * @param <T> a scalar type
     * @param <K> a parameter type value
     * @return Map&lt;String, ParameterFactory&lt;?&gt;&gt;; parameter factories by model ID
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    @SuppressWarnings("unchecked")
    public static <U extends Unit<U>, T extends AbstractDoubleScalarRel<U, T>, K> Map<String, ParameterFactory> parseParameters(
            final Definitions definitions, final List<ModelType> models, final InputParameters inputParameters,
            final Map<String, ParameterType<?>> parameterTypes, final StreamInformation streamMap) throws XmlParserException
    {
        Map<String, ParameterFactory> map = new LinkedHashMap<>();
        for (ModelType model : models)
        {
            // Parameter factory
            ParameterFactoryByType paramFactory = new ParameterFactoryByType();
            map.put(model.getId(), paramFactory);
            // set model parameters
            if (model.getModelParameters() != null)
            {
                for (Serializable parameter : model.getModelParameters().getStringOrAccelerationOrAccelerationDist())
                {
                    if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.String)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.String p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.String) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<String>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Acceleration)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Acceleration p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Acceleration) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterTypeNumeric<Acceleration>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof AccelerationDist)
                    {
                        AccelerationDist p = (AccelerationDist) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterTypeNumeric<Acceleration>) parameterTypes.get(p.getId()),
                                ParseDistribution.parseAccelerationDist(streamMap, p));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Boolean)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Boolean p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Boolean) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Boolean>) parameterTypes.get(p.getId()), p.isValue());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Class<?>>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Double)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Double p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Double) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Double>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof DoubleDist)
                    {
                        DoubleDist p = (DoubleDist) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Double>) parameterTypes.get(p.getId()),
                                ParseDistribution.makeDistContinuous(streamMap, p));
                    }
                    else if (parameter instanceof Fraction)
                    {
                        Fraction p = (Fraction) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Double>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Frequency)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Frequency p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Frequency) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterTypeNumeric<Frequency>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof FrequencyDist)
                    {
                        FrequencyDist p = (FrequencyDist) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterTypeNumeric<Frequency>) parameterTypes.get(p.getId()),
                                ParseDistribution.parseFrequencyDist(streamMap, p));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Integer)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Integer p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Integer) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Integer>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof IntegerDist)
                    {
                        IntegerDist p = (IntegerDist) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Integer>) parameterTypes.get(p.getId()),
                                ParseDistribution.makeDistDiscrete(streamMap, p));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Length)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Length p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Length) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Length>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof LengthDist)
                    {
                        LengthDist p = (LengthDist) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterTypeNumeric<Length>) parameterTypes.get(p.getId()),
                                ParseDistribution.parseLengthDist(streamMap, p));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.LinearDensity)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.LinearDensity p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.LinearDensity) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<LinearDensity>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof LinearDensityDist)
                    {
                        LinearDensityDist p = (LinearDensityDist) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterTypeNumeric<LinearDensity>) parameterTypes.get(p.getId()),
                                ParseDistribution.parseLinearDensityDist(streamMap, p));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.ModelType.ModelParameters.Speed)
                    {
                        org.opentrafficsim.xml.generated.ModelType.ModelParameters.Speed p =
                                (org.opentrafficsim.xml.generated.ModelType.ModelParameters.Speed) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterType<Speed>) parameterTypes.get(p.getId()), p.getValue());
                    }
                    else if (parameter instanceof SpeedDist)
                    {
                        SpeedDist p = (SpeedDist) parameter;
                        paramFactory.addParameter(getGtuType(p.getGtuType(), definitions),
                                (ParameterTypeNumeric<Speed>) parameterTypes.get(p.getId()),
                                ParseDistribution.parseSpeedDist(streamMap, p));
                    }
                }
            }
            // set input parameters, these may override the above parameters
            for (GtuType gtuType : inputParameters.getObjects(GtuType.class))
            {
                for (Entry<String, InputParameter<?, ?>> entry : inputParameters.getInputParameters(gtuType).entrySet())
                {
                    if (parameterTypes.containsKey(entry.getKey()))
                    {
                        ParameterType<?> parameterType = parameterTypes.get(entry.getKey());
                        InputParameter<?, ?> inputParameter = entry.getValue();
                        if (inputParameter.getValue() instanceof DistContinuous)
                        {
                            if (parameterType instanceof ParameterTypeDouble)
                            {
                                paramFactory.addParameter((ParameterTypeDouble) parameterType,
                                        (DistContinuous) inputParameter.getValue());
                            }
                            else
                            {
                                paramFactory.addParameter((ParameterTypeNumeric<T>) parameterType,
                                        (ContinuousDistDoubleScalar.Rel<T, U>) inputParameter.getValue());
                            }
                        }
                        else if (inputParameter.getValue() instanceof DistDiscrete)
                        {
                            paramFactory.addParameter(gtuType, (ParameterType<Integer>) parameterType,
                                    (DistDiscrete) inputParameter.getValue());
                        }
                        else
                        {
                            paramFactory.addParameter(gtuType, (ParameterType<K>) parameterType, (K) inputParameter.getValue());
                        }
                    }
                }
            }
        }
        return map;
    }

    /**
     * Creates strategical planner factories for models.
     * @param otsNetwork RoadNetwork; network
     * @param models List&lt;MODEL&gt;; models
     * @param inputParameters InputParameters; input parameters
     * @param parameterTypes Map&lt;String, ParameterType&lt;?&gt;&gt;; parameter types
     * @param streamInformation Map&lt;String, StreamInformation&gt;; stream information
     * @param parameterFactories Map&lt;String, ParameterFactory&gt;; parameter factories
     * @param <U> a unit
     * @param <T> a scalar type
     * @param <K> a parameter type value
     * @return Map&lt;String, LaneBasedStrategicalPlannerFactory&lt;?&gt;&gt;; strategical planner factories by model ID
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    public static <U extends Unit<U>, T extends AbstractDoubleScalarRel<U, T>,
            K> Map<String, LaneBasedStrategicalPlannerFactory<?>> parseModel(final RoadNetwork otsNetwork,
                    final List<ModelType> models, final InputParameters inputParameters,
                    final Map<String, ParameterType<?>> parameterTypes, final StreamInformation streamInformation,
                    final Map<String, ParameterFactory> parameterFactories) throws XmlParserException
    {
        Map<String, LaneBasedStrategicalPlannerFactory<?>> factories = new LinkedHashMap<>();
        for (ModelType model : models)
        {
            // Parameter factory
            ParameterFactory paramFactory = parameterFactories.get(model.getId());

            // Tactical planner
            LaneBasedTacticalPlannerFactory<?> tacticalPlannerFactory;
            if (model.getTacticalPlanner() != null)
            {
                if (model.getTacticalPlanner().getLmrs() != null)
                {
                    tacticalPlannerFactory = parseLmrs(model.getTacticalPlanner().getLmrs());
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
                // TODO: model.getSTRATEGICALPLANNER().getROUTE() defines route finding procedure: NONE|SHORTEST
                strategicalPlannerFactory = new LaneBasedStrategicalRoutePlannerFactory(tacticalPlannerFactory, paramFactory);
            }
            else
            {
                throw new XmlParserException("Strategical planner has unsupported value.");
            }

            factories.put(model.getId(), strategicalPlannerFactory);
        }
        return factories;
    }

    @SuppressWarnings("unchecked")
    private static <T> ParameterType<T> getParameterType(final String field, final Class<T> type)
    {
        int dot = field.lastIndexOf(".");
        String className = field.substring(0, dot);
        String fieldName = field.substring(dot + 1);
        try
        {
            return (ParameterType<T>) ClassUtil.resolveField(Class.forName(className), fieldName).get(null);
        }
        catch (NoSuchFieldException | ClassCastException | IllegalArgumentException | IllegalAccessException
                | ClassNotFoundException exception)
        {
            throw new RuntimeException(
                    "Unable to find parameter " + field + ", it may not be accessible or it is not a parameter of type " + type,
                    exception);
        }
    }

    /**
     * @param field a field
     * @param type a class
     * @param <T> a number type
     * @return the ParameterTypeNumeric belonging to the field
     */
    @SuppressWarnings("unchecked")
    private static <T extends Number> ParameterTypeNumeric<T> getParameterTypeNumeric(final String field, final Class<T> type)
    {
        int dot = field.lastIndexOf(".");
        String className = field.substring(0, dot);
        String fieldName = field.substring(dot + 1);
        try
        {
            return (ParameterTypeNumeric<T>) ClassUtil.resolveField(Class.forName(className), fieldName).get(null);
        }
        catch (NoSuchFieldException | ClassCastException | IllegalArgumentException | IllegalAccessException
                | ClassNotFoundException exception)
        {
            throw new RuntimeException(
                    "Unable to find parameter " + field + ", it may not be accessible or it is not a parameter of type " + type,
                    exception);
        }
    }

    /**
     * @param gtuTypeId the gtu type
     * @param definitions Definitions; parsed definitions
     * @return the GtuType belonging to the id
     */
    private static GtuType getGtuType(final String gtuTypeId, final Definitions definitions)
    {
        if (gtuTypeId == null)
        {
            return null;
        }
        return definitions.get(GtuType.class, gtuTypeId);
    }

    /**
     * Parse Lmrs model.
     * @param lmrs org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs; Lmrs information
     * @return LaneBasedTacticalPlannerFactory&lt;Lmrs&gt;; Lmrs factory
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    @SuppressWarnings("unchecked")
    private static LaneBasedTacticalPlannerFactory<Lmrs> parseLmrs(
            final org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs lmrs) throws XmlParserException
    {
        // Synchronization
        Synchronization synchronization;
        switch (lmrs.getSynchronization() != null ? lmrs.getSynchronization() : "PASSIVE")
        {
            case "DEADEND":
                synchronization = Synchronization.DEADEND;
                break;
            case "PASSIVE":
                synchronization = Synchronization.PASSIVE;
                break;
            case "PASSIVEMOVING":
                synchronization = Synchronization.PASSIVE_MOVING;
                break;
            case "ALIGNGAP":
                synchronization = Synchronization.ALIGN_GAP;
                break;
            case "ACTIVE":
                synchronization = Synchronization.ACTIVE;
                break;
            default:
                throw new XmlParserException("Synchronization " + lmrs.getSynchronization() + " is unknown.");
        }

        // Cooperation
        Cooperation cooperation;
        switch (lmrs.getCooperation() != null ? lmrs.getCooperation() : "PASSIVE")
        {
            case "PASSIVE":
                cooperation = Cooperation.PASSIVE;
                break;
            case "PASSIVEMOVING":
                cooperation = Cooperation.PASSIVE_MOVING;
                break;
            case "ACTIVE":
                cooperation = Cooperation.ACTIVE;
                break;
            default:
                throw new XmlParserException("Cooperation " + lmrs.getCooperation() + " is unknown.");
        }

        // Gap-acceptance
        GapAcceptance gapAcceptance;
        switch (lmrs.getGapAcceptance() != null ? lmrs.getGapAcceptance() : "INFORMED")
        {
            case "INFORMED":
                gapAcceptance = GapAcceptance.INFORMED;
                break;
            case "EGOHEADWAY":
                gapAcceptance = GapAcceptance.EGO_HEADWAY;
                break;
            default:
                throw new XmlParserException("GapAcceptance " + lmrs.getGapAcceptance() + " is unknown.");
        }

        // Tailgating
        Tailgating tailgating;
        switch (lmrs.getTailgating() != null ? lmrs.getTailgating() : "NONE")
        {
            case "NONE":
                tailgating = Tailgating.NONE;
                break;
            case "RHOONLY":
                tailgating = Tailgating.RHO_ONLY;
                break;
            case "PRESSURE":
                tailgating = Tailgating.PRESSURE;
                break;
            default:
                throw new XmlParserException("Tailgating " + lmrs.getTailgating() + " is unknown.");
        }

        // Mandatory incentives
        Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
        for (Incentive incentive : lmrs.getMandatoryIncentives().getIncentive())
        {
            switch (incentive.getValue())
            {
                case "ROUTE":
                    mandatoryIncentives.add(new IncentiveRoute());
                    break;
                case "GETINLANE":
                    mandatoryIncentives.add(new IncentiveGetInLane());
                    break;
                case "BUSSTOP":
                    mandatoryIncentives.add(new IncentiveBusStop());
                    break;
                case "org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class":
                    try
                    {
                        mandatoryIncentives.add((MandatoryIncentive) ClassUtil
                                .resolveConstructor(incentive.getClass(), new Class<?>[0]).newInstance());
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | ClassCastException exception)
                    {
                        throw new XmlParserException("Class " + incentive.getClass()
                                + " does not have a valid empty contructor, or is not a MandatoryIncentive.");
                    }
                    break;
                default:
                    throw new XmlParserException("MandatoryIncentive " + incentive.getValue() + " is unknown.");
            }
        }
        if (mandatoryIncentives.isEmpty())
        {
            mandatoryIncentives.add(new IncentiveDummy());
        }

        // Voluntary incentives
        Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
        for (VoluntaryIncentives.Incentive incentive : lmrs.getVoluntaryIncentives().getIncentive())
        {
            switch (incentive.getValue())
            {
                case "KEEP":
                    voluntaryIncentives.add(new IncentiveKeep());
                    break;
                case "org.opentrafficsim.xml.generated.ModelType.ModelParameters.SpeedWITHCOURTESY":
                    voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
                    break;
                case "COURTESY":
                    voluntaryIncentives.add(new IncentiveCourtesy());
                    break;
                case "SOCIOorg.opentrafficsim.xml.generated.ModelType.ModelParameters.Speed":
                    voluntaryIncentives.add(new IncentiveSocioSpeed());
                    break;
                case "STAYRIGHT":
                    voluntaryIncentives.add(new IncentiveStayRight());
                    break;
                case "org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class":
                    try
                    {
                        voluntaryIncentives.add((VoluntaryIncentive) ClassUtil
                                .resolveConstructor(incentive.getClass(), new Class<?>[0]).newInstance());
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | ClassCastException exception)
                    {
                        throw new XmlParserException("Class " + incentive.getClass()
                                + " does not have a valid empty contructor, or is not a VoluntaryIncentive.");
                    }
                    break;
                default:
                    throw new XmlParserException("VoluntaryIncentive " + incentive.getValue() + " is unknown.");
            }
        }

        // Acceleration incentives
        Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
        for (AccelerationIncentives.Incentive incentive : lmrs.getAccelerationIncentives().getIncentive())
        {
            switch (incentive.getValue())
            {
                case "BUSSTOP":
                    accelerationIncentives.add(new AccelerationBusStop());
                    break;
                case "CONFLICTS":
                    accelerationIncentives.add(new AccelerationConflicts());
                    break;
                case "org.opentrafficsim.xml.generated.ModelType.ModelParameters.SpeedLIMITTRANSITION":
                    accelerationIncentives.add(new AccelerationSpeedLimitTransition());
                    break;
                case "TRAFFICLIGHTS":
                    accelerationIncentives.add(new AccelerationTrafficLights());
                    break;
                case "NORIGHTOVERTAKE":
                    accelerationIncentives.add(new AccelerationNoRightOvertake());
                    break;
                case "org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class":
                    try
                    {
                        accelerationIncentives.add((AccelerationIncentive) ClassUtil
                                .resolveConstructor(incentive.getClass(), new Class<?>[0]).newInstance());
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | ClassCastException exception)
                    {
                        throw new XmlParserException("Class " + incentive.getClass()
                                + " does not have a valid empty contructor, or is not a AccelerationIncentive.");
                    }
                    break;
                default:
                    throw new XmlParserException("AccelerationIncentive " + incentive.getValue() + " is unknown.");
            }
        }

        // Perception
        PerceptionFactory perceptionFactory = parsePerception(lmrs.getPerception());
        // in helper method

        // Car-following model
        CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory =
                parseCarFollowingModel(lmrs.getCarFollowingModel());

        // Lmrs factory
        return new LmrsFactory(carFollowingModelFactory, perceptionFactory, synchronization, cooperation, gapAcceptance,
                tailgating, mandatoryIncentives, voluntaryIncentives, accelerationIncentives);
    }

    /**
     * Parse car-following model.
     * @param carFollowingModel CarFollowingModelType; car-following model information
     * @return CarFollowingModelFactory&lt;? extends CarFollowingModel&gt;; car-following model factory
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    private static CarFollowingModelFactory<? extends CarFollowingModel> parseCarFollowingModel(
            final CarFollowingModelType carFollowingModel) throws XmlParserException
    {
        CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory;
        if (carFollowingModel.getIdm() != null)
        {
            carFollowingModelFactory =
                    parseCarFollowingModelHeadwaySpeed(carFollowingModel.getIdm(), (headway, speed) -> new Idm(headway, speed));
        }
        else if (carFollowingModel.getIdmPlus() != null)
        {
            carFollowingModelFactory = parseCarFollowingModelHeadwaySpeed(carFollowingModel.getIdmPlus(),
                    (headway, speed) -> new IdmPlus(headway, speed));
        }
        else
        {
            throw new XmlParserException("Car-following model has unsupported value.");
        }
        return carFollowingModelFactory;
    }

    /**
     * Parse a car-following model that accepts a desired headway and desired speed model in a constructor. These are specified
     * in xsd as a {@code CarFollowingModelHeadwaySpeedType}. The argument {@code function} can be specified as a lambda
     * function:
     * 
     * <pre>
     * (headway, speed) -&gt; new MyCarFollowingModel(headway, speed)
     * </pre>
     * 
     * @param carFollowingModelHeadwaySpeed CarFollowingModelHeadwaySpeedType; information about desired headway and speed model
     * @param function BiFunction&lt;DesiredHeadwayModel, DesiredSpeedModel, T>&gt;; function to instantiate the model
     * @param <T> car-following model type
     * @return CarFollowingModelFactory&lt;T&gt; car-following model factory
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    private static <T extends CarFollowingModel> CarFollowingModelFactory<T> parseCarFollowingModelHeadwaySpeed(
            final CarFollowingModelHeadwaySpeedType carFollowingModelHeadwaySpeed,
            final BiFunction<DesiredHeadwayModel, DesiredSpeedModel, T> function) throws XmlParserException
    {
        Generator<DesiredHeadwayModel> generatorDesiredHeadwayModel =
                parseDesiredHeadwayModel(carFollowingModelHeadwaySpeed.getDesiredHeadwayModel());
        Generator<DesiredSpeedModel> generatorDesiredSpeedModel =
                parseDesiredSpeedModel(carFollowingModelHeadwaySpeed.getDesiredSpeedModel());
        return new CarFollowingModelFactory<T>()
        {
            /** {@inheritDoc} */
            @Override
            public Parameters getParameters() throws ParameterException
            {
                // in this generic setting, we cannot predefine parameters, they have to be specified in xml
                return new ParameterSet();
            }

            /** {@inheritDoc} */
            @Override
            public T generateCarFollowingModel()
            {
                try
                {
                    return function.apply(generatorDesiredHeadwayModel.draw(), generatorDesiredSpeedModel.draw());
                }
                catch (ProbabilityException | ParameterException exception)
                {
                    throw new RuntimeException("Exception while drawing desired headway or speed model.");
                }
            }
        };
    }

    /**
     * Parse desired headway model.
     * @param desiredHeadwayModel org.opentrafficsim.xml.generated.CarFollowingModelHeadwaySpeedType.DesiredHeadwayModel;
     *            desired headway model information
     * @return Generator&lt;DesiredHeadwayModel&gt;; generator for desired headway model
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    @SuppressWarnings("unchecked")
    private static Generator<DesiredHeadwayModel> parseDesiredHeadwayModel(
            final org.opentrafficsim.xml.generated.CarFollowingModelHeadwaySpeedType.DesiredHeadwayModel desiredHeadwayModel)
            throws XmlParserException
    {
        if (desiredHeadwayModel.getIdm() != null)
        {
            return new Generator<DesiredHeadwayModel>()
            {
                /** {@inheritDoc} */
                @Override
                public DesiredHeadwayModel draw() throws ProbabilityException, ParameterException
                {
                    return AbstractIdm.HEADWAY;
                }
            };
        }
        else if (desiredHeadwayModel.getClass() != null)
        {
            Constructor<? extends DesiredHeadwayModel> constructor;
            try
            {
                constructor = ClassUtil.resolveConstructor(desiredHeadwayModel.getClazz(), new Object[0]);
            }
            catch (NoSuchMethodException exception)
            {
                throw new XmlParserException(
                        "Class " + desiredHeadwayModel.getClass() + " does not have a valid empty constructor.", exception);
            }
            return new Generator<DesiredHeadwayModel>()
            {

                /** {@inheritDoc} */
                @Override
                public DesiredHeadwayModel draw() throws ProbabilityException, ParameterException
                {
                    try
                    {
                        return constructor.newInstance();
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException exception)
                    {
                        throw new RuntimeException("Exception while instantiating a desired headway model of class "
                                + desiredHeadwayModel.getClass(), exception);
                    }
                }

            };
        }
        else
        {
            throw new XmlParserException("Desired headway model has unsupported value.");
        }
    }

    /**
     * Parse desired speed model.
     * @param desiredSpeedModel DesiredSpeedModelType; desired speed model information
     * @return Generator&lt;DesiredSpeedModel&gt;; generator for desired speed model
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    @SuppressWarnings("unchecked")
    private static Generator<DesiredSpeedModel> parseDesiredSpeedModel(final DesiredSpeedModelType desiredSpeedModel)
            throws XmlParserException
    {
        if (desiredSpeedModel.getIdm() != null)
        {
            return new Generator<DesiredSpeedModel>()
            {
                /** {@inheritDoc} */
                @Override
                public DesiredSpeedModel draw() throws ProbabilityException, ParameterException
                {
                    return AbstractIdm.DESIRED_SPEED;
                }
            };
        }
        else if (desiredSpeedModel.getSocio() != null)
        {
            // SOCIO is itself a DesiredSpeedModelType, as it wraps another desired speed model
            Generator<DesiredSpeedModel> wrappedGenerator = parseDesiredSpeedModel(desiredSpeedModel.getSocio());
            return new Generator<DesiredSpeedModel>()
            {

                /** {@inheritDoc} */
                @Override
                public DesiredSpeedModel draw() throws ProbabilityException, ParameterException
                {
                    return new SocioDesiredSpeed(wrappedGenerator.draw());
                }
            };
        }
        else if (desiredSpeedModel.getClass() != null)
        {
            Constructor<? extends DesiredSpeedModel> constructor;
            try
            {
                constructor = ClassUtil.resolveConstructor(desiredSpeedModel.getClazz(), new Object[0]);
            }
            catch (NoSuchMethodException exception)
            {
                throw new XmlParserException(
                        "Class " + desiredSpeedModel.getClass() + " does not have a valid empty constructor.", exception);
            }
            return new Generator<DesiredSpeedModel>()
            {
                /** {@inheritDoc} */
                @Override
                public DesiredSpeedModel draw() throws ProbabilityException, ParameterException
                {
                    try
                    {
                        return constructor.newInstance();
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException exception)
                    {
                        throw new RuntimeException(
                                "Exception while instantiating a desired speed model of class " + desiredSpeedModel.getClass(),
                                exception);
                    }
                }
            };
        }
        else
        {
            throw new XmlParserException("Desired speed model has unsupported value.");
        }
    }

    /**
     * Parse perception for any tactical planner that has PerceptionType to support perception.
     * @param perception PerceptionType; perception xml information
     * @return PerceptionFactory; parsed perception factory
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    @SuppressWarnings("unchecked")
    private static PerceptionFactory parsePerception(final PerceptionType perception) throws XmlParserException
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
            // Estimation
            Estimation estimation;
            switch (perceived.getEstimation())
            {
                case "NONE":
                    estimation = Estimation.NONE;
                    break;
                case "UNDERESTIMATION":
                    estimation = Estimation.UNDERESTIMATION;
                    break;
                case "OVERESTIMATION":
                    estimation = Estimation.OVERESTIMATION;
                    break;
                default:
                    throw new XmlParserException("Estimation " + perceived.getEstimation() + " is unknown.");
            }
            // Anticipation
            Anticipation anticipation;
            switch (perceived.getAnticipation())
            {
                case "NONE":
                    anticipation = Anticipation.NONE;
                    break;
                case "CONSTANTorg.opentrafficsim.xml.generated.ModelType.ModelParameters.Speed":
                    anticipation = Anticipation.CONSTANT_SPEED;
                    break;
                case "CONSTANTACCELERATON":
                    anticipation = Anticipation.CONSTANT_ACCELERATION;
                    break;
                default:
                    throw new XmlParserException("Anticipation " + perceived.getAnticipation() + " is unknown.");
            }
            headwayGtuType = new PerceivedHeadwayGtuType(estimation, anticipation);
        }
        else
        {
            throw new XmlParserException("HeadwayGtuType is unknown.");
        }

        // Categories
        List<Constructor<? extends PerceptionCategory>> categoryConstructorsPerception = new ArrayList<>();
        List<Constructor<? extends PerceptionCategory>> categoryConstructorsPerceptionHeadway = new ArrayList<>();
        Class<?>[] perceptionConstructor = new Class[] {LanePerception.class};
        Class<?>[] perceptionHeadwayConstructor = new Class[] {LanePerception.class, HeadwayGtuType.class};
        for (Category category : perception.getCategory())
        {
            try
            {
                switch (category.getValue())
                {
                    case "EGO":
                        Constructor<DirectEgoPerception> c =
                                ClassUtil.resolveConstructor(DirectEgoPerception.class, new Class[] {Perception.class});
                        categoryConstructorsPerception.add(c);
                        break;
                    case "BUSSTOP":
                        categoryConstructorsPerception
                                .add(ClassUtil.resolveConstructor(DirectBusStopPerception.class, perceptionConstructor));
                        break;
                    case "INFRASTRUCTURE":
                        categoryConstructorsPerception
                                .add(ClassUtil.resolveConstructor(DirectInfrastructurePerception.class, perceptionConstructor));
                        break;
                    case "INTERSECTION":
                        categoryConstructorsPerceptionHeadway.add(
                                ClassUtil.resolveConstructor(DirectIntersectionPerception.class, perceptionHeadwayConstructor));
                        break;
                    case "NEIGHBORS":
                        categoryConstructorsPerceptionHeadway.add(
                                ClassUtil.resolveConstructor(DirectNeighborsPerception.class, perceptionHeadwayConstructor));
                        break;
                    case "TRAFFIC":
                        categoryConstructorsPerception
                                .add(ClassUtil.resolveConstructor(AnticipationTrafficPerception.class, perceptionConstructor));
                        break;
                    case "CLASS":
                        Constructor<? extends PerceptionCategory<?, ?>> constructor;
                        try
                        {
                            constructor = ClassUtil.resolveConstructor(category.getClazz(), perceptionHeadwayConstructor);
                            categoryConstructorsPerceptionHeadway.add(constructor);
                        }
                        catch (NoSuchMethodException exception)
                        {
                            constructor = ClassUtil.resolveConstructor(category.getClazz(), perceptionConstructor);
                            categoryConstructorsPerception.add(constructor);
                        }
                        catch (NullPointerException exception)
                        {
                            throw new XmlParserException(
                                    "Perception category defined with value Class but no class is specified.", exception);
                        }
                        break;
                    default:
                        throw new XmlParserException("Perception category " + category.getValue() + " is unknown.");
                }
            }
            catch (NoSuchMethodException | ClassCastException exception)
            {
                throw new XmlParserException(
                        "Unexpected problem while resolving constructor for perception category " + category.getValue());
            }
        }

        // Mental
        Mental mental;
        if (perception.getMental().getFuller() != null)
        {
            org.opentrafficsim.xml.generated.PerceptionType.Mental.Fuller fuller = perception.getMental().getFuller();

            // Tasks
            Set<Task> tasks = new LinkedHashSet<>();
            for (Class<?> taskClass : fuller.getTask())
            {
                try
                {
                    tasks.add((Task) ClassUtil.resolveConstructor(taskClass, new Class<?>[0]).newInstance());
                }
                catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException exception)
                {
                    throw new XmlParserException(
                            "Could not instantiate task of class " + taskClass + " through an empty constructor.", exception);
                }
            }

            // Behavioural adaptations
            Set<BehavioralAdaptation> behavioralAdapatations = new LinkedHashSet<>();
            for (org.opentrafficsim.xml.generated.PerceptionType.Mental.Fuller.BehavioralAdaptation behavioralAdaptation : fuller
                    .getBehavioralAdaptation())
            {
                switch (behavioralAdaptation.getValue())
                {
                    case "SITUATIONALAWARENESS":
                        behavioralAdapatations.add(new AdaptationSituationalAwareness());
                        break;
                    case "HEADWAY":
                        behavioralAdapatations.add(new AdaptationHeadway());
                        break;
                    case "SPEED":
                        behavioralAdapatations.add(new AdaptationSpeed());
                        break;
                    case "CLASS":
                        try
                        {
                            behavioralAdapatations.add((BehavioralAdaptation) ClassUtil
                                    .resolveConstructor(behavioralAdaptation.getClass(), new Object[0]).newInstance());
                        }
                        catch (NullPointerException exception)
                        {
                            throw new XmlParserException(
                                    "Behavioral adpatation defined with value org.opentrafficsim.xml.generated.ModelType.ModelParameters.Class but no class is specified.",
                                    exception);
                        }
                        catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException exception)
                        {
                            throw new XmlParserException("Unable to instantiate class " + behavioralAdaptation.getClass()
                                    + " through an empty constructor.", exception);
                        }
                        break;
                    default:
                        throw new XmlParserException(
                                "Behavioral adapatation " + behavioralAdaptation.getValue() + " is unknown.");
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
                switch (fuller.getTaskManager())
                {
                    case "SUMMATIVE":
                        taskManager = new SummativeTaskManager();
                        break;
                    case "ANTICIPATIONRELIANCE":
                        // TODO: support once more consolidated
                        throw new XmlParserException("Task manager ANTICIPATIONRELIANCE is not yet supported.");
                    default:
                        throw new XmlParserException("Task manager " + fuller.getTaskManager() + " is unknown.");
                }
            }

            // Fuller
            mental = new Fuller(tasks, behavioralAdapatations, taskManager);
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
                // in this generic setting, we cannot predefine parameters, they have to be specified in xml
                return new ParameterSet();
            }

            /** {@inheritDoc} */
            @Override
            public LanePerception generatePerception(final LaneBasedGtu gtu)
            {
                CategoricalLanePerception lanePerception = new CategoricalLanePerception(gtu, mental);
                try
                {
                    for (Constructor<? extends PerceptionCategory> constructor : categoryConstructorsPerception)
                    {
                        lanePerception.addPerceptionCategory(constructor.newInstance(lanePerception));
                    }
                    for (Constructor<? extends PerceptionCategory> constructor : categoryConstructorsPerceptionHeadway)
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
