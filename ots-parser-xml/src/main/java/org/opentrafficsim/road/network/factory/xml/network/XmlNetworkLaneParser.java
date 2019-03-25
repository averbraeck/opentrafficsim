package org.opentrafficsim.road.network.factory.xml.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.xml.generated.ANIMATION;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.MODEL;
import org.opentrafficsim.xml.generated.NETWORK;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.OTS;
import org.opentrafficsim.xml.generated.ROADLAYOUT;
import org.opentrafficsim.xml.generated.RUN;
import org.opentrafficsim.xml.generated.SCENARIO;
import org.pmw.tinylog.Level;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import nl.tudelft.simulation.dsol.SimRuntimeException;

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
public final class XmlNetworkLaneParser implements Serializable
{
    /** */
    private static final long serialVersionUID = 2019022L;

    /** */
    private XmlNetworkLaneParser()
    {
        // utility class
    }
    
    /**
     * Parse the XML file and build the network.
     * @param filename String; the name of the file to parse
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the network that contains the parsed objects
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GTUException when construction of the Strategical Planner failed
     */
    public static OTSRoadNetwork build(final String filename, final OTSRoadNetwork otsNetwork,
            final OTSSimulatorInterface simulator) throws JAXBException, URISyntaxException, NetworkException,
            OTSGeometryException, XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GTUException
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
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the network that contains the parsed objects
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GTUException when construction of the Strategical Planner failed
     */
    public static OTSRoadNetwork build(final InputStream xmlStream, final OTSRoadNetwork otsNetwork,
            final OTSSimulatorInterface simulator) throws JAXBException, URISyntaxException, NetworkException,
            OTSGeometryException, XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GTUException
    {
        JAXBContext jc = JAXBContext.newInstance(OTS.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        SAXSource saxSource = new SAXSource(xmlReader, new InputSource(xmlStream));
        OTS ots = (OTS) unmarshaller.unmarshal(saxSource);

        CategoryLogger.setLogCategories(Cat.PARSER);
        CategoryLogger.setAllLogLevel(Level.TRACE);

        Map<String, ROADLAYOUT> roadLayoutMap = new HashMap<>();
        DefinitionsParser.parseDefinitions(ots.getDEFINITIONS(), otsNetwork, true, roadLayoutMap);

        NETWORK network = ots.getNETWORK();
        NetworkParser.parseNodes(otsNetwork, network);
        Map<String, Direction> nodeDirections = NetworkParser.calculateNodeAngles(otsNetwork, network);
        NetworkParser.parseLinks(otsNetwork, network, nodeDirections, simulator);
        NetworkParser.applyRoadLayout(otsNetwork, network, simulator, roadLayoutMap);
        
        List<NETWORKDEMAND> demands = ots.getNETWORKDEMAND();
        List<CONTROL> controls = ots.getCONTROL();
        MODEL modelParameters = ots.getMODEL();
        SCENARIO scenario = ots.getSCENARIO();
        RUN run = ots.getRUN();
        ANIMATION animation = ots.getANIMATION();

        ControlParser.parseControl(otsNetwork, simulator, ots);

        return otsNetwork;
    }

    /**
     * @param args String[]; not used
     * @throws Exception on parsing error
     */
    public static void main(final String[] args) throws Exception
    {
        OTSSimulatorInterface simulator = new OTSSimulator();
        build("/example.xml", new OTSRoadNetwork("", true), simulator);
        System.exit(0);
    }
}
