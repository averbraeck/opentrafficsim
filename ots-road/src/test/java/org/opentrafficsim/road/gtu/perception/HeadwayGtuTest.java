package org.opentrafficsim.road.gtu.perception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.headway.GtuStatus;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuSimple;

/**
 * Test the HeadwayGtu class and the EnumType in the Headway interface.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class HeadwayGtuTest
{

    /** */
    private HeadwayGtuTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the constructor and the getters.
     * @throws GtuException when something fails; if uncaught; this test has failed
     */
    @Test
    public void constructorTest() throws GtuException
    {
        // Make two headway GTUs with different values to prove that HeadwayGtus do not share static fields.
        String id1 = "id1";
        GtuType gtuType1 = new GtuType("type1", DefaultsNl.CAR);
        Length distance1 = new Length(123, LengthUnit.METER);
        String id2 = "id2";
        GtuType gtuType2 = new GtuType("type2", DefaultsNl.CAR);
        Length distance2 = new Length(234, LengthUnit.METER);
        HeadwayGtuSimple hg1 = new HeadwayGtuSimple(id1, gtuType1, distance1, Length.ZERO, Length.ZERO, (Speed) null,
                (Acceleration) null, null, Length.ZERO, LateralDirectionality.NONE);
        HeadwayGtuSimple hg2 = new HeadwayGtuSimple(id2, gtuType2, distance2, Length.ZERO, Length.ZERO, (Speed) null,
                (Acceleration) null, null, Length.ZERO, LateralDirectionality.NONE);
        verifyFields(hg1, null, distance1, gtuType1, id1, Headway.ObjectType.GTU, null, null, null, null, true, false, false,
                false, false, false, false, false);
        verifyFields(hg2, null, distance2, gtuType2, id2, Headway.ObjectType.GTU, null, null, null, null, true, false, false,
                false, false, false, false, false);
        Length overlapFront = new Length(2, LengthUnit.METER);
        Length overlap = new Length(3, LengthUnit.METER);
        Length overlapRear = new Length(4, LengthUnit.METER);
        hg2 = new HeadwayGtuSimple(id2, gtuType2, overlapFront, overlap, overlapRear, Length.ZERO, Length.ZERO, (Speed) null,
                (Acceleration) null, null, Length.ZERO, LateralDirectionality.NONE);
        verifyFields(hg2, null, null, gtuType2, id2, Headway.ObjectType.GTU, overlap, overlapFront, overlapRear, null, false,
                false, false, false, false, false, false, true);
        Speed speed2 = new Speed(50, SpeedUnit.KM_PER_HOUR);
        Acceleration acceleration2 = new Acceleration(1.234, AccelerationUnit.METER_PER_SECOND_2);
        hg2 = new HeadwayGtuSimple(id2, gtuType2, overlapFront, overlap, overlapRear, Length.ZERO, Length.ZERO, speed2,
                acceleration2, null, Length.ZERO, LateralDirectionality.NONE);
        verifyFields(hg2, acceleration2, null, gtuType2, id2, Headway.ObjectType.GTU, overlap, overlapFront, overlapRear,
                speed2, false, false, false, false, false, false, false, true);
        // Test all combinations of two GtuStatus values.
        for (GtuStatus gtuStatus1 : GtuStatus.values())
        {
            for (GtuStatus gtuStatus2 : GtuStatus.values())
            {
                hg2 = new HeadwayGtuSimple(id2, gtuType2, distance2, Length.ZERO, Length.ZERO, speed2, acceleration2, null,
                        Length.ZERO, LateralDirectionality.NONE, gtuStatus1, gtuStatus2);
                boolean honking = GtuStatus.HONK == gtuStatus1 || GtuStatus.HONK == gtuStatus2;
                boolean braking = GtuStatus.BRAKING_LIGHTS == gtuStatus1 || GtuStatus.BRAKING_LIGHTS == gtuStatus2;
                boolean leftIndicator =
                        GtuStatus.LEFT_TURNINDICATOR == gtuStatus1 || GtuStatus.LEFT_TURNINDICATOR == gtuStatus2;
                boolean rightIndicator =
                        GtuStatus.RIGHT_TURNINDICATOR == gtuStatus1 || GtuStatus.RIGHT_TURNINDICATOR == gtuStatus2;
                boolean hazardLights = GtuStatus.EMERGENCY_LIGHTS == gtuStatus1 || GtuStatus.EMERGENCY_LIGHTS == gtuStatus2;
                verifyFields(hg2, acceleration2, distance2, gtuType2, id2, Headway.ObjectType.GTU, null, null, null, speed2,
                        true, false, braking, hazardLights, honking, leftIndicator, rightIndicator, false);

            }
        }
        // Verify that toString returns something
        assertTrue(hg1.toString().length() > 10, "toString returns something");
        assertTrue(hg2.toString().length() > 10, "toString returns something");
        try
        {
            new HeadwayGtuSimple(null, gtuType1, distance1, Length.ZERO, Length.ZERO, Speed.ZERO, Length.ZERO,
                    LateralDirectionality.NONE);
            fail("null for id should have thrown a GTUException");
        }
        catch (GtuException e)
        {
            // Ignore expected exception
        }
        try
        {
            new HeadwayGtuSimple(id1, gtuType1, null, Length.ZERO, Length.ZERO, Speed.ZERO, Length.ZERO,
                    LateralDirectionality.NONE);
            fail("null for distance should have thrown a GTUException");
        }
        catch (GtuException e)
        {
            // Ignore expected exception
        }
        assertTrue(hg1.getObjectType().isGtu(), "ObjectType is a GTU");
        assertFalse(hg1.getObjectType().isTrafficLight(), "ObjectType is traffic light");
        assertFalse(hg1.getObjectType().isObject(), "ObjectType is some other object");
        assertFalse(hg1.getObjectType().isDistanceOnly(), "ObjectType is distance only");
    }

    /**
     * Verify all fields in a HeadwayGtu.
     * @param headwayGTU the HeadwayGtu to check
     * @param acceleration the expected return value for getAcceleration
     * @param distance the expected return value for getDistance
     * @param gtuType the expected return value for getGtuType
     * @param id the expected return value for getId
     * @param objectType Headway.ObjectType; the expected return value for getObjectType
     * @param overlap the expected return value for getOverlap
     * @param overlapFront the expected return value for getOverlapFront
     * @param overlapRear the expected return value for getOverlapRear
     * @param speed the expected return value for getSpeed
     * @param ahead the expected return value for isAhead
     * @param behind the expected return value for isBehind
     * @param breakingLights the expected return value for isBreakingLightsOn
     * @param hazardLights the expected return value for isEmergencyLightsOn
     * @param honk the expected return value for isHonking
     * @param leftIndicator the expected return value for isLeftTurnIndicatorOn
     * @param rightIndicator the expected return value for isRightTurnIndicatorOn
     * @param parallel the expected return value for isParallel
     */
    private void verifyFields(final HeadwayGtuSimple headwayGTU, final Acceleration acceleration, final Length distance,
            final GtuType gtuType, final String id, final Headway.ObjectType objectType, final Length overlap,
            final Length overlapFront, final Length overlapRear, final Speed speed, final boolean ahead, final boolean behind,
            final boolean breakingLights, final boolean hazardLights, final boolean honk, final boolean leftIndicator,
            final boolean rightIndicator, final boolean parallel)
    {
        assertNotNull(headwayGTU, "headwayGTU should not be null");
        if (null == acceleration)
        {
            assertNull(headwayGTU.getAcceleration(), "acceleration should be null");
        }
        else
        {
            assertEquals(acceleration.si, headwayGTU.getAcceleration().si, acceleration.si / 99999,
                    "acceleration should be " + acceleration);
        }
        if (null == distance)
        {
            assertNull(headwayGTU.getDistance(), "distance should be null");
        }
        else
        {
            assertEquals(distance.si, headwayGTU.getDistance().si, distance.si / 99999, "distance should be " + distance);
        }
        assertEquals(gtuType, headwayGTU.getGtuType(), "GTU type should be " + gtuType);
        assertEquals(id, headwayGTU.getId(), "Id should be " + id);
        assertEquals(objectType, headwayGTU.getObjectType(), "Object type should be " + objectType);
        if (null == overlap)
        {
            assertNull(headwayGTU.getOverlap(), "overlap should be null");
        }
        else
        {
            assertEquals(overlap.si, headwayGTU.getOverlap().si, overlap.si / 99999, "overlap should be " + overlap);
        }
        if (null == overlapFront)
        {
            assertNull(headwayGTU.getOverlapFront(), "overlapFront should be null");
        }
        else
        {
            assertEquals(overlapFront.si, headwayGTU.getOverlapFront().si, overlapFront.si / 99999,
                    "overlapFront should be " + overlapFront);
        }
        if (null == overlap)
        {
            assertNull(headwayGTU.getOverlapRear(), "overlapRear should be null");
        }
        else
        {
            assertEquals(overlapRear.si, headwayGTU.getOverlapRear().si, overlapRear.si / 99999,
                    "overlapRear should be " + overlapRear);
        }
        if (null == speed)
        {
            assertNull(headwayGTU.getSpeed(), "speed should be null");
        }
        else
        {
            assertEquals(speed.si, headwayGTU.getSpeed().si, speed.si / 99999, "Speed should be " + speed);
        }
        if (ahead)
        {
            assertTrue(headwayGTU.isAhead(), "ahead should be true");
        }
        else
        {
            assertFalse(headwayGTU.isAhead(), "ahead should be false");
        }
        if (behind)
        {
            assertTrue(headwayGTU.isBehind(), "behind should be true");
        }
        else
        {
            assertFalse(headwayGTU.isBehind(), "behind should be false");
        }
        if (breakingLights)
        {
            assertTrue(headwayGTU.isBrakingLightsOn(), "breaking lights should be on");
        }
        else
        {
            assertFalse(headwayGTU.isBrakingLightsOn(), "breaking lights should be off");
        }
        if (hazardLights)
        {
            assertTrue(headwayGTU.isEmergencyLightsOn(), "hazard lights should be on");
        }
        else
        {
            assertFalse(headwayGTU.isEmergencyLightsOn(), "hazard lights should be off");
        }
        if (honk)
        {
            assertTrue(headwayGTU.isHonking(), "GTU should be honking");
        }
        else
        {
            assertFalse(headwayGTU.isHonking(), "GTU should not be honking");
        }
        if (leftIndicator)
        {
            assertTrue(headwayGTU.isLeftTurnIndicatorOn(), "Left indicator lights should be on");
        }
        else
        {
            assertFalse(headwayGTU.isLeftTurnIndicatorOn(), "Left indicator lights should be off");
        }
        if (rightIndicator)
        {
            assertTrue(headwayGTU.isRightTurnIndicatorOn(), "Right indicator lights should be on");
        }
        else
        {
            assertFalse(headwayGTU.isRightTurnIndicatorOn(), "Right indicator lights should be off");
        }
        if (parallel)
        {
            assertTrue(headwayGTU.isParallel(), "Parallel should be true");
        }
        else
        {
            assertFalse(headwayGTU.isParallel(), "Parallel should be false");
        }
    }

}
