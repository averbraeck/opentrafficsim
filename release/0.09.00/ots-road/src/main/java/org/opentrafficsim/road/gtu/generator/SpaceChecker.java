package org.opentrafficsim.road.gtu.generator;

/**
 * Interface to check whether a GTU will have sufficient space to be generated. Implementations of this interface need
 * to define required input for instance by their constructors.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version 5 apr. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public interface SpaceChecker
{

    /**
     * Checks whether a GTU will have enough space to be generated.
     * @return Whether a GTU will have enough space to be generated.
     */
    public boolean checkEnoughSpace();
    
}
