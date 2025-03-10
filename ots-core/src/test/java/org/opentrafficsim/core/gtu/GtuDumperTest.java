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
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.math.AngleUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.perception.HistoryManagerDevs;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.StreamInformation;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.statistics.SimulationStatistic;

/**
 * Test the GTUDumper class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class GtuDumperTest implements OtsModelInterface
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

    /** */
    private GtuDumperTest()
    {
        // do not instantiate test class
    }

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
        this.simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), this,
                HistoryManagerDevs.noHistory(this.simulator));
        this.simulator.scheduleEventAbsTime(new Time(40.0, TimeUnit.BASE_SECOND), this, "createGtu", new Object[] {});
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
    public void createGtu()
    {
        Gtu gtu = Mockito.mock(Gtu.class);
        Mockito.when(gtu.getLocation()).thenAnswer((invocationOnMock) ->
        {
            // This GTU travels a circle around 100, 20, elevation 0, radius 20, angular velocity 0.1 radial / second
            double timeSI = GtuDumperTest.this.simulator.getSimulatorTime().si;
            double angle = AngleUtil.normalizeAroundPi(timeSI / 10.0);
            return new DirectedPoint2d(100.0 + 20.0 * Math.cos(angle), 20.0 + 20.0 * Math.sin(angle), angle);
        });
        Mockito.when(gtu.getSpeed()).thenReturn(Speed.instantiateSI(2.0));
        Mockito.when(gtu.toString()).thenReturn("test GTU");
        this.network.addGTU(gtu);
    }

    @Override
    public void constructModel() throws SimRuntimeException
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
    public OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    @Override
    public InputParameterMap getInputParameterMap()
    {
        return null;
    }

    @Override
    public List<SimulationStatistic<Duration>> getOutputStatistics()
    {
        return new ArrayList<>();
    }

    @Override
    public Network getNetwork()
    {
        return null;
    }

    @Override
    public String getShortName()
    {
        return "";
    }

    @Override
    public String getDescription()
    {
        return "";
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
        this.simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(1, DurationUnit.HOUR), this,
                HistoryManagerDevs.noHistory(this.simulator));
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
