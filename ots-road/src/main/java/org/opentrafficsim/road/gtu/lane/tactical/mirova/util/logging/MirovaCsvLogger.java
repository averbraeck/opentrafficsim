package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Periodically logs GTU state data to CSV during the simulation.
 */
public class MirovaCsvLogger implements Runnable {

    private final Network network;
    private final OtsSimulator simulator;
    private final BufferedWriter writer;
    private final Duration interval;

    private boolean headerWritten = false;

    public MirovaCsvLogger(final Network network, final OtsSimulator simulator,
                           final String fileName, final Duration interval) throws IOException {
        this.network = network;
        this.simulator = simulator;
        this.interval = interval;
        this.writer = new BufferedWriter(new FileWriter(fileName));
        System.out.println("[MirovaCsvLogger] Initialized with interval = " + interval + " s");
    }

    @Override
    public void run() {
        try {
            Time now = this.simulator.getSimulatorAbsTime();

            if (!this.headerWritten) {
                this.writer.write("time,gtuId,speed_m_s,accel_m_s2,desire,headway_s,isChangingLane,actionState\n");
                this.headerWritten = true;
            }

            for (Gtu gtu : this.network.getGTUs()) {
                if (gtu.getTacticalPlanner() instanceof MirovaTacticalPlanner planner) {
                    this.writer.write(String.format("%.2f,%s,%.3f,%.3f,%.3f,%.3f,%s,%s%n",
                            now.si,
                            gtu.getId(),
                            gtu.getSpeed().si,
                            gtu.getAcceleration().si,
                            planner.getDesire(),
                            planner.getCurrentRelaxedHeadway() != null ? planner.getCurrentRelaxedHeadway().si : Double.NaN,
                            planner.getLaneChange().isChangingLane(),
                            planner.getCurrentActionState() != null ? planner.getCurrentActionState().toString() : "none"
                    ));
                }
            }

            this.writer.flush();
            System.out.println("[MirovaCsvLogger] Logged data at t=" + now + " (" + this.network.getGTUs().size() + " GTUs)");

            // schedule next logging event
            Time nextTime = this.simulator.getSimulatorAbsTime().plus(this.interval);
            this.simulator.scheduleEventAbsTime(nextTime, this, "run", new Object[] {});

        } catch (Exception e) {
            System.err.println("[MirovaCsvLogger] ERROR:");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.writer.close();
            System.out.println("[MirovaCsvLogger] Closed log file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
