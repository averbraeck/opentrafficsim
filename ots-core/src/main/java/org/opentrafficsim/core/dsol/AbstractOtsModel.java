package org.opentrafficsim.core.dsol;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.AbstractDsolModel;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

/**
 * AbstractOtsModel is the base class for a model that runs on an OtsSimulatorInterface.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public abstract class AbstractOtsModel extends AbstractDsolModel<Duration, OtsSimulatorInterface> implements OtsModelInterface
{
    /** */
    private static final long serialVersionUID = 1L;

    /** A very short description of the simulation. */
    private String shortName;

    /** A description of the simulation (HTML formatted). */
    private String description;

    /**
     * Instantiate an abstract OtsModel. The name and description will be set as the class name. Streams will be default.
     * @param simulator the simulator to use
     */
    public AbstractOtsModel(final OtsSimulatorInterface simulator)
    {
        this(simulator, "", "", defaultInitialStreams());
        this.shortName = getClass().getSimpleName();
        this.description = getClass().getSimpleName();
    }

    /**
     * Instantiate an abstract OtsModel with an initial set of streams (e.g., with seed management).
     * @param simulator the simulator to use
     * @param shortName a very short description of the simulation
     * @param description a description of the simulation (HTML formatted)
     * @param streamInformation the initial set of streams (e.g., with seed management)
     */
    public AbstractOtsModel(final OtsSimulatorInterface simulator, final String shortName, final String description,
            final StreamInformation streamInformation)
    {
        super(simulator, streamInformation);
        Throw.whenNull(shortName, "shortname cannot be null");
        Throw.whenNull(description, "description cannot be null");
        this.shortName = shortName;
        this.description = description;
    }

    /**
     * Create the default initial streams.
     * @return the default initial streams
     */
    public static StreamInformation defaultInitialStreams()
    {
        StreamInformation streamInformation = new StreamInformation();
        streamInformation.addStream("default", new MersenneTwister(10L));
        streamInformation.addStream("generation", new MersenneTwister(11L));
        return streamInformation;
    }

    @Override
    public final String getShortName()
    {
        return this.shortName;
    }

    /**
     * @param shortName set shortName
     */
    public final void setShortName(final String shortName)
    {
        this.shortName = shortName;
    }

    @Override
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * @param description set description
     */
    public final void setDescription(final String description)
    {
        this.description = description;
    }

}
