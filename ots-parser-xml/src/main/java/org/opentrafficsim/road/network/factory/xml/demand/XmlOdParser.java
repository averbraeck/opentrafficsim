package org.opentrafficsim.road.network.factory.xml.demand;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.NestedCache;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * OD parser.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 25 mei 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class XmlOdParser implements Serializable
{

    /** */
    private static final long serialVersionUID = 20180525L;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Network. */
    final OTSRoadNetwork network;

    /** GTU types. */
    private final Map<String, GTUType> gtuTypes = new LinkedHashMap<>();

    /** Categorization. */
    Categorization categorization;

    /** Categories. */
    Map<String, CategoryTag> categories = new LinkedHashMap<>();

    /** Global time vector. */
    TimeVector globalTime;

    /** Global interpolation. */
    Interpolation globalInterpolation;

    /** Demand. */
    NestedCache<Set<DemandTag>> demand =
            new NestedCache<>(org.opentrafficsim.core.network.Node.class, org.opentrafficsim.core.network.Node.class);

    /**
     * Constructor.
     * @param simulator OTSSimulatorInterface; simulator
     * @param network OTSRoadNetwork; network
     * @param gtuTypes Set&lt;GTUType&gt;; set of GTU types
     */
    public XmlOdParser(final OTSSimulatorInterface simulator, final OTSRoadNetwork network, final Set<GTUType> gtuTypes)
    {
        Throw.whenNull(simulator, "Simulator should not be null.");
        Throw.whenNull(network, "Network should not be null.");
        this.simulator = simulator;
        this.network = network;
        for (GTUType gtuType : gtuTypes)
        {
            this.gtuTypes.put(gtuType.getId(), gtuType);
        }
    }

    /**
     * Returns the GTU type for the given id.
     * @param id String; id
     * @return GTU type for the given id
     * @throws XmlParserException if the GTU type is not available
     */
    public GTUType getGTUType(final String id) throws XmlParserException
    {
        Throw.when(!this.gtuTypes.containsKey(id), XmlParserException.class, "GTU type %s is not available.", id);
        return this.gtuTypes.get(id);
    }

    /**
     * Applies demand from URL.
     * @param url URL; URL to file
     * @throws XmlParserException if URL cannot be parsed
     */
    public final void apply(final URL url) throws XmlParserException
    {
        apply(url, new ODOptions());
    }

    /**
     * Applies demand from URL using OD options.
     * @param url URL; URL to file
     * @param odOptions ODOptions; OD options
     * @throws XmlParserException if URL cannot be parsed
     */
    public void apply(final URL url, final ODOptions odOptions) throws XmlParserException
    {
        try
        {
            apply(url.openStream(), odOptions);
        }
        catch (IOException exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Applies demand from stream.
     * @param stream InputStream; stream
     * @throws XmlParserException if URL cannot be parsed
     */
    public final void apply(final InputStream stream) throws XmlParserException
    {
        apply(stream, new ODOptions());
    }

    /**
     * Applies demand from stream using OD options.
     * @param stream InputStream; stream
     * @param odOptions ODOptions; OD options
     * @throws XmlParserException if URL cannot be parsed
     */
    public final void apply(final InputStream stream, final ODOptions odOptions) throws XmlParserException
    {
        applyOD(build(stream), odOptions);
    }

    /**
     * Applies demand from xml node.
     * @param xmlNode Node; node
     * @throws XmlParserException if URL cannot be parsed
     */
    public final void apply(final Node xmlNode) throws XmlParserException
    {
        apply(xmlNode, new ODOptions());
    }

    /**
     * Applies demand from URL using OD options.
     * @param xmlNode Node; node
     * @param odOptions ODOptions; OD options
     * @throws XmlParserException if URL cannot be parsed
     */
    public void apply(final Node xmlNode, final ODOptions odOptions) throws XmlParserException
    {
        applyOD(build(xmlNode), odOptions);
    }

    /**
     * Applies the OD to the network.
     * @param od ODMatrix; OD matrix
     * @param odOptions ODOptions; options
     * @throws XmlParserException if the ODApplier fails
     */
    private void applyOD(final ODMatrix od, final ODOptions odOptions) throws XmlParserException
    {
        try
        {
            ODApplier.applyOD(this.network, od, this.simulator, odOptions);
        }
        catch (ParameterException | SimRuntimeException exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Build demand from URL.
     * @param url URL; URL to file
     * @return ODMatrix; OD matrix
     * @throws XmlParserException if URL cannot be parsed
     */
    public final ODMatrix build(final URL url) throws XmlParserException
    {
        try
        {
            return build(url.openStream());
        }
        catch (IOException exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Build demand from stream.
     * @param stream InputStream; stream
     * @return ODMatrix; OD matrix
     * @throws XmlParserException if stream cannot be parsed
     */
    public final ODMatrix build(final InputStream stream) throws XmlParserException
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);
            return build(document.getDocumentElement());
        }
        catch (ParserConfigurationException | SAXException | IOException exception)
        {
            throw new XmlParserException(exception);
        }
    }

    /**
     * Build demand from xml node.
     * @param xmlNode Node; node
     * @return ODMatrix; OD matrix
     * @throws XmlParserException if stream cannot be parsed
     */
    public final ODMatrix build(final Node xmlNode) throws XmlParserException
    {
        Throw.when(!xmlNode.getNodeName().equals("OD"), XmlParserException.class,
                "OD should be parsed from a node OD, found %s instead.", xmlNode.getNodeName());

        // name
        NamedNodeMap attributes = xmlNode.getAttributes();
        Node nameNode = attributes.getNamedItem("NAME");
        String name = nameNode == null ? "xml od" : nameNode.getNodeValue().trim();

        // global interpolation
        Node globalInterpolationNode = attributes.getNamedItem("GLOBALINTERPOLATION");
        if (globalInterpolationNode != null)
        {
            String globalInterpolationString = globalInterpolationNode.getNodeValue().trim();
            try
            {
                this.globalInterpolation = Interpolation.valueOf(globalInterpolationNode.getNodeValue().trim());
            }
            catch (IllegalArgumentException exception)
            {
                throw new XmlParserException("INTERPOLATION " + globalInterpolationString + " does not exist.", exception);
            }
        }
        else
        {
            this.globalInterpolation = Interpolation.LINEAR;
        }

        // global factor
        Node globalFractionNode = attributes.getNamedItem("GLOBALFACTOR");
        Double globalFraction = null;
        if (globalFractionNode != null)
        {
            String globalFractionString = globalFractionNode.getNodeValue().trim();
            globalFraction = DemandTag.parseFactor(globalFractionString);
        }

        // parse data
        // TODO global time as optional
        // TODO order of time values, and demand values later, is not guaranteed in xml, need a way to order them
        NodeList odNodeList = xmlNode.getChildNodes();
        List<Node> nodes = XMLParser.getNodes(odNodeList, "GLOBALTIME");
        Throw.when(nodes.size() > 1, XmlParserException.class, "Multiple GLOBALTIME tags, only 1 is allowed.");
        SortedSet<Time> timeSet = new TreeSet<>();
        if (!nodes.isEmpty())
        {
            for (Node timeNode : XMLParser.getNodes(nodes.get(0).getChildNodes(), "TIME"))
            {
                NamedNodeMap timeAttributes = timeNode.getAttributes();
                Node valueNode = timeAttributes.getNamedItem("VALUE");
                Throw.when(valueNode == null, XmlParserException.class, "LEVEL tag is missing VALUE attribute.");
                timeSet.add(Try.assign(() -> Time.valueOf(valueNode.getNodeValue()), XmlParserException.class,
                        "Unable to parse time %s.", valueNode.getNodeValue()));
            }
        }
        double[] timeArray = new double[timeSet.size()];
        Iterator<Time> it = timeSet.iterator();
        for (int i = 0; i < timeSet.size(); i++)
        {
            timeArray[i] = it.next().si;
        }
        this.globalTime = Try.assign(() -> DoubleVector.instantiate(timeArray, TimeUnit.DEFAULT, StorageType.DENSE),
                XmlParserException.class, "Unexpected exception while creating global time vector.");
        CategoryTag.parse(odNodeList, this);
        DemandTag.parse(odNodeList, this);

        // create OD matrix
        List<org.opentrafficsim.core.network.Node> origins = new ArrayList<>();
        List<org.opentrafficsim.core.network.Node> destinations = new ArrayList<>();
        for (Object oKey : this.demand.getKeys())
        {
            origins.add((org.opentrafficsim.core.network.Node) oKey);
            for (Object dKey : this.demand.getChild(oKey).getKeys())
            {
                if (!destinations.contains(dKey))
                {
                    destinations.add((org.opentrafficsim.core.network.Node) dKey);
                }
            }
        }
        ODMatrix odMatrix =
                new ODMatrix(name, origins, destinations, this.categorization, this.globalTime, this.globalInterpolation);

        // add demand
        for (org.opentrafficsim.core.network.Node origin : origins)
        {
            for (org.opentrafficsim.core.network.Node destination : destinations)
            {
                Set<DemandTag> set = this.demand.getValue(() -> new LinkedHashSet<>(), origin, destination);
                if (!set.isEmpty())
                {
                    // add demand
                    DemandTag main = null;
                    if (!this.categorization.equals(Categorization.UNCATEGORIZED))
                    {
                        for (DemandTag tag : set)
                        {
                            if (tag.category == null)
                            {
                                Throw.when(main != null, XmlParserException.class,
                                        "Multiple DEMAND tags define main demand from %s to %s.", origin.getId(),
                                        destination.getId());
                                Throw.when(set.size() == 1, XmlParserException.class,
                                        "Categorized demand from %s to %s has single DEMAND, and without category.",
                                        origin.getId(), destination.getId());
                                main = tag;
                            }
                        }
                    }
                    for (DemandTag tag : set)
                    {
                        Throw.when(this.categorization.equals(Categorization.UNCATEGORIZED) && set.size() > 1,
                                XmlParserException.class, "Multiple DEMAND tags define demand from %s to %s.", origin.getId(),
                                destination.getId());
                        if (tag.equals(main))
                        {
                            // skip main demand itself
                            continue;
                        }
                        TimeVector timeVector = tag.timeVector == null
                                ? (main == null || main.timeVector == null ? this.globalTime : main.timeVector)
                                : tag.timeVector;
                        Interpolation interpolation = tag.interpolation == null
                                ? (main == null || main.interpolation == null ? this.globalInterpolation : main.interpolation)
                                : tag.interpolation;
                        // category
                        Category category = this.categorization.equals(Categorization.UNCATEGORIZED) ? Category.UNCATEGORIZED
                                : tag.category;
                        // sort out factor
                        Double factor = globalFraction != null ? globalFraction : null;
                        Double mainFraction = main == null ? null : main.factor;
                        factor = nullMultiply(factor, mainFraction);
                        factor = nullMultiply(factor, tag.factor);
                        FrequencyVector demandVector = tag.demandVector;
                        if (demandVector == null)
                        {
                            demandVector = main.demandVector;
                        }
                        if (tag.factors != null)
                        {
                            double[] factors;
                            if (factor == null)
                            {
                                factors = tag.factors;
                            }
                            else
                            {
                                factors = new double[tag.factors.length];
                                for (int i = 0; i < tag.factors.length; i++)
                                {
                                    factors[i] = tag.factors[i] * factor;
                                }
                            }
                            odMatrix.putDemandVector(origin, destination, category, demandVector, timeVector, interpolation,
                                    factors);
                        }
                        else if (factor != null)
                        {
                            odMatrix.putDemandVector(origin, destination, category, demandVector, timeVector, interpolation,
                                    factor);
                        }
                        else
                        {
                            odMatrix.putDemandVector(origin, destination, category, demandVector, timeVector, interpolation);
                        }
                    }
                }
            }
        }

        return odMatrix;
    }

    /**
     * Returns the multiplication of two values, taking 1.0 for {@code null} and returning {@code null} if both {@code null}.
     * @param d1 Double; Double value 1
     * @param d2 Double; Double value 2
     * @return multiplication of two values, taking 1.0 for {@code null} and returning {@code null} if both {@code null}
     */
    final static Double nullMultiply(final Double d1, final Double d2)
    {
        if (d1 == null)
        {
            return d2;
        }
        if (d2 == null)
        {
            return d1;
        }
        return d1 * d2;
    }

}
