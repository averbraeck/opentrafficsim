package org.opentrafficsim.demo.trafficcontrol;

import java.awt.BorderLayout;
import java.awt.Container;
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
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.object.NonLocatedObject;
import org.opentrafficsim.demo.trafficcontrol.TrafCodDemo2.TrafCodModel;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCod;
import org.opentrafficsim.xml.generated.OTS;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TrafCodDemo2 extends OtsSimulationApplication<TrafCodModel>
{
    /** */
    private static final long serialVersionUID = 20161118L;

    /**
     * Create a Trafcod demo.
     * @param title String; the title of the Frame
     * @param panel OtsAnimationPanel; the tabbed panel to display
     * @param model TrafCODModel; the model
     * @throws OtsDrawingException on animation error
     */
    public TrafCodDemo2(final String title, final OtsAnimationPanel panel, final TrafCodModel model) throws OtsDrawingException
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
            OtsAnimator simulator = new OtsAnimator("TrafCODDemo2");
            URL url = URLResource.getResource("/resources/TrafCODDemo2/TrafCODDemo2.xml");
            System.out.println("url is " + url);
            String xml = readStringFromURL(url);
            final TrafCodModel trafcodModel = new TrafCodModel(simulator, "TrafCODModel", "TrafCOD demonstration Model", xml);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), trafcodModel);
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(trafcodModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, trafcodModel, DEFAULT_COLORER, trafcodModel.getNetwork());
            TrafCodDemo2 app = new TrafCodDemo2("TrafCOD demo complex crossing", animationPanel, trafcodModel);
            app.setExitOnClose(exitOnClose);
            animationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | NamingException | RemoteException | OtsDrawingException | DSOLException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add tabs with trafCOD status display.
     */
    @Override
    protected final void addTabs()
    {
        OtsAnimationPanel animationPanel = getAnimationPanel();
        if (null == animationPanel)
        {
            return;
        }
        ImmutableMap<String, NonLocatedObject> nonLocatedObjectMap = getModel().getNetwork().getNonLocatedObjectMap();
        for (NonLocatedObject ioi : nonLocatedObjectMap.values())
        {
            if (ioi instanceof TrafCod)
            {
                TrafCod trafCOD = (TrafCod) ioi;
                Container controllerDisplayPanel = trafCOD.getDisplayContainer();
                if (null != controllerDisplayPanel)
                {
                    JPanel wrapper = new JPanel(new BorderLayout());
                    wrapper.add(new JScrollPane(controllerDisplayPanel));
                    TabbedContentPane tabbedPane = animationPanel.getTabbedPane();
                    tabbedPane.addTab(tabbedPane.getTabCount() - 1, trafCOD.getId(), wrapper);
                }
                // trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING);
                trafCOD.addListener(getModel(), TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING);
                trafCOD.addListener(getModel(), TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED);
                trafCOD.addListener(getModel(), TrafficController.TRAFFICCONTROL_STATE_CHANGED);
                trafCOD.addListener(getModel(), TrafficController.TRAFFICCONTROL_VARIABLE_CREATED);
                trafCOD.addListener(getModel(), TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED);

            }
        }
    }

    /**
     * The simulation model.
     */
    public static class TrafCodModel extends AbstractOtsModel implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20161020L;

        /** The network. */
        private OtsRoadNetwork network;

        /** The XML. */
        private final String xml;

        /**
         * @param simulator OtsSimulatorInterface; the simulator
         * @param shortName String; name of the model
         * @param description String; description of the model
         * @param xml String; the XML string
         */
        public TrafCodModel(final OtsSimulatorInterface simulator, final String shortName, final String description,
                final String xml)
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
                this.network = new OtsRoadNetwork(getShortName(), getSimulator());
                OTS ots = XmlNetworkLaneParser.parseXml(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)));
                XmlNetworkLaneParser.build(ots, this.network, false);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public final OtsRoadNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final Event event) throws RemoteException
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
                CategoryLogger.always().info("Conflict group changed from {} to {}", (String) payload[1], (String) payload[2]);
            }
            else if (TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED.equals(type))
            {
                CategoryLogger.always().info("Variable changed %s <- %d   %s", payload[1], payload[4], payload[5]);
            }
            else if (TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING.equals(type))
            {
                CategoryLogger.always().info("Warning " + payload[1]);
            }
            else
            {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("TrafCODDemo received event of type " + event.getType() + ", payload [");
                String separator = "";
                for (Object o : payload)
                {
                    stringBuilder.append(separator + o);
                    separator = ",";
                }
                stringBuilder.append("]");
                CategoryLogger.always().info(stringBuilder.toString());
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "TrafCODModel [network=" + this.network.getId() + "]";
        }

    }

}
