package org.opentrafficsim.road.network.factory.xml.test;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.CoordinateTransformWGS84toRDNew;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.indicator.MeanSpeed;
import org.opentrafficsim.kpi.sampling.indicator.MeanTravelTimePerKm;
import org.opentrafficsim.kpi.sampling.indicator.MeanTripLength;
import org.opentrafficsim.kpi.sampling.indicator.TotalDelay;
import org.opentrafficsim.kpi.sampling.indicator.TotalNumberOfStops;
import org.opentrafficsim.kpi.sampling.indicator.TotalTravelDistance;
import org.opentrafficsim.kpi.sampling.indicator.TotalTravelTime;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.SpeedLimit;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestXMLParserKPIs extends AbstractWrappableAnimation
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
                    TestXMLParserKPIs xmlParserKPIs = new TestXMLParserKPIs();
                    // 1 hour simulation run for testing
                    xmlParserKPIs.buildAnimator(new Time(0.0, TimeUnit.SECOND), new Duration(0.0, TimeUnit.SECOND),
                            new Duration(60.0, TimeUnit.MINUTE), new ArrayList<Property<?>>(), null, true);
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
        return "TestXMLModel";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "TestXMLModel";
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts(final SimpleSimulatorInterface simulator)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        return new TestXMLModelKPIs();
    }

    /** {@inheritDoc} */
    @Override
    protected final Double makeAnimationRectangle()
    {
        // return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
        return new Rectangle2D.Double(104000, 482000, 5000, 5000);
        // return new Rectangle2D.Double(0, 0, 5000, 5000);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TestXMLParser []";
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class TestXMLModelKPIs implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** {@inheritDoc} */
        @Override
        public final void constructModel(
                final SimulatorInterface<Time, Duration, OTSSimTimeDouble> pSimulator)
                throws SimRuntimeException
        {
            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;
            // URL url = URLResource.getResource("/PNH1.xml");
            // URL url = URLResource.getResource("/offset-example.xml");
            // URL url = URLResource.getResource("/circular-road-new-gtu-example.xml");
            // URL url = URLResource.getResource("/straight-road-new-gtu-example_2.xml");
            // URL url = URLResource.getResource("/Circuit.xml");
            URL url = URLResource.getResource("/N201v8.xml");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            OTSNetwork network;
            try
            {
                network = nlp.build(url);
                // ODMatrixTrips matrix = N201ODfactory.get(network);
                // N201ODfactory.makeGeneratorsFromOD(network, matrix, this.simulator);
                RoadSampler sampler = new RoadSampler(this.simulator, new Frequency(10.0, FrequencyUnit.SI));
                sampler.registerExtendedDataType(new SpeedLimit());
                Query query =
                        N201ODfactory.getQuery(network, sampler);
                scheduleKpiEvent(30.0, this.simulator, query);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

            URL gisURL = URLResource.getResource("/N201/map.xml");
            System.err.println("GIS-map file: " + gisURL.toString());
            CoordinateTransform rdto0 = new CoordinateTransformWGS84toRDNew(0, 0);
            new GisRenderable2D(this.simulator, gisURL, rdto0);
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()
        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestXMLModel [simulator=" + this.simulator + "]";
        }

    }

    TotalTravelDistance totalTravelDistance = new TotalTravelDistance();

    TotalTravelTime totalTravelTime = new TotalTravelTime();

    MeanSpeed meanSpeed = new MeanSpeed(this.totalTravelDistance, this.totalTravelTime);

    MeanTravelTimePerKm meanTravelTimePerKm = new MeanTravelTimePerKm(this.meanSpeed);

    MeanTripLength meanTripLength = new MeanTripLength();

    TotalDelay totalDelay = new TotalDelay(new Speed(80.0, SpeedUnit.KM_PER_HOUR));

    TotalNumberOfStops totalNumberOfStops = new TotalNumberOfStops();

    public void publishKpis(double time, final OTSDEVSSimulatorInterface simulator, final Query query)
    {
        Length tdist = this.totalTravelDistance.getValue(query, new Time(time, TimeUnit.SI));
        Duration ttt = this.totalTravelTime.getValue(query, new Time(time, TimeUnit.SI));
        Speed ms = this.meanSpeed.getValue(query, new Time(time, TimeUnit.SI));
        Duration mttpkm = this.meanTravelTimePerKm.getValue(query, new Time(time, TimeUnit.SI));
        Length mtl = this.meanTripLength.getValue(query, new Time(time, TimeUnit.SI));
        Duration tdel = this.totalDelay.getValue(query, new Time(time, TimeUnit.SI));
        Dimensionless nos = this.totalNumberOfStops.getValue(query, new Time(time, TimeUnit.SI));
        System.out.println("===== @time " + time + " s =====");
        System.out.println("Total distance " + tdist);
        System.out.println("Total travel time " + ttt);
        System.out.println("Mean speed " + ms);
        System.out.println("Mean travel time " + mttpkm + " (per km)");
        System.out.println("Mean trip length " + mtl);
        System.out.println("Total delay " + tdel);
        System.out.println("Number of stops " + nos);
        scheduleKpiEvent(time + 30, simulator, query);
    }

    public void scheduleKpiEvent(double time, final OTSDEVSSimulatorInterface simulator, final Query query)
    {
        try
        {
            simulator.scheduleEventAbs(new Time(time, TimeUnit.SI), this, this, "publishKpis",
                    new Object[] { time, simulator, query });
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot schedule KPI event.", exception);
        }
    }
}
