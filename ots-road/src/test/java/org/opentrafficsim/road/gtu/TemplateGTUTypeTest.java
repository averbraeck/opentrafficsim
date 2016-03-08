package org.opentrafficsim.road.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 15 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TemplateGTUTypeTest implements UNITS
{
    /** the random stream. */
    private StreamInterface stream = new MersenneTwister();

    /**
     * Test construction of a TemplateGTUType and prove that each one uses private fields.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void constructorTest() throws Exception
    {
        String pcTypeId = "passenger car";
        final ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistConstant(this.stream, 4), METER);
        final ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistConstant(this.stream, 1.6), METER);
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcInitialSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 125), KM_PER_HOUR);
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        SimpleSimulator simulator =
                new SimpleSimulator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(3600.0, SECOND), model);
        LaneBasedTemplateGTUType passengerCar =
                new LaneBasedTemplateGTUType(pcTypeId, new IdGenerator("Passenger car "), new Generator<Length.Rel>()
                {
                    public Length.Rel draw()
                    {
                        return pcLength.draw();
                    }
                }, new Generator<Length.Rel>()
                {
                    public Length.Rel draw()
                    {
                        return pcWidth.draw();
                    }
                }, new Generator<Speed>()
                {
                    public Speed draw()
                    {
                        return pcMaximumSpeed.draw();
                    }
                }, simulator, new Generator<LaneBasedStrategicalPlanner>()
                {
                    public LaneBasedStrategicalPlanner draw()
                    {
                        return null;
                    }
                }, new Generator<LanePerceptionFull>()
                {
                    public LanePerceptionFull draw()
                    {
                        return null;
                    }
                }, null, new Generator<Speed>()
                {
                    public Speed draw()
                    {
                        return pcInitialSpeed.draw();
                    }
                }, null);
        verifyFields(passengerCar, pcTypeId, pcLength, pcWidth, pcMaximumSpeed, pcInitialSpeed, simulator);
        String truckTypeId = "truck";
        ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckInitialSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 85), KM_PER_HOUR);
        SimpleSimulator truckSimulator =
                new SimpleSimulator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(3600.0, SECOND), model);
        LaneBasedTemplateGTUType truck =
                new LaneBasedTemplateGTUType(truckTypeId, new IdGenerator("Truck "), new Generator<Length.Rel>()
                {
                    public Length.Rel draw()
                    {
                        return truckLength.draw();
                    }
                }, new Generator<Length.Rel>()
                {
                    public Length.Rel draw()
                    {
                        return truckWidth.draw();
                    }
                }, new Generator<Speed>()
                {
                    public Speed draw()
                    {
                        return truckMaximumSpeed.draw();
                    }
                }, truckSimulator, new Generator<LaneBasedStrategicalPlanner>()
                {
                    public LaneBasedStrategicalPlanner draw()
                    {
                        return null;
                    }
                }, new Generator<LanePerceptionFull>()
                {
                    public LanePerceptionFull draw()
                    {
                        return null;
                    }
                }, null, new Generator<Speed>()
                {
                    public Speed draw()
                    {
                        return truckInitialSpeed.draw();
                    }
                }, null);
        verifyFields(truck, truckTypeId, truckLength, truckWidth, truckMaximumSpeed, truckInitialSpeed, truckSimulator);
    }

    /**
     * Test the isCompatible method.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public void compatibleLaneTypeTest() throws Exception
    {
        // Create some TemplateGTUTypes
        String pcId = "passenger car";
        ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistConstant(this.stream, 4), METER);
        ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistConstant(this.stream, 1.6), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        SimpleSimulator simulator =
                new SimpleSimulator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(3600.0, SECOND), model);
        TemplateGTUType passengerCar = new TemplateGTUType(pcId, new IdGenerator("Passenger car "), new Generator<Length.Rel>()
        {
            public Length.Rel draw()
            {
                return pcLength.draw();
            }
        }, new Generator<Length.Rel>()
        {
            public Length.Rel draw()
            {
                return pcWidth.draw();
            }
        }, new Generator<Speed>()
        {
            public Speed draw()
            {
                return pcMaximumSpeed.draw();
            }
        }, simulator, null);
        String truckId = "truck";
        ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        SimpleSimulator truckSimulator =
                new SimpleSimulator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND), new Time.Rel(3600.0, SECOND), model);
        TemplateGTUType truck = new TemplateGTUType(truckId, new IdGenerator("Truck "), new Generator<Length.Rel>()
        {
            public Length.Rel draw()
            {
                return truckLength.draw();
            }
        }, new Generator<Length.Rel>()
        {
            public Length.Rel draw()
            {
                return truckWidth.draw();
            }
        }, new Generator<Speed>()
        {
            public Speed draw()
            {
                return truckMaximumSpeed.draw();
            }
        }, truckSimulator, null);
        // Create some LaneTypes
        LaneType trucksForbidden = new LaneType("No Trucks");
        trucksForbidden.addCompatibility(passengerCar.getGtuType());
        LaneType trucksOnly = new LaneType("Trucks Only");
        trucksOnly.addCompatibility(truck.getGtuType());
        LaneType bicycleLane = new LaneType("Bicycles Only");
        LaneType urbanRoad = new LaneType("Urban road - open to all traffic");
        urbanRoad.addCompatibility(passengerCar.getGtuType());
        urbanRoad.addCompatibility(truck.getGtuType());
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
     * @param id String; the expected id
     * @param length Length.Rel; the expected length
     * @param width Length.Rel; the expected width
     * @param maximumSpeed Speed; the expected maximum velocity
     * @param simulator OTSDEVSSimulatorInterface; the expected simulator
     * @throws ProbabilityException
     */
    private void verifyFields(final LaneBasedTemplateGTUType templateGTUType, final String id,
            final ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> length,
            final ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> width,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeed,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeed, final OTSDEVSSimulatorInterface simulator)
            throws ProbabilityException
    {
        assertTrue("TypeId should be " + id, id.equals(templateGTUType.getGtuType().getId()));
        LaneBasedGTUCharacteristics characteristics = templateGTUType.draw();
        assertEquals("Length should be " + length, length.draw().getSI(), characteristics.getLength().getSI(), 0.0001);
        assertEquals("Width should be " + width, width.draw().getSI(), characteristics.getWidth().getSI(), 0.0001);
        assertEquals("Maximum speed should be " + maximumSpeed, maximumSpeed.draw().getSI(), characteristics
                .getMaximumVelocity().getSI(), 0.0001);
        assertEquals("Initial speed should be " + initialSpeed, initialSpeed.draw().getSI(), characteristics.getVelocity()
                .getSI(), 0.0001);
        assertEquals("Simulator", simulator, templateGTUType.getSimulator());
    }
}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator;

    /**
     * Register the simulator.
     * @param simulator SimulatorInterface&lt;Time.Abs, Time.Rel, OTSSimTimeDouble&gt;; the simulator
     */
    public void setSimulator(
            SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
    {
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> arg0)
            throws SimRuntimeException
    {
        // Nothing happens here
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()

    {
        if (null == this.simulator)
        {
            throw new Error("getSimulator called, but simulator field is null");
        }
        return this.simulator;
    }

}
