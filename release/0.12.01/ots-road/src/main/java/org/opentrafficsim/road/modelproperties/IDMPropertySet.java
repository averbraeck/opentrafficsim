package org.opentrafficsim.road.modelproperties;

import java.util.ArrayList;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.ContinuousProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;

/**
 * Compound property for IDM or IDMPlus parameters
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-05-28 11:33:31 +0200 (Sat, 28 May 2016) $, @version $Revision: 2051 $, by $Author: averbraeck $,
 * initial version 5 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class IDMPropertySet
{
    /**
     * This class shall never be instantiated.
     */
    private IDMPropertySet()
    {
        // Prevent instantiation of this class
    }

    /**
     * Create a CompoundProperty for the IDM or IDMPlus parameters for a specified car type.
     * @param key String; the unique key of the new property
     * @param carType String; the type of the car
     * @param a Acceleration; the maximum acceleration of the car
     * @param b Acceleration; the maximum comfortable deceleration of the car
     * @param s0 Length; the stationary distance headway
     * @param tSafe Duration; the time headway
     * @param displayPriority int; the display priority of the returned CompoundProperty
     * @return CompoundProperty
     * @throws PropertyException when key is not unique, or one of the generated sub keys is not unique
     */
    public static CompoundProperty makeIDMPropertySet(final String key, final String carType, final Acceleration a,
            final Acceleration b, final Length s0, final Duration tSafe, final int displayPriority) throws PropertyException
    {
        ArrayList<Property<?>> subProperties = new ArrayList<>();
        subProperties.add(new ContinuousProperty(key + "a", "a", "maximum acceleration [m/s/s]", a.doubleValue(), 0.5, 5.0,
                "maximum acceleration %.2fm/s\u00b2", false, 0));
        subProperties.add(new ContinuousProperty(key + "b", "b", "safe deceleration [m/s/s]", b.doubleValue(), 1.0, 4.0,
                "maximum comfortable deceleration %.2fm/s\u00b2", false, 0));
        subProperties.add(new ContinuousProperty(key + "s0", "s0", "stationary distance headway [m]", s0.doubleValue(), 1.0,
                10.0, "distance headway %.2fm", false, 2));
        subProperties.add(new ContinuousProperty(key + "tSafe", "tSafe", "time headway", tSafe.doubleValue(), 0.5, 1.5,
                "time headway %.2fs", false, 3));
        return new CompoundProperty(key, "IDM/IDM+ " + carType + " params", "Parameters for the " + carType
                + " car following parameters", subProperties, true, displayPriority);
    }

    /**
     * Return the maximum acceleration.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return Acceleration
     */
    public static Acceleration getA(final CompoundProperty set)
    {
        return new Acceleration(findSubProperty(set.getKey() + "a", set), AccelerationUnit.METER_PER_SECOND_2);
    }

    /**
     * Return the maximum comfortable deceleration.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return Acceleration
     */
    public static Acceleration getB(final CompoundProperty set)
    {
        return new Acceleration(findSubProperty(set.getKey() + "b", set), AccelerationUnit.METER_PER_SECOND_2);
    }

    /**
     * Return the static headway.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return Length
     */
    public static Length getS0(final CompoundProperty set)
    {
        return new Length(findSubProperty(set.getKey() + "s0", set), LengthUnit.METER);
    }

    /**
     * Return the time headway.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return Time
     */
    public static Duration getTSafe(final CompoundProperty set)
    {
        return new Duration(findSubProperty(set.getKey() + "tSafe", set), TimeUnit.SECOND);
    }

    /**
     * Find the Continuous sub property with the specified name.
     * @param key String; name of the sub property
     * @param set CompoundProperty; the set to search
     * @return Double; the value of the Continuous sub property with the specified name
     */
    private static Double findSubProperty(final String key, final CompoundProperty set)
    {
        Property<?> pp = set.findSubPropertyByKey(key);
        if (null == pp)
        {
            throw new RuntimeException("Cannot find sub property " + key);
        }
        if (pp instanceof ContinuousProperty)
        {
            return ((ContinuousProperty) pp).getValue();
        }
        throw new RuntimeException("Cannot find Continuous sub property " + key + " in " + set.getShortName());
    }

}
