package org.opentrafficsim.core.gtu;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A GTU type identifies the type of a GTU. <br>
 * GTU types are used to check whether a particular GTU can travel over a particular part of infrastructure. E.g. a
 * (LaneBased)GTU with GTUType CAR can travel over lanes that have a LaneType that has the GTUType CAR in the compatibility set.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class GTUType implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The id of the GTUType to make it identifiable. */
    private final String id;

    /** ALL GTUType to be used only for permeability and accessibility. */
    public static final GTUType ALL = new GTUType("ALL");

    /** NONE GTUType to be used only for permeability and accessibility. */
    public static final GTUType NONE = new GTUType("NONE");

    /**
     * @param id The id of the GTUType to make it identifiable.
     */
    private GTUType(final String id)
    {
        this.id = id;
    }

    /** The set of previously instantiated GTUTypes. */
    private static final Map<Object, GTUType> INSTANTIATEDGTUTYPES = new LinkedHashMap<Object, GTUType>();

    /**
     * Construct a new GTUType or (if it already exists) return an existing GTUType.
     * @param id String; the id of the GTUType
     * @return GTUType&lt;ID&gt;
     */
    public static GTUType makeGTUType(final String id)
    {
        synchronized (INSTANTIATEDGTUTYPES)
        {
            GTUType result = INSTANTIATEDGTUTYPES.get(id);
            if (null == result)
            {
                result = new GTUType(id);
                INSTANTIATEDGTUTYPES.put(id, result);
            }
            return result;
        }
    }

    /**
     * @return id.
     */
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return "GTUType: " + this.id;
    }

}
