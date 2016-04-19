package org.opentrafficsim.road.gtu.perception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.Test;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.road.gtu.lane.perception.Headway;
import org.opentrafficsim.road.gtu.lane.perception.HeadwayGTU;

/**
 * Test the HeadwayGTU class and the EnumType in the Headway interface.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 18, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayGTUTest
{

    /**
     * Test the constructor and the getters.
     * @throws GTUException when something fails; if uncaught; this test has failed
     */
    @Test
    public void constructorTest() throws GTUException
    {
        // Make two headway GTUs with different values to prove that HeadwayGTUs do not share static fields.
        String id1 = "id1";
        GTUType gtuType1 = GTUType.makeGTUType("type1");
        Length.Rel distance1 = new Length.Rel(123, LengthUnit.METER);
        String id2 = "id2";
        GTUType gtuType2 = GTUType.makeGTUType("type2");
        Length.Rel distance2 = new Length.Rel(234, LengthUnit.METER);
        HeadwayGTU hg1 = new HeadwayGTU(id1, gtuType1, distance1);
        HeadwayGTU hg2 = new HeadwayGTU(id2, gtuType2, distance2);
        verifyFields(hg1, null, distance1, gtuType1, id1, Headway.ObjectType.GTU, null, null, null, null, true, false, false,
                false, false, false, false, false);
        verifyFields(hg2, null, distance2, gtuType2, id2, Headway.ObjectType.GTU, null, null, null, null, true, false, false,
                false, false, false, false, false);
        Length.Rel overlapFront = new Length.Rel(2, LengthUnit.METER);
        Length.Rel overlap = new Length.Rel(3, LengthUnit.METER);
        Length.Rel overlapRear = new Length.Rel(4, LengthUnit.METER);
        hg2 = new HeadwayGTU(id2, gtuType2, overlapFront, overlap, overlapRear);
        verifyFields(hg2, null, null, gtuType2, id2, Headway.ObjectType.GTU, overlap, overlapFront, overlapRear, null, false,
                false, false, false, false, false, false, true);
        Speed speed2 = new Speed(50, SpeedUnit.KM_PER_HOUR);
        Acceleration acceleration2 = new Acceleration(1.234, AccelerationUnit.METER_PER_SECOND_2);
        hg2 = new HeadwayGTU(id2, gtuType2, overlapFront, overlap, overlapRear, speed2, acceleration2);
        verifyFields(hg2, acceleration2, null, gtuType2, id2, Headway.ObjectType.GTU, overlap, overlapFront, overlapRear,
                speed2, false, false, false, false, false, false, false, true);
        // Test all combinations of two GTUStatus values.
        for (HeadwayGTU.GTUStatus gtuStatus1 : HeadwayGTU.GTUStatus.values())
        {
            for (HeadwayGTU.GTUStatus gtuStatus2 : HeadwayGTU.GTUStatus.values())
            {
                hg2 = new HeadwayGTU(id2, gtuType2, distance2, speed2, acceleration2, gtuStatus1, gtuStatus2);
                boolean honking = HeadwayGTU.GTUStatus.HONK == gtuStatus1 || HeadwayGTU.GTUStatus.HONK == gtuStatus2;
                boolean braking =
                        HeadwayGTU.GTUStatus.BRAKING_LIGHTS == gtuStatus1 || HeadwayGTU.GTUStatus.BRAKING_LIGHTS == gtuStatus2;
                boolean leftIndicator =
                        HeadwayGTU.GTUStatus.LEFT_TURNINDICATOR == gtuStatus1
                                || HeadwayGTU.GTUStatus.LEFT_TURNINDICATOR == gtuStatus2;
                boolean rightIndicator =
                        HeadwayGTU.GTUStatus.RIGHT_TURNINDICATOR == gtuStatus1
                                || HeadwayGTU.GTUStatus.RIGHT_TURNINDICATOR == gtuStatus2;
                boolean hazardLights =
                        HeadwayGTU.GTUStatus.EMERGENCY_LIGHTS == gtuStatus1
                                || HeadwayGTU.GTUStatus.EMERGENCY_LIGHTS == gtuStatus2;
                verifyFields(hg2, acceleration2, distance2, gtuType2, id2, Headway.ObjectType.GTU, null, null, null, speed2,
                        true, false, braking, hazardLights, honking, leftIndicator, rightIndicator, false);

            }
        }
        // Verify that toString returns something
        assertTrue("toString returns something", hg1.toString().length() > 10);
        assertTrue("toString returns something", hg2.toString().length() > 10);
        try
        {
            new HeadwayGTU(null, gtuType1, distance1);
            fail("null for id should have thrown a GTUException");
        }
        catch (GTUException e)
        {
            // Ignore expected exception
        }
        try
        {
            new HeadwayGTU(id1, gtuType1, null);
            fail("null for distance should have thrown a GTUException");
        }
        catch (GTUException e)
        {
            // Ignore expected exception
        }
        assertTrue("ObjectType is a GTU", hg1.getObjectType().isGtu());
        assertFalse("ObjectType is traffic light", hg1.getObjectType().isTrafficLight());
        assertFalse("ObjectType is some other object", hg1.getObjectType().isObject());
        assertFalse("ObjectType is distance only", hg1.getObjectType().isDistanceOnly());
    }

    /**
     * Verify all fields in a HeadwayGTU.
     * @param headwayGTU HeadwayGTU; the HeadwayGTU to check
     * @param acceleration Acceleration; the expected return value for getAcceleration
     * @param distance Length.Rel; the expected return value for getDistance
     * @param gtuType GTUType; the expected return value for getGTUType
     * @param id String; the expected return value for getId
     * @param objectType {@link ObjectType}; the expected return value for getObjectType
     * @param overlap Length.Rel; the expected return value for getOverlap
     * @param overlapFront Length.Rel; the expected return value for getOverlapFront
     * @param overlapRear Length.Rel; the expected return value for getOverlapRear
     * @param speed Speed; the expected return value for getSpeed
     * @param ahead boolean; the expected return value for isAhead
     * @param behind boolean; the expected return value for isBehind
     * @param breakingLights boolean; the expected return value for isBreakingLightsOn
     * @param hazardLights boolean; the expected return value for isEmergencyLightsOn
     * @param honk boolean; the expected return value for isHonking
     * @param leftIndicator boolean; the expected return value for isLeftTurnIndicatorOn
     * @param rightIndicator boolean; the expected return value for isRightTurnIndicatorOn
     * @param parallel boolean; the expected return value for isParallel
     */
    private void verifyFields(final HeadwayGTU headwayGTU, final Acceleration acceleration, final Length.Rel distance,
            final GTUType gtuType, final String id, final Headway.ObjectType objectType, final Length.Rel overlap,
            final Length.Rel overlapFront, final Length.Rel overlapRear, final Speed speed, final boolean ahead,
            final boolean behind, final boolean breakingLights, final boolean hazardLights, final boolean honk,
            final boolean leftIndicator, final boolean rightIndicator, final boolean parallel)
    {
        assertNotNull("headwayGTU should not be null", headwayGTU);
        if (null == acceleration)
        {
            assertNull("acceleration should be null", headwayGTU.getAcceleration());
        }
        else
        {
            assertEquals("acceleration should be " + acceleration, acceleration.si, headwayGTU.getAcceleration().si,
                    acceleration.si / 99999);
        }
        if (null == distance)
        {
            assertNull("distance should be null", headwayGTU.getDistance());
        }
        else
        {
            assertEquals("distance should be " + distance, distance.si, headwayGTU.getDistance().si, distance.si / 99999);
        }
        assertEquals("GTU type should be " + gtuType, gtuType, headwayGTU.getGtuType());
        assertEquals("Id should be " + id, id, headwayGTU.getId());
        assertEquals("Object type should be " + objectType, objectType, headwayGTU.getObjectType());
        if (null == overlap)
        {
            assertNull("overlap should be null", headwayGTU.getOverlap());
        }
        else
        {
            assertEquals("overlap should be " + overlap, overlap.si, headwayGTU.getOverlap().si, overlap.si / 99999);
        }
        if (null == overlapFront)
        {
            assertNull("overlapFront should be null", headwayGTU.getOverlapFront());
        }
        else
        {
            assertEquals("overlapFront should be " + overlapFront, overlapFront.si, headwayGTU.getOverlapFront().si,
                    overlapFront.si / 99999);
        }
        if (null == overlap)
        {
            assertNull("overlapRear should be null", headwayGTU.getOverlapRear());
        }
        else
        {
            assertEquals("overlapRear should be " + overlapRear, overlapRear.si, headwayGTU.getOverlapRear().si,
                    overlapRear.si / 99999);
        }
        if (null == speed)
        {
            assertNull("speed should be null", headwayGTU.getSpeed());
        }
        else
        {
            assertEquals("Speed should be " + speed, speed.si, headwayGTU.getSpeed().si, speed.si / 99999);
        }
        if (ahead)
        {
            assertTrue("ahead should be true", headwayGTU.isAhead());
        }
        else
        {
            assertFalse("ahead should be false", headwayGTU.isAhead());
        }
        if (behind)
        {
            assertTrue("behind should be true", headwayGTU.isBehind());
        }
        else
        {
            assertFalse("behind should be false", headwayGTU.isBehind());
        }
        if (breakingLights)
        {
            assertTrue("breaking lights should be on", headwayGTU.isBrakingLightsOn());
        }
        else
        {
            assertFalse("breaking lights should be off", headwayGTU.isBrakingLightsOn());
        }
        if (hazardLights)
        {
            assertTrue("hazard lights should be on", headwayGTU.isEmergencyLightsOn());
        }
        else
        {
            assertFalse("hazard lights should be off", headwayGTU.isEmergencyLightsOn());
        }
        if (honk)
        {
            assertTrue("GTU should be honking", headwayGTU.isHonking());
        }
        else
        {
            assertFalse("GTU should not be honking", headwayGTU.isHonking());
        }
        if (leftIndicator)
        {
            assertTrue("Left indicator lights should be on", headwayGTU.isLeftTurnIndicatorOn());
        }
        else
        {
            assertFalse("Left indicator lights should be off", headwayGTU.isLeftTurnIndicatorOn());
        }
        if (rightIndicator)
        {
            assertTrue("Right indicator lights should be on", headwayGTU.isRightTurnIndicatorOn());
        }
        else
        {
            assertFalse("Right indicator lights should be off", headwayGTU.isRightTurnIndicatorOn());
        }
        if (parallel)
        {
            assertTrue("Parallel should be true", headwayGTU.isParallel());
        }
        else
        {
            assertFalse("Parallel should be false", headwayGTU.isParallel());
        }
    }

}
