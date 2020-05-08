package org.opentrafficsim.demo.trafficcontrol;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventTypeInterface;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.io.URLResource;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.demo.trafficcontrol.TrafCODDemo2.TrafCODModel;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCOD;
import org.opentrafficsim.xml.generated.OTS;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * Create a Trafcod demo.
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
            OTSAnimator simulator = new OTSAnimator("TrafCODDemo2");
            URL url = URLResource.getResource("/TrafCODDemo2/TrafCODDemo2.xml");
            System.out.println("url is " + url);
            String xml = readStringFromURL(url);
            final TrafCODModel trafcodModel = new TrafCODModel(simulator, "TrafCODModel", "TrafCOD demonstration Model", xml);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), trafcodModel);
            OTSAnimationPanel animationPanel = new OTSAnimationPanel(trafcodModel.getNetwork().getExtent(),
                    new Dimension(800, 600), simulator, trafcodModel, DEFAULT_COLORER, trafcodModel.getNetwork());
            TrafCODDemo2 app = new TrafCODDemo2("TrafCOD demo complex crossing", animationPanel, trafcodModel);
            app.setExitOnClose(exitOnClose);
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | DSOLException exception)
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
        OTSAnimationPanel animationPanel = getAnimationPanel();
        if (null == animationPanel)
        {
            return;
        }
        ImmutableMap<String, InvisibleObjectInterface> invisibleObjectMap = getModel().getNetwork().getInvisibleObjectMap();
        for (InvisibleObjectInterface ioi : invisibleObjectMap.values())
        {
            if (ioi instanceof TrafCOD)
            {
                TrafCOD trafCOD = (TrafCOD) ioi;
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
    public static class TrafCODModel extends AbstractOTSModel implements EventListenerInterface
    {
        /** */
        private static final long serialVersionUID = 20161020L;

        /** The network. */
        private OTSRoadNetwork network;

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
            super(simulator, shortName, description);
            this.xml = xml;
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                this.network = new OTSRoadNetwork(getShortName(), true, getSimulator());
                OTS ots = XmlNetworkLaneParser.parseXML(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)));
                XmlNetworkLaneParser.build(ots, this.network, getSimulator(), false);
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

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            EventTypeInterface type = event.getType();
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
        public Serializable getSourceId()
        {
            return "TrafCODModel";
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "TrafCODModel [network=" + network.getId() + "]";
        }

    }

}
