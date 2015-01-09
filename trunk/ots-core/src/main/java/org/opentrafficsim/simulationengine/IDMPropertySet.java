package org.opentrafficsim.simulationengine;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Compound property for IDM or IDMPlus parameters
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 5 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDMPropertySet
{
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
    public static CompoundProperty makeIDMPropertySet(final String carType, DoubleScalar.Abs<AccelerationUnit> a,
            DoubleScalar.Abs<AccelerationUnit> b, DoubleScalar.Rel<LengthUnit> s0, DoubleScalar.Rel<TimeUnit> tSafe,
            int displayPriority)
    {
        ArrayList<AbstractProperty<?>> subProperties = new ArrayList<AbstractProperty<?>>();
        subProperties.add(new ContinuousProperty("a", "maximum acceleration [m/s/s]", a.getSI(), 0.5, 5.0,
                "maximum acceleration %.2fm/s\u00b2", false, 0));
        subProperties.add(new ContinuousProperty("b", "safe deceleration [m/s/s]", b.getSI(), 1.0, 4.0,
                "maximum comfortable deceleration %.2fm/s\u00b2", false, 0));
        subProperties.add(new ContinuousProperty("s0", "stationary distance headway [m]", s0.getSI(), 1.0, 10.0,
                "distance headway %.2fm", false, 2));
        subProperties.add(new ContinuousProperty("tSafe", "time headway", tSafe.getSI(), 0.5, 1.5,
                "time headway %.2fs", false, 3));
        return new CompoundProperty("IDM/IDM+ " + carType + " params", "Parameters for the " + carType
                + " car following parameters", subProperties, true, displayPriority);
    }

    /**
     * Return the maximum acceleration.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;
     */
    public static DoubleScalar.Abs<AccelerationUnit> getA(CompoundProperty set)
    {
        return new DoubleScalar.Abs<AccelerationUnit>(findSubProperty("a", set), AccelerationUnit.METER_PER_SECOND_2);
    }

    /**
     * Return the maximum comfortable deceleration.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;
     */
    public static DoubleScalar.Abs<AccelerationUnit> getB(CompoundProperty set)
    {
        return new DoubleScalar.Abs<AccelerationUnit>(findSubProperty("b", set), AccelerationUnit.METER_PER_SECOND_2);
    }

    /**
     * Return the static headway.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return DoubleScalar.Abs&lt;LengthUnit&gt;
     */
    public static DoubleScalar.Rel<LengthUnit> getS0(CompoundProperty set)
    {
        return new DoubleScalar.Rel<LengthUnit>(findSubProperty("s0", set), LengthUnit.METER);
    }

    /**
     * Return the time headway.
     * @param set CompoundProperty (should have been created with makeIDMPropertySet)
     * @return DoubleScalar.Abs&lt;TimeUnit&gt;
     */
    public static DoubleScalar.Rel<TimeUnit> getTSafe(CompoundProperty set)
    {
        return new DoubleScalar.Rel<TimeUnit>(findSubProperty("tSafe", set), TimeUnit.SECOND);
    }

    /**
     * Find the Continuous sub property with the specified name
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
