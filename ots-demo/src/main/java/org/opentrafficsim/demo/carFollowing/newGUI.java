package org.opentrafficsim.demo.carFollowing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;


//import org.jzy3d.chart.Chart;
//import org.jzy3d.chart.factories.AWTChartComponentFactory;
//import org.jzy3d.colors.Color;
//import org.jzy3d.maths.Coord3d;
//import org.jzy3d.plot3d.primitives.Scatter;
//import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.SpeedContourPlot;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Dec 11, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */

class VerticalMenuBar extends JMenuBar
{
    private static final LayoutManager grid = new GridLayout(0, 1);

    public VerticalMenuBar()
    {
        setLayout(grid);
    }
}

public class newGUI
{

    JFrame f = new JFrame("OpenTrafficSim GUI");

    JPanel networkPanel = new JPanel();

    JPanel modelPanel = new JPanel();

    JPanel inputPanel = new JPanel();

    JPanel outputPanel = new JPanel();

    JPanel settingPanel = new JPanel();

    JPanel simPanel = new JPanel();

    JPanel statPanel = new JPanel();

    JLabel networkLabel = new JLabel();

    JLabel modelLabel = new JLabel();

    JLabel networkDescriptor = new JLabel();

    ButtonGroup networkButtonGroup = new ButtonGroup();

    // the ButtonGroup for different network output settings, make sure only one RadioButton is selected
    ButtonGroup circularLaneBtnGroup = new ButtonGroup();

    ButtonGroup circularRoadBtnGroup = new ButtonGroup();

    ButtonGroup directLaneBtnGroup = new ButtonGroup();

    private JMenuBar mb_network = new JMenuBar();

    private JMenuBar mb_model = new JMenuBar();

    JMenu network = new JMenu("Network Settings");

    JMenu model = new JMenu("Model Settings");

    JMenuItem circularLane = new JMenuItem("Circular Lane");

    JMenuItem circularRoad = new JMenuItem("Circular Road");

    JMenuItem directLane = new JMenuItem("Direct Lane");

    JMenuItem customorize = new JMenuItem("Customorize");

    JMenu micro = new JMenu("Micro");

    JMenu macro = new JMenu("Macro");

    JMenuItem meta = new JMenuItem("Meta");

    JMenu CF = new JMenu("CF");

    JMenu LC = new JMenu("LC");

    JMenuItem IDM = new JMenuItem("IDM");

    JMenuItem Mobil = new JMenuItem("Mobil");

    JSpinner spinner1 = new JSpinner();

    JSpinner spinner2 = new JSpinner();

    JSpinner spinner3 = new JSpinner();

    JSpinner spinner4 = new JSpinner();

    JSpinner spinner5 = new JSpinner();

    private void settingPanel_conf()
    {
        networkPanel_conf();
        modelPanel_conf();

        this.settingPanel.setLayout(new BorderLayout());
        this.settingPanel.add(this.networkPanel, BorderLayout.NORTH);
        this.settingPanel.add(this.modelPanel, BorderLayout.CENTER);
        this.settingPanel.add(this.outputPanel, BorderLayout.SOUTH);
    }

