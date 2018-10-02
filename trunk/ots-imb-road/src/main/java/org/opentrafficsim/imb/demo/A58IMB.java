package org.opentrafficsim.imb.demo;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.ContinuousProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.TransformWGS84DutchRDNew;
import org.opentrafficsim.core.gtu.GTUException;
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
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class A58IMB extends AbstractWrappableAnimation
{
    /** */
    private static final long serialVersionUID = 20161007L;

    /** The model. */
    private A58Model model;

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
                    A58IMB a58Model = new A58IMB();
                    List<Property<?>> propertyList = new ArrayList<>();
                    propertyList.add(OTSIMBConnector.standardIMBProperties(0, "vps17642.public.cloudvps.com"));
                    // 1 hour simulation run for testing
                    a58Model.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(10.0, DurationUnit.HOUR), propertyList, null,
                            true);
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
        return "Model A58";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "Model A58 - IMB";
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
        System.out.println("A58IMB.makeModel called");
        this.model = new A58Model(getSavedUserModifiedProperties(), getColorer(), new OTSNetwork("A58 network"));
        return this.model;
    }

    /**
     * @return the saved user properties for a next run
     */
    private List<Property<?>> getSavedUserModifiedProperties()
    {
        return this.savedUserModifiedProperties;
    }

    /** {@inheritDoc} */
    @Override
    protected void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
        this.addToggleGISButtonText(" GIS Layers:", this.model.getGisMap(), "Turn GIS map layer on or off");
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
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
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
        private OTSSimulatorInterface simulator;

        /** User settable properties. */
        private List<Property<?>> modelProperties = null;

        /** the network as created by the AbstractWrappableIMBAnimation. */
        private final OTSNetwork network;

        /** the GIS map. */
        private GisRenderable2D gisMap;

        /** Connector to the IMB hub. */
        OTSIMBConnector imbConnector;

        /**
         * @param modelProperties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the properties
         * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
         * @param network Network; the network
         */
        A58Model(final List<Property<?>> modelProperties, final GTUColorer gtuColorer, final OTSNetwork network)
        {
            this.modelProperties = modelProperties;
            this.network = network;
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> pSimulator)
                throws SimRuntimeException
        {
            System.out.println("A58IMB: constructModel called; Connecting to IMB");
            this.simulator = (OTSSimulatorInterface) pSimulator;
            SimpleAnimator imbAnimator = (SimpleAnimator) pSimulator;
            try
            {
                CompoundProperty imbSettings = null;
                for (Property<?> property : this.modelProperties)
                {
                    if (property.getKey().equals(OTSIMBConnector.PROPERTY_KEY))
                    {
                        imbSettings = (CompoundProperty) property;
                    }
                }
                System.out.println("link count " + this.network.getLinkMap().size());
                Throw.whenNull(imbSettings, "IMB Settings not found in properties");
                this.imbConnector = OTSIMBConnector.create(imbSettings, "OTS");
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
            InputStream stream = URLResource.getResourceAsStream("/A58v2.xml");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            try
            {
                nlp.build(stream, this.network, true);
                // ODMatrixTrips matrix = A58ODfactory.get(network);
                // A58ODfactory.makeGeneratorsFromOD(network, matrix, this.simulator);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException | ValueException | ParameterException exception)
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
        public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
        {
            return this.simulator;
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

    /**
     * Retrieve a list of properties that the user can modify.
     * @return List&lt;Property&lt;?&gt;&gt;;
     */
    public List<Property<?>> getSupportedProperties()
    {
        List<Property<?>> result = new ArrayList<>();
        result.add(new ContinuousProperty("penetration", "penetration", "<html>Fraction of vehicles equipped with CACC</html>",
                0.0, 0.0, 1.0, "%.2f", false, 13));
        return result;
    }

}
