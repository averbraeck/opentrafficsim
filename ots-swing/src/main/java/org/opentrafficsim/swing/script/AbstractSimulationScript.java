package org.opentrafficsim.swing.script;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
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
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Template for simulation script. This class allows the user to run a single visualized simulation, or to batch-run the same
 * model. Parameters can be given through the command-line using djutils-ext. Fields can be added to sub-classes using the
 * {@code @Options} and similar annotations. Default values of the properties in this abstract class can be overwritten by the
 * sub-class using {@code CliUtil.changeDefaultValue()}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@Command(description = "Test program for CLI", name = "Program", mixinStandardHelpOptions = true, showDefaultValues = true)
public abstract class AbstractSimulationScript implements EventListener, Checkable
{
    /** */
    private static final long serialVersionUID = 20200129L;

    /** Name. */
    private final String name;

    /** Description. */
    private final String description;

    /** The simulator. */
    private OtsSimulatorInterface simulator;

    /** The network. */
    private RoadNetwork network;

    /** GTU colorers. */
    private List<GtuColorer> gtuColorers = OtsSwingApplication.DEFAULT_GTU_COLORERS;

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

    /** Autorun. */
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
                    formatter.format(new Date(ClassUtil.classFileDescriptor(this.getClass()).getLastChangedDate())));
        }
        catch (IllegalStateException | IllegalArgumentException | CliException exception)
        {
            throw new RuntimeException("Exception while setting properties in @Command annotation.", exception);
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
     * Returns whether to autorun.
     * @return whether to autorun
     */
    public boolean isAutorun()
    {
        return this.autorun;
    }

    /**
     * Set GTU colorers.
     * @param colorers GTU colorers
     */
    public final void setGtuColorers(final List<GtuColorer> colorers)
    {
        this.gtuColorers = colorers;
    }

    /**
     * Returns the GTU colorers.
     * @return returns the GTU colorers
     */
    public final List<GtuColorer> getGtuColorers()
    {
        return this.gtuColorers;
    }

    /**
     * Returns map of (non-default) GTU type markers. The default implementation of this method returns an empty map.
     * @return map of GTU type markers
     */
    public Map<GtuType, GtuMarker> getGtuMarkers()
    {
        return Collections.emptyMap();
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
    public final void start() throws Exception
    {
        if (isAutorun())
        {
            // TODO: wait until simulation control buttons are enabled (indicating that the tabs have been added)
            this.simulator = new OtsSimulator(this.name);
            final ScriptModel scriptModel = new ScriptModel(this.simulator);
            this.simulator.initialize(this.startTime, this.warmupTime, this.simulationTime, scriptModel,
                    new HistoryManagerDevs(this.simulator, this.historyTime, Duration.instantiateSI(10.0)));
            this.simulator.addListener(this, Replication.END_REPLICATION_EVENT);
            double tReport = 60.0;
            Time t = this.simulator.getSimulatorAbsTime();
            while (t.si < this.simulationTime.si)
            {
                this.simulator.step();
                t = this.simulator.getSimulatorAbsTime();
                if (t.si >= tReport)
                {
                    System.out.println("Simulation time is " + t);
                    tReport += 60.0;
                }
            }
            // sim.stop(); // end of simulation event
            onSimulationEnd(); // TODO this is temporary for as long as stop() gives an exception
            System.exit(0);
        }
        else
        {
            this.simulator = new OtsAnimator(this.name);
            final ScriptModel scriptModel = new ScriptModel(this.simulator);
            this.simulator.initialize(this.startTime, this.warmupTime, this.simulationTime, scriptModel,
                    new HistoryManagerDevs(this.simulator, this.historyTime, Duration.instantiateSI(10.0)));
            OtsAnimationPanel animationPanel =
                    new OtsAnimationPanel(scriptModel.getNetwork().getExtent(), new Dimension(800, 600),
                            (OtsAnimator) this.simulator, scriptModel, getGtuColorers(), scriptModel.getNetwork());
            setAnimationToggles(animationPanel);
            setupDemo(animationPanel, scriptModel.getNetwork());
            OtsSimulationApplication<ScriptModel> app =
                    new OtsSimulationApplication<ScriptModel>(scriptModel, animationPanel, getGtuMarkers())
                    {
                        /** */
                        private static final long serialVersionUID = 20190130L;

                        @Override
                        protected void setAnimationToggles()
                        {
                            // override with nothing to prevent double toggles
                        }
                    };
            addTabs(this.simulator, app);
            app.setExitOnClose(true);
            animationPanel.enableSimulationControlButtons();
        }
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(Replication.END_REPLICATION_EVENT))
        {
            // try
            // {
            // getSimulator().scheduleEventNow(this, this, "onSimulationEnd", null);
            // }
            // catch (SimRuntimeException exception)
            // {
            // throw new RuntimeException(exception);
            // }
            onSimulationEnd();
            // solve bug that event is fired twice
            AbstractSimulationScript.this.simulator.removeListener(AbstractSimulationScript.this,
                    Replication.END_REPLICATION_EVENT);
        }
    }

    /**
     * Returns the simulator.
     * @return simulator
     */
    public final OtsSimulatorInterface getSimulator()
    {
        return AbstractSimulationScript.this.simulator;
    }

    /**
     * Returns the network.
     * @return network
     */
    public final RoadNetwork getNetwork()
    {
        return AbstractSimulationScript.this.network;
    }

    // Overridable methods

    /**
     * Creates animations for nodes, links and lanes. This can be used if the network is not read from XML.
     * @param net network
     * @param animationPanel animation panel
     */
    protected void animateNetwork(final Network net, final OtsAnimationPanel animationPanel)
    {
        DefaultAnimationFactory.animateNetwork(net, net.getSimulator(),
                animationPanel.getColorControlPanel().getGtuColorerManager(), getGtuMarkers());
    }

    /**
     * Adds tabs to the animation. May be overridden.
     * @param sim simulator
     * @param animation animation to add tabs to
     */
    protected void addTabs(final OtsSimulatorInterface sim, final OtsSimulationApplication<?> animation)
    {
        //
    }

    /**
     * Method that is called when the simulation has ended. This can be used to store data.
     */
    protected void onSimulationEnd()
    {
        //
    }

    /**
     * Method that is called when the animation has been created, to add components for a demo.
     * @param animationPanel animation panel
     * @param net network
     */
    protected void setupDemo(final OtsAnimationPanel animationPanel, final RoadNetwork net)
    {
        //
    }

    /**
     * Sets the animation toggles. May be overridden.
     * @param animation animation to set the toggle on
     */
    protected void setAnimationToggles(final OtsAnimationPanel animation)
    {
        AnimationToggles.setIconAnimationTogglesStandard(animation);
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
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class ScriptModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20180409L;

        /**
         * @param simulator the simulator
         */
        @SuppressWarnings("synthetic-access")
        ScriptModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
            AbstractSimulationScript.this.simulator = simulator;
        }

        @SuppressWarnings("synthetic-access")
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
                            RuntimeException.class, "Exception while setting up simulation.");
            try
            {
                AbstractSimulationScript.this.simulator.addListener(AbstractSimulationScript.this,
                        Replication.END_REPLICATION_EVENT);
            }
            catch (RemoteException exception)
            {
                throw new SimRuntimeException(exception);
            }
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public RoadNetwork getNetwork()
        {
            return AbstractSimulationScript.this.network;
        }

    }

}
