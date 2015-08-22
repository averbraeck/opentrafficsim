package org.opentrafficsim.core.gtu.lane.changing;

import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.junit.Test;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.CompleteLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test some very basic properties of lane change models.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 14 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneChangeModelTest implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150313;

    /**
     * Create a Link.
     * @param name String; name of the new Link
     * @param from Node; start node of the new Link
     * @param to Node; end node of the new Link
     * @param width DoubleScalar.Rel&lt;LengthUnit&gt;; the width of the new Link
     * @return Link
     */
    private static CrossSectionLink makeLink(final String name, final OTSNode from, final OTSNode to,
        final DoubleScalar.Rel<LengthUnit> width)
    {
        // TODO create a LinkAnimation if the simulator is compatible with that.
        // FIXME The current LinkAnimation is too bad to use...
        OTSPoint3D[] coordinates =
            new OTSPoint3D[]{new OTSPoint3D(from.getPoint().x, from.getPoint().y, 0),
                new OTSPoint3D(to.getPoint().x, to.getPoint().y, 0)};
        OTSLine3D line = new OTSLine3D(coordinates);
        CrossSectionLink link = new CrossSectionLink(name, from, to, line);
        return link;
    }

    /**
     * Create one Lane.
     * @param link Link; the link that owns the new Lane
     * @param laneType LaneType&lt;String&gt;; the type of the new Lane
     * @param latPos DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral position of the new Lane with respect to the design line of
     *            the link
     * @param width DoubleScalar.Rel&lt;LengthUnit&gt;; the width of the new Lane
     * @return Lane
     * @throws RemoteException on communications failure
     * @throws NamingException on ???
     * @throws NetworkException on ??
     * @throws OTSGeometryException when center line or contour of a link or lane cannot be generated
     */
    private static Lane makeLane(final CrossSectionLink link, final LaneType laneType,
        final DoubleScalar.Rel<LengthUnit> latPos, final DoubleScalar.Rel<LengthUnit> width) throws RemoteException,
        NamingException, NetworkException, OTSGeometryException
    {
        DoubleScalar.Abs<FrequencyUnit> f2000 = new DoubleScalar.Abs<FrequencyUnit>(2000.0, FrequencyUnit.PER_HOUR);
        Lane result =
            new Lane(link, latPos, latPos, width, width, laneType, LongitudinalDirectionality.FORWARD, f2000,
                new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR));
        return result;
    }

    /**
     * Create a simple straight road with the specified number of Lanes.
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param laneType LaneType&lt;String&gt;; the type of GTU that can use the lanes
     * @param laneCount int; number of lanes in the road
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws Exception when something goes wrong (should not happen)
     */
    public static Lane[] makeMultiLane(final String name, final OTSNode from, final OTSNode to,
        final LaneType laneType, final int laneCount) throws Exception
    {
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(laneCount * 4.0, LengthUnit.METER);
        final CrossSectionLink link = makeLink(name, from, to, width);
        Lane[] result = new Lane[laneCount];
        width = new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            DoubleScalar.Rel<LengthUnit> latPos =
                new DoubleScalar.Rel<LengthUnit>((-0.5 - laneIndex) * width.getSI(), LengthUnit.METER);
            result[laneIndex] = makeLane(link, laneType, latPos, width);
        }
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            if (laneIndex < laneCount - 1)
            {
                result[laneIndex].addAccessibleAdjacentLane(result[laneIndex + 1], LateralDirectionality.RIGHT);
            }
            if (laneIndex > 0)
            {
                result[laneIndex].addAccessibleAdjacentLane(result[laneIndex - 1], LateralDirectionality.LEFT);
            }
        }
        return result;
    }

    /**
     * Test that a vehicle in the left lane changes to the right lane if that is empty, or there is enough room.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void changeRight() throws Exception
    {
        GTUType gtuType = GTUType.makeGTUType("car");
        LaneType laneType = new LaneType("CarLane");
        laneType.addCompatibility(gtuType);
        Lane[] lanes =
            makeMultiLane("Road with two lanes", new OTSNode("From", new OTSPoint3D(0, 0, 0)), new OTSNode("To",
                new OTSPoint3D(200, 0, 0)), laneType, 2);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions =
            new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialLongitudinalPositions.put(lanes[0], new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER));
        SimpleSimulator simpleSimulator =
            new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(0,
                TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600, TimeUnit.SECOND), this
            /*
             * CRASH - FIXME - will have to wait for Network factory
             */);
        AbstractLaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualCar car =
            new LaneBasedIndividualCar("ReferenceCar", gtuType, new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(
                1, AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.5,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.SECOND), 1d), laneChangeModel, initialLongitudinalPositions,
                new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), new DoubleScalar.Rel<LengthUnit>(4,
                    LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                new DoubleScalar.Abs<SpeedUnit>(150, SpeedUnit.KM_PER_HOUR), new CompleteLaneBasedRouteNavigator(
                    new CompleteRoute("")), simpleSimulator);
        Collection<HeadwayGTU> sameLaneGTUs = new LinkedHashSet<HeadwayGTU>();
        sameLaneGTUs.add(new HeadwayGTU(car, 0));
        Collection<HeadwayGTU> preferredLaneGTUs = new LinkedHashSet<HeadwayGTU>();
        Collection<HeadwayGTU> nonPreferredLaneGTUs = new LinkedHashSet<HeadwayGTU>();
        LaneMovementStep laneChangeModelResult =
            laneChangeModel.computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs, nonPreferredLaneGTUs,
                new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), new DoubleScalar.Rel<AccelerationUnit>(0.3,
                    AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<AccelerationUnit>(0.1,
                    AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<AccelerationUnit>(-0.3,
                    AccelerationUnit.METER_PER_SECOND_2));
        // System.out.println(laneChangeModelResult.toString());
        assertEquals("Vehicle want to change to the right lane", LateralDirectionality.RIGHT, laneChangeModelResult
            .getLaneChange());
        DoubleScalar.Rel<LengthUnit> rear = car.position(lanes[0], car.getRear());
        DoubleScalar.Rel<LengthUnit> front = car.position(lanes[0], car.getFront());
        DoubleScalar.Rel<LengthUnit> reference = car.position(lanes[0], RelativePosition.REFERENCE_POSITION);
        // System.out.println("rear:      " + rear);
        // System.out.println("front:     " + front);
        // System.out.println("reference: " + reference);
        DoubleScalar.Rel<LengthUnit> vehicleLength = DoubleScalar.minus(front, rear).immutable();
        DoubleScalar.Rel<LengthUnit> collisionStart = DoubleScalar.minus(reference, vehicleLength).immutable();
        DoubleScalar.Rel<LengthUnit> collisionEnd = DoubleScalar.plus(reference, vehicleLength).immutable();
        for (double pos = collisionStart.getSI() + 0.01; pos < collisionEnd.getSI() - 0.01; pos += 0.1)
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> otherLongitudinalPositions =
                new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            otherLongitudinalPositions.put(lanes[1], new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER));
            LaneBasedIndividualCar collisionCar =
                new LaneBasedIndividualCar("LaneChangeBlockingCar", gtuType, new IDMPlus(
                    new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                    new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                    new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                    new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.SECOND), 1d), laneChangeModel, otherLongitudinalPositions,
                    new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), vehicleLength,
                    new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(150,
                        SpeedUnit.KM_PER_HOUR), new CompleteLaneBasedRouteNavigator(new CompleteRoute("")), simpleSimulator);
            preferredLaneGTUs.clear();
            HeadwayGTU collisionHWGTU = new HeadwayGTU(collisionCar, pos - reference.getSI());
            preferredLaneGTUs.add(collisionHWGTU);
            laneChangeModelResult =
                new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs, nonPreferredLaneGTUs,
                    new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), new DoubleScalar.Rel<AccelerationUnit>(0.3,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<AccelerationUnit>(0.1,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<AccelerationUnit>(-0.3,
                        AccelerationUnit.METER_PER_SECOND_2));
            // System.out.println(laneChangeModelResult.toString());
            assertEquals("Vehicle cannot to change to the right lane because that would result in an immediate collision",
                null, laneChangeModelResult.getLaneChange());
        }
        for (double pos = 0; pos < 200; pos += 5)
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> otherLongitudinalPositions =
                new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            otherLongitudinalPositions.put(lanes[1], new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER));
            LaneBasedIndividualCar otherCar =
                new LaneBasedIndividualCar("OtherCar", gtuType, new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(
                    1, AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.5,
                    AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                    new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.SECOND), 1d), laneChangeModel, otherLongitudinalPositions,
                    new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), vehicleLength,
                    new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(150,
                        SpeedUnit.KM_PER_HOUR), new CompleteLaneBasedRouteNavigator(new CompleteRoute("")), simpleSimulator);
            preferredLaneGTUs.clear();
            HeadwayGTU collisionHWGTU = new HeadwayGTU(otherCar, pos - car.position(lanes[0], car.getReference()).getSI());
            preferredLaneGTUs.add(collisionHWGTU);
            laneChangeModelResult =
                new Egoistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs, nonPreferredLaneGTUs,
                    new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), new DoubleScalar.Rel<AccelerationUnit>(0.3,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<AccelerationUnit>(0.1,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<AccelerationUnit>(-0.3,
                        AccelerationUnit.METER_PER_SECOND_2));
            // System.out.println(String.format("pos=%5fm Egoistic:   %s", pos, laneChangeModelResult.toString()));
            laneChangeModelResult =
                new Altruistic().computeLaneChangeAndAcceleration(car, sameLaneGTUs, preferredLaneGTUs,
                    nonPreferredLaneGTUs, new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR),
                    new DoubleScalar.Rel<AccelerationUnit>(0.3, AccelerationUnit.METER_PER_SECOND_2),
                    new DoubleScalar.Rel<AccelerationUnit>(0.1, AccelerationUnit.METER_PER_SECOND_2),
                    new DoubleScalar.Rel<AccelerationUnit>(-0.3, AccelerationUnit.METER_PER_SECOND_2));
            // System.out.println(String.format("pos=%5fm Altruistic: %s", pos, laneChangeModelResult.toString()));
            // assertEquals(
            // "Vehicle cannot to change to the right lane because that would result in an immediate collision",
            // null, laneChangeModelResult.getLaneChange());
        }
    }

    // TODO test/prove the expected differences between Egoistic and Altruistic
    // TODO prove that the most restrictive car in the other lane determines what happens
    // TODO test merge into overtaking lane

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> simulator)
        throws SimRuntimeException, RemoteException
    {
        // DO NOTHING
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

}
