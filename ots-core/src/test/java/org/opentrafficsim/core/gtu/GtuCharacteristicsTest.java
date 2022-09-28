package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;

/**
 * Test the GTUCharacteristics class
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuCharacteristicsTest implements OTSModelInterface
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
        OTSNetwork network = new OTSNetwork("network", true, this.simulator);
        // Make two sets of values so we can prove that the constructed GTUCharacteristics sets are really distinct.
        GtuType gtuTypeA = new GtuType("Type A", network.getGtuType(GtuType.DEFAULTS.VEHICLE));
        GtuType gtuTypeB = new GtuType("Type B", network.getGtuType(GtuType.DEFAULTS.VEHICLE));
        Length lengthA = new Length(5, LengthUnit.METER);
        Length lengthB = new Length(10, LengthUnit.METER);
        Length widthA = new Length(2, LengthUnit.METER);
        Length widthB = new Length(2.5, LengthUnit.METER);
        Speed maximumSpeedA = new Speed(180, SpeedUnit.KM_PER_HOUR);
        Speed maximumSpeedB = new Speed(130, SpeedUnit.KM_PER_HOUR);
        GtuCharacteristics gtucA = new GtuCharacteristics(gtuTypeA, lengthA, widthA, maximumSpeedA,
                Acceleration.instantiateSI(3.0), Acceleration.instantiateSI(-8.0), lengthA.times(0.5));
        GtuCharacteristics gtucB = new GtuCharacteristics(gtuTypeB, lengthB, widthB, maximumSpeedB,
                Acceleration.instantiateSI(3.0), Acceleration.instantiateSI(-8.0), lengthB.times(0.5));
        assertEquals("gtuTypeA", gtuTypeA, gtucA.getGtuType());
        assertEquals("gtuTypeB", gtuTypeB, gtucB.getGtuType());
        assertEquals("lengthA", lengthA, gtucA.getLength());
        assertEquals("lengthB", lengthB, gtucB.getLength());
        assertEquals("widthA", widthA, gtucA.getWidth());
        assertEquals("widthB", widthB, gtucB.getWidth());
        assertEquals("maximumSpeedA", maximumSpeedA, gtucA.getMaximumSpeed());
        assertEquals("maximumSpeedB", maximumSpeedB, gtucB.getMaximumSpeed());
    }

    /** ... */
    private OTSSimulatorInterface simulator;

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        // nothing to do
    }

    /** {@inheritDoc} */
    @Override
    public final OTSNetwork getNetwork()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InputParameterMap getInputParameterMap()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List getOutputStatistics()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public OTSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortName()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setStreamInformation(final StreamInformation streamInformation)
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public StreamInformation getStreamInformation()
    {
        return null;
    }
}
