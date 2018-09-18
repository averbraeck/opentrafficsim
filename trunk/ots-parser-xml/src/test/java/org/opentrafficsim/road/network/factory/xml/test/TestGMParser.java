package org.opentrafficsim.road.network.factory.xml.test;

import static org.opentrafficsim.core.gtu.GTUType.CAR;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.animation.SpeedGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.gtu.generator.GTUGeneratorIndividual;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.factory.xml.test.TestGMParser.WGS84ToRDNewTransform.Coords;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestGMParser extends AbstractWrappableAnimation
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    TestGMParser xmlModel = new TestGMParser();
                    // 1 hour simulation run for testing
                    xmlModel.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(60.0, DurationUnit.MINUTE),
                            new ArrayList<Property<?>>(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "TestGMModel";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "TestGMModel";
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel()
    {
        return new TestGMModel();
    }

    /** {@inheritDoc} */
    @Override
    protected void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    protected final Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(0, 2900, 1400, 1200);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TestGMParser []";
    }

    /**
     * Model to test the GM XML parser.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class TestGMModel implements OTSModelInterface, Serializable
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The simulator. */
        private DEVSSimulatorInterface.TimeDoubleUnit simulator;

        /** the network. */
        private OTSNetwork network;

        /** {@inheritDoc} */
        @Override
        public final void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> pSimulator)
                throws SimRuntimeException
        {
            this.simulator = (DEVSSimulatorInterface.TimeDoubleUnit) pSimulator;
            URL url = URLResource.getResource("/networkv2_90km_V5i.xml");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            try
            {
                this.network = nlp.build(url, true);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException | ValueException | ParameterException exception)
            {
                exception.printStackTrace();
            }

            URL gisURL = URLResource.getResource("/N201/map.xml");
            System.err.println("GIS-map file: " + gisURL.toString());
            CoordinateTransform rdto0 = new CoordinateTransformRD(104450, 478845);
            new GisRenderable2D(this.simulator, gisURL, rdto0);

            // make the GTU generators.
            GTUType carType = CAR;
            StreamInterface stream = new MersenneTwister(1);
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistConstant(stream, 0.0), SpeedUnit.METER_PER_SECOND);
            ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> interarrivelTimeDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistExponential(stream, 7.0), DurationUnit.SECOND);
            ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistConstant(stream, 4.5), LengthUnit.METER);
            ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistConstant(stream, 2.0), LengthUnit.METER);
            ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDist =
                    new ContinuousDistDoubleScalar.Rel<>(new DistConstant(stream, 140.0), SpeedUnit.KM_PER_HOUR);
            int maxGTUs = Integer.MAX_VALUE;
            Time startTime = Time.ZERO;
            Time endTime = new Time(1E24, TimeUnit.BASE_HOUR);
            GTUColorer gtuColorer = new SwitchableGTUColorer(0, new IDGTUColorer(),
                    new SpeedGTUColorer(new Speed(100.0, SpeedUnit.KM_PER_HOUR)),
                    new AccelerationGTUColorer(new Acceleration(-1.0, AccelerationUnit.METER_PER_SECOND_2),
                            new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2)));

            LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(
                            new LaneBasedGTUFollowingTacticalPlannerFactory(new IDMPlusOld()));

            CrossSectionLink L2a = (CrossSectionLink) this.network.getLink("L2a");
            Lane L2a_A2 = (Lane) L2a.getCrossSectionElement("A2");
            Lane L2a_A3 = (Lane) L2a.getCrossSectionElement("A3");
            new GTUGeneratorIndividual("L2a_A2", this.simulator, carType, LaneBasedIndividualGTU.class, initialSpeedDist,
                    interarrivelTimeDist, lengthDist, widthDist, maximumSpeedDist, maxGTUs, startTime, endTime, L2a_A2,
                    new Length(10.0, LengthUnit.METER), GTUDirectionality.DIR_PLUS, gtuColorer, strategicalPlannerFactory,
                    new FixedRouteGenerator(null), this.network);
            new GTUGeneratorIndividual("L2a_A3", this.simulator, carType, LaneBasedIndividualGTU.class, initialSpeedDist,
                    interarrivelTimeDist, lengthDist, widthDist, maximumSpeedDist, maxGTUs, startTime, endTime, L2a_A3,
                    new Length(10.0, LengthUnit.METER), GTUDirectionality.DIR_PLUS, gtuColorer, strategicalPlannerFactory,
                    new FixedRouteGenerator(null), this.network);

            CrossSectionLink L49b = (CrossSectionLink) this.network.getLink("L49b");
            Lane L49b_A1 = (Lane) L49b.getCrossSectionElement("A1");
            Lane L49b_A2 = (Lane) L49b.getCrossSectionElement("A2");
            new GTUGeneratorIndividual("L49b_A1", this.simulator, carType, LaneBasedIndividualGTU.class, initialSpeedDist,
                    interarrivelTimeDist, lengthDist, widthDist, maximumSpeedDist, maxGTUs, startTime, endTime, L49b_A1,
                    new Length(10.0, LengthUnit.METER), GTUDirectionality.DIR_PLUS, gtuColorer, strategicalPlannerFactory,
                    new FixedRouteGenerator(null), this.network);
            new GTUGeneratorIndividual("L49b_A2", this.simulator, carType, LaneBasedIndividualGTU.class, initialSpeedDist,
                    interarrivelTimeDist, lengthDist, widthDist, maximumSpeedDist, maxGTUs, startTime, endTime, L49b_A2,
                    new Length(10.0, LengthUnit.METER), GTUDirectionality.DIR_PLUS, gtuColorer, strategicalPlannerFactory,
                    new FixedRouteGenerator(null), this.network);
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestGMModel [simulator=" + this.simulator + "]";
        }

    }

    /**
     * Convert coordinates to/from the Dutch RD system.
     */
    class CoordinateTransformRD implements CoordinateTransform, Serializable
    {
        /** */
        private static final long serialVersionUID = 20141017L;

        /** */
        final double dx;

        /** */
        final double dy;

        /**
         * @param dx x transform
         * @param dy y transform
         */
        public CoordinateTransformRD(final double dx, final double dy)
        {
            this.dx = dx;
            this.dy = dy;
        }

        /** {@inheritDoc} */
        @Override
        public float[] floatTransform(double x, double y)
        {
            double[] d = doubleTransform(x, y);
            return new float[] { (float) d[0], (float) d[1] };
        }

        /** {@inheritDoc} */
        @Override
        public double[] doubleTransform(double x, double y)
        {
            try
            {
                Coords c = WGS84ToRDNewTransform.ellipswgs842rd(x, y);
                return new double[] { c.x - this.dx, c.y - this.dy };
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                return new double[] { 0, 0 };
            }
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "CoordinateTransformRD [dx=" + this.dx + ", dy=" + this.dy + "]";
        }
    }

    /**
     * <p>
     * Copyright (c) 2011 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. The
     * source code and binary code of this software is proprietary information of Delft University of Technology. Specific
     * MathTransform for WGS84 WGS84 (EPSG:4326) to RD_new (EPSG:28992) conversions. Code based on C code by Peter Knoppers as
     * applied <a href="http://www.regiolab-delft.nl/?q=node/36">here</a>, which is based on
     * <a href="http://www.dekoepel.nl/pdf/Transformatieformules.pdf">this</a> paper.
     * @author Gert-Jan Stolk
     **/
    public static class WGS84ToRDNewTransform
    {

        /** */
        private static final long serialVersionUID = 20141017L;

        //@formatter:off
        private static final double r[][] = { /* p down, q right */
            {  155000.00, 190094.945,   -0.008, -32.391, 0.0   , },
            {     -0.705, -11832.228,    0.0  ,   0.608, 0.0   , },
            {      0.0  ,   -114.221,    0.0  ,   0.148, 0.0   , },
            {      0.0  ,      2.340,    0.0  ,   0.0  , 0.0   , },
            {      0.0  ,      0.0  ,    0.0  ,   0.0  , 0.0   , }};
        private static final double s[][] = { /* p down, q right */
            { 463000.00 ,      0.433, 3638.893,   0.0  ,  0.092, },
            { 309056.544,     -0.032, -157.984,   0.0  , -0.054, },
            {     73.077,      0.0  ,   -6.439,   0.0  ,  0.0  , },
            {     59.788,      0.0  ,    0.0  ,   0.0  ,  0.0  , },
            {      0.0  ,      0.0  ,    0.0  ,   0.0  ,  0.0  , }};
        //@formatter:on

        public static void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) throws Exception
        {
            int offsetDelta = dstOff - srcOff;
            for (int i = srcOff; i < srcOff + numPts && i + 1 < srcPts.length && i + offsetDelta + 1 < dstPts.length; i += 2)
            {
                Coords transformedCoords = ellipswgs842rd(srcPts[i], srcPts[i + 1]);
                dstPts[i + offsetDelta] = transformedCoords.x;
                dstPts[i + offsetDelta + 1] = transformedCoords.y;
            }
        }

        private static Coords ellipswgs842rd(double EW, double NS)
        {
            Coords result = new Coords(0, 0);
            int p;
            double pown = 1;
            double dn = 0.36 * (NS - 52.15517440);
            double de = 0.36 * (EW - 5.38720621);
            if (NS <= 50 || NS >= 54 || EW <= 3 || (EW >= 8))
            {
                System.err.println("Error: ellipswgs842rd input out of range (" + EW + ", " + NS + ")");
            }

            for (p = 0; p < 5; p++)
            {
                double powe = 1;
                int q;

                for (q = 0; q < 5; q++)
                {
                    result.x += r[p][q] * powe * pown;
                    result.y += s[p][q] * powe * pown;
                    powe *= de;
                }
                pown *= dn;
            }
            return result;
        }

        /**
         * Coordinate pair.
         */
        static class Coords implements Serializable
        {
            /** */
            private static final long serialVersionUID = 20141017L;

            public double x, y;

            public Coords(double x, double y)
            {
                this.x = x;
                this.y = y;
            }

            /** {@inheritDoc} */
            @Override
            public final String toString()
            {
                return "Coords [x=" + this.x + ", y=" + this.y + "]";
            }

        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "WGS84ToRDNewTransform []";
        }

    }

}
