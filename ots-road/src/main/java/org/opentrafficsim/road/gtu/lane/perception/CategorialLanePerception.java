package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class CategorialLanePerception extends AbstractLanePerception
{

    /** */
    private static final long serialVersionUID = 20160729L;

    /**
     * @param gtu GTU
     */
    public CategorialLanePerception(final LaneBasedGTU gtu)
    {
        super(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public void perceive() throws GTUException, NetworkException, ParameterException
    {
        // TODO what?
    }

}
