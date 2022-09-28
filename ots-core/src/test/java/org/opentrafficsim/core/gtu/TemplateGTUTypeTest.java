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
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 11, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TemplateGTUTypeTest implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20160311L;

    /**
     * Test the TemplateGTUType class.
     * @throws NamingException should never happen
     * @throws SimRuntimeException should never happen
     * @throws GTUException should never happen
     * @throws ProbabilityException if probabilities are invalid
     * @throws ParameterException in case a parameter problem occurs
     */
    @Test
    public final void templateGTUTypeTest()
            throws SimRuntimeException, NamingException, GTUException, ProbabilityException, ParameterException
    {
        OTSNetwork network = new OTSNetwork("network", true, this.simulator);
        GTUType gtuTypeA = new GTUType("type name A", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        GTUType gtuTypeB = new GTUType("type name B", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
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
        TemplateGTUType templateA = new TemplateGTUType(gtuTypeA, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA);
        TemplateGTUType templateB = new TemplateGTUType(gtuTypeB, lengthGeneratorB, widthGeneratorB, maximumSpeedGeneratorB);
        assertEquals("typenameA", gtuTypeA.getId(), templateA.getGTUType().getId());
        assertEquals("typenameB", gtuTypeB.getId(), templateB.getGTUType().getId());
        GTUCharacteristics characteristicsA = templateA.draw();
        GTUCharacteristics characteristicsB = templateB.draw();
        assertEquals("typenameA", gtuTypeA.getId(), characteristicsA.getGTUType().getId());
        assertEquals("typenameB", gtuTypeB.getId(), characteristicsB.getGTUType().getId());
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
            new TemplateGTUType(null, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, null, widthGeneratorA, maximumSpeedGeneratorA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, lengthGeneratorA, null, maximumSpeedGeneratorA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, lengthGeneratorA, widthGeneratorA, null);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
    }

    /** */
    private OTSSimulatorInterface simulator;

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        //
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
