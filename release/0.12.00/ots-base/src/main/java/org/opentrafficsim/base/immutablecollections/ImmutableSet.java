package org.opentrafficsim.base.immutablecollections;

import java.util.Set;

/**
 * A Set interface without the methods that can change it. The constructor of the ImmutableSet needs to be given an initial Set.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> the type of content of this Set
 */
public interface ImmutableSet<E> extends ImmutableCollection<E>
{
    /**
     * Returns a modifiable copy of this immutable set.
     * @return a modifiable copy of this immutable set.
     */
    Set<E> toSet();
    
    /**
     * Force to redefine equals for the implementations of immutable collection classes. 
     * @param obj the object to compare this collection with
     * @return whether the objects are equal
     */
    boolean equals(final Object obj);

    /**
     * Force to redefine hashCode for the implementations of immutable collection classes. 
     * @return the calculated hashCode
     */
    int hashCode();
}
