package org.opentrafficsim.road.gtu.perception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics;

/**
 * Test PerceivedObject.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        final Length otherLength = Length.instantiateSI(12.0);
        final Length egoLength = Length.instantiateSI(4.0);

        // downstream longer
        testSingleDistance(true, Length.instantiateSI(10.0), null, null, null, otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(5.0), null, null, null, otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(0.0), null, null, null, otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-1.0), Length.instantiateSI(3.0), Length.instantiateSI(1.0),
                Length.instantiateSI(11.0), otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-3.0), Length.instantiateSI(1.0), Length.instantiateSI(3.0),
                Length.instantiateSI(9.0), otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-4.0), Length.instantiateSI(0.0), Length.instantiateSI(4.0),
                Length.instantiateSI(8.0), otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-6.0), Length.instantiateSI(-2.0), Length.instantiateSI(4.0),
                Length.instantiateSI(6.0), otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-7.0), Length.instantiateSI(-3.0), Length.instantiateSI(4.0),
                Length.instantiateSI(5.0), otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-12.0), Length.instantiateSI(-8.0), Length.instantiateSI(4.0),
                Length.instantiateSI(0.0), otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-13.0), Length.instantiateSI(-9.0), Length.instantiateSI(3.0),
                Length.instantiateSI(-1.0), otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-15.0), Length.instantiateSI(-11.0), Length.instantiateSI(1.0),
                Length.instantiateSI(-3.0), otherLength, egoLength);
        testSingleDistance(true, Length.instantiateSI(-16.0), Length.instantiateSI(-12.0), Length.instantiateSI(0.0),
                Length.instantiateSI(-4.0), otherLength, egoLength);
        Try.testFail(
                () -> testSingleDistance(true, Length.instantiateSI(-17.0), Length.instantiateSI(-12.0),
                        Length.instantiateSI(0.0), Length.instantiateSI(-4.0), otherLength, egoLength),
                IllegalArgumentException.class);

        // upstream longer
        testSingleDistance(false, Length.instantiateSI(10.0), null, null, null, otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(5.0), null, null, null, otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(0.0), null, null, null, otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-1.0), Length.instantiateSI(-11.0), Length.instantiateSI(1.0),
                Length.instantiateSI(-3.0), otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-3.0), Length.instantiateSI(-9.0), Length.instantiateSI(3.0),
                Length.instantiateSI(-1.0), otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-4.0), Length.instantiateSI(-8.0), Length.instantiateSI(4.0),
                Length.instantiateSI(-0.0), otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-6.0), Length.instantiateSI(-6.0), Length.instantiateSI(4.0),
                Length.instantiateSI(2.0), otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-7.0), Length.instantiateSI(-5.0), Length.instantiateSI(4.0),
                Length.instantiateSI(3.0), otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-12.0), Length.instantiateSI(0.0), Length.instantiateSI(4.0),
                Length.instantiateSI(8.0), otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-13.0), Length.instantiateSI(1.0), Length.instantiateSI(3.0),
                Length.instantiateSI(9.0), otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-15.0), Length.instantiateSI(3.0), Length.instantiateSI(1.0),
                Length.instantiateSI(11.0), otherLength, egoLength);
        testSingleDistance(false, Length.instantiateSI(-16.0), Length.instantiateSI(4.0), Length.instantiateSI(0.0),
                Length.instantiateSI(12.0), otherLength, egoLength);
        Try.testFail(
                () -> testSingleDistance(false, Length.instantiateSI(-17.0), Length.instantiateSI(-12.0),
                        Length.instantiateSI(0.0), Length.instantiateSI(-4.0), otherLength, egoLength),
                IllegalArgumentException.class);

        // other shorter
        final Length otherLength2 = Length.instantiateSI(4.0);
        final Length egoLength2 = Length.instantiateSI(12.0);

        // downstream shorter
        testSingleDistance(true, Length.instantiateSI(10.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(5.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(0.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-1.0), Length.instantiateSI(11.0), Length.instantiateSI(1.0),
                Length.instantiateSI(3.0), otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-3.0), Length.instantiateSI(9.0), Length.instantiateSI(3.0),
                Length.instantiateSI(1.0), otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-4.0), Length.instantiateSI(8.0), Length.instantiateSI(4.0),
                Length.instantiateSI(0.0), otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-6.0), Length.instantiateSI(6.0), Length.instantiateSI(4.0),
                Length.instantiateSI(-2.0), otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-7.0), Length.instantiateSI(5.0), Length.instantiateSI(4.0),
                Length.instantiateSI(-3.0), otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-12.0), Length.instantiateSI(0.0), Length.instantiateSI(4.0),
                Length.instantiateSI(-8.0), otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-13.0), Length.instantiateSI(-1.0), Length.instantiateSI(3.0),
                Length.instantiateSI(-9.0), otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-15.0), Length.instantiateSI(-3.0), Length.instantiateSI(1.0),
                Length.instantiateSI(-11.0), otherLength2, egoLength2);
        testSingleDistance(true, Length.instantiateSI(-16.0), Length.instantiateSI(-4.0), Length.instantiateSI(0.0),
                Length.instantiateSI(-12.0), otherLength2, egoLength2);
        Try.testFail(
                () -> testSingleDistance(true, Length.instantiateSI(-17.0), Length.instantiateSI(-12.0),
                        Length.instantiateSI(0.0), Length.instantiateSI(-4.0), otherLength2, egoLength2),
                IllegalArgumentException.class);

        // upstream shorter
        testSingleDistance(false, Length.instantiateSI(10.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(5.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(0.0), null, null, null, otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-1.0), Length.instantiateSI(-3.0), Length.instantiateSI(1.0),
                Length.instantiateSI(-11.0), otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-3.0), Length.instantiateSI(-1.0), Length.instantiateSI(3.0),
                Length.instantiateSI(-9.0), otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-4.0), Length.instantiateSI(-0.0), Length.instantiateSI(4.0),
                Length.instantiateSI(-8.0), otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-6.0), Length.instantiateSI(2.0), Length.instantiateSI(4.0),
                Length.instantiateSI(-6.0), otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-7.0), Length.instantiateSI(3.0), Length.instantiateSI(4.0),
                Length.instantiateSI(-5.0), otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-12.0), Length.instantiateSI(8.0), Length.instantiateSI(4.0),
                Length.instantiateSI(-0.0), otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-13.0), Length.instantiateSI(9.0), Length.instantiateSI(3.0),
                Length.instantiateSI(1.0), otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-15.0), Length.instantiateSI(11.0), Length.instantiateSI(1.0),
                Length.instantiateSI(3.0), otherLength2, egoLength2);
        testSingleDistance(false, Length.instantiateSI(-16.0), Length.instantiateSI(12.0), Length.instantiateSI(0.0),
                Length.instantiateSI(4.0), otherLength2, egoLength2);
        Try.testFail(
                () -> testSingleDistance(false, Length.instantiateSI(-17.0), Length.instantiateSI(-12.0),
                        Length.instantiateSI(0.0), Length.instantiateSI(-4.0), otherLength2, egoLength2),
                IllegalArgumentException.class);
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
        assertNullableEquals(overlapRear, kinematics.getOverlap().getOverlapRear());
        assertNullableEquals(overlap, kinematics.getOverlap().getOverlap());
        assertNullableEquals(overlapFront, kinematics.getOverlap().getOverlapFront());
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
