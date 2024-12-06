package org.opentrafficsim.road.network.factory.xml.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.Flattener;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.opentrafficsim.xml.generated.Demand;
import org.opentrafficsim.xml.generated.GtuTemplate;
import org.opentrafficsim.xml.generated.ModelType;
import org.opentrafficsim.xml.generated.Network;
import org.opentrafficsim.xml.generated.Ots;
import org.opentrafficsim.xml.generated.RoadLayout;
import org.opentrafficsim.xml.generated.ScenarioType;
import org.opentrafficsim.xml.generated.StripeType;
import org.pmw.tinylog.Level;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ExperimentRunControl;
import nl.tudelft.simulation.dsol.experiment.StreamSeedInformation;

/**
 * Parse an XML file for OTS, based on the ots.xsd definition.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class XmlParser implements Serializable
{
    /** */
    private static final long serialVersionUID = 2019022L;

    /** Road network. */
    private final RoadNetwork network;

    /** Stream with the XML information. */
    private InputStream stream;

    /** Scenario to parse. */
    private String scenario;

    /** Whether to parse conflicts. */
    private boolean parseConflicts;

    /**
     * Constructor.
     * @param network network.
     */
    public XmlParser(final RoadNetwork network)
    {
        this.network = network;
    }

    /**
     * Set file name.
     * @param filename file name.
     * @return this parser for method chaining.
     * @throws IllegalStateException file, URL or stream has already been set.
     * @throws IOException file could not be opened.
     */
    public XmlParser setFile(final String filename) throws IOException
    {
        Throw.when(this.stream != null, IllegalStateException.class, "Invoke only one of setFile(), setUrl(), or setStream().");
        this.stream = URLResource.getResource(filename).openStream();
        return this;
    }

    /**
     * Set url.
     * @param url url.
     * @return this parser for method chaining.
     * @throws IllegalStateException file, URL or stream has already been set.
     * @throws IOException file could not be opened.
     */
    public XmlParser setUrl(final URL url) throws IOException
    {
        Throw.when(this.stream != null, IllegalStateException.class, "Invoke only one of setFile(), setUrl(), or setStream().");
        this.stream = url.openStream();
        return this;
    }

    /**
     * Set stream.
     * @param stream stream.
     * @return this parser for method chaining.
     * @throws IllegalStateException file, URL or stream has already been set.
     */
    public XmlParser setStream(final InputStream stream)
    {
        Throw.when(this.stream != null, IllegalStateException.class, "Invoke only one of setFile(), setUrl(), or setStream().");
        this.stream = stream;
        return this;
    }

    /**
     * Set scenario to parse.
     * @param scenario name of scenario to parse.
     * @return this parser for method chaining.
     */
    public XmlParser setScenario(final String scenario)
    {
        this.scenario = scenario;
        return this;
    }

    /**
     * Set whether to parse conflicts.
     * @param parseConflicts whether to parse conflicts.
     * @return this parser for method chaining.
     */
    public XmlParser setParseConflict(final boolean parseConflicts)
    {
        this.parseConflicts = parseConflicts;
        return this;
    }

    /**
     * Build the simulation.
     * @return the experiment based on the information in the Run tag
     * @throws IllegalStateException when no file, url or stream was set.
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    public ExperimentRunControl<Duration> build()
            throws SimRuntimeException, MalformedURLException, JAXBException, URISyntaxException, NetworkException,
            XmlParserException, SAXException, ParserConfigurationException, GtuException, IOException, TrafficControlException
    {
        Throw.when(this.stream == null, IllegalStateException.class,
                "Invoke one of setFile(), setUrl(), or setStream() before parsing.");
        return build(parseXml(this.stream), this.network, this.scenario, this.parseConflicts);
    }

    /**
     * Parse an OTS XML input stream and build an OTS object.
     * @param xmlStream the xml stream
     * @return the constructed OTS object
     * @throws JAXBException when the parsing fails
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     */
    private static Ots parseXml(final InputStream xmlStream) throws JAXBException, SAXException, ParserConfigurationException
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
     * Build the network from an OTS object (probably constructed by parsing an OTS XML file; e.g. the parseXML method).
     * @param ots the OTS object
     * @param otsNetwork the network to insert the parsed objects in
     * @param scenario scenario name, may bee {@code null} to use default values.
     * @param buildConflicts whether to build conflicts or not
     * @return the experiment based on the information in the RUN tag
     * @throws JAXBException when the parsing fails
     * @throws URISyntaxException when the filename is not valid
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws ParserConfigurationException on error with parser configuration
     * @throws SAXException on error creating SAX parser
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     * @throws TrafficControlException when construction of a traffic controller fails
     * @throws IOException when construction of a traffic controller fails
     * @throws MalformedURLException when construction of a traffic controller fails
     */
    private static ExperimentRunControl<Duration> build(final Ots ots, final RoadNetwork otsNetwork, final String scenario,
            final boolean buildConflicts) throws JAXBException, URISyntaxException, NetworkException, XmlParserException,
            SAXException, ParserConfigurationException, SimRuntimeException, GtuException, MalformedURLException, IOException,
            TrafficControlException
    {
        CategoryLogger.setLogCategories(Cat.PARSER);
        CategoryLogger.setAllLogLevel(Level.TRACE);

        // input parameters
        Eval eval = ScenarioParser.parseInputParameters(ots.getScenarios(), scenario);

        // run
        StreamSeedInformation streamInformation = RunParser.parseStreams(ots.getRun(), eval);
        ExperimentRunControl<Duration> runControl =
                RunParser.parseRun(otsNetwork.getId(), ots.getRun(), streamInformation, otsNetwork.getSimulator(), eval);

        // definitions
        Map<String, StripeType> stripes = new LinkedHashMap<>();
        Map<String, RoadLayout> roadLayoutMap = new LinkedHashMap<>();
        Map<String, GtuTemplate> gtuTemplates = new LinkedHashMap<>();
        Map<String, LaneBias> laneBiases = new LinkedHashMap<>();
        Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap = new LinkedHashMap<>();
        Definitions definitions = DefinitionsParser.parseDefinitions(ots.getDefinitions(), roadLayoutMap, gtuTemplates,
                laneBiases, linkTypeSpeedLimitMap, stripes, eval);

        // network
        Network network = ots.getNetwork();
        Map<String, Direction> nodeDirections = NetworkParser.calculateNodeAngles(otsNetwork, network, eval);
        NetworkParser.parseNodes(otsNetwork, network, nodeDirections, eval);
        Map<String, ContinuousLine> designLines = new LinkedHashMap<>();
        Map<String, Flattener> flatteners = new LinkedHashMap<>();
        NetworkParser.parseLinks(otsNetwork, definitions, network, nodeDirections, otsNetwork.getSimulator(), designLines,
                flatteners, eval);
        NetworkParser.applyRoadLayouts(otsNetwork, definitions, network, roadLayoutMap, linkTypeSpeedLimitMap, designLines,
                flatteners, stripes, eval);
        NetworkParser.buildConflicts(otsNetwork, network, eval);

        // routes, generators and sinks
        Demand demand = ots.getDemand();
        if (demand != null)
        {
            DemandParser.parseRoutes(otsNetwork, definitions, demand, eval);
            DemandParser.parseShortestRoutes(otsNetwork, definitions, demand, eval);
            Map<String, List<FrequencyAndObject<Route>>> routeMixMap = DemandParser.parseRouteMix(otsNetwork, demand, eval);
            Map<String, List<FrequencyAndObject<Route>>> shortestRouteMixMap =
                    DemandParser.parseShortestRouteMix(otsNetwork, demand, eval);

            IdGenerator idGenerator = new IdGenerator("");
            List<LaneBasedGtuGenerator> generators = DemandParser.parseGenerators(otsNetwork, definitions, demand, gtuTemplates,
                    routeMixMap, shortestRouteMixMap, streamInformation, idGenerator, eval);
            System.out.println("Created " + generators.size() + " generators based on explicit generator definitions");
            generators = DemandParser.parseInjectionGenerators(otsNetwork, definitions, demand, gtuTemplates, routeMixMap,
                    shortestRouteMixMap, streamInformation, idGenerator, eval);
            System.out
                    .println("Created " + generators.size() + " generators based on explicit injection generator definitions");
            DemandParser.parseSinks(otsNetwork, demand, definitions, eval);
        }

        // models and parameters
        // TODO: we now only take the first model, need to make models per GTU type, and with parents
        List<ModelType> models = ots.getModels() == null ? new ArrayList<>() : ots.getModels().getModel();

        Map<String, ParameterType<?>> parameterTypes = new LinkedHashMap<>();
        DefinitionsParser.parseParameterTypes(ots.getDefinitions(), parameterTypes, eval);
        ParameterFactory parameterFactory =
                ModelParser.parseParameters(definitions, models, eval, parameterTypes, streamInformation);
        Map<String, LaneBasedStrategicalPlannerFactory<?>> factories =
                ModelParser.parseModel(otsNetwork, models, eval, parameterTypes, streamInformation, parameterFactory);
        List<ScenarioType> scenarios = ots.getScenarios() == null ? new ArrayList<>() : ots.getScenarios().getScenario();
        if (demand != null)
        {
            Map<String, String> modelIdReferrals = ScenarioParser.parseModelIdReferral(scenarios, demand, eval);

            // OD generators
            List<LaneBasedGtuGenerator> generators = OdParser.parseDemand(otsNetwork, definitions, demand, gtuTemplates,
                    laneBiases, factories, modelIdReferrals, streamInformation, eval);
            System.out.println("Created " + generators.size() + " generators based on origin destination matrices");
        }

        // control
        if (ots.getControl() != null)
        {
            ControlParser.parseControl(otsNetwork, otsNetwork.getSimulator(), ots.getControl(), definitions, eval);
        }

        return runControl;
    }

    /**
     * DefaultsResolver takes care of locating the defaults include files at the right place.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private static class DefaultsResolver implements EntityResolver
    {
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
     * @param args not used
     * @throws Exception on parsing error
     */
    public static void main(final String[] args) throws Exception
    {
        OtsSimulatorInterface simulator = new OtsSimulator("XmlNetworkLaneParser");
        new XmlParser(new RoadNetwork("", simulator)).setFile("/example.xml").build();
        System.exit(0);
    }

}
