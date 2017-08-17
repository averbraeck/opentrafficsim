package org.opentrafficsim.core.gtu;

import java.io.Serializable;

import org.opentrafficsim.base.HierarchicalType;

/**
 * A GTU type identifies the type of a GTU. <br>
 * GTU types are used to check whether a particular GTU can travel over a particular part of infrastructure. E.g. a
 * (LaneBased)GTU with GTUType CAR can travel over lanes that have a LaneType that has the GTUType CAR in the compatibility set.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class GTUType extends HierarchicalType<GTUType> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** ALL GTUType to be used only for permeability and accessibility. */
    public static final GTUType ALL;

    /** NONE GTUType to be used only for permeability and accessibility. */
    public static final GTUType NONE;

    /** Super type for pedestrians. */
    public static final GTUType PEDESTRIAN;

    /** Super type for bicycle. */
    public static final GTUType BICYCLE;
    
    /** Super type for mopeds. */
    public static final GTUType MOPED;

    /** Super type for vehicles. */
    public static final GTUType VEHICLE;
    
    /** Super type for emergency vehicles. */
    public static final GTUType EMERGENCY_VEHICLE;

    /** Super type for ships. */
    public static final GTUType SHIP;

    /** Super type for trains. */
    public static final GTUType TRAIN;
    
    /** Super type for cars. */
    public static final GTUType CAR;

    /** Super type for vans. */
    public static final GTUType VAN;

    /** Super type for busses. */
    public static final GTUType BUS;

    /** Super type for trucks. */
    public static final GTUType TRUCK;

    /** Super type for scheduled busses. */
    public static final GTUType SCHEDULED_BUS;

    /* static block to guarantee that ALL is always on the first place, and NONE on the second, for code reproducibility. */
    static
    {
        ALL = new GTUType("ALL");
        NONE = new GTUType("NONE");
        
        PEDESTRIAN = new GTUType("PEDESTRIAN", ALL);
        BICYCLE = new GTUType("BICYCLE", ALL);
        SHIP = new GTUType("SHIP", ALL);
        TRAIN = new GTUType("TRAIN", ALL);
        
        MOPED = new GTUType("MOPED", ALL);
        
        VEHICLE = new GTUType("VEHICLE", ALL);
        EMERGENCY_VEHICLE = new GTUType("EMERGENCY_VEHICLE", VEHICLE);
        CAR = new GTUType("CAR", VEHICLE);
        VAN = new GTUType("VAN", VEHICLE);
        BUS = new GTUType("BUS", VEHICLE);
        TRUCK = new GTUType("TRUCK", VEHICLE);
        SCHEDULED_BUS = new GTUType("SCHEDULED BUS", BUS);
    }

    /**
     * @param id The id of the GTUType to make it identifiable.
     * @throws NullPointerException if the id is null
     */
    private GTUType(final String id) throws NullPointerException
    {
        super(id);
    }

    /**
     * @param id The id of the GTUType to make it identifiable.
     * @param parent GTUType; parent GTU type
     * @throws NullPointerException if the id is null
     */
    public GTUType(final String id, final GTUType parent) throws NullPointerException
    {
        super(id, parent);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GTUType: " + this.getId();
    }

}
