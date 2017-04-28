package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.opentrafficsim.core.gtu.GTUType.VEHICLE;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Test the GTUCharacteristics class
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 11, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUCharacteristicsTest implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20160311L;

    /**
     * Test the GTUCharacteristics class.
     * @throws SimRuntimeException should never happen
     * @throws NamingException should never happen
     */
    @Test
    public final void testGTUCharacteristics() throws SimRuntimeException, NamingException
    {
        // Make two sets of values so we can prove that the constructed GTUCharacteristics sets are really distinct.
        GTUType gtuTypeA = new GTUType("Type A", VEHICLE);
        GTUType gtuTypeB = new GTUType("Type B", VEHICLE);
        IdGenerator idGeneratorA = new IdGenerator("A");
        IdGenerator idGeneratorB = new IdGenerator("B");
        Length lengthA = new Length(5, LengthUnit.METER);
        Length lengthB = new Length(10, LengthUnit.METER);
        Length widthA = new Length(2, LengthUnit.METER);
        Length widthB = new Length(2.5, LengthUnit.METER);
        Speed maximumSpeedA = new Speed(180, SpeedUnit.KM_PER_HOUR);
        Speed maximumSpeedB = new Speed(130, SpeedUnit.KM_PER_HOUR);
        OTSDEVSSimulatorInterface simulatorA = new SimpleSimulator(new Time(0, TimeUnit.SI), new Duration(0, TimeUnit.SI),
                new Duration(1000, TimeUnit.SECOND), this);
        OTSDEVSSimulatorInterface simulatorB = new SimpleSimulator(new Time(0, TimeUnit.SI), new Duration(0, TimeUnit.SI),
                new Duration(1000, TimeUnit.SECOND), this);
        OTSNetwork networkA = new OTSNetwork("testGTUCharacteristics A");
        OTSNetwork networkB = new OTSNetwork("testGTUCharacteristics B");
        GTUCharacteristics gtucA =
                new GTUCharacteristics(gtuTypeA, idGeneratorA, lengthA, widthA, maximumSpeedA, simulatorA, networkA);
        GTUCharacteristics gtucB =
                new GTUCharacteristics(gtuTypeB, idGeneratorB, lengthB, widthB, maximumSpeedB, simulatorB, networkB);
        assertEquals("gtuTypeA", gtuTypeA, gtucA.getGTUType());
        assertEquals("gtuTypeB", gtuTypeB, gtucB.getGTUType());
        assertEquals("idGeneratorA", idGeneratorA, gtucA.getIdGenerator());
        assertEquals("idGeneratorB", idGeneratorB, gtucB.getIdGenerator());
        assertEquals("lengthA", lengthA, gtucA.getLength());
        assertEquals("lengthB", lengthB, gtucB.getLength());
        assertEquals("widthA", widthA, gtucA.getWidth());
        assertEquals("widthB", widthB, gtucB.getWidth());
        assertEquals("maximumSpeedA", maximumSpeedA, gtucA.getMaximumSpeed());
        assertEquals("maximumSpeedB", maximumSpeedB, gtucB.getMaximumSpeed());
        assertEquals("simulatorA", simulatorA, gtucA.getSimulator());
        assertEquals("simulatorB", simulatorB, gtucB.getSimulator());
        assertEquals("networkA", networkA, gtucA.getNetwork());
        assertEquals("networkB", networkB, gtucB.getNetwork());
    }

    /** ... */
    private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = theSimulator;
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSNetwork getNetwork()
    {
        return null;
    }
}
