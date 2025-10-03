package org.opentrafficsim.core.gtu;

import java.io.File;
import java.io.PrintWriter;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * GTUDUmper; create a text file with the locations, directions and speeds of all GTUs at regular intervals.
 * <p>
 * Copyright (c) 2019-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GtuDumper
{
    /**
     * Write all GTU positions in a file.
     */
    public void dump()
    {
        try
        {
            Duration now = this.simulator.getSimulatorTime();
            String fileName = String.format("%s%08.2f.txt", this.fileNamePrefix, now.si);
            PrintWriter pw = new PrintWriter(new File(fileName));
            for (Gtu gtu : this.network.getGTUs())
            {
                DirectedPoint2d dp = gtu.getLocation();
                pw.format("%s position %.3f,%.3f dir=%5.1f speed %s\n", gtu.toString(), dp.x, dp.y,
                        Math.toDegrees(dp.getDirZ()), gtu.getSpeed());
            }
            pw.close();
            this.simulator.scheduleEventRel(this.interval, () -> dump());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Interval time between dumps. */
    private final Duration interval;

    /** The network with the GTUs to dump. */
    private final Network network;

    /** Directory and first part of the file names. */
    private final String fileNamePrefix;

    /** The simulator. */
    private final OtsSimulatorInterface simulator;

    /**
     * Construct a new GTUDumper.
     * @param firstDumpTime the time of the first dump
     * @param interval the interval until each subsequent dump
     * @param network the network (that will contain the GTUs to dump)
     * @param fileNamePrefix directory and first part if the file names; the simulation time of the dump will be appended to the
     *            file name. The file type will be .txt
     * @throws SimRuntimeException when scheduling the first dump time fails
     */
    public GtuDumper(final Duration firstDumpTime, final Duration interval, final Network network, final String fileNamePrefix)
            throws SimRuntimeException
    {
        Throw.whenNull(network, "Network may not be null");
        this.simulator = network.getSimulator();
        Throw.whenNull(firstDumpTime, "firstDumpTime may not be null");
        Throw.when(firstDumpTime.lt(this.simulator.getSimulatorTime()), RuntimeException.class,
                "firstDumptTime may not be before current simulator time");
        Throw.whenNull(interval, "interval may not be null");
        Throw.when(interval.le(Duration.ZERO), RuntimeException.class, "Duration must be positive");
        Throw.whenNull(fileNamePrefix, "fileNamePrefix may not be null");
        this.interval = interval;
        this.network = network;
        this.fileNamePrefix = fileNamePrefix;
        this.simulator.scheduleEventAbs(firstDumpTime, () -> dump());
    }

    @Override
    public String toString()
    {
        return "GTUDumper [interval=" + this.interval + ", network=" + this.network + ", fileNamePrefix=" + this.fileNamePrefix
                + "]";
    }

}
