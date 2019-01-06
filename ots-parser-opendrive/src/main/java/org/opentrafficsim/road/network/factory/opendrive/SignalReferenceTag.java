package org.opentrafficsim.road.network.factory.opendrive;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
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
class SignalReferenceTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Parameter s. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length s = null;

    /** Parameter t. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length t = null;

    /** Id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** Orientation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String orientation = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param node Node; the node with signal information
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
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
            signaReferencelTag.s = new Length(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

        Node t = attributes.getNamedItem("t");
        if (t != null)
            signaReferencelTag.t = new Length(Double.parseDouble(t.getNodeValue().trim()), LengthUnit.METER);

        Node id = attributes.getNamedItem("id");
        if (id != null)
            signaReferencelTag.id = id.getNodeValue().trim();

        Node orientation = attributes.getNamedItem("orientation");
        if (orientation != null)
            signaReferencelTag.orientation = orientation.getNodeValue().trim();

        return signaReferencelTag;

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SignalReferenceTag [s=" + this.s + ", t=" + this.t + ", id=" + this.id + ", orientation=" + this.orientation
                + "]";
    }
}
