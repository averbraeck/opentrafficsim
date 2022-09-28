package org.opentrafficsim.road.network.factory.xml.demand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Category.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CategoryTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20180525L;

    /** GTU type. */
    private GTUType gtuType;

    /** Route. */
    private Route route;

    /** Lane. */
    private Lane lane;

    /** FACTOR. */
    Double factor;

    /** Created category. */
    private Category category;

    /**
     * Parse category nodes.
     * @param nodeList NodeList; node list
     * @param parser XmlOdParser; parser
     * @throws XmlParserException if category cannot be parsed
     */
    static void parse(final NodeList nodeList, final XmlOdParser parser) throws XmlParserException
    {
        List<Class<?>> categorizationClasses = new ArrayList<>();
        for (Node node : XMLParser.getNodesSorted(nodeList, "CATEGORY", "GTUTYPE", "ROUTE", "LANE"))
        {
            NamedNodeMap attributes = node.getAttributes();
            CategoryTag tag = new CategoryTag();

            Node nameNode = attributes.getNamedItem("NAME");
            Throw.when(nameNode == null, XmlParserException.class, "Missing NAME attribute in CATEGORY tag.");
            String name = nameNode.getNodeValue().trim();

            Node gtuTypeNode = attributes.getNamedItem("GTUTYPE");
            Throw.when(categorizationClasses.contains(GTUType.class) && gtuTypeNode == null, XmlParserException.class,
                    "Missing GTUTYPE attribute in CATEGORY %s.", name);
            Throw.when(
                    !categorizationClasses.isEmpty() && !categorizationClasses.contains(GTUType.class) && gtuTypeNode != null,
                    XmlParserException.class, "Missing GTUTYPE attribute in a CATEGORY prior to %s.", name);
            if (gtuTypeNode != null)
            {
                tag.gtuType = parser.getGTUType(gtuTypeNode.getNodeValue().trim());
            }

            Node routeNode = attributes.getNamedItem("ROUTE");
            Throw.when(categorizationClasses.contains(Route.class) && routeNode == null, XmlParserException.class,
                    "Missing ROUTE attribute in CATEGORY %s.", name);
            Throw.when(!categorizationClasses.isEmpty() && !categorizationClasses.contains(Route.class) && routeNode != null,
                    XmlParserException.class, "Missing ROUTE attribute in a CATEGORY prior to %s.", name);
            if (routeNode != null)
            {
                String routeId = routeNode.getNodeValue().trim();
                tag.route = parser.network.getRoute(routeId);
                Throw.when(tag.route == null, XmlParserException.class, "Route %s is not available.", routeId);
            }

            Node laneNode = attributes.getNamedItem("LANE");
            Throw.when(categorizationClasses.contains(Lane.class) && laneNode == null, XmlParserException.class,
                    "Missing LANE attribute in CATEGORY %s.", name);
            Throw.when(!categorizationClasses.isEmpty() && !categorizationClasses.contains(Lane.class) && laneNode != null,
                    XmlParserException.class, "Missing LANE attribute in a CATEGORY prior to %s.", name);
            if (laneNode != null)
            {
                String laneId = laneNode.getNodeValue().trim();
                // find lane
                for (Link link : parser.network.getLinkMap().values())
                {
                    if (link instanceof CrossSectionLink)
                    {
                        for (Lane lane : ((CrossSectionLink) link).getLanes())
                        {
                            if (lane.getFullId().equals(laneId))
                            {
                                tag.lane = lane;
                                break;
                            }
                        }
                    }
                    if (tag.lane != null)
                    {
                        break;
                    }
                }
                Throw.when(tag.lane == null, XmlParserException.class,
                        "Lane %s is not available. Make sure to use the full id 'LinkId.LaneId'.", laneId);
            }

            Node factorNode = attributes.getNamedItem("FACTOR");
            if (factorNode != null)
            {
                tag.factor = DemandTag.parseFactor(factorNode.getNodeValue().trim());
            }

            // define categorization classes
            if (categorizationClasses.isEmpty())
            {
                if (tag.gtuType != null)
                {
                    categorizationClasses.add(GTUType.class);
                }
                if (tag.route != null)
                {
                    categorizationClasses.add(Route.class);
                }
                if (tag.lane != null)
                {
                    categorizationClasses.add(Lane.class);
                }
                Throw.when(categorizationClasses.isEmpty(), XmlParserException.class, "Category contains no objects.");
            }

            // store category tag
            parser.categories.put(name, tag);
        }

        // create categorization
        if (categorizationClasses.isEmpty())
        {
            parser.categorization = Categorization.UNCATEGORIZED;
        }
        else if (categorizationClasses.size() > 1)
        {
            parser.categorization = new Categorization("od categorization", categorizationClasses.get(0), categorizationClasses
                    .subList(1, categorizationClasses.size()).toArray(new Class<?>[categorizationClasses.size() - 1]));
        }
        else
        {
            parser.categorization = new Categorization("od categorization", categorizationClasses.get(0));
        }
    }

    /**
     * Returns the category.
     * @param categorization Categorization; categorization
     * @return Category; category
     */
    public Category getCategory(final Categorization categorization)
    {
        if (this.category == null)
        {
            int n = 0;
            if (this.gtuType != null)
            {
                n++;
            }
            if (this.route != null)
            {
                n++;
            }
            if (this.lane != null)
            {
                n++;
            }
            Object first = null;
            Object[] objects = new Object[n - 1];
            int i = 0;
            if (this.gtuType != null)
            {
                first = this.gtuType;
            }
            if (this.route != null)
            {
                if (first == null)
                {
                    first = this.gtuType;
                }
                else
                {
                    objects[i] = this.route;
                    i++;
                }
            }
            if (this.lane != null)
            {
                if (first == null)
                {
                    first = this.gtuType;
                }
                else
                {
                    objects[i] = this.lane;
                }
            }
            this.category = new Category(categorization, first, objects);
        }
        return this.category;
    }

}
