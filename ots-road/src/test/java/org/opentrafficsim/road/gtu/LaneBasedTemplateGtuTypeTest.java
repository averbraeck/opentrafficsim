package org.opentrafficsim.road.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.compatibility.GtuCompatibility;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.TemplateGtuType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGtuType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.LaneType;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedTemplateGtuTypeTest implements UNITS
{
    /** The random stream. */
    private StreamInterface stream = new MersenneTwister();

    /**
     * Test construction of a TemplateGTUType and prove that each one uses private fields.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void constructorTest() throws Exception
    {
        OtsSimulatorInterface simulator = new OtsSimulator("LaneBasedTemplateGTUTypeTest");
        OtsRoadNetwork network = new OtsRoadNetwork("TemplateGTU network", true, simulator);
        GtuType pcType = DefaultsNl.CAR;
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 4), METER);
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 1.6), METER);
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        OtsModelInterface model = new DummyModelForTemplateGTUTest(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(3600.0, DurationUnit.SECOND), model);
        LaneBasedTemplateGtuType passengerCar = new LaneBasedTemplateGtuType(pcType, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return pcLength.draw();
            }
        }, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return pcWidth.draw();
            }
        }, new Generator<Speed>()
        {
            @Override
            public Speed draw()
            {
                return pcMaximumSpeed.draw();
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
        LaneBasedTemplateGtuType truck = new LaneBasedTemplateGtuType(truckType, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return truckLength.draw();
            }
        }, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return truckWidth.draw();
            }
        }, new Generator<Speed>()
        {
            @Override
            public Speed draw()
            {
                return truckMaximumSpeed.draw();
            }
        }, new DummyStrategicalPlannerFactory(), new FixedRouteGenerator(null));
        verifyFields(truck, truckType, truckLength, truckWidth, truckMaximumSpeed);
    }

    /**
     * Dummy strategical planner factory.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class DummyStrategicalPlannerFactory implements LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner>
    {

        /**
         * 
         */
        DummyStrategicalPlannerFactory()
        {
        }

        /** {@inheritDoc} */
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
    public final void compatibleLaneTypeTest() throws Exception
    {
        OtsSimulatorInterface simulator = new OtsSimulator("LaneBasedTemplateGTUTypeTest");
        OtsRoadNetwork network = new OtsRoadNetwork("TemplateGTU network", true, simulator);
        // Create some TemplateGTUTypes
        GtuType pc = DefaultsNl.CAR;
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 4), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 1.6), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        TemplateGtuType passengerCar = new TemplateGtuType(pc, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return pcLength.draw();
            }
        }, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return pcWidth.draw();
            }
        }, new Generator<Speed>()
        {
            @Override
            public Speed draw()
            {
                return pcMaximumSpeed.draw();
            }
        });
        GtuType truckType = DefaultsNl.TRUCK;
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        TemplateGtuType truck = new TemplateGtuType(truckType, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return truckLength.draw();
            }
        }, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return truckWidth.draw();
            }
        }, new Generator<Speed>()
        {
            @Override
            public Speed draw()
            {
                return truckMaximumSpeed.draw();
            }
        });

        // Create some LaneTypes
        GtuCompatibility<LaneType> noTrucks = new GtuCompatibility<>((LaneType) null);
        noTrucks.addCompatibleGtuType(passengerCar.getGtuType());
        LaneType trucksForbidden = new LaneType("No Trucks", network.getLaneType(LaneType.DEFAULTS.FREEWAY), network);

        GtuCompatibility<LaneType> truckOnly = new GtuCompatibility<>((LaneType) null);
        truckOnly.addCompatibleGtuType(truck.getGtuType());
        LaneType trucksOnly = new LaneType("Trucks Only", network.getLaneType(LaneType.DEFAULTS.FREEWAY), network);

        GtuCompatibility<LaneType> bicyclesOnly = new GtuCompatibility<>((LaneType) null);
        LaneType bicycleLane =
                new LaneType("Bicycles Only", network.getLaneType(LaneType.DEFAULTS.FREEWAY), network);

        GtuCompatibility<LaneType> urban = new GtuCompatibility<>((LaneType) null);
        urban.addCompatibleGtuType(passengerCar.getGtuType());
        urban.addCompatibleGtuType(truck.getGtuType());
        LaneType urbanRoad = new LaneType("Urban road - open to all traffic", network.getLaneType(LaneType.DEFAULTS.FREEWAY),
                network);

        // Now we test all combinations
        // TODO assertTrue("Passengers cars are allowed on a no trucks lane", passengerCar.isCompatible(trucksForbidden));
        // TODO assertFalse("Trucks are not allowed on a no trucks lane", truck.isCompatible(trucksForbidden));
        // TODO assertFalse("Passenger cars are not allowed on a trucks only lane", passengerCar.isCompatible(trucksOnly));
        // TODO assertTrue("Trucks are allowed on a trucks only lane", truck.isCompatible(trucksOnly));
        // TODO assertTrue("Passenger cars are allowed on an urban road", passengerCar.isCompatible(urbanRoad));
        // TODO assertTrue("Trucks are allowed on an urban road", truck.isCompatible(urbanRoad));
        // TODO assertFalse("Passenger cars are not allowed on a bicycle path", passengerCar.isCompatible(bicycleLane));
        // TODO assertFalse("Trucks are not allowed on an urban road", truck.isCompatible(bicycleLane));
    }

    /**
     * Verify all the values in a TemplateGTUType&lt;String&gt;.
     * @param templateGtuType TemplateGTUType&lt;String&gt;; the TemplateGTUType
     * @param gtuType String; the expected id
     * @param length Length; the expected length
     * @param width Length; the expected width
     * @param maximumSpeed Speed; the expected maximum speed
     * @throws ProbabilityException in case of probability drawing exception
     * @throws ParameterException in case of a parameter problem.
     * @throws GtuException in case of a GTU exception
     * @throws NamingException in case of a naming exception
     */
    private void verifyFields(final LaneBasedTemplateGtuType templateGtuType, final GtuType gtuType,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> length,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> width,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeed)
            throws ProbabilityException, ParameterException, NamingException, GtuException
    {
        assertTrue("Type should be " + gtuType, gtuType.equals(templateGtuType.getGtuType()));
        LaneBasedGtuCharacteristics characteristics = templateGtuType.draw();
        assertEquals("Length should be " + length, length.draw().getSI(), characteristics.getLength().getSI(), 0.0001);
        assertEquals("Width should be " + width, width.draw().getSI(), characteristics.getWidth().getSI(), 0.0001);
        assertEquals("Maximum speed should be " + maximumSpeed, maximumSpeed.draw().getSI(),
                characteristics.getMaximumSpeed().getSI(), 0.0001);
    }

    /**
     * Dummy OTSModelInterface.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     */
    static class DummyModelForTemplateGTUTest extends AbstractOtsModel
    {
        /**
         * @param simulator the simulator to use
         */
        DummyModelForTemplateGTUTest(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** */
        private static final long serialVersionUID = 20141027L;

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public final OtsRoadNetwork getNetwork()
        {
            return null;
        }
    }

}
