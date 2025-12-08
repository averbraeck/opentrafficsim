package org.opentrafficsim.kpi.sampling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vfloat.scalar.FloatDirection;
import org.djunits.value.vfloat.vector.FloatDirectionVector;
import org.djutils.data.Row;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.CrossSection.LanePosition;
import org.opentrafficsim.kpi.sampling.SamplerData.Compression;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataFloat;
import org.opentrafficsim.kpi.sampling.data.ReferenceSpeed;
import org.opentrafficsim.kpi.sampling.filter.FilterDataCrossSections;
import org.opentrafficsim.kpi.sampling.filter.FilterDataGtuType;
import org.opentrafficsim.kpi.sampling.filter.FilterDataRoute;
import org.opentrafficsim.kpi.sampling.filter.FilterDataSet;
import org.opentrafficsim.kpi.sampling.impl.TestGtuData;
import org.opentrafficsim.kpi.sampling.impl.TestLaneData;
import org.opentrafficsim.kpi.sampling.impl.TestLinkData;
import org.opentrafficsim.kpi.sampling.impl.TestSampler;
import org.opentrafficsim.kpi.sampling.impl.TestSampler.Entry;
import org.opentrafficsim.kpi.sampling.impl.TestSimulator;
import org.opentrafficsim.kpi.sampling.indicator.MeanDensity;
import org.opentrafficsim.kpi.sampling.indicator.MeanIntensity;
import org.opentrafficsim.kpi.sampling.indicator.MeanSpeed;
import org.opentrafficsim.kpi.sampling.indicator.MeanTravelTimePerDistance;
import org.opentrafficsim.kpi.sampling.indicator.MeanTripLength;
import org.opentrafficsim.kpi.sampling.indicator.TotalDelay;
import org.opentrafficsim.kpi.sampling.indicator.TotalDelayReference;
import org.opentrafficsim.kpi.sampling.indicator.TotalNumberOfStops;
import org.opentrafficsim.kpi.sampling.indicator.TotalTravelDistance;
import org.opentrafficsim.kpi.sampling.indicator.TotalTravelTime;

