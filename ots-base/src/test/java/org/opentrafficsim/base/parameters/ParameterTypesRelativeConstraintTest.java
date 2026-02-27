package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests relative constraints in standard parameter types.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParameterTypesRelativeConstraintTest
{

    /**
     * Test relative constraints.
     * @throws ParameterException when values do not comply to relative constraints
     */
    @Test
    void testPairs() throws ParameterException
    {
        testPair(ParameterTypes.TMIN, ParameterTypes.TMAX);
        testPair(ParameterTypes.B0, ParameterTypes.B);
        testPair(ParameterTypes.B0, ParameterTypes.BCRIT);
        testPair(ParameterTypes.B, ParameterTypes.BCRIT);
    }

    /**
     * Test pair in different order of adding to set and either their own or the other's default value.
     * @param <T> value type
     * @param pt1 parameter type 1
     * @param pt2 parameter type 2
     * @throws ParameterException when values do not comply to relative constraints
     */
    private <T> void testPair(final ParameterType<T> pt1, final ParameterType<T> pt2) throws ParameterException
    {
        ParameterSet parameters1 = new ParameterSet();
        parameters1.setDefaultParameter(pt1);
        parameters1.setDefaultParameter(pt2);

        ParameterSet parameters2 = new ParameterSet();
        parameters2.setDefaultParameter(pt2);
        parameters2.setDefaultParameter(pt1);

        ParameterSet parameters3 = new ParameterSet();
        parameters3.setDefaultParameter(pt1);
        assertThrows(ParameterException.class, () -> parameters3.setParameter(pt2, pt1.getDefaultValue()));

        ParameterSet parameters4 = new ParameterSet();
        parameters4.setDefaultParameter(pt2);
        assertThrows(ParameterException.class, () -> parameters4.setParameter(pt1, pt2.getDefaultValue()));
    }

}
