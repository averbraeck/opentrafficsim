package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.units.distributions.DistContinuousDoubleScalar;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 15 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TemplateGTUTypeTest
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
        String pcId = "passenger car";
        DistContinuousDoubleScalar.Rel<LengthUnit> pcLength =
            new DistContinuousDoubleScalar.Rel<LengthUnit>(new DistConstant(this.stream, 4), LengthUnit.METER);
        DistContinuousDoubleScalar.Rel<LengthUnit> pcWidth =
            new DistContinuousDoubleScalar.Rel<LengthUnit>(new DistConstant(this.stream, 1.6), LengthUnit.METER);
        DistContinuousDoubleScalar.Abs<SpeedUnit> pcMaximumSpeed =
            new DistContinuousDoubleScalar.Abs<SpeedUnit>(new DistConstant(this.stream, 180), SpeedUnit.KM_PER_HOUR);
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        SimpleSimulator simulator =
            new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(0.0,
                TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model);
        TemplateGTUType passengerCar = new TemplateGTUType(pcId, pcLength, pcWidth, pcMaximumSpeed, simulator);
        verifyFields(passengerCar, pcId, pcLength, pcWidth, pcMaximumSpeed, simulator);
        String truckId = "truck";
        DistContinuousDoubleScalar.Rel<LengthUnit> truckLength =
            new DistContinuousDoubleScalar.Rel<LengthUnit>(new DistConstant(this.stream, 18), LengthUnit.METER);
        DistContinuousDoubleScalar.Rel<LengthUnit> truckWidth =
            new DistContinuousDoubleScalar.Rel<LengthUnit>(new DistConstant(this.stream, 2.2), LengthUnit.METER);
        DistContinuousDoubleScalar.Abs<SpeedUnit> truckMaximumSpeed =
            new DistContinuousDoubleScalar.Abs<SpeedUnit>(new DistConstant(this.stream, 110), SpeedUnit.KM_PER_HOUR);
        SimpleSimulator truckSimulator =
            new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(0.0,
                TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model);
        TemplateGTUType truck = new TemplateGTUType(truckId, truckLength, truckWidth, truckMaximumSpeed, truckSimulator);
        verifyFields(truck, truckId, truckLength, truckWidth, truckMaximumSpeed, truckSimulator);
        verifyFields(passengerCar, pcId, pcLength, pcWidth, pcMaximumSpeed, simulator);
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
        DistContinuousDoubleScalar.Rel<LengthUnit> pcLength =
            new DistContinuousDoubleScalar.Rel<LengthUnit>(new DistConstant(this.stream, 4), LengthUnit.METER);
        DistContinuousDoubleScalar.Rel<LengthUnit> pcWidth =
            new DistContinuousDoubleScalar.Rel<LengthUnit>(new DistConstant(this.stream, 1.6), LengthUnit.METER);
        DistContinuousDoubleScalar.Abs<SpeedUnit> pcMaximumSpeed =
            new DistContinuousDoubleScalar.Abs<SpeedUnit>(new DistConstant(this.stream, 180), SpeedUnit.KM_PER_HOUR);
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        SimpleSimulator simulator =
            new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(0.0,
                TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model);
        TemplateGTUType passengerCar = new TemplateGTUType(pcId, pcLength, pcWidth, pcMaximumSpeed, simulator);
        String truckId = "truck";
        DistContinuousDoubleScalar.Rel<LengthUnit> truckLength =
            new DistContinuousDoubleScalar.Rel<LengthUnit>(new DistConstant(this.stream, 18), LengthUnit.METER);
        DistContinuousDoubleScalar.Rel<LengthUnit> truckWidth =
            new DistContinuousDoubleScalar.Rel<LengthUnit>(new DistConstant(this.stream, 2.2), LengthUnit.METER);
        DistContinuousDoubleScalar.Abs<SpeedUnit> truckMaximumSpeed =
            new DistContinuousDoubleScalar.Abs<SpeedUnit>(new DistConstant(this.stream, 110), SpeedUnit.KM_PER_HOUR);
        SimpleSimulator truckSimulator =
            new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(0.0,
                TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model);
        TemplateGTUType truck = new TemplateGTUType(truckId, truckLength, truckWidth, truckMaximumSpeed, truckSimulator);
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
        assertTrue("Passengers cars are allowed on a no trucks lane", passengerCar.isCompatible(trucksForbidden));
        assertFalse("Trucks are not allowed on a no trucks lane", truck.isCompatible(trucksForbidden));
        assertFalse("Passenger cars are not allowed on a trucks only lane", passengerCar.isCompatible(trucksOnly));
        assertTrue("Trucks are allowed on a trucks only lane", truck.isCompatible(trucksOnly));
        assertTrue("Passenger cars are allowed on an urban road", passengerCar.isCompatible(urbanRoad));
        assertTrue("Trucks are allowed on an urban road", truck.isCompatible(urbanRoad));
        assertFalse("Passenger cars are not allowed on a bicycle path", passengerCar.isCompatible(bicycleLane));
        assertFalse("Trucks are not allowed on an urban road", truck.isCompatible(bicycleLane));
    }

    /**
     * Verify all the values in a TemplateGTUType&lt;String&gt;.
     * @param templateGTUType TemplateGTUType&lt;String&gt;; the TemplateGTUType
     * @param id String; the expected id
     * @param length DoubleScalar.Rel&lt;LengthUnit&gt;; the expected length
     * @param width DoubleScalar.Rel&lt;LengthUnit&gt;; the expected width
     * @param maximumSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the expected maximum velocity
     * @param simulator OTSDEVSSimulatorInterface; the expected simulator
     */
    private void verifyFields(final TemplateGTUType templateGTUType, final String id,
        final DistContinuousDoubleScalar.Rel<LengthUnit> length, final DistContinuousDoubleScalar.Rel<LengthUnit> width,
        final DistContinuousDoubleScalar.Abs<SpeedUnit> maximumSpeed, final OTSDEVSSimulatorInterface simulator)
    {
        assertTrue("Id should be " + id, id.equals(templateGTUType.getGtuType().getId()));
        assertEquals("Length should be " + length, length.draw().getSI(), templateGTUType.getLength().getSI(), 0.0001);
        assertEquals("Width should be " + width, width.draw().getSI(), templateGTUType.getWidth().getSI(), 0.0001);
        assertEquals("Maximum speed should be " + maximumSpeed, maximumSpeed.draw().getSI(), templateGTUType
            .getMaximumVelocity().getSI(), 0.0001);
        assertEquals("Simulator", simulator, templateGTUType.getSimulator());
    }
}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
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
     * @param simulator SimulatorInterface&lt;DoubleScalar.Abs&lt;TimeUnit&gt;, DoubleScalar.Rel&lt;TimeUnit&gt;,
     *            OTSSimTimeDouble&gt;; the simulator
     */
    public void setSimulator(
        SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
    {
        this.simulator = simulator;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(
        SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> arg0)
        throws SimRuntimeException, RemoteException
    {
        // Nothing happens here
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
        throws RemoteException
    {
        if (null == this.simulator)
        {
            throw new Error("getSimulator called, but simulator field is null");
        }
        return this.simulator;
    }

}
