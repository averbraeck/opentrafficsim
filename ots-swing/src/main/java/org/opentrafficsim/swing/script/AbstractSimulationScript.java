package org.opentrafficsim.swing.script;

import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * Template for simulation script.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractSimulationScript implements EventListenerInterface
{

//    /** Name. */
//    private final String name;
//
//    /** Description. */
//    private final String description;
//
//    /** The simulator. */
//    OTSSimulatorInterface simulator;
//
//    /** The network. */
//    private OTSNetwork network;
//
//    /** Properties as String value, e.g. from command line. */
//    private final Map<String, String> props = new HashMap<>();
//
//    /** GTU colorer. */
//    private GTUColorer gtuColorer = new DefaultSwitchableGTUColorer();
//
//    /**
//     * Constructor.
//     * @param name String; name
//     * @param description String; description
//     * @param properties String[]; properties as name-value pairs
//     */
//    protected AbstractSimulationScript(final String name, final String description, final String[] properties)
//    {
//        this.name = name;
//        this.description = description;
//        this.props.put("seed", "1");
//        this.props.put("startTime", "0");
//        this.props.put("warmupTime", "0");
//        this.props.put("simulationTime", "3600");
//        this.props.put("autorun", "false");
//        setDefaultProperties();
//        for (int i = 0; i < properties.length; i += 2)
//        {
//            System.out.println("Adding argument " + properties[i] + " with argument " + properties[i + 1]);
//            this.props.put(properties[i], properties[i + 1]);
//        }
//    }
//
//    /**
//     * Sets a property.
//     * @param propertyName String; property name
//     * @param propertyValue Object; property value
//     */
//    public final void setProperty(final String propertyName, final Object propertyValue)
//    {
//        this.props.put(propertyName, propertyValue.toString());
//    }
//
//    /**
//     * Returns the String value of given property.
//     * @param propertyName String; property name
//     * @return String; value of property
//     */
//    public final String getProperty(final String propertyName)
//    {
//        String p = this.props.get(propertyName);
//        Throw.when(p == null, IllegalStateException.class, "Property %s is not given.", propertyName);
//        return p;
//    }
//
//    /**
//     * Returns the double value of given property.
//     * @param propertyName String; property name
//     * @return double; value of property
//     */
//    public final double getDoubleProperty(final String propertyName)
//    {
//        return Double.parseDouble(getProperty(propertyName));
//    }
//
//    /**
//     * Returns the boolean value of given property.
//     * @param propertyName String; property name
//     * @return double; value of property
//     */
//    public final boolean getInputParameterBoolean(final String propertyName)
//    {
//        return Boolean.parseBoolean(getProperty(propertyName));
//    }
//
//    /**
//     * Returns the int value of given property.
//     * @param propertyName String; property name
//     * @return int; value of property
//     */
//    public final int getInputParameterInteger(final String propertyName)
//    {
//        return Integer.parseInt(getProperty(propertyName));
//    }
//
//    /**
//     * Returns the long value of given property.
//     * @param propertyName String; property name
//     * @return long; value of property
//     */
//    public final long getLongProperty(final String propertyName)
//    {
//        return Long.parseLong(getProperty(propertyName));
//    }
//
//    /**
//     * Returns the Duration value of given property.
//     * @param propertyName String; property name
//     * @return Duration; value of property
//     */
//    public final Duration getDurationProperty(final String propertyName)
//    {
//        return Duration.createSI(getDoubleProperty(propertyName));
//    }
//
//    /**
//     * Returns the Time value of given property.
//     * @param propertyName String; property name
//     * @return Time; value of property
//     */
//    public final Time getTimeProperty(final String propertyName)
//    {
//        return Time.createSI(getDoubleProperty(propertyName));
//    }
//
//    /**
//     * Set GTU colorer.
//     * @param colorer GTUColorer; GTU colorer
//     */
//    public final void setGtuColorer(final GTUColorer colorer)
//    {
//        this.gtuColorer = colorer;
//    }
//
//    /**
//     * Returns the GTU colorer.
//     * @return returns the GTU colorer
//     */
//    public final GTUColorer getGtuColorer()
//    {
//        return this.gtuColorer;
//    }
//
//    /**
//     * Starts the simulation.
//     */
//    public final void start()
//    {
//        Time startTime = getTimeProperty("startTime");
//        Duration warmupTime = getDurationProperty("warmupTime");
//        Duration simulationTime = getDurationProperty("simulationTime");
//        if (getInputParameterBoolean("autorun"))
//        {
//
//            ScriptSimulation scriptSimulation = this.new ScriptSimulation();
//            try
//            {
//                DEVSSimulatorInterface.TimeDoubleUnit sim =
//                        scriptSimulation.buildSimulator(startTime, warmupTime, simulationTime, new ArrayList<InputParameter<?>>());
//                sim.addListener(this, SimulatorInterface.END_REPLICATION_EVENT);
//                double tReport = 60.0;
//                Time t = sim.getSimulatorTime();
//                while (t.si < simulationTime.si)
//                {
//                    sim.step();
//                    t = sim.getSimulatorTime();
//                    if (t.si >= tReport)
//                    {
//                        System.out.println("Simulation time is " + t);
//                        tReport += 60.0;
//                    }
//                }
//                //sim.stop(); // end of simulation event
//                onSimulationEnd(); // TODO this is temporary for as long as stop() gives an exception
//            }
//            catch (Exception exception)
//            {
//                exception.printStackTrace();
//            }
//        }
//        else
//        {
//            Try.execute(() -> new ScriptAnimation().buildAnimator(startTime, warmupTime, simulationTime,
//                    new ArrayList<InputParameter<?>>(), null, true), RuntimeException.class, "Exception from properties.");
//        }
//    }
//
//    /** {@inheritDoc} */
//    @Override
//    public final void notify(final EventInterface event) throws RemoteException
//    {
//        if (event.getType().equals(SimulatorInterface.END_REPLICATION_EVENT))
//        {
//            onSimulationEnd();
//            // solve bug that event is fired twice
//            AbstractSimulationScript.this.simulator.removeListener(AbstractSimulationScript.this,
//                    SimulatorInterface.END_REPLICATION_EVENT);
//        }
//    }
//
//    /**
//     * Returns the simulator.
//     * @return OTSSimulatorInterface; simulator
//     */
//    public final OTSSimulatorInterface getSimulator()
//    {
//        return AbstractSimulationScript.this.simulator;
//    }
//
//    /**
//     * Returns the network.
//     * @return OTSNetwork; network
//     */
//    public final OTSNetwork getNetwork()
//    {
//        return AbstractSimulationScript.this.network;
//    }
//
//    // Overridable methods
//
//    /**
//     * Creates animations for nodes, links and lanes. This can be used if the network is not read from XML.
//     * @param net OTSNetwork; network
//     */
//    protected void animateNetwork(final OTSNetwork net)
//    {
//        try
//        {
//            for (Node node : net.getNodeMap().values())
//            {
//                new NodeAnimation(node, AbstractSimulationScript.this.simulator);
//            }
//            for (Link link : net.getLinkMap().values())
//            {
//                new LinkAnimation(link, AbstractSimulationScript.this.simulator, 0.5f);
//                if (link instanceof CrossSectionLink)
//                {
//                    for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
//                    {
//                        if (element instanceof Lane)
//                        {
//                            new LaneAnimation((Lane) element, AbstractSimulationScript.this.simulator, Color.GRAY.brighter(),
//                                    false);
//                        }
//                        else if (element instanceof Shoulder)
//                        {
//                            new ShoulderAnimation((Shoulder) element, AbstractSimulationScript.this.simulator, Color.DARK_GRAY);
//                        }
//                        else if (element instanceof Stripe)
//                        {
//                            Stripe stripe = (Stripe) element;
//                            TYPE type;
//                            if (stripe.isPermeable(GTUType.CAR, LateralDirectionality.LEFT))
//                            {
//                                type = stripe.isPermeable(GTUType.CAR, LateralDirectionality.RIGHT) ? TYPE.DASHED
//                                        : TYPE.LEFTONLY;
//                            }
//                            else
//                            {
//                                type = stripe.isPermeable(GTUType.CAR, LateralDirectionality.RIGHT) ? TYPE.RIGHTONLY
//                                        : TYPE.SOLID;
//                            }
//                            new StripeAnimation((Stripe) element, AbstractSimulationScript.this.simulator, type);
//                        }
//                    }
//                }
//            }
//        }
//        catch (RemoteException | NamingException | OTSGeometryException exception)
//        {
//            throw new RuntimeException("Exception while creating network animation.", exception);
//        }
//    }
//
//    /**
//     * Sets the animation toggles. May be overridden.
//     * @param animation AbstractWrappableAnimation; animation to set the toggle on
//     */
//    protected void addAnimationToggles(final AbstractWrappableAnimation animation)
//    {
//        AnimationToggles.setIconAnimationTogglesFull(animation);
//        animation.toggleAnimationClass(OTSLink.class);
//        animation.toggleAnimationClass(OTSNode.class);
//        animation.toggleAnimationClass(GTUGenerator.class);
//        animation.showAnimationClass(SpeedSign.class);
//    }
//
//    /**
//     * Adds taps to the animation. May be overridden.
//     * @param sim OTSSimulatorInterface; simulator
//     * @param animation AbstractWrappableAnimation; animation to add tabs to
//     */
//    protected void addTabs(final OTSSimulatorInterface sim, final AbstractWrappableAnimation animation)
//    {
//        //
//    }
//
//    /**
//     * Sets the default properties. Can be overridden and use method {@code setProperty()}. Default implementation does nothing.
//     */
//    protected void setDefaultProperties()
//    {
//        //
//    }
//
//    /**
//     * Method that is called when the simulation has ended. This can be used to store data.
//     */
//    protected void onSimulationEnd()
//    {
//        //
//    }
//
//    /**
//     * Method that is called when the animation has been created, to add components for a demo.
//     * @param animation AbstractWrappableAnimation; animation
//     * @param net OTSNetwork; network
//     */
//    protected void setupDemo(final AbstractWrappableAnimation animation, final OTSNetwork net)
//    {
//        //
//    }
//
//    // Abstract methods
//
//    /**
//     * Sets up the simulation based on provided properties. Properties can be obtained with {@code getProperty()}. Setting up a
//     * simulation should at least create a network and some demand. Additionally this may setup traffic control, sampling, etc.
//     * @param sim OTSSimulatorInterface; simulator
//     * @return OTSNetwork; network
//     * @throws Exception on any exception
//     */
//    protected abstract OTSNetwork setupSimulation(OTSSimulatorInterface sim) throws Exception;
//
//    // Nested classes
//
//    /**
//     * Simulation.
//     * <p>
//     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
//     * <br>
//     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
//     * <p>
//     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
//     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
//     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
//     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
//     */
//    class ScriptSimulation extends AbstractWrappableSimulation
//    {
//        /** */
//        private static final long serialVersionUID = 20180409L;
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("synthetic-access")
//        @Override
//        public String shortName()
//        {
//            return AbstractSimulationScript.this.name;
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("synthetic-access")
//        @Override
//        public String description()
//        {
//            return AbstractSimulationScript.this.description;
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        protected OTSModelInterface<OTSSimulatorInterface> makeModel() throws OTSSimulationException
//        {
//            return new ScriptModel();
//        }
//    }
//
//    /**
//     * Animated simulation.
//     * <p>
//     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
//     * <br>
//     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
//     * <p>
//     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
//     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
//     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
//     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
//     */
//    class ScriptAnimation extends AbstractWrappableAnimation
//    {
//        /** */
//        private static final long serialVersionUID = 20180409L;
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("synthetic-access")
//        @Override
//        public String shortName()
//        {
//            return AbstractSimulationScript.this.name;
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("synthetic-access")
//        @Override
//        public String description()
//        {
//            return AbstractSimulationScript.this.description;
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        protected OTSModelInterface<OTSSimulatorInterface> makeModel() throws OTSSimulationException
//        {
//            return new ScriptModel();
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        protected final void addAnimationToggles()
//        {
//            AbstractSimulationScript.this.addAnimationToggles(this);
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        protected final void addTabs(final OTSSimulatorInterface sim)
//        {
//            AbstractSimulationScript.this.addTabs(sim, this);
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("synthetic-access")
//        @Override
//        public final GTUColorer getColorer()
//        {
//            return AbstractSimulationScript.this.gtuColorer;
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        protected void setupDemo(final AbstractWrappableAnimation animation, final OTSNetwork net)
//        {
//            AbstractSimulationScript.this.setupDemo(animation, net);
//        }
//
//    }
//
//    /**
//     * Model.
//     * <p>
//     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
//     * <br>
//     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
//     * <p>
//     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
//     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
//     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
//     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
//     */
//    private class ScriptModel implements OTSModelInterface<OTSSimulatorInterface>
//    {
//        /** */
//        private static final long serialVersionUID = 20180409L;
//
//        /**
//         * @param simulator the simulator
//         */
//        ScriptModel(final OTSSimulatorInterface simulator)
//        {
//            AbstractSimulationScript.this.simulator = simulator;
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("synthetic-access")
//        @Override
//        public void constructModel() throws SimRuntimeException
//        {
//            Map<String, StreamInterface> streams = new HashMap<>();
//            long seed = getLongProperty("seed");
//            StreamInterface stream = new MersenneTwister(seed);
//            streams.put("generation", stream);
//            stream = new MersenneTwister(seed + 1);
//            streams.put("default", stream);
//            AbstractSimulationScript.this.simulator.getReplication().setStreams(streams);
//            AbstractSimulationScript.this.network =
//                    Try.assign(() -> AbstractSimulationScript.this.setupSimulation(AbstractSimulationScript.this.simulator),
//                            RuntimeException.class, "Exception while setting up simulation.");
//            try
//            {
//                AbstractSimulationScript.this.simulator.addListener(AbstractSimulationScript.this,
//                        SimulatorInterface.END_REPLICATION_EVENT);
//            }
//            catch (RemoteException exception)
//            {
//                throw new SimRuntimeException(exception);
//            }
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("synthetic-access")
//        @Override
//        public OTSSimulatorInterface getSimulator()
//        {
//            return AbstractSimulationScript.this.simulator;
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("synthetic-access")
//        @Override
//        public OTSNetwork getNetwork()
//        {
//            return AbstractSimulationScript.this.network;
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        public InputParameterMap getInputParameterMap()
//        {
//            return null;
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        public List<OutputStatistic<?>> getOutputStatistics()
//        {
//            return null;
//        }
//    }

}
