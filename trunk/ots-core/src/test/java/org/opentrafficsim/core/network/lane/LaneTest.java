package org.opentrafficsim.core.network.lane;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.factory.Link;
import org.opentrafficsim.core.network.factory.Node;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Test the Lane class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 21 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneTest
{
    /**
     * Test the constructor.
     * @throws SimRuntimeException 
     * @throws RemoteException 
     * @throws NamingException 
     */
    @Test
    public void laneConstructorTest() throws RemoteException, SimRuntimeException, NamingException
    {
        // First we need two Nodes
        Node nodeFrom = new Node("AFrom", new Coordinate(0, 0, 0));
        Node nodeTo = new Node("ATo", new Coordinate(1000, 0, 0));
        // Now we can make a Link
        Coordinate[] coordinates = new Coordinate[2];
        coordinates[0] = new Coordinate(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new Coordinate(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        GeometryFactory factory = new GeometryFactory();
        LineString lineString = factory.createLineString(coordinates);
        Link link =
                new Link("AtoB", nodeFrom, nodeTo, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(), LengthUnit.METER));
        DoubleScalar.Rel<LengthUnit> startLateralPos
        // Now we can construct a Lane
        new Lane(link, latPos, xxxx, width, width, laneType, LongitudinalDirectionality.FORWARD, f2000);


    }

}
