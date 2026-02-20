package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;
import org.djutils.eval.Eval;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.core.parameters.ParameterFactoryByType.Correlation;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.gtu.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.FullerImplementation;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.Setting;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.ParseDistribution;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.ExpressionType;
import org.opentrafficsim.xml.generated.Fuller;
import org.opentrafficsim.xml.generated.FullerAnticipationReliance;
import org.opentrafficsim.xml.generated.FullerAttentionMatrix;
import org.opentrafficsim.xml.generated.FullerBehavioralAdaptations;
import org.opentrafficsim.xml.generated.FullerSummative;
import org.opentrafficsim.xml.generated.FullerTasksSummativeAndAr;
import org.opentrafficsim.xml.generated.GtuTypeLmrsModel;
import org.opentrafficsim.xml.generated.LmrsModel;
import org.opentrafficsim.xml.generated.LmrsModel.AccelerationIncentives;
import org.opentrafficsim.xml.generated.LmrsModel.MandatoryIncentives;
import org.opentrafficsim.xml.generated.LmrsModel.SocialInteractions;
import org.opentrafficsim.xml.generated.LmrsModel.VoluntaryIncentives;
import org.opentrafficsim.xml.generated.ModelParameters;
import org.opentrafficsim.xml.generated.ModelParameters.AccelerationDist;
import org.opentrafficsim.xml.generated.ModelParameters.DoubleDist;
import org.opentrafficsim.xml.generated.ModelParameters.Fraction;
import org.opentrafficsim.xml.generated.ModelParameters.FrequencyDist;
import org.opentrafficsim.xml.generated.ModelParameters.IntegerDist;
import org.opentrafficsim.xml.generated.ModelParameters.LengthDist;
import org.opentrafficsim.xml.generated.ModelParameters.LinearDensityDist;
import org.opentrafficsim.xml.generated.ModelParameters.SpeedDist;
import org.opentrafficsim.xml.generated.ModelType;
import org.opentrafficsim.xml.generated.ModelType.GtuTypeParameters;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;

