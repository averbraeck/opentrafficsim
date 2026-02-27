package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;

/**
 * Additional tests for methods not invoked elsewhere.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
class ParametersAdditionalTests
{

    /**
     * Test print value.
     * @throws ParameterException if value not present
     */
    @Test
    void testPrintValue() throws ParameterException
    {
        Parameters parameters = mock(Parameters.class);

        ParameterTypeDouble pt1 = new ParameterTypeDouble("id1", "descr1");
        when(parameters.getParameter(pt1)).thenReturn(1.0);
        assertTrue(pt1.printValue(parameters).contains("1"));

        ParameterTypeBoolean pt2 = new ParameterTypeBoolean("id2", "descr2");
        when(parameters.getParameter(pt2)).thenReturn(false);
        assertTrue(pt2.printValue(parameters).equals("false"));

        ParameterTypeInteger pt3 = new ParameterTypeInteger("id3", "descr3", 10, NumericConstraint.POSITIVEZERO);
        assertEquals("id3", pt3.getId());
        assertEquals("descr3", pt3.getDescription());
        assertEquals(10, pt3.getDefaultValue());
        assertEquals(NumericConstraint.POSITIVEZERO, pt3.getConstraint());
        when(parameters.getParameter(pt3)).thenReturn(5);
        assertTrue(pt3.printValue(parameters).equals("5"));

        ParameterTypeAcceleration pt4 = new ParameterTypeAcceleration("id4", "descr4");
        when(parameters.getParameter(pt4)).thenReturn(Acceleration.ZERO);
        assertTrue(pt4.printValue(parameters).contains("0"));

    }

    /**
     * Tests that exception is thrown when default value does not comply to custom constraint.
     */
    @Test
    void testDefaultValueOutsideConstraint()
    {
        assertThrows(OtsRuntimeException.class, () ->
        {
            new ParameterTypeNumeric<Integer>("id", "descr", Integer.class, 10)
            {
                @Override
                public void check(final Integer value, final Parameters params) throws ParameterException
                {
                    throw new ParameterException();
                }
            };
        });
    }

    /**
     * Tests equals.
     */
    @Test
    void testEquals()
    {
        ParameterTypeInteger pt1 = new ParameterTypeInteger("id1", "descr1", 10, NumericConstraint.POSITIVE);
        ParameterTypeInteger pt1b = new ParameterTypeInteger("id1", "descr1", 10, NumericConstraint.POSITIVE);
        ParameterTypeInteger pt2 = new ParameterTypeInteger("id1", "descr2", 10, NumericConstraint.POSITIVE);
        ParameterTypeInteger pt3 = new ParameterTypeInteger("id1", "descr1", 11, NumericConstraint.POSITIVE);
        ParameterTypeInteger pt4 = new ParameterTypeInteger("id1", "descr1", NumericConstraint.POSITIVE);
        assertTrue(pt1.equals(pt1));
        assertTrue(pt1.equals(pt1b));
        assertFalse(pt1.equals(null));
        assertFalse(pt1.equals(pt2));
        assertFalse(pt1.equals(pt3));
        assertFalse(pt4.equals(pt1));

        /**
         * Test class for equal class yet different value class.
         */
        class ParameterTypeObject extends ParameterType<Object>
        {
            /**
             * Constructor.
             * @param id id
             * @param description description
             * @param valueClass valueClass
             */
            ParameterTypeObject(final String id, final String description, final Class<Object> valueClass)
            {
                super(id, description, valueClass);
            }
        }

        ParameterTypeObject pt5 = new ParameterTypeObject("", "", Object.class);
        @SuppressWarnings("unchecked")
        ParameterTypeObject pt6 = new ParameterTypeObject("", "", (Class<Object>) (Object) Integer.class);
        assertFalse(pt5.equals(pt6));
        assertTrue(pt5.toString().contains("ParameterType"));
    }

    /**
     * Tests parameter set.
     * @throws ParameterException on wrong value
     */
    @Test
    void parameterSetTest() throws ParameterException
    {
        // copy set, copyOnWrite
        ParameterSet setA = new ParameterSet();
        ParameterSet setB = new ParameterSet(setA);
        setA.setParameter(ParameterTypes.A, Acceleration.ONE);
        setB.setParameter(ParameterTypes.A, ParameterTypes.A.getDefaultValue());
        assertNotEquals(setA.getParameter(ParameterTypes.A), setB.getParameter(ParameterTypes.A));

        // setAllIn
        Parameters setC = mock(Parameters.class);
        doNothing().when(setC).setAllIn(any());
        new ParameterSet(setC);
        setA.setAllIn(setC);

        // toString
        assertTrue(setA.toString().contains("Parameters"));

        /**
         * Test class to hold parameter types.
         */
        final class NoDefaultTypeHolder
        {
            /** Constructor. */
            private NoDefaultTypeHolder()
            {
            }

            /** Without default giving troubles. */
            @SuppressWarnings("unused")
            public static final ParameterTypeInteger type = new ParameterTypeInteger("int", "no-default int");
        }
        assertThrows(OtsRuntimeException.class, () -> setA.setDefaultParameters(NoDefaultTypeHolder.class));
    }

}
