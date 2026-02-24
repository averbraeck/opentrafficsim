package org.opentrafficsim.demo;

import java.rmi.RemoteException;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.animation.GraphLaneUtil;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourPlotAcceleration;
import org.opentrafficsim.draw.graphs.ContourPlotDensity;
import org.opentrafficsim.draw.graphs.ContourPlotFlow;
import org.opentrafficsim.draw.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.PlotScheduler;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.sampling.LaneDataRoad;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.OtsPlotScheduler;
import org.opentrafficsim.swing.graphs.SwingContourPlot;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.graphs.SwingTrajectoryPlot;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationPanelDecorator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;
import nl.tudelft.simulation.language.DsolException;

/**
 * Simplest contour plots demonstration.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class StraightSwing extends OtsSimulationApplication<StraightModel> implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a Straight Swing application.
     * @param title the title of the Frame
     * @param panel the tabbed panel to display
     * @param model the model
     */
    public StraightSwing(final String title, final OtsSimulationPanel panel, final StraightModel model)
    {
        super(model, panel);
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
            OtsAnimator simulator = new OtsAnimator("StraightSwing");
            final StraightModel otsModel = new StraightModel(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(1500.0), otsModel,
                        HistoryManagerDevs.noHistory(simulator));
                OtsSimulationPanel simulationPanel =
                        new OtsSimulationPanel(otsModel.getNetwork(), new OtsSimulationPanelDecorator()
                        {
                            @Override
                            public void addTabs(final OtsSimulationPanel simulationPanel, final Network network)
                            {
                                addStatisticsTabs(simulationPanel, (RoadNetwork) network, otsModel.getPath());
                            }
                        });
                StraightSwing app = new StraightSwing("Straight", simulationPanel, otsModel);
                app.setExitOnClose(exitOnClose);
                simulationPanel.enableSimulationControlButtons();
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
     * @param simulation simulation panel
     * @param network network
     * @param lanePath path
     */
    private static void addStatisticsTabs(final OtsSimulationPanel simulation, final RoadNetwork network,
            final List<Lane> lanePath)
    {
        GraphPath<LaneDataRoad> path;
        try
        {
            path = GraphLaneUtil.createPath("Lane", lanePath.get(0));
        }
        catch (NetworkException exception)
        {
            throw new OtsRuntimeException("Could not create a path as a lane has no set speed limit.", exception);
        }

        RoadSampler sampler = new RoadSampler(network);
        GraphPath.initRecording(sampler, path);
        ContourDataSource dataPool = new ContourDataSource(sampler.getSamplerData(), path);
        TablePanel charts = new TablePanel(3, 2);
        SwingPlot plot = null;
        PlotScheduler scheduler = new OtsPlotScheduler(network.getSimulator());

        plot = new SwingTrajectoryPlot(
                new TrajectoryPlot("TrajectoryPlot", Duration.ofSI(10.0), scheduler, sampler.getSamplerData(), path));
        charts.setCell(plot.getContentPane(), 0, 0);

        plot = new SwingContourPlot(new ContourPlotDensity("DensityPlot", scheduler, dataPool));
        charts.setCell(plot.getContentPane(), 1, 0);

        plot = new SwingContourPlot(new ContourPlotSpeed("SpeedPlot", scheduler, dataPool));
        charts.setCell(plot.getContentPane(), 2, 0);

        plot = new SwingContourPlot(new ContourPlotFlow("FlowPlot", scheduler, dataPool));
        charts.setCell(plot.getContentPane(), 1, 1);

        plot = new SwingContourPlot(new ContourPlotAcceleration("AccelerationPlot", scheduler, dataPool));
        charts.setCell(plot.getContentPane(), 2, 1);

        simulation.getTabbedPane().addTab(simulation.getTabbedPane().getTabCount(), "statistics ", charts);
    }
}
