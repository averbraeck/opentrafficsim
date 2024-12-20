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

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.animation.GraphLaneUtil;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.OtsPlotScheduler;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterSelectionMap;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.AbstractInputField;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.InputField;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;
import nl.tudelft.simulation.language.DsolException;

/**
 * Simplest contour plots demonstration.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class NetworksSwing extends OtsSimulationApplication<NetworksModel> implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a Networks Swing application.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public NetworksSwing(final String title, final OtsAnimationPanel panel, final NetworksModel model)
    {
        super(model, panel);
        RoadNetwork network = model.getNetwork();
        System.out.println(network.getLinkMap());
    }

    @Override
    protected void addTabs()
    {
        addStatisticsTabs(getModel().getSimulator());
    }

    /**
     * Main program.
     * @param args the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("NetworksSwing");
            final NetworksModel otsModel = new NetworksModel(simulator);
            if (NetworksParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel,
                        HistoryManagerDevs.noHistory(simulator));
                OtsAnimationPanel animationPanel = new OtsAnimationPanel(otsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, otsModel, DEFAULT_COLORER, otsModel.getNetwork());
                NetworksSwing app = new NetworksSwing("Networks", animationPanel, otsModel);
                app.setExitOnClose(exitOnClose);
                animationPanel.enableSimulationControlButtons();
            }
            else
            {
                if (exitOnClose)
                {
                    System.exit(0);
                }
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | DsolException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add the statistics tabs.
     * @param simulator the simulator on which sampling can be scheduled
     */
    protected final void addStatisticsTabs(final OtsSimulatorInterface simulator)
    {
        int graphCount = getModel().pathCount();
        int columns = 1;
        int rows = 0 == columns ? 0 : (int) Math.ceil(graphCount * 1.0 / columns);
        TablePanel charts = new TablePanel(columns, rows);
        RoadSampler sampler = new RoadSampler(getModel().getNetwork());
        Duration updateInterval = Duration.instantiateSI(10.0);
        for (int graphIndex = 0; graphIndex < graphCount; graphIndex++)
        {
            List<Lane> start = new ArrayList<>();
            start.add(getModel().getPath(graphIndex).get(0));
            GraphPath<LaneDataRoad> path;
            try
            {
                path = GraphLaneUtil.createPath("name", start.get(0));
            }
            catch (NetworkException exception)
            {
                throw new RuntimeException(exception);
            }
            GraphPath.initRecording(sampler, path);
            SwingPlot plot = new SwingPlot(new TrajectoryPlot("Trajectories on lane " + (graphIndex + 1), updateInterval,
                    new OtsPlotScheduler(simulator), sampler.getSamplerData(), path));
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
         * @param inputParameterMap the parameter map to display
         */
        NetworksParameterDialog(final InputParameterMap inputParameterMap)
        {
            super(inputParameterMap);
        }

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
         * @param inputParameterMap the parameter map to use
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
         * @param panel panel to add the field to
         * @param parameter the parameter
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
