package org.opentrafficsim.core.parameters;

import java.util.Map;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeNumeric;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar.Rel;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Extends {@link ParameterFactoryByType} with a one-shot mode. By calling {@link #setOneShotMode()} all parameters and
 * correlations added between a call to said method and to {@link #setValues(Parameters, GtuType) setValues()} are removed from
 * the factory after the call to {@link #setValues(Parameters, GtuType) setValues()}.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterFactoryOneShot extends ParameterFactoryByType
{

    /** Whether the parameter factory is in one-shot mode. */
    private boolean oneShot = false;

    /** Temporary parameters and correlations for the next one-shot. */
    // The tempParameters is of the same class such that its getBaseValues() and getCorrelations() methods can be accessed. */
    private ParameterFactoryOneShot tempParameters;

    /**
     * Constructor.
     */
    public ParameterFactoryOneShot()
    {
        this(true);
    }

    /**
     * Constructor.
     * @param initTempParameters initiates temporary parameters
     */
    private ParameterFactoryOneShot(final boolean initTempParameters)
    {
        if (initTempParameters)
        {
            this.tempParameters = new ParameterFactoryOneShot(false);
        }
    }

    /**
     * Sets the factory to one-shot mode. All parameters and correlations added between a call to this method and to
     * {@link #setValues(Parameters, GtuType) setValues()} are removed from the factory after the call to
     * {@link #setValues(Parameters, GtuType) setValues()}.
     */
    public void setOneShotMode()
    {
        this.oneShot = true;
    }

    /**
     * {@inheritDoc} Removes any parameters or correlations that were set in one-shot mode. One-shot mode is disabled.
     */
    @Override
    public void setValues(final Parameters parameters, final GtuType gtuType) throws ParameterException
    {
        Map<ParameterType<?>, Object> values = getBaseValues(gtuType);
        values.putAll(this.tempParameters.getBaseValues(gtuType));
        Map<ParameterType<?>, Map<ParameterType<?>, Correlation<?, ?>>> correls = getCorrelations(gtuType);
        correls.putAll(this.tempParameters.getCorrelations(gtuType));
        applyCorrelationsAndSetValues(parameters, values, correls);
        this.tempParameters = new ParameterFactoryOneShot(false);
        this.oneShot = false;
    }

    /**
     * Executes either the fixed or temporary parameter/correlation setting depending on normal or one-shot mode.
     * @param fixed execution to set fixed parameter value
     * @param temporary execution to set temporary parameter value
     */
    private void fixedOrTemporary(final Runnable fixed, final Runnable temporary)
    {
        (this.oneShot ? temporary : fixed).run();
    }

    @Override
    public <V> void addParameter(final GtuType gtuType, final ParameterType<V> parameterType, final V value)
    {
        fixedOrTemporary(() -> super.addParameter(gtuType, parameterType, value),
                () -> this.tempParameters.addParameter(gtuType, parameterType, value));
    }

    @Override
    public <U extends Unit<U>, V extends DoubleScalarRel<U, V>> void addParameter(final GtuType gtuType,
            final ParameterTypeNumeric<V> parameterType, final Rel<V, U> distribution)
    {
        fixedOrTemporary(() -> super.addParameter(gtuType, parameterType, distribution),
                () -> this.tempParameters.addParameter(gtuType, parameterType, distribution));
    }

    @Override
    public void addParameter(final GtuType gtuType, final ParameterType<Integer> parameterType, final DistDiscrete distribution)
    {
        fixedOrTemporary(() -> super.addParameter(gtuType, parameterType, distribution),
                () -> this.tempParameters.addParameter(gtuType, parameterType, distribution));
    }

    @Override
    public void addParameter(final GtuType gtuType, final ParameterType<Double> parameterType,
            final DistContinuous distribution)
    {
        fixedOrTemporary(() -> super.addParameter(gtuType, parameterType, distribution),
                () -> this.tempParameters.addParameter(gtuType, parameterType, distribution));
    }

    @Override
    public <V> void addParameter(final ParameterType<V> parameterType, final V value)
    {
        fixedOrTemporary(() -> super.addParameter(parameterType, value),
                () -> this.tempParameters.addParameter(parameterType, value));
    }

    @Override
    public void addParameter(final ParameterTypeDouble parameterType, final double value)
    {
        fixedOrTemporary(() -> super.addParameter(parameterType, value),
                () -> this.tempParameters.addParameter(parameterType, value));
    }

    @Override
    public <U extends Unit<U>, V extends DoubleScalarRel<U, V>> void addParameter(final ParameterTypeNumeric<V> parameterType,
            final Rel<V, U> distribution)
    {
        fixedOrTemporary(() -> super.addParameter(parameterType, distribution),
                () -> this.tempParameters.addParameter(parameterType, distribution));
    }

    @Override
    public void addParameter(final ParameterTypeDouble parameterType, final DistContinuous distribution)
    {
        fixedOrTemporary(() -> super.addParameter(parameterType, distribution),
                () -> this.tempParameters.addParameter(parameterType, distribution));
    }

    @Override
    public <C, V> void addCorrelation(final GtuType gtuType, final ParameterType<C> first, final ParameterType<V> then,
            final Correlation<C, V> correlation)
    {
        fixedOrTemporary(() -> super.addCorrelation(gtuType, first, then, correlation),
                () -> this.tempParameters.addCorrelation(gtuType, first, then, correlation));
    }

    @Override
    public <C, V> void addCorrelation(final ParameterType<C> first, final ParameterType<V> then,
            final Correlation<C, V> correlation)
    {
        fixedOrTemporary(() -> super.addCorrelation(first, then, correlation),
                () -> this.tempParameters.addCorrelation(first, then, correlation));
    }

}
