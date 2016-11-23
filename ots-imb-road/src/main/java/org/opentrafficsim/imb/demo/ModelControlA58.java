package org.opentrafficsim.imb.demo;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.TransformWGS84DutchRDNew;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.OTSIMBConnector;
import org.opentrafficsim.imb.transceiver.urbanstrategy.GTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.LaneGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.LinkGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.NetworkTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.NodeTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.SensorGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.SimulatorTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.StatisticsGTULaneTransceiver;
import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.meta.MetaDataGtuType;
import org.opentrafficsim.kpi.sampling.meta.MetaDataSet;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.sampling.GtuTypeData;
import org.opentrafficsim.road.network.sampling.LinkData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.data.ReferenceSpeed;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tno.imb.TConnection;
import nl.tno.imb.mc.ModelParameters;
import nl.tno.imb.mc.ModelStarter;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.Simulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ModelControlA58 extends ModelStarter
{

    /** Amount of ACC. */
    private double penetrationRate;

    A58Model model;

    A58Animation a58Animation;

    /**
     * @param args the command line args
     * @param providedModelName the model name
     * @param providedModelId the model id
     * @throws IMBException when IMB connection fails
     */
    public ModelControlA58(String[] args, String providedModelName, int providedModelId) throws IMBException
    {
        super(args, providedModelName, providedModelId);
    }

    /**
     * Tester.
     * @param args empty
     * @throws IMBException shen IMB connection fails
     */
    public static void main(String[] args) throws IMBException
    {
        ModelControlA58 modelControlA58 = new ModelControlA58(new String[0], "A58 model", 1248);
        modelControlA58.startModel(null, modelControlA58.connection); // null will use default penetration rate

//        try
//        {
//            Thread.sleep(2000);
//        }
//        catch (InterruptedException exception)
//        {
//            exception.printStackTrace();
//        }
//        modelControlA58.doStopModel();
    }

    /** {@inheritDoc} */
    @Override
    public void startModel(ModelParameters parameters, TConnection imbConnection)
    {
        this.penetrationRate = 0.1;
        if (parameters != null && parameters.parameterExists("penetration"))
        {
            try
            {
                this.penetrationRate = ((double) parameters.getParameterValue("penetration")) / 100;
            }
            catch (IMBException exception)
            {
                // should not happen, we check for parameter existence
                exception.printStackTrace();
            }
        }
        else
        {
            System.out.println("No penetration parameter found, using default value of " + this.penetrationRate);
        }

        this.a58Animation = new A58Animation(imbConnection);
        try
        {
            this.a58Animation.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(1, TimeUnit.HOUR), null, null, true);
        }
        catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
        {
            exception.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    @Override
    public void stopModel()
    {
        System.out.println("stopModel called");
        if (null != this.model)
        {
            ((Simulator<Time, Duration, OTSSimTimeDouble>) this.model.getSimulator()).cleanUp();
            this.model.closeWindow();
            this.model = null;
        }
        else
        {
            System.err.println("stopModel called, but no model is running");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void quitApplication()
    {
        if (null != this.model)
        {
            try
            {
                // clean up; even if stopModel was not called before quitApplication
                ((Simulator<Time, Duration, OTSSimTimeDouble>) this.model.getSimulator()).cleanUp();
            }
            catch (Exception exception)
            {
                System.out.println("caught Exception in quitApplication:");
                exception.printStackTrace();
            }
        }
        System.out.println("quitApplication called");
    }

    /** {@inheritDoc} */
    @Override
    public void parameterRequest(ModelParameters parameters)
    {
        //
    }

    /**
     * @return penetrationRate
     */
    public double getPenetrationRate()
    {
        return this.penetrationRate;
    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class A58Animation extends AbstractWrappableAnimation
    {

        /** */
        private static final long serialVersionUID = 20161118L;

        /**
         * 
         */
        TConnection imbConnection;

        /**
         * @param imbConnection IMB connection
         */
        public A58Animation(TConnection imbConnection)
        {
            this.imbConnection = imbConnection;
        }

        /** {@inheritDoc} */
        @Override
        public String shortName()
        {
            return "Model A58";
        }

        /** {@inheritDoc} */
        @Override
        public String description()
        {
            return "Model A58 - IMB";
        }

        /** {@inheritDoc} */
        @Override
        protected OTSModelInterface makeModel(GTUColorer colorer) throws OTSSimulationException
        {
            ModelControlA58.this.model = new A58Model(this.imbConnection, colorer, new OTSNetwork("A58 network"));
            return ModelControlA58.this.model;
        }

        /** {@inheritDoc} */
        @Override
        protected Double makeAnimationRectangle()
        {
            return new Rectangle2D.Double(150000, 385000, 5500, 5000);
        }

    }

    /**
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
    class A58Model implements OTSModelInterface
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** IMB connection. */
        private TConnection imbConnection;

        /** Colorer for GTU's. */
        private GTUColorer gtuColorer;

        /** the network as created by the AbstractWrappableIMBAnimation. */
        private final OTSNetwork network;

        /** Connector to the IMB hub. */
        OTSIMBConnector imbConnector;

        /**
         * @param imbConnection the connection to the IMB bus
         * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
         * @param network Network; the network
         */
        A58Model(TConnection imbConnection, final GTUColorer gtuColorer, final OTSNetwork network)
        {
            this.imbConnection = imbConnection;
            this.gtuColorer = gtuColorer;
            this.network = network;
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> pSimulator)
                throws SimRuntimeException
        {
            this.simulator = (OTSDEVSSimulatorInterface) pSimulator;
            SimpleAnimator imbAnimator = (SimpleAnimator) pSimulator;
            try
            {
                this.imbConnector = new OTSIMBConnector(this.imbConnection);
                new NetworkTransceiver(this.imbConnector, imbAnimator, this.network);
                new NodeTransceiver(this.imbConnector, imbAnimator, this.network);
                new LinkGTUTransceiver(this.imbConnector, imbAnimator, this.network);
                new LaneGTUTransceiver(this.imbConnector, imbAnimator, this.network);
                new GTUTransceiver(this.imbConnector, imbAnimator, this.network);
                new SensorGTUTransceiver(this.imbConnector, imbAnimator, this.network);
                new SimulatorTransceiver(this.imbConnector, imbAnimator);
            }
            catch (IMBException exception)
            {
                throw new SimRuntimeException(exception);
            }

            // Stream to allow the xml-file to be retrievable from a JAR file
            InputStream stream = URLResource.getResourceAsStream("/A58v1.xml");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            try
            {
                nlp.build(stream, this.network);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

            Sampler sampler = new RoadSampler(this.simulator);
            sampler.registerExtendedDataType(new ReferenceSpeed());
            // Query query = N201ODfactory.getQuery(this.network, new RoadSampler(this.simulator));
            try
            {
                MetaDataSet metaDataSet;
                Query query;
                Set<GtuTypeDataInterface> gtuTypes;

                query = getQuery(this.network, sampler, new MetaDataSet(), "All");
                new StatisticsGTULaneTransceiver(this.imbConnector, imbAnimator, this.network.getId(), query,
                        new Duration(30, TimeUnit.SECOND));

                metaDataSet = new MetaDataSet();
                gtuTypes = new HashSet<>();
                gtuTypes.add(new GtuTypeData(new GTUType("car_equipped")));
                gtuTypes.add(new GtuTypeData(new GTUType("truck_equipped")));
                metaDataSet.put(new MetaDataGtuType("gtuType"), gtuTypes);
                query = getQuery(this.network, sampler, metaDataSet, "Equipped");
                new StatisticsGTULaneTransceiver(this.imbConnector, imbAnimator, this.network.getId(), query,
                        new Duration(30, TimeUnit.SECOND));

                metaDataSet = new MetaDataSet();
                gtuTypes = new HashSet<>();
                gtuTypes.add(new GtuTypeData(new GTUType("car")));
                gtuTypes.add(new GtuTypeData(new GTUType("truck")));
                metaDataSet.put(new MetaDataGtuType("gtuType"), gtuTypes);
                query = getQuery(this.network, sampler, metaDataSet, "Not equipped");
                new StatisticsGTULaneTransceiver(this.imbConnector, imbAnimator, this.network.getId(), query,
                        new Duration(30, TimeUnit.SECOND));

                metaDataSet = new MetaDataSet();
                gtuTypes = new HashSet<>();
                gtuTypes.add(new GtuTypeData(new GTUType("car")));
                gtuTypes.add(new GtuTypeData(new GTUType("car_equipped")));
                metaDataSet.put(new MetaDataGtuType("gtuType"), gtuTypes);
                query = getQuery(this.network, sampler, metaDataSet, "Cars");
                new StatisticsGTULaneTransceiver(this.imbConnector, imbAnimator, this.network.getId(), query,
                        new Duration(30, TimeUnit.SECOND));

                metaDataSet = new MetaDataSet();
                gtuTypes = new HashSet<>();
                gtuTypes.add(new GtuTypeData(new GTUType("truck")));
                gtuTypes.add(new GtuTypeData(new GTUType("truck_equipped")));
                metaDataSet.put(new MetaDataGtuType("gtuType"), gtuTypes);
                query = getQuery(this.network, sampler, metaDataSet, "Trucks");
                new StatisticsGTULaneTransceiver(this.imbConnector, imbAnimator, this.network.getId(), query,
                        new Duration(30, TimeUnit.SECOND));

            }
            catch (IMBException exception)
            {
                throw new SimRuntimeException(exception);
            }

            // URL gisURL = URLResource.getResource("/A58/map.xml");
            // System.err.println("GIS-map file: " + gisURL.toString());
            // CoordinateTransform rdto0 = new CoordinateTransformRD(0, 0);
            // new GisRenderable2D(this.simulator, gisURL, rdto0);

            A58OdUtil.createDemand(this.network, this.gtuColorer, this.simulator, ModelControlA58.this.getPenetrationRate());

        }

        private Query getQuery(final OTSNetwork network, final Sampler sampler, final MetaDataSet metaDataSet, final String id)
        {
            Query query = new Query(sampler, id, id, metaDataSet, new Frequency(2.0, FrequencyUnit.PER_MINUTE));
            for (String link : network.getLinkMap().keySet())
            {
                query.addSpaceTimeRegionLink(new LinkData((CrossSectionLink) network.getLink(link)),
                        KpiGtuDirectionality.DIR_PLUS, Length.ZERO, network.getLink(link).getLength(), new Time(0, TimeUnit.SI),
                        new Time(1.0, TimeUnit.HOUR));
            }
            return query;
        }

        /**
         * Close and destroy the window. Please shut down and cleanup the simulator first.
         */
        @SuppressWarnings("unchecked")
        public void closeWindow()
        {
            try
            {
                ((Simulator<Time, Duration, OTSSimTimeDouble>) this.simulator).cleanUp();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
            ((JFrame) ModelControlA58.this.a58Animation.getPanel().getParent().getParent().getParent()).dispose();
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
            return "A58Model [simulator=" + this.simulator + "]";
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
                Point2D c = TransformWGS84DutchRDNew.fromWGS84(x, y);
                return new double[] { c.getX() - this.dx, c.getY() - this.dy };
            }
            catch (Exception exception)
            {
                System.err.println(exception.getMessage());
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

}
