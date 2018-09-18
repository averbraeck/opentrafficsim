package org.opentrafficsim.simulationengine;

import java.io.Serializable;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.AbstractProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSModelInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jun 18, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractWrappableSimulation implements WrappableSimulation, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The properties exhibited by this simulation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /** Override the replication number by this value if non-null. */
    private Integer replication = null;

    /**
     * Build the simulator.
     * @param startTime Time; the start time
     * @param warmupPeriod Duration; the warm up period
     * @param runLength Duration; the duration of the simulation / animation
     * @param model OTSModelInterface; the simulation model
     * @return SimpleSimulator; the newly constructed simulator
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected SimpleSimulator buildSimpleSimulator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException, PropertyException
    {
        return new SimpleSimulator(startTime, warmupPeriod, runLength, model);
    }

    /**
     * Build the simulator with the specified replication number.
     * @param startTime Time; the start time
     * @param warmupPeriod Duration; the warm up period
     * @param runLength Duration; the duration of the simulation / animation
     * @param otsModel OTSModelInterface; the simulation model
     * @param replicationNumber int; the replication number
     * @return SimpleAnimator; a newly constructed animator
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected SimpleSimulator buildSimpleSimulator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface otsModel, final int replicationNumber)
            throws SimRuntimeException, NamingException, PropertyException
    {
        return new SimpleSimulator(startTime, warmupPeriod, runLength, otsModel, replicationNumber);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SimpleSimulator buildSimulator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final ArrayList<Property<?>> userModifiedProperties)
            throws SimRuntimeException, NamingException, OTSSimulationException, PropertyException
    {
        OTSModelInterface model = makeModel();
        if (null == model)
        {
            return null; // Happens when the user cancels the file open dialog in the OpenStreetMap demo.
        }

        final SimpleSimulator simulator =
                null == this.replication ? buildSimpleSimulator(startTime, warmupPeriod, runLength, model)
                        : buildSimpleSimulator(startTime, warmupPeriod, runLength, model, this.replication);

        // final SimpleSimulator simulator = buildSimpleSimulator(startTime, warmupPeriod, runLength, model);
        return simulator;
    }

    /**
     * @return the model.
     * @throws OTSSimulationException when the construction of the model fails
     */
    protected abstract OTSModelInterface makeModel() throws OTSSimulationException;

    /** {@inheritDoc} */
    @Override
    public final ArrayList<AbstractProperty<?>> getProperties()
    {
        return new ArrayList<AbstractProperty<?>>(this.properties);
    }

    /** {@inheritDoc} */
    @Override
    public final void setNextReplication(final Integer nextReplication)
    {
        this.replication = nextReplication;
    }

}
