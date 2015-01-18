package org.opentrafficsim.core.network.point2d;

import java.awt.geom.Point2D;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


/**
 * Tests for LinkPoint2D and NodePoint2D classes.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 17 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkAndNodeTest
{
    /**
     * Test the LinkPoint2D and NodePoint2D constructors and getters.
     */
    @Test
    public void nodeTest()
    {
        double fromX = 123;
        double fromY = 234;
        Point2D.Double fromP = new Point2D.Double(fromX, fromY);
        String fromId = "From";
        NodePoint2D.STR fromNode = new NodePoint2D.STR(fromId, fromP);
        checkFields(fromNode, fromX, fromY, fromId);
        double toX = 765;
        double toY = 654;
        Point2D.Double toP = new Point2D.Double(toX, toY);
        String toId = "To";
        NodePoint2D.STR toNode = new NodePoint2D.STR(toId,  toP);
        checkFields(fromNode, fromX, fromY, fromId);
        checkFields(toNode, toX, toY, toId);
        
    }
    
    /**
     * Check the fields in a NodePoint2D.STR.
     * @param node NodePoint2D.STR
     * @param x double; expected x
     * @param y double; expected y
     * @param id String; expected id
     */
    private void checkFields(NodePoint2D.STR node, double x, double y, String id)
    {
        assertEquals("X coordinate should be " + x, x, node.getX(), 0.0001);
        assertEquals("Y coordinate should be " + y, y, node.getY(), 0.0001);
        assertEquals("Id should be " + id, id, node.getId());
        assertEquals("Z is always 0", 0d, node.getZ(), 0.000001);
        // Test the getPoint method
        assertEquals("Point should be (x,y)", 0, node.getPoint().distance(new Point2D.Double(x, y)), 0.00001);
    } 
    
}
