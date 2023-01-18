package org.opentrafficsim.demo.conflict;

import java.awt.Dimension;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGtuColorer;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.conflict.TurboRoundaboutDemo.TurboRoundaboutModel;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TurboRoundaboutDemo extends OtsSimulationApplication<TurboRoundaboutModel>
{
    /** */
    private static final long serialVersionUID = 20161211L;

    /**
     * Create a T-Junction demo.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model TurboRoundaboutModel; the model
     * @throws OtsDrawingException on animation error
     */
    public TurboRoundaboutDemo(final String title, final OtsAnimationPanel panel, final TurboRoundaboutModel model)
            throws OtsDrawingException
    {
        super(model, panel);
        animateNetwork(DefaultsNl.TRUCK, DefaultsNl.CAR);
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
            OtsAnimator simulator = new OtsAnimator("TurboRoundaboutDemo");
            final TurboRoundaboutModel junctionModel = new TurboRoundaboutModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), junctionModel);
            OtsAnimationPanel animationPanel =
                    new OtsAnimationPanel(junctionModel.getNetwork().getExtent(), new Dimension(800, 600), simulator,
                            junctionModel, new DefaultSwitchableGtuColorer(), junctionModel.getNetwork());
            TurboRoundaboutDemo app = new TurboRoundaboutDemo("Turbo-Roundabout demo", animationPanel, junctionModel);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | NamingException | RemoteException | OtsDrawingException | DSOLException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The simulation model.
     */
    public static class TurboRoundaboutModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /** The network. */
        private OtsRoadNetwork network;

        /**
         * @param simulator OTSSimulatorInterface; the simulator for this model
         */
        public TurboRoundaboutModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                URL xmlURL = URLResource.getResource("/resources/conflict/TurboRoundabout.xml");
                this.network = new OtsRoadNetwork("TurboRoundabout", true, getSimulator());
                XmlNetworkLaneParser.build(xmlURL, this.network, true);

                // add trafficlights
                for (Lane lane : ((CrossSectionLink) this.network.getLink("SEXITS2")).getLanes())
                {
                    SimpleTrafficLight trafficLight = new SimpleTrafficLight("light" + lane.getId(), lane,
                            new Length(150.0, LengthUnit.SI), this.simulator);

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
                            new Object[] {trafficLight});
                    break;
                }
                case YELLOW:
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                    this.simulator.scheduleEventRel(new Duration(56.0, DurationUnit.SECOND), this, this, "changePhase",
                            new Object[] {trafficLight});
                    break;
                }
                case GREEN:
                {
                    trafficLight.setTrafficLightColor(TrafficLightColor.YELLOW);
                    this.simulator.scheduleEventRel(new Duration(4.0, DurationUnit.SECOND), this, this, "changePhase",
                            new Object[] {trafficLight});
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
        public OtsRoadNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "TurboRoundaboutModel";
        }

    }
}
