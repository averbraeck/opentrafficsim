package org.opentrafficsim.core.network;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.mock.MockSimulator;

/**
 * Test the LinkDirection class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 2, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinkDirectionTest
{

    /**
     * Test the constructor and the getters of the LinkDirection class.
     * @throws NetworkException this test has failed if that happens uncaught
     * @throws OTSGeometryException this test has failed if that happens uncaught
     */
    @Test
    public final void linkDirectionTests() throws NetworkException, OTSGeometryException
    {
        OTSPoint3D fromPoint = new OTSPoint3D(100, 200, 300);
        OTSPoint3D toPoint = new OTSPoint3D(1000, 2000, 330);
        OTSNetwork network =
                new OTSNetwork("testNetworkForCapacityOTSLink", true, MockSimulator.createMock());
        Node fromNode = new OTSNode(network, "startNode", fromPoint);
        Node toNode = new OTSNode(network, "endNode", toPoint);
        LinkType linkType = network.getLinkType(LinkType.DEFAULTS.ROAD);
        OTSLine3D designLine = new OTSLine3D(fromPoint, toPoint);
        OTSSimulatorInterface simulator = MockSimulator.createMock();
        Link link = new OTSLink(network, "link", fromNode, toNode, linkType, designLine);
        LinkDirection ld = new LinkDirection(link, GTUDirectionality.DIR_PLUS);
        assertTrue(ld.getLink().equals(link));
        assertTrue(ld.getDirection().equals(GTUDirectionality.DIR_PLUS));
        assertTrue(ld.getNodeFrom().equals(fromNode));
        assertTrue(ld.getNodeTo().equals(toNode));
        ld = new LinkDirection(link, GTUDirectionality.DIR_MINUS);
        assertTrue(ld.getLink().equals(link));
        assertTrue(ld.getDirection().equals(GTUDirectionality.DIR_MINUS));
        assertTrue(ld.getNodeFrom().equals(toNode));
        assertTrue(ld.getNodeTo().equals(fromNode));
        assertTrue(ld.toString().contains(link.getId()));
    }

}
