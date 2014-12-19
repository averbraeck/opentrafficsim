package org.opentrafficsim.demo.IDMPlus.swing;




import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.SpeedContourPlot;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.simulationengine.ControlPanel;
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

class VerticalMenuBar extends JMenuBar {
    private static final LayoutManager grid = new GridLayout(0,1);
    public VerticalMenuBar() {
      setLayout(grid);
    }
  }


public class newGUI
{
    
    JFrame f=new JFrame("OpenTrafficSim GUI");
    
    JPanel networkPanel=new JPanel();
    JPanel modelPanel=new JPanel();
    JPanel inputPanel=new JPanel();
    JPanel outputPanel=new JPanel();
    
    JPanel settingPanel=new JPanel();
    JPanel simPanel=new JPanel();
    JPanel statPanel=new JPanel();
    
    JLabel networkLabel=new JLabel();
    JLabel modelLabel=new JLabel();
    JLabel networkDescriptor=new JLabel();
   
    
    ButtonGroup networkButtonGroup=new ButtonGroup();
    
    //the ButtonGroup for different network output settings, make sure only one RadioButton is selected
    ButtonGroup circularLaneBtnGroup=new ButtonGroup();
    ButtonGroup circularRoadBtnGroup=new ButtonGroup();
    ButtonGroup directLaneBtnGroup=new ButtonGroup();
    
    private JMenuBar mb_network=new JMenuBar();
    private JMenuBar mb_model=new JMenuBar();
    JMenu network=new JMenu("Network Settings");
    JMenu model=new JMenu("Model Settings");
    
    
    JMenuItem circularLane=new JMenuItem("Circular Lane");
    JMenuItem circularRoad=new JMenuItem("Circular Road");
    JMenuItem directLane=new JMenuItem("Direct Lane");
    JMenuItem customorize=new JMenuItem("Customorize");
    
    JMenu micro=new JMenu("Micro");
    JMenu macro=new JMenu("Macro");
    JMenuItem meta=new JMenuItem("Meta");
    
    JMenu CF=new JMenu("CF");
    JMenu LC=new JMenu("LC");
    
    JMenuItem IDM=new JMenuItem("IDM");
    JMenuItem Mobil=new JMenuItem("Mobil");
    
    
    JSpinner spinner1 = new JSpinner();
    JSpinner spinner2 = new JSpinner();
    JSpinner spinner3 = new JSpinner();
    JSpinner spinner4 = new JSpinner();
    JSpinner spinner5 = new JSpinner();
    
    private void settingPanel_conf()
    {
        networkPanel_conf();
        modelPanel_conf();
        
        
        settingPanel.setLayout(new BorderLayout());
        settingPanel.add(networkPanel, BorderLayout.NORTH);
        settingPanel.add(modelPanel, BorderLayout.CENTER);
        settingPanel.add(outputPanel,BorderLayout.SOUTH);
    }
    
