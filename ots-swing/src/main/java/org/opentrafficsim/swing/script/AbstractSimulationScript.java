package org.opentrafficsim.swing.script;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.cli.Checkable;
import org.djutils.cli.CliException;
import org.djutils.cli.CliUtil;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationPanelDecorator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.simulators.ReplicationState;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Template for simulation script. This class allows the user to run a single visualized simulation, or to batch-run the same
 * model. Parameters can be given through the command-line using djutils-ext. Fields can be added to sub-classes using the
 * {@code @Options} and similar annotations. Default values of the properties in this abstract class can be overwritten by the
 * sub-class using {@code CliUtil.changeDefaultValue()}.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@Command(description = "Simulation script", name = "Program", mixinStandardHelpOptions = true, showDefaultValues = true)
public abstract class AbstractSimulationScript implements EventListener, Checkable
{
    /** Name. */
    private final String name;

    /** Description. */
    private final String description;

    /** Simulator. */
    private OtsSimulatorInterface simulator;

    /** Network. */
    private RoadNetwork network;

    /** Seed. */
    @Option(names = "--seed", description = "Seed", defaultValue = "1")
    private long seed;

    /** Start time. */
    @Option(names = {"-s", "--startTime"}, description = "Start time", defaultValue = "0s")
    private Time startTime;

    /** Warm-up time. */
    @Option(names = {"-w", "--warmupTime"}, description = "Warm-up time", defaultValue = "0s")
    private Duration warmupTime;

    /** Simulation time. */
    @Option(names = {"-t", "--simulationTime"}, description = "Simulation time (including warm-up time)",
            defaultValue = "3600s")
    private Duration simulationTime;

    /** Simulation time. */
    @Option(names = {"-h", "--history"}, description = "Guaranteed history time", defaultValue = "0s")
    private Duration historyTime;

    /** Auto-run. */
    @Option(names = {"-a", "--autorun"}, description = "Autorun", negatable = true, defaultValue = "false")
    private boolean autorun;

    /**
     * Constructor.
     * @param name name
     * @param description description
     */
    protected AbstractSimulationScript(final String name, final String description)
    {
        this.name = name;
        this.description = description;
        try
        {
            CliUtil.changeCommandName(this, this.name);
            CliUtil.changeCommandDescription(this, this.description);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            CliUtil.changeCommandVersion(this,
                    formatter.format(new Date(ClassUtil.classFileDescriptorForClass(this.getClass()).getLastChangedDate())));
        }
        catch (IllegalStateException | IllegalArgumentException | CliException exception)
        {
            throw new OtsRuntimeException("Exception while setting properties in @Command annotation.", exception);
        }
    }

    /**
     * Returns the seed.
     * @return seed
     */
    public long getSeed()
    {
        return this.seed;
    }

    /**
     * Returns the start time.
     * @return start time
     */
    public Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * Returns the warm-up time.
     * @return warm-up time
     */
    public Duration getWarmupTime()
    {
        return this.warmupTime;
    }

    /**
     * Returns the simulation time.
     * @return simulation time
     */
    public Duration getSimulationTime()
    {
        return this.simulationTime;
    }

    /**
     * Returns whether to auto-run.
     * @return whether to auto-run
     */
    public boolean isAutorun()
    {
        return this.autorun;
    }

    @Override
    public void check() throws Exception
    {
        Throw.when(this.seed < 0, IllegalArgumentException.class, "Seed should be positive");
        Throw.when(this.warmupTime.si < 0.0, IllegalArgumentException.class, "Warm-up time should be positive");
        Throw.when(this.simulationTime.si < 0.0, IllegalArgumentException.class, "Simulation time should be positive");
        Throw.when(this.simulationTime.si < this.warmupTime.si, IllegalArgumentException.class,
                "Simulation time should be longer than warm-up time");
    }

