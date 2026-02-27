package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Test for {@link ParameterTypeString}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
class ParameterTypeStringTest
{

    /**
     * Test constructors.
     * @throws ParameterException default value does not comply to constraint
     */
    @Test
    void testConstructors() throws ParameterException
    {
        ParameterTypeString pts = new ParameterTypeString("id1", "description1");
        assertEquals("id1", pts.getId());
        assertEquals("description1", pts.getDescription());
        assertEquals(String.class, pts.getValueClass());

        ParameterTypeString pts2 = new ParameterTypeString("id2", "description2", "default");
        assertEquals("id2", pts2.getId());
        assertEquals("description2", pts2.getDescription());
        assertEquals("default", pts2.getDefaultValue());

        @SuppressWarnings("unchecked")
        Constraint<String> constraint3 = mock(Constraint.class);
        ParameterTypeString pts3 = new ParameterTypeString("id3", "description3", constraint3);
        assertEquals("id3", pts3.getId());
        assertEquals("description3", pts3.getDescription());
        assertEquals(constraint3, pts3.getConstraint());

        @SuppressWarnings("unchecked")
        Constraint<String> constraint4 = mock(Constraint.class);
        when(constraint4.accept("default4")).thenReturn(true);
        ParameterTypeString pts4 = new ParameterTypeString("id4", "description4", "default4", constraint4);
        assertEquals("id4", pts4.getId());
        assertEquals("description4", pts4.getDescription());
        assertEquals("default4", pts4.getDefaultValue());
        assertEquals(constraint4, pts4.getConstraint());
    }

    /**
     * Test to string.
     */
    @Test
    void testToString()
    {
        ParameterTypeString pts = new ParameterTypeString("myId", "myDescription");
        String result = pts.toString();
        assertTrue(result.contains("myId"));
        assertTrue(result.contains("myDescription"));
        assertEquals("ParameterTypeString [id=myId, description=myDescription]", result);
    }
}
