package org.opentrafficsim.demo.trafficcontrol;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.demo.trafficcontrol.TrafCODDemo.TrafCODModel;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventType;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 18, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafCODDemo extends OTSSimulationApplication<TrafCODModel>
{
    /** */
    private static final long serialVersionUID = 20161118L;

    /**
     * Create a TrafcodAndTurbo demo.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model TrafCODModel; the model
     * @throws OTSDrawingException on animation error
     */
    public TrafCODDemo(final String title, final OTSAnimationPanel panel, final TrafCODModel model) throws OTSDrawingException
    {
        super(model, panel);
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
            final TrafCODModel trafcodModel = new TrafCODModel(simulator);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), trafcodModel);
            OTSAnimationPanel animationPanel = new OTSAnimationPanel(trafcodModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, trafcodModel, DEFAULT_COLORER, trafcodModel.getNetwork());
            TrafCODDemo app = new TrafCODDemo("TrafCOD demo simple crossing", animationPanel, trafcodModel);
            app.setExitOnClose(exitOnClose);
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add tab with trafCOD status.
     */
    protected void addTabs()
    {
        JScrollPane scrollPane = new JScrollPane(getModel().getControllerDisplayPanel());
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane);
        getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount() - 1,
                getModel().getTrafCOD().getId(), wrapper);
    }

    /**
     * The simulation model.
     */
    static class TrafCODModel extends AbstractOTSModel implements EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20161020L;

        /** the model. */
        private OTSNetwork network;

        /** The TrafCOD controller. */
        private TrafCOD trafCOD;

        /** TrafCOD controller display. */
        private JPanel controllerDisplayPanel = new JPanel(new BorderLayout());

        /**
         * @param simulator OTSSimulatorInterface; the simulator for this model
         */
        TrafCODModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                URL url = URLResource.getResource("/TrafCODDemo1/TrafCODDemo1.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(getSimulator());
                this.network = nlp.build(url, true);

                Lane laneNX = (Lane) ((CrossSectionLink) this.network.getLink("N", "X")).getCrossSectionElement("FORWARD");
                Lane laneWX = (Lane) ((CrossSectionLink) this.network.getLink("W", "X")).getCrossSectionElement("FORWARD");
                Set<TrafficLight> trafficLights = new HashSet<>();
                SimpleTrafficLight tl08 =
                        new SimpleTrafficLight("TL08", laneWX, new Length(296, LengthUnit.METER), getSimulator());
                trafficLights.add(tl08);

                try
                {
                    new TrafficLightAnimation(tl08, this.simulator);
                }
                catch (RemoteException | NamingException exception)
                {
                    throw new NetworkException(exception);
                }

                SimpleTrafficLight tl11 =
                        new SimpleTrafficLight("TL11", laneNX, new Length(296, LengthUnit.METER), getSimulator());
                trafficLights.add(tl11);

                try
                {
                    new TrafficLightAnimation(tl11, this.simulator);
                }
                catch (RemoteException | NamingException exception)
                {
                    throw new NetworkException(exception);
                }

                Set<TrafficLightSensor> sensors = new HashSet<>();
                sensors.add(new TrafficLightSensor("D081", laneWX, new Length(292, LengthUnit.METER), laneWX,
                        new Length(294, LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR, getSimulator(),
                        Compatible.EVERYTHING));
                sensors.add(new TrafficLightSensor("D082", laneWX, new Length(260, LengthUnit.METER), laneWX,
                        new Length(285, LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR, getSimulator(),
                        Compatible.EVERYTHING));
                sensors.add(new TrafficLightSensor("D111", laneNX, new Length(292, LengthUnit.METER), laneNX,
                        new Length(294, LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR, getSimulator(),
                        Compatible.EVERYTHING));
                sensors.add(new TrafficLightSensor("D112", laneNX, new Length(260, LengthUnit.METER), laneNX,
                        new Length(285, LengthUnit.METER), null, RelativePosition.FRONT, RelativePosition.REAR, getSimulator(),
                        Compatible.EVERYTHING));
                String controllerName = "Simple TrafCOD controller";
                this.trafCOD = new TrafCOD(controllerName, URLResource.getResource("/TrafCODDemo1/simpleTest.tfc"),
                        trafficLights, sensors, getSimulator(), this.controllerDisplayPanel);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_STATE_CHANGED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_VARIABLE_CREATED);
                this.trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED);
                // Subscribe the TrafCOD machine to trace command events that we emit
                addListener(this.trafCOD, TrafficController.TRAFFICCONTROL_SET_TRACING);
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TGX", 8, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "XR1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TD1", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TGX", 11, true});
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] {controllerName, "TL", 11, true});
                // System.out.println("demo: emitting a SET TRACING event for all variables related to stream 11");
                // fireEvent(TrafficController.TRAFFICCONTROL_SET_TRACING, new Object[] { controllerName, "", 11, true });

                // this.trafCOD.traceVariablesOfStream(TrafficController.NO_STREAM, true);
                // this.trafCOD.traceVariablesOfStream(11, true);
                // this.trafCOD.traceVariable("MRV", 11, true);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public final OTSNetwork getNetwork()
        {
            return this.network;
        }

        /**
         * @return trafCOD
         */
        public final TrafCOD getTrafCOD()
        {
            return this.trafCOD;
        }

        /**
         * @return controllerDisplayPanel
         */
        public final JPanel getControllerDisplayPanel()
        {
            return this.controllerDisplayPanel;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            EventType type = event.getType();
            Object[] payload = (Object[]) event.getContent();
            if (TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING.equals(type))
            {
                // System.out.println("Evalution starts at " + getSimulator().getSimulatorTime());
                return;
            }
            else if (TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED.equals(type))
            {
                System.out.println("Conflict group changed from " + ((String) payload[1]) + " to " + ((String) payload[2]));
            }
            else if (TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED.equals(type))
            {
                System.out.println(String.format("Variable changed %s <- %d   %s", payload[1], payload[4], payload[5]));
            }
            else if (TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING.equals(type))
            {
                System.out.println("Warning " + payload[1]);
            }
            else
            {
                System.out.print("TrafCODDemo received event of type " + event.getType() + ", payload [");
                String separator = "";
                for (Object o : payload)
                {
                    System.out.print(separator + o);
                    separator = ",";
                }
                System.out.println("]");
            }
        }

    }

}
