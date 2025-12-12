package org.opentrafficsim.demo.mirova.scenariomanagement;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.animation.DefaultAnimationFactory;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData.GtuMarker;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.swing.gui.OtsSwingApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Base class for programmatic, non-CLI OTS simulations.
 *
 * This class removes all command-line bindings from AbstractSimulationScript
 * and provides a clean configurable API for:
 *
 *   - batch scenario execution
 *   - multiple runs with different seeds
 *   - automated output handling
 *   - headless or GUI simulation
 *
 */
public abstract class AbstractSimulationScriptBase implements EventListener {

    // ----------------------------------------------------------------------
    // Core configuration fields (settable by API)
    // ----------------------------------------------------------------------

    private long seed = 1L;
    private Time startTime = Time.instantiateSI(0.0);
    private Duration warmupTime = Duration.instantiateSI(0.0);
    private Duration simulationTime = Duration.instantiateSI(600.0);
    private Duration historyTime = Duration.instantiateSI(0.0);
    private boolean autorun = false;          // headless execution
    private boolean enableGui = true;         // disable for parallel batch mode

    private List<GtuColorer> gtuColorers = OtsSwingApplication.DEFAULT_GTU_COLORERS;

    // ----------------------------------------------------------------------
    // Runtime fields
    // ----------------------------------------------------------------------

    private OtsSimulatorInterface simulator;
    private RoadNetwork network;

