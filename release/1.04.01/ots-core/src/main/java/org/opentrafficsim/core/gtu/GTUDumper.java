package org.opentrafficsim.core.gtu;

import java.io.File;
import java.io.PrintWriter;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * GTUDUmper; create a text file with the locations, directions and speeds of all GTUs at regular intervals.
 * <p>
 * Copyright (c) 2019-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version July 5, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUDumper
{
    /**
     * Write all GTU positions in a file.
     */
    public void dump()
    {
        try
        {
            Time now = this.simulator.getSimulatorTime();
            String fileName = String.format("%s%08.2f.txt", fileNamePrefix, now.si);
            PrintWriter pw = new PrintWriter(new File(fileName));
            for (GTU gtu : this.network.getGTUs())
            {
                DirectedPoint dp = gtu.getOperationalPlan().getLocation(now);
                pw.format("%s position %.3f,%.3f dir=%5.1f speed %s\n", gtu.toString(), dp.x, dp.y,
                        Math.toDegrees(dp.getRotZ()), gtu.getSpeed());
            }
            pw.close();
            this.simulator.scheduleEventRel(this.interval, this, this, "dump", new Object[] {});
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /** Interval time between dumps. */
    private final Duration interval;
    
    /** The network with the GTUs to dump. */
    private final OTSNetwork network;
    
    /** Directory and first part of the file names. */
    private final String fileNamePrefix;
    
    /** The simulator. */
    private final OTSSimulatorInterface simulator;

    /**
     * Construct a new GTUDumper.
     * @param simulator OTSSimulatorInterface; the simulator
     * @param firstDumpTime Time; the time of the first dump
     * @param interval Duration; the interval until each subsequent dump
     * @param network OTSNetwork; the network (that will contain the GTUs to dump)
     * @param fileNamePrefix String; directory and first part if the file names; the simulation time of the dump will be
     *            appended to the file name. The file type will be .txt
     * @throws SimRuntimeException when scheduling the first dump time fails
     */
    public GTUDumper(final OTSSimulatorInterface simulator, final Time firstDumpTime, final Duration interval,
            final OTSNetwork network, final String fileNamePrefix) throws SimRuntimeException
    {
        this.simulator = simulator;
        this.interval = interval;
        this.network = network;
        this.fileNamePrefix = fileNamePrefix;
        simulator.scheduleEventAbs(firstDumpTime, this, this, "dump", new Object[] {});
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GTUDumper [interval=" + interval + ", network=" + network + ", fileNamePrefix=" + fileNamePrefix
                + ", simulator=" + simulator + "]";
    }

}
