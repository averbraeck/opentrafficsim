/**
 * Contains a set of immutable collection interfaces and wrapper implementations. Two versions of immutable collections are
 * implemented:
 * <ol>
 * <li>A version, identified by Immutable.COPY, where the immutable collection can neither be changed by any object "using" the
 * ImmutableCollection nor anymore by objects that have a pointer to the collection, as an internal (shallow) copy is made of
 * the collection. This is the <b>default</b> implementation.</li>
 * <li>A version, identified by Immutable.WRAP, where the immutable collection can not be changed by any object "using" the
 * ImmutableCollection, but it can still be changed by any object that has a pointer to the original collection that is
 * "wrapped". Instead of a (shallow) copy of the collection, a pointer to the collection is stored.</li>
 * </ol>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
package org.opentrafficsim.core.immutablecollections;
