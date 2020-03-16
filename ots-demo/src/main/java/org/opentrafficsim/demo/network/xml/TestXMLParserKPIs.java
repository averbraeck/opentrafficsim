package org.opentrafficsim.demo.network.xml;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
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
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.CoordinateTransformWGS84toRDNew;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.kpi.sampling.indicator.MeanSpeed;
import org.opentrafficsim.kpi.sampling.indicator.MeanTravelTimePerDistance;
import org.opentrafficsim.kpi.sampling.indicator.MeanTripLength;
import org.opentrafficsim.kpi.sampling.indicator.TotalDelay;
import org.opentrafficsim.kpi.sampling.indicator.TotalNumberOfStops;
import org.opentrafficsim.kpi.sampling.indicator.TotalTravelDistance;
import org.opentrafficsim.kpi.sampling.indicator.TotalTravelTime;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.data.SpeedLimit;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestXMLParserKPIs extends OTSSimulationApplication<OTSModelInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestXMLParserKPIs(final OTSModelInterface model, final OTSAnimationPanel animationPanel) throws OTSDrawingException
    {
        super(model, animationPanel);
    }

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
                    OTSAnimator simulator = new OTSAnimator("TestXMLParserKPIs");
                    TestXMLModelKPIs xmlModel = new TestXMLModelKPIs(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), xmlModel);
                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(xmlModel.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, xmlModel, DEFAULT_COLORER, xmlModel.getNetwork());
                    new TestXMLParserKPIs(xmlModel, animationPanel);
                }
                catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | DSOLException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
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
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class TestXMLModelKPIs extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the network. */
        private OTSRoadNetwork network;

        /**
         * @param simulator the simulator
         */
        TestXMLModelKPIs(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            URL xmlURL = URLResource.getResource("/xml/N201.xml");
            this.network = new OTSRoadNetwork("Example network", true);
            try
            {
                XmlNetworkLaneParser.build(xmlURL, this.network, getSimulator(), false);

                for (TrafficLight tl : this.network.getObjectMap(TrafficLight.class).values())
                {
                    tl.setTrafficLightColor(TrafficLightColor.GREEN);
                }

                // ODMatrixTrips matrix = N201ODfactory.get(network);
                // N201ODfactory.makeGeneratorsFromOD(network, matrix, this.simulator);
                RoadSampler sampler = new RoadSampler(this.simulator, new Frequency(10.0, FrequencyUnit.SI));
                sampler.registerExtendedDataType(new SpeedLimit());
                Query query = N201ODfactory.getQuery(this.network, sampler);
                scheduleKpiEvent(30.0, this.simulator, query);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | GTUException | OTSGeometryException
                    | JAXBException | URISyntaxException | XmlParserException | IOException | TrafficControlException exception)
            {
                exception.printStackTrace();
            }

            URL gisURL = URLResource.getResource("/xml/N201/map.xml");
            System.err.println("GIS-map file: " + gisURL.toString());
            CoordinateTransform rdto0 = new CoordinateTransformWGS84toRDNew(0, 0);
            new GisRenderable2D(this.simulator, gisURL, rdto0);
        }

        /** {@inheritDoc} */
        @Override
        public OTSRoadNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestXMLModel [simulator=" + this.simulator + "]";
        }

        TotalTravelDistance totalTravelDistance = new TotalTravelDistance();

        TotalTravelTime totalTravelTime = new TotalTravelTime();

        MeanSpeed meanSpeed = new MeanSpeed(this.totalTravelDistance, this.totalTravelTime);

        MeanTravelTimePerDistance meanTravelTimePerDistance = new MeanTravelTimePerDistance(this.meanSpeed);

        MeanTripLength meanTripLength = new MeanTripLength();

        TotalDelay totalDelay = new TotalDelay(new Speed(80.0, SpeedUnit.KM_PER_HOUR));

        TotalNumberOfStops totalNumberOfStops = new TotalNumberOfStops();

        public <G extends GtuDataInterface> void publishKpis(double time, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
                final Query<G> query)
        {
            Time t = new Time(time, TimeUnit.BASE_SECOND);
            List<TrajectoryGroup<G>> groups = query.getTrajectoryGroups(t);
            Length tdist = this.totalTravelDistance.getValue(query, t, groups);
            Duration ttt = this.totalTravelTime.getValue(query, t, groups);
            Speed ms = this.meanSpeed.getValue(query, t, groups);
            Duration mttpdist = this.meanTravelTimePerDistance.getValue(query, t, groups);
            Length mtl = this.meanTripLength.getValue(query, t, groups);
            Duration tdel = this.totalDelay.getValue(query, t, groups);
            Dimensionless nos = this.totalNumberOfStops.getValue(query, t, groups);
            System.out.println("===== @time " + time + " s =====");
            System.out.println("Total distance " + tdist);
            System.out.println("Total travel time " + ttt);
            System.out.println("Mean speed " + ms);
            System.out.println("Mean travel time " + mttpdist + " (per m)");
            System.out.println("Mean trip length " + mtl);
            System.out.println("Total delay " + tdel);
            System.out.println("Number of stops " + nos);
            scheduleKpiEvent(time + 30, simulator, query);
        }

        public void scheduleKpiEvent(double time, final DEVSSimulatorInterface.TimeDoubleUnit simulator, final Query<?> query)
        {
            try
            {
                simulator.scheduleEventAbs(new Time(time, TimeUnit.BASE_SECOND), this, this, "publishKpis",
                        new Object[] {time, simulator, query});
            }
            catch (SimRuntimeException exception)
            {
                throw new RuntimeException("Cannot schedule KPI event.", exception);
            }
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "TestXMLModelKPIs";
        }
    }

}
