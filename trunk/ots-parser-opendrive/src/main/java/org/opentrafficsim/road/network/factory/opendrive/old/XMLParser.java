package org.opentrafficsim.road.network.factory.opendrive.old;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
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
}
