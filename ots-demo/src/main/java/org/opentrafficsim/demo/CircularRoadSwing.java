package org.opentrafficsim.demo;

import java.awt.Component;
import java.awt.Container;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.JButton;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.animation.GraphLaneUtil;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.LinkPosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourPlotAcceleration;
import org.opentrafficsim.draw.graphs.ContourPlotDensity;
import org.opentrafficsim.draw.graphs.ContourPlotFlow;
import org.opentrafficsim.draw.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.graphs.FundamentalDiagram;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.Quantity;
import org.opentrafficsim.draw.graphs.GraphCrossSection;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.PlotScheduler;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.OtsPlotScheduler;
import org.opentrafficsim.swing.graphs.SwingContourPlot;
import org.opentrafficsim.swing.graphs.SwingFundamentalDiagram;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.graphs.SwingTrajectoryPlot;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;
import nl.tudelft.simulation.language.DsolException;

/**
 * Circular road simulation demo.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class CircularRoadSwing extends OtsSimulationApplication<CircularRoadModel>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a CircularRoad Swing application.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public CircularRoadSwing(final String title, final OtsAnimationPanel panel, final CircularRoadModel model)
    {
        super(model, panel, DefaultsFactory.GTU_TYPE_MARKERS.toMap());

        // NetworkAnimation networkAnimation = new NetworkAnimation(model.getNetwork());
        // networkAnimation.addDrawingInfoClass(Lane.class, new DrawingInfoShape<>(Color.GRAY));
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
        // simulatorDemo();
        demo(true);
    }

    /**
     * Run the simulation without animation.
     */
    public static void simulatorDemo()
    {
        try
        {
            OtsSimulator simulator = new OtsSimulator("CircularRoadSwing");
            final CircularRoadModel otsModel = new CircularRoadModel(simulator);
            System.out.println(otsModel.getInputParameterMap());
            TabbedParameterDialog.process(otsModel.getInputParameterMap());
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(3600.0), otsModel,
                    HistoryManagerDevs.noHistory(simulator));
            Thread getLocationThread = new Thread()
            {
                @Override
                public void run()
                {
                    System.out.println("getLocationThread starts up");
                    int iteration = 0;
                    int getLocationCalls = 0;
                    while (simulator.isStartingOrRunning())
                    {
                        iteration++;
                        for (Gtu gtu : otsModel.getNetwork().getGTUs())
                        {
                            gtu.getLocation();
                            getLocationCalls++;
                        }
                        try
                        {
                            Thread.sleep(1);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("getLocationThread exits after " + iteration + " iterations and " + getLocationCalls
                            + " getLocation calls");
                }

            };
            simulator.start();
            getLocationThread.start();
            while (simulator.isStartingOrRunning())
            {
                Thread.sleep(1000);
                // System.out.println("Simulator time is " + simulator.getSimulatorTime());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Find the start simulation button and click it.
     * @param component some component that could be the start button, or a container that contains the start button
     * @return true if the start button was found (and clicked); false otherwise
     */
    public static boolean clickStart(final Component component)
    {
        if (component instanceof JButton)
        {
            JButton button = (JButton) component;
            if (button.getText().contains("Start simulation model"))
            {
                button.doClick();
                System.out.println("Auto clicked the start button");
                return true;
            }
        }
        else if (component instanceof Container)
        {
            for (Component comp : ((Container) component).getComponents())
            {
                if (clickStart(comp))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Click the button that starts the animated simulation.
     * @param component some component that (hopefully) is, or contains the start button
     * @return true if the button was found (and clicked); false if the start button was not found
     */
    public static boolean clickRunPause(final Component component)
    {
        if (component instanceof JButton)
        {
            JButton button = (JButton) component;
            // System.out.println("Found button with name " + button.getName());
            if (button.getName().equals("runPauseButton"))
            {
                button.doClick();
                System.out.println("Auto clicked the run button");
                return true;
            }
        }
        else if (component instanceof Container)
        {
            for (Component comp : ((Container) component).getComponents())
            {
                if (clickRunPause(comp))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Start the demo.
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("CircularRoadSwing");
            final CircularRoadModel otsModel = new CircularRoadModel(simulator);
            // Thread buttonClick = new Thread()
            // {
            // @Override
            // public void run()
            // {
            // try
            // {
            // Thread.sleep(1000);
            // }
            // catch (InterruptedException e)
            // {
            // e.printStackTrace();
            // } // wait for the TabbedParameterDialog to start up
            // // Find the window
            // for (Window window : Window.getWindows())
            // {
            // // System.out.println("Name of window is " + window.getName());
            // if (window.getName().startsWith("dialog"))
            // {
            // for (Component comp : window.getComponents())
            // {
            // if (clickStart(comp))
            // {
            // return;
            // }
            // }
            // }
            // }
            //
            // }
            // };
            // buttonClick.start(); // start the thread that will try to click on the start button
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(3600.0), otsModel,
                        HistoryManagerDevs.noHistory(simulator));
                OtsAnimationPanel animationPanel = new OtsAnimationPanel(otsModel.getNetwork().getExtent(), simulator, otsModel,
                        DEFAULT_GTU_COLORERS, otsModel.getNetwork());
                CircularRoadSwing app = new CircularRoadSwing("Circular Road", animationPanel, otsModel);
                app.setExitOnClose(exitOnClose);
                animationPanel.enableSimulationControlButtons();
                // simulator.setSpeedFactor(Double.MAX_VALUE, true);
                // simulator.setSpeedFactor(1000.0, true);
                // for (Component component : app.getComponents())
                // {
                // if (clickRunPause(component))
                // {
                // break;
                // }
                // }
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
        GraphPath<LaneDataRoad> path01;
        GraphPath<LaneDataRoad> path0;
        GraphPath<LaneDataRoad> path1;
        try
        {
            List<String> names = new ArrayList<>();
            names.add("Left lane");
            names.add("Right lane");
            List<Lane> start = new ArrayList<>();
            start.add(getModel().getPath(0).get(0));
            start.add(getModel().getPath(1).get(0));
            path01 = GraphLaneUtil.createPath(names, start).setCircular(true);
            path0 = GraphLaneUtil.createPath(names.get(0), start.get(0)).setCircular(true);
            path1 = GraphLaneUtil.createPath(names.get(1), start.get(1)).setCircular(true);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not create a path as a lane has no set speed limit.", exception);
        }
        RoadSampler sampler = new RoadSampler(getModel().getNetwork());
        GraphPath.initRecording(sampler, path01);
        GraphPath.initRecording(sampler, path0);
        GraphPath.initRecording(sampler, path1);
        ContourDataSource dataPool0 = new ContourDataSource(sampler.getSamplerData(), path0);
        ContourDataSource dataPool1 = new ContourDataSource(sampler.getSamplerData(), path1);
        Duration updateInterval = Duration.ofSI(10.0);

        SwingPlot plot = null;
        GraphPath<LaneDataRoad> path = null;
        ContourDataSource dataPool = null;

        TablePanel trajectoryChart = new TablePanel(2, 2);
        PlotScheduler scheduler = new OtsPlotScheduler(simulator);
        plot = new SwingTrajectoryPlot(
                new TrajectoryPlot("Trajectory all lanes", updateInterval, scheduler, sampler.getSamplerData(), path01));
        trajectoryChart.setCell(plot.getContentPane(), 0, 0);

        List<LaneData> lanes = new ArrayList<>();
        List<Length> positions = new ArrayList<>();
        lanes.add(path01.get(0).getSource(0));
        lanes.add(path1.get(0).getSource(0));
        positions.add(Length.ZERO);
        positions.add(Length.ZERO);
        List<String> names = new ArrayList<>();
        names.add("Left lane");
        names.add("Right lane");
        LinkPosition linkPosition = new LinkPosition(getModel().getPath(0).get(0).getLink(), 0.0);
        GraphCrossSection<LaneDataRoad> crossSection;
        try
        {
            crossSection = GraphLaneUtil.createCrossSection(names, linkPosition);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException(exception);
        }

        plot = new SwingFundamentalDiagram(
                new FundamentalDiagram("Fundamental diagram Density-Flow", Quantity.DENSITY, Quantity.FLOW, scheduler,
                        FundamentalDiagram.sourceFromSampler(sampler, crossSection, true, Duration.ofSI(60.0), false), null));
        trajectoryChart.setCell(plot.getContentPane(), 1, 0);

        plot = new SwingFundamentalDiagram(
                new FundamentalDiagram("Fundamental diagram Flow-Speed", Quantity.FLOW, Quantity.SPEED, scheduler,
                        FundamentalDiagram.sourceFromSampler(sampler, crossSection, false, Duration.ofSI(60.0), false), null));
        trajectoryChart.setCell(plot.getContentPane(), 1, 1);

        getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "Trajectories",
                trajectoryChart);

        for (int lane : new int[] {0, 1})
        {
            TablePanel charts = new TablePanel(3, 2);
            path = lane == 0 ? path0 : path1;
            dataPool = lane == 0 ? dataPool0 : dataPool1;

            plot = new SwingTrajectoryPlot(
                    new TrajectoryPlot("Trajectory lane " + lane, updateInterval, scheduler, sampler.getSamplerData(), path));
            charts.setCell(plot.getContentPane(), 0, 0);

            plot = new SwingContourPlot(new ContourPlotDensity("Density lane " + lane, scheduler, dataPool));
            charts.setCell(plot.getContentPane(), 1, 0);

            plot = new SwingContourPlot(new ContourPlotSpeed("Speed lane " + lane, scheduler, dataPool));
            charts.setCell(plot.getContentPane(), 1, 1);

            plot = new SwingContourPlot(new ContourPlotFlow("Flow lane " + lane, scheduler, dataPool));
            charts.setCell(plot.getContentPane(), 2, 0);

            plot = new SwingContourPlot(new ContourPlotAcceleration("Accceleration lane " + lane, scheduler, dataPool));
            charts.setCell(plot.getContentPane(), 2, 1);

            getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "stats lane " + lane,
                    charts);
        }
    }

}
