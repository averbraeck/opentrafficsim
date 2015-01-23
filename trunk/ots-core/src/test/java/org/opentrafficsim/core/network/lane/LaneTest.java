package org.opentrafficsim.core.network.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.List;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.junit.Test;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.Link;
import org.opentrafficsim.core.network.factory.Node;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Test the Lane class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
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
     * @throws NetworkException
     */
    @Test
    public void laneConstructorTest() throws RemoteException, SimRuntimeException, NamingException, NetworkException
    {
        // First we need two Nodes
        Node nodeFrom = new Node("A", new Coordinate(0, 0, 0));
        Node nodeTo = new Node("B", new Coordinate(1000, 0, 0));
        // Now we can make a Link
        Coordinate[] coordinates = new Coordinate[2];
        coordinates[0] = new Coordinate(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new Coordinate(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        GeometryFactory factory = new GeometryFactory();
        LineString lineString = factory.createLineString(coordinates);
        Link link =
                new Link("A to B", nodeFrom, nodeTo, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(),
                        LengthUnit.METER));
        new LinearGeometry(link, lineString, null);
        DoubleScalar.Rel<LengthUnit> startLateralPos = new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> endLateralPos = new DoubleScalar.Rel<LengthUnit>(5, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> startWidth = new DoubleScalar.Rel<LengthUnit>(3, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> endWidth = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
        GTUType<String> gtuTypeCar = new GTUType<String>("Car");
        GTUType<String> gtuTypeTruck = new GTUType<String>("Truck");
        LaneType<String> laneType = new LaneType<String>("Car");
        laneType.addPermeability(gtuTypeCar);
        laneType.addPermeability(gtuTypeTruck);
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000, FrequencyUnit.PER_HOUR);
        // Now we can construct a Lane
        LongitudinalDirectionality longitudinalDirectionality = LongitudinalDirectionality.FORWARD;
        Lane lane =
                new Lane(link, startLateralPos, endLateralPos, startWidth, endWidth, laneType,
                        longitudinalDirectionality, f2000);
        // Verify the easy bits
        assertEquals("Capacity should be " + f2000, f2000.getSI(), lane.getCapacity().getSI(), 0.001);
        assertEquals("PrevLanes should be empty", 0, lane.prevLanes().size()); // this one caught a bug!
        assertEquals("NextLanes should be empty", 0, lane.nextLanes().size());
        double approximateLengthOfContour =
                2 * nodeFrom.getPoint().distance(nodeTo.getPoint()) + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane.getContour().getLength(), 0.1);
        assertEquals("Directionality should be " + longitudinalDirectionality, longitudinalDirectionality,
                lane.getDirectionality());
        assertEquals("There should be no GTUs on the lane", 0, lane.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane.getLaneType());
        List<Sensor> sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("The lane should have two sensors", 2, sensors.size());
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER));
        assertEquals("There should be one sensor at the start of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneStart",
                sensors.get(0) instanceof SensorLaneStart);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPosition().getSI(), 0.00001);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPositionSI(), 0.00001);
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(lane.getLength().getSI() - 1, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("There should be one sensor at the end of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneEnd",
                sensors.get(0) instanceof SensorLaneEnd);
        assertEquals("This sensor should be at the end of the lane", lane.getLength().getSI(), sensors.get(0)
                .getLongitudinalPosition().getSI(), 0.01);
        assertEquals("This sensor should be at the end of the lane", lane.getLength().getSI(), sensors.get(0)
                .getLongitudinalPositionSI(), 0.01);

        // Harder case; create a Link with form points along the way
        System.out.println("Constructing Link and lane with one form point");
        coordinates = new Coordinate[3];
        coordinates[0] = new Coordinate(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[1] = new Coordinate(200, 100);
        coordinates[2] = new Coordinate(nodeTo.getPoint().x, nodeTo.getPoint().y, 0);
        lineString = factory.createLineString(coordinates);
        link =
                new Link("A to B with Kink", nodeFrom, nodeTo, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(),
                        LengthUnit.METER));
        new LinearGeometry(link, lineString, null);
        lane =
                new Lane(link, startLateralPos, endLateralPos, startWidth, endWidth, laneType,
                        longitudinalDirectionality, f2000);
        // Verify the easy bits
        assertEquals("Capacity should be " + f2000, f2000.getSI(), lane.getCapacity().getSI(), 0.001);
        assertEquals("PrevLanes should be empty", 0, lane.prevLanes().size()); // this one caught a bug!
        assertEquals("NextLanes should be empty", 0, lane.nextLanes().size());
        approximateLengthOfContour =
                2 * (coordinates[0].distance(coordinates[1]) + coordinates[1].distance(coordinates[2]))
                        + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane.getContour().getLength(), 4); // This lane takes a path that is about 3m longer
        assertEquals("Directionality should be " + longitudinalDirectionality, longitudinalDirectionality,
                lane.getDirectionality());
        assertEquals("There should be no GTUs on the lane", 0, lane.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane.getLaneType());
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("The lane should have two sensors", 2, sensors.size());
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER));
        assertEquals("There should be one sensor at the start of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneStart",
                sensors.get(0) instanceof SensorLaneStart);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPosition().getSI(), 0.00001);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPositionSI(), 0.00001);
        sensors =
                lane.getSensors(new DoubleScalar.Rel<LengthUnit>(lane.getLength().getSI() - 1, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("There should be one sensor at the end of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneEnd",
                sensors.get(0) instanceof SensorLaneEnd);
        assertEquals("This sensor should be at the end of the lane", lane.getLength().getSI(), sensors.get(0)
                .getLongitudinalPosition().getSI(), 0.01);
        assertEquals("This sensor should be at the end of the lane", lane.getLength().getSI(), sensors.get(0)
                .getLongitudinalPositionSI(), 0.01);
        System.out.println("Add another Lane at the inside of the corner in the design line");
        DoubleScalar.Rel<LengthUnit> startLateralPos2 = new DoubleScalar.Rel<LengthUnit>(-8, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> endLateralPos2 = new DoubleScalar.Rel<LengthUnit>(-5, LengthUnit.METER);
        Lane lane2 =
                new Lane(link, startLateralPos2, endLateralPos2, startWidth, endWidth, laneType,
                        longitudinalDirectionality, f2000);
        // Verify the easy bits
        assertEquals("Capacity should be " + f2000, f2000.getSI(), lane2.getCapacity().getSI(), 0.001);
        assertEquals("PrevLanes should be empty", 0, lane2.prevLanes().size()); // this one caught a bug!
        assertEquals("NextLanes should be empty", 0, lane2.nextLanes().size());
        approximateLengthOfContour =
                2 * (coordinates[0].distance(coordinates[1]) + coordinates[1].distance(coordinates[2]))
                        + startWidth.getSI() + endWidth.getSI();
        assertEquals("Length of contour is approximately " + approximateLengthOfContour, approximateLengthOfContour,
                lane2.getContour().getLength(), 12); // This lane takes a path that is about 11 meters shorter
        assertEquals("Directionality should be " + longitudinalDirectionality, longitudinalDirectionality,
                lane2.getDirectionality());
        assertEquals("There should be no GTUs on the lane", 0, lane2.getGtuList().size());
        assertEquals("LaneType should be " + laneType, laneType, lane2.getLaneType());
        sensors =
                lane2.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("The lane should have two sensors", 2, sensors.size());
        sensors =
                lane2.getSensors(new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(0.1, LengthUnit.METER));
        assertEquals("There should be one sensor at the start of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneStart",
                sensors.get(0) instanceof SensorLaneStart);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPosition().getSI(), 0.00001);
        assertEquals("This sensor should be at 0m", 0, sensors.get(0).getLongitudinalPositionSI(), 0.00001);
        sensors =
                lane2.getSensors(new DoubleScalar.Rel<LengthUnit>(lane2.getLength().getSI() - 1, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(9999, LengthUnit.METER));
        assertEquals("There should be one sensor at the end of the lane", 1, sensors.size());
        assertTrue("The sensor at the start of the lane should be a SensorLaneEnd",
                sensors.get(0) instanceof SensorLaneEnd);
        assertEquals("This sensor should be at the end of the lane", lane2.getLength().getSI(), sensors.get(0)
                .getLongitudinalPosition().getSI(), 0.01);
        assertEquals("This sensor should be at the end of the lane", lane2.getLength().getSI(), sensors.get(0)
                .getLongitudinalPositionSI(), 0.01);
        // Now for the really hard case - circular Link with Lanes
        final int numberOfCoordinates = 10;
        coordinates = new Coordinate[numberOfCoordinates];
        //nodeFrom = new Node("newA", new Coordinate(-1000, -1000));
        coordinates[0] = new Coordinate(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        coordinates[numberOfCoordinates - 1] = new Coordinate(nodeFrom.getPoint().x, nodeFrom.getPoint().y, 0);
        double radius = 100;
        for (int index = 1; index < numberOfCoordinates - 1; index++)
        {
            double angle = Math.PI * 2 * index / (numberOfCoordinates - 1);
            coordinates[index] =
                    new Coordinate(nodeFrom.getPoint().x - radius + radius * Math.cos(angle), nodeFrom.getPoint().y
                            + radius * Math.sin(angle));
            //System.out.println(String.format("angle %6.3f %8.3f %8.3f", angle, coordinates[index].x, coordinates[index].y));
        }
        //CrossSectionElement.printCoordinates("Design coordinates of ring", coordinates);
        link =
                new Link("Ring", nodeFrom, nodeTo, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(),
                        LengthUnit.METER));
        lineString = factory.createLineString(coordinates);
        new LinearGeometry(link, lineString, null);
        lane =
                new Lane(link, startLateralPos, startLateralPos, startWidth, startWidth, laneType,
                        longitudinalDirectionality, f2000);
        //CrossSectionElement.printCoordinates("Lane contour", lane.getContour());
        System.out.println("Clockwise ring, lane completely outside ring design line");
        // Try the same with a ring that is traveled in clockwise direction
        radius = 20;
        for (int index = 1; index < numberOfCoordinates - 1; index++)
        {
            double angle = -Math.PI * 2 * index / (numberOfCoordinates - 1);
            coordinates[index] =
                    new Coordinate(nodeFrom.getPoint().x - radius + radius * Math.cos(angle), nodeFrom.getPoint().y
                            + radius * Math.sin(angle));
            //System.out.println(String.format("angle %6.3f %8.3f %8.3f", angle, coordinates[index].x, coordinates[index].y));
        }
        //CrossSectionElement.printCoordinates("Design coordinates of ring", coordinates);
        link =
                new Link("Ring", nodeFrom, nodeTo, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(),
                        LengthUnit.METER));
        lineString = factory.createLineString(coordinates);
        new LinearGeometry(link, lineString, null);
        lane =
                new Lane(link, startLateralPos, startLateralPos, startWidth, startWidth, laneType,
                        longitudinalDirectionality, f2000);
        //CrossSectionElement.printCoordinates("Lane contour", lane.getContour());
        System.out.println("Clockwise ring, lane touching ring design line");
        // Try the same with a ring that is traveled in clockwise direction
        radius = 20;
        for (int index = 1; index < numberOfCoordinates - 1; index++)
        {
            double angle = -Math.PI * 2 * index / (numberOfCoordinates - 1);
            coordinates[index] =
                    new Coordinate(nodeFrom.getPoint().x - radius + radius * Math.cos(angle), nodeFrom.getPoint().y
                            + radius * Math.sin(angle));
            //System.out.println(String.format("angle %6.3f %8.3f %8.3f", angle, coordinates[index].x, coordinates[index].y));
        }
        //CrossSectionElement.printCoordinates("Design coordinates of ring", coordinates);
        link =
                new Link("Ring", nodeFrom, nodeTo, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(),
                        LengthUnit.METER));
        lineString = factory.createLineString(coordinates);
        new LinearGeometry(link, lineString, null);
        lane =
                new Lane(link, startLateralPos, startLateralPos, endWidth, endWidth, laneType,
                        longitudinalDirectionality, f2000);
        //CrossSectionElement.printCoordinates("Lane contour", lane.getContour());
        System.out.println("Clockwise ring, lane overlapping ring design line");
        // Try the same with a ring that is traveled in clockwise direction
        radius = 20;
        for (int index = 1; index < numberOfCoordinates - 1; index++)
        {
            double angle = -Math.PI * 2 * index / (numberOfCoordinates - 1);
            coordinates[index] =
                    new Coordinate(nodeFrom.getPoint().x - radius + radius * Math.cos(angle), nodeFrom.getPoint().y
                            + radius * Math.sin(angle));
            //System.out.println(String.format("angle %6.3f %8.3f %8.3f", angle, coordinates[index].x, coordinates[index].y));
        }
        //CrossSectionElement.printCoordinates("Design coordinates of ring", coordinates);
        link =
                new Link("Ring", nodeFrom, nodeTo, new DoubleScalar.Rel<LengthUnit>(lineString.getLength(),
                        LengthUnit.METER));
        lineString = factory.createLineString(coordinates);
        new LinearGeometry(link, lineString, null);
        endWidth = new DoubleScalar.Rel<LengthUnit>(5, LengthUnit.METER);
        lane =
                new Lane(link, startLateralPos, startLateralPos, endWidth, endWidth, laneType,
                        longitudinalDirectionality, f2000);
        //CrossSectionElement.printCoordinates("Lane contour", lane.getContour());
    }

}
