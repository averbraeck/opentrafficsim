package org.opentrafficsim.road.network.factory.opendrive;

import org.opentrafficsim.core.network.OTSNetwork;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OpenDriveNetworkWriter
{
    /** OTS network */
    @SuppressWarnings("visibilitymodifier")
    protected OTSNetwork network = null;

    /** */
    private String outPut = null;

    /**
     * @param network
     */
    public OpenDriveNetworkWriter(OTSNetwork network)
    {
        this.network = network;
    }

    /**
     * @return output string
     */
    @SuppressWarnings("checkstyle:needbraces")
    public final String write()
    {
        writeHeader();
        writeRoads();
        writeJunctions();

        return this.outPut;
    }

    /**
     * 
     */
    private void writeHeader()
    {
    }

    /**
     * 
     */
    private void writeRoads()
    {

    }

    /**
     * 
     */
    private void writeJunctions()
    {
    }
}
