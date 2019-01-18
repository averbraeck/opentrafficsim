package org.opentrafficsim.road.test;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.network.DirectedLinkPosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.draw.graphs.AbstractPlot;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourPlotAcceleration;
import org.opentrafficsim.draw.graphs.ContourPlotDensity;
import org.opentrafficsim.draw.graphs.ContourPlotFlow;
import org.opentrafficsim.draw.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.graphs.FundamentalDiagram;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.Quantity;
import org.opentrafficsim.draw.graphs.GraphCrossSection;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSwingApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;

/**
 * Circular road simulation demo.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-11-18 20:49:04 +0100 (Sun, 18 Nov 2018) $, @version $Revision: 4743 $, by $Author: averbraeck $,
 * initial version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularRoadSwing extends OTSSwingApplication
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private CircularRoadModel model;

    /** the panel. */
    private OTSAnimationPanel animationPanel;

    /**
     * Create a CircularRoad Swing application.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     * @throws OTSDrawingException on animation error
     */
    public CircularRoadSwing(final String title, OTSAnimationPanel panel, final CircularRoadModel model)
            throws OTSDrawingException
    {
        super(model, panel);
        this.model = model;
        this.animationPanel = panel;

        // NetworkAnimation networkAnimation = new NetworkAnimation(model.getNetwork());
        // networkAnimation.addDrawingInfoClass(Lane.class, new DrawingInfoShape<>(Color.GRAY));
        OTSNetwork network = model.getNetwork();
        System.out.println(network.getLinkMap());
        DefaultAnimationFactory.animateNetwork(model.getNetwork(), model.getSimulator(), DEFAULT_COLORER);
        AnimationToggles.setTextAnimationTogglesStandard(this.animationPanel);
        addStatisticsTabs(model.getSimulator());
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        try
        {
            OTSAnimator simulator = new OTSAnimator();
            final CircularRoadModel otsModel = new CircularRoadModel(simulator);
            new TabbedParameterDialog(otsModel.getInputParameterMap());
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), otsModel);
            OTSAnimationPanel animationPanel = new OTSAnimationPanel(new Rectangle2D.Double(-500, -500, 1000, 1000),
                    new Dimension(200, 200), simulator, otsModel, DEFAULT_COLORER, otsModel.getNetwork());
            new CircularRoadSwing("Circular Road", animationPanel, otsModel);
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add the statistics tabs.
     * @param simulator the simulator on which sampling can be scheduled
     */
    protected final void addStatisticsTabs(final OTSSimulatorInterface simulator)
    {
        GraphPath<KpiLaneDirection> path01;
        GraphPath<KpiLaneDirection> path0;
        GraphPath<KpiLaneDirection> path1;
        try
        {
            List<String> names = new ArrayList<>();
            names.add("Left lane");
            names.add("Right lane");
            List<LaneDirection> start = new ArrayList<>();
            start.add(new LaneDirection(this.model.getPath(0).get(0), GTUDirectionality.DIR_PLUS));
            start.add(new LaneDirection(this.model.getPath(1).get(0), GTUDirectionality.DIR_PLUS));
            path01 = GraphLaneUtil.createPath(names, start);
            path0 = GraphLaneUtil.createPath(names.get(0), start.get(0));
            path1 = GraphLaneUtil.createPath(names.get(1), start.get(1));
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not create a path as a lane has no set speed limit.", exception);
        }
        RoadSampler sampler = new RoadSampler(simulator);
        ContourDataSource<?> dataPool0 = new ContourDataSource<>(sampler, path0);
        ContourDataSource<?> dataPool1 = new ContourDataSource<>(sampler, path1);
        Duration updateInterval = Duration.createSI(10.0);

        AbstractPlot plot = null;
        GraphPath<KpiLaneDirection> path = null;
        ContourDataSource<?> dataPool = null;

        TablePanel trajectoryChart = new TablePanel(2, 2);
        plot = new TrajectoryPlot("Trajectory all lanes", updateInterval, simulator, sampler, path01);
        trajectoryChart.setCell(plot.getContentPane(), 0, 0);

        List<KpiLaneDirection> lanes = new ArrayList<>();
        List<Length> positions = new ArrayList<>();
        lanes.add(path01.get(0).getSource(0));
        lanes.add(path1.get(0).getSource(0));
        positions.add(Length.ZERO);
        positions.add(Length.ZERO);
        List<String> names = new ArrayList<>();
        names.add("Left lane");
        names.add("Right lane");
        DirectedLinkPosition linkPosition =
                new DirectedLinkPosition(this.model.getPath(0).get(0).getParentLink(), 0.0, GTUDirectionality.DIR_PLUS);
        GraphCrossSection<KpiLaneDirection> crossSection;
        try
        {
            crossSection = GraphLaneUtil.createCrossSection(names, linkPosition);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException(exception);
        }

        plot = new FundamentalDiagram("Fundamental diagram Density-Flow", Quantity.DENSITY, Quantity.FLOW, simulator, sampler,
                crossSection, true, Duration.createSI(60.0), false);
        trajectoryChart.setCell(plot.getContentPane(), 1, 0);

        plot = new FundamentalDiagram("Fundamental diagram Flow-Speed", Quantity.FLOW, Quantity.SPEED, simulator, sampler,
                crossSection, false, Duration.createSI(60.0), false);
        trajectoryChart.setCell(plot.getContentPane(), 1, 1);

        this.animationPanel.getTabbedPane().addTab(this.animationPanel.getTabbedPane().getTabCount(), "Trajectories",
                trajectoryChart);

        for (int lane : new int[] { 0, 1 })
        {
            TablePanel charts = new TablePanel(3, 2);
            path = lane == 0 ? path0 : path1;
            dataPool = lane == 0 ? dataPool0 : dataPool1;

            plot = new TrajectoryPlot("Trajectory lane " + lane, updateInterval, simulator, sampler, path);
            charts.setCell(plot.getContentPane(), 0, 0);

            plot = new ContourPlotDensity("Density lane " + lane, simulator, dataPool);
            charts.setCell(plot.getContentPane(), 1, 0);

            plot = new ContourPlotSpeed("Speed lane " + lane, simulator, dataPool);
            charts.setCell(plot.getContentPane(), 1, 1);

            plot = new ContourPlotFlow("Flow lane " + lane, simulator, dataPool);
            charts.setCell(plot.getContentPane(), 2, 0);

            plot = new ContourPlotAcceleration("Accceleration lane " + lane, simulator, dataPool);
            charts.setCell(plot.getContentPane(), 2, 1);

            this.animationPanel.getTabbedPane().addTab(this.animationPanel.getTabbedPane().getTabCount(), "stats lane " + lane,
                    charts);
        }
    }

}
