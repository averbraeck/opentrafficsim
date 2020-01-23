package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SignalTag implements Serializable
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

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Dynamic. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String dynamic = null;

    /** Orientation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String orientation = null;

    /** The zOffset. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length zOffset = null;

    /** Country. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String country = null;

    /** Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;

    /** Sub type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String subtype = null;

    /** Value. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String value = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param node Node; the node with signal information
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @return the constructed SignalTag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static SignalTag parseSignal(final Node node, final OpenDriveNetworkLaneParserOld parser) throws SAXException, NetworkException
    {
        SignalTag signalTag = new SignalTag();
        NamedNodeMap attributes = node.getAttributes();

        Node s = attributes.getNamedItem("s");
        if (s != null)
            signalTag.s = new Length(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

        Node t = attributes.getNamedItem("t");
        if (t != null)
            signalTag.t = new Length(Double.parseDouble(t.getNodeValue().trim()), LengthUnit.METER);

        Node id = attributes.getNamedItem("id");
        if (id != null)
            signalTag.id = id.getNodeValue().trim();

        Node name = attributes.getNamedItem("name");
        if (name != null)
            signalTag.name = name.getNodeValue().trim();

        Node dynamic = attributes.getNamedItem("dynamic");
        if (dynamic != null)
            signalTag.dynamic = dynamic.getNodeValue().trim();

        Node orientation = attributes.getNamedItem("orientation");
        if (orientation != null)
            signalTag.orientation = orientation.getNodeValue().trim();

        Node zOffset = attributes.getNamedItem("zOffset");
        if (zOffset != null)
            signalTag.zOffset = new Length(Double.parseDouble(zOffset.getNodeValue().trim()), LengthUnit.METER);

        Node country = attributes.getNamedItem("country");
        if (country != null)
            signalTag.country = country.getNodeValue().trim();

        Node type = attributes.getNamedItem("type");
        if (type != null)
            signalTag.type = type.getNodeValue().trim();

        Node subtype = attributes.getNamedItem("subtype");
        if (subtype != null)
            signalTag.subtype = subtype.getNodeValue().trim();

        Node value = attributes.getNamedItem("value");
        if (value != null)
            signalTag.value = value.getNodeValue().trim();

        return signalTag;

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SignalTag [s=" + this.s + ", t=" + this.t + ", id=" + this.id + ", name=" + this.name + ", dynamic="
                + this.dynamic + ", orientation=" + this.orientation + ", zOffset=" + this.zOffset + ", country=" + this.country
                + ", type=" + this.type + ", subtype=" + this.subtype + ", value=" + this.value + "]";
    }
}
