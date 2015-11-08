package org.opentrafficsim.simulationengine.properties;

import java.util.ArrayList;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;

/**
 * Compound property for IDM or IDMPlus parameters
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
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
     * @param carType String; the type of the car
     * @param a DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum acceleration of the car
     * @param b DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum comfortable deceleration of the car
     * @param s0 DoubleScalar.Rel&lt;LengthUnit&gt;; the stationary distance headway
     * @param tSafe DoubleScalar.Rel&lt;TimeUnit&gt;; the time headway
     * @param displayPriority int; the display priority of the returned CompoundProperty
     * @return CompoundProperty
     */
    public static CompoundProperty makeIDMPropertySet(final String carType, final Acceleration a, final Acceleration b,
        final Length.Rel s0, final Time.Rel tSafe, final int displayPriority)
    {
        ArrayList<AbstractProperty<?>> subProperties = new ArrayList<AbstractProperty<?>>();
        subProperties.add(new ContinuousProperty("a", "maximum acceleration [m/s/s]", a.doubleValue(), 0.5, 5.0,
            "maximum acceleration %.2fm/s\u00b2", false, 0));
        subProperties.add(new ContinuousProperty("b", "safe deceleration [m/s/s]", b.doubleValue(), 1.0, 4.0,
            "maximum comfortable deceleration %.2fm/s\u00b2", false, 0));
        subProperties.add(new ContinuousProperty("s0", "stationary distance headway [m]", s0.doubleValue(), 1.0, 10.0,
            "distance headway %.2fm", false, 2));
        subProperties.add(new ContinuousProperty("tSafe", "time headway", tSafe.doubleValue(), 0.5, 1.5,
            "time headway %.2fs", false, 3));
        return new CompoundProperty("IDM/IDM+ " + carType + " params", "Parameters for the " + carType
            + " car following parameters", subProperties, true, displayPriority);
    }

    /**
     * Return the maximum acceleration.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;
     */
    public static Acceleration getA(final CompoundProperty set)
    {
        return new Acceleration(findSubProperty("a", set), AccelerationUnit.METER_PER_SECOND_2);
    }

    /**
     * Return the maximum comfortable deceleration.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;
     */
    public static Acceleration getB(final CompoundProperty set)
    {
        return new Acceleration(findSubProperty("b", set), AccelerationUnit.METER_PER_SECOND_2);
    }

    /**
     * Return the static headway.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return DoubleScalar.Abs&lt;LengthUnit&gt;
     */
    public static Length.Rel getS0(final CompoundProperty set)
    {
        return new Length.Rel(findSubProperty("s0", set), LengthUnit.METER);
    }

    /**
     * Return the time headway.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return DoubleScalar.Abs&lt;TimeUnit&gt;
     */
    public static Time.Rel getTSafe(final CompoundProperty set)
    {
        return new Time.Rel(findSubProperty("tSafe", set), TimeUnit.SECOND);
    }

    /**
     * Find the Continuous sub property with the specified name.
     * @param name String; name of the sub property
     * @param set CompoundProperty; the set to search
     * @return Double; the value of the Continuous sub property with the specified name
     */
    private static Double findSubProperty(final String name, final CompoundProperty set)
    {
        AbstractProperty<?> pp = set.findByShortName(name);
        if (null == pp)
        {
            throw new Error("Cannot find sub property " + name);
        }
        if (pp instanceof ContinuousProperty)
        {
            return ((ContinuousProperty) pp).getValue();
        }
        throw new Error("Cannot find Continuous sub property " + name + " in " + set.getShortName());
    }

}
