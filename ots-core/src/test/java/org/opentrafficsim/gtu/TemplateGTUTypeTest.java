package org.opentrafficsim.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.TemplateGTUType;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 15 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TemplateGTUTypeTest
{
    /**
     * Test construction of a TemplateGTUType and prove that each one uses private fields.
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    @Test
    public void constructorTest() throws RemoteException, SimRuntimeException
    {
        String pcId = "passenger car";
        DoubleScalar.Rel<LengthUnit> pcLength = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> pcWidth = new DoubleScalar.Rel<LengthUnit>(1.6, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> pcMaximumSpeed = new DoubleScalar.Abs<SpeedUnit>(180, SpeedUnit.KM_PER_HOUR);
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        SimpleSimulator simulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));
        TemplateGTUType<String> passengerCar =
                new TemplateGTUType<String>(pcId, pcLength, pcWidth, pcMaximumSpeed,
                        (OTSDEVSSimulatorInterface) simulator.getSimulator());
        verifyFields(passengerCar, pcId, pcLength, pcWidth, pcMaximumSpeed,
                (OTSDEVSSimulatorInterface) simulator.getSimulator());
        String truckId = "truck";
        DoubleScalar.Rel<LengthUnit> truckLength = new DoubleScalar.Rel<LengthUnit>(18, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> truckWidth = new DoubleScalar.Rel<LengthUnit>(2.2, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> truckMaximumSpeed = new DoubleScalar.Abs<SpeedUnit>(110, SpeedUnit.KM_PER_HOUR);
        SimpleSimulator truckSimulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));
        TemplateGTUType<String> truck =
                new TemplateGTUType<String>(truckId, truckLength, truckWidth, truckMaximumSpeed,
                        (OTSDEVSSimulatorInterface) truckSimulator.getSimulator());
        verifyFields(truck, truckId, truckLength, truckWidth, truckMaximumSpeed,
                (OTSDEVSSimulatorInterface) truckSimulator.getSimulator());
        verifyFields(passengerCar, pcId, pcLength, pcWidth, pcMaximumSpeed,
                (OTSDEVSSimulatorInterface) simulator.getSimulator());
    }
    
    /**
     * Test the isCompatible method.
     * @throws SimRuntimeException 
     * @throws RemoteException 
     */
    @Test
    public void compatibleLaneTypeTest() throws RemoteException, SimRuntimeException
    {
        // Create some TemplateGTUTypes
        String pcId = "passenger car";
        DoubleScalar.Rel<LengthUnit> pcLength = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> pcWidth = new DoubleScalar.Rel<LengthUnit>(1.6, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> pcMaximumSpeed = new DoubleScalar.Abs<SpeedUnit>(180, SpeedUnit.KM_PER_HOUR);
        OTSModelInterface model = new DummyModelForTemplateGTUTest();
        SimpleSimulator simulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));
        TemplateGTUType<String> passengerCar =
                new TemplateGTUType<String>(pcId, pcLength, pcWidth, pcMaximumSpeed,
                        (OTSDEVSSimulatorInterface) simulator.getSimulator());
        String truckId = "truck";
        DoubleScalar.Rel<LengthUnit> truckLength = new DoubleScalar.Rel<LengthUnit>(18, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> truckWidth = new DoubleScalar.Rel<LengthUnit>(2.2, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> truckMaximumSpeed = new DoubleScalar.Abs<SpeedUnit>(110, SpeedUnit.KM_PER_HOUR);
        SimpleSimulator truckSimulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));
        TemplateGTUType<String> truck =
                new TemplateGTUType<String>(truckId, truckLength, truckWidth, truckMaximumSpeed,
                        (OTSDEVSSimulatorInterface) truckSimulator.getSimulator());
        // Create some LaneTypes
        LaneType<String> trucksForbidden = new LaneType<String>("No Trucks");
        trucksForbidden.addPermeability(passengerCar);
        LaneType<String> trucksOnly = new LaneType<String>("Trucks Only");
        trucksOnly.addPermeability(truck);
        LaneType<String> bicycleLane = new LaneType<String>("Bicycles Only");
        LaneType<String> urbanRoad = new LaneType<String>("Urban road - open to all traffic");
        urbanRoad.addPermeability(passengerCar);
        urbanRoad.addPermeability(truck);
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
    private void verifyFields(final TemplateGTUType<String> templateGTUType, final String id,
            final DoubleScalar.Rel<LengthUnit> length, final DoubleScalar.Rel<LengthUnit> width,
            final DoubleScalar.Abs<SpeedUnit> maximumSpeed, final OTSDEVSSimulatorInterface simulator)
    {
        assertTrue("Id should be " + id, id.equals(templateGTUType.getId()));
        assertEquals("Length should be " + length, length.getSI(), templateGTUType.getLength().getSI(), 0.0001);
        assertEquals("Sidth should be " + width, width.getSI(), templateGTUType.getWidth().getSI(), 0.0001);
        assertEquals("Maximum speed should be " + maximumSpeed, maximumSpeed.getSI(), templateGTUType
                .getMaximumVelocity().getSI(), 0.0001);
        assertEquals("Simulator", simulator, templateGTUType.getSimulator());
    }
}

/**
 * Dummy OTSModelInterface.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 jan. 2015 <br>
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
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> arg0)
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
