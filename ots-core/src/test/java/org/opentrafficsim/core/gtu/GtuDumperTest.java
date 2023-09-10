package org.opentrafficsim.core.gtu;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.statistics.SimulationStatistic;

/**
 * Test the GTUDumper class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuDumperTest implements OtsModelInterface
{
    /** ... */
    private static final long serialVersionUID = 1L;

    /** Temporary directory that should be deleted by JUnit at end of test. */
    @TempDir
    private Path testDir;

    /** Director where GTUDumper will create files. */
    private File containerDir;

    /** The simulator. */
    private OtsSimulatorInterface simulator;

    /** The GTUDumper. */
    private GtuDumper gtuDumper;

    /** The network. */
    private Network network;

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
        Path containerPath = Files.createDirectory(Paths.get(this.testDir.toString() + File.separator + "subfolder"));
        this.containerDir = containerPath.toFile();
        // System.out.println("containerDir is " + this.containerDir);
        this.simulator = new OtsSimulator("Simulator for testing GTUDumper class");
        this.network = new Network("Network for testing GTUDumper class", this.simulator);
        this.simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), this);
        // TODO this.simulator.scheduleEventAbsTime(new Time(40, TimeUnit.BASE_SECOND), this, "createGtuDeprecated", new
        // Object[] {});
        this.simulator.start();
        while (this.simulator.isStartingOrRunning())
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
                assertTrue(line.startsWith("test GTU "), "lines says something about our test GTU");
            }
        }

        assertTrue(this.gtuDumper.toString().startsWith("GTUDumper"), "toString method returns something descriptive");
    }

    /**
     * Create one GTU with a really simple path of movement.
     */
    public void createGtuDeprecated()
    {
        /*
         * Gtu gtu = new Gtu() {
         * @Override public DirectedPoint getLocation() { // This GTU travels a circle around 100, 20, elevation 10, radius 20,
         * angular velocity 0.1 radial / second double timeSI = GtuDumperTest.this.simulator.getSimulatorTime().si; double angle
         * = timeSI / 10; return new DirectedPoint(100 + 20 * Math.cos(angle), 20 + 20 * Math.sin(angle), 10, 0, 0, angle +
         * Math.PI / 2); } };
         */
        Gtu gtu = null;
        this.network.addGTU(gtu);
    }

    @Override
    public final void constructModel() throws SimRuntimeException
    {
        // System.out.println("constructModel called.");
        try
        {
            this.gtuDumper = new GtuDumper(new Time(10, TimeUnit.BASE_SECOND), new Duration(300, DurationUnit.SECOND),
                    this.network, this.containerDir.getCanonicalPath() + "/");
        }
        catch (SimRuntimeException | IOException e)
        {
            e.printStackTrace();
            throw new SimRuntimeException(e);
        }
    }

    @Override
    public final OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    @Override
    public final InputParameterMap getInputParameterMap()
    {
        return null;
    }

    @Override
    public final List<SimulationStatistic<Duration>> getOutputStatistics()
    {
        return new ArrayList<>();
    }

    @Override
    public final Network getNetwork()
    {
        return null;
    }

    @Override
    public final String getShortName()
    {
        return "";
    }

    @Override
    public final String getDescription()
    {
        return "";
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

    /**
     * Test the argument checks of the GTUDumper constructor.
     * @throws NamingException when that happens uncaught; this test has failed
     * @throws SimRuntimeException when that happens uncaught; this test has failed
     * @throws IOException when that happens uncaught; this test has failed
     */
    @Test
    public void testArgumentChecks() throws SimRuntimeException, IOException, NamingException
    {
        Path containerPath = Files.createDirectory(Paths.get(this.testDir.toString() + File.separator + "subfolder"));
        this.containerDir = containerPath.toFile();
        this.simulator = new OtsSimulator("Simulator for testing GTUDumper class");
        this.network = new Network("Network for testing GTUDumper class", this.simulator);
        this.simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), this);
        try
        {
            new GtuDumper(null, new Duration(300, DurationUnit.SECOND), this.network,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null firstDumpTime should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GtuDumper(new Time(10, TimeUnit.BASE_SECOND), null, this.network, this.containerDir.getCanonicalPath() + "/");
            fail("null interval should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GtuDumper(new Time(10, TimeUnit.BASE_SECOND), new Duration(300, DurationUnit.SECOND), null,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null network should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GtuDumper(new Time(10, TimeUnit.BASE_SECOND), new Duration(300, DurationUnit.SECOND), this.network, null);
            fail("null fileNamePrefix should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            new GtuDumper(new Time(-10, TimeUnit.BASE_SECOND), new Duration(300, DurationUnit.SECOND), this.network,
                    this.containerDir.getCanonicalPath() + "/");
            fail("null parameter should have thrown a NullPointerException");
        }
        catch (RuntimeException rte)
        {
            // Ignore expected exception
        }

        try
        {
            new GtuDumper(new Time(10, TimeUnit.BASE_SECOND), new Duration(-300, DurationUnit.SECOND), this.network,
                    this.containerDir.getCanonicalPath() + "/");
            fail("firstDumpTime before current simulator time should have thrown a RuntimeException");
        }
        catch (RuntimeException rte)
        {
            // Ignore expected exception
        }

    }

}
