package org.opentrafficsim.core.dsol;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSReplication extends Replication<Time, Duration, OTSSimTimeDouble>
{
    /**
     * @param experiment Experiment
     * @throws NamingException when the context for the replication cannot be created
     */
    public OTSReplication(final Experiment<Time, Duration, OTSSimTimeDouble> experiment) throws NamingException
    {
        super(experiment);
    }

    /**
     * Create a new OTSReplication.
     * @param id String; id of the new OTSReplication
     * @param startTime OTSSimTimeDouble; the start time of the new OTSReplication
     * @param warmupPeriod Duration; the warmup period of the new OTSReplication
     * @param runLength DoubleScalarRel&lt;TimeUnit&gt;; the run length of the new OTSReplication
     * @param model OTSModelInterface; the model
     * @throws NamingException when the context for the replication cannot be created
     */
    public OTSReplication(final String id, final OTSSimTimeDouble startTime, final Duration warmupPeriod,
            final Duration runLength, final OTSModelInterface model) throws NamingException
    {
        super(id, startTime, warmupPeriod, runLength, model);
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
