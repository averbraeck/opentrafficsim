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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        String typeNameA = "type name A";
        String typeNameB = "type name B";
        IdGenerator idGeneratorA = new IdGenerator("A");
        IdGenerator idGeneratorB = new IdGenerator("B");
        Generator<Length.Rel> lengthGeneratorA = new Generator<Length.Rel>()
        {

            @Override
            public Length.Rel draw() throws ProbabilityException
            {
                return new Length.Rel(123, LengthUnit.DECIMETER);
            }
        };
        Generator<Length.Rel> lengthGeneratorB = new Generator<Length.Rel>()
        {

            @Override
            public Length.Rel draw() throws ProbabilityException
            {
                return new Length.Rel(234, LengthUnit.DECIMETER);
            }
        };
        Generator<Length.Rel> widthGeneratorA = new Generator<Length.Rel>()
        {

            @Override
            public Length.Rel draw() throws ProbabilityException
            {
                return new Length.Rel(123, LengthUnit.CENTIMETER);
            }
        };
        Generator<Length.Rel> widthGeneratorB = new Generator<Length.Rel>()
        {

            @Override
            public Length.Rel draw() throws ProbabilityException
            {
                return new Length.Rel(139, LengthUnit.CENTIMETER);
            }
        };
        Generator<Speed> maximumVelocityGeneratorA = new Generator<Speed>()
        {

            @Override
            public Speed draw() throws ProbabilityException
            {
                return new Speed(50, SpeedUnit.KM_PER_HOUR);
            }
        };
        Generator<Speed> maximumVelocityGeneratorB = new Generator<Speed>()
        {

            @Override
            public Speed draw() throws ProbabilityException
            {
                return new Speed(70, SpeedUnit.KM_PER_HOUR);
            }
        };
        OTSDEVSSimulatorInterface simulatorA =
            new SimpleSimulator(new Time.Abs(0, TimeUnit.SI), new Time.Rel(0, TimeUnit.SI), new Time.Rel(1000,
                TimeUnit.SECOND), this);
        OTSDEVSSimulatorInterface simulatorB =
            new SimpleSimulator(new Time.Abs(0, TimeUnit.SI), new Time.Rel(0, TimeUnit.SI), new Time.Rel(1000,
                TimeUnit.SECOND), this);
        OTSNetwork networkA = new OTSNetwork("testGTUCharacteristics A");
        OTSNetwork networkB = new OTSNetwork("testGTUCharacteristics B");
        TemplateGTUType templateA =
            new TemplateGTUType(typeNameA, idGeneratorA, lengthGeneratorA, widthGeneratorA, maximumVelocityGeneratorA,
                simulatorA, networkA);
        TemplateGTUType templateB =
            new TemplateGTUType(typeNameB, idGeneratorB, lengthGeneratorB, widthGeneratorB, maximumVelocityGeneratorB,
                simulatorB, networkB);
        assertEquals("typenameA", typeNameA, templateA.getGTUType().getId());
        assertEquals("typenameB", typeNameB, templateB.getGTUType().getId());
        GTUCharacteristics characteristicsA = templateA.draw();
        GTUCharacteristics characteristicsB = templateB.draw();
        assertEquals("typenameA", typeNameA, characteristicsA.getGTUType().getId());
        assertEquals("typenameB", typeNameB, characteristicsB.getGTUType().getId());
        assertEquals("idGeneratorA", idGeneratorA, templateA.getIdGenerator());
        assertEquals("idGeneratorB", idGeneratorB, templateB.getIdGenerator());
        assertEquals("lengthA", lengthGeneratorA.draw(), characteristicsA.getLength());
        assertEquals("lengthB", lengthGeneratorB.draw(), characteristicsB.getLength());
        assertEquals("widthA", widthGeneratorA.draw(), characteristicsA.getWidth());
        assertEquals("widthB", widthGeneratorB.draw(), characteristicsB.getWidth());
        assertEquals("maximumVelocityA", maximumVelocityGeneratorA.draw(), characteristicsA.getMaximumVelocity());
        assertEquals("maximumVelocityB", maximumVelocityGeneratorB.draw(), characteristicsB.getMaximumVelocity());
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
            new TemplateGTUType(null, idGeneratorA, lengthGeneratorA, widthGeneratorA, maximumVelocityGeneratorA,
                simulatorA, networkA);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GTUException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(typeNameA, null, lengthGeneratorA, widthGeneratorA, maximumVelocityGeneratorA, simulatorA,
                networkA);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GTUException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(typeNameA, idGeneratorA, null, widthGeneratorA, maximumVelocityGeneratorA, simulatorA,
                networkA);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GTUException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(typeNameA, idGeneratorA, lengthGeneratorA, null, maximumVelocityGeneratorA, simulatorA,
                networkA);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GTUException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(typeNameA, idGeneratorA, lengthGeneratorA, widthGeneratorA, null, simulatorA, networkA);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GTUException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(typeNameA, idGeneratorA, lengthGeneratorA, widthGeneratorA, maximumVelocityGeneratorA, null,
                networkA);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GTUException gtue)
        {
            // Ignore expected exception
        }
        try
        {
            new TemplateGTUType(typeNameA, idGeneratorA, lengthGeneratorA, widthGeneratorA, maximumVelocityGeneratorA,
                simulatorA, null);
            fail("Previous statement should have thrown a GTUException");
        }
        catch (GTUException gtue)
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
