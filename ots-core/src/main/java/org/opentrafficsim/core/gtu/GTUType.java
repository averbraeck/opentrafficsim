package org.opentrafficsim.core.gtu;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> the ID-type of the GTU, e.g. String or a certain Enum type.
 */
public final class GTUType<ID> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The id of the GTUType to make it identifiable. */
    private final ID id;

    /** ALL GTUType to be used only for permeability and accessibility. */
    public static final GTUType<String> ALL = new GTUType<String>("ALL");

    /** NONE GTUType to be used only for permeability and accessibility. */
    public static final GTUType<String> NONE = new GTUType<String>("NONE");

    /**
     * @param id The id of the GTUType to make it identifiable.
     */
    private GTUType(final ID id)
    {
        this.id = id;
    }
    
    /** The set of previously instantiated GTUTypes. */
    private static final Map<Object, GTUType<?>> INSTANTIATEDGTUTYPES = new LinkedHashMap<Object, GTUType<?>>();

    /**
     * Construct a new GTUType or (if it already exists) return an existing GTUType.
     * @param id ID; the id of the GTUType
     * @param <ID> the ID-type of the GTU, e.g. String or a certain Enum type.
     * @return GTUType&lt;ID&gt;
     */
    @SuppressWarnings("unchecked")
    public static <ID> GTUType<ID> makeGTUType(final ID id)
    {
        synchronized (INSTANTIATEDGTUTYPES)
        {
            GTUType<?> result = INSTANTIATEDGTUTYPES.get(id);
            if (null == result)
            {
                result = new GTUType<ID>(id);
                INSTANTIATEDGTUTYPES.put(id, result);
            }
            return (GTUType<ID>) result;
        }
    }

    /**
     * @return id.
     */
    public ID getId()
    {
        return this.id;
    }
    
    /** {@inheritDoc} */
    public String toString()
    {
        return "GTUType: " + this.id;
    }

}
