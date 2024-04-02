package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;

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
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> IDM class
 */
public class AbstractIdmFactory<T extends AbstractIdm> implements CarFollowingModelFactory<T>
{

    /** Single instance as it is state-less. */
    private final T idm;

    /** Distribution for fSpeed parameter. */
    private final DistContinuous fSpeed;

    /**
     * Sets the idm model, which should be state-less.
     * @param idm T; idm model, which should be state-less
     * @param randomStream StreamInterface; random number stream
     */
    public AbstractIdmFactory(final T idm, final StreamInterface randomStream)
    {
        this.idm = idm;
        this.fSpeed = new DistNormal(randomStream, 123.7 / 120.0, 0.1);
    }

    /** {@inheritDoc} */
    @Override
    public final T generateCarFollowingModel()
    {
        return this.idm;
    }

    /** {@inheritDoc} */
    @Override
    public Parameters getParameters() throws ParameterException
    {
        ParameterSet parameters = new ParameterSet();
        parameters.setDefaultParameters(AbstractIdm.class);
        parameters.setParameter(AbstractIdm.FSPEED, this.fSpeed.draw());
        return parameters;
    }

}
