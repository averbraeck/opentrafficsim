package org.opentrafficsim.road.network.factory.opendrive;

import java.io.Serializable;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Angle;
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
class ObjectTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Parameter s. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel s = null;

    /** Parameter t. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel t = null;

    /** Id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Orientation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String orientation = null;

    /** The zOffset. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel zOffset = null;

    /** Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;

    /** Valid length. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel validLength = null;

    /** Length. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel length = null;

    /** Width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel width = null;

    /** Height. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel height = null;

    /** The hdg. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle.Rel hdg = null;

    /** Pitch. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle.Rel pitch = null;

    /** Roll. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle.Rel roll = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param node the node with signal information
     * @param parser the parser with the lists of information
     * @return the constructed SignalTag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static ObjectTag parseObject(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException,
        NetworkException
    {
        ObjectTag objectTag = new ObjectTag();
        NamedNodeMap attributes = node.getAttributes();

        Node s = attributes.getNamedItem("s");
        if (s != null)
            objectTag.s = new Length.Rel(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

        Node t = attributes.getNamedItem("t");
        if (t != null)
            objectTag.t = new Length.Rel(Double.parseDouble(t.getNodeValue().trim()), LengthUnit.METER);

        Node id = attributes.getNamedItem("id");
        if (id != null)
            objectTag.id = id.getNodeValue().trim();

        Node name = attributes.getNamedItem("name");
        if (name != null)
            objectTag.name = name.getNodeValue().trim();

        Node orientation = attributes.getNamedItem("orientation");
        if (orientation != null)
            objectTag.orientation = orientation.getNodeValue().trim();

        Node zOffset = attributes.getNamedItem("zOffset");
        if (zOffset != null)
            objectTag.zOffset = new Length.Rel(Double.parseDouble(zOffset.getNodeValue().trim()), LengthUnit.METER);

        Node type = attributes.getNamedItem("type");
        if (type != null)
            objectTag.type = type.getNodeValue().trim();

        Node validLength = attributes.getNamedItem("validLength");
        if (validLength != null)
            objectTag.validLength =
                new Length.Rel(Double.parseDouble(validLength.getNodeValue().trim()), LengthUnit.METER);

        Node length = attributes.getNamedItem("length");
        if (length != null)
            objectTag.length = new Length.Rel(Double.parseDouble(length.getNodeValue().trim()), LengthUnit.METER);

        Node width = attributes.getNamedItem("width");
        if (width != null)
            objectTag.width = new Length.Rel(Double.parseDouble(width.getNodeValue().trim()), LengthUnit.METER);

        Node height = attributes.getNamedItem("height");
        if (height != null)
            objectTag.height = new Length.Rel(Double.parseDouble(height.getNodeValue().trim()), LengthUnit.METER);

        Node hdg = attributes.getNamedItem("hdg");
        if (hdg != null)
            objectTag.hdg = new Angle.Rel(Double.parseDouble(hdg.getNodeValue().trim()), AngleUnit.RADIAN);

        Node pitch = attributes.getNamedItem("pitch");
        if (pitch != null)
            objectTag.pitch = new Angle.Rel(Double.parseDouble(pitch.getNodeValue().trim()), AngleUnit.RADIAN);

        Node roll = attributes.getNamedItem("roll");
        if (roll != null)
            objectTag.roll = new Angle.Rel(Double.parseDouble(roll.getNodeValue().trim()), AngleUnit.RADIAN);

        return objectTag;

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ObjectTag [s=" + this.s + ", t=" + this.t + ", id=" + this.id + ", name=" + this.name + ", orientation="
                + this.orientation + ", zOffset=" + this.zOffset + ", type=" + this.type + ", validLength=" + this.validLength
                + ", length=" + this.length + ", width=" + this.width + ", height=" + this.height + ", hdg=" + this.hdg
                + ", pitch=" + this.pitch + ", roll=" + this.roll + "]";
    }
}
