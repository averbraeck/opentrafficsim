package org.opentrafficsim.imb.demo.generators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.AbstractDoubleScalarRel;
import org.djunits.value.vdouble.scalar.DoubleScalarInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristicsFactory;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class BehavioralCharacteristicsFactoryByType implements BehavioralCharacteristicsFactory
{

    /** Parameters. */
    private final Map<GTUType, Set<ParameterEntry>> map = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public void setValues(BehavioralCharacteristics defaultCharacteristics, GTUType gtuType)
    {
        if (this.map.containsKey(gtuType))
        {
            for (ParameterEntry entry : this.map.get(gtuType))
            {
                entry.setValue(defaultCharacteristics);
            }
        }
    }

    /**
     * @param gtuType the gtu type
     * @param parameterType the parameter type
     * @param value the value of the parameter
     */
    public <T extends DoubleScalarInterface> void addParameter(final GTUType gtuType, final ParameterType<T> parameterType,
            final T value)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new FixedEntry<>(parameterType, value));
    }

    /**
     * @param gtuType the gtu type
     * @param parameterType the parameter type
     * @param mu mean
     * @param sig standard deviation
     */
    public <U extends Unit<U>, T extends AbstractDoubleScalarRel<U, T>> void addGaussianParameter(final GTUType gtuType,
            final ParameterType<T> parameterType, final T mu, final T sig)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new GaussianEntry<>(parameterType, mu, sig));
    }

    /**
     * @param gtuType the gtu type
     * @param parameterType the parameter type
     * @param mu mean
     * @param sig standard deviation
     */
    public void addGaussianParameter(final GTUType gtuType, final ParameterTypeDouble parameterType, final double mu,
            final double sig)
    {
        assureTypeInMap(gtuType);
        this.map.get(gtuType).add(new GaussianDoubleEntry(parameterType, mu, sig));
    }

    /**
     * Assures the gtu type is in the map.
     * @param gtuType the gtu type
     */
    private void assureTypeInMap(final GTUType gtuType)
    {
        if (!this.map.containsKey(gtuType))
        {
            this.map.put(gtuType, new HashSet<>());
        }
    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private interface ParameterEntry
    {
        /**
         * Set value for parameter.
         * @param behavioralCharacteristics the behavioral characteristics
         */
        void setValue(final BehavioralCharacteristics behavioralCharacteristics);
    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <U> unit
     * @param <T> value type
     */
    private static class GaussianEntry<U extends Unit<U>, T extends AbstractDoubleScalarRel<U, T>> implements ParameterEntry
    {

        /** Parameter type. */
        private final ParameterType<T> parameterType;

        /** Mean. */
        private final T mu;

        /** Standard deviation. */
        private final T sig;

        /**
         * @param parameterType the parameter type
         * @param mu mean
         * @param sig standard deviation
         */
        public GaussianEntry(final ParameterType<T> parameterType, final T mu, final T sig)
        {
            this.parameterType = parameterType;
            this.mu = mu;
            this.sig = sig;
        }

        /** {@inheritDoc} */
        @Override
        public void setValue(final BehavioralCharacteristics behavioralCharacteristics)
        {
            Random r = new Random();
            T val = this.mu.instantiateRel(this.mu.getSI() + r.nextGaussian() * this.sig.getSI(),
                    this.mu.getUnit().getStandardUnit());
            try
            {
                behavioralCharacteristics.setParameter(this.parameterType, val);
            }
            catch (ParameterException exception)
            {
                throw new RuntimeException(
                        "Trying to set value " + val + " for parameter " + this.parameterType + ", which is out of range.",
                        exception);
            }
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class GaussianDoubleEntry implements ParameterEntry
    {

        /** Parameter type. */
        private final ParameterTypeDouble parameterType;

        /** Mean. */
        private final double mu;

        /** Standard deviation. */
        private final double sig;

        /**
         * @param parameterType the parameter type
         * @param mu mean
         * @param sig standard deviation
         */
        public GaussianDoubleEntry(final ParameterTypeDouble parameterType, final double mu, final double sig)
        {
            this.parameterType = parameterType;
            this.mu = mu;
            this.sig = sig;
        }

        /** {@inheritDoc} */
        @Override
        public void setValue(final BehavioralCharacteristics behavioralCharacteristics)
        {
            Random r = new Random();
            double val = this.mu + r.nextGaussian() * this.sig;
            try
            {
                behavioralCharacteristics.setParameter(this.parameterType, val);
            }
            catch (ParameterException exception)
            {
                throw new RuntimeException(
                        "Trying to set value " + val + " for parameter " + this.parameterType + ", which is out of range.",
                        exception);
            }
        }

    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> value type
     */
    private final class FixedEntry<T extends DoubleScalarInterface> implements ParameterEntry
    {

        /** Parameter type. */
        private final ParameterType<T> parameterType;

        /** Value. */
        private final T value;

        /**
         * @param parameterType the parameter type
         * @param value the fixed value
         */
        public FixedEntry(final ParameterType<T> parameterType, final T value)
        {
            this.parameterType = parameterType;
            this.value = value;
        }

        /** {@inheritDoc} */
        @Override
        public void setValue(final BehavioralCharacteristics behavioralCharacteristics)
        {
            try
            {
                behavioralCharacteristics.setParameter(this.parameterType, this.value);
            }
            catch (ParameterException exception)
            {
                throw new RuntimeException("Trying to set value " + this.value + " for parameter " + this.parameterType
                        + ", which is out of range.", exception);
            }
        }

    }

}
