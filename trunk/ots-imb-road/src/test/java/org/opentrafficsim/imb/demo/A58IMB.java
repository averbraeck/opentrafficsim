package org.opentrafficsim.imb.demo;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.TransformWGS84DutchRDNew;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.OTSIMBConnector;
import org.opentrafficsim.imb.demo.A58IMB.A58Model;
import org.opentrafficsim.imb.transceiver.urbanstrategy.GTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.LaneGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.LinkGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.NetworkTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.NodeTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.SensorGTUTransceiver;
import org.opentrafficsim.imb.transceiver.urbanstrategy.SimulatorTransceiver;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.swing.gui.inputparameters.TabbedParameterDialog;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class A58IMB extends OTSSimulationApplication<A58Model>
{
    /** */
    private static final long serialVersionUID = 20161007L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public A58IMB(final A58Model model, final OTSAnimationPanel animationPanel) throws OTSDrawingException
    {
        super(model, animationPanel);
    }

    /** {@inheritDoc} */
    @Override
    protected void setAnimationToggles()
    {
        super.setAnimationToggles();
        getAnimationPanel().addAllToggleGISButtonText(" GIS Layers:", getModel().getGisMap(), "Turn GIS map layer on or off");
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        demo(true);
    }

    /**
     * Start the demo.
     * @param exitOnClose boolean; when running stand-alone: true; when running as part of a demo: false
     */
    public static void demo(final boolean exitOnClose)
    {
        try
        {
            OTSAnimator simulator = new OTSAnimator();
            final A58Model otsModel = new A58Model(simulator);
            if (TabbedParameterDialog.process(otsModel.getInputParameterMap()))
            {
                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), otsModel);
                OTSAnimationPanel animationPanel = new OTSAnimationPanel(otsModel.getNetwork().getExtent(),
                        new Dimension(800, 600), simulator, otsModel, new DefaultSwitchableGTUColorer(), otsModel.getNetwork());
                A58IMB app = new A58IMB(otsModel, animationPanel);
                app.setExitOnClose(exitOnClose);
            }
            else
            {
                if (exitOnClose)
                {
                    System.exit(0);
                }
            }
        }
        catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected final Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(150000, 385000, 5500, 5000);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "A58 network - IMB []";
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
    static class A58Model extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the network as created by the AbstractWrappableIMBAnimation. */
        private final OTSRoadNetwork network = new OTSRoadNetwork("network", true);

        /** the GIS map. */
        private GisRenderable2D gisMap;

        /** Connector to the IMB hub. */
        OTSIMBConnector imbConnector;

        /**
         * @param simulator the simulator for this model
         */
        public A58Model(final OTSSimulatorInterface simulator)
        {
            super(simulator);
            InputParameterHelper.makeInputParameterMapIMB(this.inputParameterMap);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            System.out.println("A58IMB: constructModel called; Connecting to IMB");
            try
            {
                InputParameterMap imbSettings = (InputParameterMap) getInputParameterMap().get("imb");
                Throw.whenNull(imbSettings, "IMB Settings not found in properties");
                this.imbConnector = OTSIMBConnector.create(imbSettings, "A58IMB");
                new NetworkTransceiver(this.imbConnector, getSimulator(), this.network);
                new NodeTransceiver(this.imbConnector, getSimulator(), this.network);
                new LinkGTUTransceiver(this.imbConnector, getSimulator(), this.network);
                new LaneGTUTransceiver(this.imbConnector, getSimulator(), this.network);
                new GTUTransceiver(this.imbConnector, getSimulator(), this.network);
                new SensorGTUTransceiver(this.imbConnector, getSimulator(), this.network);
                new SimulatorTransceiver(this.imbConnector, getSimulator());
            }
            catch (IMBException | InputParameterException exception)
            {
                throw new SimRuntimeException(exception);
            }

            // Stream to allow the xml-file to be retrievable from a JAR file
            InputStream stream = URLResource.getResourceAsStream("/A58v2.xml");
            try
            {
                XmlNetworkLaneParser.build(stream, this.network, this.simulator, false);
                // ODMatrixTrips matrix = A58ODfactory.get(network);
                // A58ODfactory.makeGeneratorsFromOD(network, matrix, this.simulator);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | GTUException | OTSGeometryException
                    | ValueRuntimeException | JAXBException | URISyntaxException | XmlParserException exception)
            {
                exception.printStackTrace();
            }
            // TODO
            // Query query = N201ODfactory.getQuery(this.network, new Sampler(this.simulator));
            // try
            // {
            // new StatisticsGTULaneTransceiver(this.imbConnector, imbAnimator, this.network, query,
            // new Duration(30, TimeUnit.SECOND));
            // }
            // catch (IMBException exception)
            // {
            // throw new SimRuntimeException(exception);
            // }

            // TODO
            URL gisURL = URLResource.getResource("/A58/map.xml");
            System.err.println("GIS-map file: " + gisURL.toString());
            CoordinateTransform rdto0 = new CoordinateTransformRD(0, 0);
            this.gisMap = new GisRenderable2D(this.simulator, gisURL, rdto0);
            // URL nwbURL = URLResource.getResource("/A58/nwb.xml");
            // System.err.println("NWB-map file: " + nwbURL.toString());
            // new GisRenderable2D(this.simulator, nwbURL);
        }

        /**
         * @return gisMap
         */
        public final GisRenderable2D getGisMap()
        {
            return this.gisMap;
        }

        /** {@inheritDoc} */
        @Override
        public final OTSNetwork getNetwork()
        {
            return this.network;
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
    static class CoordinateTransformRD implements CoordinateTransform, Serializable
    {
        /** */
        private static final long serialVersionUID = 20141017L;

        /** */
        final double dx;

        /** */
        final double dy;

        /**
         * @param dx double; x transform
         * @param dy double; y transform
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
            return new float[] {(float) d[0], (float) d[1]};
        }

        /** {@inheritDoc} */
        @Override
        public double[] doubleTransform(double x, double y)
        {
            try
            {
                Point2D c = TransformWGS84DutchRDNew.fromWGS84(x, y);
                return new double[] {c.getX() - this.dx, c.getY() - this.dy};
            }
            catch (Exception exception)
            {
                System.err.println(exception.getMessage());
                return new double[] {0, 0};
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
