package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
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
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test the TemplateGTUType class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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
    public final void templateGTUTypeTest() throws SimRuntimeException, NamingException, GTUException, ProbabilityException,
        ParameterException
    {
        GTUType gtuTypeA = new GTUType("type name A");
        GTUType gtuTypeB = new GTUType("type name B");
        IdGenerator idGeneratorA = new IdGenerator("A");
        IdGenerator idGeneratorB = new IdGenerator("B");
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
        OTSDEVSSimulatorInterface simulatorA =
            new SimpleSimulator(new Time(0, TimeUnit.SI), new Duration(0, TimeUnit.SI), new Duration(1000,
                TimeUnit.SECOND), this);
        OTSDEVSSimulatorInterface simulatorB =
            new SimpleSimulator(new Time(0, TimeUnit.SI), new Duration(0, TimeUnit.SI), new Duration(1000,
                TimeUnit.SECOND), this);
        OTSNetwork networkA = new OTSNetwork("testGTUCharacteristics A");
        OTSNetwork networkB = new OTSNetwork("testGTUCharacteristics B");
        TemplateGTUType templateA =
            new TemplateGTUType(gtuTypeA, idGeneratorA, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA,
                simulatorA, networkA);
        TemplateGTUType templateB =
            new TemplateGTUType(gtuTypeB, idGeneratorB, lengthGeneratorB, widthGeneratorB, maximumSpeedGeneratorB,
                simulatorB, networkB);
        assertEquals("typenameA", gtuTypeA.getId(), templateA.getGTUType().getId());
        assertEquals("typenameB", gtuTypeB.getId(), templateB.getGTUType().getId());
        GTUCharacteristics characteristicsA = templateA.draw();
        GTUCharacteristics characteristicsB = templateB.draw();
        assertEquals("typenameA", gtuTypeA.getId(), characteristicsA.getGTUType().getId());
        assertEquals("typenameB", gtuTypeB.getId(), characteristicsB.getGTUType().getId());
        assertEquals("idGeneratorA", idGeneratorA, templateA.getIdGenerator());
        assertEquals("idGeneratorB", idGeneratorB, templateB.getIdGenerator());
        assertEquals("lengthA", lengthGeneratorA.draw(), characteristicsA.getLength());
        assertEquals("lengthB", lengthGeneratorB.draw(), characteristicsB.getLength());
        assertEquals("widthA", widthGeneratorA.draw(), characteristicsA.getWidth());
        assertEquals("widthB", widthGeneratorB.draw(), characteristicsB.getWidth());
        assertEquals("maximumSpeedA", maximumSpeedGeneratorA.draw(), characteristicsA.getMaximumSpeed());
        assertEquals("maximumSpeedB", maximumSpeedGeneratorB.draw(), characteristicsB.getMaximumSpeed());
        assertEquals("simulatorA", simulatorA, templateA.getSimulator());
        assertEquals("simulatorB", simulatorB, templateB.getSimulator());
        assertEquals("simulatorA", simulatorA, characteristicsA.getSimulator());
        assertEquals("simulatorB", simulatorB, characteristicsB.getSimulator());
        assertEquals("networkA", networkA, characteristicsA.getNetwork());
        assertEquals("networkB", networkB, characteristicsB.getNetwork());
        // Ensure that toString returns non null
        assertNotNull("toString should not return null", templateA.toString());
        // Test that the constructor throws the expected Exception when an argument is invalid
        try
        {
            new TemplateGTUType(null, idGeneratorA, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA,
                simulatorA, networkA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, null, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA, simulatorA,
                networkA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, idGeneratorA, null, widthGeneratorA, maximumSpeedGeneratorA, simulatorA,
                networkA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, idGeneratorA, lengthGeneratorA, null, maximumSpeedGeneratorA, simulatorA,
                networkA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, idGeneratorA, lengthGeneratorA, widthGeneratorA, null, simulatorA, networkA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, idGeneratorA, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA, null,
                networkA);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(gtuTypeA, idGeneratorA, lengthGeneratorA, widthGeneratorA, maximumSpeedGeneratorA,
                simulatorA, null);
            fail("Previous statement should have thrown a NullPointerException");
        }
        catch (NullPointerException gtue)
        {
            // Ignore expected exception
        }
    }

    /** ... */
    private SimulatorInterface<Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator;

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
        final SimulatorInterface<Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
        throws SimRuntimeException, RemoteException
    {
        this.simulator = theSimulator;
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
        throws RemoteException
    {
        return this.simulator;
    }

}
