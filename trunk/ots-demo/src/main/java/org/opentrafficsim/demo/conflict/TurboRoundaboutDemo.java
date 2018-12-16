package org.opentrafficsim.demo.conflict;

import static org.opentrafficsim.core.gtu.GTUType.VEHICLE;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.animation.TrafficLightAnimation;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.swing.gui.AnimationToggles;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TurboRoundaboutDemo extends AbstractWrappableAnimation
{

    /** */
    private static final long serialVersionUID = 20161211L;

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel() throws OTSSimulationException
    {
        return new TurboRoundaboutModel();
    }

    /** {@inheritDoc} */
    @Override
    protected final void addAnimationToggles()
    {
        AnimationToggles.setIconAnimationTogglesFull(this);
        hideAnimationClass(OTSLink.class);
        hideAnimationClass(OTSNode.class);
        // addToggleAnimationButtonText("Block", LaneBlock.class, "Show/hide Blocks", false);
        // addToggleAnimationButtonText("BlockId", LaneBlockAnimation.Text.class, "Show/hide Block Ids", false);
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Turbo roundabout demonstration";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "Turbo roundabout demonstration";
    }

    /**
     * The simulation model.
     */
    class TurboRoundaboutModel implements OTSModelInterface
    {

        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private OTSNetwork network;

        /** Simulator. */
        private OTSSimulatorInterface simulator;

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> arg0) throws SimRuntimeException
        {
            this.simulator = (OTSSimulatorInterface) arg0;
            try
            {
                URL url = URLResource.getResource("/conflict/TurboRoundabout.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator, getColorer());
                this.network = nlp.build(url, false);

                // add conflicts
                ConflictBuilder.buildConflicts(this.network, VEHICLE, this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));

                // add trafficlights
                for (Lane lane : ((CrossSectionLink) this.network.getLink("SEXITS")).getLanes())
                {
                    SimpleTrafficLight trafficLight = new SimpleTrafficLight("light" + lane.getId(), lane,
                            new Length(150.0, LengthUnit.SI), this.simulator);

                    try
                    {
                        new TrafficLightAnimation(trafficLight, simulator);
                    }
                    catch (RemoteException | NamingException exception)
                    {
                        throw new NetworkException(exception);
                    }

                    trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                    changePhase(trafficLight);
                }

                // test for ignoring conflicting GTU's upstream of traffic light
                // for (Lane lane : ((CrossSectionLink) this.network.getLink("SBEA")).getLanes())
                // {
                // SimpleTrafficLight trafficLight = new SimpleTrafficLight("light" + lane.getId(), lane,
                // new Length(10.0, LengthUnit.SI), this.simulator);
                //
                // try
                // {
                // new TrafficLightAnimation(trafficLight, simulator);
                // }
                // catch (RemoteException | NamingException exception)
                // {
                // throw new NetworkException(exception);
                // }
                //
                // trafficLight.setTrafficLightColor(TrafficLightColor.GREEN);
                // }

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * Changes color of traffic light.
         * @param trafficLight SimpleTrafficLight; traffic light
         * @throws SimRuntimeException scheduling error
         */
        private void changePhase(final SimpleTrafficLight trafficLight) throws SimRuntimeException
        {
            switch (trafficLight.getTrafficLightColor())
            {
                case RED:
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.GREEN);
                    this.simulator.scheduleEventRel(new Duration(15.0, DurationUnit.SECOND), this, this, "changePhase",
                            new Object[] { trafficLight });
                    break;
                }
                case YELLOW:
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                    this.simulator.scheduleEventRel(new Duration(56.0, DurationUnit.SECOND), this, this, "changePhase",
                            new Object[] { trafficLight });
                    break;
                }
                case GREEN:
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.YELLOW);
                    this.simulator.scheduleEventRel(new Duration(4.0, DurationUnit.SECOND), this, this, "changePhase",
                            new Object[] { trafficLight });
                    break;
                }
                default:
                {
                    //
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
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
                    TurboRoundaboutDemo animation = new TurboRoundaboutDemo();
                    // 1 hour simulation run for testing
                    animation.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(60.0, DurationUnit.MINUTE),
                            new ArrayList<InputParameter<?>>(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | InputParameterException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

}
