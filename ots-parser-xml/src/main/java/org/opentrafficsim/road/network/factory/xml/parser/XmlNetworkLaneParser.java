package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.InputParameters;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.StreamInformation;
import org.opentrafficsim.xml.generated.ANIMATION;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.GTUTEMPLATE;
import org.opentrafficsim.xml.generated.MODELTYPE;
import org.opentrafficsim.xml.generated.NETWORK;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.OTS;
import org.opentrafficsim.xml.generated.ROADLAYOUT;
import org.opentrafficsim.xml.generated.SCENARIO;
import org.pmw.tinylog.Level;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;

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
            final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, XmlParserException, SAXException,
            ParserConfigurationException, SimRuntimeException, GTUException
    {
        URL xmlURL = URLResource.getResource(filename);
        build(xmlURL, otsNetwork, simulator);
        return otsNetwork;
    }

    /**
     * Parse the XML input stream and build the network.
     * @param xmlStream InputStream; the xml input stream
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the experiment based on the information in the RUN tag
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
    public static Experiment.TimeDoubleUnit<OTSSimulatorInterface> build(final InputStream xmlStream,
            final OTSRoadNetwork otsNetwork, final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, XmlParserException, SAXException,
            ParserConfigurationException, SimRuntimeException, GTUException
    {
        return build(parseXML(xmlStream), otsNetwork, simulator);
    }

    /**
     * Parse an OTS XML input stream and build an OTS object.
     * @param xmlURL URL; the URL for the xml file or stream
     * @return OTS; the constructed OTS object
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    public static OTS parseXML(final URL xmlURL) throws JAXBException, SAXException, ParserConfigurationException
    {
        JAXBContext jc = JAXBContext.newInstance(OTS.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(true);
        return (OTS) unmarshaller.unmarshal(xmlURL);
    }

    /**
     * Parse an OTS XML input stream and build an OTS object.
     * @param xmlStream inputStream; the xml stream
     * @return OTS; the constructed OTS object
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    public static OTS parseXML(final InputStream xmlStream) throws JAXBException, SAXException, ParserConfigurationException
    {
        JAXBContext jc = JAXBContext.newInstance(OTS.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        SAXSource saxSource = new SAXSource(xmlReader, new InputSource(xmlStream));
        return (OTS) unmarshaller.unmarshal(saxSource);
    }

    /**
     * Parse the XML file and build the network.
     * @param xmlURL URL; the URL for the xml input file
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the experiment based on the information in the RUN tag
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
    public static Experiment.TimeDoubleUnit<OTSSimulatorInterface> build(final URL xmlURL, final OTSRoadNetwork otsNetwork,
            final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, XmlParserException, SAXException,
            ParserConfigurationException, SimRuntimeException, GTUException
    {
        return build(parseXML(xmlURL), otsNetwork, simulator);
    }

    /**
     * Build the network from an OTS object (probably constructed by parsing an OTS XML file; e.g. the parseXML method).
     * @param ots OTS; the OTS object
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param simulator OTSSimulatorInterface; the simulator
     * @return the experiment based on the information in the RUN tag
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
    public static Experiment.TimeDoubleUnit<OTSSimulatorInterface> build(final OTS ots, final OTSRoadNetwork otsNetwork,
            final OTSSimulatorInterface simulator)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, XmlParserException, SAXException,
            ParserConfigurationException, SimRuntimeException, GTUException
    {
        CategoryLogger.setLogCategories(Cat.PARSER);
        CategoryLogger.setAllLogLevel(Level.TRACE);

        Map<String, StreamInformation> streamMap = new LinkedHashMap<>();
        Experiment.TimeDoubleUnit<OTSSimulatorInterface> experiment =
                RunParser.parseRun(otsNetwork, ots.getRUN(), streamMap, simulator);

        Map<String, ROADLAYOUT> roadLayoutMap = new LinkedHashMap<>();
        Map<String, GTUTEMPLATE> gtuTemplates = new LinkedHashMap<>();
        Map<LinkType, Map<GTUType, Speed>> linkTypeSpeedLimitMap = new LinkedHashMap<>();
        DefinitionsParser.parseDefinitions(otsNetwork, ots.getDEFINITIONS(), true, roadLayoutMap, gtuTemplates, streamMap,
                linkTypeSpeedLimitMap);

        NETWORK network = ots.getNETWORK();
        Map<String, Direction> nodeDirections = NetworkParser.calculateNodeAngles(otsNetwork, network);
        NetworkParser.parseNodes(otsNetwork, network, nodeDirections);
        NetworkParser.parseLinks(otsNetwork, network, nodeDirections, simulator);
        NetworkParser.applyRoadLayout(otsNetwork, network, simulator, roadLayoutMap, linkTypeSpeedLimitMap);

        List<NETWORKDEMAND> demands = ots.getNETWORKDEMAND();
        for (NETWORKDEMAND demand : demands)
        {
            GeneratorSinkParser.parseRoutes(otsNetwork, demand);
            GeneratorSinkParser.parseShortestRoutes(otsNetwork, demand);
            Map<String, List<FrequencyAndObject<Route>>> routeMixMap = GeneratorSinkParser.parseRouteMix(otsNetwork, demand);
            Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap =
                    GeneratorSinkParser.parseShortestRouteMix(otsNetwork, demand);
            List<LaneBasedGTUGenerator> generators = GeneratorSinkParser.parseGenerators(otsNetwork, demand, gtuTemplates,
                    routeMixMap, shortestRouteMixMap, simulator, streamMap);
            GeneratorSinkParser.parseSinks(otsNetwork, demand, simulator);
        }

        List<MODELTYPE> models = ots.getMODEL();

        // TODO: parse input parameters
        InputParameters inputParameters = new InputParameters()
        {
            /** {@inheritDoc} */
            @Override
            public <T> Set<T> getObjects(final Class<T> clazz)
            {
                return new LinkedHashSet<>();
            }

            /** {@inheritDoc} */
            @Override
            public Map<String, InputParameter<?, ?>> getInputParameters(final Object object)
            {
                throw new UnsupportedOperationException("No input parameters.");
            }

            /** {@inheritDoc} */
            @Override
            public InputParameter<?, ?> getInputParameter(final Object object, final String id)
            {
                throw new UnsupportedOperationException("No input parameters.");
            }
        };
        Map<String, ParameterType<?>> parameterTypes = new LinkedHashMap<>();
        Map<String, ParameterFactory> parameterFactories =
                ModelParser.parseParameters(otsNetwork, models, inputParameters, parameterTypes, streamMap);
        DefinitionsParser.parseParameterTypes(ots.getDEFINITIONS(), otsNetwork, parameterTypes);
        Map<String, LaneBasedStrategicalPlannerFactory<?>> factories =
                ModelParser.parseModel(otsNetwork, models, inputParameters, parameterTypes, streamMap, parameterFactories);
        Map<String, String> modelIdReferrals = ScenarioParser.parseModelIdReferral(ots.getSCENARIO(), ots.getNETWORKDEMAND());
        List<LaneBasedGTUGenerator> generators =
                ODParser.parseDemand(otsNetwork, simulator, demands, gtuTemplates, factories, modelIdReferrals, streamMap);
        // The code below can be used to visualize the LaneStructure of a particular GTU
        /*-EventListenerInterface listener = new EventListenerInterface()
        {
            @Override
            public void notify(final EventInterface event) throws RemoteException
            {
                LaneBasedGTU gtu = (LaneBasedGTU) event.getContent();
                if (gtu.getId().equals("27"))
                {
                    try
                    {
                        LaneStructureAnimation.visualize(
                                (RollingLaneStructure) gtu.getTacticalPlanner().getPerception().getLaneStructure(), gtu);
                    }
                    catch (ParameterException | ClassCastException exception)
                    {
                        SimLogger.always().warn("Could not draw lane structure of GTU.");
                    }
                }
            }
        };
        for (LaneBasedGTUGenerator generator : generators)
        {
            generator.addListener(listener, LaneBasedGTUGenerator.GTU_GENERATED_EVENT);
        }*/

        List<CONTROL> controls = ots.getCONTROL();
        List<MODELTYPE> modelParameters = ots.getMODEL();
        List<SCENARIO> scenario = ots.getSCENARIO();
        ANIMATION animation = ots.getANIMATION();

        ControlParser.parseControl(otsNetwork, simulator, controls);

        return experiment;

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
