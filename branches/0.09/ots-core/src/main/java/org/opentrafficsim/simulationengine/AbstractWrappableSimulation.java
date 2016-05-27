package org.opentrafficsim.simulationengine;

import java.io.Serializable;
import java.util.ArrayList;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** {@inheritDoc} */
    @Override
    public final SimpleSimulator buildSimulator(final Time startTime, final Duration warmupPeriod,
        final Duration runLength, final ArrayList<AbstractProperty<?>> userModifiedProperties)
        throws SimRuntimeException, NamingException, OTSSimulationException
    {
        OTSModelInterface model = makeModel();
        final SimpleSimulator simulator = new SimpleSimulator(startTime, warmupPeriod, runLength, model);
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

}
