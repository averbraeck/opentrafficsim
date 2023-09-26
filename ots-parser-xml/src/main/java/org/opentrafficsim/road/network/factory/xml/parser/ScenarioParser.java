package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djutils.eval.Eval;
import org.djutils.eval.RetrieveValue;
import org.opentrafficsim.road.network.factory.xml.CircularDependencyException;
import org.opentrafficsim.xml.generated.Demand;
import org.opentrafficsim.xml.generated.ModelIdReferralType;
import org.opentrafficsim.xml.generated.Ots;
import org.opentrafficsim.xml.generated.ScenarioType;

/**
 * Parser of scenario tags.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ScenarioParser
{

    /**
     * Parse input parameters for scenario.
     * @param ots Ots; OTS tag.
     * @param scenario String; name of scenario tp parse.
     * @return Eval; expression evaluator for all expression in XML.
     * @throws CircularDependencyException when there is circular dependency between parameters.
     */
    public static Eval parseInputParameters(final Ots ots, final String scenario) throws CircularDependencyException
    {
        Map<String, Supplier<?>> defaultsMap = new LinkedHashMap<>();
        ParameterWrapper defaults = new ParameterWrapper(null, defaultsMap);
        Eval eval = new Eval().setRetrieveValue(defaults);
        if (ots.getScenarios() != null)
        {
            parseInputParameters(ots.getScenarios().getDefaultInputParameters(), defaultsMap, eval);
        }
        ParameterWrapper inputParameters = defaults;
        if (ots.getScenarios() != null)
        {
            for (ScenarioType scenarioTag : ots.getScenarios().getScenario())
            {
                if (scenarioTag.getId().equals(scenario))
                {
                    if (scenarioTag.getInputParameters() != null)
                    {
                        Map<String, Supplier<?>> inputParametersMap = new LinkedHashMap<>();
                        inputParameters = new ParameterWrapper(defaults, inputParametersMap);
                        eval.setRetrieveValue(inputParameters);
                        parseInputParameters(scenarioTag.getInputParameters(), inputParametersMap, eval);
                    }
                    break;
                }
            }
        }
        return eval;
    }

    /**
     * Parse model ID referrals.
     * @param scenario List&lt;ScenarioType&gt;; scenario
     * @param demand Demand; demand
     * @param eval Eval; expression evaluator.
     * @return map from ID to ID
     */
    public static final Map<String, String> parseModelIdReferral(final List<ScenarioType> scenario, final Demand demand,
            final Eval eval)
    {
        // TODO: use run to select scenario (probably outside this class, and accept a single Scenario
        Map<String, String> map = new LinkedHashMap<>();
        for (ModelIdReferralType modelIdReferral : demand.getModelIdReferral())
        {
            map.put(modelIdReferral.getId(), modelIdReferral.getModelId().get(eval));
        }
        // overwrite with scenario level ID referrals
        if (!scenario.isEmpty())
        {
            for (ModelIdReferralType modelIdReferral : scenario.get(0).getModelIdReferral())
            {
                map.put(modelIdReferral.getId(), modelIdReferral.getModelId().get(eval));
            }
        }
        return map;
    }

    /**
     * Parse input parameters.
     * @param inputParametersXml InputParameters; xml tag.
     * @param map Map&lt;String, Supplier&lt;?&gt;&gt;; map that underlines inputParameters.
     * @param eval Eval; expression evaluator.
     * @throws CircularDependencyException when there is circular dependency between parameters.
     */
    private static void parseInputParameters(final org.opentrafficsim.xml.generated.InputParameters inputParametersXml,
            final Map<String, Supplier<?>> map, final Eval eval) throws CircularDependencyException
    {
        boolean failed = true;
        int pass = 1;
        while (failed)
        {
            failed = false;
            int size = map.size();
            for (Serializable parameter : inputParametersXml.getDurationOrLengthOrSpeed())
            {
                // try
                // {
                if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Duration)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Duration p =
                            (org.opentrafficsim.xml.generated.InputParameters.Duration) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.DurationDist)
                // {
                // org.opentrafficsim.xml.generated.InputParameters.DurationDist p =
                // (org.opentrafficsim.xml.generated.InputParameters.DurationDist) parameter;
                // ContinuousDistDoubleScalar.Rel<?, ?> d = ParseDistribution.parseContinuousDist(streamInformation, p,
                // p.getDurationUnit().get(eval), eval);
                // map.put(trim(p.getId()), () -> d.draw());
                // }
                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Length)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Length p =
                            (org.opentrafficsim.xml.generated.InputParameters.Length) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.LengthDist)
                // {
                // org.opentrafficsim.xml.generated.InputParameters.LengthDist p =
                // (org.opentrafficsim.xml.generated.InputParameters.LengthDist) parameter;
                // ContinuousDistDoubleScalar.Rel<?, ?> d =
                // ParseDistribution.parseContinuousDist(streamInformation, p, p.getLengthUnit().get(eval), eval);
                // map.put(trim(p.getId()), () -> d.draw());
                // }
                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Speed)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Speed p =
                            (org.opentrafficsim.xml.generated.InputParameters.Speed) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.SpeedDist)
                // {
                // org.opentrafficsim.xml.generated.InputParameters.SpeedDist p =
                // (org.opentrafficsim.xml.generated.InputParameters.SpeedDist) parameter;
                // ContinuousDistDoubleScalar.Rel<?, ?> d =
                // ParseDistribution.parseContinuousDist(streamInformation, p, p.getSpeedUnit().get(eval), eval);
                // map.put(trim(p.getId()), () -> d.draw());
                // }
                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Acceleration)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Acceleration p =
                            (org.opentrafficsim.xml.generated.InputParameters.Acceleration) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.AccelerationDist)
                // {
                // org.opentrafficsim.xml.generated.InputParameters.AccelerationDist p =
                // (org.opentrafficsim.xml.generated.InputParameters.AccelerationDist) parameter;
                // ContinuousDistDoubleScalar.Rel<?, ?> d = ParseDistribution.parseContinuousDist(streamInformation, p,
                // p.getAccelerationUnit().get(eval), eval);
                // map.put(trim(p.getId()), () -> d.draw());
                // }
                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.LinearDensity)
                {
                    org.opentrafficsim.xml.generated.InputParameters.LinearDensity p =
                            (org.opentrafficsim.xml.generated.InputParameters.LinearDensity) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.LinearDensityDist)
                // {
                // org.opentrafficsim.xml.generated.InputParameters.LinearDensityDist p =
                // (org.opentrafficsim.xml.generated.InputParameters.LinearDensityDist) parameter;
                // ContinuousDistDoubleScalar.Rel<?, ?> d = ParseDistribution.parseContinuousDist(streamInformation, p,
                // p.getLinearDensityUnit().get(eval), eval);
                // map.put(trim(p.getId()), () -> d.draw());
                // }
                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Frequency)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Frequency p =
                            (org.opentrafficsim.xml.generated.InputParameters.Frequency) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.FrequencyDist)
                // {
                // org.opentrafficsim.xml.generated.InputParameters.FrequencyDist p =
                // (org.opentrafficsim.xml.generated.InputParameters.FrequencyDist) parameter;
                // ContinuousDistDoubleScalar.Rel<?, ?> d = ParseDistribution.parseContinuousDist(streamInformation, p,
                // p.getFrequencyUnit().get(eval), eval);
                // map.put(trim(p.getId()), () -> d.draw());
                // }

                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Double)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Double p =
                            (org.opentrafficsim.xml.generated.InputParameters.Double) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.DoubleDist)
                // {
                // org.opentrafficsim.xml.generated.InputParameters.DoubleDist p =
                // (org.opentrafficsim.xml.generated.InputParameters.DoubleDist) parameter;
                // DistContinuous d = ParseDistribution.makeDistContinuous(streamInformation, p, eval);
                // map.put(trim(p.getId()), () -> d.draw());
                // }

                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Fraction)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Fraction p =
                            (org.opentrafficsim.xml.generated.InputParameters.Fraction) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }

                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Integer)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Integer p =
                            (org.opentrafficsim.xml.generated.InputParameters.Integer) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.IntegerDist)
                // {
                // org.opentrafficsim.xml.generated.InputParameters.IntegerDist p =
                // (org.opentrafficsim.xml.generated.InputParameters.IntegerDist) parameter;
                // DistDiscrete d = ParseDistribution.makeDistDiscrete(streamInformation, p, eval);
                // map.put(trim(p.getId()), () -> d.draw());
                // }

                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Boolean)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Boolean p =
                            (org.opentrafficsim.xml.generated.InputParameters.Boolean) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }

                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.String)
                {
                    org.opentrafficsim.xml.generated.InputParameters.String p =
                            (org.opentrafficsim.xml.generated.InputParameters.String) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }

                else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Class)
                {
                    org.opentrafficsim.xml.generated.InputParameters.Class p =
                            (org.opentrafficsim.xml.generated.InputParameters.Class) parameter;
                    map.put(trim(p.getId()), () -> p.getValue().get(eval));
                }
                // }
                // catch (XmlParserException e) // TODO: catch eval exception
                // {
                // failed = true;
                // }
            }
            if ((map.size() == size && !inputParametersXml.getDurationOrLengthOrSpeed().isEmpty()) || pass == 50)
            {
                throw new CircularDependencyException("Could not parse input parameters due to circular dependency.");
            }
            pass++;
        }
    }

    /**
     * Strips curly brackets (or any character) from start and end of input string.
     * @param id String; id.
     * @return string without curly brackets.
     */
    private static String trim(final String id)
    {
        return id.substring(1, id.length() - 1);
    }

    /**
     * Wraps parameters to provide for expressions.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private static class ParameterWrapper implements RetrieveValue
    {
        /** Default input parameters. */
        private final ParameterWrapper defaults;

        /** Map of name to suppliers (constant or distribution). */
        private final Map<String, Supplier<?>> map;

        /**
         * Constructor.
         * @param defaults ParameterWrapper; default parameters, may be {@code null}.
         * @param map Map&lt;String, Supplier&lt;?&gt;&gt;; map that underlines input parameters.
         */
        public ParameterWrapper(final ParameterWrapper defaults, final Map<String, Supplier<?>> map)
        {
            this.defaults = defaults;
            this.map = map;
        }

        /** {@inheritDoc} */
        @Override
        public Object lookup(final String name)
        {
            if (this.map.containsKey(name))
            {
                Object value = this.map.get(name).get();
                if (value instanceof Double)
                {
                    return Dimensionless.instantiateSI((Double) value);
                }
                return value;
            }
            if (this.defaults == null)
            {
                throw new RuntimeException("Parameter " + name + " not available.");
            }
            return this.defaults.lookup(name);
        }
    }

}
