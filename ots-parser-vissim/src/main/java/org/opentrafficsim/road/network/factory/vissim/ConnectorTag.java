package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class ConnectorTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** fromLinkNo, cannot be null in implementation of signal head. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String fromLinkNo = "";

    /** fromLaneNo, cannot be null in implementation of signal head. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String fromLaneNo = "";

    /**
     * Position of the signalHead on the link, relative to the design line, stored as a string to parse when the length is
     * known.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String fromPositionStr = "";

    /** fromLinkNo, cannot be null in implementation of signal head. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String toLinkNo = "";

    /** fromLaneNo, cannot be null in implementation of signal head. */
    @SuppressWarnings("checkstyle:visibilitymodifier")

    String toLaneNo = "";

    String toNodeName = "";

    String fromNodeName = "";

    /**
     * Position of the signalHead on the link, relative to the design line, stored as a string to parse when the length is
     * known.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String toPositionStr = "";

    /** Class name of the TrafficLight. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String className = "";

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrafficLightTag [fromName=" + this.fromLinkNo + ", positionStr=" + this.toPositionStr + ", className="
                + this.className + "]";
    }

}
