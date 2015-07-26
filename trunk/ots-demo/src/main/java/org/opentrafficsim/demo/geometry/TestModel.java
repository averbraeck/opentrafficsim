package org.opentrafficsim.demo.geometry;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

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
import org.opentrafficsim.core.network.lane.Shoulder;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

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
public class TestModel implements OTSModelInterface
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

        OTSNode<String> n0 = new OTSNode<String>("N0", new OTSPoint3D(-25.0, 0.0));
        OTSNode<String> n1 = new OTSNode<String>("N1", new OTSPoint3D(0.0, 0.0));
        CrossSectionLink<String, String> l01 =
            new CrossSectionLink<String, String>("L01", n0, n1, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(-25.0, 0.0),
                new OTSPoint3D(0.0, 0.0)}));

        OTSNode<String> n2 = new OTSNode<String>("N2", new OTSPoint3D(25.0, 20.0));
        CrossSectionLink<String, String> l12 =
            new CrossSectionLink<String, String>("L12", n1, n2, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(0.0, 0.0),
                new OTSPoint3D(25.0, 20.0)}));

        OTSNode<String> n3 = new OTSNode<String>("N3", new OTSPoint3D(50.0, 0.0));
        CrossSectionLink<String, String> l23 =
            new CrossSectionLink<String, String>("L23", n2, n3, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(25.0, 20.0),
                new OTSPoint3D(50.0, 0.0)}));

        OTSNode<String> n4 = new OTSNode<String>("N4", new OTSPoint3D(75.0, -20.0));
        CrossSectionLink<String, String> l34 =
            new CrossSectionLink<String, String>("L34", n3, n4, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(50.0, 0.0),
                new OTSPoint3D(75.0, -20.0)}));

        OTSNode<String> n5 = new OTSNode<String>("N5", new OTSPoint3D(100.0, 0.0));
        CrossSectionLink<String, String> l45 =
            new CrossSectionLink<String, String>("L45", n4, n5, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(75.0, -20.0),
                new OTSPoint3D(100.0, 0.0)}));

        OTSNode<String> n6 = new OTSNode<String>("N6", new OTSPoint3D(125.0, 0.0));
        CrossSectionLink<String, String> l56 =
            new CrossSectionLink<String, String>("L56", n5, n6, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(100.0, 0.0),
                new OTSPoint3D(125.0, 0.0)}));

        OTSNode<String> n7 = new OTSNode<String>("N7", new OTSPoint3D(300.0, 0.0));
        CrossSectionLink<String, String> l67 =
            new CrossSectionLink<String, String>("L67", n6, n7, new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(125.0, 0.0),
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
     * SSSSS Shoulder<String, String> SL SSSSS +9.00 (width = 1.0)
     * ----------------------- +8.50
     * EEEEE Emergency ELL EEE +8.25 (width = 0.5)
     * ----------------------- +8.00
     * LLLLL Lane L1 LLLLLLLLL +6.25 (width = 3.5)
     * ----------------------- +4.50
     * LLLLL Lane L2 LLLLLLLLL +2.75 (width = 3.5)
     * ----------------------- +1.00
     * EEEEE Emergency ELM EEE +0.75 (width = 0.5)
     * ----------------------- +0.50
     * SSSSS Shoulder<String, String> SM SSSSS +0.00 (width = 1.0)
     * ----------------------- -0.50
     * EEEEE Emergency ERM EEE -0.75 (width = 0.5)
     * ----------------------- -1.00
     * LLLLL Lane R2 LLLLLLLLL -2.75 (width = 3.5)
     * ----------------------- -4.50
     * LLLLL Lane R1 LLLLLLLLL -6.25 (width = 3.5)
     * ----------------------- -8.00
     * EEEEE Emergency ERR EEE -8.25 (width = 0.5)
     * ----------------------- -8.50
     * SSSSS Shoulder<String, String> SR SSSSS -9.00 (width = 1.0)
     * ----------------------- -9.50
     * </pre>
     *
     * <br>
     * @param link link.
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException on network inconsistency
     */
    private void add2x2Lanes(final CrossSectionLink<String, String> link) throws NetworkException, OTSGeometryException
    {
        // four lanes, grass underneath, lines between lane1-2 and lane 2-3, barrier between lane 2-3
        // lane is 3.5 meters wide. gap between 3-4 is one meter. outside 0.5 meters on both sides
        DoubleScalar.Rel<LengthUnit> m05 = new DoubleScalar.Rel<LengthUnit>(0.5, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> m10 = new DoubleScalar.Rel<LengthUnit>(1.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> m35 = new DoubleScalar.Rel<LengthUnit>(3.5, LengthUnit.METER);
        DoubleScalar.Abs<FrequencyUnit> f0 = new DoubleScalar.Abs<FrequencyUnit>(0.0, FrequencyUnit.PER_HOUR);
        DoubleScalar.Abs<FrequencyUnit> f200 = new DoubleScalar.Abs<FrequencyUnit>(200.0, FrequencyUnit.PER_HOUR);
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

        Shoulder<String, String> sL =
            new Shoulder<String, String>(link, new DoubleScalar.Rel<LengthUnit>(9.0, LengthUnit.METER), m10, m10);

        Lane<String, String> laneELL =
            new Lane<String, String>(link, new DoubleScalar.Rel<LengthUnit>(8.25, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(8.25, LengthUnit.METER), m05, m05, null, LongitudinalDirectionality.NONE,
                f0, speedLimit);
        Lane<String, String> laneL1 =
            new Lane<String, String>(link, new DoubleScalar.Rel<LengthUnit>(6.25, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(6.25, LengthUnit.METER), m35, m35, null,
                LongitudinalDirectionality.BACKWARD, f200, speedLimit);
        Lane<String, String> laneL2 =
            new Lane<String, String>(link, new DoubleScalar.Rel<LengthUnit>(2.75, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(2.75, LengthUnit.METER), m35, m35, null,
                LongitudinalDirectionality.BACKWARD, f200, speedLimit);
        Lane<String, String> laneELM =
            new Lane<String, String>(link, new DoubleScalar.Rel<LengthUnit>(0.75, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(0.75, LengthUnit.METER), m05, m05, null, LongitudinalDirectionality.NONE,
                f0, speedLimit);

        Shoulder<String, String> sM =
            new Shoulder<String, String>(link, new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER), m10, m10);

        Lane<String, String> laneERM =
            new Lane<String, String>(link, new DoubleScalar.Rel<LengthUnit>(-0.75, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(-0.75, LengthUnit.METER), m05, m05, null, LongitudinalDirectionality.NONE,
                f0, speedLimit);
        Lane<String, String> laneR2 =
            new Lane<String, String>(link, new DoubleScalar.Rel<LengthUnit>(-2.75, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(-2.75, LengthUnit.METER), m35, m35, null,
                LongitudinalDirectionality.FORWARD, f200, speedLimit);
        Lane<String, String> laneR1 =
            new Lane<String, String>(link, new DoubleScalar.Rel<LengthUnit>(-6.25, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(-6.25, LengthUnit.METER), m35, m35, null,
                LongitudinalDirectionality.FORWARD, f200, speedLimit);
        Lane<String, String> laneERR =
            new Lane<String, String>(link, new DoubleScalar.Rel<LengthUnit>(-8.25, LengthUnit.METER),
                new DoubleScalar.Rel<LengthUnit>(-8.25, LengthUnit.METER), m05, m05, null, LongitudinalDirectionality.NONE,
                f0, speedLimit);

        Shoulder<String, String> sR =
            new Shoulder<String, String>(link, new DoubleScalar.Rel<LengthUnit>(-9.0, LengthUnit.METER), m10, m10);

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

            new ShoulderAnimation(sL, this.simulator);
            new ShoulderAnimation(sM, this.simulator);
            new ShoulderAnimation(sR, this.simulator);
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
