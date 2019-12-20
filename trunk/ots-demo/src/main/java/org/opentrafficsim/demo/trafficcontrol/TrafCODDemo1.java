package org.opentrafficsim.demo.trafficcontrol;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.bind.DatatypeConverter;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.trafficcontrol.TrafCODDemo1.TrafCODModel;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;
import org.opentrafficsim.xml.generated.CONTROL;
import org.opentrafficsim.xml.generated.CONTROL.TRAFCOD;
import org.opentrafficsim.xml.generated.OTS;

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
public class TrafCODDemo1 extends OTSSimulationApplication<TrafCODModel>
{
    /** */
    private static final long serialVersionUID = 20161118L;

    /**
     * Create a Trafcod demo.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model TrafCODModel; the model
     * @throws OTSDrawingException on animation error
     */
    public TrafCODDemo1(final String title, final OTSAnimationPanel panel, final TrafCODModel model) throws OTSDrawingException
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
            URL url = URLResource.getResource("/TrafCODDemo1/TrafCODDemo1.xml");
            String xml = readStringFromURL(url);
            final TrafCODModel trafcodModel = new TrafCODModel(simulator, "TrafCODModel", "TrafCOD demonstration Model", xml);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), trafcodModel);
            OTSAnimationPanel animationPanel = new OTSAnimationPanel(trafcodModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, trafcodModel, DEFAULT_COLORER, trafcodModel.getNetwork());
            TrafCODDemo1 app = new TrafCODDemo1("TrafCOD demo simple crossing", animationPanel, trafcodModel);
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
    public static class TrafCODModel extends AbstractOTSModel implements EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20161020L;

        /** The network. */
        private OTSRoadNetwork network;

        /** The TrafCOD controller. */
        private TrafCOD trafCOD;

        /** TrafCOD controller display. */
        private JPanel controllerDisplayPanel = new JPanel(new BorderLayout());

        /** The XML. */
        private final String xml;

        /**
         * @param simulator OTSSimulatorInterface; the simulator
         * @param shortName String; name of the model
         * @param description String; description of the model
         * @param xml String; the XML string
         */
        public TrafCODModel(final OTSSimulatorInterface simulator, final String shortName, final String description,
                final String xml)
        {
            super(simulator);
            this.xml = xml;
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                this.network = new OTSRoadNetwork(getShortName(), true);
                OTS ots = XmlNetworkLaneParser.parseXML(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)));
                XmlNetworkLaneParser.build(ots, this.network, getSimulator(), false);

                String controllerName = "TrafCOD_simple";
                List<CONTROL> trafficControllerList = ots.getCONTROL();
                Throw.when(trafficControllerList.size() != 1, NetworkException.class,
                        "OTS contains wrong number of traffic controllers (should be 1, got %1)", trafficControllerList.size());
                CONTROL controller = trafficControllerList.get(0);
                List<TRAFCOD> trafCodList = controller.getTRAFCOD();
                Throw.when(trafCodList.size() != 1, NetworkException.class, "Controller should contain one TRAFCOD (got %1)",
                        trafCodList.size());
                TRAFCOD trafCod = trafCodList.get(0);
                String programString = trafCod.getPROGRAM().getValue();
                List<String> program = null == programString ? TrafCOD.loadTextFromURL(new URL(trafCod.getPROGRAMFILE()))
                        : Arrays.asList(programString.split("\n"));
                TRAFCOD.CONSOLE.MAP mapData = trafCod.getCONSOLE().getMAP();
                BufferedImage backgroundImage = null;
                if (null != mapData)
                {
                    String graphicsType = mapData.getTYPE();
                    String encoding = mapData.getENCODING();
                    String encodedData = mapData.getValue();
                    if (!"base64".contentEquals(encoding))
                    {
                        throw new RuntimeException("Unexpected image encoding: " + encoding);
                    }
                    byte[] imageBytes = DatatypeConverter.parseBase64Binary(encodedData);
                    switch (graphicsType)
                    {
                        case "PNG":
                            backgroundImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                            // javax.imageio.ImageIO.write(backgroundImage, "png", new File("c:\\temp\\test.png"));
                            break;

                        default:
                            throw new RuntimeException("Unexpected image type: " + graphicsType);
                    }
                }
                String objectLocationsString = trafCod.getCONSOLE().getCOORDINATES().getValue();
                List<String> displayObjectLocations = null == objectLocationsString
                        ? TrafCOD.loadTextFromURL(new URL(trafCod.getCONSOLE().getCOORDINATESFILE()))
                        : Arrays.asList(objectLocationsString.split("\n"));
                this.trafCOD = new TrafCOD(controllerName, program, getSimulator(), this.controllerDisplayPanel,
                        backgroundImage, displayObjectLocations);

                
                Lane laneNX = (Lane) ((CrossSectionLink) this.network.getLink("N", "XS")).getCrossSectionElement("FORWARD");
                Lane laneWX = (Lane) ((CrossSectionLink) this.network.getLink("W", "XE")).getCrossSectionElement("FORWARD");
                SimpleTrafficLight tl08 = new SimpleTrafficLight(String.format("%s.%02d", controllerName, 8), laneWX,
                        new Length(296, LengthUnit.METER), getSimulator());
                try
                {
                    new TrafficLightAnimation(tl08, this.simulator);
                }
                catch (RemoteException | NamingException exception)
                {
                    throw new NetworkException(exception);
                }

                SimpleTrafficLight tl11 = new SimpleTrafficLight(String.format("%s.%02d", controllerName, 11), laneNX,
                        new Length(296, LengthUnit.METER), getSimulator());
                try
                {
                    new TrafficLightAnimation(tl11, this.simulator);
                }
                catch (RemoteException | NamingException exception)
                {
                    throw new NetworkException(exception);
                }

                this.trafCOD = new TrafCOD(controllerName, program, getSimulator(), this.controllerDisplayPanel,
                        backgroundImage, displayObjectLocations);
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
