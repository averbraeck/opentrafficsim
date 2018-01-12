package org.opentrafficsim.core.gtu;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.core.network.LinkType;

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

    /** Super type for all road users. */
    public static final GTUType ROAD_USER;

    /** Super type for all water way users. */
    public static final GTUType WATER_WAY_USER;

    /** Super type for all rail users. */
    public static final GTUType RAIL_WAY_USER;

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

    static
    {
        ROAD_USER = new GTUType("ROAD_USER", null);
        WATER_WAY_USER = new GTUType("WATER_WAY_USER", null);
        RAIL_WAY_USER = new GTUType("RAIL_WAY_USER", null);

        SHIP = new GTUType("SHIP", WATER_WAY_USER);
        TRAIN = new GTUType("TRAIN", RAIL_WAY_USER);
        PEDESTRIAN = new GTUType("PEDESTRIAN", ROAD_USER);
        BICYCLE = new GTUType("BICYCLE", ROAD_USER);

        MOPED = new GTUType("MOPED", BICYCLE);

        VEHICLE = new GTUType("VEHICLE", ROAD_USER);
        EMERGENCY_VEHICLE = new GTUType("EMERGENCY_VEHICLE", VEHICLE);
        CAR = new GTUType("CAR", VEHICLE);
        VAN = new GTUType("VAN", VEHICLE);
        BUS = new GTUType("BUS", VEHICLE);
        TRUCK = new GTUType("TRUCK", VEHICLE);
        SCHEDULED_BUS = new GTUType("SCHEDULED BUS", BUS);
    }

    /** Default characteristics per GTU Type. */
    private static final Map<GTUType, GTUCharacteristics> DEFAULT_CHARACTERISTICS = new HashMap<>();
    static
    {
        // CAR and TRUCK from "Maatgevende normen in de Nederlandse richtlijnen voor wegontwerp", R-2014-38, SWOV
        // TRUCK and BUS are non-articulated
        DEFAULT_CHARACTERISTICS.put(GTUType.CAR, new GTUCharacteristics(GTUType.CAR, Length.createSI(4.19),
                Length.createSI(1.7), new Speed(180, SpeedUnit.KM_PER_HOUR)));
        DEFAULT_CHARACTERISTICS.put(GTUType.TRUCK, new GTUCharacteristics(GTUType.TRUCK, Length.createSI(12.0),
                Length.createSI(2.55), new Speed(85, SpeedUnit.KM_PER_HOUR)));
        DEFAULT_CHARACTERISTICS.put(GTUType.BUS, new GTUCharacteristics(GTUType.CAR, Length.createSI(12.0),
                Length.createSI(2.55), new Speed(90, SpeedUnit.KM_PER_HOUR)));
        DEFAULT_CHARACTERISTICS.put(GTUType.VAN, new GTUCharacteristics(GTUType.CAR, Length.createSI(5.0), Length.createSI(2.4),
                new Speed(180, SpeedUnit.KM_PER_HOUR)));
        DEFAULT_CHARACTERISTICS.put(GTUType.EMERGENCY_VEHICLE, new GTUCharacteristics(GTUType.CAR, Length.createSI(5.0),
                Length.createSI(2.55), new Speed(180, SpeedUnit.KM_PER_HOUR)));
    }
    
    /**
     * Returns default characteristics for given GTUType. 
     * @param gtuType GTUType GTU type
     * @return default characteristics for given GTUType
     * @throws GTUException if there are no default characteristics for the GTU type
     */
    public static GTUCharacteristics defaultCharacteristics(final GTUType gtuType) throws GTUException
    {
        GTUCharacteristics defaultCharacteristics = DEFAULT_CHARACTERISTICS.get(gtuType);
        if (defaultCharacteristics == null)
        {
            GTUType parent = gtuType.getParent();
            if (parent != null)
            {
                return defaultCharacteristics(parent);
            }
            else
            {
                // due too recursion, we can't mention the gtu type in the message
                throw new GTUException("GTUType is not of any types with default characteristics.");
            }
        }
        return defaultCharacteristics;
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
