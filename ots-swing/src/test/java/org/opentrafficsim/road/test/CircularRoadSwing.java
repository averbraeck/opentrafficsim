package org.opentrafficsim.road.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.animation.network.NetworkAnimation;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.DirectedLinkPosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.swing.graphs.AbstractPlot;
import org.opentrafficsim.draw.swing.graphs.ContourDataSource;
import org.opentrafficsim.draw.swing.graphs.ContourPlotAcceleration;
import org.opentrafficsim.draw.swing.graphs.ContourPlotDensity;
import org.opentrafficsim.draw.swing.graphs.ContourPlotFlow;
import org.opentrafficsim.draw.swing.graphs.ContourPlotSpeed;
import org.opentrafficsim.draw.swing.graphs.FundamentalDiagram;
import org.opentrafficsim.draw.swing.graphs.FundamentalDiagram.Quantity;
import org.opentrafficsim.draw.swing.graphs.GraphCrossSection;
import org.opentrafficsim.draw.swing.graphs.GraphPath;
import org.opentrafficsim.draw.swing.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.swing.graphs.road.GraphLaneUtil;
import org.opentrafficsim.draw.swing.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.swing.network.LinkAnimation;
import org.opentrafficsim.draw.swing.network.NodeAnimation;
import org.opentrafficsim.draw.swing.road.LaneAnimation;
import org.opentrafficsim.draw.swing.road.ShoulderAnimation;
import org.opentrafficsim.draw.swing.road.StripeAnimation;
import org.opentrafficsim.draw.swing.road.StripeAnimation.TYPE;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.SimulatorFrame;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;

/**
 * Circular road simulation demo.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-11-18 20:49:04 +0100 (Sun, 18 Nov 2018) $, @version $Revision: 4743 $, by $Author: averbraeck $,
 * initial version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularRoadSwing extends SimulatorFrame
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private CircularRoadModel model;

    /** the panel. */
    private OTSAnimationPanel animationPanel;

    /**
     * Create a CircularRoad simulation.
     * @throws InputParameterException
     * @throws RemoteException
     * @throws OTSSimulationException
     */
    public CircularRoadSwing(final String title, OTSAnimationPanel panel, final CircularRoadModel model)
            throws InputParameterException, RemoteException, OTSSimulationException
    {
        super(title, panel);
        this.model = model;
        this.animationPanel = panel;

        NetworkAnimation networkAnimation = new NetworkAnimation(model.getNetwork());
        // networkAnimation.addDrawingInfoClass(Lane.class, new DrawingInfoShape<>(Color.GRAY));
        OTSNetwork network = model.getNetwork();
        System.out.println(network.getLinkMap());
        animateNetwork(model.getNetwork());
        AnimationToggles.setTextAnimationTogglesStandard(this.animationPanel);
        addStatisticsTab(model.getSimulator());
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     * @throws InputParameterException
     * @throws RemoteException
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
                    new Dimension(200, 200), simulator, otsModel, new DefaultSwitchableGTUColorer(), otsModel.getNetwork());
            new CircularRoadSwing("Circular Road", animationPanel, otsModel);
        }
        catch (SimRuntimeException | NamingException | RemoteException | InputParameterException
                | OTSSimulationException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Creates animations for nodes, links and lanes. This can be used if the network is not read from XML.
     * @param net OTSNetwork; network
     */
    protected void animateNetwork(final OTSNetwork net)
    {
        OTSSimulatorInterface simulator = this.model.getSimulator();
        try
        {
            for (Node node : net.getNodeMap().values())
            {
                new NodeAnimation(node, simulator);
            }
            for (Link link : net.getLinkMap().values())
            {
                new LinkAnimation(link, simulator, 0.5f);
                if (link instanceof CrossSectionLink)
                {
                    for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (element instanceof Lane)
                        {
                            new LaneAnimation((Lane) element, simulator, Color.GRAY.brighter(), false);
                        }
                        else if (element instanceof Shoulder)
                        {
                            new ShoulderAnimation((Shoulder) element, simulator, Color.DARK_GRAY);
                        }
                        else if (element instanceof Stripe)
                        {
                            Stripe stripe = (Stripe) element;
                            TYPE type;
                            if (stripe.isPermeable(GTUType.CAR, LateralDirectionality.LEFT))
                            {
                                type = stripe.isPermeable(GTUType.CAR, LateralDirectionality.RIGHT) ? TYPE.DASHED
                                        : TYPE.LEFTONLY;
                            }
                            else
                            {
                                type = stripe.isPermeable(GTUType.CAR, LateralDirectionality.RIGHT) ? TYPE.RIGHTONLY
                                        : TYPE.SOLID;
                            }
                            new StripeAnimation((Stripe) element, simulator, type);
                        }
                    }
                }
            }
            for (GTU gtu : net.getGTUs())
            {
                new DefaultCarAnimation((LaneBasedGTU) gtu, simulator);
            }
        }
        catch (RemoteException | NamingException | OTSGeometryException exception)
        {
            throw new RuntimeException("Exception while creating network animation.", exception);
        }
    }

    /**
     * Add the tabs
     * @param simulator
     * @throws OTSSimulationException
     */
    protected final void addStatisticsTab(final OTSSimulatorInterface simulator)
            throws OTSSimulationException
    {
        int columns = 7;
        int rows = 2;
        TablePanel charts = new TablePanel(columns, rows);

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
        for (int lane : new int[] { 0, 1 })
        {
            path = lane == 0 ? path01 : path1;
            dataPool = lane == 0 ? dataPool0 : dataPool1;

            plot = new TrajectoryPlot("Trajectory lane " + lane, updateInterval, simulator, sampler, path);
            charts.setCell(plot.getContentPane(), 0, 0);

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

            plot = new FundamentalDiagram("", Quantity.DENSITY, Quantity.FLOW, simulator, sampler, crossSection, true,
                    Duration.createSI(60.0), false);
            charts.setCell(plot.getContentPane(), 1, lane);

            plot = new FundamentalDiagram("", Quantity.FLOW, Quantity.SPEED, simulator, sampler, crossSection, false,
                    Duration.createSI(60.0), false);
            charts.setCell(plot.getContentPane(), 2, lane);

            plot = new ContourPlotDensity("", simulator, dataPool);
            charts.setCell(plot.getContentPane(), 3, lane);

            plot = new ContourPlotSpeed("", simulator, dataPool);
            charts.setCell(plot.getContentPane(), 4, lane);

            plot = new ContourPlotFlow("", simulator, dataPool);
            charts.setCell(plot.getContentPane(), 5, lane);

            plot = new ContourPlotAcceleration("", simulator, dataPool);
            charts.setCell(plot.getContentPane(), 6, lane);
        }

        this.animationPanel.getTabbedPane().addTab(this.animationPanel.getTabbedPane().getTabCount(), "statistics", charts);
    }

}
