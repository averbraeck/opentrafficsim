package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.Test;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
     * @throws ProbabilityException if probabilities are invalid
     * @throws ParameterException in case a parameter problem occurs
     */
    @Test
    public final void templateGtuTypeTest()
            throws SimRuntimeException, NamingException, GtuException, ProbabilityException, ParameterException
    {
        Network network = new Network("network", this.simulator);
        GtuType gtuTypeA = new GtuType("type name A", DefaultsNl.VEHICLE);
        GtuType gtuTypeB = new GtuType("type name B", DefaultsNl.VEHICLE);
        Generator<Length> lengthGeneratorA = new Generator<Length>()
        {

            @Override
            public Length draw() throws ProbabilityException
            {
                return new Length(123, LengthUnit.DECIMETER);
            }
        };
        Generator<Length> lengthGeneratorB = new Generator<Length>()
        {

            @Override
            public Length draw() throws ProbabilityException
            {
                return new Length(234, LengthUnit.DECIMETER);
            }
        };
        Generator<Length> widthGeneratorA = new Generator<Length>()
        {

            @Override
            public Length draw() throws ProbabilityException
            {
                return new Length(123, LengthUnit.CENTIMETER);
            }
        };
        Generator<Length> widthGeneratorB = new Generator<Length>()
        {

            @Override
            public Length draw() throws ProbabilityException
            {
                return new Length(139, LengthUnit.CENTIMETER);
            }
        };
        Generator<Speed> maximumSpeedGeneratorA = new Generator<Speed>()
        {

            @Override
            public Speed draw() throws ProbabilityException
            {
                return new Speed(50, SpeedUnit.KM_PER_HOUR);
            }
        };
        Generator<Speed> maximumSpeedGeneratorB = new Generator<Speed>()
        {

            @Override
            public Speed draw() throws ProbabilityException
            {
                return new Speed(70, SpeedUnit.KM_PER_HOUR);
            }
        };
        GtuTemplate templateA = new GtuTemplate(gtuTypeA, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA);
        GtuTemplate templateB = new GtuTemplate(gtuTypeB, lengthGeneratorB, widthGeneratorB, maximumSpeedGeneratorB);
        assertEquals("typenameA", gtuTypeA.getId(), templateA.getGtuType().getId());
        assertEquals("typenameB", gtuTypeB.getId(), templateB.getGtuType().getId());
        GtuCharacteristics characteristicsA = templateA.draw();
        GtuCharacteristics characteristicsB = templateB.draw();
        assertEquals("typenameA", gtuTypeA.getId(), characteristicsA.getGtuType().getId());
        assertEquals("typenameB", gtuTypeB.getId(), characteristicsB.getGtuType().getId());
        assertEquals("lengthA", lengthGeneratorA.draw(), characteristicsA.getLength());
        assertEquals("lengthB", lengthGeneratorB.draw(), characteristicsB.getLength());
        assertEquals("widthA", widthGeneratorA.draw(), characteristicsA.getWidth());
        assertEquals("widthB", widthGeneratorB.draw(), characteristicsB.getWidth());
        assertEquals("maximumSpeedA", maximumSpeedGeneratorA.draw(), characteristicsA.getMaximumSpeed());
        assertEquals("maximumSpeedB", maximumSpeedGeneratorB.draw(), characteristicsB.getMaximumSpeed());
        // Ensure that toString returns non null
        assertNotNull("toString should not return null", templateA.toString());
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

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        //
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
