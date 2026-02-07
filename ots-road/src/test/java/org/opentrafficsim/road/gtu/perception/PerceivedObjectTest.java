package org.opentrafficsim.road.gtu.perception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.test.UnitTest;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics;

/**
 * Test PerceivedObject.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class PerceivedObjectTest
{

    /** */
    private PerceivedObjectTest()
    {
        // do not instantiate test class
    }

    /**
     * Tests kinematics and overlap.
     */
    @Test
    public void testKinematicsAndOverlap()
    {
        // other longer
        final Length otherLength = Length.ofSI(12.0);
        final Length egoLength = Length.ofSI(4.0);

        // downstream longer
        testSingleDistance(true, Length.ofSI(10.0), null, null, null, otherLength, egoLength);
        testSingleDistance(true, Length.ofSI(5.0), null, null, null, otherLength, egoLength);
        testSingleDistance(true, Length.ofSI(0.0), null, null, null, otherLength, egoLength);
        testSingleDistance(true, Length.ofSI(-1.0), Length.ofSI(3.0), Length.ofSI(1.0), Length.ofSI(11.0), otherLength,
                egoLength);
        testSingleDistance(true, Length.ofSI(-3.0), Length.ofSI(1.0), Length.ofSI(3.0), Length.ofSI(9.0), otherLength,
                egoLength);
        testSingleDistance(true, Length.ofSI(-4.0), Length.ofSI(0.0), Length.ofSI(4.0), Length.ofSI(8.0), otherLength,
                egoLength);
        testSingleDistance(true, Length.ofSI(-6.0), Length.ofSI(-2.0), Length.ofSI(4.0), Length.ofSI(6.0), otherLength,
                egoLength);
        testSingleDistance(true, Length.ofSI(-7.0), Length.ofSI(-3.0), Length.ofSI(4.0), Length.ofSI(5.0), otherLength,
                egoLength);
        testSingleDistance(true, Length.ofSI(-12.0), Length.ofSI(-8.0), Length.ofSI(4.0), Length.ofSI(0.0), otherLength,
                egoLength);
        testSingleDistance(true, Length.ofSI(-13.0), Length.ofSI(-9.0), Length.ofSI(3.0), Length.ofSI(-1.0), otherLength,
                egoLength);
        testSingleDistance(true, Length.ofSI(-15.0), Length.ofSI(-11.0), Length.ofSI(1.0), Length.ofSI(-3.0), otherLength,
                egoLength);
        testSingleDistance(true, Length.ofSI(-16.0), Length.ofSI(-12.0), Length.ofSI(0.0), Length.ofSI(-4.0), otherLength,
                egoLength);
        UnitTest.testFail(() -> testSingleDistance(true, Length.ofSI(-17.0), Length.ofSI(-12.0), Length.ofSI(0.0),
                Length.ofSI(-4.0), otherLength, egoLength), IllegalArgumentException.class);

        // upstream longer
        testSingleDistance(false, Length.ofSI(10.0), null, null, null, otherLength, egoLength);
        testSingleDistance(false, Length.ofSI(5.0), null, null, null, otherLength, egoLength);
        testSingleDistance(false, Length.ofSI(0.0), null, null, null, otherLength, egoLength);
        testSingleDistance(false, Length.ofSI(-1.0), Length.ofSI(-11.0), Length.ofSI(1.0), Length.ofSI(-3.0), otherLength,
                egoLength);
        testSingleDistance(false, Length.ofSI(-3.0), Length.ofSI(-9.0), Length.ofSI(3.0), Length.ofSI(-1.0), otherLength,
                egoLength);
        testSingleDistance(false, Length.ofSI(-4.0), Length.ofSI(-8.0), Length.ofSI(4.0), Length.ofSI(-0.0), otherLength,
                egoLength);
        testSingleDistance(false, Length.ofSI(-6.0), Length.ofSI(-6.0), Length.ofSI(4.0), Length.ofSI(2.0), otherLength,
                egoLength);
        testSingleDistance(false, Length.ofSI(-7.0), Length.ofSI(-5.0), Length.ofSI(4.0), Length.ofSI(3.0), otherLength,
                egoLength);
        testSingleDistance(false, Length.ofSI(-12.0), Length.ofSI(0.0), Length.ofSI(4.0), Length.ofSI(8.0), otherLength,
                egoLength);
        testSingleDistance(false, Length.ofSI(-13.0), Length.ofSI(1.0), Length.ofSI(3.0), Length.ofSI(9.0), otherLength,
                egoLength);
        testSingleDistance(false, Length.ofSI(-15.0), Length.ofSI(3.0), Length.ofSI(1.0), Length.ofSI(11.0), otherLength,
                egoLength);
        testSingleDistance(false, Length.ofSI(-16.0), Length.ofSI(4.0), Length.ofSI(0.0), Length.ofSI(12.0), otherLength,
                egoLength);
        UnitTest.testFail(() -> testSingleDistance(false, Length.ofSI(-17.0), Length.ofSI(-12.0), Length.ofSI(0.0),
                Length.ofSI(-4.0), otherLength, egoLength), IllegalArgumentException.class);

        // other shorter
        final Length otherLength2 = Length.ofSI(4.0);
        final Length egoLength2 = Length.ofSI(12.0);

        // downstream shorter
        testSingleDistance(true, Length.ofSI(10.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(true, Length.ofSI(5.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(true, Length.ofSI(0.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(true, Length.ofSI(-1.0), Length.ofSI(11.0), Length.ofSI(1.0), Length.ofSI(3.0), otherLength2,
                egoLength2);
        testSingleDistance(true, Length.ofSI(-3.0), Length.ofSI(9.0), Length.ofSI(3.0), Length.ofSI(1.0), otherLength2,
                egoLength2);
        testSingleDistance(true, Length.ofSI(-4.0), Length.ofSI(8.0), Length.ofSI(4.0), Length.ofSI(0.0), otherLength2,
                egoLength2);
        testSingleDistance(true, Length.ofSI(-6.0), Length.ofSI(6.0), Length.ofSI(4.0), Length.ofSI(-2.0), otherLength2,
                egoLength2);
        testSingleDistance(true, Length.ofSI(-7.0), Length.ofSI(5.0), Length.ofSI(4.0), Length.ofSI(-3.0), otherLength2,
                egoLength2);
        testSingleDistance(true, Length.ofSI(-12.0), Length.ofSI(0.0), Length.ofSI(4.0), Length.ofSI(-8.0), otherLength2,
                egoLength2);
        testSingleDistance(true, Length.ofSI(-13.0), Length.ofSI(-1.0), Length.ofSI(3.0), Length.ofSI(-9.0), otherLength2,
                egoLength2);
        testSingleDistance(true, Length.ofSI(-15.0), Length.ofSI(-3.0), Length.ofSI(1.0), Length.ofSI(-11.0), otherLength2,
                egoLength2);
        testSingleDistance(true, Length.ofSI(-16.0), Length.ofSI(-4.0), Length.ofSI(0.0), Length.ofSI(-12.0), otherLength2,
                egoLength2);
        UnitTest.testFail(() -> testSingleDistance(true, Length.ofSI(-17.0), Length.ofSI(-12.0), Length.ofSI(0.0),
                Length.ofSI(-4.0), otherLength2, egoLength2), IllegalArgumentException.class);

        // upstream shorter
        testSingleDistance(false, Length.ofSI(10.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(false, Length.ofSI(5.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(false, Length.ofSI(0.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(false, Length.ofSI(-1.0), Length.ofSI(-3.0), Length.ofSI(1.0), Length.ofSI(-11.0), otherLength2,
                egoLength2);
        testSingleDistance(false, Length.ofSI(-3.0), Length.ofSI(-1.0), Length.ofSI(3.0), Length.ofSI(-9.0), otherLength2,
                egoLength2);
        testSingleDistance(false, Length.ofSI(-4.0), Length.ofSI(-0.0), Length.ofSI(4.0), Length.ofSI(-8.0), otherLength2,
                egoLength2);
        testSingleDistance(false, Length.ofSI(-6.0), Length.ofSI(2.0), Length.ofSI(4.0), Length.ofSI(-6.0), otherLength2,
                egoLength2);
        testSingleDistance(false, Length.ofSI(-7.0), Length.ofSI(3.0), Length.ofSI(4.0), Length.ofSI(-5.0), otherLength2,
                egoLength2);
        testSingleDistance(false, Length.ofSI(-12.0), Length.ofSI(8.0), Length.ofSI(4.0), Length.ofSI(-0.0), otherLength2,
                egoLength2);
        testSingleDistance(false, Length.ofSI(-13.0), Length.ofSI(9.0), Length.ofSI(3.0), Length.ofSI(1.0), otherLength2,
                egoLength2);
        testSingleDistance(false, Length.ofSI(-15.0), Length.ofSI(11.0), Length.ofSI(1.0), Length.ofSI(3.0), otherLength2,
                egoLength2);
        testSingleDistance(false, Length.ofSI(-16.0), Length.ofSI(12.0), Length.ofSI(0.0), Length.ofSI(4.0), otherLength2,
                egoLength2);
        UnitTest.testFail(() -> testSingleDistance(false, Length.ofSI(-17.0), Length.ofSI(-12.0), Length.ofSI(0.0),
                Length.ofSI(-4.0), otherLength2, egoLength2), IllegalArgumentException.class);
    }

    /**
     * Test single case of overlap.
     * @param downstream downstream or upstream distance
     * @param headway headway value
     * @param overlapRear result rear overlap
     * @param overlap resulting overlap
     * @param overlapFront resulting front overlap
     * @param otherLength length of perceived object
     * @param egoLength ego length
     */
    private void testSingleDistance(final boolean downstream, final Length headway, final Length overlapRear,
            final Length overlap, final Length overlapFront, final Length otherLength, final Length egoLength)
    {
        Kinematics kinematics =
                downstream ? Kinematics.dynamicAhead(headway, Speed.ONE, Acceleration.ONE, false, otherLength, egoLength)
                        : Kinematics.dynamicBehind(headway, Speed.ONE, Acceleration.ONE, false, otherLength, egoLength);
        assertEquals(headway.si, kinematics.getDistance().si, 1e-6);
        assertNullableEquals(overlapRear, kinematics.getOverlap().getOverlapRear().orElse(null));
        assertNullableEquals(overlap, kinematics.getOverlap().getOverlap().orElse(null));
        assertNullableEquals(overlapFront, kinematics.getOverlap().getOverlapFront().orElse(null));
    }

    /**
     * Confirms that both lengths are equal within 1e-6, where they can also both be {@code null}.
     * @param length1 length one
     * @param length2 length two
     * @return {@code true} if both are null or both are not null and within 1e-6
     */
    private boolean assertNullableEquals(final Length length1, final Length length2)
    {
        if (length1 == null)
        {
            return length2 == null;
        }
        if (length2 == null)
        {
            return false;
        }
        return Math.abs(length1.si - length2.si) < 1e-6;
    }

}