    /**
     * Starts the simulation.
     * @throws Exception on any exception
     */
    public void start() throws Exception
    {
        if (isAutorun())
        {
            this.simulator = new OtsSimulator(this.name);
            final ScriptModel scriptModel = new ScriptModel(this.simulator);
            this.simulator.initialize(this.startTime, this.warmupTime, this.simulationTime, scriptModel,
                    new HistoryManagerDevs(this.simulator, this.historyTime, Duration.ofSI(10.0)));
            this.simulator.addListener(this, Replication.END_REPLICATION_EVENT);
            double tReport = 60.0;
            Duration t = this.simulator.getSimulatorTime();
            while (t.si < this.simulationTime.si)
            {
                this.simulator.step();
                t = this.simulator.getSimulatorTime();
                if (t.si >= tReport)
                {
                    Logger.ots().info("Simulation time is " + t);
                    tReport += 60.0;
                }
            }
            if (!this.simulator.getReplicationState().equals(ReplicationState.ENDED))
            {
                onSimulationEnd();
            }
            System.exit(0);
        }
        else
        {
            this.simulator = new OtsAnimator(this.name);
            final ScriptModel scriptModel = new ScriptModel(this.simulator);
            this.simulator.initialize(this.startTime, this.warmupTime, this.simulationTime, scriptModel,
                    new HistoryManagerDevs(this.simulator, this.historyTime, Duration.ofSI(10.0)));
            OtsSimulationPanel animationPanel = new OtsSimulationPanel(scriptModel.getNetwork(), getDecorator());
            OtsSimulationApplication<ScriptModel> app = new OtsSimulationApplication<ScriptModel>(scriptModel, animationPanel);
            app.setExitOnClose(true);
            animationPanel.enableSimulationControlButtons();
        }
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(Replication.END_REPLICATION_EVENT))
        {
            onSimulationEnd();
        }
    }

    /**
     * Returns the simulator.
     * @return simulator
     */
    public OtsSimulatorInterface getSimulator()
    {
        return AbstractSimulationScript.this.simulator;
    }

    /**
     * Returns the network.
     * @return network
     */
    public RoadNetwork getNetwork()
    {
        return AbstractSimulationScript.this.network;
    }

    // Overridable methods

    /**
     * Method that is called when the simulation has ended. This can be used to store data.
     */
    protected void onSimulationEnd()
    {
        //
    }

    /**
     * Returns a decorator. The default implementation returns all default implementations of the decorator methods.
     * @return decorator
     */
    protected OtsSimulationPanelDecorator getDecorator()
    {
        return new OtsSimulationPanelDecorator()
        {
        };
    }

    // Abstract methods

    /**
     * Sets up the simulation based on provided properties. Properties can be obtained with {@code getProperty()}. Setting up a
     * simulation should at least create a network and some demand. Additionally this may setup traffic control, sampling, etc.
     * @param sim simulator
     * @return network
     * @throws Exception on any exception
     */
    protected abstract RoadNetwork setupSimulation(OtsSimulatorInterface sim) throws Exception;

    // Nested classes

    /**
     * Model.
     */
    private class ScriptModel extends AbstractOtsModel
    {

        /**
         * Constructor.
         * @param simulator simulator
         */
        ScriptModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
            AbstractSimulationScript.this.simulator = simulator;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            Map<String, StreamInterface> streams = new LinkedHashMap<>();
            StreamInterface stream = new MersenneTwister(getSeed());
            streams.put("generation", stream);
            stream = new MersenneTwister(getSeed() + 1);
            streams.put("default", stream);
            AbstractSimulationScript.this.simulator.getModel().getStreams().putAll(streams);
            AbstractSimulationScript.this.network =
                    Try.assign(() -> AbstractSimulationScript.this.setupSimulation(AbstractSimulationScript.this.simulator),
                            OtsRuntimeException.class, "Exception while setting up simulation.");
            AbstractSimulationScript.this.simulator.addListener(AbstractSimulationScript.this,
                    Replication.END_REPLICATION_EVENT);
        }

        @Override
        public RoadNetwork getNetwork()
        {
            return AbstractSimulationScript.this.network;
        }

    }

}
