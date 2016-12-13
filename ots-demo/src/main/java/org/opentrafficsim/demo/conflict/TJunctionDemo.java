package org.opentrafficsim.demo.conflict;

import static org.opentrafficsim.road.gtu.lane.RoadGTUTypes.CAR;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
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
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TJunctionDemo extends AbstractWrappableAnimation
{

    /** */
    private static final long serialVersionUID = 20161211L;

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer) throws OTSSimulationException
    {
        return new TJunctionModel();
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "T-junction demonstration";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "T-junction demonstration";
    }

    /**
     * The simulation model.
     */
    class TJunctionModel implements OTSModelInterface
    {

        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private OTSNetwork network;

        /** Simulator. */
        private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> arg0)
                throws SimRuntimeException, RemoteException
        {
            this.simulator = arg0;
            try
            {
                URL url = URLResource.getResource("/conflict/TJunction.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((OTSDEVSSimulatorInterface) arg0);
                this.network = nlp.build(url);

                // add conflicts
                ((CrossSectionLink) this.network.getLink("ECSC")).setPriority(Priority.PRIORITY);
                ((CrossSectionLink) this.network.getLink("ECWC")).setPriority(Priority.PRIORITY);
                ((CrossSectionLink) this.network.getLink("WCSC")).setPriority(Priority.PRIORITY);
                ((CrossSectionLink) this.network.getLink("WCEC")).setPriority(Priority.PRIORITY);
                // ((CrossSectionLink) this.network.getLink("SCEC")).setPriority(Priority.STOP);
                // ((CrossSectionLink) this.network.getLink("SCWC")).setPriority(Priority.STOP);
                ConflictBuilder.buildConflicts(this.network, CAR, (OTSDEVSSimulatorInterface) this.simulator);

                // add sinks
                Length sinkSpacing = new Length(30.0, LengthUnit.SI);
                for (String linkId : this.network.getLinkMap().keySet())
                {
                    // all links toward "?C" are connectors or origin lanes
                    if (!linkId.endsWith("C"))
                    {
                        Lane l = ((CrossSectionLink) this.network.getLink(linkId)).getLanes().get(0);
                        new SinkSensor(l, l.getLength().minus(sinkSpacing), (OTSDEVSSimulatorInterface) this.simulator);
                    }
                }

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    TJunctionDemo animation = new TJunctionDemo();
                    // 1 hour simulation run for testing
                    animation.buildAnimator(new Time(0.0, TimeUnit.SECOND), new Duration(0.0, TimeUnit.SECOND),
                            new Duration(60.0, TimeUnit.MINUTE), new ArrayList<Property<?>>(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

}
