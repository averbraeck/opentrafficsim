package org.opentrafficsim.core.dsol;

import javax.naming.Context;

import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;

import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSReplication extends Replication<DoubleScalarAbs<TimeUnit>, DoubleScalarRel<TimeUnit>, OTSSimTimeDouble>
{
    /**
     * @param context
     * @param experiment
     */
    public OTSReplication(Context context,
            Experiment<DoubleScalarAbs<TimeUnit>, DoubleScalarRel<TimeUnit>, OTSSimTimeDouble> experiment)
    {
        super(context, experiment);
    }

    /**
     * @param id
     * @param startTime
     * @param warmupPeriod
     * @param runLength
     * @param model
     */
    public OTSReplication(String id, OTSSimTimeDouble startTime, DoubleScalarRel<TimeUnit> warmupPeriod,
            DoubleScalarRel<TimeUnit> runLength, OTSModelInterface model)
    {
        super(id, startTime, warmupPeriod, runLength, model);
    }

    /** */
    private static final long serialVersionUID = 20140815L;
}
