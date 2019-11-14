package org.opentrafficsim.core.dsol;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;

import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.Treatment;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSReplication extends Replication.TimeDoubleUnit<OTSSimulatorInterface>
{

    /** History manager. */
    private HistoryManager historyManager;

    /**
     * @param id String; the id of the Replication.
     * @param experiment Experiment.TimeDoubleUnit&lt;OTSSimulatorInterface&gt;; Experiment
     * @throws NamingException when the context for the replication cannot be created
     */
    public OTSReplication(final String id, final Experiment.TimeDoubleUnit<OTSSimulatorInterface> experiment)
            throws NamingException
    {
        super(id, experiment);
    }

    /**
     * Create a new OTSReplication.
     * @param id String; id of the new OTSReplication
     * @param startTime Time; the start time of the new OTSReplication
     * @param warmupPeriod Duration; the warmup period of the new OTSReplication
     * @param runLength Duration; the run length of the new OTSReplication
     * @param model OTSModelInterface; the model
     * @return the created replication
     * @throws NamingException when the context for the replication cannot be created
     */
    public static OTSReplication create(final String id, final Time startTime, final Duration warmupPeriod,
            final Duration runLength, final OTSModelInterface model) throws NamingException
    {
        Experiment.TimeDoubleUnit<OTSSimulatorInterface> experiment = new Experiment.TimeDoubleUnit<>();
        experiment.setModel(model);
        Treatment.TimeDoubleUnit treatment =
                new Treatment.TimeDoubleUnit(experiment, "Treatment for " + id, startTime, warmupPeriod, runLength);
        experiment.setTreatment(treatment);
        return new OTSReplication(id, experiment);

    }

    /**
     * Returns the history manager. If none was set, one is created coupled to the simulator using 0s of history and 10s
     * clean-up time.
     * @param simulator OTSSimulatorInterface; simulator
     * @return HistoryManager; history manager
     */
    public HistoryManager getHistoryManager(final OTSSimulatorInterface simulator)
    {
        if (this.historyManager == null)
        {
            this.historyManager = new HistoryManagerDEVS(simulator, Duration.ZERO, Duration.instantiateSI(10.0));
        }
        return this.historyManager;
    }

    /**
     * Set history manager.
     * @param historyManager HistoryManager; history manager to set
     */
    public void setHistoryManager(final HistoryManager historyManager)
    {
        this.historyManager = historyManager;
    }

    /** */
    private static final long serialVersionUID = 20140815L;

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSReplication []";
    }
}
