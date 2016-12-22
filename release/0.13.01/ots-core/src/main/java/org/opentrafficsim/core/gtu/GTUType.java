package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.opentrafficsim.base.Type;

import nl.tudelft.simulation.language.Throw;

/**
 * A GTU type identifies the type of a GTU. <br>
 * GTU types are used to check whether a particular GTU can travel over a particular part of infrastructure. E.g. a
 * (LaneBased)GTU with GTUType CAR can travel over lanes that have a LaneType that has the GTUType CAR in the compatibility set.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private final String id;
    
    /** Parent GTUType. */
    private final GTUType parent;

    /** ALL GTUType to be used only for permeability and accessibility. */
    public static final GTUType ALL;

    /** NONE GTUType to be used only for permeability and accessibility. */
    public static final GTUType NONE;
    
    /** Super type for pedestrians. */
    public static final GTUType PEDESTRIAN;
    
    /** Super type for bikes. */
    public static final GTUType BIKE;
    
    /** Super type for vehicles. */
    public static final GTUType VEHICLE;
    
    /** Super type for boats. */
    public static final GTUType BOAT;
    
    /** Super type for trains. */ 
    public static final GTUType TRAIN;

    /* static block to guarantee that ALL is always on the first place, and NONE on the second, for code reproducibility. */
    static
    {
        ALL = new GTUType("ALL");
        NONE = new GTUType("NONE");
        PEDESTRIAN = new GTUType("PEDESTRIAN", ALL);
        BIKE = new GTUType("BIKE", ALL);
        VEHICLE = new GTUType("VEHICLE", ALL);
        BOAT = new GTUType("BOAT", ALL);
        TRAIN = new GTUType("TRAIN", ALL);
    }

    /**
     * @param id The id of the GTUType to make it identifiable.
     * @throws NullPointerException if the id is null
     */
    private GTUType(final String id) throws NullPointerException
    {
        Throw.whenNull(id, "id cannot be null for GTUType");
        this.id = id;
        this.parent = null;
    }
    
    /**
     * @param id The id of the GTUType to make it identifiable.
     * @param parent GTUType; parent GTU type
     * @throws NullPointerException if the id is null
     */
    public GTUType(final String id, final GTUType parent) throws NullPointerException
    {
        Throw.whenNull(id, "id cannot be null for GTUType");
        this.id = id;
        this.parent = parent;
    }

    /**
     * @return id.
     */
    public String getId()
    {
        return this.id;
    }
    
    /**
     * @return parent.
     */
    public GTUType getParent()
    {
        return this.parent;
    }

    /**
     * Whether this, or any of the parent GTU types, equals the given GTU type.
     * @param gtuType GTUType; gtu type
     * @return whether this, or any of the parent GTU types, equals the given GTU type
     */
    public boolean isOfType(final GTUType gtuType)
    {
        if (this.equals(gtuType))
        {
            return true;
        }
        if (this.parent != null)
        {
            return this.parent.isOfType(gtuType);
        }
        return false;
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
        result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
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
        if (!this.id.equals(other.id))
            return false;
        if (this.parent == null)
            if (other.parent != null)
                return false;
        else if (!this.parent.equals(other.parent))
            return false;
        return true;
    }

}
