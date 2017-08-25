package compatibility;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;

/**
 * Compatibility of infrastructure and traveling units.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 25, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <I> infrastructure type
 */
public interface Compatibility<G extends HierarchicalType<G>, I extends HierarchicalType<I>>
{
    /**
     * Test if a GTUType can travel over the infrastructure.
     * @param gtuType GTUType; the type of the GTU
     * @param directionality GTUDirectionality; the direction of the GTU with respect to the design direction of the
     *            infrastructure
     * @return boolean; true if the GTU can travel over the infrastructure in the given direction, null if the decision should
     *         be made by calling <code>isCompatible</code> on a higher level in the infrastructure hierarchy
     */
    Boolean isCompatible(GTUType gtuType, GTUDirectionality directionality);

    /**
     * Retrieve the allowed driving directions for a GTUType. If there is not match for the specified GTUType, this method will
     * recursively check all parent types of the GTUType.
     * @param gtuType GTUType; type of the GTU
     * @return LongitudinalDirectionality; the driving directions for the GTUType, or DIR_NONE if neither the GTUType or any of
     *         its parents has a known directionality
     */
    LongitudinalDirectionality getDirectionality(GTUType gtuType);

}
