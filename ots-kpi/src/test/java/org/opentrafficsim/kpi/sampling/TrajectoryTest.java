package org.opentrafficsim.kpi.sampling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.djunits.value.vfloat.scalar.FloatLength;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djunits.value.vfloat.scalar.FloatTime;
import org.djunits.value.vfloat.scalar.base.FloatScalar;
import org.djunits.value.vfloat.vector.base.FloatVector;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.kpi.sampling.Trajectory.SpaceTimeView;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.kpi.sampling.data.ReferenceSpeed;
import org.opentrafficsim.kpi.sampling.filter.FilterDataGtuType;
import org.opentrafficsim.kpi.sampling.filter.FilterDataOrigin;
import org.opentrafficsim.kpi.sampling.impl.TestGtuData;

/**
 * Test for Trajectory, in so far other tests do not happen to already use this class.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class TrajectoryTest
{

    /** */
    private TrajectoryTest()
    {
        // do not instantiate test class
    }

    /**
     * Trajectory test.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void trajectoryTest()
    {
        try
        {
            new Trajectory<>(null, Collections.emptyMap(), Collections.emptySet());
            fail("Null GTU should throw a NullPointerException");
        }
        catch (NullPointerException ex)
        {
            // expected
        }

        // prepare input
        TestGtuData gtu = new TestGtuData("id", "A", "B", "car", "route", Speed.instantiateSI(10.0));
        ExtendedDataDuration<TestGtuData> ttc = new ExtendedDataDuration<>("ttc", "time to collision")
        {
            @Override
            public FloatDuration getValue(final TestGtuData gtu)
            {
                return FloatDuration.NaN;
            }
        };

        // trajectory without extended data
        Trajectory<TestGtuData> trajectory = new Trajectory<>(gtu,
                Map.of(new FilterDataGtuType(), "car", new FilterDataOrigin(), "A"), Collections.emptySet());
        smallSimulation(trajectory, null);

        // trajectory with extended data
        trajectory = new Trajectory<>(gtu, Map.of(new FilterDataGtuType(), "car", new FilterDataOrigin(), "A"),
                Set.of(ReferenceSpeed.INSTANCE, ttc));
        try
        {
            trajectory.add(Length.ZERO, Speed.ZERO, Acceleration.ZERO, Duration.ZERO);
            fail("Should throw NullPointerException when extended data is part of the trajectory and no GTU is give.");
        }
        catch (NullPointerException ex)
        {
            // expected
        }
        smallSimulation(trajectory, gtu);

        // trajectory with constant speed
        trajectory = new Trajectory<>(gtu, Map.of(new FilterDataGtuType(), "car", new FilterDataOrigin(), "A"),
                Collections.emptySet());
        Speed v = Speed.instantiateSI(30.0);
        for (double t = 0.0; t < 10.0; t += 0.5)
        {
            Length x = Length.instantiateSI(v.si * t);
            Duration tim = Duration.instantiateSI(t);
            trajectory.add(x, v, Acceleration.ZERO, tim);
            if (t > 0.0)
            {
                Length pos = Length.instantiateSI(x.si - 0.25 * v.si);
                Duration moment = Duration.instantiateSI(tim.si - 0.25);
                assertEquals(moment.si, trajectory.getTimeAtPosition(pos).si, 0.001);
                assertEquals(v.si, trajectory.getSpeedAtPosition(pos).si, 0.001);
                assertEquals(0.0, trajectory.getAccelerationAtPosition(pos).si, 0.001);
                assertEquals(pos.si, trajectory.getPositionAtTime(moment).si, 0.001);
                assertEquals(v.si, trajectory.getSpeedAtTime(moment).si, 0.001);
                assertEquals(0.0, trajectory.getAccelerationAtTime(moment).si, 0.001);
            }
        }

        // Object methods
        Trajectory<TestGtuData> trajectory2;
        assertFalse(trajectory.equals(null));
        assertFalse(trajectory.equals("Not a trajectory"));

        trajectory = new Trajectory<>(gtu, Collections.emptyMap(), Collections.emptySet());
        trajectory2 = new Trajectory<>(gtu, Collections.emptyMap(), Collections.emptySet());
        assertTrue(trajectory.equals(trajectory2));
        assertNotNull(trajectory.toString());

        trajectory = new Trajectory<>(gtu, Collections.emptyMap(), Collections.emptySet());
        trajectory2 = new Trajectory<>(gtu, Collections.emptyMap(), Collections.emptySet());
        trajectory.add(Length.ZERO, Speed.ZERO, Acceleration.ZERO, Duration.ZERO);
        assertFalse(trajectory.equals(trajectory2));
        assertFalse(trajectory2.equals(trajectory));

        trajectory = new Trajectory<>(gtu, Collections.emptyMap(), Collections.emptySet());
        trajectory2 = new Trajectory<>(gtu, Collections.emptyMap(), Collections.emptySet());
        trajectory.add(Length.ZERO, Speed.ZERO, Acceleration.ZERO, Duration.ZERO);
        trajectory2.add(Length.ZERO, Speed.ZERO, Acceleration.ZERO, Duration.instantiateSI(1.0));
        assertFalse(trajectory.equals(trajectory2));
        assertFalse(trajectory2.equals(trajectory));

        trajectory = new Trajectory<>(gtu, Collections.emptyMap(), Collections.emptySet());
        TestGtuData gtu2 = new TestGtuData("id2", "A", "B", "car", "route", Speed.instantiateSI(10.0));
        trajectory2 = new Trajectory<>(gtu2, Collections.emptyMap(), Collections.emptySet());
        assertFalse(trajectory.equals(trajectory2)); // triggers on gtuId != gtuId
        trajectory2.add(Length.ZERO, Speed.ZERO, Acceleration.ZERO, Duration.instantiateSI(1.0));
        assertFalse(trajectory.equals(trajectory2)); // triggers on size != size
        trajectory.add(Length.ZERO, Speed.ZERO, Acceleration.ZERO, Duration.ZERO);
        trajectory2 = new Trajectory<>(gtu, Collections.emptyMap(), Collections.emptySet());
        trajectory2.add(Length.ZERO, Speed.ZERO, Acceleration.ZERO, Duration.instantiateSI(1.0));
        assertFalse(trajectory.equals(trajectory2)); // triggers on t[0] != t[0]

        assertNotNull(trajectory.toString());

    }

    /**
     * Perform a small simulation.
     * @param trajectory trajectory
     * @param gtu GTU, {@code null} in case of no extended data
     */
    private void smallSimulation(final Trajectory<TestGtuData> trajectory, final TestGtuData gtu)
    {
        double v0 = 30.0;
        Acceleration a = Acceleration.instantiateSI(-1.2);
        int n = 0;
        Length x0 = Length.instantiateSI(100.0);
        Length x1 = Length.instantiateSI(200.0);
        Length xMax = Length.instantiateSI(300.0);
        Duration t0 = Duration.instantiateSI(4.0);
        Duration t1 = Duration.instantiateSI(8.0);
        Duration tMax = Duration.instantiateSI(10.0);

        assertEquals(0, trajectory.subSet(Length.ZERO, xMax).size());
        assertEquals(0, trajectory.subSet(Duration.ZERO, tMax).size());
        assertEquals(0, trajectory.subSet(Length.ZERO, xMax, Duration.ZERO, tMax).size());

        for (double t = 0.0; t < tMax.si; t += 0.5)
        {
            Length x = Length.instantiateSI(v0 * t + 0.5 * a.si * t * t);
            Speed v = Speed.instantiateSI(v0 + t * a.si);
            Duration tim = Duration.instantiateSI(t);
            if (gtu == null)
            {
                trajectory.add(x, v, a, tim);
            }
            else
            {
                trajectory.add(x, v, a, tim, gtu);
            }

            testArray(trajectory.getX(), n, x.floatValue());
            testArray(trajectory.getV(), n, v.floatValue());
            testArray(trajectory.getT(), n, tim.floatValue());
            testArray(trajectory.getA(), n, a.floatValue());
            testUnitVector(trajectory.getPosition(), n, FloatLength.instantiateSI(x.floatValue()));
            testUnitVector(trajectory.getSpeed(), n, FloatSpeed.instantiateSI(v.floatValue()));
            testUnitVector(trajectory.getTime(), n, FloatTime.instantiateSI(tim.floatValue()));
            testUnitVector(trajectory.getAcceleration(), n, FloatAcceleration.instantiateSI(a.floatValue()));
            assertEquals(trajectory.getTotalDuration().si, tim.si, 0.001);
            assertEquals(trajectory.getTotalLength().si, x.si, 0.001);

            testSpaceTimeView(trajectory.getSpaceTimeView(), trajectory.getTotalLength(), trajectory.getTotalDuration());
            if (t > t0.si)
            {
                Length xExpected = Length.max(Length.ZERO, Length.min(x, x1).minus(x0));
                testSpaceTimeView(trajectory.getSpaceTimeView(x0, x1, Duration.ZERO, tMax), xExpected, null);
                Duration tExpected = Duration.max(Duration.ZERO, Time.min(tim, t1).minus(t0));
                testSpaceTimeView(trajectory.getSpaceTimeView(Length.ZERO, xMax, t0, t1), null, tExpected);
            }
            n++;

            if (n < 2)
            {
                assertEquals(0.0, trajectory.getSpaceTimeView().distance().si, 0.001); // empty
                assertEquals(0.0, trajectory.getSpaceTimeView().time().si, 0.001);
                assertEquals(0.0, trajectory.getSpaceTimeView(Length.ZERO, xMax, Duration.ZERO, tMax).time().si, 0.001);
            }

            assertEquals(v0, trajectory.getSpeedAtPosition(Length.ZERO).si, 0.001);
            assertEquals(v.si, trajectory.getSpeedAtPosition(x).si, 0.001);
        }
        try
        {
            trajectory.getX(-1);
            fail("Should have thrown IndexOutOfBoundsException for index < 0.");
        }
        catch (IndexOutOfBoundsException ex)
        {
            // expected
        }

        try
        {
            trajectory.getX(n);
            fail("Should have thrown IndexOutOfBoundsException for index >= size.");
        }
        catch (IndexOutOfBoundsException ex)
        {
            // expected
        }

        assertEquals(2, trajectory.getFilterDataTypes().size());
        assertEquals(gtu == null ? 0 : 2, trajectory.getExtendedDataTypes().size());
        try
        {
            trajectory.getExtendedData(new ExtendedDataDuration<TestGtuData>("does", "not exist")
            {
                @Override
                public FloatDuration getValue(final TestGtuData gtu)
                {
                    return null;
                }
            });
            fail("Requesting data for non-included extended data type should throw SamplingException.");
        }
        catch (SamplingException ex)
        {
            // expected
        }

    }

    /**
     * Check last value in array, and it's length.
     * @param array array
     * @param lastIndex what should be the last index
     * @param lastValue what should be the last value
     */
    private void testArray(final float[] array, final int lastIndex, final float lastValue)
    {
        assertEquals(lastIndex + 1, array.length);
        assertEquals(lastValue, array[lastIndex], 0.001f);
    }

    /**
     * Check last value in unit vector, and it's length.
     * @param vector vector
     * @param lastIndex what should be the last index
     * @param lastValue what should be the last value
     * @param <U> unit type
     * @param <S> scalar type
     * @param <V> vector type
     */
    private <U extends Unit<U>, S extends FloatScalar<U, S>, V extends FloatVector<U, S, V>> void testUnitVector(final V vector,
            final int lastIndex, final S lastValue)
    {
        assertEquals(lastIndex + 1, vector.size());
        assertEquals(lastValue.si, vector.get(lastIndex).si, 0.001f);
    }

    /**
     * Checks space time view ranges.
     * @param spaceTimeView space time view
     * @param distance distance
     * @param time time
     */
    private void testSpaceTimeView(final SpaceTimeView spaceTimeView, final Length distance, final Duration time)
    {
        if (distance != null)
        {
            assertEquals(distance.si, spaceTimeView.distance().si, 0.001);
        }
        if (time != null)
        {
            assertEquals(time.si, spaceTimeView.time().si, 0.001);
        }
        assertEquals(spaceTimeView.distance().si / spaceTimeView.time().si, spaceTimeView.speed().si, 0.001);
    }

}
