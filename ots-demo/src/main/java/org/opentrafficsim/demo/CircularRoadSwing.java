package org.opentrafficsim.demo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.animation.GraphLaneUtil;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
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
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.RoadNetwork;
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
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * Start the demo.
     * @param exitOnClose when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OtsAnimator simulator = new OtsAnimator("CircularRoadSwing");
            final CircularRoadModel otsModel = new CircularRoadModel(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(3600.0), otsModel,
                        HistoryManagerDevs.noHistory(simulator));
                OtsAnimationPanel animationPanel = new OtsAnimationPanel(otsModel.getNetwork().getExtent(), simulator, otsModel,
                        DEFAULT_GTU_COLORERS, otsModel.getNetwork());
                CircularRoadSwing app = new CircularRoadSwing("Circular Road", animationPanel, otsModel);
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
            throw new OtsRuntimeException("Could not create a path as a lane has no set speed limit.", exception);
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
            throw new OtsRuntimeException(exception);
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
