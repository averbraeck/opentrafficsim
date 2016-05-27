package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.Test;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version 7 apr. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class BehavioralCharacteristicsTest implements CheckInterface
{

    /**
     * Constructor tests.
     */
    @Test
    public final void constructorTest()
    {
        // Check BehavioralCharacteristics construction
        BehavioralCharacteristics bc = new BehavioralCharacteristics();
        assertNotNull("Default constructor should not return null.", bc);
        if (!bc.getParameters().isEmpty())
        {
            fail("Constructed BehavioralCharacteristics has a non-empty parameter map.");
        }

        // Check ParameterType construction (id, description, class, defaultValue)
        Length defaultValue = new Length(1.0, LengthUnit.SI);
        ParameterType<LengthUnit, Length> a =
                new ParameterType<LengthUnit, Length>("a", "along", Length.class, defaultValue);
        assertEquals("Parameter type id not properly set.", "a", a.getId());
        assertEquals("Parameter type description not properly set.", "along", a.getDescription());
        try
        {
            assertEquals("Parameter type default value not properly set.", defaultValue, a.getDefaultValue());
        }
        catch (ParameterException exception)
        {
            fail("Parameter type default value given in constructor was not set.");
        }

        // Check ParameterType construction (id, description, class)
        ParameterType<LengthUnit, Length> b = new ParameterType<LengthUnit, Length>("b", "blong", Length.class);
        assertEquals("Parameter type id not properly set.", "b", b.getId());
        assertEquals("Parameter type description not properly set.", "blong", b.getDescription());
        try
        {
            b.getDefaultValue();
            fail("Parameter type returned a default value, while none was provided.");
        }
        catch (ParameterException pe)
        {
            // ignore expected exception
        }

    }

    /**
     * Parameter value default range tests.
     */
    @Test
    public final void defaultRangeTest()
    {
        // Check default values that should work
        checkDefaultValue(1.0, POSITIVE, false);
        checkDefaultValue(-1.0, NEGATIVE, false);
        checkDefaultValue(1.0, POSITIVEZERO, false);
        checkDefaultValue(0.0, POSITIVEZERO, false);
        checkDefaultValue(-1.0, NEGATIVEZERO, false);
        checkDefaultValue(-0.0, NEGATIVEZERO, false);
        checkDefaultValue(-1.0, NONZERO, false);
        checkDefaultValue(1.0, NONZERO, false);
        checkDefaultValue(0.0, UNITINTERVAL, false);
        checkDefaultValue(0.5, UNITINTERVAL, false);
        checkDefaultValue(1.0, UNITINTERVAL, false);
        // Check default values that should not work
        checkDefaultValue(-1.0, POSITIVE, true);
        checkDefaultValue(0.0, POSITIVE, true);
        checkDefaultValue(1.0, NEGATIVE, true);
        checkDefaultValue(0.0, NEGATIVE, true);
        checkDefaultValue(-1.0, POSITIVEZERO, true);
        checkDefaultValue(1.0, NEGATIVEZERO, true);
        checkDefaultValue(0.0, NONZERO, true);
        checkDefaultValue(-0.01, UNITINTERVAL, true);
        checkDefaultValue(1.01, UNITINTERVAL, true);

        // Check set values that should work
        checkSetValue(1.0, POSITIVE, false);
        checkSetValue(-1.0, NEGATIVE, false);
        checkSetValue(1.0, POSITIVEZERO, false);
        checkSetValue(0.0, POSITIVEZERO, false);
        checkSetValue(-1.0, NEGATIVEZERO, false);
        checkSetValue(-0.0, NEGATIVEZERO, false);
        checkSetValue(-1.0, NONZERO, false);
        checkSetValue(1.0, NONZERO, false);
        checkSetValue(0.0, UNITINTERVAL, false);
        checkSetValue(0.5, UNITINTERVAL, false);
        checkSetValue(1.0, UNITINTERVAL, false);
        // Check set values that should not work
        checkSetValue(-1.0, POSITIVE, true);
        checkSetValue(0.0, POSITIVE, true);
        checkSetValue(1.0, NEGATIVE, true);
        checkSetValue(0.0, NEGATIVE, true);
        checkSetValue(-1.0, POSITIVEZERO, true);
        checkSetValue(1.0, NEGATIVEZERO, true);
        checkSetValue(0.0, NONZERO, true);
        checkSetValue(-0.01, UNITINTERVAL, true);
        checkSetValue(1.01, UNITINTERVAL, true);
    }

    /**
     * Checks a default value.
     * @param value Value to check.
     * @param check Check to perform.
     * @param shouldFail Whether the check should fail.
     */
    private void checkDefaultValue(final double value, final Check check, final boolean shouldFail)
    {
        try
        {
            new ParameterTypeAcceleration("a", "along", new Acceleration(value, AccelerationUnit.SI), check);
            if (shouldFail)
            {
                fail("Default value " + value + " fails default " + check + " check.");
            }
        }
        catch (RuntimeException re)
        {
            if (!shouldFail)
            {
                fail("Default value " + value + " does not fail default " + check + " check.");
            }
        }
    }

    /**
     * Checks a set value.
     * @param value Value to check.
     * @param check Check to perform.
     * @param shouldFail Whether the check should fail.
     */
    private void checkSetValue(final double value, final Check check, final boolean shouldFail)
    {
        try
        {
            BehavioralCharacteristics bc = new BehavioralCharacteristics();
            ParameterTypeAcceleration a = new ParameterTypeAcceleration("a", "along", check);
            bc.setParameter(a, new Acceleration(value, AccelerationUnit.SI));
            if (shouldFail)
            {
                fail("Set value " + value + " fails default " + check + " check.");
            }
        }
        catch (ParameterException pe)
        {
            if (!shouldFail)
            {
                fail("Set value " + value + " does not fail default " + check + " check.");
            }
        }
    }

    /**
     * Parameter value custom constraint tests.
     */
    @Test
    public final void customConstraintTest()
    {

        // Check values that should work
        BehavioralCharacteristics bc = new BehavioralCharacteristics();
        try
        {
            // requirement: v1 < v2
            bc.setParameter(v1, new Speed(3.0, SpeedUnit.KM_PER_HOUR));
            bc.setParameter(v2, new Speed(4.0, SpeedUnit.KM_PER_HOUR));
            bc.setParameter(v1, new Speed(2.0, SpeedUnit.KM_PER_HOUR));
            bc.setParameter(v2, new Speed(5.0, SpeedUnit.KM_PER_HOUR));
        }
        catch (ParameterException pe)
        {
            fail("Custom check of set parameter value with value of other parameter fails for correct values.");
        }

        // Check values that should not work, set v1 first
        bc = new BehavioralCharacteristics();
        try
        {
            // requirement: v1 < v2
            bc.setParameter(v1, new Speed(3.0, SpeedUnit.KM_PER_HOUR));
            bc.setParameter(v2, new Speed(2.0, SpeedUnit.KM_PER_HOUR));
            fail("Custom check of set parameter value with value of other parameter does not fail for wrong values.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // Check values that should not work, set v2 first
        bc = new BehavioralCharacteristics();
        try
        {
            // requirement: v1 < v2
            bc.setParameter(v2, new Speed(2.0, SpeedUnit.KM_PER_HOUR));
            bc.setParameter(v1, new Speed(3.0, SpeedUnit.KM_PER_HOUR));
            fail("Custom check of set parameter value with value of other parameter does not fail for wrong values.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

    }

    /** Helper parameter type for custom constraint checks. */
    private static ParameterTypeSpeed v1 = new ParameterTypeSpeed("v1", "v1long")
    {
        /** */
        private static final long serialVersionUID = 20160400L;

        @SuppressWarnings("synthetic-access")
        @Override
        public void check(final Speed v, final BehavioralCharacteristics bca) throws ParameterException
        {
            Throw.when(bca.contains(v2) && v.si > bca.getParameter(v2).si, ParameterException.class,
                    "Value of v1 is larger than value of v2.");
        }
    };

    /** Helper parameter type for custom constraint checks. */
    private static ParameterTypeSpeed v2 = new ParameterTypeSpeed("v2", "v2long")
    {
        /** */
        private static final long serialVersionUID = 20160400L;

        @SuppressWarnings("synthetic-access")
        @Override
        public void check(final Speed v, final BehavioralCharacteristics bca) throws ParameterException
        {
            Throw.when(bca.contains(v1) && v.si < bca.getParameter(v1).si, ParameterException.class,
                    "Value of v2 is smaller than value of v1.");
        }
    };

    /**
     * Tests the set/reset mechanism.
     * @throws ParameterException Should not be thrown, is for untested methods (in this test) that throw the exception.
     */
    @Test
    public final void setResetTest() throws ParameterException
    {
        ParameterTypeInteger a = new ParameterTypeInteger("a", "along", 0);

        // exception reset without set: no value -> reset
        BehavioralCharacteristics bc = new BehavioralCharacteristics();
        try
        {
            bc.resetParameter(a);
            fail("Reset of parameter that was never set does not fail.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // exception for get after reset to no value: no value -> set -> reset -> get
        bc = new BehavioralCharacteristics();
        bc.setParameter(a, 1);
        bc.resetParameter(a);
        try
        {
            bc.getParameter(a);
            fail("Get of parameter that was not given before set and reset, does not fail.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // exception for multiple resets: no value -> set -> reset -> reset
        bc = new BehavioralCharacteristics();
        bc.setParameter(a, 1);
        bc.resetParameter(a);
        try
        {
            bc.resetParameter(a);
            fail("Second reset without intermediate set does not fail when first reset was to no value.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // exception for multiple resets: set -> set -> reset -> reset
        bc = new BehavioralCharacteristics();
        bc.setParameter(a, 1);
        bc.setParameter(a, 2);
        bc.resetParameter(a);
        try
        {
            bc.resetParameter(a);
            fail("Second reset without intermediate set does not fail when first reset was to a value.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // no exception: set -> reset -> set -> reset
        bc = new BehavioralCharacteristics();
        bc.setParameter(a, 1);
        bc.resetParameter(a);
        bc.setParameter(a, 2);
        try
        {
            bc.resetParameter(a);
        }
        catch (ParameterException pe)
        {
            fail("Reset fails after set, with reset before that set.");
            // Should not fail
        }

        // same value: set(1) -> set(2) -> reset -> get(1?)
        bc = new BehavioralCharacteristics();
        bc.setParameter(a, 1);
        bc.setParameter(a, 2);
        bc.resetParameter(a);
        assertEquals("Value after reset should be the same as before last set.", 1, bc.getParameter(a));

        // If null value is ever going to be allowed, use these tests to check proper set/reset.
        // // check null is not the same as 'no value': no value -> set(null) -> reset -> get
        // // (null setting does not work on primitive data types, parameter type 'a' cannot be used)
        // ParameterTypeFrequency b = new ParameterTypeFrequency("b", "blong");
        // bc = new BehavioralCharacteristics();
        // bc.setParameter(b, null);
        // bc.resetParameter(b);
        // try
        // {
        // // as there was no value before the null set, this should fail
        // bc.getParameter(b);
        // fail("Reset after setting of null is not properly handled.");
        // }
        // catch (ParameterException pe)
        // {
        // // should fail
        // }
        //
        // // check null is not the same as no value: no value -> set(null) -> set(value) -> reset -> get(null?)
        // bc.setParameter(b, null);
        // bc.setParameter(b, new Frequency(12, FrequencyUnit.SI));
        // bc.resetParameter(b);
        // // assertEquals() with null cannot be used (defaults into deprecated array method)
        // if (bc.getParameter(b) != null)
        // {
        // fail("Value after reset is not equal to null, which it was before the last set.");
        // }

    }

    /**
     * Tests equalizations.
     * @throws ParameterException Should not be thrown, is for untested methods (in this test) that throw the exception.
     */
    @Test
    public final void equalizeTest() throws ParameterException
    {
        // equal double values from different parameters should be equal
        ParameterTypeDouble a1 = new ParameterTypeDouble("a", "along", 0.0);
        ParameterTypeDouble a2 = new ParameterTypeDouble("a", "along", 0.0);
        BehavioralCharacteristics bc1 = new BehavioralCharacteristics();
        BehavioralCharacteristics bc2 = new BehavioralCharacteristics();
        bc1.setParameter(a1, 4.0);
        bc2.setParameter(a2, 4.0);
        assertEquals("Equal double values from different parameter types should be equal.", bc1.getParameter(a1),
                bc2.getParameter(a2), 0.0);

        // equal DoubleScalar.Rel values should be equal from different characteristic sets
        ParameterTypeLinearDensity b1 = new ParameterTypeLinearDensity("b", "blong");
        ParameterTypeLinearDensity b2 = new ParameterTypeLinearDensity("b", "blong");
        bc1.setParameter(b1, new LinearDensity(4.0, LinearDensityUnit.SI));
        bc2.setParameter(b2, new LinearDensity(4.0, LinearDensityUnit.SI));
        assertEquals(
                "Equal DoubleScalar.Rel values from different parameter types and different characteristics should be equal.",
                bc1.getParameter(b1), bc2.getParameter(b2));

        // equal DoubleScalar.Rel values should be equal from the same characteristic set
        bc1.setParameter(b2, new LinearDensity(4.0, LinearDensityUnit.SI));
        assertEquals(
                "Equal DoubleScalar.Rel values from different parameter types and the same characteristics should be equal.",
                bc1.getParameter(b1), bc1.getParameter(b2));

        // values of parameter types with different value classes are not equal
        bc1.setParameter(a1, 4.0);
        bc1.setParameter(b1, new LinearDensity(4.0, LinearDensityUnit.SI));
        assertNotEquals("Values of different parameter type value classes should not be equal.", bc1.getParameter(a1),
                bc1.getParameter(b1));

    }

    /**
     * Test that exceptions are thrown when trying to use null values.
     */
    @Test
    public final void testNullNotAllowed()
    {
        // null default value
        try
        {
            new ParameterType<SpeedUnit, Speed>("v", "vlong", Speed.class, null, POSITIVE);
            fail("Setting a default value of 'null' on ParameterType did not fail.");
        }
        catch (RuntimeException re)
        {
            // should fail
        }
        try
        {
            new ParameterTypeSpeed("v", "vlong", null, POSITIVE);
            fail("Setting a default value of 'null' on ParameterTypeSpeed did not fail.");
        }
        catch (RuntimeException re)
        {
            // should fail
        }

        // set null value
        ParameterType<SpeedUnit, Speed> v = new ParameterType<SpeedUnit, Speed>("v", "vlong", Speed.class);
        BehavioralCharacteristics bc = new BehavioralCharacteristics();
        try
        {
            bc.setParameter(v, null);
            fail("Setting a value of 'null' did not fail.");
        }
        catch (ParameterException pe)
        {
            // should fail
        }

    }

    /**
     * Checks whether default values are properly set, or not in case not given.
     * @throws SecurityException Reflection.
     * @throws NoSuchMethodException Reflection.
     * @throws InvocationTargetException Reflection.
     * @throws IllegalArgumentException Reflection.
     * @throws IllegalAccessException Reflection.
     * @throws InstantiationException Reflection.
     */
    @Test
    public final void checkDefaultValues() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException
    {
        // @formatter:off
        checkDefaultValuesPerClass(ParameterType.class,              new Speed(3, SpeedUnit.SI));
        checkDefaultValuesPerClass(ParameterTypeSpeed.class,         new Speed(3, SpeedUnit.SI));
        checkDefaultValuesPerClass(ParameterTypeAcceleration.class,  new Acceleration(3, AccelerationUnit.SI));
        checkDefaultValuesPerClass(ParameterTypeLength.class,        new Length(3, LengthUnit.SI));
        checkDefaultValuesPerClass(ParameterTypeFrequency.class,     new Frequency(3, FrequencyUnit.SI));
        checkDefaultValuesPerClass(ParameterTypeDuration.class,          new Duration(3, TimeUnit.SI));
        checkDefaultValuesPerClass(ParameterTypeLinearDensity.class, new LinearDensity(3, LinearDensityUnit.SI));
        checkDefaultValuesPerClass(ParameterTypeBoolean.class,       new Boolean(false));
        checkDefaultValuesPerClass(ParameterTypeDouble.class,        new Double(3));
        checkDefaultValuesPerClass(ParameterTypeInteger.class,       new Integer(3));
        // @formatter:on
    }

    /**
     * @param clazz AbstractParameterType subclass to test.
     * @param defaultValue Default value to test with.
     * @param <R> Subclass of AbstractParameterType.
     * @throws SecurityException Reflection.
     * @throws NoSuchMethodException Reflection.
     * @throws InvocationTargetException Reflection.
     * @throws IllegalArgumentException Reflection.
     * @throws IllegalAccessException Reflection.
     * @throws InstantiationException Reflection.
     */
    private <R extends AbstractParameterType<?, ?>> void checkDefaultValuesPerClass(final Class<R> clazz,
            final Object defaultValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException
    {

        // none set
        AbstractParameterType<?, ?> ld;
        if (clazz.equals(ParameterType.class))
        {
            ld =
                    clazz.getDeclaredConstructor(String.class, String.class, Class.class).newInstance("v", "vcong",
                            getClass(defaultValue));
        }
        else
        {
            ld = clazz.getDeclaredConstructor(String.class, String.class).newInstance("v", "vcong");
        }
        try
        {
            ld.getDefaultValue();
            fail("Could obtain a default value that was not set.");
        }
        catch (ParameterException pe)
        {
            // should fail
        }
        // none set, including default check
        if (!clazz.equals(ParameterTypeBoolean.class)) // boolean has no checks
        {
            if (clazz.equals(ParameterType.class))
            {
                ld =
                        clazz.getDeclaredConstructor(String.class, String.class, Class.class, Check.class).newInstance("v",
                                "vcong", getClass(defaultValue), POSITIVE);
            }
            else
            {
                ld = clazz.getDeclaredConstructor(String.class, String.class, Check.class).newInstance("v", "vcong", POSITIVE);
            }
            try
            {
                ld.getDefaultValue();
                fail("Could obtain a default value that was not set.");
            }
            catch (ParameterException pe)
            {
                // should fail
            }
        }

        // value set
        if (clazz.equals(ParameterType.class))
        {
            ld =
                    clazz.getDeclaredConstructor(String.class, String.class, Class.class, DoubleScalar.Rel.class).newInstance(
                            "v", "vcong", getClass(defaultValue), defaultValue);
        }
        else
        {
            ld =
                    clazz.getDeclaredConstructor(String.class, String.class, getClass(defaultValue)).newInstance("v", "vcong",
                            defaultValue);
        }
        try
        {
            ld.getDefaultValue();
        }
        catch (ParameterException pe)
        {
            fail("Could not obtain a default value that was set.");
        }
        // value set, including default check
        if (!clazz.equals(ParameterTypeBoolean.class)) // boolean has no checks
        {
            if (clazz.equals(ParameterType.class))
            {
                ld =
                        clazz.getDeclaredConstructor(String.class, String.class, Class.class, DoubleScalar.Rel.class,
                                Check.class).newInstance("v", "vcong", getClass(defaultValue), defaultValue, POSITIVE);
            }
            else
            {
                ld =
                        clazz.getDeclaredConstructor(String.class, String.class, getClass(defaultValue), Check.class)
                                .newInstance("v", "vcong", defaultValue, POSITIVE);
            }
            try
            {
                ld.getDefaultValue();
            }
            catch (ParameterException pe)
            {
                fail("Could not obtain a default value that was set.");
            }
        }

    }

    /**
     * Returns the class of given default value for reflection.
     * @param defaultValue Default value.
     * @return Class of given default value for reflection.
     */
    private Class<? extends Object> getClass(final Object defaultValue)
    {
        if (defaultValue instanceof Boolean)
        {
            return Boolean.TYPE;
        }
        else if (defaultValue instanceof Double)
        {
            return Double.TYPE;
        }
        else if (defaultValue instanceof Integer)
        {
            return Integer.TYPE;
        }
        return defaultValue.getClass();
    }

}
