package compatibility;

import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Interface for infrastructure types to assess traversability by GTU types.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 25, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Compatible
{
    /**
     * Test if a GTUType can travel over the infrastructure.
     * @param gtuType GTUType; the type of the GTU
     * @param directionality GTUDirectionality; the direction of the GTU with respect to the design direction of the
     *            infrastructure
     * @return boolean; true if the GTU can travel over the infrastructure in the given direction
     */
    boolean isCompatible(GTUType gtuType, GTUDirectionality directionality);

}
