package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventTypeInterface;
import org.djutils.event.ref.ReferenceType;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.perception.PerceivableContext;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.outputstatistics.OutputStatistic;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Test the GTUDumper class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 11 2020 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUDumperTest implements OTSModelInterface
{
    /** ... */
    private static final long serialVersionUID = 1L;

    /** Temporary directory that should be deleted by Junit at end of test. */
    @Rule
    public TemporaryFolder testDir = new TemporaryFolder();

    /** Director where GTUDumper will create files. */
    private File containerDir;

    /** The simulator. */
    private OTSSimulatorInterface simulator;

    /** The GTUDumper. */
    private GTUDumper gtuDumper;

    /** The network. */
    private OTSNetwork network;

    /**
     * Test the GTUDumper class.
     * @throws NamingException when that happens uncaught; this test has failed
     * @throws SimRuntimeException when that happens uncaught; this test has failed
     * @throws InterruptedException when that happens uncaught; this test has failed
     * @throws IOException when that happens uncaught; this test has failed
     */
    @Test
    public void testGTUDumper() throws SimRuntimeException, NamingException, InterruptedException, IOException
    {
        // System.out.println("testdir is " + this.testDir.getRoot());
        this.containerDir = this.testDir.newFolder("subfolder");
        // System.out.println("containerDir is " + this.containerDir);
        this.network = new OTSNetwork("Network for testing GTUDumper class", true, simulator);
        this.simulator = new OTSSimulator("Simulator for testing GTUDumper class");
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), this);
        simulator.scheduleEventAbs(new Time(40, TimeUnit.BASE_SECOND), this, this, "createGTU", new Object[] {});
        this.simulator.start();
        while (this.simulator.isRunning())
        {
            // System.out.println("Simulator time is " + this.simulator.getSimTime());
            Thread.sleep(100);
        }
        // System.out.println("Simulator has stopped");
        // System.out.println("Simulator time is " + this.simulator.getSimTime());

        File[] listOfFiles = this.containerDir.listFiles();
        for (File file : listOfFiles)
        {
            // System.out.println("file " + file);
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.ISO_8859_1);
            for (String line : lines)
            {
                // System.out.println(" " + line);
                assertTrue("lines says something about our test GTU", line.startsWith("test GTU "));
            }
        }

        assertTrue("toString method returns something descriptive", this.gtuDumper.toString().startsWith("GTUDumper"));
    }

    /**
     * Create one GTU with a really simple path of movement.
     */
    public void createGTU()
    {
        GTU gtu = new GTU()
        {

            /** ... */
            private static final long serialVersionUID = 1L;

            @Override
            public DirectedPoint getLocation() throws RemoteException
            {
                // This GTU travels a circle around 100, 20, elevation 10, radius 20, angular velocity 0.1 radial / second
                double timeSI = simulator.getSimulatorTime().si;
                double angle = timeSI / 10;
                return new DirectedPoint(100 + 20 * Math.cos(angle), 20 + 20 * Math.sin(angle), 10, 0, 0, angle + Math.PI / 2);
            }

            @Override
            public Bounds getBounds() throws RemoteException
            {
                return null;
            }

            @Override
            public Serializable getSourceId() throws RemoteException
            {
                return null;
            }

            @Override
            public boolean addListener(final EventListenerInterface listener, final EventTypeInterface eventType)
                    throws RemoteException
            {
                return false;
            }

            @Override
            public boolean addListener(final EventListenerInterface listener, final EventTypeInterface eventType,
                    final ReferenceType referenceType) throws RemoteException
            {
                return false;
            }

            @Override
            public boolean addListener(final EventListenerInterface listener, final EventTypeInterface eventType,
                    final int position) throws RemoteException
            {
                return false;
            }

            @Override
            public boolean addListener(final EventListenerInterface listener, final EventTypeInterface eventType,
                    final int position, final ReferenceType referenceType) throws RemoteException
            {
                return false;
            }

            @Override
            public boolean removeListener(final EventListenerInterface listener, final EventTypeInterface eventType)
                    throws RemoteException
            {
                return false;
            }

            @Override
            public boolean hasListeners() throws RemoteException
            {
                return false;
            }

            @Override
            public int numberOfListeners(final EventTypeInterface eventType) throws RemoteException
            {
                return 0;
            }

            @Override
            public Set<EventTypeInterface> getEventTypesWithListeners() throws RemoteException
            {
                return null;
            }

            @Override
            public String getId()
            {
                return "GTU for GTUDumper test";
            }

            @Override
            public PerceivableContext getPerceivableContext()
            {
                return null;
            }

            @Override
            public Length getLength()
            {
                return null;
            }

            @Override
            public Length getWidth()
            {
                return null;
            }

            @Override
            public Speed getMaximumSpeed()
            {
                return null;
            }

            @Override
            public Acceleration getMaximumAcceleration()
            {
                return null;
            }

            @Override
            public Acceleration getMaximumDeceleration()
            {
                return null;
            }

            @Override
            public GTUType getGTUType()
            {
                return null;
            }

            @Override
            public OTSSimulatorInterface getSimulator()
            {
                return simulator;
            }

            @Override
            public RelativePosition getReference()
            {
                return null;
            }

            @Override
            public RelativePosition getFront()
            {
                return null;
            }

            @Override
            public RelativePosition getRear()
            {
                return null;
            }

            @Override
            public RelativePosition getCenter()
            {
                return null;
            }

            @Override
            public ImmutableSet<RelativePosition> getContourPoints()
            {
                return null;
            }

            @Override
            public ImmutableMap<TYPE, RelativePosition> getRelativePositions()
            {
                return null;
            }

            @Override
            public Speed getSpeed()
            {
                return new Speed(Math.PI * 2 * 20 / 10, SpeedUnit.METER_PER_SECOND);
            }

            @Override
            public Speed getSpeed(final Time time)
            {
                return new Speed(Math.PI * 2 * 20 / 10, SpeedUnit.METER_PER_SECOND);
            }

            @Override
            public Acceleration getAcceleration()
            {
                return Acceleration.ZERO;
            }

            @Override
            public Acceleration getAcceleration(final Time time)
            {
                return Acceleration.ZERO;
            }

            @Override
            public Length getOdometer()
            {
                return null;
            }

            @Override
            public Length getOdometer(final Time time)
            {
                return null;
            }

            @Override
            public Parameters getParameters()
            {
                return null;
            }

            @Override
            public void setParameters(final Parameters parameters)
            {
                // Do nothing
            }

            @Override
            public StrategicalPlanner getStrategicalPlanner()
            {
                return null;
            }

            @Override
            public StrategicalPlanner getStrategicalPlanner(final Time time)
            {
                return null;
            }

            @Override
            public OperationalPlan getOperationalPlan()
            {
                try
                {
                    return new OperationalPlan(this, getLocation(), simulator.getSimulatorTime(), Duration.ZERO)
                    {

                        /** ... */
                        private static final long serialVersionUID = 1L;
                    };
                }
                catch (RemoteException | OperationalPlanException e)
                {
                    e.printStackTrace();
                    throw new SimRuntimeException(e);
                }
            }

            @Override
            public OperationalPlan getOperationalPlan(final Time time)
            {
                return null;
            }

            @Override
            public void destroy()
            {
                // Do nothing
            }

            @Override
            public boolean isDestroyed()
            {
                return false;
            }

            @Override
            public void addGtu(final GTU gtu) throws GTUException
            {
                // Do nothing
            }

            @Override
            public void removeGtu(final GTU gtu)
            {
                // Do nothing
            }

            @Override
            public void setParent(final GTU gtu) throws GTUException
            {
                // Do nothing
            }

            @Override
            public GTU getParent()
            {
                return null;
            }

            @Override
            public Set<GTU> getChildren()
            {
                return null;
            }

            @Override
            public void setErrorHandler(final GTUErrorHandler errorHandler)
            {
                // Do nothing
            }

            @Override
            public String toString()
            {
                return "test GTU";
            }
        };
        this.network.addGTU(gtu);
    }

    @Override
    public final void constructModel() throws SimRuntimeException
    {
        // System.out.println("constructModel called.");
        try
        {
            this.gtuDumper = new GTUDumper(this.simulator, new Time(10, TimeUnit.BASE_SECOND),
                    new Duration(300, DurationUnit.SECOND), this.network, this.containerDir.getCanonicalPath() + "/");
        }
        catch (SimRuntimeException | IOException e)
        {
            e.printStackTrace();
            throw new SimRuntimeException(e);
        }
    }

    @Override
    public final OTSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    @Override
    public final InputParameterMap getInputParameterMap()
    {
        return null;
    }

    @Override
    public final List<OutputStatistic<?>> getOutputStatistics()
    {
        return null;
    }

    @Override
    public final OTSNetwork getNetwork()
    {
        return null;
    }

    @Override
    public final String getShortName()
    {
        return null;
    }

    @Override
    public final String getDescription()
    {
        return null;
    }

    /**
     * Test the argument checks of the GTUDumper constructor.
     * @throws NamingException when that happens uncaught; this test has failed
     * @throws SimRuntimeException when that happens uncaught; this test has failed
     * @throws IOException when that happens uncaught; this test has failed
     */
    @Test
    public void testArgumentChecks() throws SimRuntimeException, IOException, NamingException
    {
        this.containerDir = this.testDir.newFolder("subfolder");
        this.network = new OTSNetwork("Network for testing GTUDumper class", true, simulator);
        this.simulator = new OTSSimulator("Simulator for testing GTUDumper class");
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), this);
        try
        {
            new GTUDumper(null, new Time(10, TimeUnit.BASE_SECOND), new Duration(300, DurationUnit.SECOND), this.network,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null parameter should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GTUDumper(this.simulator, null, new Duration(300, DurationUnit.SECOND), this.network,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null parameter should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GTUDumper(this.simulator, new Time(10, TimeUnit.BASE_SECOND), null, this.network,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null parameter should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GTUDumper(this.simulator, new Time(10, TimeUnit.BASE_SECOND), new Duration(300, DurationUnit.SECOND), null,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null parameter should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GTUDumper(this.simulator, new Time(10, TimeUnit.BASE_SECOND), new Duration(300, DurationUnit.SECOND),
                    this.network, null);
            fail("null parameter should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GTUDumper(null, new Time(-10, TimeUnit.BASE_SECOND), new Duration(300, DurationUnit.SECOND), this.network,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null parameter should have thrown a NullPointerException");
        }
        catch (RuntimeException rte)
        {
            // Ignore expected exception
        }

        try
        {
            new GTUDumper(null, new Time(10, TimeUnit.BASE_SECOND), new Duration(-300, DurationUnit.SECOND), this.network,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null parameter should have thrown a NullPointerException");
        }
        catch (RuntimeException rte)
        {
            // Ignore expected exception
        }
        
    }

}