    private void networkPanel_conf()
    {
        network.add(circularLane);
        network.addSeparator();
        network.add(circularRoad);
        network.addSeparator();
        network.add(directLane);
        network.addSeparator();
        network.add(customorize);
        
        networkLabel.setPreferredSize(new Dimension(40,30));
        
        circularLane.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                networkLabel.setText("Circular Lane Choosen");
                networkDescriptor.setText("<html>"+"The description of Circular Lane Network"+"</html>");
                outputPanel_conf("circularLane");
            }
        });
        
        circularRoad.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                networkLabel.setText("Circular Road Choosen");
                networkDescriptor.setText("<html>"+"The description of Circular Road Network"+"</html>");
                outputPanel_conf("circularRoad");
            }
        });
        
        directLane.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                networkLabel.setText("Direct Lane Choosen");
                networkDescriptor.setText("<html>"+
                "The default Network using the Internal ContourPlots Model for simulation and contour plots"+"</html>");
                outputPanel_conf("DirectRoad");
            }
        });
        
        customorize.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                networkLabel.setText("Not Available Currently");
            }
        });
        
        mb_network.add(network);
        
        networkPanel.setLayout(new BorderLayout());
        networkPanel.add(mb_network, BorderLayout.NORTH);
        networkPanel.add(networkLabel, BorderLayout.CENTER);
        
        networkDescriptor.setPreferredSize(new Dimension(40,80));
        networkPanel.add(networkDescriptor, BorderLayout.SOUTH);
    }
    
    private void modelPanel_conf()
    {
        model.add(micro);
        model.addSeparator();
        model.add(macro);
        model.addSeparator();
        model.add(meta);
        
        
        IDM.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                inputPanel_conf("IDM");
                modelLabel.setText("IDM");
            }
        });
        
        Mobil.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                inputPanel_conf("Mobil");
                modelLabel.setText("Mobil");
            }
        });
      
        CF.add(IDM);
        LC.add(Mobil);
        micro.add(CF);
        micro.add(LC);
        
        mb_model.add(model);
        
        modelPanel.setLayout(new BorderLayout());
       // modelPanel.setPreferredSize(new Dimension(70, 80));
        modelPanel.add(mb_model, BorderLayout.NORTH);
 
        modelPanel.add(inputPanel, BorderLayout.CENTER);
    }
    
    private void inputPanel_conf(String model)
    {
        if(model.equals("IDM"))
        {
            inputPanel.removeAll();
            // set parameters of IDM model
            inputPanel.setBorder(new TitledBorder(new EtchedBorder(),"IDM Parameters"));
            setParameters("IDM a","IDM b","IDM S0","IDM tSafe","IDM delta");
            
        }
        else if(model.equals("Mobil"))
        {
            inputPanel.removeAll();
            inputPanel.setBorder(new TitledBorder(new EtchedBorder(),"Mobil Parameters"));
            setParameters("mobil a","mobil b","mobil S0","mobil tSate", "mobil delta");
            // set parameters of Mobil model
        }
        else
        {
            inputPanel.removeAll();
        }
    }
    
    private void setParameters(String first, String second, String third, String fourth, String fifth)
    {
      
        GroupLayout layout = new GroupLayout(inputPanel);
        inputPanel.setLayout(layout);

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
        
        JLabel label1=new JLabel(first);
        JLabel label2=new JLabel(second);
        JLabel label3=new JLabel(third);
        JLabel label4=new JLabel(fourth);
        JLabel label5=new JLabel(fifth);
        
        // set the initial value of parameters and the step size, the range can be updated as requirements
        SpinnerNumberModel spinnerModel1 = new SpinnerNumberModel(1.0, -10.0, 10.0, 0.1);
        SpinnerNumberModel spinnerModel2 = new SpinnerNumberModel(1.5, -10.0, 10.0, 0.1);
        SpinnerNumberModel spinnerModel3 = new SpinnerNumberModel(2.0, -10.0, 10.0, 0.1);
        SpinnerNumberModel spinnerModel4 = new SpinnerNumberModel(1.0, -10.0, 10.0, 0.1);
        SpinnerNumberModel spinnerModel5 = new SpinnerNumberModel(1.00, -10.0, 10.0, 0.01);
        //init the spninner
        
        
        spinner1.setModel(spinnerModel1);
        spinner2.setModel(spinnerModel2);
        spinner3.setModel(spinnerModel3);
        spinner4.setModel(spinnerModel4);
        spinner5.setModel(spinnerModel5);

        JSpinner.NumberEditor editor2 = (JSpinner.NumberEditor)spinner2.getEditor();  
        DecimalFormat format2 = editor2.getFormat();  
        format2.setMinimumFractionDigits(2); 

        JSpinner.NumberEditor editor3 = (JSpinner.NumberEditor)spinner3.getEditor();  
        DecimalFormat format3 = editor3.getFormat();  
        format3.setMinimumFractionDigits(2); 

        JSpinner.NumberEditor editor4 = (JSpinner.NumberEditor)spinner4.getEditor();  
        DecimalFormat format4 = editor4.getFormat();  
        format4.setMinimumFractionDigits(2); 

        JSpinner.NumberEditor editor5 = (JSpinner.NumberEditor)spinner5.getEditor();  
        DecimalFormat format5 = editor5.getFormat();  
        format5.setMinimumFractionDigits(3); 

        
        
        // Set its value
        spinner1.setValue(new Double(1.0));
        spinner2.setValue(new Double(1.5));
        spinner3.setValue(new Double(2.0));
        spinner4.setValue(new Double(1.0));
        spinner5.setValue(new Double(1.0));
        
        // Variable indentation is used to reinforce the level of grouping.
        hGroup.addGroup(layout.createParallelGroup().
                 addComponent(label1).addComponent(label2).addComponent(label3).addComponent(label4).addComponent(label5));
        hGroup.addGroup(layout.createParallelGroup().
                 addComponent(spinner1).addComponent(spinner2).addComponent(spinner3).addComponent(spinner4).addComponent(spinner5));
        layout.setHorizontalGroup(hGroup);

        // Create a sequential group for the vertical axis.
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

        // The sequential group contains two parallel groups that align
        // the contents along the baseline. The first parallel group contains
        // the first label and text field, and the second parallel group contains
        // the second label and text field. By using a sequential group
        // the labels and text fields are positioned vertically after one another.
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                 addComponent(label1).addComponent(spinner1));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                 addComponent(label2).addComponent(spinner2));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                addComponent(label3).addComponent(spinner3));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                addComponent(label4).addComponent(spinner4));
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                addComponent(label5).addComponent(spinner5));
        layout.setVerticalGroup(vGroup);
    }
    
    // change the output options based on the network and the model
    JCheckBox showNewFrame=new JCheckBox("Show All Statistis in New Frame ");
    private void outputPanel_conf(String network)
    {
        
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        //clean the panel and all the output radio buttongroup
        outputPanel.removeAll();
        
        switch(network)
        {
            case "circularLane":
                addOutputButton("Statistics in 2D",network);
                addOutputButton("Statistics in 3D", network);
                outputPanel.add(showNewFrame, network);
                break;
            case "circularRoad":
                addOutputButton("Statistics for Lane 1 in 2D", network);
                addOutputButton("Statistics for Lane 2 in 2D", network);
                addOutputButton("Statistics for Lane 1 in 3D", network);
                addOutputButton("Statistics for Lane 2 in 3D", network);
                outputPanel.add(showNewFrame);
                break;
            case "directRoad":
                addOutputButton("Statistics in 2D", network);
                addOutputButton("Statistics in 3D", network);
                outputPanel.add(showNewFrame);
                break;
            case "customorize":
                break;
            default:
                break;
                    
        }
        
        JButton done_Btn=new JButton("Done");
        outputPanel.add(done_Btn);
        done_Btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                if((!networkLabel.getText().equals(""))&&(!modelLabel.getText().equals("")))
                {
                    try
                    {
                        simPanel_conf(networkLabel.getText());
                    }
                    catch (RemoteException | SimRuntimeException exception)
                    {
                        exception.printStackTrace();
                    }
                    
                }
                else
                {
                    JOptionPane.showMessageDialog(f, "The setting is not compeleted");
                    
                }
            }
        });
        
    }
    
    private void simPanel_conf(String network) throws RemoteException, SimRuntimeException
    {
        simPanel.setLayout(new BorderLayout());
        simPanel.removeAll();
        statPanel.removeAll();
        
        if(network.equals("Circular Lane Choosen"))
        {
            LaneSimulationModel model=new LaneSimulationModel();
            SimpleSimulator simSimulator= new SimpleSimulator(new OTSSimTimeDouble(
                            new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)), new DoubleScalar.Rel<TimeUnit>(0.0,
                                 TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model,
                                 new Rectangle2D.Double(-1000, -1000, 1000, 1000));
            simPanel.add(simSimulator.getPanel(),BorderLayout.CENTER);   
            
           
            ContourPlot cp;

            cp =
                    new DensityContourPlot("DensityPlot " + model.carFollowingModel.getLongName(),
                            model.getMinimumDistance(), model.lane.getLength());
            cp.setTitle("Density Contour Graph");
          
            model.getContourPlots().add(cp);
            statPanel.add(cp.getContentPane());

            cp =
                    new SpeedContourPlot("SpeedPlot " + model.carFollowingModel.getLongName(), model.getMinimumDistance(),
                            model.lane.getLength());
            cp.setTitle("Speed Contour Graph");
            model.getContourPlots().add(cp);
            statPanel.add(cp.getContentPane());

            cp =
                    new FlowContourPlot("FlowPlot " + model.carFollowingModel.getLongName(), model.getMinimumDistance(),
                            model.lane.getLength());
            cp.setTitle("FLow Contour Graph");
            model.getContourPlots().add(cp);
            statPanel.add(cp.getContentPane());

            cp =
                    new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModel.getLongName(),
                            model.getMinimumDistance(), model.lane.getLength());
            cp.setTitle("Acceleration Contour Graph");
            model.getContourPlots().add(cp);
            statPanel.add(cp.getContentPane());

            TrajectoryPlot trajectoryPlot =
                    new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModel.getLongName(),
                            new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), model.getMinimumDistance(),
                            model.lane.getLength());
            trajectoryPlot.setTitle("Trajectories");
            statPanel.add(trajectoryPlot.getContentPane());
            model.getTrajectoryPlots().add(trajectoryPlot);
           
            if(showNewFrame.isSelected())
            {
                createCircularLaneFrame(model);
            }
        }
        else if(network.equals("Circular Road Choosen"))
        {
            RoadSimulationModel model = new RoadSimulationModel();
            SimpleSimulator simSimulator=new SimpleSimulator(new OTSSimTimeDouble(
                            new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)), new DoubleScalar.Rel<TimeUnit>(0.0,
                                 TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model,
                                 new Rectangle2D.Double(-1000, -1000, 1000, 1000));
            simPanel.add(simSimulator.getPanel(), BorderLayout.CENTER);
            
            ContourPlot cp;

            for (int laneIndex = 0; laneIndex <= 0; laneIndex++)
            {
                final String laneName = String.format(" lane %d", laneIndex + 1);
                cp =
                        new DensityContourPlot("DensityPlot " + model.carFollowingModel.getLongName() + " lane "
                                + laneIndex, model.getMinimumDistance(), model.lanes[laneIndex].getLength());
                cp.setTitle("Density Contour Graph");
               // cp.setExtendedState(MAXIMIZED_BOTH);
                model.getContourPlots().get(laneIndex).add(cp);
                statPanel.add(cp.getContentPane());

                cp =
                        new SpeedContourPlot("SpeedPlot " + model.carFollowingModel.getLongName() + laneName,
                                model.getMinimumDistance(), model.lanes[laneIndex].getLength());
                cp.setTitle("Speed Contour Graph");
                model.getContourPlots().get(laneIndex).add(cp);
                statPanel.add(cp.getContentPane());

                cp =
                        new FlowContourPlot("FlowPlot " + model.carFollowingModel.getLongName() + laneName,
                                model.getMinimumDistance(), model.lanes[laneIndex].getLength());
                cp.setTitle("FLow Contour Graph");
                model.getContourPlots().get(laneIndex).add(cp);
                statPanel.add(cp.getContentPane());

                cp =
                        new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModel.getLongName() + laneName,
                                model.getMinimumDistance(), model.lanes[laneIndex].getLength());
                cp.setTitle("Acceleration Contour Graph");
                model.getContourPlots().get(laneIndex).add(cp);
                statPanel.add(cp.getContentPane());

                TrajectoryPlot trajectoryPlot =
                        new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModel.getLongName() + laneName,
                                new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), model.getMinimumDistance(),
                                model.lanes[laneIndex].getLength());
                trajectoryPlot.setTitle("Trajectories");
                statPanel.add(trajectoryPlot.getContentPane());
                model.getTrajectoryPlots().get(laneIndex).add(trajectoryPlot);
            }
            
            if(showNewFrame.isSelected())
            {
                createCircularRoadFrame(model);
            }
            
        }
        else if(network.equals("Direct Lane Choosen"))
        {
            InternalContourPlotsModel model = new InternalContourPlotsModel();
            SimpleSimulator simSimulator= new SimpleSimulator(new OTSSimTimeDouble(
                            new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)), new DoubleScalar.Rel<TimeUnit>(0.0,
                            TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800.0, TimeUnit.SECOND), model,
                            new Rectangle2D.Double(0, -100, 5000, 200));
            simPanel.add(simSimulator.getPanel(), BorderLayout.CENTER);
            
            ContourPlot cp;

            cp = new DensityContourPlot("DensityPlot", model.getMinimumDistance(), model.getMaximumDistance());
            cp.setTitle("Density Contour Graph");
            //cp.setExtendedState(MAXIMIZED_BOTH);
            model.getContourPlots().add(cp);
            statPanel.add(cp.getContentPane());

            cp = new SpeedContourPlot("SpeedPlot", model.getMinimumDistance(), model.getMaximumDistance());
            cp.setTitle("Speed Contour Graph");
            model.getContourPlots().add(cp);
            statPanel.add(cp.getContentPane());

            cp = new FlowContourPlot("FlowPlot", model.getMinimumDistance(), model.getMaximumDistance());
            cp.setTitle("FLow Contour Graph");
            model.getContourPlots().add(cp);
            statPanel.add(cp.getContentPane());

            cp = new AccelerationContourPlot("AccelerationPlot", model.getMinimumDistance(), model.getMaximumDistance());
            cp.setTitle("Acceleration Contour Graph");
            model.getContourPlots().add(cp);
            statPanel.add(cp.getContentPane());
            
            if(showNewFrame.isSelected())
            {
                createDirectLaneFrame(model);
            }
        }
        SwingUtilities.updateComponentTreeUI(f);
    }
    
    
    //List<JCheckBox> checkboxList=new ArrayList<JCheckBox>();
    
    private void addOutputButton(String label, String network)
    {
        outputPanel.setBorder(new TitledBorder(new EtchedBorder(),"Set the Output Mode"));
        JRadioButton button=new JRadioButton(label);
        outputPanel.add(button); 
        switch(network)
        {
            case "circularLane":
                circularLaneBtnGroup.add(button);
                // set the first radio button is default choice
                if(circularLaneBtnGroup.getButtonCount()==0)
                    button.setSelected(true);
                break;     
            case "circularRoad":
                circularRoadBtnGroup.add(button);
                if(circularRoadBtnGroup.getButtonCount()==0)
                    button.setSelected(true);
                break;
            case "directLane":
                directLaneBtnGroup.add(button);
                if(directLaneBtnGroup.getButtonCount()==0)
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
        
        if(separateFrame == null)
        {
            separateFrame=new JFrame("Statistics Plots");
            separateFrame.setSize(800, 600);
        }
        else {
            //remove the previous JFrame
            separateFrame.setVisible(false);
            separateFrame.dispose();
            //create a new one
            separateFrame = new JFrame("Statistics Plots");
            separateFrame.setSize(800, 600);
        }
        
        TablePanel charts = new TablePanel(3, 2);
        
        // Make the four contour plots
        ContourPlot cp;

        cp =
                new DensityContourPlot("DensityPlot " + model.carFollowingModel.getLongName(),
                        model.getMinimumDistance(), model.lane.getLength());
        cp.setTitle("Density Contour Graph");
       // cp.setExtendedState(MAXIMIZED_BOTH);
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 0);

        cp =
                new SpeedContourPlot("SpeedPlot " + model.carFollowingModel.getLongName(), model.getMinimumDistance(),
                        model.lane.getLength());
        cp.setTitle("Speed Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 0);

        cp =
                new FlowContourPlot("FlowPlot " + model.carFollowingModel.getLongName(), model.getMinimumDistance(),
                        model.lane.getLength());
        cp.setTitle("FLow Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 1);

        cp =
                new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModel.getLongName(),
                        model.getMinimumDistance(), model.lane.getLength());
        cp.setTitle("Acceleration Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 1);

        TrajectoryPlot trajectoryPlot =
                new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModel.getLongName(),
                        new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), model.getMinimumDistance(),
                        model.lane.getLength());
        trajectoryPlot.setTitle("Trajectories");
        charts.setCell(trajectoryPlot.getContentPane(), 2, 0);
        model.getTrajectoryPlots().add(trajectoryPlot);
        
        separateFrame.add(charts);
        separateFrame.setVisible(true);
        separateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void createCircularRoadFrame(RoadSimulationModel model)
    {
        if(separateFrame == null)
        {
            separateFrame=new JFrame("Statistics Plots");
            separateFrame.setSize(800, 600);
        }
        else {
            //remove the previous JFrame
            separateFrame.setVisible(false);
            separateFrame.dispose();
            //create a new one
            separateFrame = new JFrame("Statistics Plots");
            separateFrame.setSize(800, 600);
        }
        
        TablePanel charts = new TablePanel(4, 3);
        ContourPlot cp;

        for (int laneIndex = 0; laneIndex <= 1; laneIndex++)
        {
            final String laneName = String.format(" lane %d", laneIndex + 1);
            cp =
                    new DensityContourPlot("DensityPlot " + model.carFollowingModel.getLongName() + " lane "
                            + laneIndex, model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Density Contour Graph");
            //cp.setExtendedState(MAXIMIZED_BOTH);
            model.getContourPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex, 0);

            cp =
                    new SpeedContourPlot("SpeedPlot " + model.carFollowingModel.getLongName() + laneName,
                            model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Speed Contour Graph");
            model.getContourPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex + 1, 0);

            cp =
                    new FlowContourPlot("FlowPlot " + model.carFollowingModel.getLongName() + laneName,
                            model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("FLow Contour Graph");
            model.getContourPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex, 1);

            cp =
                    new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModel.getLongName() + laneName,
                            model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Acceleration Contour Graph");
            model.getContourPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex + 1, 1);

            TrajectoryPlot trajectoryPlot =
                    new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModel.getLongName() + laneName,
                            new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), model.getMinimumDistance(),
                            model.lanes[laneIndex].getLength());
            trajectoryPlot.setTitle("Trajectories");
            charts.setCell(trajectoryPlot.getContentPane(), 1 + laneIndex, 2);
            model.getTrajectoryPlots().get(laneIndex).add(trajectoryPlot);
        }
        separateFrame.add(charts);
        separateFrame.setVisible(true);
        separateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void createDirectLaneFrame(InternalContourPlotsModel model)
    {
        if(separateFrame == null)
        {
            separateFrame=new JFrame("Statistics Plots");
            separateFrame.setSize(800, 600);
        }
        else {
            //remove the previous JFrame
            separateFrame.setVisible(false);
            separateFrame.dispose();
            //create a new one
            separateFrame = new JFrame("Statistics Plots");
            separateFrame.setSize(800, 600);
        }
        
        
        TablePanel charts = new TablePanel(2, 2);
       

        // Make the four contour plots
        ContourPlot cp;

        cp = new DensityContourPlot("DensityPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Density Contour Graph");
      //  cp.setExtendedState(MAXIMIZED_BOTH);
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 0);

        cp = new SpeedContourPlot("SpeedPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Speed Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 0);

        cp = new FlowContourPlot("FlowPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("FLow Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 1);

        cp = new AccelerationContourPlot("AccelerationPlot", model.getMinimumDistance(), model.getMaximumDistance());
        cp.setTitle("Acceleration Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 1);
        separateFrame.setVisible(true);
        separateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    
    private void threeDStat_conf()
    {
        // The east part of statistics
        int size = 500000;
        float x;
        float y;
        float z;
        float a;
        
        Coord3d[] points = new Coord3d[size];
        Color[]   colors = new Color[size];
        
        Random r = new Random();
        r.setSeed(0);
        
        for(int i=0; i<size; i++){
            x = r.nextFloat() - 0.5f;
            y = r.nextFloat() - 0.5f;
            z = r.nextFloat() - 0.5f;
            points[i] = new Coord3d(x, y, z);
            a = 0.25f;
            colors[i] = new Color(x, y, z, a);
        }
        Scatter scatter = new Scatter(points, colors);
        // Scatter scatter2 = new Scatter(points, colors);
         
        Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
        chart.getScene().add(scatter);
        
        Chart chart2 = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
        chart2.getScene().add(scatter);
        
      
        JPanel statPanel = new JPanel(new GridLayout(2, 1));
        Border b = BorderFactory.createLineBorder(java.awt.Color.black);
        statPanel.setBorder(b);
        statPanel.setSize(new Dimension(500, 500));
        statPanel.add((java.awt.Component) chart.getCanvas());
        statPanel.add((java.awt.Component) chart2.getCanvas());
        
        JSplitPane jsplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,simPanel,statPanel);
   
        //add(chartPanel, "cell 0 " + 1 + ", grow");
        f.add(jsplitpane, BorderLayout.CENTER);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setSize(1000, 680);
        f.setResizable(true);
        f.setVisible(true);
        /*
        while(true)
        {
            for(int i=0; i<size; i++){
                x = r.nextFloat() - 0.5f;
                y = r.nextFloat() - 0.5f;
                z = r.nextFloat() - 0.5f;
                points[i] = new Coord3d(x, y, z);
                a = 0.25f;
                colors[i] = new Color(x, y, z, a);
            }
            
            scatter = new Scatter(points, colors);
            statPanel.repaint();
        }*/
    }
    
    public void init() throws RemoteException, SimRuntimeException
    {
       
       settingPanel_conf();
       f.add(settingPanel, BorderLayout.WEST);
       simPanel.setPreferredSize(new Dimension(400, 800));
       f.add(simPanel, BorderLayout.CENTER);
       statPanel.setLayout(new GridLayout(2,2,3,3));
       statPanel.setPreferredSize(new Dimension(500,800));
       
       f.add(statPanel, BorderLayout.EAST);
       
      
       f.setSize(1000, 800);
       f.setVisible(true);
       f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    
    
    public static void main(String[] args) throws RemoteException, SimRuntimeException
    {
        new newGUI().init();
    }
}

