package org.opentrafficsim.swing.script;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
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
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.outputstatistics.OutputStatistic;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Template for simulation script.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractSimulationScript implements EventListenerInterface
{
    /** Name. */
    final String name;

    /** Description. */
    final String description;

    /** The simulator. */
    OTSSimulatorInterface simulator;

    /** The network. */
    private OTSRoadNetwork network;

    /** Properties as String value, e.g. from command line. */
    private final Map<String, String> props = new LinkedHashMap<>();

    /** GTU colorer. */
    private GTUColorer gtuColorer = OTSSwingApplication.DEFAULT_COLORER;

    /**
     * Constructor.
     * @param name String; name
     * @param description String; description
     * @param properties String[]; properties as name-value pairs
     */
    protected AbstractSimulationScript(final String name, final String description, final String[] properties)
    {
        this.name = name;
        this.description = description;
        this.props.put("seed", "1");
        this.props.put("startTime", "0");
        this.props.put("warmupTime", "0");
        this.props.put("simulationTime", "3600");
        this.props.put("autorun", "false");
        setDefaultProperties();
        for (int i = 0; i < properties.length; i += 2)
        {
            System.out.println("Adding " + properties[i] + " with argument " + properties[i + 1]);
            this.props.put(properties[i], properties[i + 1]);
        }
    }

    /**
     * Sets a property.
     * @param propertyName String; property name
     * @param propertyValue Object; property value
     */
    public final void setProperty(final String propertyName, final Object propertyValue)
    {
        this.props.put(propertyName, propertyValue.toString());
    }

    /**
     * Returns whether the property is present.
     * @param propertyName String; property name
     * @return boolean; whether the property is present
     */
    public final boolean hasProperty(final String propertyName)
    {
        return this.props.containsKey(propertyName);
    }
    
    /**
     * Returns the String value of given property.
     * @param propertyName String; property name
     * @return String; value of property
     */
    public final String getProperty(final String propertyName)
    {
        String p = this.props.get(propertyName);
        Throw.when(p == null, IllegalStateException.class, "Property %s is not given.", propertyName);
        return p;
    }

    /**
     * Returns the double value of given property.
     * @param propertyName String; property name
     * @return double; value of property
     */
    public final double getDoubleProperty(final String propertyName)
    {
        return Double.parseDouble(getProperty(propertyName));
    }

    /**
     * Returns the boolean value of given property.
     * @param propertyName String; property name
     * @return double; value of property
     */
    public final boolean getBooleanProperty(final String propertyName)
    {
        return Boolean.parseBoolean(getProperty(propertyName));
    }

    /**
     * Returns the int value of given property.
     * @param propertyName String; property name
     * @return int; value of property
     */
    public final int getIntegerProperty(final String propertyName)
    {
        return Integer.parseInt(getProperty(propertyName));
    }

    /**
     * Returns the long value of given property.
     * @param propertyName String; property name
     * @return long; value of property
     */
    public final long getLongProperty(final String propertyName)
    {
        return Long.parseLong(getProperty(propertyName));
    }

    /**
     * Returns the Duration value of given property.
     * @param propertyName String; property name
     * @return Duration; value of property
     */
    public final Duration getDurationProperty(final String propertyName)
    {
        return Duration.createSI(getDoubleProperty(propertyName));
    }

    /**
     * Returns the Time value of given property.
     * @param propertyName String; property name
     * @return Time; value of property
     */
    public final Time getTimeProperty(final String propertyName)
    {
        return Time.createSI(getDoubleProperty(propertyName));
    }
    
    /**
     * Returns the Length value of given property.
     * @param propertyName String; property name
     * @return Length; value of property
     */
    public final Length getLengthProperty(final String propertyName)
    {
        return Length.createSI(getDoubleProperty(propertyName));
    }
    
    /**
     * Returns the Acceleration value of given property.
     * @param propertyName String; property name
     * @return Acceleration; value of property
     */
    public final Acceleration getAccelerationProperty(final String propertyName)
    {
        return Acceleration.createSI(getDoubleProperty(propertyName));
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

    /**
     * Starts the simulation.
     * @throws Exception on any exception
     */
    public final void start() throws Exception
    {
        Time startTime = getTimeProperty("startTime");
        Duration warmupTime = getDurationProperty("warmupTime");
        Duration simulationTime = getDurationProperty("simulationTime");
        if (getBooleanProperty("autorun"))
        {
            this.simulator = new OTSSimulator();
            final ScriptModel scriptModel = new ScriptModel(this.simulator);
            this.simulator.initialize(startTime, warmupTime, simulationTime, scriptModel);
            this.simulator.addListener(this, SimulatorInterface.END_REPLICATION_EVENT);
            double tReport = 60.0;
            Time t = this.simulator.getSimulatorTime();
            while (t.si < simulationTime.si)
            {
                this.simulator.step();
                t = this.simulator.getSimulatorTime();
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
            this.simulator = new OTSAnimator();
            final ScriptModel scriptModel = new ScriptModel(this.simulator);
            this.simulator.initialize(startTime, warmupTime, simulationTime, scriptModel);
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
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(SimulatorInterface.END_REPLICATION_EVENT))
        {
//            try
//            {
//                getSimulator().scheduleEventNow(this, this, "onSimulationEnd", null);
//            }
//            catch (SimRuntimeException exception)
//            {
//                throw new RuntimeException(exception);
//            }
            onSimulationEnd();
            // solve bug that event is fired twice
            AbstractSimulationScript.this.simulator.removeListener(AbstractSimulationScript.this,
                    SimulatorInterface.END_REPLICATION_EVENT);
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
            DefaultAnimationFactory.animateNetwork(net, getSimulator(), getGtuColorer());
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
     * Sets the default properties. Can be overridden and use method {@code setProperty()}. Default implementation does nothing.
     */
    protected void setDefaultProperties()
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
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class ScriptModel implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20180409L;

        /**
         * @param simulator OTSSimulatorInterface; the simulator
         */
        ScriptModel(final OTSSimulatorInterface simulator)
        {
            AbstractSimulationScript.this.simulator = simulator;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public void constructModel() throws SimRuntimeException
        {
            Map<String, StreamInterface> streams = new LinkedHashMap<>();
            long seed = getLongProperty("seed");
            StreamInterface stream = new MersenneTwister(seed);
            streams.put("generation", stream);
            stream = new MersenneTwister(seed + 1);
            streams.put("default", stream);
            AbstractSimulationScript.this.simulator.getReplication().setStreams(streams);
            AbstractSimulationScript.this.network =
                    Try.assign(() -> AbstractSimulationScript.this.setupSimulation(AbstractSimulationScript.this.simulator),
                            RuntimeException.class, "Exception while setting up simulation.");
            try
            {
                AbstractSimulationScript.this.simulator.addListener(AbstractSimulationScript.this,
                        SimulatorInterface.END_REPLICATION_EVENT);
            }
            catch (RemoteException exception)
            {
                throw new SimRuntimeException(exception);
            }
        }

        /** {@inheritDoc} */
        @Override
        public OTSSimulatorInterface getSimulator()
        {
            return AbstractSimulationScript.this.simulator;
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
        public InputParameterMap getInputParameterMap()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public List<OutputStatistic<?>> getOutputStatistics()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public String getShortName()
        {
            return AbstractSimulationScript.this.name;
        }

        /** {@inheritDoc} */
        @Override
        public String getDescription()
        {
            return AbstractSimulationScript.this.description;
        }
    }

}
