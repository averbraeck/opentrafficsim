package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 9 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class LinearFunctionLibrary
{

    /**
     * @param xyPairs
     * @param var
     * @return piecewise linear
     */
    public static double createPieceWiseLinear(ArrayList<Point2D> xyPairs, double var)
    {
        double result = 0;
        Point2D prevPoint = null;
        for (Point2D p : xyPairs)
        {
            if (p.getX() > var)
            {
                result = p.getY() + (prevPoint.getY() - p.getY()) * var / (p.getX() - prevPoint.getX());
            }
            prevPoint = p;
        }
        return result;

    }

}
