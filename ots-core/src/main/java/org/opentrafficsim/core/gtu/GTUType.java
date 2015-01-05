package org.opentrafficsim.core.gtu;

import java.io.Serializable;

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
public class GTUType<ID> implements Serializable
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
    public GTUType(final ID id)
    {
        this.id = id;
    }

    /**
     * @return id.
     */
    public final ID getId()
    {
        return this.id;
    }

}
