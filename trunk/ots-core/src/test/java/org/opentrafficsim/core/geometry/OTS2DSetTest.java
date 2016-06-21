package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;

import org.junit.Test;

/**
 * Test the OTS2DSet class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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
     * @throws OTSGeometryException
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
        assertTrue("Adding shape to empty set should return true", set.add(shape));
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
    }

}
