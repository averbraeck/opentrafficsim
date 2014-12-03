package org.opentrafficsim.demo.ntm.fundamentaldiagrams;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.opentrafficsim.demo.ntm.LinearFunctionLibrary;

/**
 * * A (Network) Fundamental diagram shows the network production as a function of (network or link) density (number of cars in a
 * CELL). An example (PieceWiseLinear) is shown below:
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
 *         accCritica     accCritical2    accJam
 * </pre>
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
public class FundamentalDiagram

{
    /**
     * @param xyPairs
     * @param accumulatedCars
     * @return piecewise linear
     */
    public static double PieceWiseLinear(ArrayList<Point2D> xyPairs, double accumulatedCars)
    {
        return LinearFunctionLibrary.createPieceWiseLinear(xyPairs, accumulatedCars);

    }

}
