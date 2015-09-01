package org.opentrafficsim.demo.geometry;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.animation.ShoulderAnimation;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.NoTrafficLane;
import org.opentrafficsim.core.network.lane.Shoulder;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, 
 * by $Author$, initial version ct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
/** */
public class TestModel implements OTSModelInterface, OTS_SCALAR
{
    /** */
    private static final long serialVersionUID = 1L;

    /** simulator. */
    private OTSSimulatorInterface simulator;

    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> pSimulator)
        throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSSimulatorInterface) pSimulator;

        // create a small graph and a road around it.

        OTSNode n0 = new OTSNode("N0", new OTSPoint3D(-25.0, 0.0));
        OTSNode n1 = new OTSNode("N1", new OTSPoint3D(0.0, 0.0));
        CrossSectionLink l01 =
            new CrossSectionLink("L01", n0, n1, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(-25.0, 0.0),
                new OTSPoint3D(0.0, 0.0)}));

        OTSNode n2 = new OTSNode("N2", new OTSPoint3D(25.0, 20.0));
        CrossSectionLink l12 =
            new CrossSectionLink("L12", n1, n2, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(0.0, 0.0),
                new OTSPoint3D(25.0, 20.0)}));

        OTSNode n3 = new OTSNode("N3", new OTSPoint3D(50.0, 0.0));
        CrossSectionLink l23 =
            new CrossSectionLink("L23", n2, n3, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(25.0, 20.0),
                new OTSPoint3D(50.0, 0.0)}));

        OTSNode n4 = new OTSNode("N4", new OTSPoint3D(75.0, -20.0));
        CrossSectionLink l34 =
            new CrossSectionLink("L34", n3, n4, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(50.0, 0.0),
                new OTSPoint3D(75.0, -20.0)}));

        OTSNode n5 = new OTSNode("N5", new OTSPoint3D(100.0, 0.0));
        CrossSectionLink l45 =
            new CrossSectionLink("L45", n4, n5, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(75.0, -20.0),
                new OTSPoint3D(100.0, 0.0)}));

        OTSNode n6 = new OTSNode("N6", new OTSPoint3D(125.0, 0.0));
        CrossSectionLink l56 =
            new CrossSectionLink("L56", n5, n6, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(100.0, 0.0),
                new OTSPoint3D(125.0, 0.0)}));

        OTSNode n7 = new OTSNode("N7", new OTSPoint3D(300.0, 0.0));
        CrossSectionLink l67 =
            new CrossSectionLink("L67", n6, n7, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(125.0, 0.0),
                new OTSPoint3D(150.0, 0.0), new OTSPoint3D(175.0, 20.0), new OTSPoint3D(200.0, 0.0),
                new OTSPoint3D(225.0, -20.0), new OTSPoint3D(250.0, 0.0), new OTSPoint3D(300.0, 0.0)}));

        try
        {
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
        Length.Rel m05 = new Length.Rel(0.5, LengthUnit.METER);
        Length.Rel m10 = new Length.Rel(1.0, LengthUnit.METER);
        Length.Rel m35 = new Length.Rel(3.5, LengthUnit.METER);
        Speed.Abs speedLimit = new Speed.Abs(100, SpeedUnit.KM_PER_HOUR);

        Shoulder sL = new Shoulder(link, "sL", new Length.Rel(9.0, LengthUnit.METER), m10, m10);

        Lane laneELL =
            new NoTrafficLane(link, "ELL", new Length.Rel(8.25, LengthUnit.METER),
                new Length.Rel(8.25, LengthUnit.METER), m05, m05);
        Lane laneL1 =
            new Lane(link, "L1", new Length.Rel(6.25, LengthUnit.METER), new Length.Rel(
                6.25, LengthUnit.METER), m35, m35, null, LongitudinalDirectionality.BACKWARD, speedLimit);
        Lane laneL2 =
            new Lane(link, "L2", new Length.Rel(2.75, LengthUnit.METER), new Length.Rel(
                2.75, LengthUnit.METER), m35, m35, null, LongitudinalDirectionality.BACKWARD, speedLimit);
        Lane laneELM =
            new NoTrafficLane(link, "ELM", new Length.Rel(0.75, LengthUnit.METER),
                new Length.Rel(0.75, LengthUnit.METER), m05, m05);

        Shoulder sM = new Shoulder(link, "sM", new Length.Rel(0.0, LengthUnit.METER), m10, m10);

        Lane laneERM =
            new NoTrafficLane(link, "ERM", new Length.Rel(-0.75, LengthUnit.METER),
                new Length.Rel(-0.75, LengthUnit.METER), m05, m05);
        Lane laneR2 =
            new Lane(link, "R2", new Length.Rel(-2.75, LengthUnit.METER),
                new Length.Rel(-2.75, LengthUnit.METER), m35, m35, null,
                LongitudinalDirectionality.FORWARD, speedLimit);
        Lane laneR1 =
            new Lane(link, "R1", new Length.Rel(-6.25, LengthUnit.METER),
                new Length.Rel(-6.25, LengthUnit.METER), m35, m35, null,
                LongitudinalDirectionality.FORWARD, speedLimit);
        Lane laneERR =
            new NoTrafficLane(link, "ERR", new Length.Rel(-8.25, LengthUnit.METER),
                new Length.Rel(-8.25, LengthUnit.METER), m05, m05);

        Shoulder sR = new Shoulder(link, "sR", new Length.Rel(-9.0, LengthUnit.METER), m10, m10);

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
    public final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return this.simulator;
    }
}
