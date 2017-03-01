package ahfe;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.io.URLResource;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;

/**
 * Simulation for AHFE congress.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 28, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AHFESimulation extends AbstractWrappableAnimation
{

    /** */
    private static final long serialVersionUID = 20170228L;

    /**
     * Main program.
     * @param args String[]; the command line arguments
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        boolean autorun = true;
        Integer replication = null;

        for (String arg : args)
        {
            int equalsPos = arg.indexOf("=");
            if (equalsPos >= 0)
            {
                // set something
                String key = arg.substring(0, equalsPos);
                String value = arg.substring(equalsPos + 1);
                if ("autorun".equalsIgnoreCase(key))
                {
                    if ("true".equalsIgnoreCase(value))
                    {
                        autorun = true;
                    }
                    else if ("false".equalsIgnoreCase(value))
                    {
                        autorun = false;
                    }
                    else
                    {
                        System.err.println("bad autorun value " + value + " (ignored)");
                    }
                }
                else if ("replication".equalsIgnoreCase(key))
                {
                    try
                    {
                        replication = Integer.parseInt(value);
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable replication number \"" + value + "\"");
                    }
                }
                else
                {
                    System.out.println("Ignoring unknown setting " + arg);
                }
            }
            else
            {
                // not a flag
                System.err.println("Ignoring argument " + arg);
            }
        }
        final boolean finalAutoRun = autorun;
        final Integer finalReplication = replication;
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    AHFESimulation model = new AHFESimulation();
                    if (null != finalReplication)
                    {
                        System.out.println("Setting up replication " + finalReplication);
                    }
                    model.setNextReplication(finalReplication);
                    // 1 hour simulation run for testing
                    model.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(60.0, TimeUnit.MINUTE),
                            new ArrayList<Property<?>>(), null, true);
                    if (finalAutoRun)
                    {
                        int lastReportedTime = -1;
                        int reportTimeClick = 60;
                        while (true)
                        {
                            int currentTime = (int) model.getSimulator().getSimulatorTime().getTime().si;
                            if (currentTime >= lastReportedTime + reportTimeClick)
                            {
                                lastReportedTime = currentTime / reportTimeClick * reportTimeClick;
                                System.out.println("time is " + model.getSimulator().getSimulatorTime().getTime());
                            }
                            try
                            {
                                model.getSimulator().step();
                            }
                            catch (SimRuntimeException sre)
                            {
                                System.out.println("Simulation ends; time is "
                                        + model.getSimulator().getSimulatorTime().getTime());
                                System.out.println("Not yet writing results to file(s)");
                                System.exit(0);
                                break;
                            }
                        }
                    }
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException | RemoteException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** The simulator. */
    private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "AFFE Simulation";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "Simulation for AHFE congress";
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer) throws OTSSimulationException
    {
        return new AHFEModel();
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    protected final Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-50, -100, 8050, 150);
    }

    /**
     * The AHFE simulation model.
     */
    class AHFEModel extends EventProducer implements OTSModelInterface
    {

        /** */
        private static final long serialVersionUID = 20170228L;

        /** The network. */
        private OTSNetwork network;

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
                throws SimRuntimeException, RemoteException
        {
            AHFESimulation.this.simulator = theSimulator;
            try
            {
                URL url = URLResource.getResource("/AHFE/Network.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((OTSDEVSSimulatorInterface) theSimulator);
                this.network = nlp.build(url);

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return AHFESimulation.this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }

    /**
     * Retrieve the simulator.
     * @return SimulatorInterface&lt;Time, Duration, OTSSimTimeDouble&gt;; the simulator.
     */
    public final SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()
    {
        return this.simulator;
    }

}
