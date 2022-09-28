package org.opentrafficsim.swing.script;

import java.awt.Dimension;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.cli.Checkable;
import org.djutils.cli.CliException;
import org.djutils.cli.CliUtil;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.swing.gui.OTSSwingApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationInterface;
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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
@Command(description = "Test program for CLI", name = "Program", mixinStandardHelpOptions = true, showDefaultValues = true)
public abstract class AbstractSimulationScript implements EventListenerInterface, Checkable
{
    /** */
    private static final long serialVersionUID = 20200129L;

    /** Name. */
    private final String name;

    /** Description. */
    private final String description;

    /** The simulator. */
    private OTSSimulatorInterface simulator;

    /** The network. */
    private OTSRoadNetwork network;

    /** GTU colorer. */
    private GTUColorer gtuColorer = OTSSwingApplication.DEFAULT_COLORER;

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

    /** Autorun. */
    @Option(names = {"-a", "--autorun"}, description = "Autorun", negatable = true, defaultValue = "false")
    private boolean autorun;

    /**
     * Constructor.
     * @param name String; name
     * @param description String; description
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
     * @return long; seed
     */
    public long getSeed()
    {
        return this.seed;
    }

    /**
     * Returns the start time.
     * @return Time; start time
     */
    public Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * Returns the warm-up time.
     * @return Duration; warm-up time
     */
    public Duration getWarmupTime()
    {
        return this.warmupTime;
    }

    /**
     * Returns the simulation time.
     * @return Duration; simulation time
     */
    public Duration getSimulationTime()
    {
        return this.simulationTime;
    }

    /**
     * Returns whether to autorun.
     * @return boolean; whether to autorun
     */
    public boolean isAutorun()
    {
        return this.autorun;
    }

    /**
     * Set GTU colorer.
     * @param colorer GTUColorer; GTU colorer
     */
    public final void setGtuColorer(final GTUColorer colorer)
    {
        this.gtuColorer = colorer;
    }

    /**
     * Returns the GTU colorer.
     * @return returns the GTU colorer
     */
    public final GTUColorer getGtuColorer()
    {
        return this.gtuColorer;
    }

    /** {@inheritDoc} */
    @Override
    public void check() throws Exception
    {
        Throw.when(this.seed < 0, IllegalArgumentException.class, "Seed should be positive");
        Throw.when(this.warmupTime.si < 0.0, IllegalArgumentException.class, "Warm-up time should be positive");
        Throw.when(this.simulationTime.si < 0.0, IllegalArgumentException.class, "Simulation time should be positive");
        Throw.when(this.simulationTime.si < this.warmupTime.si, IllegalArgumentException.class,
                "Simulation time should be longer than warmp-up time");
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
            this.simulator = new OTSSimulator(this.name);
            final ScriptModel scriptModel = new ScriptModel(this.simulator);
            this.simulator.initialize(this.startTime, this.warmupTime, this.simulationTime, scriptModel);
            this.simulator.addListener(this, ReplicationInterface.END_REPLICATION_EVENT);
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
            this.simulator = new OTSAnimator(this.name);
            final ScriptModel scriptModel = new ScriptModel(this.simulator);
            this.simulator.initialize(this.startTime, this.warmupTime, this.simulationTime, scriptModel);
            OTSAnimationPanel animationPanel =
                    new OTSAnimationPanel(scriptModel.getNetwork().getExtent(), new Dimension(800, 600),
                            (OTSAnimator) this.simulator, scriptModel, getGtuColorer(), scriptModel.getNetwork());
            setAnimationToggles(animationPanel);
            animateNetwork(scriptModel.getNetwork());
            setupDemo(animationPanel, scriptModel.getNetwork());
            OTSSimulationApplication<ScriptModel> app = new OTSSimulationApplication<ScriptModel>(scriptModel, animationPanel)
            {
                /** */
                private static final long serialVersionUID = 20190130L;

                /** {@inheritDoc} */
                @Override
                protected void animateNetwork() throws OTSDrawingException
                {
                    // override with nothing to prevent double toggles
                }

                /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(ReplicationInterface.END_REPLICATION_EVENT))
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
                    ReplicationInterface.END_REPLICATION_EVENT);
        }
    }

    /**
     * Returns the simulator.
     * @return OTSSimulatorInterface; simulator
     */
    public final OTSSimulatorInterface getSimulator()
    {
        return AbstractSimulationScript.this.simulator;
    }

    /**
     * Returns the network.
     * @return OTSNetwork; network
     */
    public final OTSRoadNetwork getNetwork()
    {
        return AbstractSimulationScript.this.network;
    }

    // Overridable methods

    /**
     * Creates animations for nodes, links and lanes. This can be used if the network is not read from XML.
     * @param net OTSNetwork; network
     */
    protected void animateNetwork(final OTSNetwork net)
    {
        try
        {
            DefaultAnimationFactory.animateNetwork(net, net.getSimulator(), getGtuColorer());
        }
        catch (OTSDrawingException exception)
        {
            throw new RuntimeException("Exception while creating network animation.", exception);
        }
    }

    /**
     * Adds tabs to the animation. May be overridden.
     * @param sim OTSSimulatorInterface; simulator
     * @param animation OTSSimulationApplication&lt;?&gt;; animation to add tabs to
     */
    protected void addTabs(final OTSSimulatorInterface sim, final OTSSimulationApplication<?> animation)
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
     * @param animationPanel OTSAnimationPanel; animation panel
     * @param net OTSNetwork; network
     */
    protected void setupDemo(final OTSAnimationPanel animationPanel, final OTSRoadNetwork net)
    {
        //
    }

    /**
     * Sets the animation toggles. May be overridden.
     * @param animation OTSAnimationPanel; animation to set the toggle on
     */
    protected void setAnimationToggles(final OTSAnimationPanel animation)
    {
        AnimationToggles.setIconAnimationTogglesStandard(animation);
    }

    // Abstract methods

    /**
     * Sets up the simulation based on provided properties. Properties can be obtained with {@code getProperty()}. Setting up a
     * simulation should at least create a network and some demand. Additionally this may setup traffic control, sampling, etc.
     * @param sim OTSSimulatorInterface; simulator
     * @return OTSNetwork; network
     * @throws Exception on any exception
     */
    protected abstract OTSRoadNetwork setupSimulation(OTSSimulatorInterface sim) throws Exception;

    // Nested classes

    /**
     * Model.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class ScriptModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20180409L;

        /**
         * @param simulator OTSSimulatorInterface; the simulator
         */
        @SuppressWarnings("synthetic-access")
        ScriptModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
            AbstractSimulationScript.this.simulator = simulator;
        }

        /** {@inheritDoc} */
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
                        ReplicationInterface.END_REPLICATION_EVENT);
            }
            catch (RemoteException exception)
            {
                throw new SimRuntimeException(exception);
            }
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public OTSRoadNetwork getNetwork()
        {
            return AbstractSimulationScript.this.network;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return getShortName();
        }

    }

}
