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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> IDM class
 */
public class AbstractIDMFactory<T extends AbstractIDM> implements CarFollowingModelFactory<T>
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
    public AbstractIDMFactory(final T idm, final StreamInterface randomStream)
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
        parameters.setDefaultParameters(AbstractIDM.class);
        parameters.setParameter(AbstractIDM.FSPEED, this.fSpeed.draw());
        return parameters;
    }

}
