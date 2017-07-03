package org.opentrafficsim.road.gtu.generator;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class SpeedGenerator implements Generator<Speed>
{

    /** Min speed. */
    private final Speed minSpeed;

    /** Difference between max and min speed. */
    private final Speed deltaSpeed;

    /** Random stream. */
    private final StreamInterface stream;

    /**
     * @param minSpeed min speed
     * @param maxSpeed max speed
     * @param stream random stream
     */
    public SpeedGenerator(final Speed minSpeed, final Speed maxSpeed, final StreamInterface stream)
    {
        this.minSpeed = minSpeed;
        this.deltaSpeed = maxSpeed.minus(minSpeed);
        this.stream = stream;
    }

    /** {@inheritDoc} */
    @Override
    public Speed draw() throws ProbabilityException, ParameterException
    {
        return this.minSpeed.plus(this.deltaSpeed.multiplyBy(this.stream.nextDouble()));
    }

}
