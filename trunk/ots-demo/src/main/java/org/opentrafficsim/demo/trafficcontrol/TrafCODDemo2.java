package org.opentrafficsim.demo.trafficcontrol;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.demo.trafficcontrol.TrafCODDemo2.TrafCODModel;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
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
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 06, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafCODDemo2 extends OTSSimulationApplication<TrafCODModel>
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
    public TrafCODDemo2(final String title, final OTSAnimationPanel panel, final TrafCODModel model) throws OTSDrawingException
    {
        super(model, panel);
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws IOException ...
     */
    public static void main(final String[] args) throws IOException
    {
        demo(true);
    }

    /**
     * Open an URL, read it and store the contents in a string. Adapted from
     * https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
     * @param url URL; the URL
     * @return String
     * @throws IOException when reading the file fails
     */
    public static String readStringFromURL(final URL url) throws IOException
    {
        try (Scanner scanner = new Scanner(url.openStream(), StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     * @throws IOException when reading the file fails
     */
    public static void demo(final boolean exitOnClose) throws IOException
    {
        try
        {
            OTSAnimator simulator = new OTSAnimator();
            URL url = URLResource.getResource("/TrafCODDemo2/TrafCODDemo2.xml");
            String xml = readStringFromURL(url);
            final TrafCODModel trafcodModel = new TrafCODModel(simulator, "TrafCODModel", "TrafCOD demonstration Model", xml);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), trafcodModel);
            OTSAnimationPanel animationPanel = new OTSAnimationPanel(trafcodModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, trafcodModel, DEFAULT_COLORER, trafcodModel.getNetwork());
            TrafCODDemo2 app = new TrafCODDemo2("TrafCOD demo complex crossing", animationPanel, trafcodModel);
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
    @Override
    protected final void addTabs()
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

        /** The network. */
        private OTSRoadNetwork network;

        /** The XML. */
        private final String xml;

        /** The TrafCOD controller. */
        private TrafCOD trafCOD;

        /** TrafCOD controller display. */
        private JPanel controllerDisplayPanel = new JPanel(new BorderLayout());

        /**
         * @param simulator OTSSimulatorInterface; the simulator
         * @param shortName String; name of the model
         * @param description String; description of the model
         * @param xml String; the XML string
         */
        TrafCODModel(final OTSSimulatorInterface simulator, final String shortName, final String description, final String xml)
        {
            super(simulator, shortName, description);
            this.xml = xml;
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                this.network = new OTSRoadNetwork(getShortName(), true);
                XmlNetworkLaneParser.build(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)), this.network,
                        getSimulator());
                ConflictBuilder.buildConflicts(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE), getSimulator(),
                        new ConflictBuilder.FixedWidthGenerator(Length.createSI(2.0)));

                String controllerName = "TrafCOD_complex";
                /*-
                String[] directions = { "E", "S", "W", "N" };
                // Add the traffic lights and the detectors
                Length stopLineMargin = new Length(0.1, LengthUnit.METER);
                Length headDetectorLength = new Length(1, LengthUnit.METER);
                Length headDetectorMargin = stopLineMargin.plus(headDetectorLength).plus(new Length(3, LengthUnit.METER));
                Length longDetectorLength = new Length(30, LengthUnit.METER);
                Length longDetectorMargin = stopLineMargin.plus(longDetectorLength).plus(new Length(10, LengthUnit.METER));
                int stream = 1;
                for (String direction : directions)
                {
                    for (int laneNumber = 3; laneNumber >= 1; laneNumber--)
                    {
                        Lane lane = (Lane) ((CrossSectionLink) this.network.getLink(direction, direction + "C"))
                                .getCrossSectionElement("FORWARD" + laneNumber);
                        new TrafficLightSensor(String.format("%s.D%02d1", controllerName, stream), lane,
                                lane.getLength().minus(headDetectorMargin), lane,
                                lane.getLength().minus(headDetectorMargin).plus(headDetectorLength), null,
                                RelativePosition.FRONT, RelativePosition.REAR, getSimulator(), Compatible.EVERYTHING);
                        new TrafficLightSensor(String.format("%s.D%02d2", controllerName, stream), lane,
                                lane.getLength().minus(longDetectorMargin), lane,
                                lane.getLength().minus(longDetectorMargin).plus(longDetectorLength), null,
                                RelativePosition.FRONT, RelativePosition.REAR, getSimulator(), Compatible.EVERYTHING);
                        stream++;
                    }
                }
                        */
                this.trafCOD = new TrafCOD(controllerName, URLResource.getResource("/TrafCODDemo2/Intersection12Dir.tfc"),
                        getSimulator(), this.controllerDisplayPanel);
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
        public final OTSRoadNetwork getNetwork()
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
                // System.out.println("Evaluation starts at " + getSimulator().getSimulatorTime());
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
