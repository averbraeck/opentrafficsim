package org.opentrafficsim.demo.ntm;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.fundamentaldiagrams.NetworkFundamentalDiagram;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 26 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class CellBehaviourCordon extends CellBehaviour
{
    /** */
    private static final long serialVersionUID = 20140903L;



    /** {@inheritDoc} */
    //@Override
    public double retrieveSupply(final Double accumulatedCars, final Double maxCapacity, final ParametersNTM param)
    {
        double carProduction = 0.0;
        double productionSupply = Math.min(maxCapacity, carProduction); // supply
        return productionSupply;
        
    }

    /** {@inheritDoc} */
    //@Override
    public double retrieveDemand(final Double accumulatedCars, final Double maxCapacity, final ParametersNTM param)
    {
        double maxDemand = param.getFreeSpeed().getSI() * accumulatedCars; // ask Victor
        double productionDemand = Math.min(maxDemand, maxCapacity); // / demand
        return productionDemand;
    }

    /** {@inheritDoc} */
    //@Override
    public double computeAccumulation()
    {
        double accumulation = 0.0;
        return accumulation;
    }
    







    
}
