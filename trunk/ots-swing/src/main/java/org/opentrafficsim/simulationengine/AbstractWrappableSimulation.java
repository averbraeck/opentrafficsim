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
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-09-19 13:55:45 +0200 (Wed, 19 Sep 2018) $, @version $Revision: 4006 $, by $Author: averbraeck $,
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
     * @param otsModel OTSModelInterface; the simulation model
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface<OTSSimulatorInterface> otsModel)
            throws SimRuntimeException, NamingException, PropertyException
    {
        SimpleSimulator simulator = (SimpleSimulator) otsModel.getSimulator();
        simulator.initialize(startTime, warmupPeriod, runLength, otsModel);
    }

    /**
     * Build the simulator with the specified replication number.
     * @param startTime Time; the start time
     * @param warmupPeriod Duration; the warm up period
     * @param runLength Duration; the duration of the simulation / animation
     * @param otsModel OTSModelInterface; the simulation model
     * @param replicationNumber int; the replication number
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface<OTSSimulatorInterface> otsModel, final int replicationNumber)
            throws SimRuntimeException, NamingException, PropertyException
    {
        SimpleSimulator simulator = (SimpleSimulator) otsModel.getSimulator();
        simulator.initialize(startTime, warmupPeriod, runLength, otsModel, replicationNumber);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SimpleSimulator buildSimulator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final ArrayList<Property<?>> userModifiedProperties)
            throws SimRuntimeException, NamingException, OTSSimulationException, PropertyException
    {
        OTSModelInterface<OTSSimulatorInterface> model = makeModel();
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
    protected abstract OTSModelInterface<OTSSimulatorInterface> makeModel() throws OTSSimulationException;

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
