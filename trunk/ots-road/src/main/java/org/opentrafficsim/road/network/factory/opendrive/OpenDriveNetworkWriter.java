package org.opentrafficsim.road.network.factory.opendrive;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.opentrafficsim.road.network.lane.LaneType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck
 * $, initial version Jul 23, 2015 <br>
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
