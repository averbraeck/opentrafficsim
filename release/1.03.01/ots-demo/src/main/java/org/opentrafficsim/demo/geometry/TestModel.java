package org.opentrafficsim.demo.geometry;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.ShoulderAnimation;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version ct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
/** */
public class TestModel extends AbstractOTSModel implements UNITS
{
    /**
     * @param simulator OTSSimulatorInterface; the simulator
     */
    public TestModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
    }

    /** */
    private static final long serialVersionUID = 1L;

    /** the network. */
    private OTSRoadNetwork network = new OTSRoadNetwork("geometry test network", true);

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        // create a small graph and a road around it.

        try
        {
            OTSRoadNode n0 = new OTSRoadNode(this.network, "N0", new OTSPoint3D(-25.0, 0.0), Direction.ZERO);
            OTSRoadNode n1 = new OTSRoadNode(this.network, "N1", new OTSPoint3D(0.0, 0.0), Direction.ZERO);
            CrossSectionLink l01 =
                    new CrossSectionLink(this.network, "L01", n0, n1, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(-25.0, 0.0), new OTSPoint3D(0.0, 0.0)}),
                            this.simulator, LaneKeepingPolicy.KEEPRIGHT);

            OTSRoadNode n2 = new OTSRoadNode(this.network, "N2", new OTSPoint3D(25.0, 20.0), Direction.ZERO);
            CrossSectionLink l12 =
                    new CrossSectionLink(this.network, "L12", n1, n2, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(0.0, 0.0), new OTSPoint3D(25.0, 20.0)}),
                            this.simulator, LaneKeepingPolicy.KEEPRIGHT);

            OTSRoadNode n3 = new OTSRoadNode(this.network, "N3", new OTSPoint3D(50.0, 0.0), Direction.ZERO);
            CrossSectionLink l23 =
                    new CrossSectionLink(this.network, "L23", n2, n3, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(25.0, 20.0), new OTSPoint3D(50.0, 0.0)}),
                            this.simulator, LaneKeepingPolicy.KEEPRIGHT);

            OTSRoadNode n4 = new OTSRoadNode(this.network, "N4", new OTSPoint3D(75.0, -20.0), Direction.ZERO);
            CrossSectionLink l34 =
                    new CrossSectionLink(this.network, "L34", n3, n4, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(50.0, 0.0), new OTSPoint3D(75.0, -20.0)}),
                            this.simulator, LaneKeepingPolicy.KEEPRIGHT);

            OTSRoadNode n5 = new OTSRoadNode(this.network, "N5", new OTSPoint3D(100.0, 0.0), Direction.ZERO);
            CrossSectionLink l45 =
                    new CrossSectionLink(this.network, "L45", n4, n5, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(75.0, -20.0), new OTSPoint3D(100.0, 0.0)}),
                            this.simulator, LaneKeepingPolicy.KEEPRIGHT);

            OTSRoadNode n6 = new OTSRoadNode(this.network, "N6", new OTSPoint3D(125.0, 0.0), Direction.ZERO);
            CrossSectionLink l56 =
                    new CrossSectionLink(this.network, "L56", n5, n6, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(100.0, 0.0), new OTSPoint3D(125.0, 0.0)}),
                            this.simulator, LaneKeepingPolicy.KEEPRIGHT);

            OTSRoadNode n7 = new OTSRoadNode(this.network, "N7", new OTSPoint3D(300.0, 0.0), Direction.ZERO);
            CrossSectionLink l67 =
                    new CrossSectionLink(this.network, "L67", n6, n7, network.getLinkType(LinkType.DEFAULTS.ROAD),
                            new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(125.0, 0.0), new OTSPoint3D(150.0, 0.0),
                                    new OTSPoint3D(175.0, 20.0), new OTSPoint3D(200.0, 0.0), new OTSPoint3D(225.0, -20.0),
                                    new OTSPoint3D(250.0, 0.0), new OTSPoint3D(300.0, 0.0)}),
                            this.simulator, LaneKeepingPolicy.KEEPRIGHT);

            add2x2Lanes(l01);
            add2x2Lanes(l12);
            add2x2Lanes(l23);
            add2x2Lanes(l34);
            add2x2Lanes(l45);
            add2x2Lanes(l56);
            add2x2Lanes(l67);
        }
        catch (NetworkException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * The road is as follows; The design line goes from left to right.
     * 
     * <pre>
     * ----------------------- +9.50
     * SSSSS Shoulder SL SSSSS +9.00 (width = 1.0)
     * ----------------------- +8.50
     * EEEEE Emergency ELL EEE +8.25 (width = 0.5)
     * ----------------------- +8.00
     * LLLLL Lane L1 LLLLLLLLL +6.25 (width = 3.5)
     * ----------------------- +4.50
     * LLLLL Lane L2 LLLLLLLLL +2.75 (width = 3.5)
     * ----------------------- +1.00
     * EEEEE Emergency ELM EEE +0.75 (width = 0.5)
     * ----------------------- +0.50
     * SSSSS Shoulder SM SSSSS +0.00 (width = 1.0)
     * ----------------------- -0.50
     * EEEEE Emergency ERM EEE -0.75 (width = 0.5)
     * ----------------------- -1.00
     * LLLLL Lane R2 LLLLLLLLL -2.75 (width = 3.5)
     * ----------------------- -4.50
     * LLLLL Lane R1 LLLLLLLLL -6.25 (width = 3.5)
     * ----------------------- -8.00
     * EEEEE Emergency ERR EEE -8.25 (width = 0.5)
     * ----------------------- -8.50
     * SSSSS Shoulder SR SSSSS -9.00 (width = 1.0)
     * ----------------------- -9.50
     * </pre>
     *
     * <br>
     * @param link CrossSectionLink; link.
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException on network inconsistency
     */
    private void add2x2Lanes(final CrossSectionLink link) throws NetworkException, OTSGeometryException
    {
        // four lanes, grass underneath, lines between lane1-2 and lane 2-3, barrier between lane 2-3
        // lane is 3.5 meters wide. gap between 3-4 is one meter. outside 0.5 meters on both sides
        Length m05 = new Length(0.5, METER);
        Length m10 = new Length(1.0, METER);
        Length m35 = new Length(3.5, METER);
        Speed speedLimit = new Speed(100, KM_PER_HOUR);

        Shoulder sL = new Shoulder(link, "sL", new Length(9.0, METER), m10);

        Lane laneELL = new NoTrafficLane(link, "ELL", new Length(8.25, METER), new Length(8.25, METER), m05, m05);
        Lane laneL1 = new Lane(link, "L1", new Length(6.25, METER), new Length(6.25, METER), m35, m35, null, speedLimit);
        Lane laneL2 = new Lane(link, "L2", new Length(2.75, METER), new Length(2.75, METER), m35, m35, null, speedLimit);
        Lane laneELM = new NoTrafficLane(link, "ELM", new Length(0.75, METER), new Length(0.75, METER), m05, m05);

        Shoulder sM = new Shoulder(link, "sM", new Length(0.0, METER), m10);

        Lane laneERM = new NoTrafficLane(link, "ERM", new Length(-0.75, METER), new Length(-0.75, METER), m05, m05);
        Lane laneR2 = new Lane(link, "R2", new Length(-2.75, METER), new Length(-2.75, METER), m35, m35, null, speedLimit);
        Lane laneR1 = new Lane(link, "R1", new Length(-6.25, METER), new Length(-6.25, METER), m35, m35, null, speedLimit);
        Lane laneERR = new NoTrafficLane(link, "ERR", new Length(-8.25, METER), new Length(-8.25, METER), m05, m05);

        Shoulder sR = new Shoulder(link, "sR", new Length(-9.0, METER), m10);

        try
        {
            new LaneAnimation(laneELL, this.simulator, Color.GRAY);
            new LaneAnimation(laneL1, this.simulator, Color.GRAY);
            new LaneAnimation(laneL2, this.simulator, Color.GRAY);
            new LaneAnimation(laneELM, this.simulator, Color.GRAY);
            new LaneAnimation(laneERM, this.simulator, Color.GRAY);
            new LaneAnimation(laneR2, this.simulator, Color.GRAY);
            new LaneAnimation(laneR1, this.simulator, Color.GRAY);
            new LaneAnimation(laneERR, this.simulator, Color.GRAY);

            new ShoulderAnimation(sL, this.simulator, Color.GREEN);
            new ShoulderAnimation(sM, this.simulator, Color.GREEN);
            new ShoulderAnimation(sR, this.simulator, Color.GREEN);
        }
        catch (NamingException | RemoteException ne)
        {
            //
        }
    }

    /** {@inheritDoc} */
    @Override
    public final OTSRoadNetwork getNetwork()
    {
        return this.network;
    }

}
