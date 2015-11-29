package org.opentrafficsim.road.network.factory.opendrive;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class SignalReferenceTag 
{
    /** s. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel s = null;

    /** t. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel t = null;
    
    /** id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;    
    
    /** orientation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String orientation = null;
    

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param node the node with signal information
     * @param parser the parser with the lists of information
     * @return the constructed SignalTag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static SignalReferenceTag parseSignalReference(final Node node, final OpenDriveNetworkLaneParser parser)
        throws SAXException, NetworkException
    {
        SignalReferenceTag signaReferencelTag = new SignalReferenceTag();
        NamedNodeMap attributes = node.getAttributes();

        Node s = attributes.getNamedItem("s");
        if (s != null)
            signaReferencelTag.s = new Length.Rel(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

        Node t = attributes.getNamedItem("t");
        if (t != null)
            signaReferencelTag.t = new Length.Rel(Double.parseDouble(t.getNodeValue().trim()), LengthUnit.METER);

        Node id = attributes.getNamedItem("id");
        if (id != null)
            signaReferencelTag.id = id.getNodeValue().trim();

        Node orientation = attributes.getNamedItem("orientation");
        if (orientation != null)
            signaReferencelTag.orientation = orientation.getNodeValue().trim();

        return signaReferencelTag;

    }
}
