package org.opentrafficsim.road.network.factory.opendrive.parser;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.opendrive.xml.generated.OpenDRIVE;
import org.opentrafficsim.opendrive.xml.generated.OpenDRIVE.Header;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.opendrive.OpenDriveParserException;
import org.pmw.tinylog.Level;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Experiment;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class OpenDriveNetworkLaneParser implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** */
    private OpenDriveNetworkLaneParser()
    {
        // utility class
    }

    /**
     * Parse the OpenDRIVE XML file and build the network.
     * @param filename String; the name of the file to parse
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the network that contains the parsed objects
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws OpenDriveParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GTUException when construction of the Strategical Planner failed
     */
    public static OTSRoadNetwork build(final String filename, final OTSRoadNetwork otsNetwork,
            final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, OpenDriveParserException,
            SAXException, ParserConfigurationException, SimRuntimeException, GTUException
    {
        URL xmlURL = URLResource.getResource(filename);
        build(xmlURL, otsNetwork, simulator);
        return otsNetwork;
    }

    /**
     * Parse the OpenDRIVE XML input stream and build the network.
     * @param xmlStream InputStream; the xml input stream
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws OpenDriveParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GTUException when construction of the Strategical Planner failed
     */
    public static Experiment.TimeDoubleUnit<OTSSimulatorInterface> build(final InputStream xmlStream,
            final OTSRoadNetwork otsNetwork, final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, OpenDriveParserException,
            SAXException, ParserConfigurationException, SimRuntimeException, GTUException
    {
        return build(parseXML(xmlStream), otsNetwork, simulator);
    }

    /**
     * Parse an OpenDRIVE XML input stream and build an OTS object.
     * @param xmlURL URL; the URL for the xml file or stream
     * @return OpenDRIVE; the constructed OpenDRIVE object
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    public static OpenDRIVE parseXML(final URL xmlURL) throws JAXBException, SAXException, ParserConfigurationException
    {
        JAXBContext jc = JAXBContext.newInstance(OpenDRIVE.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(true);
        return (OpenDRIVE) unmarshaller.unmarshal(xmlURL);
    }

    /**
     * Parse an OpenDRIVE XML input stream and build an OTS object.
     * @param xmlStream inputStream; the xml stream
     * @return OpenDRIVE; the constructed OpenDRIVE object
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    public static OpenDRIVE parseXML(final InputStream xmlStream)
            throws JAXBException, SAXException, ParserConfigurationException
    {
        JAXBContext jc = JAXBContext.newInstance(OpenDRIVE.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        SAXSource saxSource = new SAXSource(xmlReader, new InputSource(xmlStream));
        return (OpenDRIVE) unmarshaller.unmarshal(saxSource);
    }

    /**
     * Parse the OpenDRIVE XML file and build the network.
     * @param xmlURL URL; the URL for the xml input file
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws OpenDriveParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GTUException when construction of the Strategical Planner failed
     */
    public static Experiment.TimeDoubleUnit<OTSSimulatorInterface> build(final URL xmlURL, final OTSRoadNetwork otsNetwork,
            final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, OpenDriveParserException,
            SAXException, ParserConfigurationException, SimRuntimeException, GTUException
    {
        return build(parseXML(xmlURL), otsNetwork, simulator);
    }

    /**
     * Build the network from an OpenDRIVE object (probably constructed by parsing an OpenDRIVE XML file; e.g. the parseXML
     * method).
     * @param openDrive OpenDRIVE; the OpenDRIVE object
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws OpenDriveParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GTUException when construction of the Strategical Planner failed
     */
    public static Experiment.TimeDoubleUnit<OTSSimulatorInterface> build(final OpenDRIVE openDrive,
            final OTSRoadNetwork otsNetwork, final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, OpenDriveParserException,
            SAXException, ParserConfigurationException, SimRuntimeException, GTUException
    {
        CategoryLogger.setLogCategories(Cat.PARSER);
        CategoryLogger.setAllLogLevel(Level.TRACE);

        // otsNetwork.addDefaultGtuTypes();
        // otsNetwork.addDefaultLinkTypes();
        // otsNetwork.addDefaultLaneTypes();
        
        Header header = openDrive.getHeader();
        double originLat = header.getOriginLat();
        double originLon = header.getOriginLong();
        
        new RoadParser(openDrive, otsNetwork, simulator).parseRoads();

        Experiment.TimeDoubleUnit<OTSSimulatorInterface> experiment = new Experiment.TimeDoubleUnit<OTSSimulatorInterface>();

        return experiment;

    }
}