/**
 * Tests sampler in various ways.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class SamplerTest
{

    /** */
    private SamplerTest()
    {
        // do not instantiate test class
    }

    /**
     * Test initialization, finalization, and possible overlap between space-time regions.
     */
    @Test
    public void testInitFinalize()
    {
        Length length = Length.ofSI(1000.0);
        Duration end = Duration.ofSI(3600.0);
        TestLinkData link = new TestLinkData("AB", length);
        TestLaneData lane1 = new TestLaneData("1", length, link);
        TestLaneData lane2 = new TestLaneData("2", length, link);

        for (boolean registerLater : new boolean[] {false, true})
        {
            TestSimulator simulator = new TestSimulator();
            TestSampler sampler = new TestSampler(Collections.emptySet(), Collections.emptySet(), simulator);

            sampler.registerSpaceTimeRegion(new SpaceTimeRegion<TestLaneData>(lane1, Length.ZERO, length, Duration.ZERO, end));
            sampler.registerSpaceTimeRegion(
                    new SpaceTimeRegion<TestLaneData>(lane2, Length.ZERO, length, Duration.ofSI(100.0), Duration.ofSI(200.0)));

            if (registerLater)
            {
                // move clock before we register next space-time regions
                simulator.executeUntil(Duration.ofSI(250.0));
                sampler.stopRecording(lane2); // should do nothing, end-time is 3600s & this method should be called through sim
            }
            sampler.registerSpaceTimeRegion(
                    new SpaceTimeRegion<>(lane2, Length.ZERO, length, Duration.ofSI(300.0), Duration.ofSI(400.0)));
            sampler.registerSpaceTimeRegion(
                    new SpaceTimeRegion<>(lane2, Length.ZERO, length, Duration.ofSI(350.0), Duration.ofSI(450.0)));

            simulator.executeUntil(Duration.ofSI(3600.0));

            // Let's check that all the right events, and no wrong events, occurred.
            var inits = sampler.getInits();
            assertTrue(inits.remove(new Entry(Duration.ZERO, lane1)));
            assertTrue(inits.remove(new Entry(Duration.ofSI(100.0), lane2)));
            assertTrue(inits.remove(new Entry(Duration.ofSI(300.0), lane2)));
            assertTrue(inits.isEmpty());

            var finals = sampler.getFinals();
            assertTrue(finals.remove(new Entry(end, lane1)));
            assertTrue(finals.remove(new Entry(Duration.ofSI(200.0), lane2)));
            // no stop at 400 due to overlap with 350-450
            assertNotNull(finals.remove(new Entry(Duration.ofSI(450.0), lane2)));
            assertTrue(finals.isEmpty());
        }
    }

    /**
     * Test basic functions.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testBasic()
    {
        TestSimulator simulator = new TestSimulator();
        TestSampler sampler = new TestSampler(Collections.emptySet(), Collections.emptySet(), simulator);
        assertNotNull(sampler.getSamplerData());
        assertFalse(sampler.contains(null));
        assertFalse(sampler.contains(ReferenceSpeed.INSTANCE));
        sampler.hashCode();
        assertTrue(sampler.equals(sampler));
        assertFalse(sampler.equals(null));
        assertFalse(sampler.equals("This is not a sampler."));
        assertTrue(sampler.equals(new TestSampler(Collections.emptySet(), Collections.emptySet(), simulator)));
        assertFalse(sampler.equals(new TestSampler(Set.of(ReferenceSpeed.INSTANCE), Collections.emptySet(), simulator)));
        assertFalse(sampler.equals(new TestSampler(Collections.emptySet(), Set.of(new FilterDataGtuType()), simulator)));
    }

    /**
     * Test lots in a small two-lane, three-vehicle case.
     */
    @Test
    public void testSmallDataCase()
    {
        Length length = Length.ofSI(1000.0);
        TestLinkData link = new TestLinkData("AB", length);
        TestLaneData lane1 = new TestLaneData("1", length, link);
        TestLaneData lane2 = new TestLaneData("2", length, link);
        TestLaneData lane3 = new TestLaneData("3", length, link);

        TestSimulator simulator = new TestSimulator();
        FilterDataGtuType filterDataGtuType = new FilterDataGtuType();
        FilterDataCrossSections filterDataCrossSection = new FilterDataCrossSections();
        TestSampler sampler = new TestSampler(Set.of(ReferenceSpeed.INSTANCE, new Heading()),
                Set.of(filterDataGtuType, filterDataCrossSection), simulator);

        SpaceTimeRegion<TestLaneData> space1 =
                new SpaceTimeRegion<>(lane1, Length.ZERO, length, Duration.ZERO, Duration.ofSI(60.0));
        SpaceTimeRegion<TestLaneData> space2 =
                new SpaceTimeRegion<>(lane2, Length.ZERO, length, Duration.ZERO, Duration.ofSI(60.0));
        sampler.registerSpaceTimeRegion(space1);
        sampler.registerSpaceTimeRegion(space2);

        simulator.executeUntil(Duration.ofSI(60.0));

        TestGtuData car1 = new TestGtuData("1", "A", "B", "car", "routeAB", Speed.ofSI(33.0));
        TestGtuData car2 = new TestGtuData("2", "A", "B", "car", "routeAB", Speed.ofSI(33.0));
        TestGtuData car3 = new TestGtuData("3", "A", "B", "car", "routeAB", Speed.ofSI(33.0));
        TestGtuData truck = new TestGtuData("4", "A", "B", "truck", "routeAB", Speed.ofSI(22.0));

        sampler.addGtuWithSnapshot(lane1, length.times(1.1), Speed.ofSI(25.0), Acceleration.ZERO, Duration.ZERO, car1);
        assertTrue(sampler.getSamplerData().getTrajectoryGroup(lane1).getTrajectories().isEmpty(),
                "Add GTU beyond length of lane should not have added data.");
        assertTrue(sampler.getSamplerData().isEmpty());

        // add data
        sampler.addGtuWithSnapshot(lane1, Length.ZERO, Speed.ofSI(25.0), Acceleration.ZERO, Duration.ZERO, car1);
        sampler.addGtuWithSnapshot(lane1, Length.ZERO, Speed.ofSI(25.0), Acceleration.ZERO, Duration.ofSI(10.0), car2);
        sampler.addGtuWithSnapshot(lane2, Length.ZERO, Speed.ofSI(20.0), Acceleration.ZERO, Duration.ofSI(10.0), truck);

        // test existing GTU on non measuring lane
        sampler.snapshot(lane3, Length.ZERO, Speed.ofSI(25.0), Acceleration.ZERO, Duration.ZERO, car1);
        // test non-existing (in sampler) GTU on existing lane
        sampler.snapshot(lane1, Length.ZERO, Speed.ofSI(25.0), Acceleration.ZERO, Duration.ZERO, car3);
        sampler.removeGtu(lane3, car1);
        sampler.removeGtu(lane1, car3);

        // remove
        sampler.removeGtuWithSnapshot(lane1, length, Speed.ofSI(25.0), Acceleration.ZERO, Duration.ofSI(40.0), car1);
        sampler.removeGtuWithSnapshot(lane1, length, Speed.ofSI(25.0), Acceleration.ZERO, Duration.ofSI(50.0), car2);
        sampler.removeGtuWithSnapshot(lane2, length, Speed.ofSI(20.0), Acceleration.ZERO, Duration.ofSI(60.0), truck);

        assertEquals(2, sampler.getSamplerData().getLanes().size());

        // sampler data
        testSamplerData(sampler);

        // query
        link.getLanes().remove(lane3);
        testQuery(sampler, space1, space2, filterDataGtuType, link);

        // cross sections
        crossSectionTest(sampler, link);

        // trajectory accept list
        trajectoryAcceptListTest(sampler, lane1);
    }

    /**
     * Test sampler data.
     * @param sampler sampler
     */
    private void testSamplerData(final TestSampler sampler)
    {
        try
        {
            sampler.getSamplerData().writeToFile(null);
            fail("Should fail on write to empty file name.");
        }
        catch (NullPointerException ex)
        {
            // correct
        }

        try
        {
            sampler.getSamplerData().writeToFile("", Compression.NONE);
            fail("Should fail on write to empty file name.");
        }
        catch (OtsRuntimeException ex)
        {
            assertTrue(IOException.class.isAssignableFrom(ex.getCause().getClass()));
        }

        assertFalse(sampler.getSamplerData().isEmpty());
        int i = 0;
        for (@SuppressWarnings("unused")
        Row row : sampler.getSamplerData())
        {
            i++;
        }
        assertEquals(6, i); // 3 GTUs with 2 points each
    }

    /**
     * Test query and kpi's.
     * @param sampler sampler
     * @param space1 lane 1 space-time region
     * @param space2 lane 2 space-time region
     * @param filterDataGtuType GTU type filter
     * @param link link
     */
    @SuppressWarnings("unlikely-arg-type")
    private void testQuery(final TestSampler sampler, final SpaceTimeRegion<TestLaneData> space1,
            final SpaceTimeRegion<TestLaneData> space2, final FilterDataGtuType filterDataGtuType, final TestLinkData link)
    {
        FilterDataSet filterDataSet = new FilterDataSet();
        filterDataSet.put(filterDataGtuType, Set.of("car"));
        testFilterDataSet(filterDataSet, filterDataGtuType);

        Query<TestGtuData, TestLaneData> query = new Query<>(sampler, "id", "description", filterDataSet);
        assertEquals("id", query.getId());
        assertEquals("description", query.getDescription());
        assertNull(query.getUpdateFrequency());
        assertNull(query.getInterval());
        assertEquals(1, query.filterSize());
        assertTrue(query.getFilterDataSetIterator().hasNext());
        assertEquals(sampler, query.getSampler());

        query.hashCode();
        assertTrue(query.equals(query));
        assertFalse(query.equals(null));
        assertFalse(query.equals("This is not a query."));
        assertTrue(query.equals(new Query<TestGtuData, TestLaneData>(sampler, "id", "description", filterDataSet)));
        assertFalse(query.equals(new Query<TestGtuData, TestLaneData>(
                new TestSampler(Collections.emptySet(), Collections.emptySet(), new TestSimulator()), "id", "description",
                filterDataSet)));
        assertFalse(query.equals(new Query<TestGtuData, TestLaneData>(sampler, "id2", "description", filterDataSet)));
        assertFalse(query.equals(new Query<TestGtuData, TestLaneData>(sampler, "id", "description2", filterDataSet)));
        assertFalse(query.equals(new Query<TestGtuData, TestLaneData>(sampler, "id", "description", new FilterDataSet())));

        assertFalse(query.equals(
                new Query<TestGtuData, TestLaneData>(sampler, "id", "description", filterDataSet, Frequency.ONE, null)));
        assertFalse(query
                .equals(new Query<TestGtuData, TestLaneData>(sampler, "id", "description", filterDataSet, null, Duration.ONE)));
        assertNotNull(query.toString());
        assertFalse(query.getSpaceTimeIterator().hasNext());

        Duration t = Duration.ofSI(30.0);
        for (boolean wholeLink : new boolean[] {false, true})
        {
            for (boolean includeTrucks : new boolean[] {false, true})
            {
                filterDataSet = new FilterDataSet();
                if (includeTrucks)
                {
                    filterDataSet.put(filterDataGtuType, Set.of("car", "truck"));
                }
                else
                {
                    filterDataSet.put(filterDataGtuType, Set.of("car"));
                }
                query = new Query<>(sampler, "id", "description", filterDataSet);
                if (wholeLink)
                {
                    query.addSpaceTimeRegionLink(link, Length.ZERO, space1.endPosition(), Duration.ZERO, t);
                }
                else
                {
                    query.addSpaceTimeRegion(space1.lane(), Length.ZERO, space1.endPosition(), Duration.ZERO, t);
                }
                for (double tEnd = 0.0; tEnd < 35.0; tEnd += 10.0)
                {
                    List<TrajectoryGroup<TestGtuData>> groups = query.getTrajectoryGroups(Duration.ofSI(tEnd));
                    testTrajectoryGroup(groups.get(0));

                    assertEquals(wholeLink ? 2 : 1, groups.size());
                    assertEquals(wholeLink ? 2 : 1, query.spaceTimeRegionSize());

                    t = Duration.ofSI(tEnd);
                    double t2 = tEnd > 10.0 ? tEnd - 10.0 : 0.0;
                    double totalTravelDistance = tEnd * 25.0 + t2 * 25.0 + (wholeLink & includeTrucks ? t2 * 20.0 : 0.0);
                    TotalTravelDistance totalTravelDistanceKpi = new TotalTravelDistance();
                    assertEquals(totalTravelDistance, totalTravelDistanceKpi.getValue(query, t, groups).si, 0.001);
                    double totalTravelTime = tEnd + t2 + (wholeLink & includeTrucks ? t2 : 0.0);
                    TotalTravelTime totalTravelTimeKpi = new TotalTravelTime();
                    assertEquals(totalTravelTime, totalTravelTimeKpi.getValue(query, t, groups).si, 0.001);
                    double meanTripLength;
                    if (tEnd == 0.0)
                    {
                        meanTripLength = Double.NaN;
                    }
                    else if (tEnd <= 10.0)
                    {
                        meanTripLength = totalTravelDistance;
                    }
                    else
                    {
                        meanTripLength = totalTravelDistance / (wholeLink & includeTrucks ? 3.0 : 2.0);
                    }
                    assertEquals(meanTripLength, new MeanTripLength().getValue(query, t, groups).si, 0.001);
                    double meanSpeed = totalTravelDistance / totalTravelTime;
                    MeanSpeed meanSpeedKpi = new MeanSpeed(totalTravelDistanceKpi, totalTravelTimeKpi);
                    assertEquals(meanSpeed, meanSpeedKpi.getValue(query, t, groups).si, 0.001);
                    double area = 1000.0 * tEnd * (wholeLink ? 2 : 1);
                    double density = totalTravelTime / area;
                    assertEquals(density, new MeanDensity(totalTravelTimeKpi).getValue(query, t, groups).si, 0.001);
                    double intensity = totalTravelDistance / area;
                    assertEquals(intensity, new MeanIntensity(totalTravelDistanceKpi).getValue(query, t, groups).si, 0.001);
                    assertEquals(1.0 / meanSpeed, new MeanTravelTimePerDistance(meanSpeedKpi).getValue(query, t, groups).si,
                            0.001);
                    assertEquals(0.0, new TotalNumberOfStops().getValue(query, t, groups).si, 0.001);
                    // ref based on fixed speed
                    double refTravelTime =
                            tEnd * 25.0 / 35.0 + t2 * 25.0 / 35.0 + (wholeLink & includeTrucks ? t2 * 20.0 / 35.0 : 0.0);
                    assertEquals(totalTravelTime - refTravelTime,
                            new TotalDelay(Speed.ofSI(35.0)).getValue(query, t, groups).si, 0.001);
                    // ref based on provider giving pre-defined ref speeds per GTU type
                    refTravelTime =
                            tEnd * 25.0 / 33.0 + t2 * 25.0 / 33.0 + (wholeLink & includeTrucks ? t2 * 20.0 / 22.0 : 0.0);
                    assertEquals(totalTravelTime - refTravelTime,
                            new TotalDelay((lane, gtuTypeId) -> Speed.ofSI(gtuTypeId.equals("car") ? 33.0 : 22.0))
                                    .getValue(query, t, groups).si,
                            0.001);
                    // ref based on recorded ref speed, which is equal to ref speeds per GTU type
                    assertEquals(totalTravelTime - refTravelTime, new TotalDelayReference().getValue(query, t, groups).si,
                            0.001);
                }
            }
        }

    }

    /**
     * Test trajectory group.
     * @param group group
     */
    @SuppressWarnings("unlikely-arg-type")
    private void testTrajectoryGroup(final TrajectoryGroup<?> group)
    {
        LaneData<?> lane = group.getLane();
        TestGtuData gtu = new TestGtuData("5", "A", "B", "bus", "routeAB", Speed.ZERO);
        assertFalse(group.contains(new Trajectory<>(gtu, new LinkedHashMap<>(), new LinkedHashSet<>())));
        if (group.size() > 0)
        {
            assertTrue(group.contains((Trajectory<?>) group.getTrajectories().get(0)));
        }
        assertEquals(group.size(), group.getTrajectoryGroup(Length.ZERO, Length.ofSI(1000.0)).size());
        assertEquals(0, group.getTrajectoryGroup(Length.ofSI(900.0), Length.ofSI(1000.0)).size());
        assertEquals(group.size(), group.getTrajectoryGroup(Duration.ZERO, Duration.ofSI(60.0)).size());
        assertEquals(0, group.getTrajectoryGroup(Duration.ofSI(50.0), Duration.ofSI(60.0)).size());

        group.hashCode();
        assertTrue(group.equals(group));
        assertFalse(group.equals(null));
        assertFalse(group.equals("This is not a group."));
        assertFalse(group.equals(new TrajectoryGroup<>(Duration.ofSI(1.0), lane)));
        assertFalse(group.equals(new TrajectoryGroup<>(Duration.ZERO, Length.ONE, Length.ofSI(1000.0), lane)));
        assertFalse(group.equals(new TrajectoryGroup<>(Duration.ZERO, Length.ZERO, Length.ofSI(1001.0), lane)));
        assertNotNull(group.toString());
    }

    /**
     * Test filter data set.
     * @param filterDataSet filter data set
     * @param filterDataGtuType GTU type filter
     */
    @SuppressWarnings("unlikely-arg-type")
    private void testFilterDataSet(final FilterDataSet filterDataSet, final FilterDataGtuType filterDataGtuType)
    {
        new FilterDataSet(filterDataSet);
        assertTrue(filterDataSet.contains(filterDataGtuType));
        assertFalse(filterDataSet.contains(new FilterDataRoute()));
        assertEquals(1, filterDataSet.size());
        assertEquals(1, filterDataSet.getFilterDataTypes().size());
        assertEquals(1, filterDataSet.get(filterDataGtuType).size());
        assertTrue(filterDataSet.getFilterDataSetIterator().hasNext());
        filterDataSet.hashCode();
        try
        {
            filterDataSet.get(new FilterDataRoute());
        }
        catch (NoSuchElementException ex)
        {
            // expected
        }
        assertTrue(filterDataSet.equals(filterDataSet));
        assertFalse(filterDataSet.equals(null));
        assertFalse(filterDataSet.equals("This is not a filter data set."));
        FilterDataSet filterDataSet2 = new FilterDataSet();
        filterDataSet2.put(filterDataGtuType, Set.of("car"));
        assertTrue(filterDataSet.equals(filterDataSet2));
        filterDataSet2 = new FilterDataSet();
        filterDataSet2.put(filterDataGtuType, Set.of("truck"));
        assertFalse(filterDataSet.equals(filterDataSet2));
        filterDataSet.toString();
    }

    /**
     * Test FilterDataCrossSection.
     * @param sampler sampler
     * @param link link
     */
    public void crossSectionTest(final TestSampler sampler, final TestLinkData link)
    {
        FilterDataSet filterDataSet = new FilterDataSet();
        CrossSection crossSection = new CrossSection(Set.of(new LanePosition(link.getLanes().get(0), Length.ofSI(600.0))));
        assertNotNull(crossSection.toString());
        assertEquals(1, crossSection.size());
        assertEquals(1, crossSection.getLanePositions().size());
        FilterDataCrossSections filterDataCrossSections = new FilterDataCrossSections();
        assertNotNull(filterDataCrossSections.toString());
        filterDataSet.put(filterDataCrossSections, Set.of(crossSection));
        // GTU 1 leaves at t=0, GTU 2 at t=10, both at 25m/s, then after 20/30/40s, 0/1/2 of them crossed 600m
        double[] tArray = new double[] {20.0, 30.0, 40.0};
        for (int i = 0; i < tArray.length; i++)
        {
            Query<TestGtuData, TestLaneData> query = new Query<>(sampler, "id", "description", filterDataSet);
            query.addSpaceTimeRegion(link.getLanes().get(0), Length.ZERO, Length.ofSI(1000.0), Duration.ZERO,
                    Duration.ofSI(tArray[i]));
            List<TrajectoryGroup<TestGtuData>> groups = query.getTrajectoryGroups(Duration.ofSI(60.0));
            assertEquals(i, groups.get(0).size());
        }

        crossSection = new CrossSection(link, 0.6);
        assertNotNull(crossSection.toString());
        assertEquals(2, crossSection.size());
        assertEquals(2, crossSection.getLanePositions().size());
    }

    /**
     * Test trajectoryAcceptListTest.
     * @param sampler sampler
     * @param lane lane to test on
     */
    public void trajectoryAcceptListTest(final TestSampler sampler, final TestLaneData lane)
    {
        TrajectoryAcceptList trajectoryAcceptList = new TrajectoryAcceptList();
        TrajectoryGroup<TestGtuData> group = sampler.getSamplerData().getTrajectoryGroup(lane);
        List<Trajectory<TestGtuData>> trajectories = new ArrayList<>();
        for (Trajectory<TestGtuData> trajectory : group)
        {
            if (trajectories.isEmpty())
            {
                trajectoryAcceptList.addTrajectory(trajectory, group);
            }
            else
            {
                try
                {
                    trajectoryAcceptList.addTrajectory(trajectory, group);
                    fail("Should throw IllegalArgumentException on different GTU in same TrajectoryAcceptList");
                }
                catch (IllegalArgumentException ex)
                {
                    // expected
                }
            }
            trajectories.add(trajectory);
        }

        trajectoryAcceptList.rejectAll();
        assertFalse(trajectoryAcceptList.isAccepted(trajectories.get(0)));
        try
        {
            trajectoryAcceptList.isAccepted(trajectories.get(1));
            fail("Should throw IllegalArgumentException on accept request of trajectory not in TrajectoryAcceptList");
        }
        catch (IllegalArgumentException ex)
        {
            // expected: may not add trajectory with different GTU id to same accept list
        }

        trajectoryAcceptList.acceptAll();
        assertTrue(trajectoryAcceptList.isAccepted(trajectories.get(0)));

        trajectoryAcceptList.rejectTrajectory(trajectories.get(0));
        assertFalse(trajectoryAcceptList.isAccepted(trajectories.get(0)));

        trajectoryAcceptList.acceptTrajectory(trajectories.get(0));
        assertTrue(trajectoryAcceptList.isAccepted(trajectories.get(0)));

        assertTrue(trajectoryAcceptList.getTrajectoryIterator().hasNext());
        assertTrue(trajectoryAcceptList.getTrajectoryGroupIterator().hasNext());

        assertNotNull(trajectoryAcceptList.toString());
    }

    /**
     * Helper extended data type without SI.
     */
    private static class Heading extends ExtendedDataFloat<DirectionUnit, FloatDirection, FloatDirectionVector, TestGtuData>
    {

        /**
         * Constructor.
         */
        Heading()
        {
            super("heading", "heading", FloatDirection.class);
        }

        @Override
        protected FloatDirection convertValue(final float value)
        {
            return FloatDirection.ofSI(value);
        }

        @Override
        protected FloatDirectionVector convert(final float[] storage)
        {
            return new FloatDirectionVector(storage, DirectionUnit.EAST_DEGREE);
        }

        @Override
        public FloatDirection getValue(final TestGtuData gtu)
        {
            return FloatDirection.ZERO;
        }

        @Override
        public FloatDirection parseValue(final String string)
        {
            return FloatDirection.valueOf(string);
        }

    }

}
