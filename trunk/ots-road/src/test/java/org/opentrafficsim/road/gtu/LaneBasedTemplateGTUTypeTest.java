package org.opentrafficsim.road.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opentrafficsim.core.gtu.GTUType.CAR;
import static org.opentrafficsim.core.gtu.GTUType.TRUCK;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.compatibility.GTUCompatibility;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.lane.LaneType;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 15 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedTemplateGTUTypeTest implements UNITS
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
        GTUType pcType = CAR;
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 4), METER);
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 1.6), METER);
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        LaneBasedTemplateGTUType passengerCar = new LaneBasedTemplateGTUType(pcType, new Generator<Length>()
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
        GTUType truckType = TRUCK;
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        LaneBasedTemplateGTUType truck = new LaneBasedTemplateGTUType(truckType, new Generator<Length>()
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
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 2, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        public LaneBasedStrategicalPlanner create(final LaneBasedGTU gtu, final Route route, final Node origin,
                final Node destination) throws GTUException
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
        // Create some TemplateGTUTypes
        GTUType pc = CAR;
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 4), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 1.6), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        TemplateGTUType passengerCar = new TemplateGTUType(pc, new Generator<Length>()
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
        GTUType truckType = TRUCK;
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        TemplateGTUType truck = new TemplateGTUType(truckType, new Generator<Length>()
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
        GTUCompatibility<LaneType> noTrucks = new GTUCompatibility<>((LaneType) null);
        noTrucks.addAllowedGTUType(passengerCar.getGTUType(), LongitudinalDirectionality.DIR_BOTH);
        LaneType trucksForbidden = new LaneType("No Trucks", LaneType.FREEWAY, noTrucks);

        GTUCompatibility<LaneType> truckOnly = new GTUCompatibility<>((LaneType) null);
        truckOnly.addAllowedGTUType(truck.getGTUType(), LongitudinalDirectionality.DIR_BOTH);
        LaneType trucksOnly = new LaneType("Trucks Only", LaneType.FREEWAY, truckOnly);

        GTUCompatibility<LaneType> bicyclesOnly = new GTUCompatibility<>((LaneType) null);
        LaneType bicycleLane = new LaneType("Bicycles Only", LaneType.FREEWAY, bicyclesOnly);

        GTUCompatibility<LaneType> urban = new GTUCompatibility<>((LaneType) null);
        urban.addAllowedGTUType(passengerCar.getGTUType(), LongitudinalDirectionality.DIR_BOTH);
        urban.addAllowedGTUType(truck.getGTUType(), LongitudinalDirectionality.DIR_BOTH);
        LaneType urbanRoad = new LaneType("Urban road - open to all traffic", LaneType.FREEWAY, urban);

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
     * @param templateGTUType TemplateGTUType&lt;String&gt;; the TemplateGTUType
     * @param gtuType String; the expected id
     * @param length Length; the expected length
     * @param width Length; the expected width
     * @param maximumSpeed Speed; the expected maximum speed
     * @throws ProbabilityException in case of probability drawing exception
     * @throws ParameterException in case of a parameter problem.
     * @throws GTUException in case of a GTU exception
     * @throws NamingException in case of a naming exception
     */
    private void verifyFields(final LaneBasedTemplateGTUType templateGTUType, final GTUType gtuType,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> length,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> width,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeed)
            throws ProbabilityException, ParameterException, NamingException, GTUException
    {
        assertTrue("Type should be " + gtuType, gtuType.equals(templateGTUType.getGTUType()));
        LaneBasedGTUCharacteristics characteristics = templateGTUType.draw();
        assertEquals("Length should be " + length, length.draw().getSI(), characteristics.getLength().getSI(), 0.0001);
        assertEquals("Width should be " + width, width.draw().getSI(), characteristics.getWidth().getSI(), 0.0001);
        assertEquals("Maximum speed should be " + maximumSpeed, maximumSpeed.draw().getSI(),
                characteristics.getMaximumSpeed().getSI(), 0.0001);
    }
}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 4 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class DummyModelForTemplateGTUTest implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150114L;

    /** The simulator. */
    private SimulatorInterface<Time, Duration, SimTimeDoubleUnit> simulator;

    /**
     * Register the simulator.
     * @param simulator SimulatorInterface&lt;Time, Duration, SimTimeDoubleUnit&gt;; the simulator
     */
    public void setSimulator(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> simulator)
    {
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> arg0) throws SimRuntimeException
    {
        // Nothing happens here
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()

    {
        if (null == this.simulator)
        {
            throw new Error("getSimulator called, but simulator field is null");
        }
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return null;
    }

}
