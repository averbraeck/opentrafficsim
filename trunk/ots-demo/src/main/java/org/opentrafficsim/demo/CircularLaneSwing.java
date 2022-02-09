package org.opentrafficsim.demo;

import java.awt.Dimension;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.graphs.ContourDataSource;
import org.opentrafficsim.draw.graphs.ContourPlotAcceleration;
import org.opentrafficsim.draw.graphs.ContourPlotDensity;
import org.opentrafficsim.draw.graphs.ContourPlotFlow;
import org.opentrafficsim.draw.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.SwingContourPlot;
import org.opentrafficsim.swing.graphs.SwingPlot;
import org.opentrafficsim.swing.graphs.SwingTrajectoryPlot;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;
import nl.tudelft.simulation.language.DSOLException;

/**
 * Circular lane simulation demo.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-11-18 20:49:04 +0100 (Sun, 18 Nov 2018) $, @version $Revision: 4743 $, by $Author: averbraeck $,
 * initial version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularLaneSwing extends OTSSimulationApplication<CircularLaneModel>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Create a CircularLane Swing application.
     * @param title String; the title of the Frame
     * @param panel OTSAnimationPanel; the tabbed panel to display
     * @param model CircularLaneModel; the model
     * @throws OTSDrawingException on animation error
     */
    public CircularLaneSwing(final String title, final OTSAnimationPanel panel, final CircularLaneModel model)
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
            OTSAnimator simulator = new OTSAnimator("CircularLaneSwing");
            final CircularLaneModel otsModel = new CircularLaneModel(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(otsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, otsModel, DEFAULT_COLORER, otsModel.getNetwork());
                CircularLaneSwing app = new CircularLaneSwing("Circular Lane", animationPanel, otsModel);
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
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | DSOLException exception)
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
        GraphPath<KpiLaneDirection> path;
        try
        {
            path = GraphLaneUtil.createPath("Lane", new LaneDirection(getModel().getPath().get(0), GTUDirectionality.DIR_PLUS));
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not create a path as a lane has no set speed limit.", exception);
        }

        RoadSampler sampler = new RoadSampler(getModel().getNetwork());
        GraphPath.initRecording(sampler, path);
        ContourDataSource<?> dataPool = new ContourDataSource<>(sampler.getSamplerData(), path);
        TablePanel charts = new TablePanel(3, 2);
        SwingPlot plot = null;

        plot = new SwingTrajectoryPlot(
                new TrajectoryPlot("TrajectoryPlot", Duration.instantiateSI(10.0), simulator, sampler.getSamplerData(), path));
        charts.setCell(plot.getContentPane(), 0, 0);

        plot = new SwingContourPlot(new ContourPlotDensity("DensityPlot", simulator, dataPool));
        charts.setCell(plot.getContentPane(), 1, 0);

        plot = new SwingContourPlot(new ContourPlotSpeed("SpeedPlot", simulator, dataPool));
        charts.setCell(plot.getContentPane(), 2, 0);

        plot = new SwingContourPlot(new ContourPlotFlow("FlowPlot", simulator, dataPool));
        charts.setCell(plot.getContentPane(), 1, 1);

        plot = new SwingContourPlot(new ContourPlotAcceleration("AccelerationPlot", simulator, dataPool));
        charts.setCell(plot.getContentPane(), 2, 1);

        getAnimationPanel().getTabbedPane().addTab(getAnimationPanel().getTabbedPane().getTabCount(), "statistics ", charts);
    }

}
