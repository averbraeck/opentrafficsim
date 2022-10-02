package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
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

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.InputParameters;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.xml.generated.ANIMATION;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.GTUTEMPLATE;
import org.opentrafficsim.xml.generated.LINK;
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
import nl.tudelft.simulation.dsol.experiment.ExperimentRunControl;
import nl.tudelft.simulation.dsol.experiment.StreamSeedInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;

/**
 * Parse an XML file for an OTS network, based on the ots-network.xsd definition.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
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
     * @param buildConflicts boolean; whether to build conflicts or not
     * @return the network that contains the parsed objects
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public static OTSRoadNetwork build(final String filename, final OTSRoadNetwork otsNetwork, final boolean buildConflicts)
            throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException, XmlParserException, SAXException,
            ParserConfigurationException, SimRuntimeException, GtuException, MalformedURLException, IOException,
            TrafficControlException
    {
        URL xmlURL = URLResource.getResource(filename);
        build(xmlURL, otsNetwork, buildConflicts);
        return otsNetwork;
    }

    /**
     * Parse the XML input stream and build the network.
     * @param xmlStream InputStream; the xml input stream
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param buildConflicts boolean; whether to build conflicts or not
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public static ExperimentRunControl<Duration> build(final InputStream xmlStream, final OTSRoadNetwork otsNetwork,
            final boolean buildConflicts) throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException,
            XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GtuException,
            MalformedURLException, IOException, TrafficControlException
    {
        return build(parseXML(xmlStream), otsNetwork, buildConflicts);
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
     * @param buildConflicts boolean; whether to build conflicts or not
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public static ExperimentRunControl<Duration> build(final URL xmlURL, final OTSRoadNetwork otsNetwork,
            final boolean buildConflicts) throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException,
            XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GtuException,
            MalformedURLException, IOException, TrafficControlException
    {
        return build(parseXML(xmlURL), otsNetwork, buildConflicts);
    }

    /**
     * Build the network from an OTS object (probably constructed by parsing an OTS XML file; e.g. the parseXML method).
     * @param ots OTS; the OTS object
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param buildConflicts boolean; whether to build conflicts or not
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public static ExperimentRunControl<Duration> build(final OTS ots, final OTSRoadNetwork otsNetwork,
            final boolean buildConflicts) throws JAXBException, URISyntaxException, NetworkException, OTSGeometryException,
            XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GtuException,
            MalformedURLException, IOException, TrafficControlException
    {
        CategoryLogger.setLogCategories(Cat.PARSER);
        CategoryLogger.setAllLogLevel(Level.TRACE);

        StreamSeedInformation streamInformation = new StreamSeedInformation();
        ExperimentRunControl<Duration> runControl =
                RunParser.parseRun(otsNetwork.getId(), ots.getRUN(), streamInformation, otsNetwork.getSimulator());

        Map<String, ROADLAYOUT> roadLayoutMap = new LinkedHashMap<>();
        Map<String, GTUTEMPLATE> gtuTemplates = new LinkedHashMap<>();
        Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap = new LinkedHashMap<>();
        DefinitionsParser.parseDefinitions(otsNetwork, ots.getDEFINITIONS(), true, roadLayoutMap, gtuTemplates,
                streamInformation, linkTypeSpeedLimitMap);

        NETWORK network = ots.getNETWORK();
        Map<String, Direction> nodeDirections = NetworkParser.calculateNodeAngles(otsNetwork, network);
        NetworkParser.parseNodes(otsNetwork, network, nodeDirections);
        NetworkParser.parseLinks(otsNetwork, network, nodeDirections, otsNetwork.getSimulator());
        NetworkParser.applyRoadLayout(otsNetwork, network, otsNetwork.getSimulator(), roadLayoutMap, linkTypeSpeedLimitMap);

        List<NETWORKDEMAND> demands = ots.getNETWORKDEMAND();
        for (NETWORKDEMAND demand : demands)
        {
            GeneratorSinkParser.parseRoutes(otsNetwork, demand);
            GeneratorSinkParser.parseShortestRoutes(otsNetwork, demand);
            Map<String, List<FrequencyAndObject<Route>>> routeMixMap = GeneratorSinkParser.parseRouteMix(otsNetwork, demand);
            Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap =
                    GeneratorSinkParser.parseShortestRouteMix(otsNetwork, demand);
            List<LaneBasedGtuGenerator> generators = GeneratorSinkParser.parseGenerators(otsNetwork, demand, gtuTemplates,
                    routeMixMap, shortestRouteMixMap, streamInformation);
            System.out.println("Created " + generators.size() + " generators based on explicit generator definitions");
            GeneratorSinkParser.parseSinks(otsNetwork, demand, otsNetwork.getSimulator());
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
                ModelParser.parseParameters(otsNetwork, models, inputParameters, parameterTypes, streamInformation);
        DefinitionsParser.parseParameterTypes(ots.getDEFINITIONS(), otsNetwork, parameterTypes);
        Map<String, LaneBasedStrategicalPlannerFactory<?>> factories = ModelParser.parseModel(otsNetwork, models,
                inputParameters, parameterTypes, streamInformation, parameterFactories);
        Map<String, String> modelIdReferrals = ScenarioParser.parseModelIdReferral(ots.getSCENARIO(), ots.getNETWORKDEMAND());
        try
        {
            List<LaneBasedGtuGenerator> generators =
                    ODParser.parseDemand(otsNetwork, demands, gtuTemplates, factories, modelIdReferrals, streamInformation);
            System.out.println("Created " + generators.size() + " generators based on origin destination matrices");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // conflicts
        if (buildConflicts)
        {
            otsNetwork.getSimulator().getLogger().always().info("Generating conflicts");
            Map<String, Set<Link>> conflictCandidateMap = new LinkedHashMap<String, Set<Link>>();
            for (Object o : network.getIncludeOrNODEOrCONNECTOR())
            {
                if (o instanceof LINK)
                {
                    LINK link = (LINK) o;
                    if (link.getCONFLICTID() != null)
                    {
                        if (!conflictCandidateMap.containsKey(link.getCONFLICTID()))
                        {
                            conflictCandidateMap.put(link.getCONFLICTID(), new LinkedHashSet<Link>());
                        }
                        conflictCandidateMap.get(link.getCONFLICTID()).add(otsNetwork.getLink(link.getID()));
                    }
                }
            }
            otsNetwork.getSimulator().getLogger().always().info("Map size of conflict candidate regions = {}",
                    conflictCandidateMap.size());

            if (conflictCandidateMap.size() == 0)
            {
                ConflictBuilder.buildConflictsParallel(otsNetwork, otsNetwork.getGtuType(GtuType.DEFAULTS.VEHICLE),
                        otsNetwork.getSimulator(), new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));
            }
            else
            {
                ConflictBuilder.buildConflictsParallel(otsNetwork, conflictCandidateMap,
                        otsNetwork.getGtuType(GtuType.DEFAULTS.VEHICLE), otsNetwork.getSimulator(),
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));
            }
            otsNetwork.getSimulator().getLogger().always().info("Object map size = {}", otsNetwork.getObjectMap().size());
        }

        // The code below can be used to visualize the LaneStructure of a particular GTU
        /*-EventListenerInterface listener = new EventListenerInterface()
        {
            @Override
            public void notify(final EventInterface event) throws RemoteException
            {
                LaneBasedGtu gtu = (LaneBasedGtu) event.getContent();
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
        for (LaneBasedGtuGenerator generator : generators)
        {
            generator.addListener(listener, LaneBasedGtuGenerator.GTU_GENERATED_EVENT);
        }*/

        List<CONTROL> controls = ots.getCONTROL();
        List<MODELTYPE> modelParameters = ots.getMODEL();
        List<SCENARIO> scenario = ots.getSCENARIO();
        ANIMATION animation = ots.getANIMATION();

        ControlParser.parseControl(otsNetwork, otsNetwork.getSimulator(), controls);

        return runControl;
    }

    /**
     * @param args String[]; not used
     * @throws Exception on parsing error
     */
    public static void main(final String[] args) throws Exception
    {
        OtsSimulatorInterface simulator = new OtsSimulator("XmlNetworkLaneParser");
        build("/example.xml", new OTSRoadNetwork("", true, simulator), false);
        System.exit(0);
    }
}
