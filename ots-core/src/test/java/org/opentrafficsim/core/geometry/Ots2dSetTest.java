package org.opentrafficsim.core.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;

/**
 * Test the Ots2dSet class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Ots2dSetTest
{
    /**
     * Test some simple things.
     * @throws OtsGeometryException when something goes wrong; if it does; this test has failed
     */
    @Test
    public final void testBasics() throws OtsGeometryException
    {
        try
        {
            new Ots2dSet(null, 1);
            fail("null argument for rectangle should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        Bounds2d rectangle = new Bounds2d(-200, 200, -400, 400);
        try
        {
            new Ots2dSet(rectangle, 0);
            fail("minimum cell size <= 0 should have thrown an OTSGeometryException");
        }
        catch (OtsGeometryException e)
        {
            // Ignore expected exception
        }
        Ots2dSet set = new Ots2dSet(rectangle, 10);
        assertEquals(0, set.size(), "Size of set should be 0");
        assertTrue(set.isEmpty(), "Set should be empty");
        // Generate an approximation of a circle.
        int maxPoint = 10;
        double radius = 40;
        double centerX = 95;
        double centerY = 55;
        Point2d[] shapePoints = new Point2d[maxPoint];
        for (int i = 0; i < maxPoint; i++)
        {
            double angle = 2 * Math.PI * i / maxPoint;
            shapePoints[i] = new Point2d(centerX + radius * Math.sin(angle), centerY + radius * Math.cos(angle));
        }
        Polygon2d shape = new Polygon2d(shapePoints);
        assertFalse(set.contains(shape), "set does not contain shape");
        assertTrue(set.add(shape), "Adding shape to empty set should return true");
        assertEquals(1, set.size(), "Size of the set should now be one");
        assertFalse(set.isEmpty(), "Set should not be empty");
        assertFalse(set.add(shape), "Adding the same shape again should return false");
        assertEquals(1, set.size(), "Size of the set should still be one");
        assertEquals(1, set.toArray().length, "Length of result of toArray should be 0");
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
        assertTrue(set.contains(shape), "set contains shape");
        int count = 0;
        for (Polygon2d s : set)
        {
            assertEquals(shape, s, "With one object in the set, the iterator should return that object");
            count++;
        }
        assertEquals(set.size(), count, "The number of items returned by the iterator must match the size");
        Object[] array = set.toArray();
        assertEquals(1, array.length, "toArray returns array of length 1");
        assertEquals(shape, array[0], "Element in array is our shape");

        Polygon2d[] elements = set.toArray(new Polygon2d[5]);
        elements[1] = shape;
        set.toArray(elements);
        assertEquals(shape, elements[0], "Element 0 in array is our shape");
        assertNull(elements[1], "There is a null pointer at position 1 in the array");
        elements = new Polygon2d[0]; // too short; should be replaced by toArray by one that has the correct length
        elements = set.toArray(elements);
        assertEquals(1, elements.length, "Elements is new array with length 1");
        assertEquals(shape, elements[0], "Element 0 in array is our shape");
        assertFalse(set.remove("String"), "Attempt to remove a non Polygon2d should return false");
        assertEquals(1, set.size(), "Size of the set should still be one");
        assertTrue(set.remove(shape), "Removal of shape should succeed");
        assertEquals(0, set.size(), "Size of set should be 0 again");
        assertFalse(set.contains(shape), "Set no longer contains shape");
        assertTrue(set.isEmpty(), "Set should be empty again");
        System.out.println("level 3:");
        System.out.println(set.toStringGraphic(3));
        set.add(shape);
        assertEquals(1, set.size(), "Set should contain one shape");
        Polygon2d triangleShape = new Polygon2d(new Point2d(-1, 0), new Point2d(1, 0), new Point2d(0, 1));
        set.add(triangleShape);
        assertEquals(2, set.size(), "Set should contain two shapes");
        Polygon2d triangleShape2 = new Polygon2d(new Point2d(0, 0), new Point2d(2, 0), new Point2d(1, 1));
        set.add(triangleShape2);
        assertEquals(3, set.size(), "Set should contain two shapes");
        for (Iterator<Polygon2d> it = set.iterator(); it.hasNext();)
        {
            it.next();
            it.remove();
        }
        assertEquals(0, set.size(), "Set should be empty again");
        double left = rectangle.getMaxX();
        left += Math.ulp(left);
        Polygon2d shape2 = new Polygon2d(new Point2d(left, rectangle.getMinY()), new Point2d(left + 10, rectangle.getMinY()),
                new Point2d(left, rectangle.getMaxY()));
        // This shape is one ULP outside the area of the set (without that ULP it would be added).
        assertFalse(set.add(shape2), "Polygon2d just outside rectangle should not be added");
        assertTrue(set.isEmpty(), "set should still be empty");
        rectangle = new Bounds2d(0, Math.ulp(0d), 0, 100);
        set = new Ots2dSet(rectangle, 10);
        // Adding this shape should cause underflow
        set.add(triangleShape);
        rectangle = new Bounds2d(0, 100, 0, Math.ulp(0d));
        set = new Ots2dSet(rectangle, 10);
        // Adding this shape should cause underflow
        set.add(triangleShape);

    }

}