    private void networkPanel_conf()
    {
        this.network.add(this.circularLane);
        this.network.addSeparator();
        this.network.add(this.circularRoad);
        this.network.addSeparator();
        this.network.add(this.directLane);
        this.network.addSeparator();
        this.network.add(this.customorize);

        this.networkLabel.setPreferredSize(new Dimension(40, 30));

        this.circularLane.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                newGUI.this.networkLabel.setText("Circular Lane Chosen");
                newGUI.this.networkDescriptor.setText("<html>" + "The description of Circular Lane Network" + "</html>");
                outputPanel_conf("circularLane");
            }
        });

        this.circularRoad.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                newGUI.this.networkLabel.setText("Circular Road Chosen");
                newGUI.this.networkDescriptor.setText("<html>" + "The description of Circular Road Network" + "</html>");
                outputPanel_conf("circularRoad");
            }
        });

        this.directLane.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                newGUI.this.networkLabel.setText("Direct Lane Chosen");
                newGUI.this.networkDescriptor.setText("<html>"
                    + "The default Network using the Internal ContourPlots Model for simulation and contour plots"
                    + "</html>");
                outputPanel_conf("DirectRoad");
            }
        });

        this.customorize.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                newGUI.this.networkLabel.setText("Not Available Currently");
            }
        });

        this.mb_network.add(this.network);

        this.networkPanel.setLayout(new BorderLayout());
        this.networkPanel.add(this.mb_network, BorderLayout.NORTH);
        this.networkPanel.add(this.networkLabel, BorderLayout.CENTER);

        this.networkDescriptor.setPreferredSize(new Dimension(40, 80));
        this.networkPanel.add(this.networkDescriptor, BorderLayout.SOUTH);
    }

    private void modelPanel_conf()
    {
        this.model.add(this.micro);
        this.model.addSeparator();
        this.model.add(this.macro);
        this.model.addSeparator();
        this.model.add(this.meta);

        this.IDM.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                inputPanel_conf("IDM");
                newGUI.this.modelLabel.setText("IDM");
            }
        });

        this.Mobil.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                inputPanel_conf("Mobil");
                newGUI.this.modelLabel.setText("Mobil");
            }
        });

        this.CF.add(this.IDM);
        this.LC.add(this.Mobil);
        this.micro.add(this.CF);
        this.micro.add(this.LC);

        this.mb_model.add(this.model);

        this.modelPanel.setLayout(new BorderLayout());
        // modelPanel.setPreferredSize(new Dimension(70, 80));
        this.modelPanel.add(this.mb_model, BorderLayout.NORTH);

        this.modelPanel.add(this.inputPanel, BorderLayout.CENTER);
    }

    private void inputPanel_conf(String model)
    {
        if (model.equals("IDM"))
        {
            this.inputPanel.removeAll();
            // set parameters of IDM model
            this.inputPanel.setBorder(new TitledBorder(new EtchedBorder(), "IDM Parameters"));
            setParameters("IDM a", "IDM b", "IDM S0", "IDM tSafe", "IDM delta");

        }
        else if (model.equals("Mobil"))
        {
            this.inputPanel.removeAll();
            this.inputPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mobil Parameters"));
            setParameters("mobil a", "mobil b", "mobil S0", "mobil tSate", "mobil delta");
            // set parameters of Mobil model
        }
        else
        {
            this.inputPanel.removeAll();
        }
    }

    private void setParameters(String first, String second, String third, String fourth, String fifth)
    {

        GroupLayout layout = new GroupLayout(this.inputPanel);
        this.inputPanel.setLayout(layout);

        // Turn on automatically adding gaps between components
        layout.setAutoCreateGaps(true);

        // Turn on automatically creating gaps between components that touch
        // the edge of the container and the container.
        layout.setAutoCreateContainerGaps(true);

        // Create a sequential group for the horizontal axis.

        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        // The sequential group in turn contains two parallel groups.
        // One parallel group contains the labels, the other the text fields.
        // Putting the labels in a parallel group along the horizontal axis
        // positions them at the same x location.
        //

        JLabel label1 = new JLabel(first);
        JLabel label2 = new JLabel(second);
        JLabel label3 = new JLabel(third);
        JLabel label4 = new JLabel(fourth);
        JLabel label5 = new JLabel(fifth);

        // set the initial value of parameters and the step size, the range can be updated as requirements
        SpinnerNumberModel spinnerModel1 = new SpinnerNumberModel(1.0, -10.0, 10.0, 0.1);
        SpinnerNumberModel spinnerModel2 = new SpinnerNumberModel(1.5, -10.0, 10.0, 0.1);
        SpinnerNumberModel spinnerModel3 = new SpinnerNumberModel(2.0, -10.0, 10.0, 0.1);
        SpinnerNumberModel spinnerModel4 = new SpinnerNumberModel(1.0, -10.0, 10.0, 0.1);
        SpinnerNumberModel spinnerModel5 = new SpinnerNumberModel(1.00, -10.0, 10.0, 0.01);
        // init the spninner

        this.spinner1.setModel(spinnerModel1);
        this.spinner2.setModel(spinnerModel2);
        this.spinner3.setModel(spinnerModel3);
        this.spinner4.setModel(spinnerModel4);
        this.spinner5.setModel(spinnerModel5);

        JSpinner.NumberEditor editor2 = (JSpinner.NumberEditor) this.spinner2.getEditor();
        DecimalFormat format2 = editor2.getFormat();
        format2.setMinimumFractionDigits(2);

        JSpinner.NumberEditor editor3 = (JSpinner.NumberEditor) this.spinner3.getEditor();
        DecimalFormat format3 = editor3.getFormat();
        format3.setMinimumFractionDigits(2);

        JSpinner.NumberEditor editor4 = (JSpinner.NumberEditor) this.spinner4.getEditor();
        DecimalFormat format4 = editor4.getFormat();
        format4.setMinimumFractionDigits(2);

        JSpinner.NumberEditor editor5 = (JSpinner.NumberEditor) this.spinner5.getEditor();
        DecimalFormat format5 = editor5.getFormat();
        format5.setMinimumFractionDigits(3);

        // Set its value
        this.spinner1.setValue(new Double(1.0));
        this.spinner2.setValue(new Double(1.5));
        this.spinner3.setValue(new Double(2.0));
        this.spinner4.setValue(new Double(1.0));
        this.spinner5.setValue(new Double(1.0));

        // Variable indentation is used to reinforce the level of grouping.
        hGroup.addGroup(layout.createParallelGroup().addComponent(label1).addComponent(label2).addComponent(label3)
            .addComponent(label4).addComponent(label5));
        hGroup.addGroup(layout.createParallelGroup().addComponent(this.spinner1).addComponent(this.spinner2).addComponent(
            this.spinner3).addComponent(this.spinner4).addComponent(this.spinner5));
        layout.setHorizontalGroup(hGroup);

        // Create a sequential group for the vertical axis.
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

        // The sequential group contains two parallel groups that align
        // the contents along the baseline. The first parallel group contains
        // the first label and text field, and the second parallel group contains
        // the second label and text field. By using a sequential group
        // the labels and text fields are positioned vertically after one another.
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label1).addComponent(this.spinner1));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label2).addComponent(this.spinner2));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label3).addComponent(this.spinner3));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label4).addComponent(this.spinner4));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label5).addComponent(this.spinner5));
        layout.setVerticalGroup(vGroup);
    }

    // change the output options based on the network and the model
    JCheckBox showNewFrame = new JCheckBox("Show All Statistis in New Frame ");

    private void outputPanel_conf(String network)
    {

        this.outputPanel.setLayout(new BoxLayout(this.outputPanel, BoxLayout.Y_AXIS));
        // clean the panel and all the output radio buttongroup
        this.outputPanel.removeAll();

        switch (network)
        {
            case "circularLane":
                addOutputButton("Statistics in 2D", network);
                addOutputButton("Statistics in 3D", network);
                this.outputPanel.add(this.showNewFrame, network);
                break;
            case "circularRoad":
                addOutputButton("Statistics for Lane 1 in 2D", network);
                addOutputButton("Statistics for Lane 2 in 2D", network);
                addOutputButton("Statistics for Lane 1 in 3D", network);
                addOutputButton("Statistics for Lane 2 in 3D", network);
                this.outputPanel.add(this.showNewFrame);
                break;
            case "directRoad":
                addOutputButton("Statistics in 2D", network);
                addOutputButton("Statistics in 3D", network);
                this.outputPanel.add(this.showNewFrame);
                break;
            case "customorize":
                break;
            default:
                break;

        }

        JButton done_Btn = new JButton("Done");
        this.outputPanel.add(done_Btn);
        done_Btn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if ((!newGUI.this.networkLabel.getText().equals("")) && (!newGUI.this.modelLabel.getText().equals("")))
                {
                    try
                    {
                        simPanel_conf(newGUI.this.networkLabel.getText());
                    }
                    catch (RemoteException | SimRuntimeException exception)
                    {
                        exception.printStackTrace();
                    }

                }
                else
                {
                    JOptionPane.showMessageDialog(newGUI.this.f, "The setting is not compeleted");

                }
            }
        });

    }

    void simPanel_conf(String network) throws RemoteException, SimRuntimeException
    {
        this.simPanel.setLayout(new BorderLayout());
        this.simPanel.removeAll();
        this.statPanel.removeAll();

        if (network.equals("Circular Lane Chosen"))
        {
            LaneSimulationModel model = new LaneSimulationModel(new ArrayList<AbstractProperty<?>>());
            SimpleSimulator simSimulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                    new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                        TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 1000, 1000));
            this.simPanel.add(simSimulator.getPanel(), BorderLayout.CENTER);

            ContourPlot cp;

            cp =
                new DensityContourPlot("DensityPlot " + model.carFollowingModelCars.getLongName(), model
                    .getMinimumDistance(), model.lane1.getLength());
            cp.setTitle("Density Contour Graph");

            model.getPlots().add(cp);
            this.statPanel.add(cp.getContentPane());

            cp =
                new SpeedContourPlot("SpeedPlot " + model.carFollowingModelCars.getLongName(), model.getMinimumDistance(),
                    model.lane1.getLength());
            cp.setTitle("Speed Contour Graph");
            model.getPlots().add(cp);
            this.statPanel.add(cp.getContentPane());

            cp =
                new FlowContourPlot("FlowPlot " + model.carFollowingModelCars.getLongName(), model.getMinimumDistance(),
                    model.lane1.getLength());
            cp.setTitle("FLow Contour Graph");
            model.getPlots().add(cp);
            this.statPanel.add(cp.getContentPane());

            cp =
                new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModelCars.getLongName(), model
                    .getMinimumDistance(), model.lane1.getLength());
            cp.setTitle("Acceleration Contour Graph");
            model.getPlots().add(cp);
            this.statPanel.add(cp.getContentPane());

            List<Lane> path = new ArrayList<Lane>();
            path.add(model.lane1);
            TrajectoryPlot trajectoryPlot =
                new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModelCars.getLongName(),
                    new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), path);
            trajectoryPlot.setTitle("Trajectories");
            this.statPanel.add(trajectoryPlot.getContentPane());
            model.getTrajectoryPlots().add(trajectoryPlot);

            if (this.showNewFrame.isSelected())
            {
                createCircularLaneFrame(model);
            }
        }
        else if (network.equals("Circular Road Chosen"))
        {
            RoadSimulationModel model = new RoadSimulationModel(new ArrayList<AbstractProperty<?>>());
            SimpleSimulator simSimulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                    new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                        TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 1000, 1000));
            this.simPanel.add(simSimulator.getPanel(), BorderLayout.CENTER);

            ContourPlot cp;

            for (int laneIndex = 0; laneIndex <= 0; laneIndex++)
            {
                final String laneName = String.format(" lane %d", laneIndex + 1);
                cp =
                    new DensityContourPlot(
                        "DensityPlot " + model.carFollowingModelCars.getLongName() + " lane " + laneIndex, model
                            .getMinimumDistance(), model.lanes[laneIndex].getLength());
                cp.setTitle("Density Contour Graph");
                // cp.setExtendedState(MAXIMIZED_BOTH);
                model.getPlots().get(laneIndex).add(cp);
                this.statPanel.add(cp.getContentPane());

                cp =
                    new SpeedContourPlot("SpeedPlot " + model.carFollowingModelCars.getLongName() + laneName, model
                        .getMinimumDistance(), model.lanes[laneIndex].getLength());
                cp.setTitle("Speed Contour Graph");
                model.getPlots().get(laneIndex).add(cp);
                this.statPanel.add(cp.getContentPane());

                cp =
                    new FlowContourPlot("FlowPlot " + model.carFollowingModelCars.getLongName() + laneName, model
                        .getMinimumDistance(), model.lanes[laneIndex].getLength());
                cp.setTitle("FLow Contour Graph");
                model.getPlots().get(laneIndex).add(cp);
                this.statPanel.add(cp.getContentPane());

                cp =
                    new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModelCars.getLongName() + laneName,
                        model.getMinimumDistance(), model.lanes[laneIndex].getLength());
                cp.setTitle("Acceleration Contour Graph");
                model.getPlots().get(laneIndex).add(cp);
                this.statPanel.add(cp.getContentPane());

                List<Lane> path = new ArrayList<Lane>();
                path.add(model.lanes[laneIndex]);
                TrajectoryPlot trajectoryPlot =
                    new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModelCars.getLongName() + laneName,
                        new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), path);
                trajectoryPlot.setTitle("Trajectories");
                this.statPanel.add(trajectoryPlot.getContentPane());
                model.getPlots().get(laneIndex).add(trajectoryPlot);
            }

            if (this.showNewFrame.isSelected())
            {
                createCircularRoadFrame(model);
            }

        }
        else if (network.equals("Direct Lane Chosen"))
        {
            StraightModel model = new StraightModel(new ArrayList<AbstractProperty<?>>());
            SimpleSimulator simSimulator =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                    new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0,
                        TimeUnit.SECOND), model, new Rectangle2D.Double(0, -100, 5000, 200));
            this.simPanel.add(simSimulator.getPanel(), BorderLayout.CENTER);

            ContourPlot cp;

            cp = new DensityContourPlot("DensityPlot", model.getMinimumDistance(), model.getMaximumDistance());
            cp.setTitle("Density Contour Graph");
            // cp.setExtendedState(MAXIMIZED_BOTH);
            model.getPlots().add(cp);
            this.statPanel.add(cp.getContentPane());

            cp = new SpeedContourPlot("SpeedPlot", model.getMinimumDistance(), model.getMaximumDistance());
            cp.setTitle("Speed Contour Graph");
            model.getPlots().add(cp);
            this.statPanel.add(cp.getContentPane());

            cp = new FlowContourPlot("FlowPlot", model.getMinimumDistance(), model.getMaximumDistance());
            cp.setTitle("FLow Contour Graph");
            model.getPlots().add(cp);
            this.statPanel.add(cp.getContentPane());

            cp = new AccelerationContourPlot("AccelerationPlot", model.getMinimumDistance(), model.getMaximumDistance());
            cp.setTitle("Acceleration Contour Graph");
            model.getPlots().add(cp);
            this.statPanel.add(cp.getContentPane());

            if (this.showNewFrame.isSelected())
            {
                createDirectLaneFrame(model);
            }
        }
        SwingUtilities.updateComponentTreeUI(this.f);
    }

    // List<JCheckBox> checkboxList=new ArrayList<JCheckBox>();

    private void addOutputButton(String label, String network)
    {
        this.outputPanel.setBorder(new TitledBorder(new EtchedBorder(), "Set the Output Mode"));
        JRadioButton button = new JRadioButton(label);
        this.outputPanel.add(button);
        switch (network)
        {
            case "circularLane":
                this.circularLaneBtnGroup.add(button);
                // set the first radio button is default choice
                if (this.circularLaneBtnGroup.getButtonCount() == 0)
                    button.setSelected(true);
                break;
            case "circularRoad":
                this.circularRoadBtnGroup.add(button);
                if (this.circularRoadBtnGroup.getButtonCount() == 0)
                    button.setSelected(true);
                break;
            case "directLane":
                this.directLaneBtnGroup.add(button);
                if (this.directLaneBtnGroup.getButtonCount() == 0)
                    button.setSelected(true);
                break;
            default:
                break;
        }
        // checkboxList.add(button);
    }

    JFrame separateFrame;

    private void createCircularLaneFrame(LaneSimulationModel model)
    {

        if (this.separateFrame == null)
        {
            this.separateFrame = new JFrame("Statistics Plots");
            this.separateFrame.setSize(800, 600);
        }
        else
        {
            // remove the previous JFrame
            this.separateFrame.setVisible(false);
            this.separateFrame.dispose();
            // create a new one
            this.separateFrame = new JFrame("Statistics Plots");
            this.separateFrame.setSize(800, 600);
        }

        TablePanel charts = new TablePanel(3, 2);

        // Make the four contour plots
        ContourPlot cp;

        cp =
            new DensityContourPlot("DensityPlot " + model.carFollowingModelCars.getLongName(), model.getMinimumDistance(),
                model.lane1.getLength());
        cp.setTitle("Density Contour Graph");
        // cp.setExtendedState(MAXIMIZED_BOTH);
        model.getPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 0);

        cp =
            new SpeedContourPlot("SpeedPlot " + model.carFollowingModelCars.getLongName(), model.getMinimumDistance(),
                model.lane1.getLength());
        cp.setTitle("Speed Contour Graph");
        model.getPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 0);

        cp =
            new FlowContourPlot("FlowPlot " + model.carFollowingModelCars.getLongName(), model.getMinimumDistance(),
                model.lane1.getLength());
        cp.setTitle("FLow Contour Graph");
        model.getPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 1);

        cp =
            new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModelCars.getLongName(), model
                .getMinimumDistance(), model.lane1.getLength());
        cp.setTitle("Acceleration Contour Graph");
        model.getPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 1);

        List<Lane> path = new ArrayList<Lane>();
        path.add(model.lane1);
        TrajectoryPlot trajectoryPlot =
            new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModelCars.getLongName(),
                new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), path);
        trajectoryPlot.setTitle("Trajectories");
        charts.setCell(trajectoryPlot.getContentPane(), 2, 0);
        model.getTrajectoryPlots().add(trajectoryPlot);

        this.separateFrame.add(charts);
        this.separateFrame.setVisible(true);
        this.separateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void createCircularRoadFrame(RoadSimulationModel model)
    {
        if (this.separateFrame == null)
        {
            this.separateFrame = new JFrame("Statistics Plots");
            this.separateFrame.setSize(800, 600);
        }
        else
        {
            // remove the previous JFrame
            this.separateFrame.setVisible(false);
            this.separateFrame.dispose();
            // create a new one
            this.separateFrame = new JFrame("Statistics Plots");
            this.separateFrame.setSize(800, 600);
        }

        TablePanel charts = new TablePanel(4, 3);
        ContourPlot cp;

        for (int laneIndex = 0; laneIndex <= 1; laneIndex++)
        {
            final String laneName = String.format(" lane %d", laneIndex + 1);
            cp =
                new DensityContourPlot("DensityPlot " + model.carFollowingModelCars.getLongName() + " lane " + laneIndex,
                    model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Density Contour Graph");
            // cp.setExtendedState(MAXIMIZED_BOTH);
            model.getPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex, 0);

            cp =
                new SpeedContourPlot("SpeedPlot " + model.carFollowingModelCars.getLongName() + laneName, model
                    .getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Speed Contour Graph");
            model.getPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex + 1, 0);

            cp =
                new FlowContourPlot("FlowPlot " + model.carFollowingModelCars.getLongName() + laneName, model
                    .getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("FLow Contour Graph");
            model.getPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex, 1);

            cp =
                new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModelCars.getLongName() + laneName,
                    model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Acceleration Contour Graph");
            model.getPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex + 1, 1);

            List<Lane> path = new ArrayList<Lane>();
            path.add(model.lanes[laneIndex]);
            TrajectoryPlot trajectoryPlot =
                new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModelCars.getLongName() + laneName,
                    new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), path);
            trajectoryPlot.setTitle("Trajectories");
            charts.setCell(trajectoryPlot.getContentPane(), 1 + laneIndex, 2);
            model.getPlots().get(laneIndex).add(trajectoryPlot);
        }
        this.separateFrame.add(charts);
        this.separateFrame.setVisible(true);
        this.separateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void createDirectLaneFrame(StraightModel model)
    {
        if (this.separateFrame == null)
        {
            this.separateFrame = new JFrame("Statistics Plots");
            this.separateFrame.setSize(800, 600);
        }
        else
        {
            // remove the previous JFrame
            this.separateFrame.setVisible(false);
            this.separateFrame.dispose();
            // create a new one
            this.separateFrame = new JFrame("Statistics Plots");
            this.separateFrame.setSize(800, 600);
        }

        TablePanel charts = new TablePanel(2, 2);

        // Make the four contour plots
        ContourPlot cp;

        cp = new DensityContourPlot("DensityPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Density Contour Graph");
        // cp.setExtendedState(MAXIMIZED_BOTH);
        model.getPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 0);

        cp = new SpeedContourPlot("SpeedPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Speed Contour Graph");
        model.getPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 0);

        cp = new FlowContourPlot("FlowPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("FLow Contour Graph");
        model.getPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 1);

        cp = new AccelerationContourPlot("AccelerationPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Acceleration Contour Graph");
        model.getPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 1);
        this.separateFrame.setVisible(true);
        this.separateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void init() throws RemoteException, SimRuntimeException
    {

        settingPanel_conf();
        this.f.add(this.settingPanel, BorderLayout.WEST);
        this.simPanel.setPreferredSize(new Dimension(400, 800));
        this.f.add(this.simPanel, BorderLayout.CENTER);
        this.statPanel.setLayout(new GridLayout(2, 2, 3, 3));
        this.statPanel.setPreferredSize(new Dimension(500, 800));

        this.f.add(this.statPanel, BorderLayout.EAST);

        this.f.setSize(1000, 800);
        this.f.setVisible(true);
        this.f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) throws RemoteException, SimRuntimeException
    {
        // Create the application
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    new newGUI().init();
                }
                catch (RemoteException | SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }
}
