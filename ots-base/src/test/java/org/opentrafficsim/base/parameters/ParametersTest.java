package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.Constraint;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class ParametersTest implements ConstraintInterface
{

    /**
     * Defaults tests.
     */
    @Test
    public final void defaultsTest()
    {
        Parameters params = new ParameterSet().setDefaultParameters(ParameterTypes.class);
        try
        {
            assertTrue(params.getParameter(ParameterTypes.A).equals(ParameterTypes.A.getDefaultValue()),
                    "Default value is not correctly set.");
        }
        catch (ParameterException exception)
        {
            fail("Default value is not set at all.");
        }
    }

    /**
     * Constructor tests.
     */
    @Test
    public final void constructorTest()
    {
        // Check Parameters constructor
        ParameterSet params = new ParameterSet();
        assertNotNull(params, "Default constructor should not return null.");
        if (!params.getParameters().isEmpty())
        {
            fail("Constructed Parameters has a non-empty parameter map.");
        }

        // Check ParameterType construction (id, description, class, defaultValue)
        Length defaultValue = new Length(1.0, LengthUnit.SI);
        ParameterTypeLength a = new ParameterTypeLength("a", "along", defaultValue);
        assertEquals("a", a.getId(), "Parameter type id not properly set.");
        assertEquals("along", a.getDescription(), "Parameter type description not properly set.");
        assertTrue(a.hasDefaultValue(), "has a default value");
        try
        {
            assertEquals(defaultValue, a.getDefaultValue(), "Parameter type default value not properly set.");
        }
        catch (ParameterException exception)
        {
            fail("Parameter type default value given in constructor was not set.");
        }

        // Check ParameterType construction (id, description, class)
        ParameterTypeLength b = new ParameterTypeLength("b", "blong");
        assertEquals("b", b.getId(), "Parameter type id not properly set.");
        assertEquals("blong", b.getDescription(), "Parameter type description not properly set.");
        assertFalse(b.hasDefaultValue(), "does not have a default value");
        try
        {
            b.getDefaultValue();
            fail("Parameter type returned a default value, while none was provided.");
        }
        catch (ParameterException pe)
        {
            // ignore expected exception
        }
        assertTrue(b.toString().contains("ParameterType"), "toString returns something with ParameterType in it");
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
        checkDefaultValue(1.0, ATLEASTONE, false);
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
        checkDefaultValue(0.99, ATLEASTONE, true);

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
        checkSetValue(1.0, ATLEASTONE, false);
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
        checkSetValue(0.99, ATLEASTONE, true);
    }

    /**
     * Checks a default value.
     * @param value Value to check.
     * @param constraint Constraint to perform.
     * @param shouldFail Whether the check should fail.
     */
    private void checkDefaultValue(final double value, final Constraint<Number> constraint, final boolean shouldFail)
    {
        try
        {
            new ParameterTypeAcceleration("a", "along", new Acceleration(value, AccelerationUnit.SI), constraint);
            if (shouldFail)
            {
                fail("Default value " + value + " fails default " + constraint + " constraint.");
            }
        }
        catch (RuntimeException re)
        {
            if (!shouldFail)
            {
                re.printStackTrace();
                fail("Default value " + value + " does not fail default " + constraint + " constraint.");
            }
        }
    }

    /**
     * Checks a set value.
     * @param value Value to check.
     * @param constraint Constraint to perform.
     * @param shouldFail Whether the check should fail.
     */
    private void checkSetValue(final double value, final Constraint<Number> constraint, final boolean shouldFail)
    {
        try
        {
            Parameters params = new ParameterSet();
            ParameterTypeAcceleration a = new ParameterTypeAcceleration("a", "along", constraint);
            params.setParameter(a, new Acceleration(value, AccelerationUnit.SI));
            if (shouldFail)
            {
                fail("Set value " + value + " fails default " + constraint + " constraint.");
            }
        }
        catch (ParameterException pe)
        {
            if (!shouldFail)
            {
                fail("Set value " + value + " does not fail default " + constraint + " constraint.");
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
        Parameters params = new ParameterSet();
        try
        {
            // requirement: v1 < v2
            params.setParameter(v1, new Speed(3.0, SpeedUnit.KM_PER_HOUR));
            params.setParameter(v2, new Speed(4.0, SpeedUnit.KM_PER_HOUR));
            params.setParameter(v1, new Speed(2.0, SpeedUnit.KM_PER_HOUR));
            params.setParameter(v2, new Speed(5.0, SpeedUnit.KM_PER_HOUR));
        }
        catch (ParameterException pe)
        {
            fail("Custom check of set parameter value with value of other parameter fails for correct values.");
        }

        // Check values that should not work, set v1 first
        params = new ParameterSet();
        try
        {
            // requirement: v1 < v2
            params.setParameter(v1, new Speed(3.0, SpeedUnit.KM_PER_HOUR));
            params.setParameter(v2, new Speed(2.0, SpeedUnit.KM_PER_HOUR));
            fail("Custom check of set parameter value with value of other parameter does not fail for wrong values.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // Check values that should not work, set v2 first
        params = new ParameterSet();
        try
        {
            // requirement: v1 < v2
            params.setParameter(v2, new Speed(2.0, SpeedUnit.KM_PER_HOUR));
            params.setParameter(v1, new Speed(3.0, SpeedUnit.KM_PER_HOUR));
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
        public void check(final Speed v, final Parameters paramsa) throws ParameterException
        {
            Speed u2 = paramsa.getParameterOrNull(v2);
            Throw.when(u2 != null && v.si > u2.si, ParameterException.class, "Value of v1 is larger than value of v2.");
        }
    };

    /** Helper parameter type for custom constraint checks. */
    private static ParameterTypeSpeed v2 = new ParameterTypeSpeed("v2", "v2long")
    {
        /** */
        private static final long serialVersionUID = 20160400L;

        @SuppressWarnings("synthetic-access")
        @Override
        public void check(final Speed v, final Parameters paramsa) throws ParameterException
        {
            Speed u1 = paramsa.getParameterOrNull(v1);
            Throw.when(u1 != null && v.si < u1.si, ParameterException.class, "Value of v2 is smaller than value of v1.");
        }
    };

    /**
     * Tests the set/reset mechanism.
     * @throws ParameterException Should not be thrown, is for untested methods (in this test) that throw the exception.
     */
    @SuppressWarnings("cast")
    @Test
    public final void setResetTest() throws ParameterException
    {
        ParameterTypeInteger a = new ParameterTypeInteger("a", "along", 0);

        // exception reset without set: no value -> reset
        Parameters params = new ParameterSet();
        try
        {
            params.resetParameter(a);
            fail("Reset of parameter that was never set does not fail.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // exception for get after reset to no value: no value -> set -> reset -> get
        params = new ParameterSet();
        params.setParameterResettable(a, 1);
        params.resetParameter(a);
        try
        {
            params.getParameter(a);
            fail("Get of parameter that was not given before set and reset, does not fail.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // exception for multiple resets: no value -> set -> reset -> reset
        params = new ParameterSet();
        params.setParameterResettable(a, 1);
        params.resetParameter(a);
        try
        {
            params.resetParameter(a);
            fail("Second reset without intermediate set does not fail when first reset was to no value.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // exception for multiple resets: set -> set -> reset -> reset
        params = new ParameterSet();
        params.setParameterResettable(a, 1);
        params.setParameterResettable(a, 2);
        params.resetParameter(a);
        try
        {
            params.resetParameter(a);
            fail("Second reset without intermediate set does not fail when first reset was to a value.");
        }
        catch (ParameterException pe)
        {
            // Should fail
        }

        // no exception: set -> reset -> set -> reset
        params = new ParameterSet();
        params.setParameterResettable(a, 1);
        params.resetParameter(a);
        params.setParameterResettable(a, 2);
        try
        {
            params.resetParameter(a);
        }
        catch (ParameterException pe)
        {
            fail("Reset fails after set, with reset before that set.");
            // Should not fail
        }

        // same value: set(1) -> set(2) -> reset -> get(1?)
        params = new ParameterSet();
        params.setParameterResettable(a, 1);
        params.setParameterResettable(a, 2);
        params.resetParameter(a);
        assertEquals(1.0, (double) params.getParameter(a), 0.0, "Value after reset should be the same as before last set.");

        // no reset after (none resettable) set
        params = new ParameterSet();
        params.setParameter(a, 1);
        try
        {
            params.resetParameter(a);
            fail("Reset should fail after regular set.");
        }
        catch (ParameterException pe)
        {
            // should fail
        }

        // no reset after (none resettable) set, even with resettable set before
        params = new ParameterSet();
        params.setParameterResettable(a, 1);
        params.setParameter(a, 2);
        try
        {
            params.resetParameter(a);
            fail("Reset should fail after regular set, dispite resettable set before.");
        }
        catch (ParameterException pe)
        {
            // should fail
        }

        // same value: regular set(1) -> set(2) -> reset -> get(1?)
        params = new ParameterSet();
        params.setParameter(a, 1);
        params.setParameterResettable(a, 2);
        params.resetParameter(a);
        assertEquals(1.0, (double) params.getParameter(a), 0.0, "Value after reset should be the same as before last set.");

        // If null value is ever going to be allowed, use these tests to check proper set/reset.
        // // check null is not the same as 'no value': no value -> set(null) -> reset -> get
        // // (null setting does not work on primitive data types, parameter type 'a' cannot be used)
        // ParameterTypeFrequency b = new ParameterTypeFrequency("b", "blong");
        // bc = new BehavioralCharacteristics();
        // params.setParameter(b, null);
        // bc.resetParameter(b);
        // try
        // {
        // // as there was no value before the null set, this should fail
        // params.getParameter(b);
        // fail("Reset after setting of null is not properly handled.");
        // }
        // catch (ParameterException pe)
        // {
        // // should fail
        // }
        //
        // // check null is not the same as no value: no value -> set(null) -> set(value) -> reset -> get(null?)
        // params.setParameter(b, null);
        // params.setParameter(b, new Frequency(12, FrequencyUnit.SI));
        // bc.resetParameter(b);
        // // assertEquals() with null cannot be used (defaults into deprecated array method)
        // if (params.getParameter(b) != null)
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
        Parameters params1 = new ParameterSet();
        Parameters params2 = new ParameterSet();
        params1.setParameter(a1, 4.0);
        params2.setParameter(a2, 4.0);
        assertEquals(params1.getParameter(a1), params2.getParameter(a2), 0.0,
                "Equal double values from different parameter types should be equal.");

        // equal DoubleScalar.Rel values should be equal from different characteristic sets
        ParameterTypeLinearDensity b1 = new ParameterTypeLinearDensity("b", "blong");
        ParameterTypeLinearDensity b2 = new ParameterTypeLinearDensity("b", "blong");
        params1.setParameter(b1, new LinearDensity(4.0, LinearDensityUnit.SI));
        params2.setParameter(b2, new LinearDensity(4.0, LinearDensityUnit.SI));
        assertEquals(params1.getParameter(b1), params2.getParameter(b2),
                "Equal DoubleScalar.Rel values from different parameter types and different characteristics should be equal.");

        // equal DoubleScalar.Rel values should be equal from the same characteristic set
        params1.setParameter(b2, new LinearDensity(4.0, LinearDensityUnit.SI));
        assertEquals(params1.getParameter(b1), params1.getParameter(b2),
                "Equal DoubleScalar.Rel values from different parameter types and the same characteristics should be equal.");

        // values of parameter types with different value classes are not equal
        params1.setParameter(a1, 4.0);
        params1.setParameter(b1, new LinearDensity(4.0, LinearDensityUnit.SI));
        assertNotEquals(params1.getParameter(a1), params1.getParameter(b1),
                "Values of different parameter type value classes should not be equal.");

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
            new ParameterTypeSpeed("v", "vlong", null, POSITIVE);
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
        ParameterTypeSpeed v = new ParameterTypeSpeed("v", "vlong");
        Parameters params = new ParameterSet();
        try
        {
            params.setParameter(v, null);
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
        checkDefaultValuesPerClass(ParameterTypeSpeed.class,         Speed.instantiateSI(3));
        checkDefaultValuesPerClass(ParameterTypeAcceleration.class,  Acceleration.instantiateSI(3));
        checkDefaultValuesPerClass(ParameterTypeLength.class,        Length.instantiateSI(3));
        checkDefaultValuesPerClass(ParameterTypeFrequency.class,     Frequency.instantiateSI(3));
        checkDefaultValuesPerClass(ParameterTypeDuration.class,      Duration.instantiateSI(3));
        checkDefaultValuesPerClass(ParameterTypeLinearDensity.class, LinearDensity.instantiateSI(3));
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
    private <R extends ParameterType<?>> void checkDefaultValuesPerClass(final Class<R> clazz, final Object defaultValue)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException
    {
        // none set
        ParameterType<?> ld;
        if (clazz.equals(ParameterTypeNumeric.class))
        {
            ld = clazz.getDeclaredConstructor(String.class, String.class, Class.class).newInstance("v", "vcong",
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
        String toStringResult = ld.toString();
        // System.out.println("tostring yields \"" + toStringResult + "\"");
        // System.out.println("clazz is " + clazz.getSimpleName());
        assertTrue(toStringResult.contains(clazz.getSimpleName()), "toString returns something with the class name in it ");

        // none set, including default check
        if (!clazz.equals(ParameterTypeBoolean.class)) // boolean has no checks
        {
            if (clazz.equals(ParameterTypeNumeric.class))
            {
                ld = clazz.getDeclaredConstructor(String.class, String.class, Class.class, Constraint.class).newInstance("v",
                        "vcong", getClass(defaultValue), POSITIVE);
            }
            else
            {
                ld = clazz.getDeclaredConstructor(String.class, String.class, Constraint.class).newInstance("v", "vcong",
                        POSITIVE);
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
        if (clazz.equals(ParameterTypeNumeric.class))
        {
            ld = clazz.getDeclaredConstructor(String.class, String.class, Class.class, Number.class).newInstance("v", "vcong",
                    getClass(defaultValue), defaultValue);
        }
        else
        {
            ld = clazz.getDeclaredConstructor(String.class, String.class, getClass(defaultValue)).newInstance("v", "vcong",
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
            if (clazz.equals(ParameterTypeNumeric.class))
            {
                ld = clazz.getDeclaredConstructor(String.class, String.class, Class.class, Number.class, Constraint.class)
                        .newInstance("v", "vcong", getClass(defaultValue), defaultValue, POSITIVE);
            }
            else
            {
                ld = clazz.getDeclaredConstructor(String.class, String.class, defaultValue.getClass(), Constraint.class)
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

    /**
     * Tests the merging of parameter sets using setAll.
     * @throws ParameterException parameter exception
     */
    @Test
    public final void mergeTest() throws ParameterException
    {
        ParameterSet paramsA = new ParameterSet();
        paramsA.setDefaultParameter(ParameterTypes.A);
        ParameterSet paramsB = new ParameterSet();
        paramsB.setDefaultParameter(ParameterTypes.B);
        paramsB.setAllIn(paramsA);
        assertTrue(paramsA.contains(ParameterTypes.B),
                "When merging set B with set A, set A should contain the parameters of set B.");
        assertTrue(paramsA.getParameter(ParameterTypes.B).eq(paramsB.getParameter(ParameterTypes.B)),
                "When merging set B with set A, parameter values should be equal.");
        assertFalse(paramsB.contains(ParameterTypes.A),
                "When merging set B with set A, set B should not contain the parameters of set A.");
    }

}
