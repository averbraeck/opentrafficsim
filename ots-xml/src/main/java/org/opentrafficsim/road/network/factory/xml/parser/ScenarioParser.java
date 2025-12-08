package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djutils.base.Identifiable;
import org.djutils.eval.Eval;
import org.djutils.eval.RetrieveValue;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.road.network.factory.xml.CircularDependencyException;
import org.opentrafficsim.xml.bindings.types.ExpressionType;
import org.opentrafficsim.xml.generated.Demand;
import org.opentrafficsim.xml.generated.InputParameters;
import org.opentrafficsim.xml.generated.ModelIdReferralType;
import org.opentrafficsim.xml.generated.ScenarioType;
import org.opentrafficsim.xml.generated.Scenarios;

/**
 * Parser of scenario tags.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ScenarioParser
{

    /**
     * Constructor.
     */
    private ScenarioParser()
    {
        //
    }

    /**
     * Parse input parameters for scenario.
     * @param scenarios scenarios tag.
     * @param scenario name of scenario tp parse.
     * @return expression evaluator for all expression in XML.
     */
    public static Eval parseInputParameters(final Scenarios scenarios, final String scenario)
    {
        if (scenarios == null)
        {
            return new Eval();
        }
        ScenariosWrapper scenariosWrapper = new ScenariosWrapper()
        {
            @Override
            public Iterable<ParameterWrapper> getDefaultInputParameters()
            {
                return getInputParameterIterator(scenarios.getDefaultInputParameters());
            }

            @Override
            public Iterable<ParameterWrapper> getScenarioInputParameters()
            {
                for (ScenarioType scenarioTag : scenarios.getScenario())
                {
                    if (scenarioTag.getId().equals(scenario))
                    {
                        return getInputParameterIterator(scenarioTag.getInputParameters());
                    }
                }
                return null;
            }
        };
        return parseInputParameters(scenariosWrapper);
    }

    /**
     * Parse input parameters for scenario.
     * @param scenariosWrapper scenarios wrapper, from XML or Xsd Tree nodes in editor.
     * @return expression evaluator for all expression in XML.
     */
    public static Eval parseInputParameters(final ScenariosWrapper scenariosWrapper)
    {
        Map<String, Supplier<?>> defaultsMap = new LinkedHashMap<>();
        ParameterMap defaults = new ParameterMap(defaultsMap);
        Eval eval = new Eval().setRetrieveValue(defaults);
        parseInputParameters(scenariosWrapper.getDefaultInputParameters(), defaultsMap, defaults);
        if (scenariosWrapper.getScenarioInputParameters() != null)
        {
            Map<String, Supplier<?>> inputParametersMap = new LinkedHashMap<>();
            ParameterMap inputParameters = new ParameterMap(inputParametersMap);
            defaults.setScenarioMap(inputParameters);
            parseInputParameters(scenariosWrapper.getScenarioInputParameters(), inputParametersMap, defaults);
        }
        // test whether values can be obtained successfully (might throw CircularDependencyException)
        Map<String, String> result = new LinkedHashMap<>();
        for (ParameterWrapper parameter : scenariosWrapper.getDefaultInputParameters())
        {
            String id = parameter.getId();
            result.put(id, eval.evaluate(id.substring(1, id.length() - 1)).toString());
        }
        if (scenariosWrapper.getScenarioInputParameters() != null)
        {
            for (ParameterWrapper parameter : scenariosWrapper.getScenarioInputParameters())
            {
                String id = parameter.getId();
                result.put(id, eval.evaluate(id.substring(1, id.length() - 1)).toString());
            }
        }
        Logger.ots().trace("Input parameters:");
        result.forEach((k, v) -> Logger.ots().trace("{}: {}", k, v));
        return eval;
    }

    /**
     * Creates parsable parameters from an InputParameters XML tag (default or of a scenario).
     * @param inputParameters parameters XML tag.
     * @return parameters in generic form for parsing.
     */
    private static Iterable<ParameterWrapper> getInputParameterIterator(final InputParameters inputParameters)
    {
        List<ParameterWrapper> parameters = new ArrayList<>();
        if (inputParameters == null)
        {
            return parameters;
        }
        for (Serializable parameter : inputParameters.getDurationOrLengthOrSpeed())
        {
            if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Duration)
            {
                org.opentrafficsim.xml.generated.InputParameters.Duration p =
                        (org.opentrafficsim.xml.generated.InputParameters.Duration) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Length)
            {
                org.opentrafficsim.xml.generated.InputParameters.Length p =
                        (org.opentrafficsim.xml.generated.InputParameters.Length) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Speed)
            {
                org.opentrafficsim.xml.generated.InputParameters.Speed p =
                        (org.opentrafficsim.xml.generated.InputParameters.Speed) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Acceleration)
            {
                org.opentrafficsim.xml.generated.InputParameters.Acceleration p =
                        (org.opentrafficsim.xml.generated.InputParameters.Acceleration) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.LinearDensity)
            {
                org.opentrafficsim.xml.generated.InputParameters.LinearDensity p =
                        (org.opentrafficsim.xml.generated.InputParameters.LinearDensity) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Frequency)
            {
                org.opentrafficsim.xml.generated.InputParameters.Frequency p =
                        (org.opentrafficsim.xml.generated.InputParameters.Frequency) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Double)
            {
                org.opentrafficsim.xml.generated.InputParameters.Double p =
                        (org.opentrafficsim.xml.generated.InputParameters.Double) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Fraction)
            {
                org.opentrafficsim.xml.generated.InputParameters.Fraction p =
                        (org.opentrafficsim.xml.generated.InputParameters.Fraction) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Integer)
            {
                org.opentrafficsim.xml.generated.InputParameters.Integer p =
                        (org.opentrafficsim.xml.generated.InputParameters.Integer) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Boolean)
            {
                org.opentrafficsim.xml.generated.InputParameters.Boolean p =
                        (org.opentrafficsim.xml.generated.InputParameters.Boolean) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.String)
            {
                org.opentrafficsim.xml.generated.InputParameters.String p =
                        (org.opentrafficsim.xml.generated.InputParameters.String) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
            else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Class)
            {
                org.opentrafficsim.xml.generated.InputParameters.Class p =
                        (org.opentrafficsim.xml.generated.InputParameters.Class) parameter;
                parameters.add(new ParameterWrapper(p.getId(), p.getValue()));
            }
        }
        return parameters;
    }

    /**
     * Parse input parameters.
     * @param inputParameters xml tag.
     * @param map map that underlines inputParameters.
     * @param retrieve value retrieval.
     */
    private static void parseInputParameters(final Iterable<ParameterWrapper> inputParameters,
            final Map<String, Supplier<?>> map, final ParameterMap retrieve)
    {
        Eval eval = new Eval().setRetrieveValue(retrieve);
        for (ParameterWrapper parameter : inputParameters)
        {
            // need to create a new Eval each time, as input parameters may depend on others
            // NOTE: if Eval has additional user defined functions or unit parsers, that's not included here
            String id = parameter.getId();
            map.put(id.substring(1, id.length() - 1), () -> parameter.get().get(eval));
        }
    }

    /**
     * Parse model ID referrals.
     * @param scenario scenario
     * @param demand demand
     * @param eval expression evaluator.
     * @return map from ID to ID
     */
    public static Map<String, String> parseModelIdReferral(final List<ScenarioType> scenario, final Demand demand,
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
     * Generic scenario form for parsing.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface ScenariosWrapper
    {
        /**
         * Returns default parameters in generic form for parsing.
         * @return default parameters in generic form for parsing.
         */
        Iterable<ParameterWrapper> getDefaultInputParameters();

        /**
         * Returns scenario parameters in generic form for parsing.
         * @return scenario parameters in generic form for parsing.
         */
        Iterable<ParameterWrapper> getScenarioInputParameters();
    }

    /**
     * Generic parameters for parsing.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param id id.
     * @param value value expression type.
     */
    public record ParameterWrapper(String id, ExpressionType<?> value) implements Identifiable, Supplier<ExpressionType<?>>
    {
        @Override
        public String getId()
        {
            return this.id();
        }

        @Override
        public ExpressionType<?> get()
        {
            return this.value();
        }
    }

    /**
     * Wraps parameters to provide for expressions.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private static class ParameterMap implements RetrieveValue
    {
        /** Map of name to suppliers (constant or distribution). */
        private final Map<String, Supplier<?>> map;

        /** Set of currently looked up values, to detect circular dependency. */
        private final Set<String> lookingUp = new LinkedHashSet<>();

        /** More scenario input parameters. */
        private ParameterMap scenario;

        /**
         * Constructor.
         * @param map map that underlines input parameters.
         */
        ParameterMap(final Map<String, Supplier<?>> map)
        {
            this.map = map;
        }

        /**
         * Set scenario input parameters.
         * @param scenario parameter map of the scenario.
         */
        public void setScenarioMap(final ParameterMap scenario)
        {
            this.scenario = scenario;
        }

        @Override
        public Object lookup(final String name)
        {
            if (!this.lookingUp.add(name))
            {
                throw new CircularDependencyException("Parameter " + name + " is part of a circular dependency.");
            }
            Object value;
            if (this.scenario != null && this.scenario.map.containsKey(name))
            {
                value = this.scenario.map.get(name).get();
            }
            else if (this.map.containsKey(name))
            {
                value = this.map.get(name).get();
            }
            else
            {
                this.lookingUp.remove(name);
                throw new OtsRuntimeException("Parameter " + name + " not available.");
            }
            this.lookingUp.remove(name);
            if (value instanceof Double)
            {
                return Dimensionless.ofSI((Double) value);
            }
            if (value instanceof Integer)
            {
                return Dimensionless.ofSI((Integer) value);
            }
            return value;
        }
    }
}