/**
 * Parser of the {@code Model} tags. Returns a map of strategical planner factories by model ID for use in demand parsing.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ModelParser
{
    /** */
    private ModelParser()
    {
        // static class
    }

    /**
     * Parse parameter factories.
     * @param definitions parsed definitions.
     * @param models models
     * @param eval expression evaluator
     * @param parameterTypes parameter types
     * @param streamMap stream information
     * @param <U> a unit
     * @param <T> a scalar type
     * @param <K> a parameter type value
     * @return parameter factories by model ID
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    public static <U extends Unit<U>, T extends DoubleScalarRel<U, T>, K> ParameterFactory parseParameters(
            final Definitions definitions, final List<ModelType> models, final Eval eval,
            final Map<String, ParameterType<?>> parameterTypes, final StreamInformation streamMap) throws XmlParserException
    {
        ParameterFactoryByType parameterFactory = new ParameterFactoryByType();
        for (ModelType model : models)
        {
            // set model parameters
            if (model.getDefaultParameters() != null)
            {
                parseParameters(model.getDefaultParameters(), parameterFactory, null, parameterTypes, streamMap, eval);
            }
            for (GtuTypeParameters modelParameters : model.getGtuTypeParameters())
            {
                GtuType gtuType = definitions.getOrThrow(GtuType.class, modelParameters.getGtuType().get(eval));
                parseParameters(modelParameters, parameterFactory, gtuType, parameterTypes, streamMap, eval);
            }
        }
        return parameterFactory;
    }

    /**
     * Parse parameters, default or for GTU type.
     * @param modelParameters parameters tag
     * @param parameterFactory parameter factory
     * @param gtuType GTU type, may be {@code null} for default parameters
     * @param parameterTypes parameter types
     * @param streamMap random stream
     * @param eval expression evaluator
     * @throws XmlParserException when distribution could not be parsed
     */
    @SuppressWarnings("unchecked")
    private static void parseParameters(final ModelParameters modelParameters, final ParameterFactoryByType parameterFactory,
            final GtuType gtuType, final Map<String, ParameterType<?>> parameterTypes, final StreamInformation streamMap,
            final Eval eval) throws XmlParserException
    {
        for (Serializable parameter : modelParameters.getDurationOrDurationDistOrLength())
        {
            if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.String)
            {
                org.opentrafficsim.xml.generated.ModelParameters.String p =
                        (org.opentrafficsim.xml.generated.ModelParameters.String) parameter;
                parameterFactory.addParameter(gtuType, (ParameterType<String>) parameterTypes.get(p.getId().get(eval)),
                        p.getValue().get(eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Acceleration)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Acceleration p =
                        (org.opentrafficsim.xml.generated.ModelParameters.Acceleration) parameter;
                parameterFactory.addParameter(gtuType,
                        (ParameterTypeNumeric<Acceleration>) parameterTypes.get(p.getId().get(eval)), p.getValue().get(eval));
            }
            else if (parameter instanceof AccelerationDist)
            {
                AccelerationDist p = (AccelerationDist) parameter;
                parameterFactory.addParameter(gtuType,
                        (ParameterTypeNumeric<Acceleration>) parameterTypes.get(p.getId().get(eval)),
                        ParseDistribution.parseContinuousDist(streamMap, p, p.getAccelerationUnit().get(eval), eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Boolean)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Boolean p =
                        (org.opentrafficsim.xml.generated.ModelParameters.Boolean) parameter;
                parameterFactory.addParameter(gtuType, (ParameterType<Boolean>) parameterTypes.get(p.getId().get(eval)),
                        p.getValue().get(eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Class)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Class p =
                        (org.opentrafficsim.xml.generated.ModelParameters.Class) parameter;
                parameterFactory.addParameter(gtuType, (ParameterType<Class<?>>) parameterTypes.get(p.getId().get(eval)),
                        p.getValue().get(eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Double)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Double p =
                        (org.opentrafficsim.xml.generated.ModelParameters.Double) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Double>) parameterTypes.get(p.getId().get(eval)),
                        p.getValue().get(eval));
            }
            else if (parameter instanceof DoubleDist)
            {
                DoubleDist p = (DoubleDist) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Double>) parameterTypes.get(p.getId().get(eval)),
                        ParseDistribution.makeDistContinuous(streamMap, p, eval));
            }
            else if (parameter instanceof Fraction)
            {
                Fraction p = (Fraction) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Double>) parameterTypes.get(p.getId().get(eval)),
                        p.getValue().get(eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Frequency)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Frequency p =
                        (org.opentrafficsim.xml.generated.ModelParameters.Frequency) parameter;
                parameterFactory.addParameter(gtuType,
                        (ParameterTypeNumeric<Frequency>) parameterTypes.get(p.getId().get(eval)), p.getValue().get(eval));
            }
            else if (parameter instanceof FrequencyDist)
            {
                FrequencyDist p = (FrequencyDist) parameter;
                parameterFactory.addParameter(gtuType,
                        (ParameterTypeNumeric<Frequency>) parameterTypes.get(p.getId().get(eval)),
                        ParseDistribution.parseContinuousDist(streamMap, p, p.getFrequencyUnit().get(eval), eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Integer)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Integer p =
                        (org.opentrafficsim.xml.generated.ModelParameters.Integer) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Integer>) parameterTypes.get(p.getId().get(eval)),
                        p.getValue().get(eval).intValue());
            }
            else if (parameter instanceof IntegerDist)
            {
                IntegerDist p = (IntegerDist) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Integer>) parameterTypes.get(p.getId().get(eval)),
                        ParseDistribution.makeDistDiscrete(streamMap, p, eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Length)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Length p =
                        (org.opentrafficsim.xml.generated.ModelParameters.Length) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Length>) parameterTypes.get(p.getId().get(eval)),
                        p.getValue().get(eval));
            }
            else if (parameter instanceof LengthDist)
            {
                LengthDist p = (LengthDist) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Length>) parameterTypes.get(p.getId().get(eval)),
                        ParseDistribution.parseContinuousDist(streamMap, p, p.getLengthUnit().get(eval), eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.LinearDensity)
            {
                org.opentrafficsim.xml.generated.ModelParameters.LinearDensity p =
                        (org.opentrafficsim.xml.generated.ModelParameters.LinearDensity) parameter;
                parameterFactory.addParameter(gtuType,
                        (ParameterTypeNumeric<LinearDensity>) parameterTypes.get(p.getId().get(eval)), p.getValue().get(eval));
            }
            else if (parameter instanceof LinearDensityDist)
            {
                LinearDensityDist p = (LinearDensityDist) parameter;
                parameterFactory.addParameter(gtuType,
                        (ParameterTypeNumeric<LinearDensity>) parameterTypes.get(p.getId().get(eval)),
                        ParseDistribution.parseContinuousDist(streamMap, p, p.getLinearDensityUnit().get(eval), eval));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Speed)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Speed p =
                        (org.opentrafficsim.xml.generated.ModelParameters.Speed) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Speed>) parameterTypes.get(p.getId().get(eval)),
                        p.getValue().get(eval));
            }
            else if (parameter instanceof SpeedDist)
            {
                SpeedDist p = (SpeedDist) parameter;
                parameterFactory.addParameter(gtuType, (ParameterTypeNumeric<Speed>) parameterTypes.get(p.getId().get(eval)),
                        ParseDistribution.parseContinuousDist(streamMap, p, p.getSpeedUnit().get(eval), eval));
            }
        }
        // correlations
        for (Serializable parameter : modelParameters.getDurationOrDurationDistOrLength())
        {
            if (parameter instanceof org.opentrafficsim.xml.generated.ModelParameters.Correlation)
            {
                org.opentrafficsim.xml.generated.ModelParameters.Correlation c =
                        (org.opentrafficsim.xml.generated.ModelParameters.Correlation) parameter;
                parseCorrelation(gtuType, c, parameterTypes, parameterFactory, eval);
            }
        }
    }

    /**
     * Parses a correlation.
     * @param <F> value type of first parameter.
     * @param <T> value type of then parameter.
     * @param gtuType GTU type for which the correlation applies.
     * @param correlationTag correlation tag.
     * @param parameterTypes parameter types.
     * @param parameterFactory parameter factory.
     * @param eval evaluator (to evaluate First and Then node values).
     */
    @SuppressWarnings("unchecked")
    private static <F, T> void parseCorrelation(final GtuType gtuType,
            final org.opentrafficsim.xml.generated.ModelParameters.Correlation correlationTag,
            final Map<String, ParameterType<?>> parameterTypes, final ParameterFactoryByType parameterFactory, final Eval eval)
    {
        ParameterType<F> firstType = null;
        if (correlationTag.getFirst() != null)
        {
            if (correlationTag.getFirst().getAcceleration() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getAcceleration().get(eval));
            }
            else if (correlationTag.getFirst().getDouble() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getDouble().get(eval));
            }
            else if (correlationTag.getFirst().getDuration() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getDuration().get(eval));
            }
            else if (correlationTag.getFirst().getFraction() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getFraction().get(eval));
            }
            else if (correlationTag.getFirst().getFrequency() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getFrequency().get(eval));
            }
            else if (correlationTag.getFirst().getInteger() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getInteger().get(eval));
            }
            else if (correlationTag.getFirst().getLength() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getLength().get(eval));
            }
            else if (correlationTag.getFirst().getLinearDensity() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getLinearDensity().get(eval));
            }
            else if (correlationTag.getFirst().getSpeed() != null)
            {
                firstType = (ParameterType<F>) parameterTypes.get(correlationTag.getFirst().getSpeed().get(eval));
            }
            else
            {
                throw new OtsRuntimeException("First in Correlation is not valid.");
            }
        }
        ParameterType<T> thenType = null;
        Correlation<F, T> correlation = null;
        String expression = correlationTag.getExpression();
        if (correlationTag.getThen().getAcceleration() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getAcceleration().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, Acceleration.class);
        }
        else if (correlationTag.getThen().getDouble() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getDouble().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, Double.class);
        }
        else if (correlationTag.getThen().getDuration() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getDuration().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, Duration.class);
        }
        else if (correlationTag.getThen().getFraction() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getFraction().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, Double.class);
        }
        else if (correlationTag.getThen().getFrequency() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getFrequency().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, Frequency.class);
        }
        else if (correlationTag.getThen().getInteger() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getInteger().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, Integer.class);
        }
        else if (correlationTag.getThen().getLength() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getLength().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, Length.class);
        }
        else if (correlationTag.getThen().getLinearDensity() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getLinearDensity().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, LinearDensity.class);
        }
        else if (correlationTag.getThen().getSpeed() != null)
        {
            thenType = (ParameterType<T>) parameterTypes.get(correlationTag.getThen().getSpeed().get(eval));
            correlation = (Correlation<F, T>) (f, t) -> correlateFromExpression(f, t, expression, Speed.class);
        }
        else
        {
            throw new OtsRuntimeException("Then in Correlation is not valid.");
        }
        parameterFactory.addCorrelation(gtuType, firstType, thenType, correlation);
    }

    /**
     * Correlates parameters using an expression.
     * @param <F> value type of first parameter.
     * @param <T> value type of then parameter.
     * @param first value of first parameter.
     * @param then value of then parameter.
     * @param expression expression of the correlation.
     * @param clazz Class<?>; type of T, not explicit as equivalence is not derivable in the calling context.
     * @return correlated new value of then parameter.
     */
    @SuppressWarnings("unchecked")
    private static <F, T> T correlateFromExpression(final F first, final T then, final String expression, final Class<?> clazz)
    {
        Eval eval = new Eval();
        eval.setRetrieveValue((value) ->
        {
            if (value.equals("first"))
            {
                if (first instanceof DoubleScalar<?, ?>)
                {
                    return first;
                }
                return Dimensionless.ofSI(((Number) first).doubleValue());
            }
            if (value.equals("then"))
            {
                if (then instanceof DoubleScalar<?, ?>)
                {
                    return then;
                }
                return Dimensionless.ofSI(((Number) then).doubleValue());
            }
            throw new OtsRuntimeException(
                    "Value for " + value + " in correlation expression is not valid. Only 'first' and 'then' allowed.");
        });
        Object result = eval.evaluate(expression);
        if (DoubleScalar.class.isAssignableFrom(clazz))
        {
            try
            {
                Method method = clazz.getDeclaredMethod("ofSI", new Class<?>[] {double.class});
                return (T) method.invoke(null, ((DoubleScalar<?, ?>) result).si);
            }
            catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
            {
                throw new OtsRuntimeException("Unable to cast result of expression " + expression, ex);
            }
        }
        if (Double.class.isAssignableFrom(clazz))
        {
            return (T) (Double) ((Number) result).doubleValue();
        }
        return (T) (Integer) ((Number) result).intValue();
    }

    /**
     * Creates strategical planner factories for models.
     * @param otsNetwork network
     * @param definitions parsed definitions
     * @param models models
     * @param eval expression evaluator
     * @param parameterTypes parameter types
     * @param streamInformation stream information
     * @param parameterFactory parameter factories
     * @param <U> a unit
     * @param <T> a scalar type
     * @param <K> a parameter type value
     * @return strategical planner factories by model ID
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    public static <U extends Unit<U>, T extends DoubleScalarRel<U, T>,
            K> Map<String, LaneBasedStrategicalPlannerFactory<?>> parseModel(final RoadNetwork otsNetwork,
                    final Definitions definitions, final List<ModelType> models, final Eval eval,
                    final Map<String, ParameterType<?>> parameterTypes, final StreamInformation streamInformation,
                    final ParameterFactory parameterFactory) throws XmlParserException
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
                    tacticalPlannerFactory =
                            parseLmrs(model.getTacticalPlanner().getLmrs(), definitions, streamInformation, eval);
                }
                else
                {
                    throw new XmlParserException("Tactical planner has unsupported value.");
                }
            }
            else
            {
                // default
                tacticalPlannerFactory = new LmrsFactory<>(Lmrs::new).set(Setting.ACCELERATION_TRAFFIC_LIGHTS, true)
                        .set(Setting.ACCELERATION_CONFLICTS, true);
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
     * Parse LMRS model.
     * @param lmrs org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs; Lmrs information
     * @param definitions parsed definitions
     * @param streamInformation stream information
     * @param eval expression evaluator.
     * @return LMRS factory
     * @throws XmlParserException unknown value, missing constructor, etc.
     */
    private static LaneBasedTacticalPlannerFactory<Lmrs> parseLmrs(
            final org.opentrafficsim.xml.generated.ModelType.TacticalPlanner.Lmrs lmrs, final Definitions definitions,
            final StreamInformation streamInformation, final Eval eval) throws XmlParserException
    {
        LmrsFactory<Lmrs> lmrsFactory =
                new LmrsFactory<Lmrs>(definitions.getAll(GtuType.class).values().stream().toList(), Lmrs::new);
        setForModel(lmrs.getDefaultModel(), lmrsFactory, null, eval);
        for (GtuTypeLmrsModel gtuTypeModel : lmrs.getGtuTypeModel())
        {
            setForModel(gtuTypeModel, lmrsFactory, definitions.getOrThrow(GtuType.class, gtuTypeModel.getGtuType().get(eval)),
                    eval);
        }
        lmrsFactory.setStream(streamInformation.getStream("default"));
        return lmrsFactory;
    }

    /**
     * Set all settings in the model tag on the factory for the given GTU type. The GTU type may be {@code null} for the default
     * model setting the default values.
     * @param gtuTypeModel LMRS model tag
     * @param lmrsFactory LMRS factory
     * @param gtuType GTU type
     * @param eval evaluator for expressions
     * @throws XmlParserException in case of unknown Fuller implementation
     */
    private static void setForModel(final LmrsModel gtuTypeModel, final LmrsFactory<Lmrs> lmrsFactory, final GtuType gtuType,
            final Eval eval) throws XmlParserException
    {
        // behavioral modules
        setForModel(lmrsFactory, gtuTypeModel.getCarFollowingModel(), Setting.CAR_FOLLOWING_MODEL, gtuType, eval);
        setForModel(lmrsFactory, gtuTypeModel.getSynchronization(), Setting.SYNCHRONIZATION, gtuType, eval);
        setForModel(lmrsFactory, gtuTypeModel.getCooperation(), Setting.COOPERATION, gtuType, eval);
        setForModel(lmrsFactory, gtuTypeModel.getGapAcceptance(), Setting.GAP_ACCEPTANCE, gtuType, eval);

        // mandatory incentives
        MandatoryIncentives mi = gtuTypeModel.getMandatoryIncentives();
        setForModel(lmrsFactory, mi.getRoute(), Setting.INCENTIVE_ROUTE, gtuType, eval);
        setForModel(lmrsFactory, mi.getGetInLane(), Setting.INCENTIVE_GET_IN_LANE, gtuType, eval);

        // voluntary incentives
        VoluntaryIncentives vi = gtuTypeModel.getVoluntaryIncentives();
        setForModel(lmrsFactory, vi.getSpeed(), Setting.INCENTIVE_SPEED_WITH_COURTESY, gtuType, eval);
        setForModel(lmrsFactory, vi.getKeep(), Setting.INCENTIVE_KEEP, gtuType, eval);
        setForModel(lmrsFactory, vi.getCourtesy(), Setting.INCENTIVE_COURTESY, gtuType, eval);
        setForModel(lmrsFactory, vi.getQueue(), Setting.INCENTIVE_QUEUE, gtuType, eval);
        setForModel(lmrsFactory, vi.getStayOnSlowLanes(), Setting.INCENTIVE_STAY_ON_SLOW_LANES, gtuType, eval);

        // acceleration incentives
        AccelerationIncentives ai = gtuTypeModel.getAccelerationIncentives();
        setForModel(lmrsFactory, ai.getSpeedLimitTransitions(), Setting.ACCELERATION_SPEED_LIMIT_TRANSITION, gtuType, eval);
        setForModel(lmrsFactory, ai.getTrafficLights(), Setting.ACCELERATION_TRAFFIC_LIGHTS, gtuType, eval);
        setForModel(lmrsFactory, ai.getConflicts(), Setting.ACCELERATION_CONFLICTS, gtuType, eval);
        setForModel(lmrsFactory, ai.getNoSlowLaneOvertake(), Setting.ACCELERATION_NO_SLOW_LANE_OVERTAKE, gtuType, eval);

        // social interactions
        SocialInteractions si = gtuTypeModel.getSocialInteractions();
        setForModel(lmrsFactory, si.getSocialPressure(), Setting.SOCIO_PRESSURE, gtuType, eval);
        setForModel(lmrsFactory, si.getTailgating(), Setting.SOCIO_TAILGATING, gtuType, eval);
        setForModel(lmrsFactory, si.getLaneChanges(), Setting.SOCIO_LANE_CHANGE, gtuType, eval);
        setForModel(lmrsFactory, si.getSpeed(), Setting.SOCIO_SPEED, gtuType, eval);

        // perception
        if (gtuTypeModel.getPerception().getNone() == null)
        {
            if (gtuTypeModel.getPerception().getFullerAttentionMatrix() != null)
            {
                lmrsFactory.set(Setting.FULLER_IMPLEMENTATION, FullerImplementation.ATTENTION_MATRIX);
                FullerAttentionMatrix f = gtuTypeModel.getPerception().getFullerAttentionMatrix();
                parseFuller(f, lmrsFactory, gtuType, eval);

                // tasks
                FullerAttentionMatrix.Tasks t = f.getTasks();
                setForModel(lmrsFactory, t.getFreeAcceleration(), Setting.TASK_FREE_ACCELERATION, gtuType, eval);
                setForModel(lmrsFactory, t.getTrafficLights(), Setting.TASK_TRAFFIC_LIGHTS, gtuType, eval);
                setForModel(lmrsFactory, t.getSignal(), Setting.TASK_SIGNAL, gtuType, eval);
                setForModel(lmrsFactory, t.getCooperation(), Setting.TASK_COOPERATION, gtuType, eval);
                setForModel(lmrsFactory, t.getIntersection(), Setting.TASK_CONFLICTS, gtuType, eval);
                setForModel(lmrsFactory, t.getCarFollowing(), Setting.TASK_CAR_FOLLOWING, gtuType, eval);
                setForModel(lmrsFactory, t.getLaneChanging(), Setting.TASK_LANE_CHANGE, gtuType, eval);
                setForModel(lmrsFactory, t.getRoadSideDistraction(), Setting.TASK_ROADSIDE_DISTRACTION, gtuType, eval);

                // behavioral adaptations
                FullerAttentionMatrix.BehavioralAdaptations ba = f.getBehavioralAdaptations();
                parseSummativeOrArAdaptations(f.getBehavioralAdaptations(), lmrsFactory, gtuType, eval);
                setForModel(lmrsFactory, ba.getUpdateTime(), Setting.ADAPTATION_UPDATE_TIME, gtuType, eval);
            }
            else if (gtuTypeModel.getPerception().getFullerAnticipationReliance() != null)
            {
                lmrsFactory.set(Setting.FULLER_IMPLEMENTATION, FullerImplementation.ANTICIPATION_RELIANCE);
                FullerAnticipationReliance f = gtuTypeModel.getPerception().getFullerAnticipationReliance();
                parseFuller(f, lmrsFactory, gtuType, eval);

                // tasks
                parseSummativeOrArTasks(f.getTasks(), lmrsFactory, gtuType, eval);
                setForModel(lmrsFactory, f.getTasks().getPrimaryTask(), Setting.PRIMARY_TASK, gtuType, eval);

                // behavioral adaptations
                parseSummativeOrArAdaptations(f.getBehavioralAdaptations(), lmrsFactory, gtuType, eval);
            }
            else if (gtuTypeModel.getPerception().getFullerSummative() != null)
            {
                lmrsFactory.set(Setting.FULLER_IMPLEMENTATION, FullerImplementation.SUMMATIVE);
                FullerSummative f = gtuTypeModel.getPerception().getFullerSummative();
                parseFuller(f, lmrsFactory, gtuType, eval);

                // tasks
                parseSummativeOrArTasks(f.getTasks(), lmrsFactory, gtuType, eval);

                // behavioral adaptations
                parseSummativeOrArAdaptations(f.getBehavioralAdaptations(), lmrsFactory, gtuType, eval);
            }
            else
            {
                throw new XmlParserException("Model has unknown perception model selected.");
            }
        }
    }

    /**
     * Parse common Fuller settings.
     * @param fuller Fuller
     * @param lmrsFactory LMRS factory
     * @param gtuType GTU type
     * @param eval evaluator for expressions
     */
    private static void parseFuller(final Fuller fuller, final LmrsFactory<Lmrs> lmrsFactory, final GtuType gtuType,
            final Eval eval)
    {
        setForModel(lmrsFactory, fuller.getTemporalAnticipation(), Setting.TEMPORAL_ANTICIPATION, gtuType, eval);
        setForModel(lmrsFactory, fuller.getFractionOverestimation(), Setting.FRACTION_OVERESTIMATION, gtuType, eval);
    }

    /**
     * Parse common parts of tasks between summative and AR Fuller implementations.
     * @param tasks tasks
     * @param lmrsFactory LMRS factory
     * @param gtuType GTU type
     * @param eval evaluator for expressions
     */
    private static void parseSummativeOrArTasks(final FullerTasksSummativeAndAr tasks, final LmrsFactory<Lmrs> lmrsFactory,
            final GtuType gtuType, final Eval eval)
    {
        boolean altCf = tasks.getAlternateCarFollowing() == null ? false : tasks.getAlternateCarFollowing().get(eval);
        boolean altLc = tasks.getAlternateLaneChanging() == null ? false : tasks.getAlternateLaneChanging().get(eval);
        setForModel(lmrsFactory, tasks.getCarFollowing(),
                altCf ? Setting.TASK_CAR_FOLLOWING_ALTERNATE : Setting.TASK_CAR_FOLLOWING, gtuType, eval);
        setForModel(lmrsFactory, tasks.getLaneChanging(), altLc ? Setting.TASK_LANE_CHANGE_ALTERNATE : Setting.TASK_LANE_CHANGE,
                gtuType, eval);
        // make sure the non-chosen versions are false
        BooleanType bool = new BooleanType(false);
        setForModel(lmrsFactory, bool, !altCf ? Setting.TASK_CAR_FOLLOWING_ALTERNATE : Setting.TASK_CAR_FOLLOWING, gtuType,
                eval);
        setForModel(lmrsFactory, bool, !altLc ? Setting.TASK_LANE_CHANGE_ALTERNATE : Setting.TASK_LANE_CHANGE, gtuType, eval);
        setForModel(lmrsFactory, tasks.getRoadSideDistraction(), Setting.TASK_ROADSIDE_DISTRACTION, gtuType, eval);
    }

    /**
     * Parse common behavioral adaptations.
     * @param ba behavioral adaptations
     * @param lmrsFactory LMRS factory
     * @param gtuType GTU type
     * @param eval evaluator for expressions
     */
    private static void parseSummativeOrArAdaptations(final FullerBehavioralAdaptations ba, final LmrsFactory<Lmrs> lmrsFactory,
            final GtuType gtuType, final Eval eval)
    {
        setForModel(lmrsFactory, ba.getSpeed(), Setting.ADAPTATION_SPEED, gtuType, eval);
        setForModel(lmrsFactory, ba.getHeadway(), Setting.ADAPTATION_HEADWAY, gtuType, eval);
        setForModel(lmrsFactory, ba.getLaneChange(), Setting.ADAPTATION_LANE_CHANGE, gtuType, eval);
    }

    /**
     * Sets the value for the GTU type as setting in the LMRS factory. If the GTU type is {@code null}, this is done as default
     * setting.
     * @param <V> value type
     * @param lmrsFactory LMRS factory
     * @param expression expression type for value
     * @param setting the correct factory setting for the value to set
     * @param gtuType GTU type, may be {@code null}
     * @param eval evaluator for expressions
     */
    private static <V> void setForModel(final LmrsFactory<Lmrs> lmrsFactory, final ExpressionType<V> expression,
            final Setting<V> setting, final GtuType gtuType, final Eval eval)
    {
        if (expression != null)
        {
            if (gtuType == null)
            {
                lmrsFactory.set(setting, expression.get(eval));
            }
            else
            {
                lmrsFactory.set(setting, expression.get(eval), gtuType);
            }
        }
    }

}
