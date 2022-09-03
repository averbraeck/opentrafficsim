package org.opentrafficsim.road.network.factory.xml.demand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.djutils.exceptions.Throw;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class XMLParser
{
    /** Utility class. */
    private XMLParser()
    {
        // do not instantiate
    }

    /**
     * @param nodeList NodeList; the list of nodes to process
     * @param tag String; the tag to look for, e.g., LINK
     * @return the nodes (which can contain nodeLists themselves) with the given tag
     */
    public static List<Node> getNodes(final NodeList nodeList, final String tag)
    {
        List<Node> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            if (node instanceof Element)
            {
                if (tag.equals(node.getNodeName()))
                {
                    result.add(node);
                }
            }
        }
        return result;
    }

    /**
     * Returns nodes sorted by attributes and/or value. Nodes are sorted according to the given attribute order. In this order
     * the node value can used by providing {@code null}.
     * @param nodeList NodeList; the list of nodes to process
     * @param tag String; the tag to look for, e.g., LINK
     * @param sortAttributes String...; list of attributes, which may contain {@code null} to use the node value
     * @return sorted nodes by attributes and/or value
     */
    public static List<Node> getNodesSorted(final NodeList nodeList, final String tag, final String... sortAttributes)
    {
        List<Node> result = getNodes(nodeList, tag);
        Collections.sort(result, new Comparator<Node>()
        {
            /** {@inheritDoc} */
            @Override
            public int compare(final Node o1, final Node o2)
            {
                for (String attribute : sortAttributes)
                {
                    if (attribute == null)
                    {
                        Throw.when(o1.getNodeValue() == null || o2.getNodeValue() == null, RuntimeException.class,
                                "Tag %s cannot be sorted using it's value as tags without value are encountered.", tag);
                        return o1.getNodeValue().compareTo(o2.getNodeValue());
                    }
                    Node n1 = o1.getAttributes().getNamedItem(attribute);
                    Node n2 = o2.getAttributes().getNamedItem(attribute);
                    int order = 0;
                    if (n1 != null && n2 != null)
                    {
                        String attr1 = n1.getNodeValue();
                        String attr2 = n2.getNodeValue();
                        if (attr1 == null && attr2 != null)
                        {
                            order = -1;
                        }
                        else if (attr1 == null)
                        {
                            order = 0;
                        }
                        else if (attr2 == null)
                        {
                            order = 1;
                        }
                        else
                        {
                            order = attr1.compareTo(attr2);
                        }
                        if (order != 0)
                        {
                            return order;
                        }
                    }
                }
                return 0;
            }
        });
        return result;
    }
}
