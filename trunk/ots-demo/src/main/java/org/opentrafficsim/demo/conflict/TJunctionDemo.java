package org.opentrafficsim.demo.conflict;

import static org.opentrafficsim.core.gtu.GTUType.VEHICLE;

import java.awt.Dimension;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSwingApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 11 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TJunctionDemo extends OTSSwingApplication
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a T-Junction demo.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     * @throws OTSDrawingException on animation error
     */
    public TJunctionDemo(final String title, final OTSAnimationPanel panel, final TJunctionModel model)
            throws OTSDrawingException
    {
        super(model, panel);
        DefaultAnimationFactory.animateNetwork(model.getNetwork(), model.getSimulator(), DEFAULT_COLORER);
        AnimationToggles.setTextAnimationTogglesStandard(panel);
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OTSAnimator simulator = new OTSAnimator();
            final TJunctionModel junctionModel = new TJunctionModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), junctionModel);
            OTSAnimationPanel animationPanel = new OTSAnimationPanel(junctionModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, junctionModel, DEFAULT_COLORER, junctionModel.getNetwork());
            TJunctionDemo app = new TJunctionDemo("T-Junction demo", animationPanel, junctionModel);
            app.setExitOnClose(exitOnClose);
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    static class TJunctionModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private OTSNetwork network;

        /**
         * @param simulator the simulator for this model
         */
        TJunctionModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                URL url = URLResource.getResource("/conflict/TJunction.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator, new DefaultSwitchableGTUColorer());
                this.network = nlp.build(url, false);

                // add conflicts
                // ((CrossSectionLink) this.network.getLink("SCEC")).setPriority(Priority.STOP);
                // ((CrossSectionLink) this.network.getLink("SCWC")).setPriority(Priority.STOP);
                ConflictBuilder.buildConflicts(this.network, VEHICLE, this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));

                // add trafficlight after
                Lane lane = ((CrossSectionLink) this.network.getLink("ECE")).getLanes().get(0);
                SimpleTrafficLight trafficLight =
                        new SimpleTrafficLight("light", lane, new Length(50.0, LengthUnit.SI), this.simulator);

                try
                {
                    new TrafficLightAnimation(trafficLight, this.simulator);
                }
                catch (RemoteException | NamingException exception)
                {
                    throw new NetworkException(exception);
                }
                trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                changePhase(trafficLight);

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
                    this.simulator.scheduleEventRel(new Duration(30.0, DurationUnit.SECOND), this, this, "changePhase",
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
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }
}
