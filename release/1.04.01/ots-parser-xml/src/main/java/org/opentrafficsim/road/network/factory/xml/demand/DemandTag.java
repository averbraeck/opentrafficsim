package org.opentrafficsim.road.network.factory.xml.demand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Demand.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 25 mei 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class DemandTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20180525L;

    /** Unit for storage. */
    private final static FrequencyUnit UNIT = FrequencyUnit.PER_HOUR;

    /** Origin. */
    org.opentrafficsim.core.network.Node origin;

    /** Destination. */
    org.opentrafficsim.core.network.Node destination;

    /** Interpolation. */
    Interpolation interpolation;

    /** Category. */
    Category category;

    /** Category name. */
    String categoryName;

    /** Demand type. */
    DemandType demandType = null;

    /** Time vector. */
    TimeVector timeVector;

    /** Demand vector. */
    FrequencyVector demandVector;

    /** Fraction. */
    Double factor;

    /** Fractions. */
    double[] factors;

    /**
     * Parse demand nodes.
     * @param nodeList NodeList; node list
     * @param parser XmlOdParser; parser
     * @throws XmlParserException if category cannot be parsed
     */
    static void parse(final NodeList nodeList, final XmlOdParser parser) throws XmlParserException
    {
        for (Node node : XMLParser.getNodesSorted(nodeList, "DEMAND", "ORIGIN", "DESTINATION", "CATEGORY"))
        {
            NamedNodeMap attributes = node.getAttributes();
            DemandTag tag = new DemandTag();

            Node originNode = attributes.getNamedItem("ORIGIN");
            Throw.when(originNode == null, XmlParserException.class, "Missing ORIGIN attribute in DEMAND tag.");
            String originId = originNode.getNodeValue().trim();
            tag.origin = parser.network.getNode(originId);
            Throw.when(tag.origin == null, XmlParserException.class, "Origin %s is not available.", originId);

            Node destinationNode = attributes.getNamedItem("DESTINATION");
            Throw.when(destinationNode == null, XmlParserException.class, "Missing DESTINATION attribute in DEMAND tag.");
            String destinationId = destinationNode.getNodeValue().trim();
            tag.destination = parser.network.getNode(destinationId);
            Throw.when(tag.destination == null, XmlParserException.class, "Destination %s is not available.", destinationId);

            Node interpolationNode = attributes.getNamedItem("INTERPOLATION");
            if (interpolationNode != null)
            {
                String interpolation = interpolationNode.getNodeValue().trim();
                try
                {
                    tag.interpolation = Interpolation.valueOf(interpolation);
                }
                catch (IllegalArgumentException exception)
                {
                    throw new XmlParserException("INTERPOLATION " + interpolation + " does not exist.", exception);
                }
            }

            Node factorNode = attributes.getNamedItem("FACTOR");
            if (factorNode != null)
            {
                tag.factor = parseFactor(factorNode.getNodeValue().trim());
            }

            Node categoryNode = attributes.getNamedItem("CATEGORY");
            if (categoryNode != null)
            {
                String categoryName = categoryNode.getNodeValue().trim();
                Throw.when(!parser.categories.containsKey(categoryName), XmlParserException.class,
                        "Category %s is not available.", categoryName);
                tag.category = parser.categories.get(categoryName).getCategory(parser.categorization);
                tag.factor = XmlOdParser.nullMultiply(tag.factor, parser.categories.get(categoryName).factor);
            }

            NodeList childList = node.getChildNodes();
            List<Double> timeList = new ArrayList<>();
            List<Double> valueList = new ArrayList<>();
            List<Node> demandNodes = XMLParser.getNodes(childList, "LEVEL");
            Throw.when(categoryNode == null && demandNodes.isEmpty(), XmlParserException.class,
                    "DEMAND without CATEGORY attribute should contain demand data.");
            if (demandNodes.size() == 0)
            {
                tag.demandType = DemandType.FACTOR;
            }
            for (Node level : demandNodes)
            {
                if (tag.demandType == null)
                {
                    tag.demandType = DemandType.fromLevelNode(level);
                    Throw.when(
                            categoryNode == null && !tag.demandType.equals(DemandType.FREQUENCIES)
                                    && !tag.demandType.equals(DemandType.TIMED_FREQUENCIES),
                            XmlParserException.class,
                            "DEMAND without CATEGORY attribute should contain non-factoral demand data.");
                }
                NamedNodeMap levelAttributes = level.getAttributes();
                if (tag.demandType.equals(DemandType.TIMED_FACTORS) || tag.demandType.equals(DemandType.TIMED_FREQUENCIES))
                {
                    Node timeNode = levelAttributes.getNamedItem("TIME");
                    Throw.when(timeNode == null, XmlParserException.class, "A LEVEL tag is missing attribute TIME.");
                    String timeString = timeNode.getNodeValue().trim();
                    Time time = Try.assign(() -> Time.valueOf(timeString), XmlParserException.class,
                            "Unable to parse %s as time.", timeString);
                    timeList.add(time.si);
                }
                Node valueNode = levelAttributes.getNamedItem("VALUE");
                Throw.when(valueNode == null, XmlParserException.class, "A LEVEL tag is missing attribute VALUE.");
                if (tag.demandType.equals(DemandType.TIMED_FACTORS) || tag.demandType.equals(DemandType.FACTORS))
                {
                    valueList.add(parseFactor(valueNode.getNodeValue().trim()));
                }
                if (tag.demandType.equals(DemandType.TIMED_FREQUENCIES) || tag.demandType.equals(DemandType.FREQUENCIES))
                {
                    String valueString = valueNode.getNodeValue().trim().toLowerCase();
                    valueList.add(Try.assign(() -> Frequency.valueOf(valueString.replace("veh", "")).getInUnit(UNIT),
                            "Unable to parse %s as frequency.", valueString));
                }
            }
            // time vector
            if (tag.demandType.equals(DemandType.TIMED_FACTORS) || tag.demandType.equals(DemandType.TIMED_FREQUENCIES))
            {
                tag.timeVector = Try.assign(
                        () -> DoubleVector.instantiate(timeList.stream().mapToDouble(d -> d).toArray(), TimeUnit.DEFAULT, StorageType.DENSE),
                        "Unexpected exception while converting list of time values to an array.");
            }
            // factor or demand vector
            if (tag.demandType.equals(DemandType.TIMED_FACTORS) || tag.demandType.equals(DemandType.FACTORS))
            {
                tag.factors = valueList.stream().mapToDouble(d -> d).toArray();
            }
            else if (tag.demandType.equals(DemandType.TIMED_FREQUENCIES) || tag.demandType.equals(DemandType.FREQUENCIES))
            {
                tag.demandVector = Try.assign(
                        () -> DoubleVector.instantiate(valueList.stream().mapToDouble(d -> d).toArray(), UNIT, StorageType.DENSE),
                        "Unexpected exception while converting list of time values to an array.");
            }

            parser.demand.get(() -> new LinkedHashSet<>(), tag.origin, tag.destination).add(tag);
        }
    }

    /**
     * Parses a String of percentage or value to a double.
     * @param value String; value
     * @return double
     * @throws XmlParserException if factor is not in range from 0 to 1
     */
    public static double parseFactor(final String value) throws XmlParserException
    {
        double f;
        if (value.endsWith("%"))
        {
            f = Double.parseDouble(value.substring(0, value.length() - 1).trim()) / 100.0;
        }
        else
        {
            f = Double.parseDouble(value);
        }
        Throw.when(f < 0.0, XmlParserException.class, "Factor %d is not positive.", f);
        return f;
    }

    /**
     * Demand type. Demand may be defined in several tags. For instance, 1 tag may define time and demand, while other tags only
     * specify a factor of this demand applicable to a specific category.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 25 mei 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum DemandType
    {
        /** Demand given in other tag to which a factor applies. */
        FACTOR,

        /** Demand given in other tag to which a factors apply. */
        FACTORS,

        /** Timed factors that apply to demand in another tag. */
        TIMED_FACTORS,

        /** Frequencies that apply to the global time vector. */
        FREQUENCIES,

        /** Timed frequencies. */
        TIMED_FREQUENCIES;

        /**
         * Returns the demand type based on a LEVEL tag. This does not work on FACTOR, as no LEVEL tag should be defined then.
         * @param level Node; level of the node
         * @return demand type
         * @throws XmlParserException if the VALUE attribute is missing
         */
        public static DemandType fromLevelNode(final Node level) throws XmlParserException
        {
            NamedNodeMap attributes = level.getAttributes();
            Node timeNode = attributes.getNamedItem("TIME");
            Node valueNode = attributes.getNamedItem("VALUE");
            Throw.when(valueNode == null, XmlParserException.class, "LEVEL tag in DEMAND is missing attribute VALUE.");
            boolean frequency = valueNode.getNodeValue().trim().toLowerCase().contains("veh");
            if (timeNode != null)
            {
                if (frequency)
                {
                    return TIMED_FREQUENCIES;
                }
                return TIMED_FACTORS;
            }
            if (frequency)
            {
                return FREQUENCIES;
            }
            return FACTORS;
        }

    }

}
