package org.opentrafficsim.core.dsol;

import javax.naming.Context;

import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSReplication extends Replication<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
{
    /**
     * @param context Context
     * @param experiment Experiment
     */
    public OTSReplication(final Context context,
        final Experiment<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> experiment)
    {
        super(context, experiment);
    }

    /**
     * Create a new OTSReplication.
     * @param id String; id of the new OTSReplication
     * @param startTime OTSSimTimeDouble; the start time of the new OTSReplication
     * @param warmupPeriod DoubleScalar.Rel&lt;TimeUnit&gt;; the warmup period of the new OTSReplication
     * @param runLength DoubleScalarRel&lt;TimeUnit&gt;; the run length of the new OTSReplication
     * @param model OTSModelInterface; the model
     */
    public OTSReplication(final String id, final OTSSimTimeDouble startTime, final DoubleScalar.Rel<TimeUnit> warmupPeriod,
        final DoubleScalar.Rel<TimeUnit> runLength, final OTSModelInterface model)
    {
        super(id, startTime, warmupPeriod, runLength, model);
    }

    /** */
    private static final long serialVersionUID = 20140815L;
}
