package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.AbstractDoubleScalarRel;
import org.djunits.value.vdouble.scalar.DoubleScalarInterface;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Sets parameter values based on the the GTU type. This includes stochastic parameters. Parameters may also be defined for all
 * GTU types. Similarly, correlations between two parameters can be determined, for all or a specific GTU type.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterFactoryByType implements ParameterFactory
{

    /** Parameters. */
    private final Map<GTUType, Set<ParameterEntry<?>>> map = new HashMap<>();

    /** Map of correlations. */
    private Map<GTUType, Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>>> correlations = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public void setValues(final Parameters parameters, final GTUType gtuType)
    {
        // set of parameters that this class is going to set
        Map<ParameterType<?>, ParameterEntry<?>> setType = new LinkedHashMap<>();
        for (GTUType type : new GTUType[] { null, gtuType })
        {
            if (this.map.containsKey(type))
            {
                for (ParameterEntry<?> entry : this.map.get(type))
                {
                    setType.put(entry.getParameterType(), entry);
                }
            }
        }

        /* {@formatter:off}
         * Based on all given correlations we create two maps: 
         * - remainingCorrelations, keys are ParameterTypes that depend on values that this class still needs to set 
         * - allCorrelations, correlations combined from all and the specific GTU type, used to actually alter the values set
         * The map remainingCorrelations will only contain correlations to parameters also defined in this class. For other 
         * correlations the independent parameter should already be present in the input Parameters set. If it's not, an 
         * exception follows as the parameter could not be retrieved. Circular dependencies are recognized as a loop has not 
         * done anything, while remainingCorrelations is not empty (i.e. some parameters should still be set, but can't as they 
         * all depend on parameters not yet set). {@formatter:on}
         */
        Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> remainingCorrelations = new LinkedHashMap<>();
        Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> allCorrelations = new LinkedHashMap<>();
        for (GTUType type : new GTUType[] { null, gtuType }) // null first, so specific type overwrites
        {
            if (this.correlations.containsKey(type))
            {
                Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> map1 = this.correlations.get(type);
                for (ParameterType<?> then : map1.keySet())
                {
                    Map<ParameterType<?>, Correlation<?, ?>> map2a = map1.get(then);
                    Map<ParameterType<?>, Correlation<?, ?>> map2b = new LinkedHashMap<>(map2a); // safe copy
                    // retain only independent correlation parameters this class will set
                    map2b.keySet().retainAll(setType.keySet());
                    if (!map2b.isEmpty())
                    {
                        Map<ParameterType<?>, Correlation<?, ?>> map3 = remainingCorrelations.get(then);
                        if (map3 == null)
                        {
                            map3 = new LinkedHashMap<>();
                            remainingCorrelations.put(then, map3);
                        }
                        map3.putAll(map2b);
                    }
                    if (!map2a.isEmpty())
                    {
                        Map<ParameterType<?>, Correlation<?, ?>> map3 = allCorrelations.get(then);
                        if (map3 == null)
                        {
                            map3 = new LinkedHashMap<>();
                            allCorrelations.put(then, map3);
                        }
                        map3.putAll(map2a);
                    }
                }
            }
        }

        // loop and set parameters that do not correlate to parameters not yet set
        boolean altered = true;
        while (altered)
        {
            altered = false;

            Iterator<ParameterType<?>> iterator = setType.keySet().iterator();
            while (iterator.hasNext())
            {
                ParameterType<?> parameterType = iterator.next();
                ParameterEntry<?> entry = setType.get(parameterType);

                if (!remainingCorrelations.containsKey(parameterType))
                {
                    altered = true;
                    iterator.remove();
                    Object value = entry.getValue();
                    setParameter(parameterType, value, parameters, allCorrelations.get(parameterType));
                    // remove the set parameter from correlations that need to be considered
                    Iterator<ParameterType<?>> it = remainingCorrelations.keySet().iterator();
                    while (it.hasNext())
                    {
                        Map<ParameterType<?>, Correlation<?, ?>> remMap = remainingCorrelations.get(it.next());
                        remMap.remove(parameterType);
                        if (remMap.isEmpty())
                        {
                            it.remove(); // all independent parameters were set, remove correlation to consider
                        }
                    }
                }
            }
        }
        if (!altered && !remainingCorrelations.isEmpty())
        {
            throw new RuntimeException("Circular correlation between parameters.");
        }

    }

    /**
     * Sets a parameter including type casting.
     * @param parameterType ParameterType&lt;?&gt;; parameter type
     * @param value Object; value
     * @param parameters Parameters; parameters to set in
     * @param correls Map&lt;ParameterType&lt;?&gt;, Correlation&lt;?, ?&gt;&gt;; correlations
     * @param <C> parameter value type of first parameter
     * @param <T> parameter value type of then parameter
     */
    @SuppressWarnings("unchecked")
    private <C, T> void setParameter(final ParameterType<?> parameterType, final Object value, final Parameters parameters,
            final Map<ParameterType<?>, Correlation<?, ?>> correls)
    {
        T val = (T) value;
        try
        {
            if (correls != null)
            {
                for (ParameterType<?> param : correls.keySet())
                {
                    Correlation<C, T> correlation = (Correlation<C, T>) correls.get(param);
                    if (param == null)
                    {
                        val = correlation.correlate(null, val);
                    }
                    else
                    {
                        val = correlation.correlate(parameters.getParameter((ParameterType<C>) param), val);
                    }
                }
            }
            parameters.setParameter((ParameterType<T>) parameterType, val);
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Value out of bounds or dependent parameter not present.", exception);
        }
    }

    /**
     * @param gtuType GTUType; the gtu type
     * @param parameterType ParameterType&lt;T&gt;; the parameter type
     * @param value T; the value of the parameter
     * @param <T> parameter value type
     */
    public <T extends DoubleScalarInterface> void addParameter(final GTUType gtuType, final ParameterType<T> parameterType,
            final T value)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new FixedEntry<>(parameterType, value));
    }

    /**
     * @param gtuType GTUType; the gtu type
     * @param parameterType ParameterTypeDouble; the parameter type
     * @param value double; the value of the parameter
     */
    public void addParameter(final GTUType gtuType, final ParameterTypeDouble parameterType, final double value)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new FixedEntryDouble(parameterType, value));
    }

    /**
     * @param gtuType GTUType; the gtu type
     * @param parameterType ParameterTypeNumeric&lt;T&gt;; the parameter type
     * @param distribution ContinuousDistDoubleScalar.Rel&lt;T,U&gt;; the distribution of the parameter
     * @param <U> unit type
     * @param <T> parameter value type
     */
    public <U extends Unit<U>, T extends AbstractDoubleScalarRel<U, T>> void addParameter(final GTUType gtuType,
            final ParameterTypeNumeric<T> parameterType, final ContinuousDistDoubleScalar.Rel<T, U> distribution)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new DistributedEntry<>(parameterType, distribution));
    }

    /**
     * @param gtuType GTUType; the gtu type
     * @param parameterType ParameterTypeDouble; the parameter type
     * @param distribution DistContinuous; the distribution of the parameter
     */
    public void addParameter(final GTUType gtuType, final ParameterTypeDouble parameterType, final DistContinuous distribution)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new DistributedEntryDouble(parameterType, distribution));
    }

    /**
     * Add parameter for all GTU types.
     * @param parameterType ParameterType&lt;T&gt;; the parameter type
     * @param value T; the value of the parameter
     * @param <T> type
     */
    public <T extends DoubleScalarInterface> void addParameter(final ParameterType<T> parameterType, final T value)
    {
        addParameter(null, parameterType, value);
    }

    /**
     * Add parameter for all GTU types.
     * @param parameterType ParameterTypeDouble; the parameter type
     * @param value double; the value of the parameter
     */
    public void addParameter(final ParameterTypeDouble parameterType, final double value)
    {
        addParameter(null, parameterType, value);
    }

    /**
     * Add parameter for all GTU types.
     * @param parameterType ParameterTypeNumeric&lt;T&gt;; the parameter type
     * @param distribution ContinuousDistDoubleScalar.Rel&lt;T,U&gt;; the distribution of the parameter
     * @param <U> unit type
     * @param <T> parameter value type
     */
    public <U extends Unit<U>, T extends AbstractDoubleScalarRel<U, T>> void addParameter(
            final ParameterTypeNumeric<T> parameterType, final ContinuousDistDoubleScalar.Rel<T, U> distribution)
    {
        addParameter(null, parameterType, distribution);
    }

    /**
     * Add parameter for all GTU types.
     * @param parameterType ParameterTypeDouble; the parameter type
     * @param distribution DistContinuous; the distribution of the parameter
     */
    public void addParameter(final ParameterTypeDouble parameterType, final DistContinuous distribution)
    {
        addParameter(null, parameterType, distribution);
    }

    /**
     * Correlates one parameter to another. The parameter 'first' may also be {@code null}, in which case the parameter can be
     * correlated to an external source.
     * @param gtuType GTUType; GTU type
     * @param first ParameterType&lt;C&gt;; independent parameter
     * @param then ParameterType&lt;T&gt;; dependent parameter
     * @param correlation Correlation&lt;C, T&gt;; correlation
     * @param <C> parameter value type of first parameter
     * @param <T> parameter value type of then parameter
     */
    public <C, T> void addCorrelation(final GTUType gtuType, final ParameterType<C> first, final ParameterType<T> then,
            final Correlation<C, T> correlation)
    {
        assureTypeInMap(gtuType);
        Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> map1 = this.correlations.get(gtuType);
        Map<ParameterType<?>, Correlation<?, ?>> map2 = map1.get(then);
        if (map2 == null)
        {
            map2 = new LinkedHashMap<>();
            map1.put(then, map2);
        }
        map2.put(first, correlation);
    }

    /**
     * Correlates one parameter to another for all GTU types.
     * @param first ParameterType&lt;C&gt;; independent parameter
     * @param then ParameterType&lt;T&gt;; dependent parameter
     * @param correlation Correlation&lt;C, T&gt;; correlation
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
     * @param gtuType GTUType; the gtu type
     */
    private void assureTypeInMap(final GTUType gtuType)
    {
        if (!this.map.containsKey(gtuType))
        {
            this.map.put(gtuType, new LinkedHashSet<>());
            this.correlations.put(gtuType, new LinkedHashMap<>());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ParameterFactoryByType [map=" + this.map + "]";
    }

    /**
     * Local storage interface for parameters.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> value type
     */
    private interface ParameterEntry<T>
    {
        /**
         * Returns the value for parameter.
         * @return T; value for the parameter
         */
        T getValue();

        /**
         * Returns the parameter type.
         * @return ParameterType; parameter type
         */
        ParameterType<T> getParameterType();
    }

    /**
     * Fixed parameter.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> value type
     */
    private final class FixedEntry<T> implements ParameterEntry<T>, Serializable
    {
        /** */
        private static final long serialVersionUID = 20170400L;

        /** Parameter type. */
        private final ParameterType<T> parameterType;

        /** Value. */
        private final T value;

        /**
         * @param parameterType ParameterType&lt;T&gt;; the parameter type
         * @param value T; the fixed value
         */
        FixedEntry(final ParameterType<T> parameterType, final T value)
        {
            this.parameterType = parameterType;
            this.value = value;
        }

        /** {@inheritDoc} */
        @Override
        public T getValue()
        {
            return this.value;
        }

        /** {@inheritDoc} */
        @Override
        public ParameterType<T> getParameterType()
        {
            return this.parameterType;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "FixedEntry [parameterType=" + this.parameterType + ", value=" + this.value + "]";
        }

    }

    /**
     * Fixed double value.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private final class FixedEntryDouble implements ParameterEntry<Double>, Serializable
    {
        /** */
        private static final long serialVersionUID = 20170400L;

        /** Parameter type. */
        private final ParameterTypeDouble parameterType;

        /** Value. */
        private final double value;

        /**
         * @param parameterType ParameterTypeDouble; the parameter type
         * @param value double; the fixed value
         */
        FixedEntryDouble(final ParameterTypeDouble parameterType, final double value)
        {
            this.parameterType = parameterType;
            this.value = value;
        }

        /** {@inheritDoc} */
        @Override
        public Double getValue()
        {
            return this.value;
        }

        /** {@inheritDoc} */
        @Override
        public ParameterType<Double> getParameterType()
        {
            return this.parameterType;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "FixedEntryDouble [parameterType=" + this.parameterType + ", value=" + this.value + "]";
        }
    }

    /**
     * Distributed parameter.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <U> unit type
     * @param <T> value type
     */
    private final class DistributedEntry<U extends Unit<U>, T extends AbstractDoubleScalarRel<U, T>>
            implements ParameterEntry<T>, Serializable
    {
        /** */
        private static final long serialVersionUID = 20180203L;

        /** Parameter type. */
        private final ParameterType<T> parameterType;

        /** Distribution of the parameter. */
        private final ContinuousDistDoubleScalar.Rel<T, U> distribution;

        /**
         * @param parameterType ParameterType&lt;T&gt;; the parameter type
         * @param distribution ContinuousDistDoubleScalar.Rel&lt;T,U&gt;; the distribution of the parameter
         */
        DistributedEntry(final ParameterType<T> parameterType, final ContinuousDistDoubleScalar.Rel<T, U> distribution)
        {
            this.parameterType = parameterType;
            this.distribution = distribution;
        }

        /** {@inheritDoc} */
        @Override
        public T getValue()
        {
            return this.distribution.draw();
        }

        /** {@inheritDoc} */
        @Override
        public ParameterType<T> getParameterType()
        {
            return this.parameterType;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "DistributedEntry [parameterType=" + this.parameterType + ", distribution=" + this.distribution + "]";
        }
    }

    /**
     * Distributed double value.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private final class DistributedEntryDouble implements ParameterEntry<Double>, Serializable
    {
        /** */
        private static final long serialVersionUID = 20180203L;

        /** Parameter type. */
        private final ParameterTypeDouble parameterType;

        /** Parameter distribution. */
        private final DistContinuous distribution;

        /**
         * @param parameterType ParameterTypeDouble; the parameter type
         * @param distribution DistContinuous; parameter distribution
         */
        DistributedEntryDouble(final ParameterTypeDouble parameterType, final DistContinuous distribution)
        {
            this.parameterType = parameterType;
            this.distribution = distribution;
        }

        /** {@inheritDoc} */
        @Override
        public Double getValue()
        {
            return this.distribution.draw();
        }

        /** {@inheritDoc} */
        @Override
        public ParameterType<Double> getParameterType()
        {
            return this.parameterType;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "DistributedEntryDouble [parameterType=" + this.parameterType + ", distribution=" + this.distribution + "]";
        }
    }

    /**
     * Correlates two parameter values.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mrt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <C> value type of independent parameter
     * @param <T> value type of dependent parameter
     */
    @FunctionalInterface
    public interface Correlation<C, T>
    {
        /**
         * Returns the correlated value.
         * @param first C; value of independent parameter
         * @param then T; pre-determined value
         * @return correlated value
         */
        T correlate(C first, T then);
    }

}
