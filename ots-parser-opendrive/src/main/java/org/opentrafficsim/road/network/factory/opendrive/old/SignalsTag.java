package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class SignalsTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** Signal tags */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<SignalTag> signalTags = new ArrayList<SignalTag>();

    /** Signal reference tag */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<SignalReferenceTag> signalReferenceTag = new ArrayList<SignalReferenceTag>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param roadTag RoadTag; the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseSignals(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final RoadTag roadTag)
            throws SAXException, NetworkException
    {
        SignalsTag signalsTag = new SignalsTag();
        for (Node node0 : XMLParser.getNodes(nodeList, "signals"))
        {
            for (Node node : XMLParser.getNodes(node0.getChildNodes(), "signal"))
            {
                SignalTag signalTag = SignalTag.parseSignal(node, parser);
                signalsTag.signalTags.add(signalTag);
                parser.signalTags.put(signalTag.id, signalTag);
            }
            for (Node node : XMLParser.getNodes(node0.getChildNodes(), "signalReference"))
            {
                SignalReferenceTag signalReferenceTag = SignalReferenceTag.parseSignalReference(node, parser);
                signalsTag.signalReferenceTag.add(signalReferenceTag);
            }
        }
        roadTag.signalsTag = signalsTag;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SignalsTag [signalTags=" + this.signalTags + ", signalReferenceTag=" + this.signalReferenceTag + "]";
    }
}
