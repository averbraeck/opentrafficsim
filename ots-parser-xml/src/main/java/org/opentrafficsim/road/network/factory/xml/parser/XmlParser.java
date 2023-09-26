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
import java.util.function.Supplier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.eval.Eval;
import org.djutils.eval.RetrieveValue;
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
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.ParseDistribution;
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
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.experiment.StreamSeedInformation;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Parse an XML file for OTS, based on the ots.xsd definition.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
     * @param network RoadNetwork; network.
     */
    public XmlParser(final RoadNetwork network)
    {
        this.network = network;
    }

    /**
     * Set file name.
     * @param filename String; file name.
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
     * @param url URL; url.
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
     * @param stream InputStream; stream.
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
     * @param scenario String; name of scenario to parse.
     * @return this parser for method chaining.
     */
    public XmlParser setScenario(final String scenario)
    {
        this.scenario = scenario;
        return this;
    }

    /**
     * Set whether to parse conflicts.
     * @param parseConflicts boolean; whether to parse conflicts.
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
    public ExperimentRunControl<Duration> build() throws SimRuntimeException, MalformedURLException, JAXBException,
            URISyntaxException, NetworkException, OtsGeometryException, XmlParserException, SAXException,
            ParserConfigurationException, GtuException, IOException, TrafficControlException
    {
        Throw.when(this.stream == null, IllegalStateException.class,
                "Invoke one of setFile(), setUrl(), or setStream() before parsing.");
        return build(parseXml(this.stream), this.network, this.scenario, this.parseConflicts);
    }

    /**
     * Parse an OTS XML input stream and build an OTS object.
     * @param xmlStream inputStream; the xml stream
     * @return Ots; the constructed OTS object
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
     * @param ots Ots; the OTS object
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param scenario String; scenario name, may bee {@code null} to use default values.
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
    private static ExperimentRunControl<Duration> build(final Ots ots, final RoadNetwork otsNetwork, final String scenario,
            final boolean buildConflicts) throws JAXBException, URISyntaxException, NetworkException, OtsGeometryException,
            XmlParserException, SAXException, ParserConfigurationException, SimRuntimeException, GtuException,
            MalformedURLException, IOException, TrafficControlException
    {
        CategoryLogger.setLogCategories(Cat.PARSER);
        CategoryLogger.setAllLogLevel(Level.TRACE);

        // run and input parameters
        StreamSeedInformation streamInformation = RunParser.parseStreams(ots.getRun());
        Map<String, Supplier<?>> defaultsMap = new LinkedHashMap<>();
        ParameterWrapper defaults = new ParameterWrapper(null, defaultsMap);
        Eval eval = new Eval().setRetrieveValue(defaults);
        if (ots.getScenarios() != null)
        {
            parseInputParameters(ots.getScenarios().getDefaultInputParameters(), streamInformation, eval, defaultsMap);
        }
        ParameterWrapper inputParameters = defaults;
        if (ots.getScenarios() != null)
        {
            for (ScenarioType scenarioTag : ots.getScenarios().getScenario())
            {
                if (scenarioTag.getId().equals(scenario))
                {
                    if (scenarioTag.getInputParameters() != null)
                    {
                        Map<String, Supplier<?>> inputParametersMap = new LinkedHashMap<>();
                        inputParameters = new ParameterWrapper(defaults, inputParametersMap);
                        eval.setRetrieveValue(inputParameters);
                        parseInputParameters(scenarioTag.getInputParameters(), streamInformation, eval, inputParametersMap);
                    }
                    break;
                }
            }
        }
        ExperimentRunControl<Duration> runControl =
                RunParser.parseRun(otsNetwork.getId(), ots.getRun(), streamInformation, otsNetwork.getSimulator(), eval);

        // definitions
        Map<String, RoadLayout> roadLayoutMap = new LinkedHashMap<>();
        Map<String, GtuTemplate> gtuTemplates = new LinkedHashMap<>();
        Map<String, LaneBias> laneBiases = new LinkedHashMap<>();
        Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap = new LinkedHashMap<>();
        Definitions definitions = DefinitionsParser.parseDefinitions(ots.getDefinitions(), roadLayoutMap, gtuTemplates,
                laneBiases, linkTypeSpeedLimitMap, eval);

        // network
        Network network = ots.getNetwork();
        Map<String, Direction> nodeDirections = NetworkParser.calculateNodeAngles(otsNetwork, network, eval);
        NetworkParser.parseNodes(otsNetwork, network, nodeDirections, eval);
        Map<String, ContinuousLine> designLines = new LinkedHashMap<>();
        NetworkParser.parseLinks(otsNetwork, definitions, network, nodeDirections, otsNetwork.getSimulator(), designLines,
                eval);
        NetworkParser.applyRoadLayout(otsNetwork, definitions, network, otsNetwork.getSimulator(), roadLayoutMap,
                linkTypeSpeedLimitMap, designLines, eval);
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
            List<LaneBasedGtuGenerator> generators = DemandParser.parseGenerators(otsNetwork, definitions, demand, gtuTemplates,
                    routeMixMap, shortestRouteMixMap, streamInformation, eval);
            System.out.println("Created " + generators.size() + " generators based on explicit generator definitions");
            DemandParser.parseSinks(otsNetwork, demand, otsNetwork.getSimulator(), definitions, eval);
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
        Map<String, String> modelIdReferrals = ScenarioParser.parseModelIdReferral(scenarios, ots.getDemand(), eval);

        // OD generators
        List<LaneBasedGtuGenerator> generators = OdParser.parseDemand(otsNetwork, definitions, demand, gtuTemplates, laneBiases,
                factories, modelIdReferrals, streamInformation, eval);
        System.out.println("Created " + generators.size() + " generators based on origin destination matrices");

        // control
        if (ots.getControl() != null)
        {
            ControlParser.parseControl(otsNetwork, otsNetwork.getSimulator(), ots.getControl(), definitions, eval);
        }

        return runControl;
    }

    /**
     * Wraps parameters to provide for expressions.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private static class ParameterWrapper implements RetrieveValue
    {
        /** Default input parameters. */
        private final ParameterWrapper defaults;

        /** Map of name to suppliers (constant or distribution). */
        private final Map<String, Supplier<?>> map;

        /**
         * Constructor.
         * @param defaults ParameterWrapper; default parameters, may be {@code null}.
         * @param map Map&lt;String, Supplier&lt;?&gt;&gt;; map that underlines input parameters.
         */
        public ParameterWrapper(final ParameterWrapper defaults, final Map<String, Supplier<?>> map)
        {
            this.defaults = defaults;
            this.map = map;
        }

        /** {@inheritDoc} */
        @Override
        public Object lookup(final String name)
        {
            if (this.map.containsKey(name))
            {
                Object value = this.map.get(name).get();
                if (value instanceof Double)
                {
                    return Dimensionless.instantiateSI((Double) value);
                }
                return value;
            }
            if (this.defaults == null)
            {
                throw new RuntimeException("Parameter " + name + " not available.");
            }
            return this.defaults.lookup(name);
        }
    }

    /**
     * Parse input parameters.
     * @param inputParametersXml InputParameters; xml tag.
     * @param streamInformation StreamInformation; stream information.
     * @param eval Eval; expression evaluator.
     * @param map Map&lt;String, Supplier&lt;?&gt;&gt;; map that underlines inputParameters.
     * @throws XmlParserException when there is circular dependency between parameters.
     */
    private static void parseInputParameters(final org.opentrafficsim.xml.generated.InputParameters inputParametersXml,
            final StreamInformation streamInformation, final Eval eval, final Map<String, Supplier<?>> map)
            throws XmlParserException
    {
        boolean failed = true;
        int pass = 1;
        while (failed)
        {
            failed = false;
            int size = map.size();
            for (Serializable parameter : inputParametersXml.getDurationOrDurationDistOrLength())
            {
                try
                {
                    if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Duration)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Duration p =
                                (org.opentrafficsim.xml.generated.InputParameters.Duration) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.DurationDist)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.DurationDist p =
                                (org.opentrafficsim.xml.generated.InputParameters.DurationDist) parameter;
                        ContinuousDistDoubleScalar.Rel<?, ?> d = ParseDistribution.parseContinuousDist(streamInformation, p,
                                p.getDurationUnit().get(eval), eval);
                        map.put(trim(p.getId()), () -> d.draw());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Length)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Length p =
                                (org.opentrafficsim.xml.generated.InputParameters.Length) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.LengthDist)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.LengthDist p =
                                (org.opentrafficsim.xml.generated.InputParameters.LengthDist) parameter;
                        ContinuousDistDoubleScalar.Rel<?, ?> d =
                                ParseDistribution.parseContinuousDist(streamInformation, p, p.getLengthUnit().get(eval), eval);
                        map.put(trim(p.getId()), () -> d.draw());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Speed)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Speed p =
                                (org.opentrafficsim.xml.generated.InputParameters.Speed) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.SpeedDist)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.SpeedDist p =
                                (org.opentrafficsim.xml.generated.InputParameters.SpeedDist) parameter;
                        ContinuousDistDoubleScalar.Rel<?, ?> d =
                                ParseDistribution.parseContinuousDist(streamInformation, p, p.getSpeedUnit().get(eval), eval);
                        map.put(trim(p.getId()), () -> d.draw());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Acceleration)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Acceleration p =
                                (org.opentrafficsim.xml.generated.InputParameters.Acceleration) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.AccelerationDist)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.AccelerationDist p =
                                (org.opentrafficsim.xml.generated.InputParameters.AccelerationDist) parameter;
                        ContinuousDistDoubleScalar.Rel<?, ?> d = ParseDistribution.parseContinuousDist(streamInformation, p,
                                p.getAccelerationUnit().get(eval), eval);
                        map.put(trim(p.getId()), () -> d.draw());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.LinearDensity)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.LinearDensity p =
                                (org.opentrafficsim.xml.generated.InputParameters.LinearDensity) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.LinearDensityDist)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.LinearDensityDist p =
                                (org.opentrafficsim.xml.generated.InputParameters.LinearDensityDist) parameter;
                        ContinuousDistDoubleScalar.Rel<?, ?> d = ParseDistribution.parseContinuousDist(streamInformation, p,
                                p.getLinearDensityUnit().get(eval), eval);
                        map.put(trim(p.getId()), () -> d.draw());
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Frequency)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Frequency p =
                                (org.opentrafficsim.xml.generated.InputParameters.Frequency) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.FrequencyDist)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.FrequencyDist p =
                                (org.opentrafficsim.xml.generated.InputParameters.FrequencyDist) parameter;
                        ContinuousDistDoubleScalar.Rel<?, ?> d = ParseDistribution.parseContinuousDist(streamInformation, p,
                                p.getFrequencyUnit().get(eval), eval);
                        map.put(trim(p.getId()), () -> d.draw());
                    }

                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Double)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Double p =
                                (org.opentrafficsim.xml.generated.InputParameters.Double) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.DoubleDist)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.DoubleDist p =
                                (org.opentrafficsim.xml.generated.InputParameters.DoubleDist) parameter;
                        DistContinuous d = ParseDistribution.makeDistContinuous(streamInformation, p, eval);
                        map.put(trim(p.getId()), () -> d.draw());
                    }

                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Fraction)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Fraction p =
                                (org.opentrafficsim.xml.generated.InputParameters.Fraction) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }

                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Integer)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Integer p =
                                (org.opentrafficsim.xml.generated.InputParameters.Integer) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.IntegerDist)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.IntegerDist p =
                                (org.opentrafficsim.xml.generated.InputParameters.IntegerDist) parameter;
                        DistDiscrete d = ParseDistribution.makeDistDiscrete(streamInformation, p, eval);
                        map.put(trim(p.getId()), () -> d.draw());
                    }

                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Boolean)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Boolean p =
                                (org.opentrafficsim.xml.generated.InputParameters.Boolean) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }

                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.String)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.String p =
                                (org.opentrafficsim.xml.generated.InputParameters.String) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }

                    else if (parameter instanceof org.opentrafficsim.xml.generated.InputParameters.Class)
                    {
                        org.opentrafficsim.xml.generated.InputParameters.Class p =
                                (org.opentrafficsim.xml.generated.InputParameters.Class) parameter;
                        map.put(trim(p.getId()), () -> p.getValue().get(eval));
                    }
                }
                catch (XmlParserException e) // TODO: catch eval exception
                {
                    failed = true;
                }
            }
            if ((map.size() == size && !inputParametersXml.getDurationOrDurationDistOrLength().isEmpty()) || pass == 50)
            {
                throw new XmlParserException("Could not parse input parameters due to circular dependency.");
            }
            pass++;
        }
    }

    /**
     * Strips curly brackets (or any character) from start and end of input string.
     * @param id String; id.
     * @return string without curly brackets.
     */
    private static String trim(final String id)
    {
        return id.substring(1, id.length() - 1);
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
    private static class DefaultsResolver implements EntityResolver
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
        new XmlParser(new RoadNetwork("", simulator)).setFile("/example.xml").build();
        System.exit(0);
    }

}
