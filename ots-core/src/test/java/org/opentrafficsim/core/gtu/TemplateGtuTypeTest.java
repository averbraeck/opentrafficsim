package org.opentrafficsim.core.gtu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.Supplier;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.statistics.SimulationStatistic;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class TemplateGtuTypeTest implements OtsModelInterface
{
    /** */
    private TemplateGtuTypeTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the TemplateGTUType class.
     * @throws NamingException should never happen
     * @throws SimRuntimeException should never happen
     * @throws GtuException should never happen
     * @throws ParameterException in case a parameter problem occurs
     */
    @Test
    public void templateGtuTypeTest() throws SimRuntimeException, NamingException, GtuException, ParameterException
    {
        GtuType gtuTypeA = new GtuType("type name A", DefaultsNl.VEHICLE);
        GtuType gtuTypeB = new GtuType("type name B", DefaultsNl.VEHICLE);
        Supplier<Length> lengthSupplierA = new Supplier<Length>()
        {

            @Override
            public Length get()
            {
                return new Length(123, LengthUnit.DECIMETER);
            }
        };
        Supplier<Length> lengthSupplierB = new Supplier<Length>()
        {

            @Override
            public Length get()
            {
                return new Length(234, LengthUnit.DECIMETER);
            }
        };
        Supplier<Length> widthSupplierA = new Supplier<Length>()
        {

            @Override
            public Length get()
            {
                return new Length(123, LengthUnit.CENTIMETER);
            }
        };
        Supplier<Length> widthSupplierB = new Supplier<Length>()
        {

            @Override
            public Length get()
            {
                return new Length(139, LengthUnit.CENTIMETER);
            }
        };
        Supplier<Speed> maximumSpeedSupplierA = new Supplier<Speed>()
        {

            @Override
            public Speed get()
            {
                return new Speed(50, SpeedUnit.KM_PER_HOUR);
            }
        };
        Supplier<Speed> maximumSpeedSupplierB = new Supplier<Speed>()
        {

            @Override
            public Speed get()
            {
                return new Speed(70, SpeedUnit.KM_PER_HOUR);
            }
        };
        GtuTemplate templateA = new GtuTemplate(gtuTypeA, lengthSupplierA, widthSupplierA, maximumSpeedSupplierA);
        GtuTemplate templateB = new GtuTemplate(gtuTypeB, lengthSupplierB, widthSupplierB, maximumSpeedSupplierB);
        assertEquals(gtuTypeA.getId(), templateA.getGtuType().getId(), "typenameA");
        assertEquals(gtuTypeB.getId(), templateB.getGtuType().getId(), "typenameB");
        GtuCharacteristics characteristicsA = templateA.get();
        GtuCharacteristics characteristicsB = templateB.get();
        assertEquals(gtuTypeA.getId(), characteristicsA.getGtuType().getId(), "typenameA");
        assertEquals(gtuTypeB.getId(), characteristicsB.getGtuType().getId(), "typenameB");
        assertEquals(lengthSupplierA.get(), characteristicsA.getLength(), "lengthA");
        assertEquals(lengthSupplierB.get(), characteristicsB.getLength(), "lengthB");
        assertEquals(widthSupplierA.get(), characteristicsA.getWidth(), "widthA");
        assertEquals(widthSupplierB.get(), characteristicsB.getWidth(), "widthB");
        assertEquals(maximumSpeedSupplierA.get(), characteristicsA.getMaximumSpeed(), "maximumSpeedA");
        assertEquals(maximumSpeedSupplierB.get(), characteristicsB.getMaximumSpeed(), "maximumSpeedB");
        // Ensure that toString returns non null
        assertNotNull(templateA.toString(), "toString should not return null");
        // Test that the constructor throws the expected Exception when an argument is invalid
        try
        {
            new GtuTemplate(null, lengthSupplierA, widthSupplierA, maximumSpeedSupplierA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new GtuTemplate(gtuTypeA, null, widthSupplierA, maximumSpeedSupplierA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new GtuTemplate(gtuTypeA, lengthSupplierA, null, maximumSpeedSupplierA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new GtuTemplate(gtuTypeA, lengthSupplierA, widthSupplierA, null);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
    }

    /** Simulator. */
    private OtsSimulatorInterface simulator;

    @Override
    public void constructModel() throws SimRuntimeException
    {
        //
    }

    @Override
    public Network getNetwork()
    {
        return null;
    }

    @Override
    public InputParameterMap getInputParameterMap()
    {
        return null;
    }

    @Override
    public List<SimulationStatistic<Duration>> getOutputStatistics()
    {
        return null;
    }

    @Override
    public OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    @Override
    public String getShortName()
    {
        return null;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public void setStreamInformation(final StreamInformation streamInformation)
    {
        //
    }

    @Override
    public StreamInformation getStreamInformation()
    {
        return null;
    }

    @Override
    public void setResetApplicationExecutable(final Runnable resetApplicationExecutable)
    {
    }

    @Override
    public Runnable getResetApplicationExecutable()
    {
        return null;
    }

    @Override
    public void resetApplication()
    {
    }

    @Override
    public void setInputParameterMap(final InputParameterMap inputParameterMap)
    {
    }
}
