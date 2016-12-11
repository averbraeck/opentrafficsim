package org.opentrafficsim.road.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    public void constructorTest() throws Exception
    {
        OTSNetwork network = new OTSNetwork("network");
        GTUType pcType = new GTUType("passenger car");
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistConstant(this.stream, 4), METER);
        final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistConstant(this.stream, 1.6), METER);
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcInitialSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 125), KM_PER_HOUR);
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>();
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        SimpleSimulator simulator =
                new SimpleSimulator(new Time(0.0, SECOND), new Duration(0.0, SECOND), new Duration(3600.0, SECOND), model);
        LaneBasedTemplateGTUType passengerCar =
                new LaneBasedTemplateGTUType(pcType, new IdGenerator("Passenger car "), new Generator<Length>()
                {
                    public Length draw()
                    {
                        return pcLength.draw();
                    }
                }, new Generator<Length>()
                {
                    public Length draw()
                    {
                        return pcWidth.draw();
                    }
                }, new Generator<Speed>()
                {
                    public Speed draw()
                    {
                        return pcMaximumSpeed.draw();
                    }
                }, simulator, new DummyStrategicalPlannerFactory(), new FixedRouteGenerator(null),
                        /*-new Generator<LaneBasedStrategicalPlanner>()
                        {
                            public LaneBasedStrategicalPlanner draw()
                            {
                                return null;
                            }
                        }, 
                        */
                        initialLongitudinalPositions, new Generator<Speed>()
                        {
                            public Speed draw()
                            {
                                return pcInitialSpeed.draw();
                            }
                        }, network);
        verifyFields(passengerCar, pcType, pcLength, pcWidth, pcMaximumSpeed, pcInitialSpeed, simulator);
        GTUType truckType = new GTUType("truck");
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckInitialSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 85), KM_PER_HOUR);
        SimpleSimulator truckSimulator =
                new SimpleSimulator(new Time(0.0, SECOND), new Duration(0.0, SECOND), new Duration(3600.0, SECOND), model);
        LaneBasedTemplateGTUType truck =
                new LaneBasedTemplateGTUType(truckType, new IdGenerator("Truck "), new Generator<Length>()
                {
                    public Length draw()
                    {
                        return truckLength.draw();
                    }
                }, new Generator<Length>()
                {
                    public Length draw()
                    {
                        return truckWidth.draw();
                    }
                }, new Generator<Speed>()
                {
                    public Speed draw()
                    {
                        return truckMaximumSpeed.draw();
                    }
                }, truckSimulator, new DummyStrategicalPlannerFactory(), new FixedRouteGenerator(null),
                        /*-new Generator<LaneBasedStrategicalPlanner>()
                        {
                            public LaneBasedStrategicalPlanner draw()
                            {
                                return null;
                            }
                        },
                        */
                        initialLongitudinalPositions, new Generator<Speed>()
                        {
                            public Speed draw()
                            {
                                return truckInitialSpeed.draw();
                            }
                        }, network);
        verifyFields(truck, truckType, truckLength, truckWidth, truckMaximumSpeed, truckInitialSpeed, truckSimulator);
    }

    /**
     * Dummy strategical planner factory.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
        public DummyStrategicalPlannerFactory()
        {
        }

        /** {@inheritDoc} */
        @Override
        public BehavioralCharacteristics getDefaultBehavioralCharacteristics()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void setBehavioralCharacteristics(final BehavioralCharacteristics behavioralCharacteristics)
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedStrategicalPlanner create(LaneBasedGTU gtu, Route route) throws GTUException
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
        OTSNetwork network = new OTSNetwork("network");
        GTUType pc = new GTUType("passenger car");
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcLength =
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistConstant(this.stream, 4), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> pcWidth =
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistConstant(this.stream, 1.6), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> pcMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 180), KM_PER_HOUR);
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        SimpleSimulator simulator =
                new SimpleSimulator(new Time(0.0, SECOND), new Duration(0.0, SECOND), new Duration(3600.0, SECOND), model);
        TemplateGTUType passengerCar = new TemplateGTUType(pc, new IdGenerator("Passenger car "), new Generator<Length>()
        {
            public Length draw()
            {
                return pcLength.draw();
            }
        }, new Generator<Length>()
        {
            public Length draw()
            {
                return pcWidth.draw();
            }
        }, new Generator<Speed>()
        {
            public Speed draw()
            {
                return pcMaximumSpeed.draw();
            }
        }, simulator, network);
        GTUType truckType = new GTUType("truck");
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckLength =
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistConstant(this.stream, 18), METER);
        ContinuousDistDoubleScalar.Rel<Length, LengthUnit> truckWidth =
                new ContinuousDistDoubleScalar.Rel<Length, LengthUnit>(new DistConstant(this.stream, 2.2), METER);
        ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> truckMaximumSpeed =
                new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistConstant(this.stream, 110), KM_PER_HOUR);
        SimpleSimulator truckSimulator =
                new SimpleSimulator(new Time(0.0, SECOND), new Duration(0.0, SECOND), new Duration(3600.0, SECOND), model);
        TemplateGTUType truck = new TemplateGTUType(truckType, new IdGenerator("Truck "), new Generator<Length>()
        {
            public Length draw()
            {
                return truckLength.draw();
            }
        }, new Generator<Length>()
        {
            public Length draw()
            {
                return truckWidth.draw();
            }
        }, new Generator<Speed>()
        {
            public Speed draw()
            {
                return truckMaximumSpeed.draw();
            }
        }, truckSimulator, network);

        // Create some LaneTypes
        Set<GTUType> trucksForbiddenSet = new HashSet<>();
        trucksForbiddenSet.add(passengerCar.getGTUType());
        LaneType trucksForbidden = new LaneType("No Trucks", trucksForbiddenSet);

        Set<GTUType> trucksOnlySet = new HashSet<>();
        trucksOnlySet.add(truck.getGTUType());
        LaneType trucksOnly = new LaneType("Trucks Only", trucksOnlySet);

        LaneType bicycleLane = new LaneType("Bicycles Only", new HashSet<GTUType>());

        Set<GTUType> urbanRoadSet = new HashSet<>();
        urbanRoadSet.add(passengerCar.getGTUType());
        trucksOnlySet.add(truck.getGTUType());
        LaneType urbanRoad = new LaneType("Urban road - open to all traffic", urbanRoadSet);

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
     * @param initialSpeed Speed; the initial speed
     * @param simulator OTSDEVSSimulatorInterface; the expected simulator
     * @throws ProbabilityException in case of probability drawing exception
     * @throws ParameterException in case of a parameter problem.
     * @throws GTUException in case of a GTU exception
     * @throws NamingException in case of a naming exception
     */
    private void verifyFields(final LaneBasedTemplateGTUType templateGTUType, final GTUType gtuType,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> length,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> width,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeed,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeed, final OTSDEVSSimulatorInterface simulator)
            throws ProbabilityException, ParameterException, NamingException, GTUException
    {
        assertTrue("Type should be " + gtuType, gtuType.equals(templateGTUType.getGTUType()));
        LaneBasedGTUCharacteristics characteristics = templateGTUType.draw();
        assertEquals("Length should be " + length, length.draw().getSI(), characteristics.getLength().getSI(), 0.0001);
        assertEquals("Width should be " + width, width.draw().getSI(), characteristics.getWidth().getSI(), 0.0001);
        assertEquals("Maximum speed should be " + maximumSpeed, maximumSpeed.draw().getSI(),
                characteristics.getMaximumSpeed().getSI(), 0.0001);
        assertEquals("Initial speed should be " + initialSpeed, initialSpeed.draw().getSI(), characteristics.getSpeed().getSI(),
                0.0001);
        assertEquals("Simulator", simulator, templateGTUType.getSimulator());
    }
}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

    /**
     * Register the simulator.
     * @param simulator SimulatorInterface&lt;Time, Duration, OTSSimTimeDouble&gt;; the simulator
     */
    public void setSimulator(SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator)
    {
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Time, Duration, OTSSimTimeDouble> arg0) throws SimRuntimeException
    {
        // Nothing happens here
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()

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
