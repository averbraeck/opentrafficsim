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
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu.Maneuver;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu.Signals;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtuSimple;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics.Overlap;

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
        PerceivedGtu hg1 = new PerceivedGtuSimple(id1, gtuType1, Length.ZERO, Length.ZERO,
                new Kinematics.Record(distance1, Speed.ZERO, Acceleration.ZERO, true, Overlap.AHEAD), Signals.NONE,
                Maneuver.NONE);
        PerceivedGtu hg2 = new PerceivedGtuSimple(id2, gtuType2, Length.ZERO, Length.ZERO,
                new Kinematics.Record(distance2, Speed.ZERO, Acceleration.ZERO, true, Overlap.AHEAD), Signals.NONE,
                Maneuver.NONE);
        verifyFields(hg1, Acceleration.ZERO, distance1, gtuType1, id1, PerceivedObject.ObjectType.GTU, null, null, null,
                Speed.ZERO, true, false, false, false, false, false, false, false);
        verifyFields(hg2, Acceleration.ZERO, distance2, gtuType2, id2, PerceivedObject.ObjectType.GTU, null, null, null,
                Speed.ZERO, true, false, false, false, false, false, false, false);
        Length overlapFront = new Length(2, LengthUnit.METER);
        Length overlap = new Length(3, LengthUnit.METER);
        Length overlapRear = new Length(4, LengthUnit.METER);
        hg2 = new PerceivedGtuSimple(id2, gtuType2, Length.ZERO, Length.ZERO, new Kinematics.Record(Length.ZERO, Speed.ZERO,
                Acceleration.ZERO, true, new Overlap.Record(overlap, overlapFront, overlapRear, false, false)), Signals.NONE,
                Maneuver.NONE);
        verifyFields(hg2, Acceleration.ZERO, Length.ZERO, gtuType2, id2, PerceivedObject.ObjectType.GTU, overlap, overlapFront,
                overlapRear, Speed.ZERO, false, false, false, false, false, false, false, true);
        Speed speed2 = new Speed(50, SpeedUnit.KM_PER_HOUR);
        Acceleration acceleration2 = new Acceleration(1.234, AccelerationUnit.METER_PER_SECOND_2);
        hg2 = new PerceivedGtuSimple(id2, gtuType2, Length.ZERO, Length.ZERO, new Kinematics.Record(Length.ZERO, speed2,
                acceleration2, true, new Overlap.Record(overlap, overlapFront, overlapRear, false, false)), Signals.NONE,
                Maneuver.NONE);
        verifyFields(hg2, acceleration2, Length.ZERO, gtuType2, id2, PerceivedObject.ObjectType.GTU, overlap, overlapFront,
                overlapRear, speed2, false, false, false, false, false, false, false, true);
        // Verify that toString returns something
        assertTrue(hg1.toString().length() > 10, "toString returns something");
        assertTrue(hg2.toString().length() > 10, "toString returns something");
        try
        {
            new PerceivedGtuSimple(null, gtuType1, Length.ZERO, Length.ZERO,
                    new Kinematics.Record(distance1, speed2, acceleration2, true, Overlap.AHEAD), Signals.NONE, Maneuver.NONE);
            fail("null for id should have thrown a GTUException");
        }
        catch (NullPointerException e)
        {
            // Ignore expected exception
        }
        try
        {
            new PerceivedGtuSimple(id1, gtuType1, Length.ZERO, Length.ZERO,
                    new Kinematics.Record(null, speed2, acceleration2, true, Overlap.AHEAD), Signals.NONE, Maneuver.NONE);
            fail("null for distance should have thrown a NullPointerException");
        }
        catch (NullPointerException e)
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
     * @param perceivedGTU the HeadwayGtu to check
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
    private void verifyFields(final PerceivedGtu perceivedGTU, final Acceleration acceleration, final Length distance,
            final GtuType gtuType, final String id, final PerceivedObject.ObjectType objectType, final Length overlap,
            final Length overlapFront, final Length overlapRear, final Speed speed, final boolean ahead, final boolean behind,
            final boolean breakingLights, final boolean hazardLights, final boolean honk, final boolean leftIndicator,
            final boolean rightIndicator, final boolean parallel)
    {
        assertNotNull(perceivedGTU, "headwayGTU should not be null");
        if (null == acceleration)
        {
            assertNull(perceivedGTU.getAcceleration(), "acceleration should be null");
        }
        else
        {
            assertEquals(acceleration.si, perceivedGTU.getAcceleration().si, acceleration.si / 99999,
                    "acceleration should be " + acceleration);
        }
        if (null == distance)
        {
            assertNull(perceivedGTU.getDistance(), "distance should be null");
        }
        else
        {
            assertEquals(distance.si, perceivedGTU.getDistance().si, distance.si / 99999, "distance should be " + distance);
        }
        assertEquals(gtuType, perceivedGTU.getGtuType(), "GTU type should be " + gtuType);
        assertEquals(id, perceivedGTU.getId(), "Id should be " + id);
        assertEquals(objectType, perceivedGTU.getObjectType(), "Object type should be " + objectType);
        if (null == overlap)
        {
            assertNull(perceivedGTU.getKinematics().getOverlap().getOverlap(), "overlap should be null");
        }
        else
        {
            assertEquals(overlap.si, perceivedGTU.getKinematics().getOverlap().getOverlap().si, overlap.si / 99999,
                    "overlap should be " + overlap);
        }
        if (null == overlapFront)
        {
            assertNull(perceivedGTU.getKinematics().getOverlap().getOverlapFront(), "overlapFront should be null");
        }
        else
        {
            assertEquals(overlapFront.si, perceivedGTU.getKinematics().getOverlap().getOverlapFront().si,
                    overlapFront.si / 99999, "overlapFront should be " + overlapFront);
        }
        if (null == overlap)
        {
            assertNull(perceivedGTU.getKinematics().getOverlap().getOverlapRear(), "overlapRear should be null");
        }
        else
        {
            assertEquals(overlapRear.si, perceivedGTU.getKinematics().getOverlap().getOverlapRear().si, overlapRear.si / 99999,
                    "overlapRear should be " + overlapRear);
        }
        if (null == speed)
        {
            assertNull(perceivedGTU.getSpeed(), "speed should be null");
        }
        else
        {
            assertEquals(speed.si, perceivedGTU.getSpeed().si, speed.si / 99999, "Speed should be " + speed);
        }
        if (ahead)
        {
            assertTrue(perceivedGTU.getKinematics().getOverlap().isAhead(), "ahead should be true");
        }
        else
        {
            assertFalse(perceivedGTU.getKinematics().getOverlap().isAhead(), "ahead should be false");
        }
        if (behind)
        {
            assertTrue(perceivedGTU.getKinematics().getOverlap().isBehind(), "behind should be true");
        }
        else
        {
            assertFalse(perceivedGTU.getKinematics().getOverlap().isBehind(), "behind should be false");
        }
        if (breakingLights)
        {
            assertTrue(perceivedGTU.getSignals().isBrakingLightsOn(), "breaking lights should be on");
        }
        else
        {
            assertFalse(perceivedGTU.getSignals().isBrakingLightsOn(), "breaking lights should be off");
        }
        if (hazardLights)
        {
            assertTrue(perceivedGTU.getSignals().getTurnIndicatorStatus().isHazard(), "hazard lights should be on");
        }
        else
        {
            assertFalse(perceivedGTU.getSignals().getTurnIndicatorStatus().isHazard(), "hazard lights should be off");
        }
        if (leftIndicator)
        {
            assertTrue(perceivedGTU.getSignals().getTurnIndicatorStatus().isLeft(), "Left indicator lights should be on");
        }
        else
        {
            assertFalse(perceivedGTU.getSignals().getTurnIndicatorStatus().isLeft(), "Left indicator lights should be off");
        }
        if (rightIndicator)
        {
            assertTrue(perceivedGTU.getSignals().getTurnIndicatorStatus().isRight(), "Right indicator lights should be on");
        }
        else
        {
            assertFalse(perceivedGTU.getSignals().getTurnIndicatorStatus().isRight(), "Right indicator lights should be off");
        }
        if (parallel)
        {
            assertTrue(perceivedGTU.getKinematics().getOverlap().isParallel(), "Parallel should be true");
        }
        else
        {
            assertFalse(perceivedGTU.getKinematics().getOverlap().isParallel(), "Parallel should be false");
        }
    }

}
