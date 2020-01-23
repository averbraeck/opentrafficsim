package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.junit.Test;

/**
 * Test the OTS2DSet class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTS2DSetTest
{
    /**
     * Test some simple things.
     * @throws OTSGeometryException when something goes wrong; if it does; this test has failed
     */
    @Test
    public final void testBasics() throws OTSGeometryException
    {
        try
        {
            new OTS2DSet(null, 1);
            fail("null argument for rectangle should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        Rectangle2D rectangle = new Rectangle2D.Double(10, 10, 0, 1);
        try
        {
            new OTS2DSet(rectangle, 1);
            fail("Rectangle with 0 width should have thrown an OTSGeometryException");
        }
        catch (OTSGeometryException e)
        {
            // Ignore expected exception
        }
        rectangle = new Rectangle2D.Double(10, 10, 1, 0);
        try
        {
            new OTS2DSet(rectangle, 1);
            fail("Rectangle with 0 height should have thrown an OTSGeometryException");
        }
        catch (OTSGeometryException e)
        {
            // Ignore expected exception
        }
        rectangle = new Rectangle2D.Double(-200, -200, 400, 400);
        try
        {
            new OTS2DSet(rectangle, 0);
            fail("minimum cell size <= 0 should have thrown an OTSGeometryException");
        }
        catch (OTSGeometryException e)
        {
            // Ignore expected exception
        }
        OTS2DSet set = new OTS2DSet(rectangle, 10);
        assertEquals("Size of set should be 0", 0, set.size());
        assertTrue("Set should be empty", set.isEmpty());
        // Generate an approximation of a circle.
        int maxPoint = 10;
        double radius = 40;
        double centerX = 95;
        double centerY = 55;
        OTSPoint3D[] shapePoints = new OTSPoint3D[maxPoint];
        for (int i = 0; i < maxPoint; i++)
        {
            double angle = 2 * Math.PI * i / maxPoint;
            shapePoints[i] = new OTSPoint3D(centerX + radius * Math.sin(angle), centerY + radius * Math.cos(angle));
        }
        OTSShape shape = new OTSShape(shapePoints);
        assertFalse("set does not contain shape", set.contains(shape));
        assertTrue("Adding shape to empty set should return true", set.add(shape));
        assertEquals("Size of the set should now be one", 1, set.size());
        assertFalse("Set should not be empty", set.isEmpty());
        assertFalse("Adding the same shape again should return false", set.add(shape));
        assertEquals("Size of the set should still be one", 1, set.size());
        assertEquals("Length of result of toArray should be 0", 1, set.toArray().length);
        System.out.println("set is " + set.toString(200));
        System.out.println("level 0:");
        System.out.println(set.toStringGraphic(0));
        System.out.println("level 1:");
        System.out.println(set.toStringGraphic(1));
        System.out.println("level 2:");
        System.out.println(set.toStringGraphic(2));
        System.out.println("level 3:");
        System.out.println(set.toStringGraphic(3));
        System.out.println("level 4:");
        System.out.println(set.toStringGraphic(4));
        System.out.println("level 5:");
        System.out.println(set.toStringGraphic(5));
        // System.out.println("level 6:");
        // System.out.println(set.toStringGraphic(6));
        assertTrue("set contains shape", set.contains(shape));
        int count = 0;
        for (OTSShape s : set)
        {
            assertEquals("With one object in the set, the iterator should return that object", shape, s);
            count++;
        }
        assertEquals("The number of items returned by the iterator must match the size", set.size(), count);
        Object[] array = set.toArray();
        assertEquals("toArray returns array of length 1", 1, array.length);
        assertEquals("Element in array is our shape", shape, array[0]);

        OTSShape[] elements = set.toArray(new OTSShape[5]);
        elements[1] = shape;
        set.toArray(elements);
        assertEquals("Element 0 in array is our shape", shape, elements[0]);
        assertNull("There is a null pointer at position 1 in the array", elements[1]);
        elements = new OTSShape[0]; // too short; should be replaced by toArray by one that has the correct length
        elements = set.toArray(elements);
        assertEquals("Elements is new array with length 1", 1, elements.length);
        assertEquals("Element 0 in array is our shape", shape, elements[0]);
        assertFalse("Attempt to remove a non OTSShape should return false", set.remove("String"));
        assertEquals("Size of the set should still be one", 1, set.size());
        assertTrue("Removal of shape should succeed", set.remove(shape));
        assertEquals("Size of set should be 0 again", 0, set.size());
        assertFalse("Set no longer contains shape", set.contains(shape));
        assertTrue("Set should be empty again", set.isEmpty());
        System.out.println("level 3:");
        System.out.println(set.toStringGraphic(3));
        set.add(shape);
        assertEquals("Set should contain one shape", 1, set.size());
        OTSShape triangleShape = new OTSShape(new OTSPoint3D(-1, 0), new OTSPoint3D(1, 0), new OTSPoint3D(0, 1));
        set.add(triangleShape);
        assertEquals("Set should contain two shapes", 2, set.size());
        OTSShape triangleShape2 = new OTSShape(new OTSPoint3D(0, 0), new OTSPoint3D(2, 0), new OTSPoint3D(1, 1));
        set.add(triangleShape2);
        assertEquals("Set should contain two shapes", 3, set.size());
        for (Iterator<OTSShape> it = set.iterator(); it.hasNext();)
        {
            it.next();
            it.remove();
        }
        assertEquals("Set should be empty again", 0, set.size());
        double left = rectangle.getMaxX();
        left += Math.ulp(left);
        OTSShape shape2 = new OTSShape(new OTSPoint3D(left, rectangle.getMinY()),
                new OTSPoint3D(left + 10, rectangle.getMinY()), new OTSPoint3D(left, rectangle.getMaxY()));
        // This shape is one ULP outside the area of the set (without that ULP it would be added).
        assertFalse("OTSShape just outside rectangle should not be added", set.add(shape2));
        assertTrue("set should still be empty", set.isEmpty());
        rectangle = new Rectangle2D.Double(0, 0, Math.ulp(0d), 100);
        set = new OTS2DSet(rectangle, 10);
        // Adding this shape should cause underflow
        set.add(triangleShape);
        rectangle = new Rectangle2D.Double(0, 0, 100, Math.ulp(0d));
        set = new OTS2DSet(rectangle, 10);
        // Adding this shape should cause underflow
        set.add(triangleShape);

    }

}
