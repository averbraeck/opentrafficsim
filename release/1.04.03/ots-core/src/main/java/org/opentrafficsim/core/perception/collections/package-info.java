/**
 * Historical versions of almost all java non-concurrent collections. {@code ArrayDeque} is not supported as it does not allow
 * reversal of it's state. No implementation for {@code IdentityHashMap} is provided as it only has very specific use-cases that
 * are not in line with historical states. Of the legacy collections only {@code Vector} has an implementation.<br>
 * <br>
 * This package adds to each of the collections a {@code get()} and a {@code get(Time)} method. The {@code get()} returns a
 * shallow copy of the current contents, while {@code get(Time)} constructs past contents by working from the current content
 * backwards in time. To be able to do this, events are maintained for each change to the collection. This means that all
 * changes have to go through a specific process.<br>
 * <br>
 * There is no backing-up of provided views on collections. Subsets, key sets, value sets, etc. are hence unmodifiable. Also
 * iterators do not allow the collection to be altered. The reason for these actions not being supported is that there is no way
 * to trigger events upon changes to these sets and by iterators. All changes must be made directly on the collection.<br>
 * <br>
 * Implementations in this package use 1 or 2 event types, generally for adding and removing an element. More advanced
 * operations, such as {@code addAll()} are translated into events per individual element.<br>
 * <br>
 * Some of the interface ({@code HistoricalCollection}, {@code HistoricalList}, {@code HistoricalMap}) override default
 * implementations with other default implementations, as the original default methods use unsupported features
 * ({@code Iterator.remove()}, {@code ListIterator.set(E)}, {@code Map.Entry.setValue(V)}).
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
package org.opentrafficsim.core.perception.collections;
