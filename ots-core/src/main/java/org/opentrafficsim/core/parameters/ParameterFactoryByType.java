package org.opentrafficsim.core.parameters;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Sets parameter values based on the the GTU type. This includes stochastic parameters. Parameters may also be defined for all
 * GTU types. Similarly, correlations between two parameters can be determined, for all or a specific GTU type.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterFactoryByType implements ParameterFactory
{

    /** Parameters. */
    private final Map<GtuType, Set<ParameterEntry<?>>> map = new LinkedHashMap<>();

    /** Map of correlations per {@link GtuType}, dependent {@link ParameterType} and independent {@link ParameterType}. */
    private Map<GtuType, Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>>> correlations = new LinkedHashMap<>();

    /**
     * Constructor.
     */
    public ParameterFactoryByType()
    {
        //
    }

    @Override
    public void setValues(final Parameters parameters, final GtuType gtuType) throws ParameterException
    {
        Map<ParameterType<?>, Object> values = getBaseValues(gtuType);
        Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> correls = getCorrelations(gtuType);
        applyCorrelationsAndSetValues(parameters, values, correls);
    }

    /**
     * Gathers all the base values set by the factory, fixed or random, as given for the GTU type's hierarchy.
     * @param gtuType GTU type
     * @return all the base values set by the factory
     */
    protected Map<ParameterType<?>, Object> getBaseValues(final GtuType gtuType)
    {
        Map<ParameterType<?>, Object> baseValues = new LinkedHashMap<>();
        GtuType parent = gtuType;
        while (true)
        {
            for (ParameterEntry<?> entry : this.map.getOrDefault(parent, Collections.emptySet()))
            {
                // maintain specificity of GTU type
                baseValues.putIfAbsent(entry.getParameterType(), entry.getValue());
            }
            if (parent == null)
            {
                break;
            }
            parent = parent.getParent();
        }
        return baseValues;
    }

    /**
     * Gathers all the correlations from the factory, as given for the GTU type's hierarchy. This method should return all
     * correlations, regardless of circular or multiple dependencies. It is up to implementations using this method to deal with
     * that (throwing exception or solving is specific way).
     * @param gtuType GTU type
     * @return all the correlations from the factory, by dependent parameter type and independent parameter type
     */
    protected Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> getCorrelations(final GtuType gtuType)
    {
        Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> correls = new LinkedHashMap<>();
        GtuType parent = gtuType;
        while (true)
        {
            for (Entry<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> thenEntry : this.correlations
                    .getOrDefault(parent, Collections.emptyMap()).entrySet())
            {
                Map<ParameterType<?>, Correlation<?, ?>> thenMapOut =
                        correls.computeIfAbsent(thenEntry.getKey(), (t) -> new LinkedHashMap<>());
                for (Entry<ParameterType<?>, Correlation<?, ?>> firstEntry : thenEntry.getValue().entrySet())
                {
                    // maintain specificity of GTU type
                    thenMapOut.putIfAbsent(firstEntry.getKey(), firstEntry.getValue());
                }
            }
            if (parent == null)
            {
                break;
            }
            parent = parent.getParent();
        }
        return correls;
    }

    /**
     * Applies all given correlations to the values map and sets all values in the parameters.
     * @param parameters possible source of independent parameters (for parameters not defined in this factory), and receives
     *            all final parameters values
     * @param baseValues base values map (altered by this method)
     * @param typeCorrelations correlations as gathered for a relevant GTU type, by dependent parameter type and independent
     *            parameter type
     * @throws ParameterException when a parameter is dependent on multiple parameters or a parameter was correlated beyond its
     *             bounds
     */
    protected void applyCorrelationsAndSetValues(final Parameters parameters, final Map<ParameterType<?>, Object> baseValues,
            final Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> typeCorrelations) throws ParameterException
    {
        Map<ParameterType<?>, Correlation<?, ?>> correls = new LinkedHashMap<>();
        Map<ParameterType<?>, ParameterType<?>> thenFirst = new LinkedHashMap<>();
        for (Entry<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> thenEntry : typeCorrelations.entrySet())
        {
            Throw.when(thenEntry.getValue().size() > 1, ParameterException.class,
                    "Dependent parameter %s is dependent on multiple parameters.", thenEntry.getKey().getId());
            Entry<ParameterType<?>, Correlation<?, ?>> firstEntry = thenEntry.getValue().entrySet().iterator().next();
            correls.put(thenEntry.getKey(), firstEntry.getValue());
            thenFirst.put(thenEntry.getKey(), firstEntry.getKey());
        }
        while (!correls.isEmpty())
        {
            Iterator<Entry<ParameterType<?>, Correlation<?, ?>>> it = correls.entrySet().iterator();
            boolean anySet = false;
            while (it.hasNext())
            {
                Entry<ParameterType<?>, Correlation<?, ?>> entry = it.next();
                ParameterType<?> dependent = entry.getKey();
                ParameterType<?> independent = thenFirst.get(dependent);
                // check independent parameter does not need to be correlated itself
                if (!thenFirst.containsKey(independent))
                {
                    Object first = getValue(parameters, baseValues, independent);
                    Object then = getValue(parameters, baseValues, dependent);
                    if (first != null && then != null)
                    {
                        baseValues.put(dependent, correlateValue(first, then, entry.getValue()));
                        it.remove();
                        thenFirst.remove(dependent);
                        anySet = true;
                    }
                }
            }
            if (!anySet)
            {
                Set<String> ids = correls.keySet().stream().map((pt) -> pt.getId()).collect(Collectors.toSet());
                throw new ParameterException(
                        "Values for parameters " + ids + " are in a circular dependency or depend on a missing parameter.");
            }
        }
        setValues(parameters, baseValues);
    }

    /**
     * Returns a parameter value for correlation. First, a value from the values map is returned if available. Otherwise from
     * the predetermined parameters, which will return {@code null} if the parameter is not available.
     * @param <T> value type
     * @param parameters predetermined parameters
     * @param values map of values determined by this factory
     * @param parameterType parameter type
     * @return parameter value for correlation, {@code null} if no value is available
     */
    @SuppressWarnings("unchecked")
    protected <T> T getValue(final Parameters parameters, final Map<ParameterType<?>, ?> values,
            final ParameterType<?> parameterType)
    {
        if (values.containsKey(parameterType))
        {
            return (T) values.get(parameterType);
        }
        return parameters.getParameterOrNull((ParameterType<T>) parameterType);
    }

    /**
     * Helper method to correlate a typed parameter.
     * @param <T> dependent parameter type
     * @param <C> independent parameter type
     * @param first independent value
     * @param then dependent value
     * @param correlation correlation
     * @return correlated value
     */
    @SuppressWarnings("unchecked")
    protected <T, C> T correlateValue(final Object first, final Object then, final Correlation<?, ?> correlation)
    {
        return ((Correlation<C, T>) correlation).correlate((C) first, (T) then);
    }

    /**
     * Set all values from the map in the parameters.
     * @param parameters parameters
     * @param values value map
     * @throws ParameterException if a parameter was correlated beyond its bounds
     */
    protected void setValues(final Parameters parameters, final Map<ParameterType<?>, Object> values) throws ParameterException
    {
        while (!values.isEmpty())
        {
            Iterator<Entry<ParameterType<?>, Object>> it = values.entrySet().iterator();
            boolean anySet = false;
            while (it.hasNext())
            {
                Entry<ParameterType<?>, Object> entry = it.next();
                boolean setParameter = setParameterValue(parameters, entry.getKey(), entry.getValue());
                /*
                 * If not set, a correlation may have changed the value beyond a limit determined by another parameter. This
                 * other parameter might still need to be set to a correlated value that allows the first parameters. For
                 * example take the condition Tmin < Tmax, with base values 0.56 and 1.2. If both correlate to a third parameter
                 * which should increase both by a factor 2.5, then temporarily 2.5 * 0.56 < 1.2 could not hold. Delay setting.
                 */
                if (setParameter)
                {
                    it.remove();
                    anySet = true;
                }
            }
            if (!anySet)
            {
                Set<String> ids = values.keySet().stream().map((pt) -> pt.getId()).collect(Collectors.toSet());
                throw new ParameterException(
                        "Values for parameters " + ids + " could not be set (probably correlated out of bounds).");
            }
        }
    }

    /**
     * Helper method to set a type parameter and catch exception due to bound and a different required order for the parameters
     * to be set.
     * @param <T> value type
     * @param parameters parameters
     * @param parameterType parameter type
     * @param value value
     * @return whether the value was successfully set
     */
    @SuppressWarnings("unchecked")
    protected <T> boolean setParameterValue(final Parameters parameters, final ParameterType<?> parameterType,
            final Object value)
    {
        try
        {
            parameters.setParameter((ParameterType<T>) parameterType, (T) value);
            return true;
        }
        catch (ParameterException ex)
        {
            return false;
        }
    }

    /**
     * Add parameter.
     * @param gtuType the gtu type
     * @param parameterType the parameter type
     * @param value the value of the parameter
     * @param <T> parameter value type
     */
    public <T> void addParameter(final GtuType gtuType, final ParameterType<T> parameterType, final T value)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new FixedEntry<>(parameterType, value));
    }

    /**
     * Add parameter.
     * @param gtuType the gtu type
     * @param parameterType the parameter type
     * @param distribution ContinuousDistDoubleScalar.Rel&lt;T,U&gt;; the distribution of the parameter
     * @param <U> unit type
     * @param <T> parameter value type
     */
    public <U extends Unit<U>, T extends DoubleScalarRel<U, T>> void addParameter(final GtuType gtuType,
            final ParameterTypeNumeric<T> parameterType, final ContinuousDistDoubleScalar.Rel<T, U> distribution)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new DistributedEntry<>(parameterType, distribution));
    }

    /**
     * Add parameter.
     * @param gtuType the gtu type
     * @param parameterType the parameter type
     * @param distribution the distribution of the parameter
     */
    public void addParameter(final GtuType gtuType, final ParameterType<Integer> parameterType, final DistDiscrete distribution)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new DistributedEntryInteger(parameterType, distribution));
    }

    /**
     * Add parameter.
     * @param gtuType the gtu type
     * @param parameterType the parameter type
     * @param distribution the distribution of the parameter
     */
    public void addParameter(final GtuType gtuType, final ParameterType<Double> parameterType,
            final DistContinuous distribution)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new DistributedEntryDouble(parameterType, distribution));
    }

    /**
     * Add parameter for all GTU types.
     * @param parameterType the parameter type
     * @param value the value of the parameter
     * @param <T> type
     */
    public <T> void addParameter(final ParameterType<T> parameterType, final T value)
    {
        addParameter(null, parameterType, value);
    }

    /**
     * Add parameter for all GTU types.
     * @param parameterType the parameter type
     * @param value the value of the parameter
     */
    public void addParameter(final ParameterTypeDouble parameterType, final double value)
    {
        addParameter(null, parameterType, value);
    }

    /**
     * Add parameter for all GTU types.
     * @param parameterType the parameter type
     * @param distribution ContinuousDistDoubleScalar.Rel&lt;T,U&gt;; the distribution of the parameter
     * @param <U> unit type
     * @param <T> parameter value type
     */
    public <U extends Unit<U>, T extends DoubleScalarRel<U, T>> void addParameter(final ParameterTypeNumeric<T> parameterType,
            final ContinuousDistDoubleScalar.Rel<T, U> distribution)
    {
        addParameter(null, parameterType, distribution);
    }

    /**
     * Add parameter for all GTU types.
     * @param parameterType the parameter type
     * @param distribution the distribution of the parameter
     */
    public void addParameter(final ParameterTypeDouble parameterType, final DistContinuous distribution)
    {
        addParameter(null, parameterType, distribution);
    }

    /**
     * Correlates one parameter to another. The parameter 'first' may also be {@code null}, in which case the parameter can be
     * correlated to an external source.
     * @param gtuType GTU type
     * @param first independent parameter
     * @param then dependent parameter
     * @param correlation correlation
     * @param <C> parameter value type of first parameter
     * @param <T> parameter value type of then parameter
     */
    public <C, T> void addCorrelation(final GtuType gtuType, final ParameterType<C> first, final ParameterType<T> then,
            final Correlation<C, T> correlation)
    {
        assureTypeInMap(gtuType);
        this.correlations.get(gtuType).computeIfAbsent(then, (t) -> new LinkedHashMap<>()).put(first, correlation);
    }

    /**
     * Correlates one parameter to another for all GTU types.
     * @param first independent parameter
     * @param then dependent parameter
     * @param correlation correlation
     * @param <C> parameter value type of first parameter
     * @param <T> parameter value type of then parameter
     */
    public <C, T> void addCorrelation(final ParameterType<C> first, final ParameterType<T> then,
            final Correlation<C, T> correlation)
    {
        addCorrelation(null, first, then, correlation);
    }

    /**
     * Assures the gtu type is in the map.
     * @param gtuType the gtu type
     */
    private void assureTypeInMap(final GtuType gtuType)
    {
        if (!this.map.containsKey(gtuType))
        {
            this.map.put(gtuType, new LinkedHashSet<>());
            this.correlations.put(gtuType, new LinkedHashMap<>());
        }
    }

    @Override
    public String toString()
    {
        return "ParameterFactoryByType [map=" + this.map + "]";
    }

    /**
     * Local storage interface for parameters.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> value type
     */
    private interface ParameterEntry<T>
    {
        /**
         * Returns the value for parameter.
         * @return value for the parameter
         */
        T getValue();

        /**
         * Returns the parameter type.
         * @return parameter type
         */
        ParameterType<T> getParameterType();
    }

    /**
     * Fixed parameter.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> value type
     */
    private final class FixedEntry<T> implements ParameterEntry<T>
    {
        /** Parameter type. */
        private final ParameterType<T> parameterType;

        /** Value. */
        private final T value;

        /**
         * @param parameterType the parameter type
         * @param value the fixed value
         */
        FixedEntry(final ParameterType<T> parameterType, final T value)
        {
            this.parameterType = parameterType;
            this.value = value;
        }

        @Override
        public T getValue()
        {
            return this.value;
        }

        @Override
        public ParameterType<T> getParameterType()
        {
            return this.parameterType;
        }

        @Override
        public String toString()
        {
            return "FixedEntry [parameterType=" + this.parameterType + ", value=" + this.value + "]";
        }

    }

    /**
     * Distributed parameter.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <U> unit type
     * @param <T> value type
     */
    private final class DistributedEntry<U extends Unit<U>, T extends DoubleScalarRel<U, T>> implements ParameterEntry<T>
    {
        /** Parameter type. */
        private final ParameterType<T> parameterType;

        /** Distribution of the parameter. */
        private final ContinuousDistDoubleScalar.Rel<T, U> distribution;

        /**
         * @param parameterType the parameter type
         * @param distribution ContinuousDistDoubleScalar.Rel&lt;T,U&gt;; the distribution of the parameter
         */
        DistributedEntry(final ParameterType<T> parameterType, final ContinuousDistDoubleScalar.Rel<T, U> distribution)
        {
            this.parameterType = parameterType;
            this.distribution = distribution;
        }

        @Override
        public T getValue()
        {
            return this.distribution.get();
        }

        @Override
        public ParameterType<T> getParameterType()
        {
            return this.parameterType;
        }

        @Override
        public String toString()
        {
            return "DistributedEntry [parameterType=" + this.parameterType + ", distribution=" + this.distribution + "]";
        }
    }

    /**
     * Distributed double value.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private final class DistributedEntryDouble implements ParameterEntry<Double>
    {
        /** Parameter type. */
        private final ParameterType<Double> parameterType;

        /** Parameter distribution. */
        private final DistContinuous distribution;

        /**
         * @param parameterType the parameter type
         * @param distribution parameter distribution
         */
        DistributedEntryDouble(final ParameterType<Double> parameterType, final DistContinuous distribution)
        {
            this.parameterType = parameterType;
            this.distribution = distribution;
        }

        @Override
        public Double getValue()
        {
            return this.distribution.draw();
        }

        @Override
        public ParameterType<Double> getParameterType()
        {
            return this.parameterType;
        }

        @Override
        public String toString()
        {
            return "DistributedEntryDouble [parameterType=" + this.parameterType + ", distribution=" + this.distribution + "]";
        }
    }

    /**
     * Distributed integer value.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private final class DistributedEntryInteger implements ParameterEntry<Integer>
    {
        /** Parameter type. */
        private final ParameterType<Integer> parameterType;

        /** Parameter distribution. */
        private final DistDiscrete distribution;

        /**
         * @param parameterType the parameter type
         * @param distribution parameter distribution
         */
        DistributedEntryInteger(final ParameterType<Integer> parameterType, final DistDiscrete distribution)
        {
            this.parameterType = parameterType;
            this.distribution = distribution;
        }

        @Override
        public Integer getValue()
        {
            return (int) this.distribution.draw();
        }

        @Override
        public ParameterType<Integer> getParameterType()
        {
            return this.parameterType;
        }

        @Override
        public String toString()
        {
            return "DistributedEntryInteger [parameterType=" + this.parameterType + ", distribution=" + this.distribution + "]";
        }
    }

    /**
     * Correlates two parameter values.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <C> value type of independent parameter
     * @param <T> value type of dependent parameter
     */
    @FunctionalInterface
    public interface Correlation<C, T>
    {
        /**
         * Returns the correlated value.
         * @param first value of independent parameter
         * @param then pre-determined value, the correlation may be relative to a base value
         * @return correlated value
         */
        T correlate(C first, T then);
    }

}
