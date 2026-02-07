package org.opentrafficsim.road.gtu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuTemplate;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplate;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.LaneType;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class LaneBasedTemplateGtuTypeTest implements UNITS
{
    /** The random stream. */
    private StreamInterface stream = new MersenneTwister();

    /** */
    private LaneBasedTemplateGtuTypeTest()
    {
        // do not instantiate test class
    }

    /**
     * Test construction of a TemplateGTUType and prove that each one uses private fields.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void constructorTest() throws Exception
    {
        OtsSimulatorInterface simulator = new OtsSimulator("LaneBasedTemplateGTUTypeTest");
        GtuType pcType = DefaultsNl.CAR;
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 4), METER);
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 1.6), METER);
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        OtsModelInterface model = new DummyModelForTemplateGTUTest(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model,
                HistoryManagerDevs.noHistory(simulator));
        LaneBasedGtuTemplate passengerCar = new LaneBasedGtuTemplate(pcType, new Supplier<Length>()
        {
            @Override
            public Length get()
            {
                return pcLength.get();
            }
        }, new Supplier<Length>()
        {
            @Override
            public Length get()
            {
                return pcWidth.get();
            }
        }, new Supplier<Speed>()
        {
            @Override
            public Speed get()
            {
                return pcMaximumSpeed.get();
            }
        }, new DummyStrategicalPlannerFactory(), new FixedRouteGenerator(null));
        verifyFields(passengerCar, pcType, pcLength, pcWidth, pcMaximumSpeed);
        GtuType truckType = DefaultsNl.TRUCK;
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        LaneBasedGtuTemplate truck = new LaneBasedGtuTemplate(truckType, new Supplier<Length>()
        {
            @Override
            public Length get()
            {
                return truckLength.get();
            }
        }, new Supplier<Length>()
        {
            @Override
            public Length get()
            {
                return truckWidth.get();
            }
        }, new Supplier<Speed>()
        {
            @Override
            public Speed get()
            {
                return truckMaximumSpeed.get();
            }
        }, new DummyStrategicalPlannerFactory(), new FixedRouteGenerator(null));
        verifyFields(truck, truckType, truckLength, truckWidth, truckMaximumSpeed);
    }

    /**
     * Dummy strategical planner factory.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class DummyStrategicalPlannerFactory implements LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner>
    {

        /**
         *
         */
        DummyStrategicalPlannerFactory()
        {
        }

        @Override
        public LaneBasedStrategicalPlanner create(final LaneBasedGtu gtu, final Route route, final Node origin,
                final Node destination) throws GtuException
        {
            return null;
        }

    }

    /**
     * Test the isCompatible method.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void compatibleLaneTypeTest() throws Exception
    {
        // Create some TemplateGTUTypes
        GtuType pc = DefaultsNl.CAR;
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 4), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 1.6), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        GtuTemplate passengerCar = new GtuTemplate(pc, new Supplier<Length>()
        {
            @Override
            public Length get()
            {
                return pcLength.get();
            }
        }, new Supplier<Length>()
        {
            @Override
            public Length get()
            {
                return pcWidth.get();
            }
        }, new Supplier<Speed>()
        {
            @Override
            public Speed get()
            {
                return pcMaximumSpeed.get();
            }
        });
        GtuType truckType = DefaultsNl.TRUCK;
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        GtuTemplate truck = new GtuTemplate(truckType, new Supplier<Length>()
        {
            @Override
            public Length get()
            {
                return truckLength.get();
            }
        }, new Supplier<Length>()
        {
            @Override
            public Length get()
            {
                return truckWidth.get();
            }
        }, new Supplier<Speed>()
        {
            @Override
            public Speed get()
            {
                return truckMaximumSpeed.get();
            }
        });

        // Create some LaneTypes
        LaneType trucksForbidden = new LaneType("No Trucks", null);
        trucksForbidden.addCompatibleGtuType(passengerCar.getGtuType());

        LaneType trucksOnly = new LaneType("Trucks Only", null);
        trucksOnly.addCompatibleGtuType(truck.getGtuType());

        LaneType bicycleLane = new LaneType("Bicycles Only", null);
        bicycleLane.addCompatibleGtuType(DefaultsNl.BICYCLE);

        LaneType urbanRoad = new LaneType("Urban road - open to all traffic", null);
        urbanRoad.addCompatibleGtuType(passengerCar.getGtuType());
        urbanRoad.addCompatibleGtuType(truck.getGtuType());

        // Now we test all combinations
        assertTrue(trucksForbidden.isCompatible(passengerCar.getGtuType()), "Passengers cars are allowed on a no trucks lane");
        assertFalse(trucksForbidden.isCompatible(truck.getGtuType()), "Trucks are not allowed on a no trucks lane");
        assertFalse(trucksOnly.isCompatible(passengerCar.getGtuType()), "Passenger cars are not allowed on a trucks only lane");
        assertTrue(trucksOnly.isCompatible(truck.getGtuType()), "Trucks are allowed on a trucks only lane");
        assertTrue(urbanRoad.isCompatible(passengerCar.getGtuType()), "Passenger cars are allowed on an urban road");
        assertTrue(urbanRoad.isCompatible(truck.getGtuType()), "Trucks are allowed on an urban road");
        assertFalse(bicycleLane.isCompatible(passengerCar.getGtuType()), "Passenger cars are not allowed on a bicycle path");
        assertFalse(bicycleLane.isCompatible(truck.getGtuType()), "Trucks are not allowed on an urban road");
    }

    /**
     * Verify all the values in a TemplateGTUType&lt;String&gt;.
     * @param templateGtuType the TemplateGTUType
     * @param gtuType the expected id
     * @param length the expected length
     * @param width the expected width
     * @param maximumSpeed the expected maximum speed
     * @throws ParameterException in case of a parameter problem.
     * @throws GtuException in case of a GTU exception
     * @throws NamingException in case of a naming exception
     */
    private void verifyFields(final LaneBasedGtuTemplate templateGtuType, final GtuType gtuType,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> length,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> width,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeed)
            throws ParameterException, NamingException, GtuException
    {
        assertTrue(gtuType.equals(templateGtuType.getGtuType()), "Type should be " + gtuType);
        LaneBasedGtuCharacteristics characteristics = templateGtuType.draw();
        assertEquals(length.get().getSI(), characteristics.getLength().getSI(), 0.0001, "Length should be " + length);
        assertEquals(width.get().getSI(), characteristics.getWidth().getSI(), 0.0001, "Width should be " + width);
        assertEquals(maximumSpeed.get().getSI(), characteristics.getMaximumSpeed().getSI(), 0.0001,
                "Maximum speed should be " + maximumSpeed);
    }

    /**
     * Dummy OtsModelInterface.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     */
    static class DummyModelForTemplateGTUTest extends AbstractOtsModel
    {
        /**
         * Constructor.
         * @param simulator the simulator to use
         */
        DummyModelForTemplateGTUTest(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        @Override
        public final RoadNetwork getNetwork()
        {
            return null;
        }
    }

}
