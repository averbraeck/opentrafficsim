package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.function.Supplier;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Factory for IDM types.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> IDM class
 */
public class AbstractIdmFactory<T extends AbstractIdm> implements CarFollowingModelFactory<T>
{

    /** Single instance as it is state-less. */
    private final Supplier<? extends T> idm;

    /** Distribution for fSpeed parameter. */
    private final DistContinuous fSpeed;

    /**
     * Sets the idm model.
     * @param idm idm model supplier
     * @param randomStream random number stream
     */
    public AbstractIdmFactory(final Supplier<? extends T> idm, final StreamInterface randomStream)
    {
        this.idm = idm;
        this.fSpeed = new DistNormal(randomStream, 123.7 / 120.0, 0.1);
    }

    @Override
    public final T get()
    {
        return this.idm.get();
    }

    @Override
    public Parameters getParameters(final GtuType gtuType) throws ParameterException
    {
        ParameterSet parameters = new ParameterSet();
        parameters.setDefaultParameters(AbstractIdm.class);
        parameters.setParameter(AbstractIdm.FSPEED, this.fSpeed.draw());
        return parameters;
    }

}
