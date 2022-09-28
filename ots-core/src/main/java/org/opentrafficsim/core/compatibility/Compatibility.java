package org.opentrafficsim.core.compatibility;

import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.network.LongitudinalDirectionality;

/**
 * Compatibility of infrastructure and traveling units.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <I> infrastructure type
 */
public interface Compatibility<G extends HierarchicalType<G>, I extends HierarchicalType<I>>
{
    /**
     * Test if a GTUType can travel over the infrastructure.
     * @param gtuType G; the type of the GTU
     * @param directionality GTUDirectionality; the direction of the GTU with respect to the design direction of the
     *            infrastructure
     * @return boolean; true if the GTU can travel over the infrastructure in the given direction; false if the GTU can not
     *         travel over the infrastructure in the given direction; null if the decision should be made by calling
     *         <code>isCompatible</code> on a higher level in the infrastructure hierarchy
     */
    Boolean isCompatible(G gtuType, GTUDirectionality directionality);

    /**
     * Retrieve the allowed driving directions for a GTUType. If there is no match for the specified GTUType in this
     * infrastructure type, this method will recursively check the parent types of the infrastructure element until either a
     * match is found or the root parental type of the infrastructure is reached. When the latter happens without finding a
     * match, what happens next depends on the value of <code>tryParentsOfGTUType</code>. <br>
     * If <code>tryParentsOfGTUType</code> is false, the value null is returned. If true; the parent of the GTUType is used and
     * the search is repeated, etc. If none of the parents of the GTUType yields a result, this method returns
     * <code>LongitudinalDirectionality.DIR_NONE</code>.
     * @param gtuType G; type of the GTU
     * @param tryParentsOfGTUType boolean; if true; the parents of the GTUType are tried if no match was found for the given
     *            GTUType
     * @return LongitudinalDirectionality; the driving directions for the GTUType, or
     *         <code>LongitudinalDirectionality.DIR_NONE</code> if neither the GTUType or any of its parents specifies a
     *         directionality
     */
    LongitudinalDirectionality getDirectionality(G gtuType, boolean tryParentsOfGTUType);

}
