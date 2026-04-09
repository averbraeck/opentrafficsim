package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Periodically logs GTU state data to a CSV file during the simulation.
 * <p>
 * This utility class captures critical tactical metrics (speed, acceleration, desire,
 * action states) at fixed intervals to facilitate offline analysis and debugging of
 * the MiRoVA tactical planner.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MirovaCsvLogger implements Runnable {

    /** The simulation network containing the GTUs. */
    private final Network network;

    /** The DSOL/OTS simulator engine used for scheduling logging events. */
    private final OtsSimulator simulator;

    /** The buffered writer used to stream data to the CSV file. */
    private final BufferedWriter writer;

    /** The fixed time interval between consecutive log entries. */
    private final Duration interval;

    /** Flag to ensure the CSV header is only written once. */
    private boolean headerWritten = false;

    /**
     * Constructs a new CSV logger for the MiRoVA tactical planner.
     *
     * @param network   the simulation network containing the vehicles
     * @param simulator the simulator engine to retrieve time and schedule events
     * @param fileName  the path and name of the target CSV file
     * @param interval  the time duration between logging cycles
     * @throws IOException if the file cannot be created or opened for writing
     */
    public MirovaCsvLogger(final Network network, final OtsSimulator simulator,
                           final String fileName, final Duration interval) throws IOException {
        this.network = network;
        this.simulator = simulator;
        this.interval = interval;
        this.writer = new BufferedWriter(new FileWriter(fileName));
        System.out.println("[MirovaCsvLogger] Initialized with interval = " + interval + " s");
    }

    /**
     * Executes the logging cycle.
     * <p>
     * Gathers state data from all GTUs governed by the {@link MirovaTacticalPlanner},
     * writes a row to the CSV file, and reschedules itself for the next interval.
     * </p>
     */
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
                    // Use Locale.US to ensure floats are formatted with dots (.) instead of commas (,)
                    // to prevent breaking the CSV structure on European systems.
                    this.writer.write(String.format(Locale.US, "%.2f,%s,%.3f,%.3f,%.3f,%.3f,%s,%s%n",
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

            // Schedule the next logging event
            Time nextTime = this.simulator.getSimulatorAbsTime().plus(this.interval);
            this.simulator.scheduleEventAbsTime(nextTime, this, "run", new Object[0]);

        } catch (Exception e) {
            // Defensive catch to prevent a logging error from crashing the entire simulation
            System.err.println("[MirovaCsvLogger] ERROR during logging cycle:");
            e.printStackTrace();
        }
    }

    /**
     * Closes the buffered writer and releases file locks.
     * <p>
     * Should be called when the simulation ends.
     * </p>
     */
    public void close() {
        try {
            this.writer.close();
            System.out.println("[MirovaCsvLogger] Closed log file.");
        } catch (IOException e) {
            System.err.println("[MirovaCsvLogger] Error closing the log file.");
            e.printStackTrace();
        }
    }
}