package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Test for {@link ParameterTypeClass}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
class ParameterTypeClassTest
{

    /**
     * Test constructors.
     * @throws ParameterException default value does not comply to constraint
     */
    @Test
    void testConstructorWithoutDefaultAndConstraint() throws ParameterException
    {
        ParameterTypeClass<Number> pt1 = new ParameterTypeClass<>("id1", "desc1", Number.class);
        assertEquals("id1", pt1.getId());
        assertEquals("desc1", pt1.getDescription());
        assertNotNull(pt1.getValueClass());

        ParameterTypeClass<Number> pt2 = new ParameterTypeClass<>("id2", "desc2", Number.class, Integer.class);
        assertEquals("id2", pt2.getId());
        assertEquals("desc2", pt2.getDescription());
        assertEquals(Integer.class, pt2.getDefaultValue());

        @SuppressWarnings("unchecked")
        Constraint<Class<? extends Number>> constraint3 = mock(Constraint.class);
        ParameterTypeClass<Number> pt3 = new ParameterTypeClass<>("id3", "desc3", Number.class, constraint3);
        assertEquals("id3", pt3.getId());
        assertEquals("desc3", pt3.getDescription());
        assertEquals(constraint3, pt3.getConstraint());

        @SuppressWarnings("unchecked")
        Constraint<Class<? extends Number>> constraint4 = mock(Constraint.class);
        when(constraint4.accept(Integer.class)).thenReturn(true);
        ParameterTypeClass<Number> pt4 = new ParameterTypeClass<>("id4", "desc4", Number.class, Integer.class, constraint4);
        assertEquals("id4", pt4.getId());
        assertEquals("desc4", pt4.getDescription());
        assertEquals(Integer.class, pt4.getDefaultValue());
        assertEquals(constraint4, pt4.getConstraint());
    }

    /**
     * Test print value.
     * @throws ParameterException if value not present
     */
    @Test
    void testPrintValueReturnsSimpleName() throws ParameterException
    {
        ParameterTypeClass<Number> pt5 = new ParameterTypeClass<>("id5", "desc5", Number.class);
        Parameters parameters = mock(Parameters.class);
        // this way of setting the return value solves difficult generics (Class<Integer> not being a Class<? extends Number)
        doReturn(Integer.class).when(parameters).getParameter(pt5);
        String result = pt5.printValue(parameters);
        assertEquals("Integer", result);
        verify(parameters, times(1)).getParameter(pt5);

        ParameterTypeClass<Number> pt6 = new ParameterTypeClass<>("id6", "desc6", Number.class);
        when(parameters.getParameter(pt6)).thenThrow(new ParameterException("error"));
        assertThrows(ParameterException.class, () -> pt6.printValue(parameters));
        verify(parameters, times(1)).getParameter(pt6);
    }

    /**
     * Test to string.
     */
    @Test
    @SuppressWarnings("unchecked")
    void testToStringUsesConstraintToString()
    {
        Constraint<Class<? extends Number>> constraint = mock(Constraint.class);
        when(constraint.toString()).thenReturn("MyConstraint");
        ParameterTypeClass<Number> pt = new ParameterTypeClass<>("id7", "desc7", Number.class, constraint);
        String result = pt.toString();
        assertEquals("ParameterTypeClass [MyConstraint]", result);
    }

}
