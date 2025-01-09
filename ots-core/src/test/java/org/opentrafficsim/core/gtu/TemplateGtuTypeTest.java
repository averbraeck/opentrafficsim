package org.opentrafficsim.core.gtu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class TemplateGtuTypeTest implements OtsModelInterface
{
    /** */
    private static final long serialVersionUID = 20160311L;

    /**
     * Test the TemplateGTUType class.
     * @throws NamingException should never happen
     * @throws SimRuntimeException should never happen
     * @throws GtuException should never happen
     * @throws ParameterException in case a parameter problem occurs
     */
    @Test
    public final void templateGtuTypeTest() throws SimRuntimeException, NamingException, GtuException, ParameterException
    {
        Network network = new Network("network", this.simulator);
        GtuType gtuTypeA = new GtuType("type name A", DefaultsNl.VEHICLE);
        GtuType gtuTypeB = new GtuType("type name B", DefaultsNl.VEHICLE);
        Generator<Length> lengthGeneratorA = new Generator<Length>()
        {

            @Override
            public Length draw()
            {
                return new Length(123, LengthUnit.DECIMETER);
            }
        };
        Generator<Length> lengthGeneratorB = new Generator<Length>()
        {

            @Override
            public Length draw()
            {
                return new Length(234, LengthUnit.DECIMETER);
            }
        };
        Generator<Length> widthGeneratorA = new Generator<Length>()
        {

            @Override
            public Length draw()
            {
                return new Length(123, LengthUnit.CENTIMETER);
            }
        };
        Generator<Length> widthGeneratorB = new Generator<Length>()
        {

            @Override
            public Length draw()
            {
                return new Length(139, LengthUnit.CENTIMETER);
            }
        };
        Generator<Speed> maximumSpeedGeneratorA = new Generator<Speed>()
        {

            @Override
            public Speed draw()
            {
                return new Speed(50, SpeedUnit.KM_PER_HOUR);
            }
        };
        Generator<Speed> maximumSpeedGeneratorB = new Generator<Speed>()
        {

            @Override
            public Speed draw()
            {
                return new Speed(70, SpeedUnit.KM_PER_HOUR);
            }
        };
        GtuTemplate templateA = new GtuTemplate(gtuTypeA, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA);
        GtuTemplate templateB = new GtuTemplate(gtuTypeB, lengthGeneratorB, widthGeneratorB, maximumSpeedGeneratorB);
        assertEquals(gtuTypeA.getId(), templateA.getGtuType().getId(), "typenameA");
        assertEquals(gtuTypeB.getId(), templateB.getGtuType().getId(), "typenameB");
        GtuCharacteristics characteristicsA = templateA.draw();
        GtuCharacteristics characteristicsB = templateB.draw();
        assertEquals(gtuTypeA.getId(), characteristicsA.getGtuType().getId(), "typenameA");
        assertEquals(gtuTypeB.getId(), characteristicsB.getGtuType().getId(), "typenameB");
        assertEquals(lengthGeneratorA.draw(), characteristicsA.getLength(), "lengthA");
        assertEquals(lengthGeneratorB.draw(), characteristicsB.getLength(), "lengthB");
        assertEquals(widthGeneratorA.draw(), characteristicsA.getWidth(), "widthA");
        assertEquals(widthGeneratorB.draw(), characteristicsB.getWidth(), "widthB");
        assertEquals(maximumSpeedGeneratorA.draw(), characteristicsA.getMaximumSpeed(), "maximumSpeedA");
        assertEquals(maximumSpeedGeneratorB.draw(), characteristicsB.getMaximumSpeed(), "maximumSpeedB");
        // Ensure that toString returns non null
        assertNotNull(templateA.toString(), "toString should not return null");
        // Test that the constructor throws the expected Exception when an argument is invalid
        try
        {
            new GtuTemplate(null, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new GtuTemplate(gtuTypeA, null, widthGeneratorA, maximumSpeedGeneratorA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new GtuTemplate(gtuTypeA, lengthGeneratorA, null, maximumSpeedGeneratorA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new GtuTemplate(gtuTypeA, lengthGeneratorA, widthGeneratorA, null);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
    }

    /** */
    private OtsSimulatorInterface simulator;

    @Override
    public final void constructModel() throws SimRuntimeException
    {
        //
    }

    @Override
    public final Network getNetwork()
    {
        return null;
    }

    @Override
    public InputParameterMap getInputParameterMap()
    {
        return null;
    }

    @Override
    public List getOutputStatistics()
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
}
