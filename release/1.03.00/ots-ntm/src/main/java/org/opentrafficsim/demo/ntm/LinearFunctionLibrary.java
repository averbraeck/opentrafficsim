package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 9 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class LinearFunctionLibrary
{

    /**
     * An example (PieceWiseLinear) is shown below:
     * 
     * <pre>
     *   production
     *      |      
     *    Y |      _____________________ 
     *      |     /                      \
     *      |    /                        \ 
     *      |   /                          \  
     *      |  /                            \   
     *      | /                              \ 
     *      |/________________________________\__________ density (number of vehicles)
     *            ^                     ^ 
     *            x1                   x2     x3
     * </pre>
     * <p>
     * @param xyPairs ArrayList&lt;Point2D&gt;; point that define the curve
     * @param valueX double; provides te value to execute (returns the Y-value)
     * @return the Y value of a valueX
     */
    public static double createPieceWiseLinear(final ArrayList<Point2D> xyPairs, final double valueX)
    {
        double result = 0;
        if (valueX < 0)
        {
            System.out.println("Strange: negative X value");
        }
        else if (valueX > xyPairs.get(xyPairs.size() - 1).getX())
        {
            System.out.println("Strange: X value above maximum");

        }
        else
        {
            Point2D prevPoint = null;
            for (Point2D p : xyPairs)
            {
                if (p.getX() > valueX)
                {
                    if (prevPoint == null)
                    {
                        System.out.println("test");
                    }
                    result = prevPoint.getY()
                            + (p.getY() - prevPoint.getY()) * (valueX - prevPoint.getX()) / (p.getX() - prevPoint.getX());
                    break;
                }
                prevPoint = p;
            }
        }
        return result;

    }

}
