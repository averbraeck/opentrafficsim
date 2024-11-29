package org.opentrafficsim.demo.doc.tutorials;

import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.cli.CliUtil;
import org.djutils.draw.DrawRuntimeException;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe.StripeType;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import picocli.CommandLine.Option;

/**
 * This class contains code snippets that are used in the documentation. Whenever errors arise in this code, they need to be
 * fixed -and- the code in the documentation needs to be updated.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("all")
// @docs/08-tutorials/simulation-setup.md#how-to-set-up-a-simulation
public class SimpleSimulation extends AbstractSimulationScript
{

    @Option(names = "--output", description = "Generate output.", negatable = true, defaultValue = "false")
    private boolean output;

    protected SimpleSimulation()
    {
        super("Simple simulation", "Example simple simulation");
    }

    public static void main(final String[] args) throws Exception
    {
        SimpleSimulation simpleSimulation = new SimpleSimulation();
        CliUtil.execute(simpleSimulation, args);
        simpleSimulation.start();
    }

    // @docs/08-tutorials/simulation-setup.md#how-to-set-up-a-simulation
    @Override
    protected RoadNetwork setupSimulation(final OtsSimulatorInterface sim)
            throws NullPointerException, DrawRuntimeException, NetworkException
    {
        RoadNetwork network = new RoadNetwork("Simple network", sim);
        Point2d pointA = new Point2d(0, 0);
        Point2d pointB = new Point2d(500, 0);
        Node nodeA = new Node(network, "A", pointA, Direction.ZERO);
        Node nodeB = new Node(network, "B", pointB, Direction.ZERO);
        GtuType car = DefaultsNl.CAR;
        GtuType.registerTemplateSupplier(car, Defaults.NL);
        LinkType freewayLink = DefaultsNl.FREEWAY;
        LaneType freewayLane = DefaultsRoadNl.FREEWAY;
        CrossSectionLink link = new CrossSectionLink(network, "AB", nodeA, nodeB, freewayLink, new OtsLine2d(pointA, pointB),
                null, LaneKeepingPolicy.KEEPRIGHT);
        LaneGeometryUtil.createStraightLane(link, "Left", Length.instantiateSI(1.75), Length.instantiateSI(3.5), freewayLane,
                Map.of(car, new Speed(120, SpeedUnit.KM_PER_HOUR)));
        LaneGeometryUtil.createStraightLane(link, "Right", Length.instantiateSI(-1.75), Length.instantiateSI(3.5), freewayLane,
                Map.of(car, new Speed(120, SpeedUnit.KM_PER_HOUR)));
        LaneGeometryUtil.createStraightStripe(StripeType.SOLID, "1", link, Length.instantiateSI(3.5), Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(StripeType.DASHED, "2", link, Length.instantiateSI(0.0), Length.instantiateSI(0.2));
        LaneGeometryUtil.createStraightStripe(StripeType.SOLID, "3", link, Length.instantiateSI(-3.5), Length.instantiateSI(0.2));
        return network;
    }

}
