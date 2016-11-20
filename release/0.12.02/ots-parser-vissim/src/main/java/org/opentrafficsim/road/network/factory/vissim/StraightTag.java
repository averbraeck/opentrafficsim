package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class StraightTag implements Serializable {
    /** */
    private static final long serialVersionUID = 20150724L;

    /** Length. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length length = null;

    /**
     * Parse the LINK.STRAIGHT tag.
     * @param straightNode the XML-node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseStraight(final String coords, final VissimNetworkLaneParser parser, final LinkTag linkTag)
        throws SAXException, NetworkException {
        linkTag.straightTag = new StraightTag();

    }

    /**
     *
     */
    public StraightTag() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param length
     */
    public StraightTag(StraightTag straightTag) {
        if (straightTag != null) {
            this.length = straightTag.length;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return "StraightTag [length=" + this.length + "]";
    }
}
