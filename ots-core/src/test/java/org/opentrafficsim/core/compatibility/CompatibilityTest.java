package org.opentrafficsim.core.compatibility;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.HierarchicallyTyped;

/**
 * Test the classes and interfaces in the compatibility package.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class CompatibilityTest
{

    /**
     * Test Compatibility and GtuCompatibility.
     */
    @Test
    public void testCompatibility()
    {
        InfraType root = new InfraType("root");
        InfraType sub1 = new InfraType("sub1", root);
        InfraType sub2 = new InfraType("sub1", sub1);
        GtuCompatibility<InfraType> gc = new GtuCompatibility<>(root);
    }

    /** Infra belonging to InfraType. */
    static class Infra implements HierarchicallyTyped<InfraType, Infra>
    {
        @Override
        public InfraType getType()
        {
            return null;
        }
        
    }
    
    /** InfraType as a hierarchical type. */
    static class InfraType extends GtuCompatibleInfraType<InfraType, Infra>
    {
        /**
         * Instantiate an infrastructure type.
         * @param id String; the id
         * @param parent InfraType; the parent
         */
        InfraType(final String id, final InfraType parent)
        {
            super(id, parent);
        }

        /**
         * Instantiate an infrastructure type without a parent.
         * @param id String; the id
         */
        InfraType(final String id)
        {
            super(id);
        }
    }
}
