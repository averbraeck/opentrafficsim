package org.opentrafficsim.core.gtu;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 8, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <ID> the ID-type of the GTU, e.g. String or a certain Enum type.
 */
public interface GTUType<ID>
{
    /** @return the id of the GTU Type, could be String or Integer */
    ID getID();

}
