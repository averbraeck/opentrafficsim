package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class LaneTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** linkNo, cannot be null in implementation of Lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String linkNo = "";

    /** laneNo, cannot be null in implementation of Lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String laneNo = "";

    /** width, cannot be null in implementation of lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String width = "";

    /** Class name of the TrafficLight. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String className = null;

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneTag [laneName=" + this.laneNo + ", linkNo=" + this.linkNo + ", className=" + this.className + "]";
    }

}
