package org.opentrafficsim.demo.geometry;

import java.awt.Color;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.animation.LaneAnimation;
import org.opentrafficsim.core.network.animation.ShoulderAnimation;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version ct 17, 2014 <br>
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
        GeometryFactory factory = new GeometryFactory();
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        NodeGeotools.STR n0 = new NodeGeotools.STR("N0", new Coordinate(-25.0, 0.0));
        NodeGeotools.STR n1 = new NodeGeotools.STR("N1", new Coordinate(0.0, 0.0));
        CrossSectionLink l01 =
                new CrossSectionLink("L01", n0, n1, new DoubleScalar.Rel<LengthUnit>(25.0, LengthUnit.METER));
        LineString ls01 =
                factory.createLineString(new Coordinate[]{new Coordinate(-25.0, 0.0), new Coordinate(0.0, 0.0)});

        NodeGeotools.STR n2 = new NodeGeotools.STR("N2", new Coordinate(25.0, 20.0));
        CrossSectionLink l12 =
                new CrossSectionLink("L12", n1, n2, new DoubleScalar.Rel<LengthUnit>(Math.sqrt(25 * 25 + 20 * 20),
                        LengthUnit.METER));
        LineString ls12 =
                factory.createLineString(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(25.0, 20.0)});

        NodeGeotools.STR n3 = new NodeGeotools.STR("N3", new Coordinate(50.0, 0.0));
        CrossSectionLink l23 =
                new CrossSectionLink("L23", n2, n3, new DoubleScalar.Rel<LengthUnit>(Math.sqrt(25 * 25 + 20 * 20),
                        LengthUnit.METER));
        LineString ls23 =
                factory.createLineString(new Coordinate[]{new Coordinate(25.0, 20.0), new Coordinate(50.0, 0.0)});

        NodeGeotools.STR n4 = new NodeGeotools.STR("N4", new Coordinate(75.0, -20.0));
        CrossSectionLink l34 =
                new CrossSectionLink("L34", n3, n4, new DoubleScalar.Rel<LengthUnit>(Math.sqrt(25 * 25 + 20 * 20),
                        LengthUnit.METER));
        LineString ls34 =
                factory.createLineString(new Coordinate[]{new Coordinate(50.0, 0.0), new Coordinate(75.0, -20.0)});

        NodeGeotools.STR n5 = new NodeGeotools.STR("N5", new Coordinate(100.0, 0.0));
        CrossSectionLink l45 =
                new CrossSectionLink("L45", n4, n5, new DoubleScalar.Rel<LengthUnit>(Math.sqrt(25 * 25 + 20 * 20),
                        LengthUnit.METER));
        LineString ls45 =
                factory.createLineString(new Coordinate[]{new Coordinate(75.0, -20.0), new Coordinate(100.0, 0.0)});

        NodeGeotools.STR n6 = new NodeGeotools.STR("N6", new Coordinate(125.0, 0.0));
        CrossSectionLink l56 =
                new CrossSectionLink("L56", n5, n6, new DoubleScalar.Rel<LengthUnit>(25.0, LengthUnit.METER));
        LineString ls56 =
                factory.createLineString(new Coordinate[]{new Coordinate(100.0, 0.0), new Coordinate(125.0, 0.0)});

        NodeGeotools.STR n7 = new NodeGeotools.STR("N7", new Coordinate(300.0, 0.0));
        CrossSectionLink l67 =
                new CrossSectionLink("L67", n6, n7, new DoubleScalar.Rel<LengthUnit>(
                        75.0 + 4.0 * Math.sqrt(25 * 25 + 20 * 20), LengthUnit.METER));
        LineString ls67 =
                factory.createLineString(new Coordinate[]{new Coordinate(125.0, 0.0), new Coordinate(150.0, 0.0),
                        new Coordinate(175.0, 20.0), new Coordinate(200.0, 0.0), new Coordinate(225.0, -20.0),
                        new Coordinate(250.0, 0.0), new Coordinate(300.0, 0.0)});

        try
        {
            new LinearGeometry(l01, ls01, crs);
            new LinearGeometry(l12, ls12, crs);
            new LinearGeometry(l23, ls23, crs);
            new LinearGeometry(l34, ls34, crs);
            new LinearGeometry(l45, ls45, crs);
            new LinearGeometry(l56, ls56, crs);
            new LinearGeometry(l67, ls67, crs);
        }
        catch (NetworkException ne)
        {
            throw new SimRuntimeException(ne);
        }

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
        catch (NetworkException exception)
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
     */
    private void add2x2Lanes(final CrossSectionLink link) throws NetworkException
    {
        // four lanes, grass underneath, lines between lane1-2 and lane 2-3, barrier between lane 2-3
        // lane is 3.5 meters wide. gap between 3-4 is one meter. outside 0.5 meters on both sides
        DoubleScalar.Rel<LengthUnit> m05 = new DoubleScalar.Rel<LengthUnit>(0.5, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> m10 = new DoubleScalar.Rel<LengthUnit>(1.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> m35 = new DoubleScalar.Rel<LengthUnit>(3.5, LengthUnit.METER);
        DoubleScalar.Abs<FrequencyUnit> f0 = new DoubleScalar.Abs<FrequencyUnit>(0.0, FrequencyUnit.PER_HOUR);
        DoubleScalar.Abs<FrequencyUnit> f200 = new DoubleScalar.Abs<FrequencyUnit>(200.0, FrequencyUnit.PER_HOUR);
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

        Shoulder sL = new Shoulder(link, new DoubleScalar.Rel<LengthUnit>(9.0, LengthUnit.METER), m10, m10);

        Lane laneELL =
                new Lane(link, new DoubleScalar.Rel<LengthUnit>(8.25, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(8.25, LengthUnit.METER), m05, m05, null,
                        LongitudinalDirectionality.NONE, f0, speedLimit);
        Lane laneL1 =
                new Lane(link, new DoubleScalar.Rel<LengthUnit>(6.25, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(6.25, LengthUnit.METER), m35, m35, null,
                        LongitudinalDirectionality.BACKWARD, f200, speedLimit);
        Lane laneL2 =
                new Lane(link, new DoubleScalar.Rel<LengthUnit>(2.75, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(2.75, LengthUnit.METER), m35, m35, null,
                        LongitudinalDirectionality.BACKWARD, f200, speedLimit);
        Lane laneELM =
                new Lane(link, new DoubleScalar.Rel<LengthUnit>(0.75, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(0.75, LengthUnit.METER), m05, m05, null,
                        LongitudinalDirectionality.NONE, f0, speedLimit);

        Shoulder sM = new Shoulder(link, new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER), m10, m10);

        Lane laneERM =
                new Lane(link, new DoubleScalar.Rel<LengthUnit>(-0.75, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(-0.75, LengthUnit.METER), m05, m05, null,
                        LongitudinalDirectionality.NONE, f0, speedLimit);
        Lane laneR2 =
                new Lane(link, new DoubleScalar.Rel<LengthUnit>(-2.75, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(-2.75, LengthUnit.METER), m35, m35, null,
                        LongitudinalDirectionality.FORWARD, f200, speedLimit);
        Lane laneR1 =
                new Lane(link, new DoubleScalar.Rel<LengthUnit>(-6.25, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(-6.25, LengthUnit.METER), m35, m35, null,
                        LongitudinalDirectionality.FORWARD, f200, speedLimit);
        Lane laneERR =
                new Lane(link, new DoubleScalar.Rel<LengthUnit>(-8.25, LengthUnit.METER),
                        new DoubleScalar.Rel<LengthUnit>(-8.25, LengthUnit.METER), m05, m05, null,
                        LongitudinalDirectionality.NONE, f0, speedLimit);

        Shoulder sR = new Shoulder(link, new DoubleScalar.Rel<LengthUnit>(-9.0, LengthUnit.METER), m10, m10);

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
    public final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }
}