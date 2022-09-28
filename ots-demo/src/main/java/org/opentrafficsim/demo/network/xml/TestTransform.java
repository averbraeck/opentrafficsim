package org.opentrafficsim.demo.network.xml;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSShape;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TestTransform
{

    /**
     * 
     */
    public TestTransform()
    {
    }

    /**
     * @param args a
     * @throws OTSGeometryException on error
     */
    public static void main(String[] args) throws OTSGeometryException
    {
        double x = 200;
        double y = 300;
        double l = 4;
        double w = 2;
        Rectangle2D rect = new Rectangle2D.Double(-l / 2.0, -w / 2.0, l, w);
        Path2D path = new Path2D.Double(rect);
        AffineTransform t = new AffineTransform();
        t.translate(x, y);
        t.rotate(Math.toRadians(45));
        path.transform(t);
        OTSShape s = new OTSShape(path);
        System.out.println(s);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TestTransform []";
    }

}
