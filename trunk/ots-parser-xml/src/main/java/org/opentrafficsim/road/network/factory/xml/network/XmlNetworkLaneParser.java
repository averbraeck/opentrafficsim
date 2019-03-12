package org.opentrafficsim.road.network.factory.xml.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.ANIMATION;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.DEFINITIONS;
import org.opentrafficsim.xml.generated.MODEL;
import org.opentrafficsim.xml.generated.NETWORK;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.OTS;
import org.opentrafficsim.xml.generated.RUN;
import org.opentrafficsim.xml.generated.SCENARIO;

/**
 * Parse an XML file for an OTS network, based on the ots-network.xsd definition.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class XmlNetworkLaneParser implements Serializable
{
    /** */
    private static final long serialVersionUID = 2019022L;

    /**
     * Parse the XML file and build the network.
     * @param filename the name of the file to parse
     * @param otsNetwork the network to insert the parsed objects in
     * @param simulator the simulator
     * @return the network that contains the parsed objects
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    public static OTSNetwork build(final String filename, final OTSNetwork otsNetwork, final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, XmlParserException
    {
        File xml = new File(URLResource.getResource(filename).toURI().getPath());
        try
        {
            build(new FileInputStream(xml), otsNetwork, simulator);
        }
        catch (FileNotFoundException exception)
        {
            throw new XmlParserException("File could not be found.", exception);
        }

        return otsNetwork;
    }
    
    /**
     * Parse the XML file and build the network.
     * @param xmlStream InputStream; the xml input stream
     * @param otsNetwork the network to insert the parsed objects in
     * @param simulator the simulator
     * @return the network that contains the parsed objects
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    public static OTSNetwork build(final InputStream xmlStream, final OTSNetwork otsNetwork, final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, XmlParserException
    {
        JAXBContext jc = JAXBContext.newInstance(OTS.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        OTS ots = (OTS) unmarshaller.unmarshal(xmlStream);
        
        DEFINITIONS definitions = ots.getDEFINITIONS();
        NETWORK network = ots.getNETWORK();
        List<NETWORKDEMAND> demands = ots.getNETWORKDEMAND();
        List<CONTROL> controls = ots.getCONTROL();
        MODEL modelParameters = ots.getMODEL();
        SCENARIO scenario = ots.getSCENARIO();
        RUN run = ots.getRUN();
        ANIMATION animation = ots.getANIMATION();

        NodeParser.parseNodes(otsNetwork, network);
        Map<String, Direction> nodeDirections = NodeParser.calculateNodeAngles(otsNetwork, network);
        LinkParser.parseLinks(otsNetwork, network, nodeDirections, simulator);
        LinkParser.applyRoadTypes(otsNetwork, network, simulator);

        ControlParser.parseControl(otsNetwork, simulator, network);

        return otsNetwork;
    }

    /**
     * @param args not used
     * @throws Exception on parsing error
     */
    public static void main(final String[] args) throws Exception
    {
        OTSSimulatorInterface simulator = new OTSSimulator();
        build("/N201v8.xml", new OTSNetwork(""), simulator);
        System.exit(0);
    }
}
