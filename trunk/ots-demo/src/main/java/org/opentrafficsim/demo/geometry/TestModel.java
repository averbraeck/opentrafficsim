package org.opentrafficsim.demo.geometry;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.animation.ShoulderAnimation;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version ct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
/** */
public class TestModel implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The simulator. */
    private OTSSimulatorInterface simulator;

    /** {@inheritDoc} */
    @Override
    public final void
        constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSSimulatorInterface) pSimulator;

        // create a small graph and a road around it.

        try
        {
            OTSNode n0 = new OTSNode("N0", new OTSPoint3D(-25.0, 0.0));
            OTSNode n1 = new OTSNode("N1", new OTSPoint3D(0.0, 0.0));
            CrossSectionLink l01 =
                new CrossSectionLink("L01", n0, n1, LinkType.ALL, new OTSLine3D(new OTSPoint3D[]{
                    new OTSPoint3D(-25.0, 0.0), new OTSPoint3D(0.0, 0.0)}), LongitudinalDirectionality.DIR_BOTH,
                    LaneKeepingPolicy.KEEP_RIGHT);

            OTSNode n2 = new OTSNode("N2", new OTSPoint3D(25.0, 20.0));
            CrossSectionLink l12 =
                new CrossSectionLink("L12", n1, n2, LinkType.ALL, new OTSLine3D(new OTSPoint3D[]{
                    new OTSPoint3D(0.0, 0.0), new OTSPoint3D(25.0, 20.0)}), LongitudinalDirectionality.DIR_BOTH,
                    LaneKeepingPolicy.KEEP_RIGHT);

            OTSNode n3 = new OTSNode("N3", new OTSPoint3D(50.0, 0.0));
            CrossSectionLink l23 =
                new CrossSectionLink("L23", n2, n3, LinkType.ALL, new OTSLine3D(new OTSPoint3D[]{
                    new OTSPoint3D(25.0, 20.0), new OTSPoint3D(50.0, 0.0)}), LongitudinalDirectionality.DIR_BOTH,
                    LaneKeepingPolicy.KEEP_RIGHT);

            OTSNode n4 = new OTSNode("N4", new OTSPoint3D(75.0, -20.0));
            CrossSectionLink l34 =
                new CrossSectionLink("L34", n3, n4, LinkType.ALL, new OTSLine3D(new OTSPoint3D[]{
                    new OTSPoint3D(50.0, 0.0), new OTSPoint3D(75.0, -20.0)}), LongitudinalDirectionality.DIR_BOTH,
                    LaneKeepingPolicy.KEEP_RIGHT);

            OTSNode n5 = new OTSNode("N5", new OTSPoint3D(100.0, 0.0));
            CrossSectionLink l45 =
                new CrossSectionLink("L45", n4, n5, LinkType.ALL, new OTSLine3D(new OTSPoint3D[]{
                    new OTSPoint3D(75.0, -20.0), new OTSPoint3D(100.0, 0.0)}), LongitudinalDirectionality.DIR_BOTH,
                    LaneKeepingPolicy.KEEP_RIGHT);

            OTSNode n6 = new OTSNode("N6", new OTSPoint3D(125.0, 0.0));
            CrossSectionLink l56 =
                new CrossSectionLink("L56", n5, n6, LinkType.ALL, new OTSLine3D(new OTSPoint3D[]{
                    new OTSPoint3D(100.0, 0.0), new OTSPoint3D(125.0, 0.0)}), LongitudinalDirectionality.DIR_BOTH,
                    LaneKeepingPolicy.KEEP_RIGHT);

            OTSNode n7 = new OTSNode("N7", new OTSPoint3D(300.0, 0.0));
            CrossSectionLink l67 =
                new CrossSectionLink("L67", n6, n7, LinkType.ALL, new OTSLine3D(new OTSPoint3D[]{
                    new OTSPoint3D(125.0, 0.0), new OTSPoint3D(150.0, 0.0), new OTSPoint3D(175.0, 20.0),
                    new OTSPoint3D(200.0, 0.0), new OTSPoint3D(225.0, -20.0), new OTSPoint3D(250.0, 0.0),
                    new OTSPoint3D(300.0, 0.0)}), LongitudinalDirectionality.DIR_BOTH, LaneKeepingPolicy.KEEP_RIGHT);

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
     * @param link link.
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

        Lane laneELL =
            new NoTrafficLane(link, "ELL", new Length(8.25, METER), new Length(8.25, METER), m05, m05);
        Lane laneL1 =
            new Lane(link, "L1", new Length(6.25, METER), new Length(6.25, METER), m35, m35, null,
                LongitudinalDirectionality.DIR_MINUS, speedLimit, new OvertakingConditions.LeftAndRight());
        Lane laneL2 =
            new Lane(link, "L2", new Length(2.75, METER), new Length(2.75, METER), m35, m35, null,
                LongitudinalDirectionality.DIR_MINUS, speedLimit, new OvertakingConditions.LeftAndRight());
        Lane laneELM =
            new NoTrafficLane(link, "ELM", new Length(0.75, METER), new Length(0.75, METER), m05, m05);

        Shoulder sM = new Shoulder(link, "sM", new Length(0.0, METER), m10);

        Lane laneERM =
            new NoTrafficLane(link, "ERM", new Length(-0.75, METER), new Length(-0.75, METER), m05, m05);
        Lane laneR2 =
            new Lane(link, "R2", new Length(-2.75, METER), new Length(-2.75, METER), m35, m35, null,
                LongitudinalDirectionality.DIR_PLUS, speedLimit, new OvertakingConditions.LeftAndRight());
        Lane laneR1 =
            new Lane(link, "R1", new Length(-6.25, METER), new Length(-6.25, METER), m35, m35, null,
                LongitudinalDirectionality.DIR_PLUS, speedLimit, new OvertakingConditions.LeftAndRight());
        Lane laneERR =
            new NoTrafficLane(link, "ERR", new Length(-8.25, METER), new Length(-8.25, METER), m05, m05);

        Shoulder sR = new Shoulder(link, "sR", new Length(-9.0, METER), m10);

        try
        {
            new LaneAnimation(laneELL, this.simulator, Color.GRAY, false);
            new LaneAnimation(laneL1, this.simulator, Color.GRAY, false);
            new LaneAnimation(laneL2, this.simulator, Color.GRAY, false);
            new LaneAnimation(laneELM, this.simulator, Color.GRAY, false);
            new LaneAnimation(laneERM, this.simulator, Color.GRAY, false);
            new LaneAnimation(laneR2, this.simulator, Color.GRAY, false);
            new LaneAnimation(laneR1, this.simulator, Color.GRAY, false);
            new LaneAnimation(laneERR, this.simulator, Color.GRAY, false);

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
    public final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
        throws RemoteException
    {
        return this.simulator;
    }
}
