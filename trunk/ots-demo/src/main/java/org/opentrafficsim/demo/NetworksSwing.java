package org.opentrafficsim.demo;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionMap;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.AbstractInputField;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputField;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;

/**
 * Simplest contour plots demonstration.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2019-01-06 01:35:05 +0100 (Sun, 06 Jan 2019) $, @version $Revision: 4831 $, by $Author: averbraeck $,
 * initial version 12 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class NetworksSwing extends OTSSimulationApplication<NetworksModel> implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a Networks Swing application.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model NetworksModel; the model
     * @throws OTSDrawingException on animation error
     */
    public NetworksSwing(final String title, final OTSAnimationPanel panel, final NetworksModel model)
            throws OTSDrawingException
    {
        super(model, panel);
        OTSRoadNetwork network = model.getNetwork();
        System.out.println(network.getLinkMap());
    }

    /** {@inheritDoc} */
    @Override
    protected void addTabs()
    {
        addStatisticsTabs(getModel().getSimulator());
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
            final NetworksModel otsModel = new NetworksModel(simulator);
            if (NetworksParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), otsModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(otsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, otsModel, DEFAULT_COLORER, otsModel.getNetwork());
                NetworksSwing app = new NetworksSwing("Networks", animationPanel, otsModel);
                app.setExitOnClose(exitOnClose);
            }
            else
            {
                if (exitOnClose)
                {
                    System.exit(0);
                }
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add the statistics tabs.
     * @param simulator OTSSimulatorInterface; the simulator on which sampling can be scheduled
     */
    protected final void addStatisticsTabs(final OTSSimulatorInterface simulator)
    {
        int graphCount = getModel().pathCount();
        int columns = 1;
        int rows = 0 == columns ? 0 : (int) Math.ceil(graphCount * 1.0 / columns);
        TablePanel charts = new TablePanel(columns, rows);
        RoadSampler sampler = new RoadSampler(simulator);
        Duration updateInterval = Duration.createSI(10.0);
        for (int graphIndex = 0; graphIndex < graphCount; graphIndex++)
        {
            List<LaneDirection> start = new ArrayList<>();
            start.add(new LaneDirection(getModel().getPath(graphIndex).get(0), GTUDirectionality.DIR_PLUS));
            GraphPath<KpiLaneDirection> path;
            try
            {
                path = GraphLaneUtil.createPath("name", start.get(0));
            }
            catch (NetworkException exception)
            {
                throw new RuntimeException(exception);
            }
            SwingPlot plot = new SwingPlot(
                    new TrajectoryPlot("Trajectories on lane " + (graphIndex + 1), updateInterval, simulator, sampler, path));
            charts.setCell(plot.getContentPane(), graphIndex % columns, graphIndex / columns);
        }

        getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "statistics ", charts);
    }

    /** A parameter dialog with a radio button for the network choice tab. */
    private static class NetworksParameterDialog extends TabbedParameterDialog
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param inputParameterMap InputParameterMap; the parameter map to display
         */
        NetworksParameterDialog(final InputParameterMap inputParameterMap)
        {
            super(inputParameterMap);
        }

        /** {@inheritDoc} */
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void addParameterField(final JPanel panel, final InputParameter<?, ?> parameter)
        {
            if (parameter instanceof InputParameterSelectionMap<?, ?>)
            {
                this.fields.add(new InputFieldSelectionMapRadio(panel, (InputParameterSelectionMap<?, ?>) parameter));
            }
            else
            {
                super.addParameterField(panel, parameter);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void actionPerformed(final ActionEvent e)
        {
            boolean ok = true;
            try
            {
                for (InputField field : this.fields)
                {
                    if (field instanceof InputFieldSelectionMapRadio<?, ?>)
                    {
                        InputFieldSelectionMapRadio<?, ?> f = (InputFieldSelectionMapRadio<?, ?>) field;
                        f.getParameter().setObjectValue(f.getValue());
                    }
                }
            }
            catch (Exception exception)
            {
                JOptionPane.showMessageDialog(null, exception.getMessage(), "Data Entry Error", JOptionPane.ERROR_MESSAGE);
                ok = false;
            }
            if (ok)
            {
                super.actionPerformed(e);
            }
        }

        /**
         * Construct a tabbed parameter dialog that is not a part of a higher dialog.
         * @param inputParameterMap InputParameterMap; the parameter map to use
         * @return whether the data was entered correctly or not
         */
        public static boolean process(final InputParameterMap inputParameterMap)
        {
            NetworksParameterDialog dialog = new NetworksParameterDialog(inputParameterMap);
            return !dialog.stopped;
        }
    }

    /**
     * Radio button selection map.
     * @param <K> key of the selection map
     * @param <T> return type of the selection map
     */
    public static class InputFieldSelectionMapRadio<K, T> extends AbstractInputField
    {
        /** combo box for the user interface. */
        private List<JRadioButton> buttons = new ArrayList<>();

        /** combo box for the user interface. */
        private List<T> values = new ArrayList<>();

        /**
         * Create a string field on the screen.
         * @param panel JPanel; panel to add the field to
         * @param parameter InputParameterSelectionMap&lt;K,T&gt;; the parameter
         */
        public InputFieldSelectionMapRadio(final JPanel panel, final InputParameterSelectionMap<K, T> parameter)
        {
            super(parameter);
            Box box = Box.createVerticalBox();
            box.add(new JLabel("  "));
            box.add(new JLabel(parameter.getShortName()));
            ButtonGroup group = new ButtonGroup();
            for (K option : parameter.getOptions().keySet())
            {
                String item = option.toString();
                T value = parameter.getOptions().get(option);
                JRadioButton button = new JRadioButton(item);
                button.setActionCommand(item);
                if (value.equals(parameter.getDefaultValue()))
                {
                    button.setSelected(true);
                }
                group.add(button);
                box.add(button);
                this.buttons.add(button);
                this.values.add(value);
            }
            panel.add(box);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public InputParameterSelectionMap<K, T> getParameter()
        {
            return (InputParameterSelectionMap<K, T>) super.getParameter();
        }

        /** @return the mapped value of the field in the gui, selected by the key's toString() value. */
        public T getValue()
        {
            for (JRadioButton button : this.buttons)
            {
                if (button.isSelected())
                {
                    System.out.println("SELECTED: " + this.values.get(this.buttons.indexOf(button)));
                    return this.values.get(this.buttons.indexOf(button));
                }
            }
            return this.values.get(0);
        }
    }
}
