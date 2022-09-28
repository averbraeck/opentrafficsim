package org.opentrafficsim.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test the basics of the HierarchicalType class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class HierarchicalTypeTest
{

    /**
     * Test the basics of the HierarchicalType class.
     */
    @Test
    public final void testBasics()
    {
        SubType st = new SubType("id");
        assertEquals("id check", "id", st.getId());
        assertNull("parent type is null", st.getParent());
        assertTrue("type is type of st", st.isOfType(st));
        assertFalse("type is not of type null", st.isOfType(null));
        SubType st2 = new SubType("id2", st);
        assertEquals("id check", "id2", st2.getId());
        assertEquals("parent type is null", st, st2.getParent());
        assertTrue("parent is type of child", st2.isOfType(st));
        assertFalse("childis not of type of parent", st.isOfType(st2));
        assertTrue("hash codes should be different", st.hashCode() != st2.hashCode());
        assertFalse("st not equals to some String", st.equals("String"));
        SubType st3 = new SubType("id3", st);
        assertFalse("other subtype with different name is not equal", st2.equals(st3));
        st3 = new SubType("id2", st);
        assertTrue("other subtype with same name and parent is equal", st2.equals(st3));
        st3 = new SubType("id2");
        assertFalse("other subtype with same name but no parent is not equal", st2.equals(st3));
        assertFalse("other subtype with same name but no parent is not equal", st3.equals(st2));
        st3 = new SubType("id3", st2);
        SubType different = new SubType("different");
        assertNull("No common ancestor", different.commonAncestor(st3));
        assertNull("No common ancestor", st3.commonAncestor(different));
        SubType common = new SubType("common", st2);
        assertEquals("Common ancestor", st2, common.commonAncestor(st3));
        assertEquals("Common ancestor", st2, st3.commonAncestor(common));
        assertTrue("toString method returns something descriptive", common.toString().startsWith("SubType "));
        try
        {
            new SubType(null, common);
            fail("Constructor with null for type name should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new SubType(null);
            fail("Constructor with null for type name should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
    }

    /**
     * Extend the HierarchicalType class so we instantiate (and then test) things.
     */
    static class SubType extends HierarchicalType<SubType>
    {

        /**
         * Construct a new SubType instance.
         * @param id String; id of the new SubType
         * @param parent SubType; parent of the new SubType instance
         * @throws NullPointerException ...
         */
        SubType(final String id, final SubType parent) throws NullPointerException
        {
            super(id, parent);
        }

        /**
         * Construct a new SubType instance.
         * @param id String; id of the new SubType
         * @throws NullPointerException ...
         */
        SubType(final String id) throws NullPointerException
        {
            super(id);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("checkstyle:designforextension")
        @Override
        public String toString()
        {
            return "SubType [id=" + getId() + ", parent=" + getParent() + "]";
        }

    }
}
