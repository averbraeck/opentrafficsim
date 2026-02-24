package org.opentrafficsim.demo.trafficcontrol;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Optional;
import java.util.Scanner;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.object.NonLocatedObject;
import org.opentrafficsim.demo.trafficcontrol.TrafCodDemo2.TrafCodModel;
import org.opentrafficsim.road.network.factory.xml.OtsXmlModel;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationPanelDecorator;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficController;
import org.opentrafficsim.trafficcontrol.trafcod.TrafCod;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.TabbedContentPane;
import nl.tudelft.simulation.language.DsolException;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrafCodDemo2 extends OtsSimulationApplication<TrafCodModel>
{
    /** */
    private static final long serialVersionUID = 20161118L;

    /**
     * Create a Trafcod demo.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public TrafCodDemo2(final String title, final OtsSimulationPanel panel, final TrafCodModel model)
    {
        super(model, panel);
    }

    /**
     * Main program.
     * @param args the command line arguments (not used)
     * @throws IOException ...
     */
    public static void main(final String[] args) throws IOException
    {
        demo(true);
    }

    /**
     * Open an URL, read it and store the contents in a string. Adapted from
     * https://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code
     * @param url the URL
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
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     * @throws IOException when reading the file fails
     */
    public static void demo(final boolean exitOnClose) throws IOException
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("TrafCODDemo2");
            final TrafCodModel trafcodModel = new TrafCodModel(simulator, "TrafCODModel", "TrafCOD demonstration Model");
            OtsSimulationPanel simulationPanel =
                    new OtsSimulationPanel(trafcodModel.getNetwork(), new OtsSimulationPanelDecorator()
                    {
                        @Override
                        public void addTabs(final OtsSimulationPanel simulationPanel, final Network network)
                        {
                            TrafCodDemo2.addTabs(simulationPanel, network);
                        }
                    });
            TrafCodDemo2 app = new TrafCodDemo2("TrafCOD demo complex crossing", simulationPanel, trafcodModel);
            app.setExitOnClose(exitOnClose);
            simulationPanel.enableSimulationControlButtons();
        }
        catch (SimRuntimeException | RemoteException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add tabs with trafCOD status display.
     * @param animationPanel animation panel
     * @param network network
     */
    private static void addTabs(final OtsSimulationPanel animationPanel, final Network network)
    {
        if (null == animationPanel)
        {
            return;
        }
        ImmutableMap<String, NonLocatedObject> nonLocatedObjectMap = network.getNonLocatedObjectMap();
        for (NonLocatedObject ioi : nonLocatedObjectMap.values())
        {
            if (ioi instanceof TrafCod)
            {
                TrafCod trafCOD = (TrafCod) ioi;
                Optional<Container> controllerDisplayPanel = trafCOD.getDisplayContainer();
                if (controllerDisplayPanel.isPresent())
                {
                    JPanel wrapper = new JPanel(new BorderLayout());
                    wrapper.add(new JScrollPane(controllerDisplayPanel.get()));
                    TabbedContentPane tabbedPane = animationPanel.getTabbedPane();
                    tabbedPane.addTab(tabbedPane.getTabCount() - 1, trafCOD.getId(), wrapper);
                }
                TrafCodListener listener = new TrafCodListener(network.getSimulator());
                // trafCOD.addListener(this, TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING);
                trafCOD.addListener(listener, TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING);
                trafCOD.addListener(listener, TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED);
                trafCOD.addListener(listener, TrafficController.TRAFFICCONTROL_STATE_CHANGED);
                trafCOD.addListener(listener, TrafficController.TRAFFICCONTROL_VARIABLE_CREATED);
                trafCOD.addListener(listener, TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED);
            }
        }
    }

    /**
     * Logs TrafCod events.
     */
    private static class TrafCodListener implements EventListener
    {

        /** Simulator. */
        private final OtsSimulatorInterface simulator;

        /**
         * Constructor.
         * @param simulator simulator
         */
        TrafCodListener(final OtsSimulatorInterface simulator)
        {
            this.simulator = simulator;
        }

        @Override
        public void notify(final Event event)
        {
            EventType type = event.getType();
            Object[] payload = (Object[]) event.getContent();
            if (TrafficController.TRAFFICCONTROL_CONTROLLER_EVALUATING.equals(type))
            {
                Logger.ots().info("Evaluation starts at " + this.simulator.getSimulatorTime());
                return;
            }
            else if (TrafficController.TRAFFICCONTROL_CONFLICT_GROUP_CHANGED.equals(type))
            {
                Logger.ots().info("Conflict group changed from {} to {}", (String) payload[1], (String) payload[2]);
            }
            else if (TrafficController.TRAFFICCONTROL_TRACED_VARIABLE_UPDATED.equals(type))
            {
                Logger.ots().info("Variable changed %s <- %d   %s", payload[1], payload[4], payload[5]);
            }
            else if (TrafficController.TRAFFICCONTROL_CONTROLLER_WARNING.equals(type))
            {
                Logger.ots().info("Warning " + payload[1]);
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
                Logger.ots().info(stringBuilder.toString());
            }
        }

    }

    /**
     * The simulation model.
     */
    public static class TrafCodModel extends OtsXmlModel
    {

        /**
         * Constructor.
         * @param simulator the simulator
         * @param shortName name of the model
         * @param description description of the model
         */
        public TrafCodModel(final OtsSimulatorInterface simulator, final String shortName, final String description)
        {
            super(simulator, shortName, description, AbstractOtsModel.defaultInitialStreams(),
                    "/resources/TrafCODDemo2/TrafCODDemo2.xml", null);
        }

        @Override
        public String toString()
        {
            return "TrafCODModel [network=" + getNetwork().getId() + "]";
        }

    }

}
