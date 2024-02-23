package org.opentrafficsim.core.gtu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;

/**
 * Test the GtuCharacteristics class
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuCharacteristicsTest implements OtsModelInterface
{
    /** */
    private static final long serialVersionUID = 20160311L;

    /**
     * Test the GtuCharacteristics class.
     * @throws SimRuntimeException should never happen
     * @throws NamingException should never happen
     */
    @Test
    public final void testGtuCharacteristics() throws SimRuntimeException, NamingException
    {
        Network network = new Network("network", this.simulator);
        // Make two sets of values so we can prove that the constructed GtuCharacteristics sets are really distinct.
        GtuType gtuTypeA = new GtuType("Type A", DefaultsNl.VEHICLE);
        GtuType gtuTypeB = new GtuType("Type B", DefaultsNl.VEHICLE);
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
        assertEquals(gtuTypeA, gtucA.getGtuType(), "gtuTypeA");
        assertEquals(gtuTypeB, gtucB.getGtuType(), "gtuTypeB");
        assertEquals(lengthA, gtucA.getLength(), "lengthA");
        assertEquals(lengthB, gtucB.getLength(), "lengthB");
        assertEquals(widthA, gtucA.getWidth(), "widthA");
        assertEquals(widthB, gtucB.getWidth(), "widthB");
        assertEquals(maximumSpeedA, gtucA.getMaximumSpeed(), "maximumSpeedA");
        assertEquals(maximumSpeedB, gtucB.getMaximumSpeed(), "maximumSpeedB");
    }

    /** ... */
    private OtsSimulatorInterface simulator;

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        // nothing to do
    }

    /** {@inheritDoc} */
    @Override
    public final Network getNetwork()
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
    public OtsSimulatorInterface getSimulator()
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
