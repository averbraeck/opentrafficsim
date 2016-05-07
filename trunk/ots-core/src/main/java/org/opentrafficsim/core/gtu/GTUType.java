package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.Type;

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
public final class GTUType extends Type<GTUType> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The id of the GTUType to make it identifiable. */
    private String id;

    /** ALL GTUType to be used only for permeability and accessibility. */
    public static final GTUType ALL;

    /** NONE GTUType to be used only for permeability and accessibility. */
    public static final GTUType NONE;

    /* static block to guarantee that ALL is always on the first place, and NONE on the second, for code reproducibility. */
    static
    {
        ALL = new GTUType("ALL");
        NONE = new GTUType("NONE");
    }

    /**
     * @param id The id of the GTUType to make it identifiable.
     * @throws NullPointerException if the id is null
     */
    public GTUType(final String id) throws NullPointerException
    {
        Throw.whenNull(id, "id cannot be null for GTUType");
        this.id = id;
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

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:needbraces")
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GTUType other = (GTUType) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        return true;
    }

}
