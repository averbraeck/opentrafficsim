package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.Mental;

/**
 * Standard perception completely based on connected perception categories.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CategoricalLanePerception extends AbstractLanePerception
{

    /** */
    private static final long serialVersionUID = 20160729L;

    /**
     * @param gtu LaneBasedGTU; GTU
     */
    public CategoricalLanePerception(final LaneBasedGTU gtu)
    {
        super(gtu);
    }

    /**
     * @param gtu LaneBasedGTU; GTU
     * @param mental Mental; Mental
     */
    public CategoricalLanePerception(final LaneBasedGTU gtu, final Mental mental)
    {
        super(gtu, mental);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return super.toString();
    }

}
