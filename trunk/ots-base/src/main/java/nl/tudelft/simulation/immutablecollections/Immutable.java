package nl.tudelft.simulation.immutablecollections;

/**
 * Indicate whether the immutable collection contains a COPY of the collection (neither changeable by the user of the immutable
 * collection, nor by anyone holding a pointer to the original collection), or a WRAP for the original collection (not
 * changeable by the user of the immutable collection, but can be changed by anyone holding a pointer to the original collection
 * that is wrapped).
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum Immutable
{
    /**
     * A copy is neither changeable by the user of the immutable collection, nor by anyone holding a pointer to the original
     * collection that is put into the immutable collection.
     */
    COPY,

    /**
     * A wrapped immutable collection is not changeable by the user of the immutable collection, but can be changed by anyone
     * holding a pointer to the original collection that is wrapped.
     */
    WRAP;
}
