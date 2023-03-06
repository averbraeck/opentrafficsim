package org.opentrafficsim.demo.doc.tutorials;

import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.cli.CliUtil;
import org.djutils.draw.DrawRuntimeException;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Type;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import picocli.CommandLine.Option;

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
            throws NullPointerException, DrawRuntimeException, NetworkException, OtsGeometryException
    {
        RoadNetwork network = new RoadNetwork("Simple network", sim);
        OtsPoint3d pointA = new OtsPoint3d(0, 0, 0);
        OtsPoint3d pointB = new OtsPoint3d(500, 0, 0);
        Node nodeA = new Node(network, "A", pointA, Direction.ZERO);
        Node nodeB = new Node(network, "B", pointB, Direction.ZERO);
        GtuType car = DefaultsNl.CAR;
        GtuType.registerTemplateSupplier(car, Defaults.NL);
        LinkType freewayLink = DefaultsNl.FREEWAY;
        LaneType freewayLane = DefaultsRoadNl.FREEWAY;
        CrossSectionLink link = new CrossSectionLink(network, "AB", nodeA, nodeB, freewayLink, new OtsLine3d(pointA, pointB),
                LaneKeepingPolicy.KEEPRIGHT);
        new Lane(link, "Left", Length.instantiateSI(1.75), Length.instantiateSI(3.5), freewayLane,
                Map.of(car, new Speed(120, SpeedUnit.KM_PER_HOUR)));
        new Lane(link, "Right", Length.instantiateSI(-1.75), Length.instantiateSI(3.5), freewayLane,
                Map.of(car, new Speed(120, SpeedUnit.KM_PER_HOUR)));
        new Stripe(Type.SOLID, link, Length.instantiateSI(3.5), Length.instantiateSI(0.2));
        new Stripe(Type.DASHED, link, Length.instantiateSI(0.0), Length.instantiateSI(0.2));
        new Stripe(Type.SOLID, link, Length.instantiateSI(-3.5), Length.instantiateSI(0.2));
        return network;
    }

}
