package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.InputParameters;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.xml.generated.Demand;
import org.opentrafficsim.xml.generated.GtuTemplate;
import org.opentrafficsim.xml.generated.ModelType;
import org.opentrafficsim.xml.generated.Network;
import org.opentrafficsim.xml.generated.Ots;
import org.opentrafficsim.xml.generated.RoadLayout;
import org.opentrafficsim.xml.generated.ScenarioType;
import org.pmw.tinylog.Level;
import org.xml.sax.EntityResolver;
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
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param buildConflicts boolean; whether to build conflicts or not
     * @return the network that contains the parsed objects
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public static RoadNetwork build(final String filename, final RoadNetwork otsNetwork, final boolean buildConflicts)
            throws JAXBException, URISyntaxException, NetworkException, OtsGeometryException, XmlParserException, SAXException,
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
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param buildConflicts boolean; whether to build conflicts or not
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public static ExperimentRunControl<Duration> build(final InputStream xmlStream, final RoadNetwork otsNetwork,
            final boolean buildConflicts) throws JAXBException, URISyntaxException, NetworkException, OtsGeometryException,
            XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GtuException,
            MalformedURLException, IOException, TrafficControlException
    {
        return build(parseXml(xmlStream), otsNetwork, buildConflicts);
    }

    /**
     * Parse an OTS XML input stream and build an OTS object.
     * @param xmlURL URL; the URL for the xml file or stream
     * @return Ots; the constructed OTS object
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws IOException if the URL does not exist
     */
    public static Ots parseXml(final URL xmlURL) throws JAXBException, SAXException, ParserConfigurationException, IOException
    {
        return parseXml(xmlURL.openStream());
    }

    /**
     * Parse an OTS XML input stream and build an OTS object.
     * @param xmlStream inputStream; the xml stream
     * @return Ots; the constructed OTS object
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    public static Ots parseXml(final InputStream xmlStream) throws JAXBException, SAXException, ParserConfigurationException
    {
        Locale locale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        JAXBContext jc = JAXBContext.newInstance(Ots.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(new DefaultsResolver());
        SAXSource saxSource = new SAXSource(xmlReader, new InputSource(xmlStream));
        Ots result = (Ots) unmarshaller.unmarshal(saxSource);
        Locale.setDefault(locale);
        return result;
    }

    /**
     * Parse the XML file and build the network.
     * @param xmlURL URL; the URL for the xml input file
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param buildConflicts boolean; whether to build conflicts or not
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public static ExperimentRunControl<Duration> build(final URL xmlURL, final RoadNetwork otsNetwork,
            final boolean buildConflicts) throws JAXBException, URISyntaxException, NetworkException, OtsGeometryException,
            XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GtuException,
            MalformedURLException, IOException, TrafficControlException
    {
        return build(parseXml(xmlURL), otsNetwork, buildConflicts);
    }

    /**
     * Build the network from an OTS object (probably constructed by parsing an OTS XML file; e.g. the parseXML method).
     * @param ots Ots; the OTS object
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param buildConflicts boolean; whether to build conflicts or not
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line of a link is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public static ExperimentRunControl<Duration> build(final Ots ots, final RoadNetwork otsNetwork,
            final boolean buildConflicts) throws JAXBException, URISyntaxException, NetworkException, OtsGeometryException,
            XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GtuException,
            MalformedURLException, IOException, TrafficControlException
    {
        CategoryLogger.setLogCategories(Cat.PARSER);
        CategoryLogger.setAllLogLevel(Level.TRACE);

        StreamSeedInformation streamInformation = new StreamSeedInformation();
        ExperimentRunControl<Duration> runControl =
                RunParser.parseRun(otsNetwork.getId(), ots.getRun(), streamInformation, otsNetwork.getSimulator());

        Map<String, RoadLayout> roadLayoutMap = new LinkedHashMap<>();
        Map<String, GtuTemplate> gtuTemplates = new LinkedHashMap<>();
        Map<String, LaneBias> laneBiases = new LinkedHashMap<>();
        Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap = new LinkedHashMap<>();
        Definitions definitions = DefinitionsParser.parseDefinitions(ots.getDefinitions(), roadLayoutMap, gtuTemplates,
                laneBiases, streamInformation, linkTypeSpeedLimitMap);

        Network network = ots.getNetwork();
        Map<String, Direction> nodeDirections = NetworkParser.calculateNodeAngles(otsNetwork, network);
        NetworkParser.parseNodes(otsNetwork, network, nodeDirections);
        Map<String, ContinuousLine> designLines = new LinkedHashMap<>();
        NetworkParser.parseLinks(otsNetwork, definitions, network, nodeDirections, otsNetwork.getSimulator(), designLines);
        NetworkParser.applyRoadLayout(otsNetwork, definitions, network, otsNetwork.getSimulator(), roadLayoutMap,
                linkTypeSpeedLimitMap, designLines);

        Demand demand = ots.getDemand();
        if (demand != null)
        {
            GeneratorSinkParser.parseRoutes(otsNetwork, definitions, demand);
            GeneratorSinkParser.parseShortestRoutes(otsNetwork, definitions, demand);
            Map<String, List<FrequencyAndObject<Route>>> routeMixMap = GeneratorSinkParser.parseRouteMix(otsNetwork, demand);
            Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap =
                    GeneratorSinkParser.parseShortestRouteMix(otsNetwork, demand);
            List<LaneBasedGtuGenerator> generators = GeneratorSinkParser.parseGenerators(otsNetwork, definitions, demand,
                    gtuTemplates, routeMixMap, shortestRouteMixMap, streamInformation);
            System.out.println("Created " + generators.size() + " generators based on explicit generator definitions");
            GeneratorSinkParser.parseSinks(otsNetwork, demand, otsNetwork.getSimulator(), definitions);
        }

        // TODO: we now only take the first model, need to make models per GTU type, and with parents
        List<ModelType> models = ots.getModels() == null ? new ArrayList<>() : ots.getModels().getModel();

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
        DefinitionsParser.parseParameterTypes(ots.getDefinitions(), parameterTypes);
        Map<String, ParameterFactory> parameterFactories =
                ModelParser.parseParameters(definitions, models, inputParameters, parameterTypes, streamInformation);
        Map<String, LaneBasedStrategicalPlannerFactory<?>> factories = ModelParser.parseModel(otsNetwork, models,
                inputParameters, parameterTypes, streamInformation, parameterFactories);
        List<ScenarioType> scenarios = ots.getScenarios() == null ? new ArrayList<>() : ots.getScenarios().getScenario();
        Map<String, String> modelIdReferrals =
                ScenarioParser.parseModelIdReferral(scenarios, ots.getDemand());
        try
        {
            List<LaneBasedGtuGenerator> generators = OdParser.parseDemand(otsNetwork, definitions, demand, gtuTemplates,
                    laneBiases, factories, modelIdReferrals, streamInformation);
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
            for (Object o : network.getNodeOrLinkOrCentroid())
            {
                if (o instanceof org.opentrafficsim.xml.generated.Link)
                {
                    org.opentrafficsim.xml.generated.Link link = (org.opentrafficsim.xml.generated.Link) o;
                    if (link.getConflictId() != null)
                    {
                        if (!conflictCandidateMap.containsKey(link.getConflictId()))
                        {
                            conflictCandidateMap.put(link.getConflictId(), new LinkedHashSet<Link>());
                        }
                        conflictCandidateMap.get(link.getConflictId()).add(otsNetwork.getLink(link.getId()));
                    }
                }
            }
            otsNetwork.getSimulator().getLogger().always().info("Map size of conflict candidate regions = {}",
                    conflictCandidateMap.size());

            if (conflictCandidateMap.size() == 0)
            {
                ConflictBuilder.buildConflictsParallel(otsNetwork, otsNetwork.getSimulator(),
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));
            }
            else
            {
                ConflictBuilder.buildConflictsParallel(otsNetwork, conflictCandidateMap, otsNetwork.getSimulator(),
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));
            }
            otsNetwork.getSimulator().getLogger().always().info("Object map size = {}", otsNetwork.getObjectMap().size());
        }

        // The code below can be used to visualize the LaneStructure of a particular GTU
        /*-EventListener listener = new EventListener()
        {
            @Override
            public void notify(final Event event) throws RemoteException
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

        // Control control = ots.getControl();
        // List<ModelType> modelParameters = ots.getModels();
        // List<Scenario> scenario = ots.getScenario();
        // Animation animation = ots.getAnimation();

        if (ots.getControl() != null)
        {
            ControlParser.parseControl(otsNetwork, otsNetwork.getSimulator(), ots.getControl(), definitions);
        }

        return runControl;
    }

    /**
     * DefaultsResolver takes care of locating the defaults include files at the right place.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    static class DefaultsResolver implements EntityResolver
    {
        /** {@inheritDoc} */
        @Override
        public InputSource resolveEntity(final String publicId, final String systemId)
        {
            if (systemId.contains("defaults/"))
            {
                String location = "/resources/xsd/defaults" + systemId.substring(systemId.lastIndexOf('/'));
                InputStream stream = URLResource.getResourceAsStream(location);
                return new InputSource(stream);
            }
            else
            {
                return new InputSource(URLResource.getResourceAsStream(systemId));
            }
        }
    }

    /**
     * @param args String[]; not used
     * @throws Exception on parsing error
     */
    public static void main(final String[] args) throws Exception
    {
        OtsSimulatorInterface simulator = new OtsSimulator("XmlNetworkLaneParser");
        build("/example.xml", new RoadNetwork("", simulator), false);
        System.exit(0);
    }
}