    private final String name;
    private final String description;

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    protected AbstractSimulationScriptBase(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    // ----------------------------------------------------------------------
    // Configuration API (no CLI!)
    // ----------------------------------------------------------------------

    public void setSeed(final long seed) {
        this.seed = seed;
    }

    public void setStartTime(final Time time) {
        this.startTime = time;
    }

    public void setWarmupTime(final Duration warmupTime) {
        this.warmupTime = warmupTime;
    }

    public void setSimulationTime(final Duration simTime) {
        this.simulationTime = simTime;
    }

    public void setHistoryTime(final Duration hist) {
        this.historyTime = hist;
    }

    public void setAutorun(final boolean autorun) {
        this.autorun = autorun;
    }

    public void setGuiEnabled(final boolean enableGui) {
        this.enableGui = enableGui;
    }

    public void setGtuColorers(final List<GtuColorer> colorers) {
        this.gtuColorers = colorers;
    }

    // ----------------------------------------------------------------------
    // Starting the simulation
    // ----------------------------------------------------------------------

    public final void start() throws Exception {
        validate();

        if (!this.enableGui) {
            runHeadless();
        } else {
            runWithGui();
        }
    }

    private void validate() {
        Throw.when(this.seed < 0, IllegalArgumentException.class, "Seed must be >= zero");
        Throw.when(this.warmupTime.si < 0.0, IllegalArgumentException.class, "Warmup must be >= 0");
        Throw.when(this.simulationTime.si < this.warmupTime.si, IllegalArgumentException.class,
            "Simulation time must be >= warmup time");
    }

    // ----------------------------------------------------------------------
    // Headless / batch mode
    // ----------------------------------------------------------------------

    private void runHeadless() throws Exception {
        this.simulator = new OtsSimulator(this.name);
        ScriptModel model = new ScriptModel(this.simulator);
        Integer replicationId = (int) Thread.currentThread().getId();
        this.simulator.initialize(
                this.startTime,
                this.warmupTime,
                this.simulationTime,
                model,
            new HistoryManagerDevs(this.simulator, this.historyTime, Duration.instantiateSI(10.0)),
            replicationId);

        this.simulator.addListener(this, Replication.END_REPLICATION_EVENT);


        double simEnd = this.simulationTime.si;
        double nextReport = 0.0;
        double reportIntervalSimTime = 10.0;  // alle 10 Sekunden Simulation
        int lastPercent = -1;

        while (this.simulator.getSimulatorAbsTime().si < this.simulationTime.si) {
            this.simulator.step();
            double t = this.simulator.getSimulatorAbsTime().si;

            // --- Zeitbasiertes Reporting (alle X Sekunden Simulationszeit) ---
            if (t >= nextReport) {
                int percent = (int) Math.round((t / simEnd) * 100.0);

                // nur ausgeben wenn Prozent fortschreiten
                if (percent != lastPercent) {
                    String bar = progressBar(percent, 30);
                    System.out.printf("[SIM %s] %s %3d%%  t=%.0f/%.0f s%n",
                        this.name, bar, percent, t, simEnd);
                    lastPercent = percent;
                }
                nextReport += reportIntervalSimTime;
            }
        }

        onSimulationEnd();
    }

    // ----------------------------------------------------------------------
    // GUI mode
    // ----------------------------------------------------------------------

    private void runWithGui() throws Exception {
        this.simulator = new OtsAnimator(this.name);
        ScriptModel model = new ScriptModel(this.simulator);
        this.simulator.initialize(this.startTime, this.warmupTime, this.simulationTime, model,
            new HistoryManagerDevs(this.simulator, this.historyTime, Duration.instantiateSI(10.0)));

        final OtsAnimationPanel animationPanel =
            new OtsAnimationPanel(model.getNetwork().getExtent(), new Dimension(900, 600),
                (OtsAnimator) this.simulator, model, this.gtuColorers, model.getNetwork());

        setupDemo(animationPanel, model.getNetwork());
        setAnimationToggles(animationPanel);

        OtsSimulationApplication<ScriptModel> app =
            new OtsSimulationApplication<>(model, animationPanel, getGtuMarkers()) {
                private static final long serialVersionUID = 1L;
                @Override protected void setAnimationToggles() { }
            };

        app.setExitOnClose(true);
        animationPanel.enableSimulationControlButtons();
    }

    // ----------------------------------------------------------------------
    // Listener for end-of-run
    // ----------------------------------------------------------------------

    @Override
    public void notify(final Event event) throws RemoteException {
        if (event.getType().equals(Replication.END_REPLICATION_EVENT)) {
            onSimulationEnd();
            this.simulator.removeListener(this, Replication.END_REPLICATION_EVENT);
        }
    }

    // ----------------------------------------------------------------------
    // Abstract and overridable methods
    // ----------------------------------------------------------------------

    protected abstract RoadNetwork setupSimulation(OtsSimulatorInterface sim) throws Exception;

    protected void onSimulationEnd() { }

    protected void setupDemo(final OtsAnimationPanel panel, final RoadNetwork net) { }

    protected void setAnimationToggles(final OtsAnimationPanel panel) {
        AnimationToggles.setIconAnimationTogglesStandard(panel);
    }

    protected Map<GtuType, GtuMarker> getGtuMarkers() {
        return Collections.emptyMap();
    }

    private static String progressBar(final int percent, final int width) {
        int filled = (percent * width) / 100;
        char[] bar = new char[width];
        Arrays.fill(bar, 0, filled, '#');
        Arrays.fill(bar, filled, width, '-');
        return "[" + new String(bar) + "]";
    }


    // ----------------------------------------------------------------------
    // Model wrapper
    // ----------------------------------------------------------------------

    private class ScriptModel extends AbstractOtsModel {

        private static final long serialVersionUID = 1L;

        ScriptModel(final OtsSimulatorInterface sim) {
            super(sim);
            AbstractSimulationScriptBase.this.simulator = sim;
        }

        @Override
        public void constructModel() throws SimRuntimeException {
            Map<String, StreamInterface> streams = new LinkedHashMap<>();
            streams.put("generation", new MersenneTwister(AbstractSimulationScriptBase.this.seed));
            streams.put("default", new MersenneTwister(AbstractSimulationScriptBase.this.seed + 1));
            this.simulator.getModel().getStreams().putAll(streams);

            AbstractSimulationScriptBase.this.network = Try.assign(
                () -> setupSimulation(this.simulator),
                RuntimeException.class,
                "Exception during setupSimulation()"
            );

            try {
                this.simulator.addListener(AbstractSimulationScriptBase.this, Replication.END_REPLICATION_EVENT);
            } catch (RemoteException e) {
                throw new SimRuntimeException(e);
            }
        }

        @Override
        public RoadNetwork getNetwork() {
            return AbstractSimulationScriptBase.this.network;
        }
    }
}
